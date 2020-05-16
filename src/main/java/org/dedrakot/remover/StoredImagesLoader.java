package org.dedrakot.remover;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.file.Path;

public class StoredImagesLoader implements ImageLoader {
    public static final String DEFAULT_PREFIX = "http:";
    private final Path imageDir;

    public StoredImagesLoader(Path imageDir) {
        this.imageDir = imageDir;
    }

    @Override
    public Mat loadImage(String imageAttributeValue) {
        String imageUrl = getImageUrl(imageAttributeValue, DEFAULT_PREFIX);
        return Imgcodecs.imread(imageDir.resolve(getLocalFileName(imageUrl, DEFAULT_PREFIX)).toString());
    }

    public static String getImageUrl(String imageAttributeValue, String prefix) {
        return URIUtils.encodeUri(prefix + imageAttributeValue);
    }

    public static String getLocalFileName(String imageUrl, String prefix) {
        return imageUrl.substring(prefix.length() + 2);
    }
}
