/*
 * OpenRemote, the Home of the Digital Home.
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.openremote.controller.Constants;
import org.openremote.controller.config.ControllerXMLChangedException;
import org.openremote.controller.exception.ControlCommandException;
import org.openremote.controller.exception.ControllerException;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.exception.NoSuchComponentException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.service.ServiceContext;
import org.openremote.controller.service.StatusPollingByNameService;
import org.openremote.controller.service.StatusPollingService;
import org.openremote.controller.spring.SpringContext;
import org.openremote.controller.statuscache.StatusCache;
import org.openremote.controller.utils.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This servlet implements the REST API '/rest/polling/{device_id}/{sensor_id},{sensor_id}' and
 * '/rest/polling/{device_id}?name={sensor_name}&name={sensor_name}' functionality which
 * returns an XML or JSON document with the modified sensor values. <p>
 *
 * <h1>By Sensor ID</h1>
 *
 * <h2>XML Request</h2>
 *
 * <pre>
 * {@code
 * GET /rest/polling/a8d9c1/241601,240581 HTTP/1.1
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
 * <openremote xmlns="http://www.openremote.org" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *  xsi:schemaLocation="http://www.openremote.org http://www.openremote.org/schemas/controller.xsd">
 *   <status id="241601">12</status>
 * </openremote>
 * }
 * </pre>
 *
 * <h2>XML Response (error)</h2>
 *
 * <pre>
 * {@code
 * HTTP/1.1 200 OK
 * Content-Type: application/xml;charset=UTF-8
 *
 * <?xml version="1.0" encoding="UTF-8"?>
 * <openremote xmlns="http://www.openremote.org" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *  xsi:schemaLocation="http://www.openremote.org http://www.openremote.org/schemas/controller.xsd">
 *   <error>
 *     <code>504</code>
 *     <message>...</message>
 *   </error>
 * </openremote>
 * }
 * </pre>
 *
 * <h1>By Sensor Name</h1>
 *
 * <h2>XML Request</h2>
 *
 * <pre>
 * {@code
 * GET /rest/polling/a8d9c1?name=TV_Power&name=DVD_Power HTTP/1.1
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
 * GET /rest/polling/a8d9c1?name=TV_Power&name=DVD_Power HTTP/1.1
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
 * @author Handy.Wang 2009-10-19
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class StatusPollingRESTServlet extends RESTAPI {

  // TODO : add appropriate subcategory to logging
  private final static Logger logger = Logger.getLogger(Constants.HTTP_REST_LOG_CATEGORY);


  private StatusCache deviceStateCache = ServiceContext.getDeviceStateCache();

   /** This service is responsible for observe statuses change and return the changed statuses(xml-formatted). */
   private StatusPollingService statusPollingService =
      (StatusPollingService) SpringContext.getInstance().getBean("statusPollingService");
			
  private StatusPollingByNameService statusPollingByNameService =
     (StatusPollingByNameService) SpringContext.getInstance().getBean("statusPollingByNameService");


  // RESTAPI Overrides ----------------------------------------------------------------------------

   /**
    * {@inheritDoc}
    */
   @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
   {
      if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
         sendResponse(request, response, ControlCommandException.INVALID_POLLING_URL, "Invalid polling url:" + request.getRequestURL());
         return;
      }

      if (isPollingByName(request) || DeviceRESTServlet.isDeviceRequest(request, 3, "polling"))
      {
        handleRequestByName(request, response);

        return;
      }

      String regexp = "\\/(.*?)\\/(.*)";
      Pattern pattern = Pattern.compile(regexp);
      Matcher matcher = pattern.matcher(request.getPathInfo());
      String unParsedSensorIDs;

      if (matcher.find()) {
         String deviceID = matcher.group(1);
         if (deviceID == null || "".equals(deviceID)) {
            sendResponse(request, response, ControlCommandException.INVALID_POLLING_URL, "Device id was null");
         }
         unParsedSensorIDs = matcher.group(2);
         try {
            checkSensorId(unParsedSensorIDs);
            String pollingResults = statusPollingService.queryChangedState(deviceID, unParsedSensorIDs);
            if (pollingResults != null && !"".equals(pollingResults)) {
               if (Constants.SERVER_RESPONSE_TIME_OUT.equalsIgnoreCase(pollingResults)) {
                  sendResponse(request, response, 504, "Time out");
               } else {
                  logger.info("Return the polling status.");
                  sendResponse(request, response, pollingResults);
               }
            } else {
               sendResponse(request, response, 504, "Time out");
            }
            logger.info("Finished polling at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
         } catch (ControllerXMLChangedException e) {
            logger.info("Controller XML file changed while polling status, this can be ignored...");
            sendResponse(request, response, e.getErrorCode(), e.getMessage());
         } catch (ControllerException e) {
            logger.error("ControllerException occurs", e);
            sendResponse(request, response, e.getErrorCode(), e.getMessage());
         }
      } else {
          sendResponse(request, response, ControlCommandException.INVALID_POLLING_URL, "Invalid polling url:" + request.getRequestURL());
      }
   }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * check whether the sensor id is valid.
   *
   * @param unParsedSensorIDs
   */
  private void checkSensorId(String unParsedSensorIDs)
  {
    String[] sensorIDs = (unParsedSensorIDs == null || "".equals(unParsedSensorIDs)) ? new String[] {}
          : unParsedSensorIDs.split(Constants.STATUS_POLLING_SENSOR_IDS_SEPARATOR);

    if (sensorIDs.length == 0)
    {
      throw new NullPointerException("Polling ids were null.");
    }

    String tmpStr = null;

    try
    {
      for (int i = 0; i < sensorIDs.length; i++)
      {
        tmpStr = sensorIDs[i];
        deviceStateCache.queryStatus(Integer.parseInt(tmpStr));
      }
    }

    catch (NumberFormatException e)
    {
       throw new NoSuchComponentException("Wrong sensor id :'"+tmpStr+"' The sensor id can only be digit");
    }
  }

  /**
   * Checks whether the servlet REST API '/rest/polling/{device_id}/{sensor_id},{sensor_id}' or
   * '/rest/polling/{device_id}?name={sensor_name}&name={sensor_name}' is requested.
   *
   * @param request  the http request.
   *
   * @return  <tt>true</tt> if the servlet REST API
   *         '/rest/polling/{device_id}?name={sensor_name}&name={sensor_name}' is requested,
   *         <tt>false</tt> otherwise
   */
  private boolean isPollingByName(HttpServletRequest request)
  {
    String pathInfo = request.getPathInfo();

    int count = 0;

    // pathInfo for polling by name : '/{device_id}'

    if (pathInfo != null)
    {
      StringTokenizer st = new StringTokenizer(pathInfo, "/");

      while (st.hasMoreTokens())
      {
        st.nextToken();
        ++count;
      }
    }

    return (count <= 1);
  }

  /**
   * Handles the servlet REST API '/rest/polling/{device_id}?name={sensor_name}&name={sensor_name}' or
   * '/rest/devices/{device_name}/polling/{device_id}?name={sensor_name}&name={sensor_name}'
   * functionality.
   *
   * @param request   the HTTP request
   *
   * @param response  the HTTP response
   *
   * @throws org.openremote.controller.exception.ControllerRESTAPIException  in case of an invalid URL
   */
  private void handleRequestByName(HttpServletRequest request, HttpServletResponse response)
  {
    try
    {
      Set<String> sensorNames = sensorNamesFromQueryString(request);
      Map<String, Integer> name2IDMap = null;
      String deviceID = null;

      if (DeviceRESTServlet.isDeviceRequest(request, 3, "polling"))
      {
        // /rest/devices/{device_name}/polling/{device_id}?name={sensor_name}&name={sensor_name}

        String deviceName = deviceNameFromRequest(request);

        name2IDMap = statusPollingByNameService.getSensorIDsFromNames(
            deviceName, sensorNames
        );

        deviceID = getDeviceIDFromDeviceRequest(request);
      }

      else
      {
        // /rest/polling/{device_id}?name={sensor_name}&name={sensor_name}

        name2IDMap = statusPollingByNameService.getSensorIDsFromNames(
            sensorNames
        );

        deviceID = getDeviceID(request);
      }

      String sensorIDsAsString = getSensorIDsAsString(
          name2IDMap.values()
      );

      if ("".equals(sensorIDsAsString))
      {
        sendSensorValueResponse(request, response, new HashMap<String, String>());

        return;
      }

      String pollingResults = statusPollingService.queryChangedState(deviceID, sensorIDsAsString);

      if (pollingResults != null && !"".equals(pollingResults))
      {
        if (Constants.SERVER_RESPONSE_TIME_OUT.equalsIgnoreCase(pollingResults))
        {
          sendSensorValueResponse(request, response, new HashMap<String, String>());
        }
        else
        {
          Map<Integer, String> id2ValueMap = new HashMap<Integer, String>();
          Map<String, String> name2ValueMap = new HashMap<String, String>();

          id2ValueMap = getSensorValuesFromXML(pollingResults);

          for (String curSensorName : name2IDMap.keySet())
          {
            Integer sensorID = name2IDMap.get(curSensorName);

            String value = Sensor.UNKNOWN_STATUS;

            if (sensorID != null && id2ValueMap.containsKey(sensorID))
            {
              value = id2ValueMap.get(sensorID);
              name2ValueMap.put(curSensorName, value);
            }
          }

          sendSensorValueResponse(request, response, name2ValueMap);
        }
      }
      else
      {
        sendSensorValueResponse(request, response, new HashMap<String, String>());
      }
    }

    catch (ControllerRESTAPIException e)
    {
      String queryString = request.getQueryString() != null ?
                           request.getQueryString() : "";

      String requestURL = request.getRequestURL() +
                          (queryString.length() > 0 ? "?" : "") + queryString;

      logger.error(
          "Failed to execute REST API call '" + requestURL + "' : " + e.getDeveloperMsg()
      );

      sendErrorResponse(e, request, response);
    }
  }

  private String getDeviceID(HttpServletRequest request) throws ControllerRESTAPIException
  {
    String deviceID = null;

    // PathInfo : /{device_id}

    String pathInfo = request.getPathInfo();
    StringTokenizer st = null;

    if (pathInfo != null)
    {
      st = new StringTokenizer(pathInfo, "/");
    }

    if (st != null && st.hasMoreTokens())
    {
      deviceID = st.nextToken();
    }

    if (deviceID == null || "".equals(deviceID))
    {
      String queryString = request.getQueryString();
      if (queryString == null)
      {
        queryString = "";
      }

      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_BAD_REQUEST, // 400
          ControllerRESTAPIException.ERROR_CODE_INVALID_URL_MISSING_DEVICE_ID,
          "The polling request was rejected by the controller because of an invalid format.",
          "Invalid URL format -- expected format '/rest/polling/{device_id}?name={sensor_name}', got '" +
          request.getServletPath() + (queryString.length() > 0 ? "?" : "") + queryString + "'."
      );
    }

    return deviceID;
  }

  private String getDeviceIDFromDeviceRequest(HttpServletRequest request) throws ControllerRESTAPIException
  {
    String deviceID = null;

    // PathInfo : /{device_name}/polling/{device_id}

    String pathInfo = request.getPathInfo();
    StringTokenizer st = new StringTokenizer(pathInfo, "/");

    for(int i = 0; i < 3; i++)
    {
      deviceID = st.nextToken();
    }

    return deviceID;
  }

  private String getSensorIDsAsString(Collection<Integer> sensorIDs)
  {
    String sensorIDsString = "";
    boolean isFirst = true;

    for (Integer curSensorID : sensorIDs)
    {
      if (curSensorID != null)
      {
        sensorIDsString = sensorIDsString + (isFirst ? "" : ",") +  Integer.toString(curSensorID);

        isFirst = false;
      }
    }

    return sensorIDsString;
  }

  private Map<Integer, String> getSensorValuesFromXML(String pollingResultAsXML) throws ControllerRESTAPIException
  {
    Map<Integer, String> map = new HashMap<Integer, String>();

    Namespace ns = Namespace.getNamespace("http://www.openremote.org");

    SAXBuilder builder = new SAXBuilder();
    Document doc = null;

    StringReader reader = new StringReader(pollingResultAsXML);

    try
    {
      doc = builder.build(reader);
      Element root = doc.getRootElement();

      List<Element> statusList = root.getChildren("status", ns);

      for (Element curStatus : statusList)
      {
        Attribute idAttr = curStatus.getAttribute("id");

        int id = idAttr.getIntValue();
        String value = curStatus.getText();

        map.put(id, value);
      }
    }

    catch (JDOMException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "Failed to parse (XML) polling result : '" +
          e.getMessage() + "'."
      );
    }

    catch (IOException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "IOException while parsing (XML) polling result : '"
          + e.getMessage() + "'."
      );
    }

    return map;
  }

  private String deviceNameFromRequest(HttpServletRequest request)
  {
    // PathInfo : /{device_name}/polling/{device_id}

    String pathInfo = request.getPathInfo();

    StringTokenizer st = new StringTokenizer(pathInfo, "/");
    String deviceName = st.nextToken();

    return deviceName;
  }
}
