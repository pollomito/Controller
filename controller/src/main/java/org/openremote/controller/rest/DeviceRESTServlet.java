/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2015, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.rest;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openremote.controller.Constants;
import org.openremote.controller.component.LevelSensor;
import org.openremote.controller.component.RangeSensor;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.model.Command;
import org.openremote.controller.model.event.CustomState;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.Event;
import org.openremote.controller.service.DeviceService;
import org.openremote.controller.spring.SpringContext;
import org.openremote.controller.utils.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Dispatcher servlet that is responsible for processing all kinds of '/rest/devices/...'
 * REST API requests.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class DeviceRESTServlet extends RESTAPI
{

  // Class Members --------------------------------------------------------------------------------

  public static boolean isDeviceRequest(HttpServletRequest request, int segmentCount, String expectedVerb)
  {
    boolean isRequest = false;

    String pathInfo = request.getPathInfo();

    if (pathInfo != null)
    {
      StringTokenizer st = new StringTokenizer(pathInfo, "/");

      List<String> segments = new ArrayList<String>(3);

      while (st.hasMoreElements())
      {
        segments.add(st.nextToken());
      }

      if (expectedVerb != null)
      {
        if (segmentCount == segments.size())
        {
          String actualVerb = null;

          if ("polling".equals(expectedVerb))
          {
            // [HTTP GET]  /rest/devices/{device_name}/polling/{device_id}?name={sensor_name}&name={sensor_name}

            actualVerb = segments.get(segmentCount - 2);
          }
          else
          {
            // [HTTP GET]  /rest/devices/{device_name}/status?name={sensor_name}&name={sensor_name}

            actualVerb = segments.get(segmentCount - 1);
          }

          if (expectedVerb.equals(actualVerb))
          {
            isRequest = true;
          }
        }
      }
      else
      {
        if (segmentCount == segments.size())
        {
          // [HTTP GET]  /rest/devices/{device_name}
          // [HTTP GET]  /rest/devices/

          isRequest = true;
        }
      }
    }
    else
    {
      if (segmentCount == 0)
      {
        // [HTTP GET]  /rest/devices

        isRequest = true;
      }
    }

    return isRequest;
  }

  private final static Logger log = Logger.getLogger(Constants.HTTP_REST_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  private DeviceService deviceService =
      (DeviceService) SpringContext.getInstance().getBean("deviceService");


  // Implements RESTAPI ---------------------------------------------------------------------------

  @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
  {
    String queryString = request.getQueryString() != null ?
        request.getQueryString() : "";

    String fullRequestString = request.getRequestURI() +
        (queryString.length() > 0 ? "?" : "") + queryString;

    try
    {
      if (isDeviceRequest(request, 2, "commands"))
      {
        // [HTTP POST] /rest/devices/{device_name}/commands?name={command_name}

        CommandRESTServlet cmdServlet = new CommandRESTServlet();
        cmdServlet.handleRequest(request, response);
      }
      else if (isDeviceRequest(request, 2, "status"))
      {
        // [HTTP GET]  /rest/devices/{device_name}/status?name={sensor_name}&name={sensor_name}

        StatusCommandByNameRESTServlet statusServlet = new StatusCommandByNameRESTServlet();
        statusServlet.handleRequest(request, response);
      }
      else if (isDeviceRequest(request, 3, "polling"))
      {
        // [HTTP GET]  /rest/devices/{device_name}/polling/{device_id}?name={sensor_name}&name={sensor_name}

        StatusPollingRESTServlet pollingServlet = new StatusPollingRESTServlet();
        pollingServlet.handleRequest(request, response);
      }
      else if (isDeviceRequest(request, 1, null))
      {
        // [HTTP GET]  /rest/devices/{device_name}

        handleDeviceRequest(request, response);
      }
      else if (isDeviceRequest(request, 0, null))
      {
        // [HTTP GET]  /rest/devices

        handleDeviceListRequest(request, response);
      }
      else
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_BAD_REQUEST, // 400
            ControllerRESTAPIException.ERROR_CODE_INVALID_URL,
            "The command call was rejected by the controller because of an invalid request URL.",
            "Bad request URL : '" + fullRequestString  + "'."
        );
      }
    }
    catch(ControllerRESTAPIException e)
    {
      log.error(
          "Failed to execute REST API call '" + fullRequestString + "' : " + e.getDeveloperMsg()
      );

      sendErrorResponse(e, request, response);
    }
  }

  // Private Instance Methods ---------------------------------------------------------------------

  private void handleDeviceRequest(HttpServletRequest request, HttpServletResponse response)
      throws ControllerRESTAPIException
  {
    // [HTTP GET]  /rest/devices/{device_name}

    String deviceName = deviceNameFromRequest(request);

    List<Integer> deviceIDs = deviceService.getDeviceIDs(deviceName);

    if (deviceIDs.size() == 0)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_NOT_FOUND, // 404
          ControllerRESTAPIException.ERROR_CODE_DEVICE_NOT_FOUND,
          "The command call was rejected by the controller because the addressed device could not be found.",
          "Unknown device '" + deviceName + "'."
      );
    }

    if (deviceIDs.size() > 1)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_CONFLICT, // 409
          ControllerRESTAPIException.ERROR_CODE_DEVICE_AMBIGUITY,
          "The command call was rejected by the controller because of a device ambiguity error.",
          "Device name ambiguity error - a device with the name '" + deviceName +
          "' exists more than once."
      );
    }

    int deviceID = deviceIDs.get(0);

    List<Command> commands = deviceService.getCommands(deviceID);
    List<Sensor> sensors = deviceService.getSensors(deviceID);

    ResponseType responseType = (ResponseType)request.getAttribute("responseType");
    boolean isJSON = (responseType == ResponseType.APPLICATION_JSON);

    if (isJSON)
    {
      sendJSONDeviceReponse(response, deviceID, deviceName, commands, sensors);
    }

    else
    {
      sendXMLDeviceResponse(response, deviceID, deviceName, commands, sensors);
    }
  }

  private void handleDeviceListRequest(HttpServletRequest request, HttpServletResponse response)
      throws ControllerRESTAPIException
  {
    // [HTTP GET]  /rest/devices

    ResponseType responseType = (ResponseType)request.getAttribute("responseType");
    boolean isJSON = (responseType == ResponseType.APPLICATION_JSON);

    if (isJSON)
    {
      sendJSONDeviceListReponse(response);
    }
    
    else
    {
      sendXMLDeviceListResponse(response);
    }
  }

  private void sendJSONDeviceReponse(HttpServletResponse response, int deviceID,
                                     String deviceName, List<Command> commands, List<Sensor> sensors)
      throws ControllerRESTAPIException
  {
    JSONObject deviceObj = new JSONObject();
    String jsonStr = null;

    try
    {
      deviceObj.put("id", Integer.toString(deviceID));
      deviceObj.put("name", deviceName);

      JSONArray cmdsArray = new JSONArray();
      deviceObj.put("commands", cmdsArray);

      for (Command curCmd : commands)
      {
        JSONObject cmdObj = new JSONObject();
        cmdObj.put("id", Integer.toString(curCmd.getID()));
        cmdObj.put("name", curCmd.getName());
        cmdObj.put("protocol", curCmd.getProtocolType());

        cmdsArray.put(cmdObj);

        JSONArray tagArray = new JSONArray();

        for (String curTag : curCmd.getTags())
        {
          tagArray.put(curTag);
        }

        if (tagArray.length() > 0)
        {
          cmdObj.put("tags", tagArray);
        }
      }

      JSONArray sensorArray = new JSONArray();
      deviceObj.put("sensors", sensorArray);

      for (Sensor curSensor : sensors)
      {
        String sensorType = getSensorType(curSensor);

        if (sensorType == null)
        {
          // TODO : log unknown sensor type

          continue;
        }

        JSONObject sensObj = new JSONObject();
        sensObj.put("type", sensorType);
        sensObj.put("id", Integer.toString(curSensor.getSensorID()));
        sensObj.put("name", curSensor.getName());
        sensObj.put("command_id", Integer.toString(curSensor.getCommandID()));

        addSensorProperties(sensObj, curSensor);

        sensorArray.put(sensObj);
      }

      int INDENT_FACTOR = 2;
      jsonStr = deviceObj.toString(INDENT_FACTOR);
    }

    catch (JSONException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "Failed to create JSON HTTP response content : '" +
          e.getMessage() + "'."
      );
    }

    try
    {
      PrintWriter writer = response.getWriter();

      writer.print(jsonStr);
      writer.flush();
    }

    catch(IOException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "IOException while writing JSON HTTP response content  : '"
          + e.getMessage() + "'."
      );
    }
  }

  private void sendJSONDeviceListReponse(HttpServletResponse response)
      throws ControllerRESTAPIException
  {
    JSONArray deviceArray = new JSONArray();
    String jsonStr = null;

    try
    {
      List<Integer> deviceIDs = deviceService.getDeviceIDs();

      for (Integer curDeviceID : deviceIDs)
      {
        String deviceName = deviceService.getDeviceName(curDeviceID);

        if (deviceName == null)
        {
          continue;
        }

        JSONObject deviceObj = new JSONObject();
        deviceObj.put("id", curDeviceID);
        deviceObj.put("name", deviceName);

        deviceArray.put(deviceObj);
      }

      int INDENT_FACTOR = 2;
      jsonStr = deviceArray.toString(INDENT_FACTOR);
    }

    catch (JSONException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "Failed to create JSON HTTP response content : '" +
          e.getMessage() + "'."
      );
    }

    try
    {
      PrintWriter writer = response.getWriter();

      writer.print(jsonStr);
      writer.flush();
    }

    catch(IOException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "IOException while writing JSON HTTP response content  : '"
          + e.getMessage() + "'."
      );
    }
  }

  private void sendXMLDeviceResponse(HttpServletResponse response, int deviceID,
                                     String deviceName, List<Command> commands, List<Sensor> sensors)
      throws ControllerRESTAPIException
  {
    Document doc = new Document();

    Element root = new Element("device");
    root.setAttribute("id", Integer.toString(deviceID));
    root.setAttribute("name", deviceName);
    doc.addContent(root);

    Element eleCmds = new Element("commands");
    root.addContent(eleCmds);

    for (Command curCmd : commands)
    {
      Element eleCmd = new Element("command");
      eleCmd.setAttribute("id", Integer.toString(curCmd.getID()));
      eleCmd.setAttribute("name", curCmd.getName());
      eleCmd.setAttribute("protocol", curCmd.getProtocolType());

      eleCmds.addContent(eleCmd);

      for (String curTag : curCmd.getTags())
      {
        Element eleTag = new Element("tag");
        eleTag.setText(curTag);

        eleCmd.addContent(eleTag);
      }
    }

    Element eleSensors = new Element("sensors");
    root.addContent(eleSensors);

    for (Sensor curSensor : sensors)
    {
      String sensorType = getSensorType(curSensor);

      if (sensorType == null)
      {
        // TODO : log unknown sensor type

        continue;
      }

      Element eleSens = new Element("sensor");
      eleSens.setAttribute("id", Integer.toString(curSensor.getSensorID()));
      eleSens.setAttribute("type", sensorType);
      eleSens.setAttribute("name", curSensor.getName());
      eleSens.setAttribute("command_id", Integer.toString(curSensor.getCommandID()));

      addSensorProperties(eleSens, curSensor);

      eleSensors.addContent(eleSens);
    }

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

    StringWriter stringWriter = new StringWriter();

    try
    {
      outputter.output(doc, stringWriter);

      PrintWriter printWriter = response.getWriter();

      printWriter.print(stringWriter.toString());
      printWriter.flush();
    }

    catch(IOException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "IOException while writing XML HTTP response content  : '"
          + e.getMessage() + "'."
      );
    }
  }

  private void sendXMLDeviceListResponse(HttpServletResponse response)
      throws ControllerRESTAPIException
  {
    Document doc = new Document();

    Element root = new Element("devices");
    doc.addContent(root);

    List<Integer> deviceIDs = deviceService.getDeviceIDs();

    for (Integer curDeviceID : deviceIDs)
    {
      String deviceName = deviceService.getDeviceName(curDeviceID);

      if (deviceName == null)
      {
        continue;
      }

      Element device = new Element("device");
      device.setAttribute("id", Integer.toString(curDeviceID));
      device.setAttribute("name", deviceName);

      root.addContent(device);
    }

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

    StringWriter stringWriter = new StringWriter();

    try
    {
      outputter.output(doc, stringWriter);

      PrintWriter printWriter = response.getWriter();

      printWriter.print(stringWriter.toString());
      printWriter.flush();
    }

    catch(IOException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "IOException while writing XML HTTP response content  : '"
              + e.getMessage() + "'."
      );
    }
  }

  private void addSensorProperties(Element eleSensor, Sensor sensor)
  {
    if (sensor.getClass() == RangeSensor.class)
    {
      Element eleMin = new Element("min");
      eleMin.setAttribute("value", Integer.toString(((RangeSensor)sensor).getMinValue()));
      eleSensor.addContent(eleMin);

      Element eleMax = new Element("max");
      eleMax.setAttribute("value", Integer.toString(((RangeSensor) sensor).getMaxValue()));
      eleSensor.addContent(eleMax);
    }

    else if(sensor.getClass() == StateSensor.class)
    {
      Map<String, String> sensorProps = sensor.getProperties();

      for (String curPropName : sensorProps.keySet())
      {
        if (curPropName.startsWith("state-"))
        {
          String value = sensorProps.get(curPropName);

          if(value == null)
          {
            continue;
          }

          Event event = ((StateSensor)sensor).processEvent(value);

          if (event instanceof CustomState)
          {
            String mappedValue = ((CustomState)event).getValue();

            if (mappedValue == null)
            {
              continue;
            }

            Element eleState = new Element("state");
            eleState.setAttribute("name", mappedValue);
            eleSensor.addContent(eleState);
          }
        }
      }
    }
  }

  private void addSensorProperties(JSONObject sensObj, Sensor sensor) throws JSONException
  {
    if (sensor.getClass() == RangeSensor.class)
    {
      sensObj.put("min", Integer.toString(((RangeSensor)sensor).getMinValue()));
      sensObj.put("max", Integer.toString(((RangeSensor) sensor).getMaxValue()));
    }

    else if(sensor.getClass() == StateSensor.class)
    {
      JSONArray statesArray = new JSONArray();
      sensObj.put("states", statesArray);

      Map<String, String> sensorProps = sensor.getProperties();

      for (String curPropName : sensorProps.keySet())
      {
        if (curPropName.startsWith("state-"))
        {
          String value = sensorProps.get(curPropName);

          if (value == null)
          {
            continue;
          }

          Event event = ((StateSensor)sensor).processEvent(value);

          if (event instanceof CustomState)
          {
            String mappedValue = ((CustomState)event).getValue();

            if (mappedValue == null)
            {
              continue;
            }

            statesArray.put(mappedValue);
          }
        }
      }
    }
  }

  private String getSensorType(Sensor sensor)
  {
    String sensorType = null;

    if (sensor.getClass() == RangeSensor.class)
    {
      sensorType = "range";
    }

    else if (sensor.getClass() == LevelSensor.class)
    {
      sensorType = "level";
    }

    else if (sensor.getClass() == SwitchSensor.class)
    {
      sensorType = "switch";
    }

    else if (sensor.getClass() == StateSensor.class)
    {
      sensorType = "custom";
    }

    return sensorType;
  }


  private String deviceNameFromRequest(HttpServletRequest request)
  {
    // PathInfo : /{device_name}

    String pathInfo = request.getPathInfo();

    StringTokenizer st = new StringTokenizer(pathInfo, "/");
    String deviceName = st.nextToken();

    return deviceName;
  }
}
