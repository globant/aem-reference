package com.globant.aem.reference.site.replication.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationAction;
import com.globant.aem.reference.site.replication.services.ReplicationCommand;
import com.globant.aem.reference.site.replication.services.ReplicationHandlerService;

@Component(
  immediate = true,
  metatype = true,
  label = "Replication Commands",
  description = "Handles replication events for the configured paths by dispatching the appropiate "
              + "method invocation of registered "
              + "com.globant.aem.reference.site.replication.services.ReplicationHandlerService.",
  configurationFactory = true,
  policy = ConfigurationPolicy.REQUIRE)
@Service(value = ReplicationCommand.class)
@References({
  @Reference(
    cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
    policy = ReferencePolicy.DYNAMIC,
    referenceInterface = ReplicationHandlerService.class,
    bind = "bindReplicationHandlerService",
    unbind = "unbindReplicationHandlerService")})
public class ReplicationCommandImpl implements ReplicationCommand {
  private static final String PATTERN = "pattern";
  private static final String HANDLER_PIDS = "handlers.pids";

  private static final Logger log = LoggerFactory.getLogger(ReplicationCommandImpl.class);

  @Property(
    name = PATTERN,
    label = "Pattern",
    description = "Regular expression indicating the path of the "
                + "resources accepted by this command.")
  private String pattern;

  @Property(
    name = HANDLER_PIDS,
    label = "Handler Pids",
    description = "List of PIDs of the "
                + "com.globant.aem.reference.site.replication.services.ReplicationHandlerService "
                + "that will be executed when the resource path matches the pattern",
    unbounded = PropertyUnbounded.ARRAY)
  private List<String> handlerPids;

  private Map<String, ReplicationHandlerService> handlersMap =
      new HashMap<String, ReplicationHandlerService>();

  public ReplicationCommandImpl() {
    handlerPids = new ArrayList<String>();
    handlersMap = new HashMap<String, ReplicationHandlerService>();
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public List<String> getHandlers() {
    return Collections.unmodifiableList(handlerPids);
  }

  public void setHandlers(List<String> handlers) {
    this.handlerPids.clear();
    this.handlerPids.addAll(handlers);
  }

  @Activate
  protected void configure(
    final BundleContext bundleContext,
    final Map<String, Object> properties) {

    String[] handlersArray = PropertiesUtil.toStringArray(properties.get(HANDLER_PIDS));

    pattern = PropertiesUtil.toString(properties.get(PATTERN), "");
    handlerPids = Arrays.asList(handlersArray != null ? handlersArray : new String[] {});
  }

  @Override
  public void execute(ReplicationAction action, Resource resource) {
    if (!accepts(resource)) {
      log.debug("Skipping resource. {} does not match {}", resource.getPath(), pattern);
      return;
    }

    for (String handlerPid: handlerPids) {
      ReplicationHandlerService handler = handlersMap.get(handlerPid);

      if (handler != null) {
        dispatchAction(action, resource, handler);
      }
    }
  }

  private void dispatchAction(
      ReplicationAction action, Resource resource, ReplicationHandlerService handler) {
    switch (action.getType()) {
    //checkstyle:OneStatementPerLine OFF
      case ACTIVATE:      handler.activateResource(resource);     break;
      case DEACTIVATE:    handler.deactivateResource(resource);   break;
      case DELETE:        handler.deleteResource(resource);       break;
      case TEST:          handler.testResource(resource);         break;
      case REVERSE:       handler.reverseResource(resource);      break;
      case INTERNAL_POLL: handler.internalPollResource(resource); break;
      default: break;
      //checkstyle:OneStatementPerLine ON
    }
  }
 
  @Override
  public boolean accepts(Resource resource) {
    return resource.getPath().matches(pattern);
  }

  protected void bindReplicationHandlerService(
      ReplicationHandlerService ref,
      @SuppressWarnings("rawtypes") Map properties) {

    String pid = (String) properties.get("service.pid");

    log.info("Binding replication handler {}", pid);

    handlersMap.put(pid, ref);
  }

  protected void unbindReplicationHandlerService(
      ReplicationHandlerService myService,
      @SuppressWarnings("rawtypes") Map properties) {

    String pid = (String) properties.get("service.pid");

    log.info("Unbinding replication handler {}", pid);

    handlersMap.remove(pid);
  }
}
