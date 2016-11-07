package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VariousUtil {

    private static final Logger LOGGER = LogManager.getLogger(VariousUtil.class);
    private static final String DATE_FORMAT_FOR_DIRECTORY = "dd-MMM-yyyy_HH-mm-ss";
    private static final String DATE_FORMAT_FOR_TABLE = "dd-MMM-yyyy HH:mm:ss";

    public static String getDateTimeFormat(Date now, boolean forDirectory) {
        SimpleDateFormat sdfFolder;
        if (forDirectory) {
            sdfFolder = new SimpleDateFormat(DATE_FORMAT_FOR_DIRECTORY);
        } else {
            sdfFolder = new SimpleDateFormat(DATE_FORMAT_FOR_TABLE);
        }
        return sdfFolder.format(now);
    }

    public static Document getDocumentFromXML(Path xmlPath) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        LOGGER.info("Loading XML from: " + xmlPath.toAbsolutePath().toString());
        return documentBuilder.parse(xmlPath.toFile());
    }

    public static void saveDocumentToXML(Document document, Path xmlPath) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(xmlPath.toFile());
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
        LOGGER.info("Saving XML from: " + xmlPath.toAbsolutePath().toString());
    }

}
