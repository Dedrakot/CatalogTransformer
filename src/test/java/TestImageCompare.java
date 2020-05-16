import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Range;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class TestImageCompare {

    @Disabled
    @Test
    // https://docs.opencv.org/3.4/d8/dc8/tutorial_histogram_comparison.html
    void sampleCode() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat srcBase = Imgcodecs.imread("tmp/hm.jpg");
        Mat srcTest1 = Imgcodecs.imread("tmp/hm2.jpg");
        Mat srcTest2 = Imgcodecs.imread("tmp/images/flat/310C00%200354768%20002%2095%201006.jpg");

        if (srcBase.empty() || srcTest1.empty() || srcTest2.empty()) {
            System.err.println("Cannot read the images");
            System.exit(0);
        }
        Mat hsvBase = new Mat(), hsvTest1 = new Mat(), hsvTest2 = new Mat();
        Imgproc.cvtColor( srcBase, hsvBase, Imgproc.COLOR_BGR2HSV );
        Imgproc.cvtColor( srcTest1, hsvTest1, Imgproc.COLOR_BGR2HSV );
        Imgproc.cvtColor( srcTest2, hsvTest2, Imgproc.COLOR_BGR2HSV );
        Mat hsvHalfDown = hsvBase.submat( new Range( hsvBase.rows()/2, hsvBase.rows() - 1 ), new Range( 0, hsvBase.cols() - 1 ) );
        int hBins = 50, sBins = 60;
        int[] histSize = { hBins, sBins };
        // hue varies from 0 to 179, saturation from 0 to 255
        float[] ranges = { 0, 180, 0, 256 };
        // Use the 0-th and 1-st channels
        int[] channels = { 0, 1 };
        Mat histBase = new Mat(), histHalfDown = new Mat(), histTest1 = new Mat(), histTest2 = new Mat();
        List<Mat> hsvBaseList = Collections.singletonList(hsvBase);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(channels), new Mat(), histBase, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histBase, histBase, 0, 1, Core.NORM_MINMAX);
        List<Mat> hsvHalfDownList = Collections.singletonList(hsvHalfDown);
        Imgproc.calcHist(hsvHalfDownList, new MatOfInt(channels), new Mat(), histHalfDown, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histHalfDown, histHalfDown, 0, 1, Core.NORM_MINMAX);
        List<Mat> hsvTest1List = Collections.singletonList(hsvTest1);
        Imgproc.calcHist(hsvTest1List, new MatOfInt(channels), new Mat(), histTest1, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histTest1, histTest1, 0, 1, Core.NORM_MINMAX);
        List<Mat> hsvTest2List = Collections.singletonList(hsvTest2);
        Imgproc.calcHist(hsvTest2List, new MatOfInt(channels), new Mat(), histTest2, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histTest2, histTest2, 0, 1, Core.NORM_MINMAX);
        for( int compareMethod = 0; compareMethod < 4; compareMethod++ ) {
            double baseBase = Imgproc.compareHist( histBase, histBase, compareMethod );
            double baseHalf = Imgproc.compareHist( histBase, histHalfDown, compareMethod );
            double baseTest1 = Imgproc.compareHist( histBase, histTest1, compareMethod );
            double baseTest2 = Imgproc.compareHist( histBase, histTest2, compareMethod );
            System.out.println("Method " + compareMethod + " Perfect, Base-Half, Base-Test(1), Base-Test(2) : " + baseBase + " / " + baseHalf
                    + " / " + baseTest1 + " / " + baseTest2);
        }
    }
}
