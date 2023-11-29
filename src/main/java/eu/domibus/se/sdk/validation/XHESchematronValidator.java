package eu.domibus.se.sdk.validation;

import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.core.spi.validation.UserMessageValidatorSpiException;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import net.sf.saxon.s9api.*;
import org.springframework.stereotype.Component;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class XHESchematronValidator implements UserMessageValidatorSpi {

    private static final DomibusLogger log = DomibusLoggerFactory.getLogger(XHESchematronValidator.class);

    private final Processor processor;
    private final XsltCompiler xsltCompiler;
    private XsltExecutable xsltExecutable;

    private static ThreadLocal<XsltTransformer> threadLocalTransformer;

    public XHESchematronValidator() {
        processor = new Processor(false);
        xsltCompiler = processor.newXsltCompiler();
        try {
            xsltExecutable = xsltCompiler.compile(new StreamSource("classpath:DIGG-XHE-Business-Rules.xslt"));

        } catch (SaxonApiException e) {
            throw new RuntimeException("Failed to start XHESchematronValidator: Could not compile xhe-schematron.xsl", e);
        }

        threadLocalTransformer = ThreadLocal.withInitial(() -> xsltExecutable.load());
    }

    @Override
    public void validateUserMessage(UserMessageDTO userMessageDTO) throws UserMessageValidatorSpiException {
        long startTime = System.currentTimeMillis();
        if (log.isTraceEnabled()) {
            log.trace("XHESchematronValidator.validateUserMessage starting validateUserMessage");
            // userMessage as JSON
            log.trace("userMessageDTO: " + userMessageDTO);
        }


    }

    @Override
    public void validatePayload(InputStream inputStream, String mimeType) throws UserMessageValidatorSpiException {
        long startTime = System.currentTimeMillis();
        log.trace("XHESchematronValidator.validatePayload starting validatePayload");

        if (log.isTraceEnabled()) {
            // input stream to string
            String payload = inputStreamToString(inputStream);
            log.trace("Payload inputStream as string: " + payload);
            inputStream = new ByteArrayInputStream(payload.getBytes());
        }

        XdmDestination chainResult = new XdmDestination();
        threadLocalTransformer.get().setDestination(chainResult);

        try {
            threadLocalTransformer.get().setSource(new StreamSource(inputStream));
            threadLocalTransformer.get().transform();
        } catch (SaxonApiException e) {
            throw new UserMessageValidatorSpiException("Failed to validate payload, could not run schematron transform", e);
        }

        List<String> errorList = new ArrayList<>();
        XdmNode rootNode = chainResult.getXdmNode();

        for (XdmSequenceIterator<XdmNode> it = rootNode.axisIterator(Axis.CHILD); it.hasNext(); ) {
            XdmItem item = it.next();
            XdmNode node = (XdmNode) item;
            if ("failed-assert".equals(node.getNodeName().getLocalName())) {
                errorList.add(node.getAttributeValue(new QName("test")));
            }
        }

        if (!errorList.isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Payload not valid. Found {} validation issues. It took {}ms to validate payload", errorList.size(), duration);
            throw new UserMessageValidatorSpiException("Found validations issues in payload: " + errorList);
        }

        if (log.isTraceEnabled()) {
            long duration = System.currentTimeMillis() - startTime;
            log.trace("Payload is valid. Validation took {}}ms.", duration);
        }
    }

    public String inputStreamToString(InputStream stream) {
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}