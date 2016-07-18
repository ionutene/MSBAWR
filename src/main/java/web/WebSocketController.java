package web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import service.WebSocketReindexTestsService;

@Controller
public class WebSocketController {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketController.class);

    @Autowired
    WebSocketReindexTestsService webSocketReindexTestsService;

    @Autowired
    private SimpMessagingTemplate template;

    @Async
    @MessageMapping("/section")
//    @SendTo("/topic/message")
    public void result(String message) throws Exception {


//        WebSocketConfig.JOBS.add()
        if (message != null && message.equals("reindex")) LOGGER.info("LOLEX!");
        template.convertAndSend("/topic/message", "First Try!");
        template.convertAndSend("/topic/message", "Second Try!");
        webSocketReindexTestsService.webSocketReindexTests(template);
    }
}
