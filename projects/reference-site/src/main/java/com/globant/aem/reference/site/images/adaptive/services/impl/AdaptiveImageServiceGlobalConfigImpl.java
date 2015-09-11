package com.globant.aem.reference.site.images.adaptive.services.impl;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;

import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageServiceGlobalConfig;

@Service(value = AdaptiveImageServiceGlobalConfig.class)
@Component(immediate = true, metatype = true, label = "Adaptive Image Service Global Config", description = "Provides Global Parameters for the Adaptive Image Service.")
public class AdaptiveImageServiceGlobalConfigImpl implements
        AdaptiveImageServiceGlobalConfig {

    @Property(intValue = 10, label = "Allowed Concurrent Threads", description = "This is the maximum number of concurrent threads allowed for the service.")
    public static final String PROPERTY_CONCURRENT_THREADS = "concurrent.threads";

    private int concurrent_threads;

    @Activate
    protected void activate(final BundleContext bundleContext,
            final Map<String, Object> properties) {
        configureService(properties);
    }

    private void configureService(Map<String, Object> properties) {
        concurrent_threads = PropertiesUtil.toInteger(properties.get(PROPERTY_CONCURRENT_THREADS), 10);
    }

    @Override
    public int getConcurrentThreads() {
        return this.concurrent_threads;
    }
}
