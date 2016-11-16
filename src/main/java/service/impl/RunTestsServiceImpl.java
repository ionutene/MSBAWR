package service.impl;

import data.SearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import service.AfterTestsService;
import service.RunTestsService;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RunTestsServiceImpl implements RunTestsService {

    private static final Logger LOGGER = LogManager.getLogger(RunTestsServiceImpl.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Value("${os.cmd.cd}")
    private String osCMDCd;

    @Value("${os.cmd.andJar}")
    private String osCMDAndJar;

    @Value("${os.cmd.anyJar}")
    private String osCMDAnyJar;

    @Value("${regressionFrameworkLocation}")
    private String regressionFrameworkLocation;

    private String processedArguments;

    @Autowired
    AfterTestsService afterTestsService;

    private SimpMessagingTemplate messagingTemplate;
    private SearchCriteria searchCriteria;
    @Value("${stompDestination}")
    private String stompDestination;

    public void setSimpMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void setSearchCriteria(SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public void runTests()
            throws IOException, ParserConfigurationException, SAXException, TransformerException {

        List<Path> paths = FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, osCMDAnyJar);

        if (paths.size() != 1) throw new IOException("Too many .jar files!");

//      TODO remove CTA test hardcoding
        String commandToExecute = osCMDCd + regressionFrameworkLocation + osCMDAndJar + paths.get(0) + " " + searchCriteria.getEnvironment() + " cta";
        LOGGER.info(commandToExecute);
        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), stompDestination, messagingTemplate);
        while (p.isAlive()) {/* wait until process finishes */}
//      Then clean-up
        finishTestsAndCleanUp();
    }

    public void parseArguments() {
        boolean isReallyUnique = true;
        Set<String> uniqueTests = new HashSet<>();
        Set<String> notSoUniqueTests = new HashSet<>();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilderPreTag = new StringBuilder();

        for (String test : searchCriteria.getCheckBoxes()) {
            for (String otherTest : searchCriteria.getCheckBoxes()) {
                if (!test.equals(otherTest) && otherTest.contains(test + ".")) {
                    notSoUniqueTests.add(otherTest);
                    for (String innerUniqueness : uniqueTests) {
                        if (test.contains(innerUniqueness)) {
                            isReallyUnique = false;
                        }
                    }
                    if (isReallyUnique) {
                        uniqueTests.add(test);
/*                        LOGGER.info("Found test package <" + test + ">");
                        messagingTemplate.convertAndSend(stompDestination, "Found test package &lt;" + test + "&gt;\n");*/
                    }
                    isReallyUnique = true;
                }
            }
        }

        List<String> finalTests = searchCriteria.getCheckBoxes().stream().filter(test -> !notSoUniqueTests.contains(test)).collect(Collectors.toList());
        /*                LOGGER.info("Test was put in final list [" + test + "]");
                        messagingTemplate.convertAndSend(stompDestination, "Test was put in final list [" + test + "]\n");*/

        for (String test : finalTests) {
            if (uniqueTests.contains(test)) {
                stringBuilder.append("<").append(test).append("> ");
                stringBuilderPreTag.append("&lt;").append(test).append("&gt; ");
            } else {
                stringBuilder.append(test).append(" ");
                stringBuilderPreTag.append(test).append(" ");
            }
        }
        String argsToExecute = stringBuilder.toString();
//      TODO implement HTML special character escape to avoid 2 x StringBuilders
        LOGGER.info("args[]: " + argsToExecute);
        messagingTemplate.convertAndSend(stompDestination, "args[]: " + stringBuilderPreTag.toString() + "\n");
        this.processedArguments = argsToExecute;
    }

    private void finishTestsAndCleanUp() throws IOException, TransformerException, SAXException, ParserConfigurationException {
        afterTestsService.setEnvironment(searchCriteria.getEnvironment());
        afterTestsService.init();
//      If tests weren't stopped manually, move output results and update results.xml
        if (!afterTestsService.wereTestsStoppedManually()) {
            messagingTemplate.convertAndSend(stompDestination, "Tests were finished successfully, moving results and updating results.xml!");
            afterTestsService.moveTestsOutputToResults();
            afterTestsService.updateResultsXML();
        } else {
//            Clean-up result-output folder
            messagingTemplate.convertAndSend(stompDestination, "Tests were stopped, clean-up ensues!");
            afterTestsService.deleteTestOutputResults();
        }
//      Delete recent log file regardless of tests run time
        afterTestsService.deleteRecentTestsLogFile();
    }
}
