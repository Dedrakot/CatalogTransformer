package org.dedrakot.remover;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InterruptedException, XPathExpressionException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String imagesDir = "tmp/images/catalog";
        String productCatalogXml = "tmp/products.xml";
        String resultsFileName = "tmp/result.txt";
        String defaultSample = args.length > 0 ? args[0] : "tmp/hm.jpg";

        final HistImageMatcher matcher = createMatcher(defaultSample);

        final ConcurrentHashMap<String, Boolean> matchedMap = new ConcurrentHashMap<>();
        final ImageLookup imageLookup = new ImageLookup(Arrays.asList("model_image", "cutout_image"));
        final ImageLoader imageLoader = new StoredImagesLoader(Paths.get(imagesDir));
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (FileInputStream fileIS = new FileInputStream(productCatalogXml)) {
            imageLookup.inspect(fileIS, imageAttrValue -> {
                if (!matchedMap.containsKey(imageAttrValue)) {
                    executor.submit(() -> {
                        matchedMap.computeIfAbsent(imageAttrValue, iav -> {
                            Mat mat = imageLoader.loadImage(iav);
                            if (mat.empty()) {
                                System.out.println(iav);
                                return false;
                            }
                            Boolean result = matcher.match(mat);
                            mat.release();
                            return result;
                        });
                    });
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.HOURS);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFileName, StandardCharsets.UTF_8))) {
            matchedMap.entrySet().stream().filter(Map.Entry::getValue).forEach(e -> {
                try {
                    writer.write(e.getKey());
                    writer.newLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static HistImageMatcher createMatcher(String fileName) {
        Mat srcBase = Imgcodecs.imread(fileName);
        HistImageMatcher ret = new HistImageMatcher(srcBase);
        srcBase.release();
        return ret;
    }

}
