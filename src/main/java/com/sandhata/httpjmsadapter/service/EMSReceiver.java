package com.sandhata.httpjmsadapter.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Getter
@Component
public class EMSReceiver {

    private static final Logger log = LoggerFactory.getLogger(EMSReceiver.class);

    private CompletableFuture<String> futureMessage = new CompletableFuture<>();

    public void receiveMessage(Message message) {
        try {
            if (message instanceof TextMessage) {

                TextMessage textMessage = (TextMessage) message;

                System.out.println(message + "Check for Message from EMS");
                System.out.println(textMessage + "Check for Text Message from EMS");
                System.out.println(textMessage.getText() + "Check for Text Message Body");

                String contentType = message.getStringProperty("ContentType");
                String response = "";

                if (contentType != null) {
                    if (contentType.equalsIgnoreCase("application/json")) {
                        response = readResponseFile("response/jsonResponse.json");
                    } else if (contentType.equalsIgnoreCase("text/xml") || contentType.equalsIgnoreCase("application/xml") || contentType.equalsIgnoreCase("application/soap+xml")) {
                        response = readResponseFile("response/soapResponse.xml");
                    }

                    log.info("Header: JMSDestination = "+ textMessage.getJMSDestination().toString());
                    log.info("Header: ContentType = " + message.getStringProperty("ContentType"));

//                    logHeaders(message);
                    futureMessage.complete(response);
                } else {
                    log.warn("ContentType property is missing in the message.");
                    futureMessage.complete("ContentType not provided");
                }

                // Reset the future for the next message
                futureMessage = new CompletableFuture<>();
            } else {
                log.warn("Received non-text message: {}", message);
            }
        } catch (JMSException e) {
            log.error("Error processing received message", e);
        }
    }

//    private void logHeaders(Message message) throws JMSException {
//        Enumeration<?> propertyNames = message.getPropertyNames();
//        while (propertyNames.hasMoreElements()) {
//            String propertyName = (String) propertyNames.nextElement();
//            String propertyValue = message.getStringProperty(propertyName);
//            log.info("Header: {} = {}", propertyName, propertyValue);
//        }
//    }

    private String readResponseFile(String filePath) {
        try {
            Path path = Paths.get(new ClassPathResource(filePath).getURI());
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            log.error("Error reading response file: {}", filePath, e);
            return "Error reading response file";
        }
    }
}

//    private String processJsonResponse(String messageBody) {
//        // Logic to format or handle JSON response as required
//        return messageBody;
//    }
//
//    private String processSoapResponse(String messageBody) {
//        // Logic to format or handle SOAP response as required
//        return messageBody;
//    }

