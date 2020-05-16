package org.dedrakot.remover;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

public class HistImageMatcher {
    private static final int hBins = 50, sBins = 60;
    private final int[] histSize = {hBins, sBins};
    // hue varies from 0 to 179, saturation from 0 to 255
    private final float[] ranges = {0, 180, 0, 256};
    // Use the 0-th and 1-st channels
    private final int[] channels = {0, 1};

    private final Mat histBase;

    public HistImageMatcher(Mat base) {
        histBase = calcHist(base);
    }

    public boolean match(Mat other) {
        // 3 - CV_COMP_BHATTACHARYYA
        Mat hist = calcHist(other);
        double val = Imgproc.compareHist(histBase, hist, 3);
        hist.release();
        return val == 0.0;
    }

    private Mat calcHist(Mat src) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
        Mat hist = new Mat();
        List<Mat> hsvBaseList = Collections.singletonList(hsv);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(channels), new Mat(), hist, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);
        hsv.release();
        return hist;
    }
}
