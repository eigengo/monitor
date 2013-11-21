package org.eigengo.monitor.agent.akka;

import akka.actor.ActorPath;
import scala.Option;

import java.util.ArrayList;
import java.util.List;

public class ActorPathTagger {
    static final Option<String> ANONYMOUS_ACTOR_CLASS_NAME = Option.empty();

    private final boolean includeRoutees;

    ActorPathTagger(boolean includeRoutees) {
        this.includeRoutees = includeRoutees;
    }

    /**
     * Formats the given {@code path} in a way that can be used for tagging. For example, for
     * path <code>akka://default/user/a/b/c</code>, it returns <code>akka.path:/default/user/a/b/c</code>
     *
     * @param path the path
     * @return the tag-ready path
     */
    protected final String actorPathToString(ActorPath path) {
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
    protected final String actorSystemToString(ActorPath path) {
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
     * Computes the tags for the given {@code actorPath} and {@code actor} instances.
     *
     * @param actorPath the actor path; never {@code null}
     * @param actorClassName the actor instance; may be {@code null}
     * @return non-{@code null} array of tags
     */
    final String[] getTags(final ActorPath actorPath, final Option<String> actorClassName) {
        List<String> tags = new ArrayList<String>(3);

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

        tags.add(actorSystemToString(actorPath));
        if (actorClassName.isDefined()) {
            tags.add(String.format("akka.type:%s.%s", actorPath.address().system(), actorClassName.get()));
        }

        return tags.toArray(new String[tags.size()]);
    }
}
