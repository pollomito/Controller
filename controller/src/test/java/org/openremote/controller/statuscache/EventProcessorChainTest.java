/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2016, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.statuscache;

import org.junit.Test;
import org.mockito.Mockito;
import org.openremote.controller.exception.InitializationException;

import static org.mockito.Mockito.*;

import java.util.Arrays;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class EventProcessorChainTest
{

  @Test
  public void testEventProcessorStartStop() throws InitializationException
  {
    EventProcessor testProcessor = Mockito.mock(EventProcessor.class);
    EventProcessorChain chain = new EventProcessorChain();
    chain.setEventProcessors(Arrays.asList(testProcessor));

    chain.start();
    verify(testProcessor, times(1)).start((LifeCycleEvent) isNotNull());

    chain.stop();
    verify(testProcessor, times(1)).start((LifeCycleEvent) isNotNull());
  }

  @Test
  public void testPushEvent() {
    EventProcessor testProcessor = Mockito.mock(EventProcessor.class);
    EventProcessorChain chain = new EventProcessorChain();
    chain.setEventProcessors(Arrays.asList(testProcessor));
    chain.start();

    EventContext mockCtx = Mockito.mock(EventContext.class);
    chain.push(mockCtx);
    verify(testProcessor, times(1)).push(mockCtx);

    chain.stop();
  }

  @Test
  /**
   * Tests that if an event processor decides to terminate the event,
   * the next processor in the chain does not receive it.
   */
  public void testEventProcessorStoppingPushedEventPropagation() {
    EventProcessor stopProcessor = spy(new EventStopEventProcessor());
    EventProcessor testProcessor = Mockito.mock(EventProcessor.class);
    EventProcessorChain chain = new EventProcessorChain();
    chain.setEventProcessors(Arrays.asList(stopProcessor, testProcessor));
    chain.start();

    EventContext mockCtx = new EventContext(null, null, null);
    chain.push(mockCtx);

    verify(stopProcessor, times(1)).push(mockCtx);
    verify(testProcessor, never()).push(mockCtx);

    chain.stop();
  }

  private class EventStopEventProcessor extends EventProcessor {
    @Override
    public void push(EventContext ctx)
    {
      ctx.terminateEvent();
    }

    @Override
    public String getName()
    {
      return "StopProcessor";
    }
  }

}
