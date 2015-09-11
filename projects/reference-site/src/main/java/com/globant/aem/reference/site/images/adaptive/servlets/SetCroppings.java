package com.globant.aem.reference.site.images.adaptive.servlets;

import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageServiceFacade;

@Component(immediate = true, metatype = true, label = "Croppings Storage Servlet")
@Service
@Properties({
        @Property(name = "sling.servlet.paths", value = { "/bin/services/core/public/setcroppings" }),
        @Property(name = "sling.servlet.methods", value = { "GET" }) })
public class SetCroppings extends SlingSafeMethodsServlet {

    /**
     * Serial Version ID
     */
    private static final long serialVersionUID = 1331999881285118556L;

    @Reference
    private AdaptiveImageServiceFacade serviceFacade;

    @Override
    protected void doGet(SlingHttpServletRequest request,
            final SlingHttpServletResponse response) throws ServletException,
            IOException {

        String path = request.getParameter("path");

        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource imageResource = resourceResolver.resolve(path);

        Resource contentResource = imageResource.getChild("jcr:content");
        Resource croppingsResource = imageResource
                .getChild("jcr:content/croppings");

        if (croppingsResource == null) {
            croppingsResource = resourceResolver.create(contentResource,
                    "croppings", null);
        }

        Enumeration<?> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            if (!name.equalsIgnoreCase("path")) {
                String cords = request.getParameter(name);
                String[] parts = cords.split("/");

                if (parts.length == 2) {
                    String[] rect = parts[0].split(",");
                    if (rect.length == 4) {

                        name = name.toLowerCase();
                        Resource croppingIdResource = croppingsResource
                                .getChild(name);
                        if (croppingIdResource == null) {
                            croppingIdResource = resourceResolver.create(
                                    croppingsResource, name, null);
                        }
                        ModifiableValueMap properties = croppingIdResource
                                .adaptTo(ModifiableValueMap.class);
                        properties.put("x", rect[0]);
                        properties.put("y", rect[1]);
                        properties.put("w", rect[2]);
                        properties.put("h", rect[3]);
                    }
                }
            }

            ModifiableValueMap properties = contentResource
                    .adaptTo(ModifiableValueMap.class);
            properties.put("jcr:lastModified", Calendar.getInstance());
            
            resourceResolver.commit();
        }
    }
}
