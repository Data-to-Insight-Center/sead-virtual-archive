package org.dataconservancy.dcs.ingest.services.rules.impl;

import org.dataconservancy.dcs.ingest.services.util.Output;
import org.drools.definition.rule.Rule;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.BeforeActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;

import java.util.logging.Logger;

/**
 * Listener for events when Drools rules are fired
 */
public class MatchmakerEventListener extends DefaultAgendaEventListener  {

    @Override
    public void beforeActivationFired(final BeforeActivationFiredEvent event) {
        final Rule rule = event.getActivation().getRule();
        final Logger log = Logger.getLogger(rule.getPackageName() + "." + rule.getName());
        log.info(event.getClass().getSimpleName());
        System.out.println(("Matchmaker matched and executed the rule "+ event.getActivation().getRule().getName()));
        Executor.outputMessages.add("Matchmaker matched and executed the rule "+ event.getActivation().getRule().getName());
     }

    @Override
    public void afterActivationFired(final AfterActivationFiredEvent event) {
        if( event.getActivation().getRule().getName().contains("Decision"))
            Executor.outputMessages.add("Final matched repository based on priorities: "+ Output.repoName);
    }
}
