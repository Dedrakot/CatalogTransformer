import org.dedrakot.remover.StoredImagesLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CopyResultList {

    @Disabled
    @Test
    void copyImagesFromResults() throws IOException {
        String path = "tmp/result.txt";
        Path destDir = Paths.get("tmp/images/flat");
        Path imageDir = Paths.get("tmp/images/catalog");
        if (!Files.exists(destDir)) {
            Files.createDirectories(destDir);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))) {
            String attributeValue;
            int counter = 1;
            while((attributeValue = reader.readLine())!=null) {
                Path subPath = Paths.get(StoredImagesLoader.getLocalFileName(StoredImagesLoader.getImageUrl
                        (attributeValue, StoredImagesLoader.DEFAULT_PREFIX), StoredImagesLoader.DEFAULT_PREFIX));

                Path imgPath = imageDir.resolve(subPath);
                Files.createLink(destDir.resolve(counter++ + "_" + subPath.getFileName()), imgPath);
            }
        }
    }
}
