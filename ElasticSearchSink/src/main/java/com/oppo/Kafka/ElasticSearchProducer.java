package com.oppo.Kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class ElasticSearchProducer {
    public static KafkaProducer<String,String> getProducer(String bootstrapServer) throws Exception
    {
        KafkaProducer<String, String> producer = null;
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServer);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
        return producer;
    }

    public static void sendMessage(KafkaProducer<String,String> producer,String message,String topic) throws Exception
    {
        ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topic,message);
        producer.send(producerRecord);
    }

    public static void close(KafkaProducer<String,String> producer)
    {
        producer.close();
    }
}
