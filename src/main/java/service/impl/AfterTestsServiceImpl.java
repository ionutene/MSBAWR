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
import util.VariousUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Value("${regressionFrameworkIndexFile}")
    private String regressionFrameworkIndexFile;

    @Autowired
    PrepareForTestsService prepareForTestsService;

    private String newResultDirectoryName;
    private String newTableDateTime;
    private String newResultLogFileName;
    private String environment;
    private Path testSourcePath;
    private Path testTargetPath;
    private Path logFilePath;
    private Path resultXMLPath;

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void init() {
        testSourcePath = Paths.get(regressionFrameworkLocation, regressionFrameworkTestOutputDirectoryName);
        Date date = new Date();
        newResultDirectoryName = regressionFrameworkTestOutputDirectoryName + regressionFrameworkLogSeparator + VariousUtil.getDateTimeFormat(date, true);
        newTableDateTime = VariousUtil.getDateTimeFormat(date, false);
        testTargetPath = Paths.get(webRegressionFrameworkResultsLocation, newResultDirectoryName);
        newResultLogFileName = regressionFrameworkLogFileName + regressionFrameworkLogSeparator + environment + regressionFrameworkLogFormatType;
        logFilePath = Paths.get(regressionFrameworkLocation, newResultLogFileName);
        resultXMLPath = Paths.get(webRegressionFrameworkResultsLocation, webRegressionFrameworkResultsFileName);
    }

    public void moveTestsOutputToResults() throws IOException {
        LOGGER.info("Moving folder: " + testSourcePath.toString() + " to folder: " + testTargetPath.toString());
        FilesAndDirectoryUtil.moveDirectory(testSourcePath, testTargetPath);
    }

    public void updateResultsXML() throws ParserConfigurationException, SAXException, IOException, TransformerException {
        Document document = VariousUtil.getDocumentFromXML(resultXMLPath);
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

        VariousUtil.saveDocumentToXML(document, resultXMLPath);
    }

    public void deleteRecentTestsLogFile() throws IOException {
        LOGGER.info("Deleting recent log file: " + logFilePath.toAbsolutePath().toString());
        Files.delete(logFilePath);
    }

    @Override
    public boolean wereTestsStoppedManually() {
//      Check if test-output exists
        if (Files.exists(testSourcePath)) {
//          Check if index.html is no were to be found inside test-output
            if (!Files.exists(testSourcePath.resolve(regressionFrameworkIndexFile))) {
//              That means that tests weren't finished for some reason
                return true;
            }
        } else return true;

        return false;
    }

    public void deleteTestOutputResults() throws IOException {
        if (Files.exists(testSourcePath)) {
            LOGGER.info("Deleting recent test source directory: " + testSourcePath.toAbsolutePath().toString());
            FilesAndDirectoryUtil.deleteDirectory(testSourcePath);
        }
    }
}
