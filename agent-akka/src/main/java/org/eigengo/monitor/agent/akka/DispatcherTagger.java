package org.eigengo.monitor.agent.akka;

import akka.actor.ActorCell;
import akka.actor.ActorPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains logic that performs tagging of the dispatcher.
 */
public class DispatcherTagger extends AbstractTagger {
    DispatcherTagger(boolean includeRoutees) {
        super(includeRoutees);
    }

    /**
     * Computes the tags for the dispatcher associated with subsequent operations on the
     * given {@code actorCell}.
     *
     * @param actorCell the ActorCell that will perform some operation later
     * @return the tags
     */
    final String[] getTags(final ActorCell actorCell) {
        final List<String> tags = new ArrayList<String>(3);
        final ActorPath actorPath = actorCell.self().path();

        addActorPathTagsTo(actorPath, tags);
        addSystemTagsTo(actorPath, tags);
        tags.add(String.format("akka.dispatcher:%s", actorCell.dispatcher().id()));

        return tags.toArray(new String[tags.size()]);

    }
}
