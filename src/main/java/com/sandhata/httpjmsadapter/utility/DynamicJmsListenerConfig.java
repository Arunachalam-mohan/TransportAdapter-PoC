package com.sandhata.httpjmsadapter.utility;

import com.sandhata.httpjmsadapter.service.EMSReceiver;
import com.sandhata.httpjmsadapter.service.EMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import java.util.List;

@Configuration
public class DynamicJmsListenerConfig implements JmsListenerConfigurer {

    @Autowired
    private ServiceConfigLoader serviceConfigLoader;

    @Autowired
    private EMSReceiver emsReceiver;  // The class containing the message handling logic

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        List<String> topicNames = serviceConfigLoader.getAllTopicNames();

        for (String topicName : topicNames) {
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            endpoint.setId(topicName + "-listener");
            endpoint.setDestination(topicName);
            endpoint.setMessageListener(message -> emsReceiver.receiveMessage(message));
            registrar.registerEndpoint(endpoint);
        }
    }
}

