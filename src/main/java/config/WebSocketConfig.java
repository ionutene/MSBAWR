package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.*;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@ComponentScan({"web"})
public class WebSocketConfig extends WebSocketMessageBrokerConfigurationSupport implements WebSocketMessageBrokerConfigurer  {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketConfig.class);

    private static final StringMessageConverter MESSAGE_CONVERTER;
    private static final int MESSAGE_LIMIT = 8 * 1024 * 1024;

    static {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.TEXT_PLAIN);

        MESSAGE_CONVERTER = new StringMessageConverter();
        MESSAGE_CONVERTER.setContentTypeResolver(resolver);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        stompEndpointRegistry.addEndpoint("/ws-stomp-stockjs").withSockJS()
                .setStreamBytesLimit(MESSAGE_LIMIT).setHttpMessageCacheSize(MESSAGE_LIMIT);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration) {
        webSocketTransportRegistration.setMessageSizeLimit(MESSAGE_LIMIT)
                .setSendBufferSizeLimit(MESSAGE_LIMIT);
        super.configureWebSocketTransport(webSocketTransportRegistration);
        LOGGER.info("WebSocket configured with: " + MESSAGE_LIMIT);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration channelRegistration) {
//        Default implementation 2 x Number of Cores
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration channelRegistration) {
//        Default implementation 2 x Number of Cores
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> handlerMethodArgumentResolvers) {

    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlerMethodReturnValueHandlers) {

    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        messageConverters.add(MESSAGE_CONVERTER);
        return false;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
        messageBrokerRegistry.enableSimpleBroker("/topic/");
        messageBrokerRegistry.setApplicationDestinationPrefixes("/app");
    }
}
