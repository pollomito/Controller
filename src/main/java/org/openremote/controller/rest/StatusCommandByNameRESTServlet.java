/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2014, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.rest;

import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.service.StatusCommandByNameService;
import org.openremote.controller.spring.SpringContext;
import org.openremote.controller.utils.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This servlet implements the REST API '/rest/status?name={sensor_name}&name={sensor_name}' or
 * '/rest/devices/{device_name}/status?name={sensor_name}&name={sensor_name}' functionality
 * which returns an XML or JSON document with the current values of all requested
 * sensors. <p>
 *
 * If the cache does not have a value for the given sensor,
 * '{@link org.openremote.controller.model.sensor.Sensor#UNKNOWN_STATUS}' is returned. In case
 * of an device name or sensor name ambiguity error the HTTP error code 409 is returned.
 *
 * <h2>XML Request</h2>
 *
 * <pre>
 * {@code
 * GET /rest/status?name=TV_Power&name=DVD_Power HTTP/1.1
 * Host: 192.168.1.1
 * Accept: application/xml
 * }
 * </pre>
 *
 * <h2>XML Response</h2>
 *
 * <pre>
 * {@code
 * HTTP/1.1 200 OK
 * Content-Type: application/xml;charset=UTF-8
 *
 * <?xml version="1.0" encoding="UTF-8"?>
 * <sensors>
 *   <sensor name="TV_Power">on</sensor>
 *   <sensor name="DVD_Power">off</sensor>
 * </sensors>
 * }
 * </pre>
 *
 * <h2>XML Response (error)</h2>
 *
 * <pre>
 * {@code
 * HTTP/1.1 400 Bad Request
 * Content-Type: application/xml;charset=UTF-8
 *
 * <?xml version="1.0" encoding="UTF-8"?>
 * <error>
 *   <status>400</status>
 *   <code>...</code>
 *   <message>...</message>
 *   <developerMessage>...</developerMessage>
 * </error>
 * }
 * </pre>
 *
 * <h2>JSON Request</h2>
 *
 * <pre>
 * {@code
 * GET /rest/status?name=TV_Power&name=DVD_Power HTTP/1.1
 * Host: 192.168.1.1
 * Accept: application/json
 * }
 * </pre>
 *
 * <h2>JSON Response</h2>
 *
 * <pre>
 * {@code
 * HTTP/1.1 200 OK
 * Content-Type: application/json;charset=UTF-8
 *
 * [
 *   {
 *     "name" = "TV_Power",
 *     "value" = "on"
 *   },
 *   {
 *     "name" = "DVD_Power",
 *     "value" = "off"
 *   }
 * ]
 *}
 * </pre>
 *
 * <h2>JSON Response (error)</h2>
 *
 * <pre>
 * {@code
 * HTTP/1.1 400 Bad Request
 * Content-Type: application/json;charset=UTF-8
 *
 * {
 *   "status" = "400"
 *   "code" = "..."
 *   "message" = "..."
 *   "developerMessage" = "..."
 * }
 * }
 * </pre>
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class StatusCommandByNameRESTServlet extends RESTAPI
{

  // Class Members --------------------------------------------------------------------------------

  private static StatusCommandByNameService commandService =
      (StatusCommandByNameService) SpringContext.getInstance().getBean("statusCommandByNameService");

  private final static Logger log = Logger.getLogger(Constants.HTTP_REST_LOG_CATEGORY);


  // RESTAPI Overrides ----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
  {
    try
    {
      Map<String, String> valueMap = null;

      Set<String> sensorNames = sensorNamesFromQueryString(request);

      if (DeviceRESTServlet.isDeviceRequest(request, 2, "status"))
      {
        // '/rest/devices/{device_name}/status?name={sensor_name}&name={sensor_name}'

        String deviceName = deviceNameFromRequest(request);

        valueMap = commandService.readFromCache(deviceName, sensorNames);
      }

      else
      {
        // '/rest/status?name={sensor_name}&name={sensor_name}'

        valueMap = commandService.readFromCache(sensorNames);
      }

      sendSensorValueResponse(request, response, valueMap);
    }

    catch(ControllerRESTAPIException exc)
    {
      String queryString = request.getQueryString() != null ?
                           request.getQueryString() : "";

      String requestURL = request.getRequestURL() +
                          (queryString.length() > 0 ? "?" : "") + queryString;

      log.error(
          "Failed to execute REST API call '" + requestURL + "' : " + exc.getDeveloperMsg()
      );

      sendErrorResponse(exc, request, response);
    }
  }

  private String deviceNameFromRequest(HttpServletRequest request)
  {
    // PathInfo : /{device_name}/status

    String pathInfo = request.getPathInfo();

    StringTokenizer st = new StringTokenizer(pathInfo, "/");
    String deviceName = st.nextToken();

    return deviceName;
  }
}
