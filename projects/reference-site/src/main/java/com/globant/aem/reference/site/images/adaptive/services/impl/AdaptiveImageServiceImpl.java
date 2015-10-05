package com.globant.aem.reference.site.images.adaptive.services.impl;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;

import com.globant.aem.reference.site.images.adaptive.services.AdaptiveImageService;

@Component(
  immediate = true, 
  configurationFactory = true, 
  metatype = true, 
  policy = ConfigurationPolicy.REQUIRE, 
  label = "Adaptive Image Service", 
  description = "Provides the rules applied to images in a section of the repository.")
@Service(value = AdaptiveImageService.class)
public class AdaptiveImageServiceImpl implements AdaptiveImageService {

  @Property(label = "Section", description = "Section in which this configuration is applied.")
  public static final String PROPERTY_SECTION = "section";

  @Property(
    unbounded = PropertyUnbounded.ARRAY, 
    label = "Supported Formats", 
    description = "List of formats this component is permitted to generate. Order by min-width size"
                + " when using min-width media queries, and by smallest width size when using "
                + "max-width media queries")
  public static final String PROPERTY_SUPPORTED_FORMATS = "image.supported.formats";

  @Property(
    unbounded = PropertyUnbounded.ARRAY, 
    label = "Supported Image types", 
    description = "List of image types this component is permitted to generate. Referencing "
                + "directly the image file extension")
  public static final String PROPERTY_SUPPORTED_IMAGES_TYPES = "image.supported.type.images";

  @Property(
    unbounded = PropertyUnbounded.ARRAY,
    label = "Supported Croppings",
    description = "List of croppings allowed in this section of the repository.")
  public static final String PROPERTY_SUPPORTED_CROPPINGS = "image.supported.croppings";

  @Property(
    boolValue = false,
    label = "Enable Compression",
    description = "Flag that indicates whether image compression is enabled.")
  public static final String PROPERTY_ENABLE_COMPRESION = "image.enable.compression";

  @Property(
    floatValue = 1F,
    label = "Compression Factor",
    description = "Compression factor that determines compression level in case it is enabled. "
                + "Enter 1 for no compression.")
  public static final String PROPERTY_COMPRESION_FACTOR = "image.compression.factor";

  private String section;

  private List<String> supportedFormatsArray;

  private List<String> supportedImageTypes;

  private List<String> supportedCroppingsArray;

  private Boolean enabledCompression;

  private Float compressionFactor;

  @Activate
  protected void activate(final BundleContext bundleContext, final Map<String, Object> properties) {
    configureService(properties);
  }

  private void configureService(Map<String, Object> properties) {
    section = PropertiesUtil.toString(properties.get(PROPERTY_SECTION), "");
    supportedFormatsArray = 
        asList(PropertiesUtil.toStringArray(properties.get(PROPERTY_SUPPORTED_FORMATS)));

    if (properties.get(PROPERTY_SUPPORTED_IMAGES_TYPES) != null) {
      supportedImageTypes = 
          asList(PropertiesUtil.toStringArray(properties.get(PROPERTY_SUPPORTED_IMAGES_TYPES)));
    }

    if (supportedImageTypes == null || supportedImageTypes.size() == 0) {
      supportedImageTypes = new ArrayList<>(asList("jpg", "png", "bmp", "jpeg"));
    }

    enabledCompression = 
        PropertiesUtil.toBoolean(properties.get(PROPERTY_ENABLE_COMPRESION), false);
    
    compressionFactor = 
        (float) PropertiesUtil.toDouble(
            properties.get(AdaptiveImageServiceImpl.PROPERTY_COMPRESION_FACTOR), 0D);

    if (properties.get(PROPERTY_SUPPORTED_CROPPINGS) != null) {
      supportedCroppingsArray = 
          asList(PropertiesUtil.toStringArray(properties.get(PROPERTY_SUPPORTED_CROPPINGS)));
    } else {
      supportedCroppingsArray = null;
    }
  }

  @Override
  public String getSection() {
    return this.section;
  }

  @Override
  public String getAllowedWidthsString() {
    List<String> supportedWidthsArray = getAllowedWidthsArray();

    // TODO: Removing the brackets for now just to avoid errors in the
    // sites.
    String supportedWidthsTemp = Arrays.toString(supportedWidthsArray.toArray());
    supportedWidthsTemp = supportedWidthsTemp.substring(1);
    supportedWidthsTemp = supportedWidthsTemp.substring(0, supportedWidthsTemp.length() - 1);

    return supportedWidthsTemp;
  }

  @Override
  public Boolean getCompressionEnabled() {
    return enabledCompression;
  }

  @Override
  public Float getCompressionFactor() {
    return compressionFactor;
  }

  @Override
  public List<String> getAllowedWidthsArray() {
    List<String> result = new ArrayList<String>();
    List<Format> supportedFormats = getSupportedFormats();
    for (Format format : supportedFormats) {
      result.add(format.getWidth());
    }
    return result;
  }

  @Override
  public List<String> getAllowedCroppingsArray() {
    return supportedCroppingsArray;
  }

  @Override
  public List<Format> getSupportedFormats() {
    List<Format> result = new ArrayList<AdaptiveImageService.Format>();
    for (String formats : supportedFormatsArray) {
      Format formatResult = new ImplFormat(formats);
      result.add(formatResult);
    }
    return result;
  }

  /**
   * Returns true if non of the supported format is empty.
   */
  public boolean isMediaSet(List<Format> supportedFormats) {
    for (Format format : supportedFormats) {
      if (StringUtils.isEmpty(format.getMedia())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public List<String> getSupportedImageTypes() {

    return supportedImageTypes;
  }

  public static class ImplFormat implements AdaptiveImageService.Format {

    String width;
    String media;
    String[] pixelDensity;
    String fmt;

    /** 
     * Basic implementation of AdaptiveImageService.Format 
     **/
    public ImplFormat(String formats) {
      String[] formatArray = formats.split("\\|");
      for (String format : formatArray) {
        if (format.startsWith(AdaptiveImageService.FORMAT_WIDTH)) {
          setWidth(getFormatValue(format, AdaptiveImageService.FORMAT_WIDTH));
        } else if (format.startsWith(AdaptiveImageService.FORMAT_MEDIA)) {
          setMedia(getFormatValue(format, AdaptiveImageService.FORMAT_MEDIA));
        } else if (format.startsWith(AdaptiveImageService.FORMAT_PIXEL_DENSITY)) {
          setPixelDensity(getFormatValue(format, AdaptiveImageService.FORMAT_PIXEL_DENSITY));
        } else {
          setWidth(format);
        }
      }
      setFmt(formats);
    }

    @Override
    public String getWidth() {
      return width;
    }

    public void setWidth(String width) {
      this.width = width;
    }

    @Override
    public String getMedia() {
      return media;
    }

    public void setMedia(String media) {
      this.media = media;
    }

    @Override
    public String[] getPixelDensity() {
      return (pixelDensity != null) ? pixelDensity : new String[] { "1" };
    }

    public void setPixelDensity(String pixelDensity) {
      this.pixelDensity = pixelDensity.split(",");
    }

    public String getFormatValue(String format, String key) {
      return format.substring(format.indexOf(key) + key.length());
    }

    public String getFmt() {
      return fmt;
    }

    public void setFmt(String fmt) {
      this.fmt = fmt;
    }

  }

}
