package com.globant.aem.reference.site.replication.content.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ContentBuilder;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationContent;
import com.day.cq.replication.ReplicationContentFacade;
import com.day.cq.replication.ReplicationContentFactory;
import com.day.cq.replication.ReplicationException;

@Component(metatype = false)
@Service(ContentBuilder.class)
@Property(name = "name", value = "JSON")
public class JsonContentBuilder implements ContentBuilder {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Reference
  private ResourceResolverFactory resolverFactory;

  @Override
  public ReplicationContent create(Session session, ReplicationAction action,
      ReplicationContentFactory factory) throws ReplicationException {
    return create(session, action, factory, null);
  }

  @Override
  public ReplicationContent create(Session session, ReplicationAction action,
      ReplicationContentFactory factory, Map<String, Object> map) throws ReplicationException {

    log.error("**********XXXXXXXXXXXXXXXX**************");
    log.error("**********XXXXXXXXXXXXXXXX**************");
    log.error("**********XXXXXXXXXXXXXXXX**************");
    log.error("DEBUG");
    log.error("**********XXXXXXXXXXXXXXXX**************");
    log.error("**********XXXXXXXXXXXXXXXX**************");
    log.error("**********XXXXXXXXXXXXXXXX**************");
    Map<String, Object> prop = new HashMap<String, Object>();
    prop.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);

    BufferedWriter out = null;
    File tmpFile = null;
    try {
//      ResourceResolver resourceResolver = resolverFactory.getResourceResolver(prop);
      ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
      Resource resource = resourceResolver.resolve(action.getPath());

      JSONObject json = new JSONObject();
      buildJsonObject(json, resource);

      tmpFile = createTempFile();
      out = new BufferedWriter(new FileWriter(tmpFile));
      out.write(json.toString());

      ReplicationContent content = factory.create("application/json", tmpFile, true);

      log.error("REPLICATING:::::::");
      log.error("REPLICATING:::::::");
      log.error("REPLICATING:::::::");
      log.error("REPLICATING:::::::");
      log.error(content.getContentType());
      
      return content;
    } catch (Exception exception) {
      throw new ReplicationException(exception);
    } finally {
      if (out != null) {
        try { 
          out.close(); 
        } catch (IOException e) {
          log.error("Exception when closing output stream", e);
        }
      }
      if (tmpFile != null && tmpFile.exists()) {
        tmpFile.delete();
      }
    }
  }

  private File createTempFile() throws IOException {
    File file = File.createTempFile("cq5", ".post");
    log.error("Temp file created: {}", file.getAbsolutePath());
    return file;
  }

  private void buildJsonObject(JSONObject json, Resource resource) throws JSONException {
    for (Resource child : resource.getChildren()) {
      JSONObject childJson = new JSONObject();
      buildJsonObject(childJson, child);
      json.put(resource.getName(), childJson.toString());
    }
    for (Entry<String, Object> entry : resource.getValueMap().entrySet()) {
      json.put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public String getName() {
    return "JSON";
  }

  @Override
  public String getTitle() {
    return "JSON";
  }
}
