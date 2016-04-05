/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2014, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.exception;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Exception indicating that a controller REST API call failed.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class ControllerRESTAPIException extends Exception
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Common error code that indicates that the URL couldn't be recognized as a valid URL.
   */
  public static final int ERROR_CODE_INVALID_URL = 40400;

  /**
   * Error code that indicates that the REST API call failed because the URL does not contain
   * the ID of the device that has sent the request. <p>
   *
   * '/rest/polling/{device_id}?name={sensor_name}&name={sensor_name}'
   */
  public static final int ERROR_CODE_INVALID_URL_MISSING_DEVICE_ID = 40401;

  /**
   * Error code that indicates that the REST API call failed because the HTTP query string does
   * not contain a command name parameter. <p>
   *
   * '/rest/commands?name={command_name}'
   */
  public static final int ERROR_CODE_INVALID_HTTP_QUERY_STRING = 40402;

  /**
   * Error code that indicates that the REST API call failed because the format of the HTTP
   * body is invalid and therefore it was not possible to read the command parameter.<p>
   *
   * <h3>XML Command Parameter</h3>
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
   * <h3>JSON Command Parameter</h3>
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
   */
  public static final int ERROR_CODE_INVALID_HTTP_BODY = 40403;

  /**
   * Error code that indicates that the REST API call failed because the command from the HTTP
   * query string could not be found on the controller.
   */
  public static final int ERROR_CODE_COMMAND_NOT_FOUND = 40404;

  /**
   * Error code that indicates that the REST API call failed because the device name
   * of the HTTP request is unknown.
   */
  public static final int ERROR_CODE_DEVICE_NOT_FOUND = 40405;

  /**
   * Error code that indicates that the REST API call failed because at least one sensor
   * name of the HTTP query string is unknown.
   */
  public static final int ERROR_CODE_SENSOR_NOT_FOUND = 40406;

  /**
   * Error code that indicates that the REST API call failed because the referenced
   * command name of the HTTP query string has been used more than once.
   */
  public static final int ERROR_CODE_COMMAND_AMBIGUITY = 40410;

  /**
   * Error code that indicates that the REST API call failed because the referenced
   * device name of the HTTP request has been used more than once.
   */
  public static final int ERROR_CODE_DEVICE_AMBIGUITY = 40411;

  /**
   * Error code that indicates that the REST API call failed because one referenced
   * sensor name of the HTTP query string has been used more than once.
   */
  public static final int ERROR_CODE_SENSOR_AMBIGUITY = 40412;

  /**
   * Error code that indicates that the REST API call failed because of an internal controller
   * error.
   */
  public static final int ERROR_CODE_INTERNAL_CONTROLLER_ERROR = 50500;

  /**
   * Name of the data field that contains the HTTP status code as part of the error structure.
   */
  public static final String ERROR_FIELD_NAME_STATUS_CODE = "status";

  /**
   * Name of the data field that contains the error code as part of the error structure.
   */
  public static final String ERROR_FIELD_NAME_ERROR_CODE = "code";

  /**
   * Name of the data field that contains the user friendly message as part of the error
   * structure.
   */
  public static final String ERROR_FIELD_NAME_MSG = "message";

  /**
   * Name of the data field that contains the developer oriented message as part of the error
   * structure.
   */
  public static final String ERROR_FIELD_NAME_DEV_MSG = "developerMessage";

  /**
   * Number of spaces that are used to indent JSON data.
   */
  public static final int JSON_INDENT_FACTOR = 2;


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * HTTP status code.
   */
  private int statusCode;

  /**
   * More specific error code.
   */
  private int errorCode;

  /**
   * User friendly message describing the error.
   */
  private String errorMsg;

  /**
   * More detailed developer oriented description of the error that should not be
   * presented to the end user.
   */
  private String developerMsg;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an exception instance.
   *
   * @param statusCode     HTTP status code.
   *
   * @param errorCode      more specific error code.
   *
   * @param errorMsg       user friendly message describing the error.
   *
   * @param developerMsg   more detailed developer oriented description of the error that
   *                       should not be presented to the end user.
   */
  public ControllerRESTAPIException(int statusCode, int errorCode, String errorMsg, String developerMsg)
  {
    this.statusCode = statusCode;
    this.errorCode = errorCode;
    this.errorMsg = errorMsg;
    this.developerMsg = developerMsg;
  }

  /**
   * Returns the HTTP status code.
   *
   * @return  the HTTP status code
   */
  public int getStatusCode()
  {
    return statusCode;
  }

  /**
   * Returns the more specific error code.
   *
   * @return  the error code
   */
  public int getErrorCode()
  {
    return errorCode;
  }

  /**
   * Returns the user friendly error message.
   *
   * @return  the error message
   */
  public String getErrorMsg()
  {
    return errorMsg;
  }

  /**
   * Returns the developer oriented error message.
   *
   * @return  the developer oriented error message.
   */
  public String getDeveloperMsg()
  {
    return developerMsg;
  }

  /**
   * Returns the error information in the JSON format.
   *
   * <pre>
   * {@code
   *   {
   *     "status": "400",
   *     "code": "...",
   *     "message" = "...",
   *     "developerMessage": "..."
   *   }
   * }
   * </pre>
   *
   * @return  string that contains the error information in the JSON format
   */
  public String toJSON() throws JSONException
  {
    JSONObject jso = new JSONObject();

    jso.put(ERROR_FIELD_NAME_STATUS_CODE, getStatusCode());
    jso.put(ERROR_FIELD_NAME_ERROR_CODE,  getErrorCode());
    jso.put(ERROR_FIELD_NAME_MSG,         getErrorMsg());
    jso.put(ERROR_FIELD_NAME_DEV_MSG,     getDeveloperMsg());

    return jso.toString(JSON_INDENT_FACTOR);
  }

  /**
   * Returns the error information in the XML format.
   *
   * <pre>
   * {@code
   *   {
   *     "status": "400",
   *     "code": "...",
   *     "message" = "...",
   *     "developerMessage": "..."
   *   }
   * }
   * </pre>
   *
   * @return  string that contains the error information in the XML format
   */
  public String toXML() throws IOException
  {
    Document doc = new Document();

    Element root = new Element("error");
    doc.addContent(root);

    Element eleStatus = new Element(ERROR_FIELD_NAME_STATUS_CODE);
    eleStatus.setText(Integer.toString(getStatusCode()));
    root.addContent(eleStatus);

    Element eleErrCode = new Element(ERROR_FIELD_NAME_ERROR_CODE);
    eleErrCode.setText(Integer.toString(getErrorCode()));
    root.addContent(eleErrCode);

    Element eleMsg = new Element(ERROR_FIELD_NAME_MSG);
    eleMsg.setText(getErrorMsg());
    root.addContent(eleMsg);

    Element eleDevMsg = new Element(ERROR_FIELD_NAME_DEV_MSG);
    eleDevMsg.setText(getDeveloperMsg());
    root.addContent(eleDevMsg);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    StringWriter writer = new StringWriter();

    outputter.output(doc, writer);

    return writer.toString();
  }
}
