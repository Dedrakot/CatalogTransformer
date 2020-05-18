package org.dedrakot.remover;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltTransformer;
import org.dedrakot.saxon.NodeContentBanFunction;
import org.dedrakot.saxon.TransformerMaker;

import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class TransformCatalog {

    public static void main(String[] args) throws IOException, SaxonApiException {
        String bannedListPath = "tmp/result.txt";
        String productCatalogPath = "tmp/products.xml";
        String resultCatalogPath = "tmp/changedProducts.xml";
        Set<String> bannedImages = loadBannedImages(bannedListPath);

        TransformerMaker transformerMaker = new TransformerMaker(".");
        NodeContentBanFunction nodeContentBanFunction = new NodeContentBanFunction(bannedImages);
        transformerMaker.getProcessor().registerExtensionFunction(nodeContentBanFunction);

        try (InputStream xslInput = TransformCatalog.class.getResourceAsStream("/removeImages.xsl");
             InputStream productCatalog = new BufferedInputStream(new FileInputStream(productCatalogPath));
             OutputStream result = new BufferedOutputStream(new FileOutputStream(resultCatalogPath))) {

            XsltTransformer transformer = transformerMaker.createTransformer(xslInput);
            transformer.setSource(new StreamSource(productCatalog));
            transformerMaker.setDestination(transformer, result);
            transformer.transform();
        }
        System.out.println("Banned count: " + nodeContentBanFunction.getCounter());
    }

    private static Set<String> loadBannedImages(String file) throws IOException {
        return new HashSet<>(Files.readAllLines(Paths.get(file)));

    }
}
