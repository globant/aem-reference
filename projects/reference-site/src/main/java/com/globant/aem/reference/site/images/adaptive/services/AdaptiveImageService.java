package com.globant.aem.reference.site.images.adaptive.services;

import java.util.List;

public interface AdaptiveImageService {
    
    String FORMAT_WIDTH = "width:";
    String FORMAT_MEDIA = "media:";
    String FORMAT_PIXEL_DENSITY = "pixel-density:";

    /**
     * Returns the section of the repository in which these settings should be applied
     * @return
     */
    String getSection();

    /**
     * True if the compression is enabled
     * @return
     */
    Boolean getCompressionEnabled();

    /**
     * Compression factor for this section of the repository.
     * @return
     */
    Float getCompressionFactor();

    /**
     * Returns the array of available widths in a plain string compatible with javascript.
     * @return
     */
    String getAllowedWidthsString();

    /**
     * Returns the list of available widths in this section of the repository
     * @return
     */
    List<String> getAllowedWidthsArray();

    /**
     * Returns the list of available croppings in this section of the repository
     * @return
     */
    List<String> getAllowedCroppingsArray();
    
    /**
     * Returns the list of supported images extension files
     * @return
     */
    List<String> getSupportedImageTypes();

    List<Format> getSupportedFormats();

    boolean isMediaSet(List<Format> supportedFormats);

    interface Format {
        String getWidth();

        String getMedia();

        String[] getPixelDensity();

        String getFmt();

        void setFmt(String fmt);

        void setWidth(String width);

        void setMedia(String media);

        void setPixelDensity(String pixelDensity);

        String getFormatValue(String format, String key);
    }

}
