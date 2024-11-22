package com.sandhata.httpjmsadapter.entity;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class ServiceConfig {

    private String serviceName;
    private String soapAction;
    private String topicName;

}
