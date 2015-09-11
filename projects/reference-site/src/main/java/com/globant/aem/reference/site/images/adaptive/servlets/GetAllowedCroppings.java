package com.globant.aem.reference.site.images.adaptive.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;

import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageService;
import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageServiceFacade;

@Component(immediate = true, metatype = true, label = "Allowed Croppings Provider")
@Service
@Properties({
        @Property(name = "sling.servlet.paths", value = { "/bin/services/core/public/allowedcroppings" }),
        @Property(name = "sling.servlet.methods", value = { "GET" }) })
public class GetAllowedCroppings extends SlingSafeMethodsServlet {

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

        Resource resource = request.getResourceResolver().resolve(path);
        resource = resource.getChild("jcr:content/croppings");

        HashMap<String, String> croppings = new HashMap<>();

        if (resource != null) {
            for (Resource child : resource.getChildren()) {
                ValueMap croppingRect = child.adaptTo(ValueMap.class);
                if (croppingRect != null) {
                    String x = croppingRect.get("x", String.class);
                    String y = croppingRect.get("y", String.class);
                    String w = croppingRect.get("w", String.class);
                    String h = croppingRect.get("h", String.class);
                    croppings.put(child.getName(), x + "," + y + "," + w
                            + "," + h + "/");
                }
            }

        }

        AdaptiveImageService adaptiveImageService = serviceFacade
                .getServiceReference(path);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Writer out = response.getWriter();
        JSONWriter writer = new JSONWriter(out);
        try {
            writer.object();
            writer.key("imageCroppings");
            writer.array();

            if (adaptiveImageService != null
                    && adaptiveImageService.getAllowedCroppingsArray() != null) {
                List<String> allowedCroppings = adaptiveImageService
                        .getAllowedCroppingsArray();

                for (String cropping : allowedCroppings) {
                    writer.object();
                    String[] data = cropping.split(":");

                    String rect = data[1] + "," + data[2];
                    String ratio = rect;
                    String key = data[0].toLowerCase();
                    if (croppings.containsKey(key)) {
                        rect = croppings.get(key) + rect;
                    }
                    writer.key("id").value(data[0]);
                    writer.key("rect").value(rect);
                    writer.key("ratio").value(ratio);
                    writer.endObject();
                }
            }

            writer.endArray();
            writer.endObject();
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    }
}
