package web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {

    private static final Logger LOGGER = LogManager.getLogger(HomeController.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    private static final String OPERATING_SYSTEM = System.getProperty("os.name");

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        LOGGER.info(OPERATING_SYSTEM);
        LOGGER.info(osCMDPath);

        if (!OPERATING_SYSTEM.contains("Windows") && osCMDPath.equals("cmd.exe")) {
            return "error";
        } else {
            return "home";
        }
    }
}
