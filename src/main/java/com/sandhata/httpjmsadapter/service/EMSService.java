package com.sandhata.httpjmsadapter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Enumeration;
import java.util.Map;

@Service
@Slf4j
public class EMSService {

    @Autowired
    private JmsTemplate jmsTemplate;

//    @Autowired
//    public EMSService(JmsTemplate jmsTemplate) {
//        this.jmsTemplate = jmsTemplate;
//    }


    public void sendMessage(String destination, String messageBody, Map<String, String> jmsHeaders) {
        log.debug("Preparing to send message to destination: {}", destination);
        jmsTemplate.send(destination, session -> {
            TextMessage message = session.createTextMessage(messageBody);
            log.info("Message body: {}", messageBody);

            // Set JMS headers
            jmsHeaders.forEach((key, value) -> {
                try {
                    message.setStringProperty(key, value);
                    log.debug("Set JMS header: {} = {}", key, value);
                } catch (JMSException e) {
                    // Handle the exception, e.g., log it
                    System.err.println("Error setting JMS property: " + key + " = " + value);
                    e.printStackTrace();
                }
            });
            return message;
        });
        log.info("Message successfully sent to destination: {}", destination);
    }

//    public String receiveMessage(String destination) throws JMSException {
//        String message = null;
//
//        Message msg = jmsTemplate.receive(destination);
//        if(msg instanceof TextMessage) {
//            message = ((TextMessage) msg).getText();
//        }
//
//        return message;
//    }


//    public void receiveMessage(Message message) {
//        try {
//            if (message instanceof TextMessage) {
//                TextMessage textMessage = (TextMessage) message;
//                String messageBody = textMessage.getText();
//                log.info("Received message from EMS: {}", messageBody);
//
//                // Log headers if needed
//                Enumeration<?> propertyNames = message.getPropertyNames();
//                while (propertyNames.hasMoreElements()) {
//                    String propertyName = (String) propertyNames.nextElement();
//                    String propertyValue = message.getStringProperty(propertyName);
//                    log.info("Header: {} = {}", propertyName, propertyValue);
//                }
//            } else {
//                log.warn("Received non-text message: {}", message);
//            }
//        } catch (JMSException e) {
//            log.error("Error processing received message", e);
//        }
//    }
}