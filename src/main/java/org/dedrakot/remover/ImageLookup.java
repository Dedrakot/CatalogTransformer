package org.dedrakot.remover;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public class ImageLookup {
    private final List<String> imageAttributes;

    public ImageLookup(List<String> imageAttributes) {
        this.imageAttributes = imageAttributes;
    }

    public void inspect(InputStream catalogInputStream, Consumer<String> fileNameConsumer) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = builderFactory.newDocumentBuilder();

        Document xmlDocument = builder.parse(catalogInputStream);

        XPath xPath = XPathFactory.newInstance().newXPath();

        findImages(xPath, xmlDocument, fileNameConsumer);
    }

    private void findImages(XPath xPath, Document xmlDocument, Consumer<String> consumer) throws XPathExpressionException {
        findImages("/operations/add/product/", xPath, xmlDocument, consumer);
        findImages("/operations/add/variant/", xPath, xmlDocument, consumer);
    }

    private void findImages(String prefix, XPath xPath, Document xmlDocument, Consumer<String> consumer) throws XPathExpressionException {
        for(String attributeName: imageAttributes) {
            readQuery(prefix + attributeName, xPath, xmlDocument, consumer);
        }
    }

    private static void readQuery(String query, XPath xPath, Document xmlDocument, Consumer<String> consumer) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPath.compile(query).evaluate(xmlDocument, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            String value = nodeList.item(i).getTextContent();
            consumer.accept(value);
        }

    }
}
