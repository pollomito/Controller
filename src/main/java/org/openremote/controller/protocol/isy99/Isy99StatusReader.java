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
package org.openremote.controller.protocol.isy99;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.openremote.controller.utils.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * TODO : missing class description
 *
 * @author Kevin Purney
 */
public class Isy99StatusReader
{
  // Constants -----------------------------------------------------------------------------------

  /**
    * The maximum number of threads for the ISY-99 query class.
    */
  private final static int NUM_THREADS = 1;

  /**
    * The initial delay for the ISY-99 query class.
    */
  private final static int INITIAL_DELAY = 0;

  /**
    * The delay time between executions of the ISY-99 query class.
    */
  private final static int PERIOD_BETWEEN_EXECUTIONS = 5;

  // Class Members -------------------------------------------------------------------------------

  /**
    * Logging. Use common log category for all related classes.
    */
  private final static Logger log = Logger.getLogger(Isy99CommandBuilder.ISY99_LOG_CATEGORY);

  // Instance Fields -----------------------------------------------------------------------------
   
  /**
    * Element to hold the results of a rest query from the ISY-99.
    */
  private static Element nodesRootElement = null;

  // Implements Isy99 Status Query ---------------------------------------------------------------

  /**
   * Scheduled thread that retrieves the ISY node list periodically.
   */
  private static class QueryTask implements Runnable
  {
    // Instance Fields -------------------------------------------------------------
      
    private String host;
    private String username;
    private String password;

    private String formReadCommandUrl()
    {
      StringBuilder url = new StringBuilder();

      url.append("http://");
      url.append(this.host);
      url.append("/rest/nodes/");

      return url.toString();
    }

    // Constructors ----------------------------------------------------------------

    /**
      * @param host       hostname or IP address for the ISY-99
      * @param username   username for authentication to the ISY-99
      * @param password   password for authentication to the ISY-99
      */
    public QueryTask(String host, String username, String password)
    {
      this.host = host;
      this.username = username;
      this.password = password;
    }

    // Methods ---------------------------------------------------------------------

    public void run()
    {
      String url = formReadCommandUrl();

      DefaultHttpClient client = new DefaultHttpClient();

      if (this.username != null && !this.username.equals(""))
      {
        CredentialsProvider cred = new BasicCredentialsProvider();

        cred.setCredentials(AuthScope.ANY, 
            new UsernamePasswordCredentials(this.username, this.password));

        client.setCredentialsProvider(cred);
      }

      HttpGet req = new HttpGet(url);

      InputStream content = null;
      try
      {
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();

        if (response.getStatusLine().getStatusCode() == 200)
        {
          content = response.getEntity().getContent();

          SAXBuilder builder = new SAXBuilder();
          Document document = builder.build(content);
          nodesRootElement = document.getRootElement();
        }
        else
        {
          log.debug("status line " + response.getStatusLine());
          if (entity != null)
          {
            log.debug("Response content length: " + entity.getContentLength());
          }
        }
      } 
      catch (IOException ioe)
      {
        log.error("IOException while reading data from ISY-99", ioe);
      }
      catch (JDOMException jdomex) 
      {
        log.error("JDOMException while parsing response from ISY-99", jdomex);
      }
      finally
      {
        try
        {
          if (content != null)
          {
            content.close();
          }
        }
        catch (Exception e)
        {
          log.warn("Error on closing HTML entity content stream : {0}", e.getMessage());
        }
      }
    }
  }
   
  // Constructors --------------------------------------------------------------------------------

  /**
    * @param host       hostname or IP address for the ISY-99
    * @param username   username for authentication to the ISY-99
    * @param password   password for authentication to the ISY-99
    */
  public Isy99StatusReader(String host, String username, String password)
  {
    Runnable queryTask = new QueryTask(host, username, password);
      
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
    scheduler.scheduleAtFixedRate(queryTask, INITIAL_DELAY, PERIOD_BETWEEN_EXECUTIONS, 
        TimeUnit.SECONDS);
  }

  // Methods -------------------------------------------------------------------------------------
   
  /**
   * Checks the local status cache for a specific node address.
   *
   * @param address    node address for the ISY-99
   */
  public String getStatus(String address)
  {
    String value = "";
      
    if (nodesRootElement != null)
    {
      @SuppressWarnings("unchecked")
      List<Element> nodeList = nodesRootElement.getChildren("node");
      for (Element node : nodeList)
      {
        if (address.equals(node.getChild("address").getValue()))
        {
          value = node.getChild("property").getAttributeValue("value");
          break;
        }
      }

      if (value == null || value.contains(" "))
      {
        value = "";
      }
    }

    return value;
  }

  /**
   * Updates the local status cache which allows OpenRemote switches
   * to reflect an instant status change.
   *
   * @param address         node address for the ISY-99
   * @param command         command for the ISY-99
   * @param commandParam    command parameter for the ISY-99, i.e. dim level
   */
  public void setLocalStatus(String address, String command, String commandParam)
  {
    if (nodesRootElement != null)
    {
      @SuppressWarnings("unchecked")
      List<Element> nodeList = nodesRootElement.getChildren("node");
      for (Element node : nodeList)
      {
        if (address.equals(node.getChild("address").getValue()))
        {
          Element propertyNode = node.getChild("property");
               
          if (propertyNode.getAttributeValue("id").equals("ST"))
          {
            if (command.equalsIgnoreCase(Isy99Command.ISY99_ON_COMMAND))
            {
              if (commandParam != null && !commandParam.equals(""))
              {
                String formattedValue = "" + ((Integer.valueOf(commandParam) 
                    * Isy99Command.ISY99_MAX_DIMMER_VALUE) / 100);
                        
                propertyNode.setAttribute("value", formattedValue);
                propertyNode.setAttribute("formatted", commandParam);
              }
              else
              {
                propertyNode.setAttribute("value", "255");
                propertyNode.setAttribute("formatted", "On");
              }
            }
            else
            {
              propertyNode.setAttribute("value", "0");
              propertyNode.setAttribute("formatted", "Off");
            }
                  
            break;
          }
        }
      }
    }
  }
}
