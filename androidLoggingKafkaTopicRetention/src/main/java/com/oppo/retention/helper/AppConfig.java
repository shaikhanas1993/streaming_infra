package com.oppo.retention.helper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Value("${app.mysqloffset.topic}")
    public String mysqlOffSetTopic;
    @Value("${app.mysqloffset.consumergroup}")
    public String mysqlOffSetTopicConsumerGroup;
    @Value("${app.kafka.bootstrapServers}")
    public String bootstrapServer;
    //Used in addition of @PropertySource
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}