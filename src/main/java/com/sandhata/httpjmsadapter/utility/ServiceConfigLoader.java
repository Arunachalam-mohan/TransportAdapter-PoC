package com.sandhata.httpjmsadapter.utility;

import com.sandhata.httpjmsadapter.entity.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ServiceConfigLoader {
    private final Map<String, ServiceConfig> mappings = new HashMap<>();

    @PostConstruct
    public void loadMappings() {
        log.info("Loading service configurations from CSV file");

        try (InputStream is = new ClassPathResource("service-config.csv").getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            // Get header map to identify column names dynamically
//            Map<String, Integer> headerMap = csvParser.getHeaderMap();

            // Check for required headers
//            if (!headerMap.containsKey("serviceName") || !headerMap.containsKey("soapAction") || !headerMap.containsKey("topicName")) {
//                throw new IllegalArgumentException("CSV file must contain headers: ServiceName, SoapAction, TopicName");
//            }

            for (CSVRecord csvRecord : csvParser) {
                ServiceConfig config = new ServiceConfig();
                config.setServiceName(csvRecord.values()[0]);
                config.setSoapAction(csvRecord.values()[1]);
                config.setTopicName(csvRecord.values()[2]);
//                config.setServiceName(csvRecord.get("serviceName"));
//                config.setSoapAction(csvRecord.get("soapAction"));
//                config.setTopicName(csvRecord.get("topicName"));

                mappings.put(config.getServiceName(), config);
            }

            log.info("Service configurations loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load service mappings", e);
            throw new RuntimeException("Failed to load service mappings", e);
        }
    }

    public ServiceConfig getMapping(String serviceName) {
        log.debug("Retrieving configuration for service: {}", serviceName);
        return mappings.get(serviceName);
    }

    public List<String> getAllTopicNames() {
        return mappings.values().stream()
                .map(ServiceConfig::getTopicName)
                .distinct()
                .collect(Collectors.toList());
    }
}
