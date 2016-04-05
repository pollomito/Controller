/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.openremote.controller.exception.ControllerException;
import org.openremote.controller.service.StatusCommandService;
import org.openremote.controller.spring.SpringContext;

/**
 * The Class Status Command REST Servlet.
 * 
 * This servlet is responsible for 
 *   parsing RESTful url "http://xxx.xxx.xxx/controller/rest/status/{sensor_id},{sensor_id}...",
 *   building StatusCommand,
 *   status query with stateful StatusCommand,
 *   and conpose status result into XML formatted data to RESTful service client.
 * 
 * @author Handy.Wang
 */
@SuppressWarnings("serial")
public class StatusCommandRESTServlet extends RESTAPI {

    /** The logger. */
    private Logger logger = Logger.getLogger(StatusCommandRESTServlet.class.getName());

    /** The control command service. */
    private static StatusCommandService statusCommandService = (StatusCommandService) SpringContext.getInstance().getBean("statusCommandService");

   @Override
   protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {
       if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
           sendResponse(request, response, 400, "Bad REST Request, should be /rest/status/{sensor_id},{sensor_id}...");
       } else {
           String unParsedSensorIDs = request.getPathInfo().substring(1);
           try {
               sendResponse(request, response, statusCommandService.readFromCache(unParsedSensorIDs));
           } catch (ControllerException e) {
               logger.error("CommandException occurs", e);
               sendResponse(request, response, e.getErrorCode(), e.getMessage());
           }
       }
   }
}
