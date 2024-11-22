package com.sandhata.httpjmsadapter.controller;

import com.sandhata.httpjmsadapter.entity.ServiceConfig;
import com.sandhata.httpjmsadapter.service.EMSReceiver;
import com.sandhata.httpjmsadapter.service.EMSService;
import com.sandhata.httpjmsadapter.utility.ServiceConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class EMSController {

    @Autowired
    private EMSService emsService;
    @Autowired
    private EMSReceiver emsReceiver;
    @Autowired
    private ServiceConfigLoader serviceConfigLoader;

    @PostMapping(
            value = "/soapService/{serviceUri}/{v2}/{get}",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> handleSoapRequest(@PathVariable String serviceUri,
                                                    @PathVariable String v2,
                                                    @PathVariable String get,
                                                    @RequestBody String body,
                                                    @RequestHeader(value = "Source", required = false) String source,
                                                    @RequestHeader(value = "JMS_TIBCO_Sender", required = false) String jmsTibcoSender) {
        String serviceName = serviceUri + "/" + v2 + "/" + get;
        log.info("Received SOAP request for service: {}", serviceName);
        return processRequest(serviceName, body, MediaType.APPLICATION_XML_VALUE, source, jmsTibcoSender);
    }

    @PostMapping(
            value = "/jsonService/{serviceUri}/{v2}/{get}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> handleJsonRequest(@PathVariable String serviceUri,
                                                    @PathVariable String v2,
                                                    @PathVariable String get,
                                                    @RequestBody String body,
                                                    @RequestHeader(value = "Source", required = false) String source,
                                                    @RequestHeader(value = "JMS_TIBCO_Sender", required = false) String jmsTibcoSender) {
        String serviceName = serviceUri + "/" + v2 + "/" + get;
        log.info("Received JSON request for service: {}", serviceName);
        return processRequest(serviceName, body, MediaType.APPLICATION_JSON_VALUE, source, jmsTibcoSender);
    }

    private ResponseEntity<String> processRequest(String serviceName, String body, String contentType, String source, String jmsSender) {
        log.debug("Processing request for service: {}, Content-Type: {}", serviceName, contentType);

        ServiceConfig config = serviceConfigLoader.getMapping(serviceName);
        if (config == null) {
            log.error("Configuration not found for service: {}", serviceName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service configuration not found");
        }

        // Prepare JMS headers
        Map<String, String> jmsHeaders = new HashMap<>();
        jmsHeaders.put("SoapAction", config.getSoapAction());
        jmsHeaders.put("JMSDestination", config.getTopicName());
        jmsHeaders.put("ContentType", contentType);

        if (source != null) {
            jmsHeaders.put("Source", source);
        }
        if (jmsSender != null) {
            jmsHeaders.put("JMS_TIBCO_Sender", jmsSender);
        }

        log.info("Sending message to EMS with JMS headers: {}", jmsHeaders);

        emsService.sendMessage(config.getTopicName(), body, jmsHeaders);

        try {
            String responseMessage = emsReceiver.getFutureMessage().get(20, TimeUnit.SECONDS);
            log.info("Received response: {}", responseMessage);

            // Ensure the response has the correct content type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(contentType));
            return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error waiting for EMS response", e);
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("No response received from EMS");
        }
    }
}


