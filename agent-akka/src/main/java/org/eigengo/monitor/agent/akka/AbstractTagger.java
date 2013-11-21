package org.eigengo.monitor.agent.akka;

import akka.actor.ActorPath;
import scala.Option;

import java.util.List;

/**
 * Convenience superclass for the concrete taggers. It is expected that the concrete taggers will expose
 * {@code String[] getTags(...)} method that computes the tags using appropriate parameters.
 */
abstract class AbstractTagger {
    private final boolean includeRoutees;

    AbstractTagger(boolean includeRoutees) {
        this.includeRoutees = includeRoutees;
    }

    /**
     * Formats the given {@code path} in a way that can be used for tagging. For example, for
     * path <code>akka://default/user/a/b/c</code>, it returns <code>akka.path:/default/user/a/b/c</code>
     *
     * @param path the path
     * @return the tag-ready path
     */
    private String actorPathToString(ActorPath path) {
        StringBuilder sb = new StringBuilder();
        sb.append("akka.path:/");
        sb.append(path.address().system());
        for (String element : path.getElements()) {
            sb.append("/");
            sb.append(element);
        }
        return sb.toString();
    }

    /**
     * Formats the system's portion of the given {@code path} in a way that can be used for tagging.
     * For example, for path <code>akka://default/user/a/b/c</code>,
     * it returns <code>akka.system:/default</code>
     *
     * @param path the path
     * @return the tag-ready path
     */
    private String actorSystemToString(ActorPath path) {
        StringBuilder sb = new StringBuilder();
        sb.append("akka.system:");
        sb.append(path.address().system());
        return sb.toString();
    }

    /**
     * Decides whether {@code path} represents the "root" of the user actors
     *
     * @param path the actor path
     * @return {@code true} if root of user actors
     */
    private boolean isUserRoot(ActorPath path) {
        return "user".equals(path.elements().mkString("/"));
    }

    /**
     * Computes actor path tags & adds them to the given {@code tags}.
     *
     * @param actorPath the actor path
     * @param tags the tags that will be modified with the tags
     */
    protected final void addActorPathTagsTo(final ActorPath actorPath, final List<String> tags) {
        // TODO: Improve detection of routed actors. This only detects "root" unnamed actors
        String lastPathElement = actorPath.elements().last();
        if (lastPathElement.startsWith("$")) {
            // this is routed actor.
            final ActorPath parent = actorPath.parent();
            if (isUserRoot(parent)) {
                // the parent is akka://xxx/user
                tags.add(actorPathToString(actorPath));
            } else {
                // the parent is some other actor, akka://xxx/user/foo
                tags.add(actorPathToString(parent));
                if (this.includeRoutees) tags.add(actorPathToString(actorPath));
            }
        } else {
            // there is no supervisor
            tags.add(actorPathToString(actorPath));
        }
    }

    /**
     * Computes the system tags & adds them to the given {@code tags}.
     *
     * @param actorPath the actor path
     * @param tags the tags that will be modified with the tags
     */
    protected final void addSystemTagsTo(final ActorPath actorPath, final List<String> tags) {
        tags.add(actorSystemToString(actorPath));
    }

    /**
     * Computes the type tags & adds them to the given {@code tags}.
     *
     * @param actorPath the actor path
     * @param actorClassName the optional actor class name
     * @param tags the tags that will be modified with the tags
     */
    protected final void addTypeTagsTo(final ActorPath actorPath, final Option<String> actorClassName, final List<String> tags) {
        if (actorClassName.isDefined()) {
            tags.add(String.format("akka.type:%s.%s", actorPath.address().system(), actorClassName.get()));
        }
    }

}
