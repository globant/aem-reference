package com.globant.aem.reference.site.replication.content.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import com.day.cq.replication.ContentBuilder;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationContent;
import com.day.cq.replication.ReplicationContentFactory;
import com.day.cq.replication.ReplicationException;

/**
 * Custom dispatcher flush content builder that sends a list of URIs to be re-fetched immediately
 * upon flushing a page.
 */
@Component(metatype = false)
@Service(ContentBuilder.class)
@Property(name = "name", value = "dispatcher")
public class DispatcherFlushContentBuilder implements ContentBuilder {

  public static final String NAME = "dispatcher";

  public static final String TITLE = "Re-fetch Dispatcher Flush";

  /**
   * Documentation from {@link com.day.cq.replication.ContentBuilder#(Session, ReplicationAction,
   *   ReplicationContentFactory)}.
   * 
   * {@inheritDoc}
   */
  public ReplicationContent create(Session session, ReplicationAction action,
      ReplicationContentFactory factory) throws ReplicationException {

    /*
     * In this simple example we check whether the page activated has no extension (e.g.
     * /content/geometrixx/en/services) and add this page plus html extension to the list of URIs to
     * re-fetch
     */
    String path = action.getPath();
    if (path != null) {
      int pathSep = path.lastIndexOf('/');
      if (pathSep != -1) {
        int extSep = path.indexOf('.', pathSep);
        if (extSep == -1) {
          String[] uris = new String[] { path + ".html" };
          return create(factory, uris);
        } else {
          String[] uris = new String[] { path };
          return create(factory, uris);
        }
      }
    }
    return ReplicationContent.VOID;
  }


  @Override
  public ReplicationContent create(Session session, ReplicationAction action,
      ReplicationContentFactory factory, Map<String, Object> map) throws ReplicationException {

    return this.create(session, action, factory);
  }

  /**
   * Create the replication content, containing one or more URIs to be re-fetched immediately upon
   * flushing a page.
   *
   * @param factory factory
   * @param uris URIs to re-fetch
   * @return replication content
   *
   * @throws ReplicationException if an error occurs
   */
  private ReplicationContent create(ReplicationContentFactory factory, String[] uris)
      throws ReplicationException {

    File tmpFile;
    BufferedWriter out = null;

    try {
      tmpFile = File.createTempFile("cq5", ".post");
    } catch (IOException e) {
      throw new ReplicationException("Unable to create temp file", e);
    }

    try {
      out = new BufferedWriter(new FileWriter(tmpFile));
      for (int i = 0; i < uris.length; i++) {
        out.write(uris[i]);
        out.newLine();
      }
      
      return factory.create("text/plain", tmpFile, true);
    } catch (IOException e) {
      
      throw new ReplicationException("Unable to create repository content", e);
    } finally {
      try {
        if (out != null) {
          out.close();
        }
        if (tmpFile != null) {
          tmpFile.delete();
        }
      } catch (IOException e) {
        // ignore exception
      }
    }
    
  }

  /**
   * Documentation from {@link com.day.cq.replication.ContentBuilder#getName()}.
   * 
   * {@inheritDoc}
   *
   * @return {@value #NAME}
   */
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Documentation from {@link com.day.cq.replication.ContentBuilder#getTitle()}.
   * 
   * {@inheritDoc}
   *
   * @return {@value #TITLE}
   */
  @Override
  public String getTitle() {
    return TITLE;
  }
}
