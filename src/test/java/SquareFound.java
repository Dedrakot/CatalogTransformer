import org.dedrakot.remover.ImageUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SquareFound {

    @BeforeAll
    static void loadLib() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Disabled
    @Test
    void foundSquare() {
        Imgcodecs.imwrite("tmp/square.png", center("tmp/hm.jpg"));
    }

    @Disabled
    @Test
    void scale() {
        Mat src = Imgcodecs.imread("tmp/square.png");
        Imgcodecs.imwrite("tmp/square2.png", ImageUtils.sqare64(src));
    }

    @Disabled
    @Test
    void scaleResults() throws IOException {
        Path destDir = Paths.get("tmp/images/square2");
        Path path = Paths.get("tmp/images/flat2");

        if (!Files.exists(destDir)) {
            Files.createDirectory(destDir);
        }

        try (var ds = Files.newDirectoryStream(path)) {
            ds.forEach(f -> {
                Mat mat = square64(f.toString());
                String fileName = f.getFileName().toString();
                String resultFileName = destDir.resolve(fileName.substring(0, fileName.length() - 4) + ".png").toString();
                Imgcodecs.imwrite(resultFileName, mat);
                mat.release();
            });
        }
    }

    @Disabled
    @Test
    void graySquare() {
        Mat t = ImageUtils.gray(Imgcodecs.imread("tmp/hm.jpg"));
        HighGui.imshow( "Source image", t );
        HighGui.waitKey(0);
    }

    private static Mat square64(String fileName) {
        Mat center = center(fileName);
        Mat ret = ImageUtils.sqare64(center);
        center.release();
        return ret;
    }

    public static Mat center(String fileName) {
        Mat src = Imgcodecs.imread(fileName);
        Mat squareMat = ImageUtils.center(src);
        src.release();
        return squareMat;
    }

}