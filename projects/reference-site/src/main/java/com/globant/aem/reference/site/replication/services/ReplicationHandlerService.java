package com.globant.aem.reference.site.replication.services;

import org.apache.sling.api.resource.Resource;

public interface ReplicationHandlerService {
  void activateResource(Resource resource);
  
  void deactivateResource(Resource resource);
  
  void deleteResource(Resource resource);
  
  void testResource(Resource resource);
  
  void reverseResource(Resource resource);
  
  void internalPollResource(Resource resource);
}
