package com.oppo.Helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Value("${app.elasticsearch.topic}")
    public String elasticSinkTopic;
    @Value("${app.elasticsearch.consumergroup}")
    public String elasticSinkConsumerGroup;
    @Value("${app.kafka.bootstrapServers}")
    public String bootstrapServer;
    @Value("${app.kafka.bootstrapServers}")
    public String elasticSinkFailedTopic;

    //Used in addition of @PropertySource
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}