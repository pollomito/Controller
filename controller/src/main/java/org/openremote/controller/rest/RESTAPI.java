/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2010, OpenRemote Inc.
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

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.rest.support.json.JSONTranslator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This superclass contains the common implementation elements for the OpenRemote Controller
 * HTTP/REST API.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class RESTAPI extends HttpServlet
{

  public static enum ResponseType { APPLICATION_XML, APPLICATION_JSON, TEXT_JAVASCRIPT }


  // Class Members --------------------------------------------------------------------------------

  /**
   * Common log category for HTTP REST API.
   */
  private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);


  // TODO :
  //   once all REST API servlets have been migrated to RESTAPI subclasses, this can become
  //   an instance method
  //                                                                    [JPL]
  //
  public static String composeXMLErrorDocument(int errorCode, String errorMessage)
  {
     StringBuffer sb = new StringBuffer();
     sb.append(Constants.STATUS_XML_HEADER);
     sb.append("\n<error>\n");
     sb.append("  <code>");
     sb.append(errorCode);
     sb.append("</code>\n");

     sb.append("  <message>");
     sb.append(errorMessage);
     sb.append("</message>\n");
     sb.append("</error>\n");
     sb.append(Constants.STATUS_XML_TAIL);
    
     return sb.toString();
  }

  // Servlet Implementation -----------------------------------------------------------------------
  @Override protected void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
  {
     /*
      *  This is the HTTP Method that CORS uses for it's pre-flight check just need to
      *  return headers and no content.
      */
     String acceptHeader = request.getHeader(Constants.HTTP_ACCEPT_HEADER);
     
     // Add HTML5 CORS headers
     response.addHeader("Access-Control-Allow-Origin", "*");
     response.addHeader("Access-Control-Allow-Methods", "GET, POST");
     response.addHeader("Access-Control-Allow-Headers", "origin, authorization, accept, X-HTTP-Method-Override");
     response.addHeader("Access-Control-Max-Age", "99999");
     
     if (Constants.MIME_APPLICATION_JSON.equalsIgnoreCase(acceptHeader))
     {
       response.setContentType(Constants.MIME_APPLICATION_JSON);
     }
     else if (Constants.MIME_TEXT_JAVASCRIPT.equalsIgnoreCase(acceptHeader))
     {
        response.setContentType(Constants.MIME_TEXT_JAVASCRIPT);
     }
     else
     {
       response.setContentType(Constants.MIME_APPLICATION_XML);
     }
     
     response.setStatus(200);
     response.getWriter().flush();
  }
  
  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    doPost(request, response);
  }

  @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    // Get the 'accept' header from client -- this will indicate whether we will send
    // application/xml or application/json response...     
    String acceptHeader = request.getHeader(Constants.HTTP_ACCEPT_HEADER);
    ResponseType responseType;
    
    // Set character encoding...

    response.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF8);

    // Add HTML5 CORS header
    response.addHeader("Access-Control-Allow-Origin", "*");
    
    // Set the response content type to either 'application/xml' or 'application/json'
    // according to client's 'accept' header...

    if (Constants.MIME_APPLICATION_JSON.equalsIgnoreCase(acceptHeader))
    {
      response.setContentType(Constants.MIME_APPLICATION_JSON);
      responseType = ResponseType.APPLICATION_JSON;
    }
    
    else if (Constants.MIME_TEXT_JAVASCRIPT.equalsIgnoreCase(acceptHeader))
    {
       response.setContentType(Constants.MIME_TEXT_JAVASCRIPT);
       responseType = ResponseType.TEXT_JAVASCRIPT;       
    }

    else
    {
      // Currently if we don't recognize accept type, default to 'application/xml'...

      response.setContentType(Constants.MIME_APPLICATION_XML);

      responseType = ResponseType.APPLICATION_XML;
    }
    
    // Store response type in request object
    request.setAttribute("responseType", responseType);

    try
    {
      handleRequest(request, response);
    }
    catch (Throwable t)
    {
      logger.error("Error in handling REST API response: " + t.getMessage(), t);

      response.setStatus(500);
    }
    finally
    {
      response.getWriter().flush();
    }
  }



  protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response);



  protected void sendResponse(HttpServletRequest request, HttpServletResponse response, String xml)
  {
    ResponseType responseType = ResponseType.APPLICATION_XML;
    Object obj = request.getAttribute("responseType");
    if (obj != null) {
       responseType = (ResponseType)obj;
    }
    try
    {
      switch (responseType)
      {
        case APPLICATION_JSON:
          response.getWriter().print(JSONTranslator.translateXMLToJSON(request, response, xml));
          break;
        case TEXT_JAVASCRIPT:
           // Additional JSON Formatter implemented to ensure JSONArray output where required
           String output = JSONTranslator.translateXMLToJSONP(request, response, xml);
           response.getWriter().print(output);
           break;           
        case APPLICATION_XML:     // fall through to default...
        default:

          response.getWriter().print(xml);

          break;
      }
    }
    catch (IOException e)
    {
      logger.error("Unable to write response: " + e.getMessage(), e);
    }
  }


  protected void sendResponse(HttpServletRequest request, HttpServletResponse response, int errorCode, String message)
  {
     ResponseType responseType = ResponseType.APPLICATION_XML;
     Object obj = request.getAttribute("responseType");
     if (obj != null) {
        responseType = (ResponseType)obj;
     }
    switch (responseType)
    {
      case APPLICATION_XML:

        response.setStatus(errorCode);

        break;

      case APPLICATION_JSON:

        // TODO :
        //
        //    The JSON implementation is inconsistent with regards to how to handle
        //    error responses -- on one hand HTTP return code field should be used but
        //    on other parts of the implementation HTTP OK is preferred as return
        //    code for cases where the error is returned as an document response.
        //
        //    Whatever is the correct behavior needs to be tested. No proper tests
        //    were added by the amateurs who wrote the original code (as usual).
        //    Assuming HTTP OK here for JSON responses (XML responses set the HTTP
        //    return code to match the error code as expected).
        //                                                                        [JPL]
      default:

        break;

    }

    sendResponse(request, response, composeXMLErrorDocument(errorCode, message));
  }

  /**
   * Formats the sensor values in the form of XML or JSON data and and sends the
   * data as the content of the HTTP response. <p>
   *
   * The sensor data format is as follows:
   *
   * <h2>XML Sensor Value Response</h2>
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
   * <h2>JSON Sensor Value Response</h2>
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
   *   },
   *   {
   *     "name" = "TV_Power",
   *     "value" = "on"
   *   }
   * ]
   *}
   * </pre>
   *
   * @param request   the HTTP request.
   *
   * @param response  the HTTP response.
   *
   * @param valueMap  map that contains the sensor names and sensor values
   *
   * @throws org.openremote.controller.exception.ControllerRESTAPIException  if an exception occurred while sending the sensor
   *                                     value HTTP response content
   */
  protected void sendSensorValueResponse(HttpServletRequest request, HttpServletResponse response,
                                         Map<String, String> valueMap) throws ControllerRESTAPIException
  {
    ResponseType responseType = (ResponseType)request.getAttribute("responseType");
    boolean isJSON = (responseType == ResponseType.APPLICATION_JSON);

    if (isJSON)
    {
      sendJSONReponse(response, valueMap);
    }

    else
    {
      sendXMLReponse(response, valueMap);
    }
  }

  /**
   * Sends the HTTP error response. <p>
   *
   * The format of the HTTP error response is as follows:
   *
   * <h2>XML Error Response</h2>
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
   * <h2>JSON Error Response</h2>
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
   * @param exc       the exception that contains error information
   *
   * @param request   the HTTP request.
   *
   * @param response  the HTTP response.
   */
  protected void sendErrorResponse(ControllerRESTAPIException exc,
                                   HttpServletRequest request, HttpServletResponse response)
  {
    ResponseType responseType = (ResponseType)request.getAttribute("responseType");
    boolean isJSON = (responseType == ResponseType.APPLICATION_JSON);

    String errorStr = null;
    Exception newException = null;

    try
    {
      errorStr = isJSON ? exc.toJSON() : exc.toXML();

      response.setStatus(exc.getStatusCode());

      PrintWriter writer = response.getWriter();

      writer.print(errorStr);
      writer.flush();
    }

    catch (IOException e)
    {
      newException = e;
    }

    catch(JSONException e)
    {
      newException = e;
    }

    if (newException != null)
    {
      String queryString = request.getQueryString() != null ?
          request.getQueryString() : "";

      String requestURL = request.getRequestURL() +
          (queryString.length() > 0 ? "?" : "") + queryString;

      logger.error(
          "Failed to send error response for REST API call '" + requestURL + "' : " +
          newException.getMessage()
      );
    }
  }

  /**
   * Extracts the sensor names from the HTTP query string. <p>
   *
   * The format of the HTTP query string is as follows:
   *
   * <pre>
   * {@code
   * GET /rest/polling/a8d9c1?name={sensor_name}&name={sensor_name} HTTP/1.1
   * Host: 192.168.1.1
   * Accept: application/json
   * }
   * </pre>
   *
   * @param request  the HTTP request.
   *
   * @return  returns the names of all sensors that are requested
   */
  protected Set<String> sensorNamesFromQueryString(HttpServletRequest request)
  {
    Set<String> sensorNameSet = null;

    Map<String, String[]> paramMap = request.getParameterMap();

    if (paramMap.containsKey("name"))
    {
      String[] sensorNameArray = paramMap.get("name");

      sensorNameSet = new HashSet<String>(Arrays.asList(sensorNameArray));
    }

    else
    {
      sensorNameSet = new HashSet<String>(0);
    }

    return sensorNameSet;
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private void sendJSONReponse(HttpServletResponse response, Map<String, String> valueMap)
      throws ControllerRESTAPIException
  {
    JSONArray arr = new JSONArray();
    String jsonStr = null;

    try
    {
      for (String curSensorName : valueMap.keySet())
      {
        JSONObject obj = new JSONObject();

        obj.put("name", curSensorName);
        obj.put("value", valueMap.get(curSensorName));

        arr.put(obj);
      }

      int INDENT_FACTOR = 2;
      jsonStr = arr.toString(INDENT_FACTOR);
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

  private void sendXMLReponse(HttpServletResponse response, Map<String, String> valueMap)
      throws ControllerRESTAPIException
  {
    Document doc = new Document();

    Element root = new Element("sensors");
    doc.addContent(root);

    for (String curSensorName : valueMap.keySet())
    {
      Element ele = new Element("sensor");
      root.addContent(ele);

      ele.setAttribute("name", curSensorName);
      ele.setText(valueMap.get(curSensorName));
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
}

