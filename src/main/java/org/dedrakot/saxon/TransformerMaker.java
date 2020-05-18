package org.dedrakot.saxon;

import net.sf.saxon.s9api.*;

import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class TransformerMaker {
    private final Processor processor = new Processor(false);
    private final XsltCompiler compiler = processor.newXsltCompiler();

    public TransformerMaker(String basePath) {
        compiler.setURIResolver(createURIResolver(basePath));
    }

    public Processor getProcessor() {
        return processor;
    }

    public XsltCompiler getCompiler() {
        return compiler;
    }

    public XsltTransformer createTransformer(InputStream xslInput) throws SaxonApiException {
        XsltExecutable exec = compiler.compile(new StreamSource(xslInput));
        return exec.load();
    }

    private static URIResolver createURIResolver(String basePath) {
        File xslFolder = new File(basePath);
        if (!xslFolder.isDirectory())
            throw new IllegalArgumentException("xsl folder not exists on base path");

        String realBase = xslFolder.getAbsolutePath();

        return (href, base) -> {
            File sourceFile = new File(href);
            if(!sourceFile.canRead())
                sourceFile = new File(realBase, href);
            try {
                return new StreamSource(new FileInputStream(sourceFile));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    public XsltTransformer setDestination(XsltTransformer transformer, OutputStream os) {
        transformer.setDestination(processor.newSerializer(os));
        return transformer;
    }
}
