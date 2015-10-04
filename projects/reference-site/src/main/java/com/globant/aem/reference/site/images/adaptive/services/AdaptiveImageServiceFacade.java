package com.globant.aem.reference.site.images.adaptive.services;

public interface AdaptiveImageServiceFacade {

  /**
   * Returns the instance of the server that matches with the received requestPath.
   * 
   * @param requestPath
   *          the request path
   */
  AdaptiveImageService getServiceReference(String requestPath);
}
