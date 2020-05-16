package org.dedrakot.remover;

import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageUtils {
    public static final int BGR_DATA_SIZE64 = 64 * 64 *3;
    private static final Size S64 = new Size(64, 64);

    public static Mat gray(Mat src) {
        Mat mat = new Mat();
        Imgproc.cvtColor( src, mat, Imgproc.COLOR_BGR2GRAY);
        src.release();
        return mat;
    }

    public static void grayData(Mat src, byte[] buffer) {
        Mat t = ImageUtils.gray(src);
        t.get(0,0, buffer);
        t.release();
    }

    public static void bgrData(Mat src, byte[] buffer) {
        src.get(0,0, buffer);
        src.release();
    }

    public static Mat center(Mat src) {
        int centralPart = src.cols() / 3;
        int x1 = centralPart, x2 = src.cols() - centralPart;
        int y1 = (src.rows() - centralPart) / 2, y2 = y1 + centralPart;
        return src.submat(new Range(y1, y2), new Range(x1, x2));
    }

    public static Mat sqare64(Mat center) {
        Mat mat = new Mat();
        Imgproc.resize(center, mat, S64, 0, 0, Imgproc.INTER_CUBIC);
        return mat;
    }
}
