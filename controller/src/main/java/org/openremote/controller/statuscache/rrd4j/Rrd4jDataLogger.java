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
package org.openremote.controller.statuscache.rrd4j;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.exception.InitializationException;
import org.openremote.controller.service.ServiceContext;
import org.openremote.controller.statuscache.EventContext;
import org.openremote.controller.statuscache.EventProcessor;
import org.openremote.controller.statuscache.LifeCycleEvent;
import org.openremote.controller.utils.Logger;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This is an {@link org.openremote.controller.statuscache.EventProcessor} which parses
 * rrd4j-config.xml file and creates RRD4j datastores. Each sensor update creates a new data entry
 * within a rrd4j datasource. The rrd4j-config.xml can also be used to configure graphs which
 * later can be displayed within the console. The graphs are provided from a servlet.
 *
 * @author marcus
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class Rrd4jDataLogger extends EventProcessor
{

  // Class Members --------------------------------------------------------------------------------

  /**
   *   Runtime RRD event processor logging category. Should be used for logging information
   *   recorded during the execution and operation of the event processor (outside of startup
   *   or shutdown or other lifecycle events).
   */
  private final static Logger runtime = Logger.getLogger(
      Constants.RUNTIME_EVENTPROCESSOR_LOG_CATEGORY + ".rrd"
  );

  /**
   * Separate logging of the lifecycle/configuration events. Usually these are more visible
   * to the user on the console, boot logs, etc. whereas runtime logs in general are directed
   * into their own specific files.
   */
  private final static Logger init = Logger.getLogger(
      Constants.EVENT_PROCESSOR_INIT_LOG_CATEGORY + ".rrd"
  );


  // Instance Fields ------------------------------------------------------------------------------

  private List<RrdDb> rrdDbList = Collections.emptyList();
  private Map<String,String> graphDefMap;

  /**
   * A "file" URI pointing to the RRD data directory.
   */
  private URI rrdDataDir = null;

  /**
   * Flag to indicate whether this event processor should process incoming events. In case of
   * missing or incomplete or incorrect configuration, this flag can be set to false avoiding
   * unnecessary processing when it cannot be completed.
   */
  private boolean processing = true;

  /**
   * Contains the controller configuration 'resource.path' property resolved to a valid URI. <p>
   *
   * TODO:
   *       the use of this field should be temporary since the proper place to resolve the
   *       config properties into URIs would be to do it early when the ControllerConfiguration
   *       instance is constructed.
   */
  private URI resourcePath = null;


  // Public Instance Methods ----------------------------------------------------------------------

  public String getGraphDef(String graphName)
  {
    return graphDefMap.get(graphName);
  }


  // EventProcessor Overrides ---------------------------------------------------------------------

  @Override public String getName()
  {
    return "RRD4J Data Logger";
  }

  @Override public synchronized void push(EventContext ctx)
  {
    if (!processing)
    {
      return;
    }

    String sensorName = ctx.getEvent().getSource();
    Object eventValue = ctx.getEvent().getValue();

    for (RrdDb rrdDatabase : rrdDbList)
    {
      try
      {
        if (rrdDatabase.getDatasource(sensorName) == null)
        {
          continue;
        }
      }

      catch (IOException e)
      {
        runtime.error("Could not retrieve RRD datasource ''{0}'': {1}", e, sensorName, e.getMessage());

        continue;
      }

      try
      {
        long newUpdate = System.currentTimeMillis() / 1000;
        long lastUpdate = rrdDatabase.getLastUpdateTime();

        if (lastUpdate < newUpdate)
        {
          // TODO:
          //   - the implementation below is brittle as it relies on implementation details
          //     of each event value's toString() method -- there's no specified contract at
          //     this point that event's value has an appropriate toString() implementation.
          //
          //     In case of numeric events, a more robust even definition could be specified
          //     that enforces a Number or other type that guarantees a typed number
          //     value. An exception would still have to be made for Custom or other string
          //     type events where parse compatibility would depend on user and device
          //     configuration.
          //                                                                      [JPL]

          double value = Double.parseDouble(eventValue.toString());

          Sample sample = rrdDatabase.createSample(newUpdate);
          sample.setValue(sensorName, value);
          sample.update();
        }
      }

      catch (NumberFormatException e)
      {
        runtime.error(
            "Attempted to store a non-number value to RRD database: ''{0}''",
            e, ctx.getEvent().getValue()
        );
      }

      catch (IOException e)
      {
        runtime.error(
            "I/O error writing value ''{0}'' to RRD database ''{1}'': {2}",
            e, eventValue, rrdDatabase, e.getMessage()
        );
      }
    }
  }

  @Override public void start(LifeCycleEvent ctx) throws InitializationException
  {
    // Resolve the location of data files. Creates or uses an existing 'rrd' directory in that
    // location. Will throw a config exceptions if can't be resolved, or created...

    resourcePath = resolveResourcePath();
    rrdDataDir = getRRDDataDirectory();

    if (!isRRDConfigAvailable())
    {
      init.info("RRD configuration not present. Sensor value history not saved.");

      processing = false;

      return;
    }

    //Parse XML for RRD4J databases and datasources

    URI rrdConfig = resolveRRDConfigurationFile();

    List<RrdDef> rrdDefList = parseConfigXML(rrdConfig);
    rrdDbList = new ArrayList<RrdDb>();

    for (RrdDef rrdDef : rrdDefList)
    {
      RrdDb rrdDb = null;
      String dbFileName = rrdDef.getPath();

      URI rrdFileURI = rrdDataDir.resolve(dbFileName);

      try
      {
        File rrdFile = new File(rrdFileURI);

        if (rrdFile.exists())
        {
          rrdDb = new RrdDb(rrdFile.getAbsolutePath());
        }

        else
        {
          rrdDef.setPath(rrdFile.getAbsolutePath());
          rrdDb = new RrdDb(rrdDef);
        }

        rrdDbList.add(rrdDb);
      }

      catch (IllegalArgumentException e)
      {
         init.error(
             "RRD datasource URI ''{0}'' must follow ''file'' schema: {1}",
             e, rrdFileURI, e.getMessage()
         );
      }

      catch (SecurityException e)
      {
         init.error(
             "Security manager has denied access to RRD data file ''{0}'': {1}",
             e, rrdFileURI, e.getMessage()
         );
      }

      catch (IOException e)
      {
         init.error(
             "I/O error in accessing RRD datafile ''{0}'': {1}", e, rrdFileURI, e.getMessage()
         );
      }
    }
      
    //Parse XML for RRD4J graph definitions
    graphDefMap = parseConfigXMLGraphs(rrdConfig, rrdDataDir);

   }


  @Override public void stop()
  {
    for (RrdDb rrdDb : rrdDbList)
    {
      try
      {
        rrdDb.close();
      }

      catch (IOException e)
      {
        init.warn("I/O error while shutting down RRD database (''{0}''): {1}", e, rrdDb, e.getMessage());
      }
    }
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private Map<String,String> parseConfigXMLGraphs(URI configUri, URI rddDirUri)
     throws InitializationException
  {
    File xmlFile = new File(configUri);

    try
    {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(xmlFile);
         doc.getDocumentElement().normalize();
         Map<String,String> graphDefMap = new HashMap<String,String>();

         NodeList nList = doc.getElementsByTagName("rrd");
         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            String a = nNode.getTextContent();
            nNode.setTextContent(rddDirUri.resolve(a).getPath());
         }
         
         nList = doc.getElementsByTagName("rrd_graph_def");
         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            NamedNodeMap attributes = nNode.getAttributes();
            String graphName = attributes.getNamedItem("name").getNodeValue();
            String def = nodeToString(nNode);
            graphDefMap.put(graphName, def);
         }
         return graphDefMap;
         
    }

    catch (ParserConfigurationException e)
    {
      throw new ConfigurationException("Failed to create XML parser: {0}", e, e.getMessage());
    }

    catch (SAXException e)
    {
      throw new InitializationException(
          "Unable to parse file ''{0}'', error: {1}", e, xmlFile, e.getMessage()
      );
    }

    catch (IOException e)
    {
      throw new InitializationException(
          "I/O error parsing file ''{0}'', error: {1}", e, xmlFile, e.getMessage()
      );
    }
  }

   private List<RrdDef> parseConfigXML(URI configUri) throws InitializationException {
      try {
         File fXmlFile = new File(configUri);
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(fXmlFile);
         doc.getDocumentElement().normalize();
         List<RrdDef> rrdDefList = new ArrayList<RrdDef>();

         NodeList nList = doc.getElementsByTagName("rrdDB");
         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            NamedNodeMap attributes = nNode.getAttributes();
            String dbFileName = attributes.getNamedItem("fileName").getNodeValue();
            String dbStep = attributes.getNamedItem("step").getNodeValue();

            RrdDef rrdDef = new RrdDef(dbFileName, Long.parseLong(dbStep));

            NodeList childs = nNode.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
               Node child = childs.item(i);
               if (child.getNodeType() == Node.ELEMENT_NODE) {
                  if (child.getNodeName().equalsIgnoreCase("datasource")) {
                     attributes = child.getAttributes();
                     String name = attributes.getNamedItem("name").getNodeValue();
                     String type = attributes.getNamedItem("type").getNodeValue();
                     String heartbeat = attributes.getNamedItem("heartbeat").getNodeValue();
                     String minValue = null;
                     String maxValue = null;
                     if (attributes.getNamedItem("minValue") != null) {
                        minValue = attributes.getNamedItem("minValue").getNodeValue();
                     }
                     if (attributes.getNamedItem("maxValue") != null) {
                        maxValue = attributes.getNamedItem("maxValue").getNodeValue();
                     }
                     rrdDef.addDatasource(name, DsType.valueOf(type), Long.parseLong(heartbeat),
                           (minValue == null) ? Double.NaN : Long.parseLong(minValue), (maxValue == null) ? Double.NaN
                                 : Long.parseLong(maxValue));
                  } else if (child.getNodeName().equalsIgnoreCase("archive")) {
                     attributes = child.getAttributes();
                     String function = attributes.getNamedItem("function").getNodeValue();
                     String xff = attributes.getNamedItem("xff").getNodeValue();
                     String steps = attributes.getNamedItem("steps").getNodeValue();
                     String rows = attributes.getNamedItem("rows").getNodeValue();
                     rrdDef.addArchive(ConsolFun.valueOf(function), Double.parseDouble(xff), Integer.parseInt(steps),
                           Integer.parseInt(rows));
                  }
               }
            }
            rrdDefList.add(rrdDef);
         }
         return rrdDefList;
      } catch (Exception e) {
         e.printStackTrace();
         throw new InitializationException("Error parsinf rrd4j-config.xml", e);
      }
   }


  /**
   * Attempts to resolve or create a RRD data directory. First checks for the presence of
   * 'rrd' directory under the 'resource.path' location. If an 'rrd' directory is present,
   * returns an URI to that location. <p>
   *
   * Otherwise attempts to create a new 'rrd' directory under the location pointed by
   * 'resource.path' controller configuration property.
   *
   * @see org.openremote.controller.ControllerConfiguration#getResourcePath()
   *
   * @return    an URI pointing to the 'rrd' data directory if successful
   *
   * @throws    InitializationException if creating or accessing an existing RRD data directory
   *                                    failed for any reason
   */
  private URI getRRDDataDirectory() throws InitializationException
  {

    URI rrdURI = resourcePath.resolve("rrd/");

    if (!hasDirectoryReadAccess(rrdURI))
    {
      // throws config exception if fails..

      createRRDDataDirectory(rrdURI);
    }

    return rrdURI;
  }

  /**
   * Checks that the rrd directory exists and we can access it.
   *
   * @param uri
   *           file URI pointing to the directory where rrd files are stored
   *
   * @return true if we can read/write the dir, false otherwise
   *
   * @throws InitializationException
   *            if URI is null or security manager was installed but write access was not granted
   *            to directory pointed by the given file URI
   */
  private boolean hasDirectoryReadAccess(URI uri) throws InitializationException
  {
    if (uri == null)
    {
      throw new InitializationException("RRD resource directory resolved to 'null'");
    }

    File dir = new File(uri);

    try
    {
      return dir.exists() && dir.canRead() && dir.canWrite();
    }

    catch (SecurityException e)
    {
      throw new InitializationException(
         "Security Manager has denied write access to directory ''{0}''. " +
         "In order to write rrd data, file write access must be explicitly " +
         "granted to this directory. ({1})", e, uri, e.getMessage()
      );
    }
  }


  /**
   * Resolves the rrd4j configuration xml file location within the RRD data directory.
   *
   * @see #rrdDataDir
   *
   * @return an URI pointing to RRD configuration file
   */
  private URI resolveRRDConfigurationFile()
  {
    return rrdDataDir.resolve("rrd4j-config.xml");
  }

  /**
   * Checks whether the RRD configuration file is available and readable.
   *
   * @return  true if file exists and is readable
   */
  private boolean isRRDConfigAvailable()
  {
    URI uri = resolveRRDConfigurationFile();

    try
    {
      File f = new File(uri);

      return f.exists() && f.canRead();
    }

    catch (IllegalArgumentException e)
    {
      init.warn("Configured path for RRD configuration file ''{0}'' must follow a ''file'' schema: {1}",
          e, uri, e.getMessage()
      );

      return false;
    }

    catch (SecurityException e)
    {
      init.info(
          "Security manager has denied read access to RRD configuration file ''{0}'': {1}",
          e, uri, e.getMessage()
      );

      return false;
    }
  }

  /**
   * Attempts to create a RRD data directory if one does not exist.
   *
   * @param uri   an URI with "file" schema pointing to the location of the desired RRD
   *              data directory, usually prefixed with the 'resource.path' configuration
   *              variable in {@link org.openremote.controller.ControllerConfiguration#getResourcePath()}
   *
   * @throws ConfigurationException if creating the directory fails because of insufficient
   *                                security permissions or for any other reason
   */
  private void createRRDDataDirectory(URI uri) throws ConfigurationException
  {
    try
    {
      File f = new File(uri);

      try
      {
        if (!f.isDirectory())
        {
          try
          {
            boolean success = f.mkdir();

            if (!success)
            {
              throw new ConfigurationException(
                  "Failed to create a file directory ''{0}'' (reason unknown)", f
              );
            }
          }

          catch (SecurityException e)
          {
            throw new ConfigurationException(
                "Security manager has denied write access to create directory ''{0}'': {1}",
                e, f, e.getMessage()
            );
          }
        }
      }

      catch (SecurityException e)
      {
        throw new ConfigurationException(
            "Security manager has denied read access to path ''{0}'': {1}", e, f, e.getMessage()
        );
      }
    }

    catch (IllegalArgumentException e)
    {
      throw new ConfigurationException(
          "Configured path for RRD data directory ''{0}'' must follow a ''file'' schema: {1}",
          e, uri, e.getMessage()
      );
    }
  }

  private URI resolveResourcePath() throws ConfigurationException
  {
    // TODO:
    //   Getting controller configuration is currently an expensive operation (due to poor API
    //   design). Make sure we only fetch it once when this event processor is started.
    //
    //   As is mentioned in the config.getResourcePath() method, the conversion to valid
    //   URI should be made already when accepting the new value into controller configuration
    //   class (and at initialization time) so these URI conversions and checks become
    //   unnecessary at deeper code levels (where they are likely to differ in semantics).
    //                                                                                      [JPL]

    ControllerConfiguration config = ServiceContext.getControllerConfiguration().readXML();

    try
    {
      URI resourceURI = new URI(config.getResourcePath());

      if (!resourceURI.isAbsolute())
      {
        resourceURI = new File(config.getResourcePath()).toURI();
      }

      return resourceURI;
    }

    catch (URISyntaxException e)
    {
      throw new ConfigurationException(
          "Property 'resource.path' value ''{0}'' cannot be parsed. It must contain a valid URI: {1}",
          e, config.getResourcePath(), e.getMessage()
      );
    }
  }

  private String nodeToString(Node node) throws ConfigurationException
  {
    StringWriter sw = new StringWriter();

    try
    {
      Transformer t = TransformerFactory.newInstance().newTransformer();

      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

      try
      {
        t.transform(new DOMSource(node), new StreamResult(sw));
      }

      catch (TransformerException e)
      {
        runtime.error("Unable to transform node {0}, error: {1}", e, node, e.getMessage());
      }
    }

    catch (TransformerConfigurationException e)
    {
      // when creating the transformer fails for any reason

      throw new ConfigurationException("Creating XML transformer failed: {0}", e, e.getMessage());
    }

    catch (TransformerFactoryConfigurationError e)
    {
      // Transformer is resolved as follows:
      //  - check javax.xml.transform.TransformerFactory property
      //  - from "lib/jaxp.properties" file
      //  - check META-INF/services/javax.xml.transform.TransformerFactory
      //  - default SDK implementation
      //
      //  This error is thrown if any of the above methods fails to produce a valid instance

      throw new ConfigurationException(
          "Unable to instantiate XML transform factory: {0}", e, e.getMessage()
      );
    }

    return sw.toString();
  }

}
