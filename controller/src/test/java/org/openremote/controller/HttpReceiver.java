/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
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
package org.openremote.controller;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.DecoderException;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a receiver implementation for {@Link org.openremote.controller.TCPTestServer}
 * that allows asserting incoming HTTP request properties. It allows configuration of HTTP
 * responses to incoming requests with complete, partial, erroneous etc. responses to test
 * the requester (client) behavior with unexpected data. The incoming request handling can
 * also be adjusted to create unusual conditions: partially received requests, connection
 * closing in the middle of receiving requests, etc.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class HttpReceiver implements TCPTestServer.Receiver
{

  // Constants ------------------------------------------------------------------------------------

  private final static String HTTP_VERSION_STRING = " HTTP/1.1";


  // Instance Fields ------------------------------------------------------------------------------

  private Method method = null;
  private String path = "";
  private Headers headers = new Headers();
  private Map<String, String> responses = new HashMap<String, String>(3);
  private Map<Pattern, String> regex = new HashMap<Pattern, String>();
  private StringBuilder requestMessageBody = null;


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Adds a response object to a given path, when a specified HTTP method request is received.
   */
  public void addResponse(Method method, String path, String response)
  {
    responses.put(method.name() + " " + path.trim(), response);
  }

  /**
   * Adds a response object to a given regexp pattern. The regular expression must match the
   * HTTP path element, so for example '/something/.*'.
   */
  public void addResponse(Pattern pattern, String response)
  {
    regex.put(pattern, response);
  }

  /**
   * Returns the HTTP method of the incoming/received HTTP request.
   */
  public Method getMethod()
  {
    return method;
  }

  /**
   * Returns the 'context path' of the incoming/received HTTP request.
   */
  public String getPath()
  {
    return path;
  }

  /**
   * Returns the 'Host' header of the incoming/received HTTP request.
   */
  public String getHost()
  {
    return headers.host;
  }

  /**
   * Returns the 'Host' port of the incoming/received HTTP request.
   */
  public Integer getPort()
  {
    return headers.port;
  }

  /**
   * Returns the username of 'Authorization' header of incoming/received HTTP request, if present.
   */
  public String getUserName()
  {
    return headers.username;
  }

  /**
   * Returns the content of a given header field from the incoming/received HTTP request,
   * if present.
   */
  public String getHeader(String name)
  {
    return headers.getHeader(name);
  }

  public String getRequestMessageBody()
  {
    return (requestMessageBody == null) ? null : requestMessageBody.toString();
  }


  // TCPTestServer.Receiver Implementation --------------------------------------------------------


  private boolean readingRequestDocument = false;

  /**
   * Attempts to interpret the incoming TCP connection content as HTTP 1.1 request.
   */
  @Override public void received(String tcpString, TCPTestServer.Response response)
  {
    // if connection has no more content (likely socket closed)...

    if (tcpString == null && !readingRequestDocument)
    {
      return;
    }

    // if we don't have a HTTP method yet, attempt to resolve it from the first line of
    // the incoming TCP data (as per the HTTP specs)...

    if (method == null)
    {
      resolveMethodAndPath(tcpString);
    }

    else
    {
      if (tcpString == null && readingRequestDocument)
      {
        readingRequestDocument = false;

        respond(response);

        return;
      }

      // if we receive the empty line after method and/or headers, assume the rest is the
      // request payload

      else if (tcpString.trim().equals(""))
      {
        readingRequestDocument = true;

        if (headers.contentLength == 0)
        {
          respond(response);
        }
      }

      else if (readingRequestDocument)
      {
        if (addRequestDocumentLine(tcpString))
        {
          readingRequestDocument = false;

          TCPTestServer.log.info("MESSAGE BODY: \n" + requestMessageBody);

          respond(response);

          return;
        }
      }

      // otherwise attempt to resolve lines as HTTP headers until we reach the empty line
      // that separates request documents from headers...

      else
      {
        boolean resolvedHeader = resolveHeader(tcpString);
      }
    }

    TCPTestServer.log.info("RECEIVED: \n" + tcpString);
  }



  // Protected Instance Methods -------------------------------------------------------------------


  /**
   * Creates a HTTP 200 - OK response with a configured response document.
   */
  protected void respond(TCPTestServer.Response response)
  {
    String responseDocument = responses.get(method.name() + " " + path);

    if (responseDocument == null)
    {
      for (Pattern p : regex.keySet())
      {
        Matcher matcher = p.matcher(path);

        if (matcher.matches())
        {
          responseDocument = regex.get(p);

          break;
        }
      }
    }

    StringBuilder builder = new StringBuilder(1024);
    builder.append("HTTP/1.1 200 OK\n");
    builder.append("\n");
    builder.append(responseDocument);

    response.respond(builder.toString());
  }



  // Private Instance Methods ---------------------------------------------------------------------

  private boolean addRequestDocumentLine(String line)
  {
    if (line == null)
    {
      return true;
    }

    if (requestMessageBody == null)
    {
      requestMessageBody = new StringBuilder();
    }

    requestMessageBody.append(line);

    return requestMessageBody.length() >= headers.contentLength;
  }

  private boolean resolveHeader(String tcpString)
  {
    tcpString = tcpString.trim();

    int index = tcpString.indexOf(":");

    if (index == -1)
    {
      return false;
    }

    String header = tcpString.substring(0, index).trim();
    String value  = tcpString.substring(index + 1).trim();

    if (header.equalsIgnoreCase("Accept"))
    {
      return headers.addAcceptedContentTypes(value);
    }

    else if (header.equalsIgnoreCase("Authorization"))
    {
      if (value.startsWith("Basic"))
      {
        String authValue = value.substring("Basic ".length());

        return headers.addBasicAuthorization(authValue.trim());
      }

      else
      {
        return false;
      }
    }

    else if (header.equalsIgnoreCase("Connection"))
    {
      return headers.addConnection(value);
    }

    else if (header.equalsIgnoreCase("Host"))
    {
      return headers.addHost(value);
    }

    else if (header.equalsIgnoreCase("User-Agent"))
    {
      return headers.addUserAgent(value);
    }

    else if (header.equalsIgnoreCase("Content-Length"))
    {
      return headers.addContentLength(value);
    }

    else
    {
      return headers.addHeader(header, value);
    }
  }

  private boolean resolveMethodAndPath(String tcpString)
  {
    if (tcpString.startsWith("GET") && tcpString.toUpperCase().trim().endsWith(HTTP_VERSION_STRING))
    {
      method = Method.GET;
      path = tcpString.substring("GET".length(), tcpString.length() - HTTP_VERSION_STRING.length()).trim();

      return true;
    }

    else if (tcpString.startsWith("PUT") && tcpString.toUpperCase().trim().endsWith(HTTP_VERSION_STRING))
    {
      method = Method.PUT;
      path = tcpString.substring("PUT".length(), tcpString.length() - HTTP_VERSION_STRING.length()).trim();

      return true;
    }

    else if (tcpString.startsWith("POST") && tcpString.toUpperCase().trim().endsWith(HTTP_VERSION_STRING))
    {
      method = Method.POST;
      path = tcpString.substring("POST".length(), tcpString.length() - HTTP_VERSION_STRING.length()).trim();

      return true;
    }

    else if (tcpString.startsWith("DELETE") && tcpString.toUpperCase().trim().endsWith(HTTP_VERSION_STRING))
    {
      method = Method.DELETE;
      path = tcpString.substring("DELETE".length(), tcpString.length() - HTTP_VERSION_STRING.length()).trim();

      return true;
    }

    else if (tcpString.startsWith("PATCH") && tcpString.toUpperCase().trim().endsWith(HTTP_VERSION_STRING))
    {
      method = Method.PATCH;
      path = tcpString.substring("PATCH".length(), tcpString.length() - HTTP_VERSION_STRING.length()).trim();

      return true;
    }

    else if (tcpString.startsWith("TRACE") && tcpString.toUpperCase().trim().endsWith(HTTP_VERSION_STRING))
    {
      method = Method.TRACE;
      path = tcpString.substring("TRACE".length(), tcpString.length() - HTTP_VERSION_STRING.length()).trim();

      return true;
    }

    else
    {
      return false;
    }
  }


  // Nested Enums ---------------------------------------------------------------------------------


  public enum Method
  {
    GET, PUT, POST, DELETE, PATCH, TRACE, OPTIONS
  }


  // Nested Classes -------------------------------------------------------------------------------

  private static class Headers
  {
    private Map<String, String> headers = new HashMap<String, String>(5);
    private Set<String> mimeTypes = new HashSet<String>(3);
    private String username = "";
    private Boolean isKeepAliveConnection = false;
    private String host = "";
    private Integer port = 80;
    private String userAgent = "";
    private Integer contentLength = 0;


    private String getHeader(String name)
    {
      return headers.get(name);
    }

    private boolean addHeader(String name, String content)
    {
      if (name == null || name.equals(""))
      {
        return false;
      }

      if (content == null || content.equals(""))
      {
        return false;
      }

      this.headers.put(name.trim(), content.trim());

      return true;
    }

    private boolean addAcceptedContentTypes(String value)
    {
      boolean success = addHeader("Accept", value);

      if (!success)
      {
        return false;
      }

      String[] mimeElements = value.split(",");

      for (String mime : mimeElements)
      {
        String[] elements = mime.split(";");

        mimeTypes.add(elements[0].trim());
      }

      return true;
    }

    private boolean addBasicAuthorization(String value)
    {
      String basicAuth = "";

      try
      {
        basicAuth = new String(Base64.decode(value), Charset.forName("UTF-8"));
      }

      catch (DecoderException e)
      {
        System.err.println(value);

        e.printStackTrace();
      }

      String[] elements = basicAuth.split(":");

      if (elements.length != 2)
      {
        return false;
      }

      boolean success = addHeader("Authorization", value.trim());

      if (!success)
      {
        return false;
      }

      username = elements[0].trim();

      return true;
    }

    private boolean addConnection(String value)
    {
      boolean success = addHeader("Connection", value.trim());

      if (!success)
      {
        return false;
      }

      if (value.trim().equalsIgnoreCase("keep-alive"))
      {
        this.isKeepAliveConnection = true;
      }

      return true;
    }

    private boolean addHost(String value)
    {
      value = value.trim();

      boolean success = addHeader("Host", value);

      if (!success)
      {
        return false;
      }

      int index = value.indexOf(":");

      if (index == -1)
      {
        this.host = value;

        return true;
      }

      this.host = value.substring(0, index).trim();

      try
      {
        this.port = Integer.parseInt(value.substring(index + 1).trim());

        return true;
      }

      catch (NumberFormatException e)
      {
        return false;
      }
    }

    private boolean addUserAgent(String value)
    {
      boolean success = addHeader("User-Agent", value.trim());

      if (!success)
      {
        return false;
      }

      this.userAgent = value.trim();

      return true;
    }

    private boolean addContentLength(String value)
    {
      Integer contentLen = -1;

      try
      {
        contentLen = Integer.parseInt(value);
      }

      catch (NumberFormatException e)
      {
        return false;
      }

      if (contentLen < 0)
      {
        return false;
      }

      boolean success = addHeader("Content-Length", value.trim());

      if (!success)
      {
        return false;
      }

      this.contentLength = contentLen;

      return true;
    }

  }


}

