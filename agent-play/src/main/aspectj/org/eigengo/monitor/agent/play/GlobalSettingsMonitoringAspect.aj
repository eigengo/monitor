/*
 * Copyright (c) 2013 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eigengo.monitor.agent.play;

import org.eigengo.monitor.agent.AgentConfiguration;
import org.eigengo.monitor.output.CounterInterface;
import scala.Option;
import play.api.GlobalSettings;
import play.api.mvc.RequestHeader

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains advices for monitoring behaviour of an actor; typically imprisoned in an {@code ActorCell}.
 */
public final aspect GlobalSettingsMonitoringAspect extends AbstractMonitoringAspect issingleton() {
    private PlayAgentConfiguration agentConfiguration;
    private final CounterInterface counterInterface;

    /**
     * Constructs this aspect
     */
    public GlobalSettingMonitoringAspect() {
        AgentConfiguration<PlayAgentConfiguration> configuration = getAgentConfiguration("play", PlayAgentConfigurationJapi.apply());
        this.agentConfiguration = configuration.agent();
        this.counterInterface = createCounterInterface(configuration.common());
    }

    /**
     * Advises {@code GlobalSettings.onRouteRequest(request: RequestHeader): Option<Handler>} pointcut
     *
     * @param request the RequestHeader containing the header information the HTTP request
     */
    after(RequestHeader request) returning (Option<Handler> handler) : Pointcuts.playReceiveRequest(request) {
        // TODO: Use request to get request URL and use as tag
        // TODO: Use handler to get controller class & method and use as tag
        this.counterInterface.incrementCounter(Aspects.requestCount, new String[]);
    }

}
