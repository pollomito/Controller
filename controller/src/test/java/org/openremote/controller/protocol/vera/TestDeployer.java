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
package org.openremote.controller.protocol.vera;

import java.util.List;
import java.util.Map;

import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.deployer.ModelBuilder;
import org.openremote.controller.exception.InitializationException;
import org.openremote.controller.service.Deployer;
import org.openremote.controller.statuscache.StatusCache;
import org.openremote.devicediscovery.domain.DiscoveredDeviceDTO;

public class TestDeployer extends Deployer
{

   public TestDeployer(String serviceName, StatusCache deviceStateCache,
                       ControllerConfiguration controllerConfig, Map<String, ModelBuilder> builders)
       throws InitializationException
   {
      super(serviceName, deviceStateCache, controllerConfig, builders);
   }

   public List<DiscoveredDeviceDTO> getDiscoveredDevicesToAnnounce()
   {
      return super.discoveredDevicesToAnnounce;
   }
}
