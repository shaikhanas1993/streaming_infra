package com.oppo.retention.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducer {
    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    public static org.apache.kafka.clients.producer.KafkaProducer<String,String> getProducer(String bootstrapServer) throws Exception
    {
        org.apache.kafka.clients.producer.KafkaProducer<String, String> producer = null;
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServer);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
        return producer;
    }

    public static void sendMessage(org.apache.kafka.clients.producer.KafkaProducer<String,String> producer, String message, String topic) throws Exception
    {
        ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topic,message);
        producer.send(producerRecord);
    }

    public static void close(org.apache.kafka.clients.producer.KafkaProducer<String,String> producer)
    {
        producer.close();
    }
}
