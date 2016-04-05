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
package org.openremote.controller.protocol.samsungtv;

import org.jdom.Element;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.service.Deployer;

/**
 * This is a dummy placeholder for a Samsung TV implementation.
 *
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class SamsungTVRemoteCommandBuilder implements CommandBuilder
{

  private static boolean hasAnnounced = false;

  public SamsungTVRemoteCommandBuilder(Deployer d)
  {

    if (!hasAnnounced && System.getProperty("samsungTV.silent") == null)
    {
      System.out.println(
          "\n\n" +
          "******************************************************************************\n\n" +
          "  The license of some of the source code used to support \n" +
          "  Samsung TV IP control prevents its distribution in OpenRemote Professional. \n" +
          "" +
          "  To use Samsung TV control, download a freely available binary from \n" +
          "  http://sourceforge.net/projects/openremote/files/samsung and follow the \n" +
          "  installation instructions at http://www.openremote.org/x/-4YoAQ \n\n" +
          "" +
          "******************************************************************************\n\n"
      );

      hasAnnounced = true;
    }
  }

  @Override public Command build(Element el)
  {
    // no-op

    return null;
  }

}