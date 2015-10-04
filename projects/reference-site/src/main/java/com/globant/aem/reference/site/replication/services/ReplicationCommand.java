package com.globant.aem.reference.site.replication.services;

import org.apache.sling.api.resource.Resource;

import com.day.cq.replication.ReplicationAction;

public interface ReplicationCommand {
  boolean accepts(Resource resource);
  
  void execute(ReplicationAction action, Resource resource);
}
