package web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.WebSocketSession;
import service.WebSocketReindexTestsService;

@Controller
public class WebSocketReindexController {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketReindexController.class);

    @Autowired
    WebSocketReindexTestsService webSocketReindexTestsService;

    @RequestMapping(value = "/getZipper", produces = MediaType.TEXT_HTML_VALUE)
    public void getWebSocketReindexController(WebSocketSession session) {
        webSocketReindexTestsService.webSocketReindexTests(session);
    }
}
