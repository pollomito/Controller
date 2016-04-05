/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2013, OpenRemote Inc.
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
package org.openremote.controller.protocol.lagarto;

import org.junit.Test;

/**
 * TODO: LagartoCommand related tests should go here
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class LagartoCommandTest
{

  /**
   * Regression test for issue ORCJAVA-346 (http://jira.openremote.org/browse/ORCJAVA-346) <p>
   *
   * Send fails with NPE if setSensor() is not called first to initialize lagartoClient reference.
   * However, send is part of ExecutableCommand interface whereas setSensor() is part of
   * EventListener interface -- these two interfaces are treated as separate entities in the
   * system and have no known initialization order. Therefore it was possible that send() was
   * called before setSensor() was executed, yielding an NPE on the uninitialized lagartoClient
   * reference. <p>
   *
   * Fix added a simple NPE guard in the send() implementation.  This test simply executes send()
   * without ever invoking setSensor() that contains the initialization code to make sure the
   * send() fails gracefully.
   */
  @Test public void testSendNPE_ORCJAVA_346()
  {
    LagartoCommand cmd = new LagartoCommand(
        "test_network_name", "test_endpoint_id", "test_endpoint_value"
    );

    cmd.send();
  }
}

