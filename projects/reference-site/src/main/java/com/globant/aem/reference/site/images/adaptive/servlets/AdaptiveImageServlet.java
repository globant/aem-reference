package com.globant.aem.reference.site.images.adaptive.servlets;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static java.lang.Integer.parseInt;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.ImageHelper;
import com.day.cq.commons.ImageResource;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.commons.AbstractImageServlet;
import com.day.cq.wcm.foundation.Image;
import com.day.image.Layer;
import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageService;
import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageServiceFacade;
import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageServiceGlobalConfig;

@SlingServlet(
  resourceTypes = "sling/servlet/default", 
  methods = "GET",
  selectors = { "adapt" },
  extensions = { "jpg", "png", "gif", "jpeg", "JPG", "PNG", "GIF", "JPEG" })
@Properties({
  @Property(
    name = "service.pid",
    value = "com.globant.aem.reference.site.images.adaptive.servlets.AdaptiveImageServlet",
    propertyPrivate = false),
    @Property(name = "service.vendor", value = "Globant-Reference", propertyPrivate = false) })
public class AdaptiveImageServlet extends AbstractImageServlet {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final long serialVersionUID = 3550347050814047028L;

  private static int concurrentThreads = 0;

  @Reference
  private AdaptiveImageServiceFacade serviceFacade;

  @Reference
  private AdaptiveImageServiceGlobalConfig globalConfigs;

  @Override
  protected Layer createLayer(ImageContext imagecontext) throws RepositoryException, IOException {
    return null;
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    try {
      synchronized (this) {
        concurrentThreads++;
        if (concurrentThreads > globalConfigs.getConcurrentThreads()) {
          response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
          return;
        }
      }

      String type;

      try {
        if (checkModifiedSince(request, response)) {
          return;
        }
      } catch (Exception e) {
        log.warn("AdaptiveImageServlet Exception: ", e);
      }

      type = getImageType(request.getRequestPathInfo().getExtension().toLowerCase());

      if (type == null) {
        response.sendError(404, "Image type not supported");
        return;
      }
      ImageContext context = new ImageContext(request, type);
      Layer layer = null;
      try {
        layer = createLayer(context);
      } catch (RepositoryException e) {
        log.warn("AdaptiveImageServlet Exception: ", e);
      }
      if (layer != null) {
        applyDiff(layer, context);
      }

      writeLayer(request, response, context, layer);

    } catch (RepositoryException e) {
      log.warn("AdaptiveImageServlet Exception: ", e);
    } finally {
      concurrentThreads--;
    }
  }

  @SuppressWarnings("static-access")
  @Override
  protected void writeLayer(SlingHttpServletRequest request, SlingHttpServletResponse response,
      ImageContext context, Layer layer) throws IOException, RepositoryException {

    AdaptiveImageService adaptiveImageService = serviceFacade.getServiceReference(request
        .getRequestPathInfo().getResourcePath());

    if (adaptiveImageService == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    List<String> supportedWidthsArray = adaptiveImageService.getAllowedWidthsArray();

    Float factor = adaptiveImageService.getCompressionFactor();
    List<String> allowedCroppingsArray = adaptiveImageService.getAllowedCroppingsArray();

    String[] selectors = request.getRequestPathInfo().getSelectors();

    if ((selectors.length < 2 || !isNumeric(selectors[1]))) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    } else if (!supportedWidthsArray.contains(selectors[1])) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    String resourcePath = getResourcePath(request);
    Resource resource = request.getResourceResolver().getResource(resourcePath);
    if (resource == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    Asset asset = resource.adaptTo(Asset.class);

    String mimeType = asset.getMimeType();
    if (ImageHelper.getExtensionFromType(mimeType) == null) {
      // get default mime type
      mimeType = "image/jpeg";
    }

    response.setContentType(mimeType);
    BufferedImage srcImage = ImageIO.read(asset.getOriginal().getStream());

    if (selectors.length == 4) {
      if (allowedCroppingsArray == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      String croppingId = null;
      String temp = selectors[3].toLowerCase() + ":";
      for (String item : allowedCroppingsArray) {
        if (item.toLowerCase().startsWith(temp)) {
          croppingId = item;
        }
      }

      Resource croppingResource = resource.getChild("jcr:content/croppings/"
          + selectors[3].toLowerCase());
      if (croppingResource != null) {
        ValueMap croppingRect = croppingResource.adaptTo(ValueMap.class);
        String coordX = croppingRect.get("x", String.class);
        String coordY = croppingRect.get("y", String.class);
        String width = croppingRect.get("w", String.class);
        String height = croppingRect.get("h", String.class);

        if (isNumeric(coordX) && isNumeric(coordY) && isNumeric(width)
            && isNumeric(height)) {
          srcImage = getImageCrop(srcImage, parseInt(coordX), parseInt(coordY),
              parseInt(width) - parseInt(coordX), parseInt(height) - parseInt(coordY));
        } else {
          srcImage = cropImageWithCroppingId(srcImage, croppingId);
        }
      } else {
        srcImage = cropImageWithCroppingId(srcImage, croppingId);
      }
    }

    int multiplier = 1;

    if (selectors.length > 2 && isNumeric(selectors[2])) {
      multiplier = parseInt(selectors[2]);
    }

    int width = parseInt(selectors[1]);
    width *= multiplier;

    double ratio = ((double) width) / ((double) srcImage.getWidth());
    int height = (int) (((double) srcImage.getHeight()) * ratio);

    java.awt.Image img = srcImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);

    BufferedImage dstImage = srcImage.getColorModel().hasAlpha() ? new BufferedImage(width, height,
        BufferedImage.TYPE_4BYTE_ABGR) : new BufferedImage(width, height,
        BufferedImage.TYPE_INT_RGB);

    dstImage.getGraphics().drawImage(img, 0, 0, width, height, null);

    Boolean enableCompression = adaptiveImageService.getCompressionEnabled();
    
    if (enableCompression) {
      ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageOutputStream ios = ImageIO.createImageOutputStream(baos);

      try {
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(param.MODE_EXPLICIT);
        param.setCompressionQuality(factor);

        writer.setOutput(ios);
        writer.write(null, new IIOImage(dstImage, null, null), param);

        ByteArrayInputStream bai = new ByteArrayInputStream(baos.toByteArray());
        dstImage = ImageIO.read(bai);
      } catch (Exception ex) {
        log.warn("AdaptiveImageServlet Exception: ", ex);
      } finally {
        ios.flush();
        ios.close();
        writer.dispose();
      }
    }

    flushResponse(response, mimeType, dstImage);
  }

  /**
   * Crops the image getting a centered cropping based on the aspect ratio assigned to the cropping
   * id.
   * 
   * @param srcImage the image source
   * @param croppingId the cropping id
   */
  private BufferedImage cropImageWithCroppingId(BufferedImage srcImage, String croppingId) {
    String[] items = croppingId.split(":");
    if (items.length == 3 && isNumeric(items[1]) && isNumeric(items[2])) {
      srcImage = getImageCrop(srcImage, parseInt(items[1]), parseInt(items[2]));
    }
    return srcImage;
  }

  /**
   * Write an image in the response.
   *
   * @param response the http response
   * @param mimeType the mime type of the response
   * @param dstImage the destination buffer 
   * @throws IOException if there is an error
   */
  private void flushResponse(SlingHttpServletResponse response, String mimeType,
      BufferedImage dstImage) throws IOException {
    OutputStream os = response.getOutputStream();
    String extension = ImageHelper.getExtensionFromType(mimeType);

    try {
      ImageIO.write(dstImage, extension, os);
    } catch (Exception ex) {
      log.warn("AdaptiveImageServlet Exception: ", ex);
    } finally {
      os.close();
    }

    response.flushBuffer();
  }

  /**
   * Get a crop of an image based on its aspect ratio.
   * 
   * @param source Source image
   * @param widthRatio Aspect ratio width
   * @param heightRatio Aspect ratio height
   */
  private BufferedImage getImageCrop(BufferedImage source, int widthRatio, int heightRatio) {
    int coordX;
    int coordY;
    int width;
    int height;

    if (widthRatio > heightRatio) {
      coordX = 0;
      width = source.getWidth();
      height = (heightRatio * source.getWidth()) / widthRatio;
      if (height > source.getHeight()) {
        height = source.getHeight();
      }
      coordY = (source.getHeight() - height) / 2;
    } else {
      coordY = 0;
      height = source.getHeight();
      width = (widthRatio * source.getHeight()) / heightRatio;
      if (width > source.getWidth()) {
        width = source.getWidth();
      }
      coordX = (source.getWidth() - width) / 2;
    }

    source = getImageCrop(source, coordX, coordY, width, height);
    return source;
  }

  /**
   * Get a crop of an image based on cropping coordinates.
   * 
   * @param source Source image
   * @param coordX Cropping rectangle left coordinate
   * @param coordY Cropping rectangle right coordinate
   * @param width Cropping rectangle width
   * @param height Cropping rectangle height
   */
  private BufferedImage getImageCrop(
      BufferedImage source, int coordX, int coordY, int width, int height) {
    source = source.getSubimage(coordX, coordY, width, height);
    return source;
  }

  /**
   * {@inheritDoc}
   *
   * <p/>
   * Override default ImageResource creation to support assets.
   */
  @Override
  protected ImageResource createImageResource(Resource resource) {
    return new Image(resource);
  }

  protected String getResourcePath(SlingHttpServletRequest request) {
    String[] selectors = request.getRequestPathInfo().getSelectors();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < selectors.length; i++) {
      sb.append(selectors[i]).append(".");
    }

    return request.getRequestPathInfo().getResourcePath().replace(sb.toString(), "");
  }
}
