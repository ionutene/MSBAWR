package web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import service.CopyZipFromJenkinsService;

@RestController
public class CopyZipFromJenkinsController {

    private static final Logger LOGGER = LogManager.getLogger(CopyZipFromJenkinsController.class);

    @Autowired
    CopyZipFromJenkinsService copyZipFromJenkinsService;

    @RequestMapping(value = "/getZip", produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String getCheckboxesViaAjax() {
        return copyZipFromJenkinsService.getInstallPath();
    }
}
