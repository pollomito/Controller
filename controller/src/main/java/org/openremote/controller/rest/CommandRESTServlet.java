/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2014, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.rest;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.service.CommandService;
import org.openremote.controller.service.ServiceContext;
import org.openremote.controller.utils.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This servlet implements the REST API '/rest/commands?name={command_name}'
 * functionality which executes the command with the given name. <p>
 *
 * <h2>XML Request</h2>
 *
 * <pre>
 * {@code
 * POST /rest/commands?name=TV_On HTTP/1.1
 * Host: 192.168.1.1
 * Content-Length: ...
 * Content-Type: application/xml;charset=UTF-8
 * Accept: application/xml
 *
 * <?xml version="1.0" encoding="UTF-8"?>
 * <parameter>...</parameter>
 * }
 * </pre>
 *
 * <h2>JSON Request</h2>
 *
 * <pre>
 * {@code
 * POST /rest/commands?name=TV_On HTTP/1.1
 * Host: 192.168.1.1
 * Content-Length: ...
 * Content-Type: application/json;charset=UTF-8
 * Accept: application/json
 *
 * {
 *   "parameter"="..."
 * }
 * }
 * </pre>
 *
 * <h2>Response</h2>
 *
 * <pre>
 * {@code
 * HTTP/1.1 204 No Content
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
public class CommandRESTServlet extends RESTAPI
{

  // Class Members --------------------------------------------------------------------------------

  private final static Logger log = Logger.getLogger(Constants.HTTP_REST_LOG_CATEGORY);

  private final static CommandService commandService  = ServiceContext.getCommandService();


  // RESTAPI Overrides ----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
  {
    // [HTTP POST] /rest/devices/{device_name}/commands?name={command_name}

    String deviceName = null;
    String cmdName  = null;
    String cmdParam = null;

    try
    {
      deviceName = deviceNameFromRequest(request);

      cmdName  = cmdNameFromQueryString(request);

      if (isContentType(request, Constants.MIME_APPLICATION_JSON))
      {
        cmdParam = cmdParamFromJSONBody(request);
      }

      else if (isContentType(request, Constants.MIME_APPLICATION_XML))
      {
        cmdParam = cmdParamFromXMLBody(request);
      }

      commandService.execute(deviceName, cmdName, cmdParam);

      response.setStatus(
          HttpServletResponse.SC_NO_CONTENT // 204
      );
    }

    catch (ControllerRESTAPIException exc)
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


  // Private Instance Methods ---------------------------------------------------------------------

  private String deviceNameFromRequest(HttpServletRequest request)
  {
    String deviceName = "";

    if (DeviceRESTServlet.isDeviceRequest(request, 2, "commands"))
    {
      // PathInfo : /{device_name}/commands

      String pathInfo = request.getPathInfo();

      StringTokenizer st = new StringTokenizer(pathInfo, "/");
      deviceName = st.nextToken();
    }

    return deviceName;
  }

  private String cmdNameFromQueryString(HttpServletRequest request) throws ControllerRESTAPIException
  {
    String cmdName = null;

    Map<String, String[]> paramMap = request.getParameterMap();

    if (paramMap.containsKey("name") && paramMap.get("name").length > 0)
    {
      cmdName = paramMap.get("name")[0];
    }

    else
    {
      String queryString = request.getQueryString();
      if (queryString == null)
      {
        queryString = "";
      }

      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_BAD_REQUEST, // 400
          ControllerRESTAPIException.ERROR_CODE_INVALID_HTTP_QUERY_STRING,
          "The command call was rejected by the controller because of an invalid format.",
          "Invalid URL format -- expected format '/rest/devices/{device_name}/commands?name={command_name}', got '" +
          request.getServletPath() + request.getPathInfo() + (queryString.length() > 0 ? "?" : "") + queryString + "'."
      );
    }

    return cmdName;
  }

  private String cmdParamFromJSONBody(HttpServletRequest request) throws ControllerRESTAPIException
  {
    String param = null;

    String content = readBody(request);

    if (content.length() > 0)
    {
      try
      {
        JSONObject jso = new JSONObject(content);

        if (!jso.has("parameter"))
        {
          throw new ControllerRESTAPIException(
              HttpServletResponse.SC_BAD_REQUEST, // 400
              ControllerRESTAPIException.ERROR_CODE_INVALID_HTTP_BODY,
              "The command call was rejected by the controller because of an invalid format.",
              "Invalid HTTP body (JSON) -- expected '{\"parameter\": \"{value}\"}', got '" + content + "'."
          );
        }

        param = jso.getString("parameter");
      }

      catch (JSONException e)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_BAD_REQUEST, // 400
            ControllerRESTAPIException.ERROR_CODE_INVALID_HTTP_BODY,
            "The command call was rejected by the controller because of an invalid format.",
            "Invalid HTTP body (JSON) -- expected '{\"parameter\": \"{value}\"}', got '" + content + "'.'"
        );
      }
    }

    return param;
  }

  private String cmdParamFromXMLBody(HttpServletRequest request) throws ControllerRESTAPIException
  {
    String param = null;

    String content = readBody(request);

    if (content.length() > 0)
    {
      StringReader reader = new StringReader(content);

      Document doc = null;
      SAXBuilder builder = new SAXBuilder();

      try
      {
        doc = builder.build(reader);

        Element root = doc.getRootElement();

        if (root == null || !root.getName().equalsIgnoreCase("parameter"))
        {
          throw new ControllerRESTAPIException(
              HttpServletResponse.SC_BAD_REQUEST, // 400
              ControllerRESTAPIException.ERROR_CODE_INVALID_HTTP_BODY,
              "The command call was rejected by the controller because of an invalid format.",
              "Invalid HTTP body (XML) -- expected '<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
              "<parameter>{value}</parameter>', got '" + content + "'."
          );
        }

        param = root.getTextTrim();
      }

      catch (JDOMException e)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_BAD_REQUEST, // 400
            ControllerRESTAPIException.ERROR_CODE_INVALID_HTTP_BODY,
            "The command call was rejected by the controller because of an invalid format.",
            "Invalid HTTP body (XML) -- expected '<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<parameter>{value}</parameter>', got '" + content + "'."
        );
      }

      catch (IOException e)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
            ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
            "Internal controller error.",
            "IOException while parsing the content (XML) of the HTTP request body : '" +
            e.getMessage() + "'."
        );
      }
    }

    return param;
  }

  private String readBody(HttpServletRequest request) throws ControllerRESTAPIException
  {
    StringBuffer buffer = new StringBuffer();
    String line = null;

    try
    {
      BufferedReader reader = request.getReader();

      while ((line = reader.readLine()) != null)
      {
        buffer.append(line);
      }
    }

    catch(IOException e)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, // 500
          ControllerRESTAPIException.ERROR_CODE_INTERNAL_CONTROLLER_ERROR,
          "Internal controller error.",
          "IOException while reading HTTP request body : '" +
          e.getMessage() + "'."
      );
    }

    return buffer.toString().trim();
  }

  private boolean isContentType(HttpServletRequest request, String type)
  {
    boolean isType = false;

    String contentType = request.getContentType();

    if (contentType != null)
    {
      String[] typeArray = contentType.split(";");

      for (String curType : typeArray)
      {
        if (curType.trim().equalsIgnoreCase(type))
        {
          isType = true;
          break;
        }
      }
    }

    return isType;
  }
}
