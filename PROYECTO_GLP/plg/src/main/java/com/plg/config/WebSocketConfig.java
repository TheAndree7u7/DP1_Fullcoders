package com.plg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Bean
    public TaskScheduler webSocketHeartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Configurar el broker con un TaskScheduler para los heartbeats
        config.enableSimpleBroker("/topic")
            .setHeartbeatValue(new long[] {10000, 10000})
            .setTaskScheduler(webSocketHeartbeatTaskScheduler());
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Usar setAllowedOriginPatterns en lugar de setAllowedOrigins
                .withSockJS()
                .setStreamBytesLimit(512 * 1024) // 512KB
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000); // 30 segundos
    }
    
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(1024 * 1024); // 1MB
        registration.setSendBufferSizeLimit(1024 * 1024); // 1MB
        registration.setSendTimeLimit(20 * 1000); // 20 segundos
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor(createExecutor("clientInbound", 8));
    }
    
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor(createExecutor("clientOutbound", 8));
    }
    
    private ThreadPoolTaskExecutor createExecutor(String threadNamePrefix, int corePoolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(threadNamePrefix + "-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }
}