package org.eigengo.monitor.agent.akka;

import akka.actor.ActorCell;

public aspect MailboxSizeMonitoringAspect extends AbstractMonitoringAspect {

    before(ActorCell actorCell, Object msg): Pointcuts.receiveMessage(actorCell, msg) {
        System.out.println("$$#@$@Size " + actorCell.mailbox().numberOfMessages());
    }
}
