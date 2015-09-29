package com.globant.aem.reference.site.replication.event.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationAction;
import com.globant.aem.reference.site.replication.services.ReplicationCommand;

/**
 * This is an example listener that listens for replication events and
 * logs a message.
 */
@Component(
  metatype = false,
  immediate = true,
  description = "Handles com/day/cq/replication events by delegating to accepting instances of "
              + "com.globant.aem.reference.site.replication.services.ReplicationCommand"
)
@Service(value = { EventHandler.class })
@Property(name = "event.topics", value = { ReplicationAction.EVENT_TOPIC })
@References({
  @Reference(
    cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
    policy = ReferencePolicy.DYNAMIC,
    referenceInterface = ReplicationCommand.class,
    bind = "bindReplicationCommand",
    unbind = "unbindReplicationCommand")
})
public class ReplicationEventDispatcher implements EventHandler {
  private static final Logger log = LoggerFactory.getLogger(ReplicationEventDispatcher.class);

  @Reference
  private ResourceResolverFactory resolverFactory;

  private List<ReplicationCommand> commands = new ArrayList<ReplicationCommand>();

	public void handleEvent(Event event) {
		ReplicationAction action = ReplicationAction.fromEvent(event);

		if(action != null) {
		  String path = action.getPath();
		  try {
        Resource resource = this.resolve(path);

        for(ReplicationCommand command: this.commands) {
          if (command.accepts(resource)) {
            command.execute(action, resource);
          }
        }

      } catch (LoginException e) {
        log.error("Exception found when trying to access resource {}. Skipping resource.", path, e);
      }
		} else {
		  log.debug("Can't create a ReplicationAction from event {}", event.getTopic());
		}
	}

  private Resource resolve(String path) throws LoginException {
    ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
    Resource resource = resourceResolver.resolve(path);

    return resource;
  }

  protected void bindReplicationCommand(
      ReplicationCommand ref,
      @SuppressWarnings("rawtypes") Map properties) {

    log.info("Binding {}", ref.getClass().getName());

    commands.add(ref);
  }

  protected void unbindReplicationCommand(
      ReplicationCommand myService,
      @SuppressWarnings("rawtypes") Map properties) {

    log.info("Unbinding {}", myService.getClass().getName());

    commands.remove(myService);
  }
}
