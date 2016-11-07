package service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import service.AfterTestsService;
import service.PrepareForTestsService;
import util.FilesAndDirectoryUtil;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AfterTestsServiceImpl implements AfterTestsService {

    private static final Logger LOGGER = LogManager.getLogger(AfterTestsServiceImpl.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Value("${regressionFrameworkLocation}")
    private String regressionFrameworkLocation;

    @Value("${regressionFrameworkLocationCMD}")
    private String regressionFrameworkLocationCMD;

    @Value("${regressionFrameworkTestOutputDirectoryName}")
    private String regressionFrameworkTestOutputDirectoryName;

    @Value("${webRegressionFrameworkResultsLocation}")
    private String webRegressionFrameworkResultsLocation;

    @Value("${webRegressionFrameworkResultsFileName}")
    private String webRegressionFrameworkResultsFileName;

    @Value("${regressionFrameworkLogFileName}")
    private String regressionFrameworkLogFileName;

    @Value("${regressionFrameworkLogFormatType}")
    private String regressionFrameworkLogFormatType;

    @Value("${regressionFrameworkLogSeparator}")
    private String regressionFrameworkLogSeparator;

    @Autowired
    PrepareForTestsService prepareForTestsService;

    private String newResultDirectoryName;
    private String newTableDateTime;
    private String newResultLogFileName;
    private String environment;

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void moveTestsOutputToResults() throws IOException {
        Path source = Paths.get(regressionFrameworkLocation, regressionFrameworkTestOutputDirectoryName);
        newResultDirectoryName = regressionFrameworkTestOutputDirectoryName + regressionFrameworkLogSeparator + getDateTimeFormatForDirectory();
        newTableDateTime = getDateTimeFormatForTable();
        Path target = Paths.get(webRegressionFrameworkResultsLocation, newResultDirectoryName);
        LOGGER.info("Moving folder: " + source.toString() + " to folder: " + target.toString());
        FilesAndDirectoryUtil.moveDirectory(source, target);
    }

    private String getDateTimeFormatForDirectory() {
        SimpleDateFormat sdfFolder = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        Date now = new Date();
        return sdfFolder.format(now);
    }

    private String getDateTimeFormatForTable() {
        SimpleDateFormat sdfTable = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        Date now = new Date();
        return sdfTable.format(now);
    }

    private Document getDocumentFromXML() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        LOGGER.info("Loading XML from: " + webRegressionFrameworkResultsLocation + webRegressionFrameworkResultsFileName);
        return documentBuilder.parse(webRegressionFrameworkResultsLocation + webRegressionFrameworkResultsFileName);
    }

    private void saveDocumentToXML(Document document, String resultFilePath) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        //initialize StreamResult with File object to save to file

        StreamResult result = new StreamResult(Paths.get(resultFilePath).toFile());
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
        LOGGER.info("Saving XML from: " + Paths.get(resultFilePath).toAbsolutePath().toString());
    }

    public void updateResultsXML() throws ParserConfigurationException, SAXException, IOException, TransformerException {
        newResultLogFileName = regressionFrameworkLogFileName + regressionFrameworkLogSeparator + environment + regressionFrameworkLogFormatType;

        Document document = getDocumentFromXML();
        document.getDocumentElement().normalize();
        Element resultsTag = document.getDocumentElement();

        Element newResult = document.createElement("Result");

        Element name = document.createElement("Name");
        name.setTextContent(newResultDirectoryName);

        Element date = document.createElement("Date");
        date.setTextContent(newTableDateTime);

        Element log = document.createElement("Log");
        log.setTextContent(newResultLogFileName);

        prepareForTestsService.setEnvironment(environment);
        prepareForTestsService.processMSBAdapterVersions();

        Element mas = document.createElement("Mas");
        mas.setTextContent(prepareForTestsService.getMasVersion());

        Element mpos = document.createElement("Mpos");
        mpos.setTextContent(prepareForTestsService.getMposVersion());

        newResult.appendChild(name);
        newResult.appendChild(date);
        newResult.appendChild(log);
        newResult.appendChild(mas);
        newResult.appendChild(mpos);

        resultsTag.insertBefore(newResult, resultsTag.getFirstChild());
        document.getDocumentElement().normalize();

        saveDocumentToXML(document, webRegressionFrameworkResultsLocation + webRegressionFrameworkResultsFileName);
    }

    public void deleteRecentTestsLogFile() throws IOException {
        Path logFilePath = Paths.get(regressionFrameworkLocation, newResultLogFileName);
        LOGGER.info("Deleting recent log file: " + logFilePath.toAbsolutePath().toString());
        Files.delete(logFilePath);
    }
}
