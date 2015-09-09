package com.globant.aem.reference.site.images.adaptive.services.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;

import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageService;
import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageServiceFacade;

@Component
@Service
@References({ @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC, referenceInterface = AdaptiveImageService.class, name = "AdaptiveImageService") })
public class AdaptiveImageServiceFacadeImpl implements AdaptiveImageServiceFacade {

    private final Map<String, ServiceReference> adaptiveImageServiceConfigs = new HashMap<String, ServiceReference>();
    private final List<String> adaptiveImageServicePatterns = new Vector<String>();

    @Override
    public AdaptiveImageService getServiceReference(String requestPath) {

        for (String pattern : adaptiveImageServicePatterns) {
            if (Pattern.matches(pattern, requestPath)) {
                ServiceReference config = adaptiveImageServiceConfigs.get(pattern);
                if (config == null)
                    continue;
                return (AdaptiveImageService) config.getBundle().getBundleContext().getService(config);
            }
        }
        return null;
    }

    protected void bindAdaptiveImageService(ServiceReference ref) {
        String sectionRegex = (String) ref.getProperty(AdaptiveImageServiceImpl.PROPERTY_SECTION);
        adaptiveImageServiceConfigs.put(sectionRegex, ref);
        adaptiveImageServicePatterns.add(sectionRegex);
    }

    protected void unbindAdaptiveImageService(ServiceReference ref) {
        String sectionRegex = (String) ref.getProperty(AdaptiveImageServiceImpl.PROPERTY_SECTION);
        adaptiveImageServiceConfigs.remove(sectionRegex);
        for (Iterator<String> iterator = adaptiveImageServicePatterns.iterator(); iterator.hasNext();) {
            String pattern = iterator.next();
            if (pattern.equals(sectionRegex)) {
                iterator.remove();
            }
        }
    }
}
