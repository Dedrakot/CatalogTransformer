package org.dedrakot.remover;

import org.opencv.core.Mat;

public interface ImageLoader {
    Mat loadImage(String imageAttributeValue);
}
