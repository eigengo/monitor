package org.eigengo.monitor.agent.akka;

import akka.actor.ActorPath;
import scala.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains logic for tagging actor paths; used in tagging actor's lifecycle and performance
 * of the {@code receive} method.
 */
public class ActorPathTagger extends AbstractTagger {
    static final Option<String> ANONYMOUS_ACTOR_CLASS_NAME = Option.empty();

    /**
     * Constructs this instance, assigns the {@code includeRoutees} field.
     *
     * @param includeRoutees {@code true} if the tagging should include the routees
     * @see org.eigengo.monitor.agent.akka.AbstractTagger#addActorPathTagsTo(akka.actor.ActorPath, java.util.List)
     */
    ActorPathTagger(boolean includeRoutees) {
        super(includeRoutees);
    }

    /**
     * Computes the tags for the given {@code actorPath} and {@code actor} instances.
     *
     * @param actorPath the actor path; never {@code null}
     * @param actorClassName the actor instance; may be {@code null}
     * @return non-{@code null} array of tags
     */
    final String[] getTags(final ActorPath actorPath, final Option<String> actorClassName) {
        List<String> tags = new ArrayList<String>(4);

        addActorPathTagsTo(actorPath, tags);
        addSystemTagsTo(actorPath, tags);
        addTypeTagsTo(actorPath, actorClassName, tags);

        return tags.toArray(new String[tags.size()]);
    }
}
