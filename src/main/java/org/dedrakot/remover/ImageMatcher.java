package org.dedrakot.remover;

import org.opencv.core.Mat;

public interface ImageMatcher {
    boolean match(Mat other);
}
