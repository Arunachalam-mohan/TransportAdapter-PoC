package com.sandhata.httpjmsadapter.utility;

import com.tibco.tibjms.TibjmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@Configuration
@EnableJms
public class JmsConfig {

//    @Value("${tibco.ems.host}")
//    private String host;
//
//    @Value("${tibco.ems.port}")
//    private String port;

    @Value("${tibco.ems.username}")
    private String username;
    @Value("${tibco.ems.password}")
    private String password;


    @Bean
    public TibjmsConnectionFactory connectionFactory() throws JMSException {
        TibjmsConnectionFactory factory = new TibjmsConnectionFactory();
        factory.setServerUrl("tcp://localhost:7222");
        factory.setUserName(username);
        factory.setUserPassword(password);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate() throws JMSException {
        JmsTemplate template = new JmsTemplate(connectionFactory());
        template.setPubSubDomain(true);
        return template;
    }

//    @Bean
//    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() throws JMSException {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory());
//        factory.setPubSubDomain(true); // Set this to `false` if you are using queues instead of topics
//        return factory;
//    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory() throws JMSException {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setPubSubDomain(true);
//        factory.setConcurrency("10-50");
        return factory;
    }
}
