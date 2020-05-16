import org.dedrakot.remover.ImageLookup;
import org.dedrakot.remover.StoredImagesLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadProductCatalogImages {

    @Disabled
    @Test
    void loadImages() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, InterruptedException {
        ImageLookup imageLookup = new ImageLookup(Arrays.asList("model_image", "cutout_image"));

        final String prefix = StoredImagesLoader.DEFAULT_PREFIX;
        final Path destDir = Paths.get("tmp/images/catalog");
        if (!Files.exists(destDir)) {
            Files.createDirectories(destDir);
        }
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final AtomicInteger imagesLoaded = new AtomicInteger(0);

        ConcurrentHashMap<String, Boolean> loadedImages = new ConcurrentHashMap<>();

        final int[] counters = {0, 0};

        try (FileInputStream fileIS = new FileInputStream("tmp/products.xml")) {
            imageLookup.inspect(fileIS, imageAttrValue -> {
                counters[0]++;
                if (imageAttrValue.startsWith("//") && imageAttrValue.length() > 2) {
                    counters[1]++;
                    executor.submit(() -> {
                        String imageUrl = StoredImagesLoader.getImageUrl(imageAttrValue, prefix);
                        loadedImages.computeIfAbsent(imageUrl, urlS -> {
                            try {
                                URL url = new URL(urlS);
                                String path = StoredImagesLoader.getLocalFileName(urlS, prefix);
                                Path destFile = destDir.resolve(path);
                                Files.createDirectories(destFile.getParent());
                                try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                                     FileChannel fileChannel = new FileOutputStream(destFile.toFile()).getChannel()) {
                                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                                    return true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        });
                    });
                } else {
                    System.out.println("Different path format: " + imageAttrValue);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.HOURS);

        System.out.println("Images found; " + counters[0] +", images with expected name; " + counters[1]);
    }
}
