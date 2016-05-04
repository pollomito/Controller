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
package org.openremote.controller.statuscache.rules;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.TimedRuleExectionOption;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.definition.KnowledgePackage;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.RuleListener;
import org.openremote.controller.exception.InitializationException;
import org.openremote.controller.protocol.Event;
import org.openremote.controller.service.ServiceContext;
import org.openremote.controller.statuscache.EventContext;
import org.openremote.controller.statuscache.EventProcessor;
import org.openremote.controller.statuscache.LevelFacade;
import org.openremote.controller.statuscache.LifeCycleEvent;
import org.openremote.controller.statuscache.RangeFacade;
import org.openremote.controller.statuscache.SwitchFacade;
import org.openremote.controller.utils.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class RuleEngine extends EventProcessor
{

  // TODO : integrate with statuscache/deployer lifecycle




  // Enums ----------------------------------------------------------------------------------------

  public enum ResourceFileType
  {
    DROOLS_RULE_LANGUAGE(".drl"),

    CSV_DECISION_TABLE(".csv");


    // Enum Implementation ------------------------------------------------------------------------

    private String fileExtension;

    private ResourceFileType(String fileExtension)
    {
      this.fileExtension = fileExtension;
    }

    public String getFileExtension()
    {
      return fileExtension;
    }
  }



  // Class Members --------------------------------------------------------------------------------

  private final static Logger log = Logger.getLogger(
      Constants.RUNTIME_EVENTPROCESSOR_LOG_CATEGORY + ".drools"
  );

  private final static Logger initLog = Logger.getLogger(
      Constants.EVENT_PROCESSOR_INIT_LOG_CATEGORY
  );


  // Private Instance Fields ----------------------------------------------------------------------

  private KieBase kb;
  private KieSession knowledgeSession;
  private Map<Integer, FactHandle> eventSources = new HashMap<Integer, FactHandle>();
  private long factCount;

  private SwitchFacade switchFacade;
  private LevelFacade levelFacade;
  private RangeFacade rangeFacade;



  // Implements EventProcessor --------------------------------------------------------------------



  /**
   * TODO
   */
  @Override public void push(EventContext ctx)
  {
    // if we got no rules, just push event back to next processor...
    if (kb == null)
    {
      return;
    }

    // TODO : add listener for logging

    Event evt = ctx.getEvent();

    switchFacade.pushEventContext(ctx);
    switchFacade.pushLogger(log);
    levelFacade.pushEventContext(ctx);
    levelFacade.pushLogger(log);
    rangeFacade.pushEventContext(ctx);
    rangeFacade.pushLogger(log);

//    SwitchFacade switchFacade = new SwitchFacade();
//
//    knowledgeSession.setGlobal("switch", switchFacade);
//
//    knowledgeSession.setGlobal("event", eventFacade);

    try
    {
      long factNewCount;
      if (!knowledgeSession.getObjects().contains(evt))
      {
        boolean debug = true;
        if (eventSources.keySet().contains(evt.getSourceID()))
        {
          try
          {
            knowledgeSession.delete(eventSources.get(evt.getSourceID()));
          }
          finally
          {
            // Doing this in the finally to make sure we don't keep a reference to a fact when we should not (ORCJAVA-407)
            eventSources.remove(evt.getSourceID());
          }
          debug = false;
        }

        FactHandle handle = knowledgeSession.insert(evt);
        eventSources.put(evt.getSourceID(), handle);

        log.trace("Inserted event {0}", evt);
        if(debug)
        {
           log.debug("Inserted new event source \"{0}\"", evt.getSource());
        }
        factNewCount = knowledgeSession.getFactCount();
        log.trace("Fact count: " + factNewCount);
        if(factNewCount != factCount)
        {
           log.debug("Fact count changed from {0} to {1} on \"{2}\"", factCount, factNewCount, evt.getSource());
        }
        factCount = factNewCount;
      }
      
      knowledgeSession.fireAllRules();

      factNewCount = knowledgeSession.getFactCount();
      if(factNewCount >= 1000) // look for runaway insertion of facts
      if(factNewCount != factCount)
      {
         log.debug("Fact count changed from {0} to {1} on fireAllRules() after \"{2}\"", factCount, factNewCount, evt.getSource());
      }
      factCount = factNewCount;
    }

    catch (Throwable t)
    {
      log.error(
          "Error in executing rule : {0}\n\tEvent {1} not processed!",
          t, evt.getSource()+":"+t.getMessage(), ctx.getEvent()
      );

      if (t.getCause() != null)
      {
        log.error("Root Cause: \n", t.getCause());
      }
    }
  }


  /**
   * TODO
   *
   */
  @Override public void start(LifeCycleEvent event) throws InitializationException
  {
    ControllerConfiguration config = ServiceContext.getControllerConfiguration();

    URI resourceURI;

    try
    {
      resourceURI = new URI(config.getResourcePath());

      if (!resourceURI.isAbsolute())
      {
        resourceURI = new File(config.getResourcePath()).toURI();
      }
    }

    catch (URISyntaxException e)
    {
      throw new InitializationException(
          "Property 'resource.path' value ''{0}'' cannot be parsed. " +
          "It must contain a valid URI : {1}",
          e, config.getResourcePath(), e.getMessage()
      );
    }

    URI rulesURI = resourceURI.resolve("rules");

    if (!hasDirectoryReadAccess(rulesURI))
    {
      throw new InitializationException(
          "Directory ''{0}'' does not exist or cannot be read.", rulesURI
      );
    }

    KieServices kieServices = KieServices.Factory.get();

    Map<Resource, File> ruleDefinitions = getRuleDefinitions(kieServices, rulesURI, ResourceFileType.DROOLS_RULE_LANGUAGE);
    Map<Resource, File> csvDecisionTables = getRuleDefinitions(kieServices, rulesURI, ResourceFileType.CSV_DECISION_TABLE);

    if (ruleDefinitions.isEmpty() && csvDecisionTables.isEmpty())
    {
      initLog.info("No rule definitions found in ''{0}''.", new File(rulesURI).getAbsolutePath());

      return;
    }

    // Note, knowledgebuilder is not thread-safe...

    KieModuleModel kieModuleModel = kieServices.newKieModuleModel();

    KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel("OpenRemoteKBase")
            .setDefault(true)
            .setEqualsBehavior(EqualityBehaviorOption.EQUALITY);
    KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel("OpenRemoteKSession")
            .setDefault(true)
            .setType(KieSessionModel.KieSessionType.STATEFUL);

    KieFileSystem kfs = kieServices.newKieFileSystem();
    kfs.writeKModuleXML(kieModuleModel.toXML());


    addResources(kieServices, kfs, ruleDefinitions, ResourceFileType.DROOLS_RULE_LANGUAGE);
    addResources(kieServices, kfs, csvDecisionTables, ResourceFileType.CSV_DECISION_TABLE);

    /*
    kb = KnowledgeBaseFactory.newKnowledgeBase(kbConfiguration);

    Set<KnowledgePackage> packages1 = getValidKnowledgePackages(ruleDefinitions, ResourceFileType.DROOLS_RULE_LANGUAGE);
    Set<KnowledgePackage> packages2 = getValidKnowledgePackages(csvDecisionTables, ResourceFileType.CSV_DECISION_TABLE);

    // TODO :
    //    Leaving XLS out for now -- not sure they're really necessary (you can always
    //    save as CSV) and don't want to introduce additional library dependencies (POI et al)
    //                                                                                          [JPL]


    kb.addKnowledgePackages(packages1);
    kb.addKnowledgePackages(packages2);


    knowledgeSession = kb.newStatefulKnowledgeSession();
    */

    KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
    kb = kieContainer.getKieBase();

    KieSessionConfiguration kieSessionConfiguration = kieServices.newKieSessionConfiguration();
    // Use this option to ensure timer rules are fired even in passive mode (triggered by fireAllRules.
    // This ensures compatibility with the behaviour of previous controller using drools 5.1
    kieSessionConfiguration.setOption(TimedRuleExectionOption.YES);
    knowledgeSession = kb.newKieSession(kieSessionConfiguration, null);

    switchFacade = new SwitchFacade();
    rangeFacade = new RangeFacade();
    levelFacade = new LevelFacade();

    
    try
    {
      knowledgeSession.setGlobal("execute", event.getCommandFacade());
    }

    catch (Throwable t)
    {}

    try
    {
      knowledgeSession.setGlobal("switches", switchFacade);
    }

    catch (Throwable t)
    {}

    try
    {
      knowledgeSession.setGlobal("ranges", rangeFacade);
    }

    catch (Throwable t)
    {}

    try
    {
      knowledgeSession.setGlobal("levels", levelFacade);
    }
    
    catch (Throwable t)
    {}

    try
    {
       RuleListener ruleListener = new RuleListener();
       knowledgeSession.addEventListener(ruleListener);
    }

    catch (Throwable t)
    {
       log.debug("Exception in addEventListener");
    }
    
    log.debug("Rule engine started");

    knowledgeSession.fireAllRules();
  }


  /**
   * TODO
   */
  @Override public void stop()
  {
    log.debug("Stopping RuleEngine");
    if (knowledgeSession != null)
    {
      knowledgeSession.dispose();
      log.debug("Knowledge session disposed");
    }
    eventSources.clear();

    // We're disposing of the knowledge base, don't keep references to any facts (ORCJAVA-407)
    eventSources.clear();
    
    kb = null;
  }


  /**
   * Returns the name of this event processor.
   *
   * @return    rule engine name
   */
  @Override public String getName()
  {
    return "Drools Rule Engine";
  }


  // Private Instance Methods ---------------------------------------------------------------------


  /**
   * Checks that the rule directory exists and we can access it.
   *
   * @param uri   file URI pointing to the rule definition directory
   *
   * @return      true if we can read the dir, false otherwise
   *
   * @throws      InitializationException   if URI is null or security manager was installed
   *                                        but read access was not granted to directory pointed
   *                                        by the given file URI
   */
  private boolean hasDirectoryReadAccess(URI uri) throws InitializationException
  {

    if (uri == null)
    {
      throw new InitializationException("Rule resource directory was resolved to 'null'");
    }

    File dir = new File(uri);

    try
    {
      return dir.exists() && dir.canRead();
    }

    catch (SecurityException e)
    {
      throw new InitializationException(
          "Security Manager has denied read access to directory ''{0}''. " +
          "In order to deploy rule definitions, file read access must be explicitly " +
          "granted to this directory. ({1})",
          e, uri, e.getMessage()
      );
    }
  }


  /**
   * Loads the rule definitions of a given type from a pre-defined file URI.  <p>
   *
   * Note that if security manager is enabled, it must explicitly grant read access to the
   * directory referenced by the file URI.
   *
   * @param uri               File URI pointing to a <b>directory</b> that contains rule definitions
   * @param resourceFileType  The file type of the rule definitions to be loaded.
   *
   * @return  a map containing a resource handle (that can be used by Drools) as key, and the file
   *          reference to the physical file that was used to create the resource reference
   */
  private Map<Resource, File> getRuleDefinitions(KieServices kieServices, URI uri, final ResourceFileType resourceFileType)
  {
    final File dir = new File(uri);

    File[] files;

    try
    {
      files = dir.listFiles(new FilenameFilter()
      {
        @Override public boolean accept(File path, String name)
        {
          return (path.equals(dir) && name.endsWith(resourceFileType.getFileExtension()));
        }
      });
    }

    catch (SecurityException e)
    {
      initLog.error(
          "Unable to list files in directory ''{0}'' due to security restrictions. " +
          "Security manager must grant read access to this directory. No rules were loaded. " +
          "(Exception: {1})", e, dir.getAbsolutePath(), e.getMessage()
      );

      return new HashMap<Resource, File>(0);
    }

    if (files == null)
    {
      initLog.error(
          "File location ''{0}'' is not a directory, or an I/O error occured trying to list " +
          "files in this directory. No rules were loaded.", dir.getAbsolutePath()
      );

      return new HashMap<Resource, File>(0);
    }

    Map<Resource, File> ruleDefinitions = new HashMap<Resource, File>();

    for (File file : files)
    {
      try
      {
        if (file.length() >0) {
          Resource resource = kieServices.getResources().newFileSystemResource(file);
          ruleDefinitions.put(resource, file);
          initLog.debug("Adding Rule ''{0}''...", file.getName());
        }
      }

      catch (Throwable t)
      {
        initLog.warn(
            "Unable to add rule definition ''{0}'' : {1}",
            t, file.getAbsoluteFile(), t.getMessage()
        );
      }
    }

    return ruleDefinitions;
  }


  /**
   * This is a bit of an ugly hack -- was in a hurry and couldn't find if there's a better
   * way in Drools API to handle this... Drools responds to errors in rule definitions by
   * throwing runtime exceptions. This is a bit of an issue when you are potentially deploying
   * multiple separate files, as it is not clear if the correct ones will be deployed or if
   * everything gets discarded if even one doesn't parse.  <p>
   *
   * So the ugly hack part is that we try to add rule definitions individually to temporary
   * knowledge bases. If they get parsed successfully then we keep them. Otherwise we don't
   * keep them on the list, just tell user about the error and continue to deploy the ones
   * that do work. For each rule definition we discard the temporary knowledge base and then
   * later deploy all successful ones in the real knowledge base that we'll use at runtime. <p>
   *
   * Doing this feels wrong so if there's a more natural way to accomplish this (deploying
   * different types of rule definitions -- DRL, CSV, etc -- and discarding the faulty ones
   * only rather than all of them) then this should be fixed.
   *
   * @param definitions     potential rule definition candidates -- these will be attempted to
   *                        add to temporary knowledge base just to see if their definitions
   *                        cause any exceptions to be thrown
   *
   * @param resourceType    Resource file type -- DRL or CSV file
   *
   * @return  set of knowledge packages that seemed to deploy correctly -- to be deployed in
   *          the actual knowledge base we use at runtime
   */
  /*
  private Set<KnowledgePackage> getValidKnowledgePackages(Map<Resource, File> definitions,
                                                          ResourceFileType resourceType)
  {
    Set<KnowledgePackage> packages = new HashSet<KnowledgePackage>();

    for (Resource resource : definitions.keySet())
    {
      KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();

      try
      {
        if (resourceType == ResourceFileType.CSV_DECISION_TABLE)
        {
          DecisionTableConfiguration conf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
          conf.setInputType(DecisionTableInputType.CSV);

          builder.add(resource, ResourceType.DTABLE, conf);
        }

        else if (resourceType == ResourceFileType.DROOLS_RULE_LANGUAGE)
        {
          builder.add(resource, ResourceType.DRL);
        }

        else
        {
          initLog.warn("Unrecognized rule definition file format : {0}", resourceType);
        }

        if (builder.hasErrors())
        {
          Collection<KnowledgeBuilderError> errors = builder.getErrors();

          initLog.error(
              "Rule definition ''{0}'' could not be deployed. See errors below.",
              definitions.get(resource).getName()
          );

          for (KnowledgeBuilderError error : errors)
          {
            initLog.error(error.getMessage());
          }
        }else{
           initLog.info("Added rule definition ''{0}'' to knowledge.", definitions.get(resource).getName());
        }
      }

      catch (Throwable t)
      {
        initLog.error(
            "Error in rule definition ''{0}'' : {1}",
            t, definitions.get(resource).getName(), t.getMessage()
        );
      }

      try
      {
        KnowledgeBase kb = builder.newKnowledgeBase();

        kb.addKnowledgePackages(builder.getKnowledgePackages());

        packages.addAll(builder.getKnowledgePackages());

        initLog.debug("Adding rule definitions from ''{0}''...", definitions.get(resource).getName());
      }

      catch (IllegalArgumentException e)
      {
        initLog.error(
            "There was an error parsing the rule definition ''{0}'' : {1}",
            e, definitions.get(resource).getName(), e.getMessage()
        );
      }
    }

    return packages;
  }
 */

  private void addResources(KieServices kieServices, KieFileSystem kfs, Map<Resource, File> definitions,
                                                          ResourceFileType resourceType)
  {
    Set<KnowledgePackage> packages = new HashSet<KnowledgePackage>();

    for (Resource resource : definitions.keySet())
    {
      try
      {
        kfs.write("src/main/resources/" + definitions.get(resource).getName(), resource);

        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();

        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR))
        {

          Collection<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);

          initLog.error(
                  "Rule definition ''{0}'' could not be deployed. See errors below.",
                  definitions.get(resource).getName()
          );

          for (Message error : errors)
          {
            initLog.error(error.getText());
          }
          // If compilation failed, remove rules from FileSystem so it won't fail on next pass here if any
          kfs.delete("src/main/resources/" + definitions.get(resource).getName());

        } else
        {
          initLog.info("Added rule definition ''{0}'' to knowledge.", definitions.get(resource).getName());
        }
      } catch (Throwable t) {
        initLog.error(
                "Error in rule definition ''{0}'' : {1}",
                t, definitions.get(resource).getName(), t.getMessage()
        );
        // If compilation failed, remove rules from FileSystem so it won't fail on next pass here if any
        kfs.delete("src/main/resources/" + definitions.get(resource).getName());
      }

    }
  }
}

