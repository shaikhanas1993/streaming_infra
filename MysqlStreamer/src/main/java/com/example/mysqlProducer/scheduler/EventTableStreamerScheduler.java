package com.example.mysqlProducer.scheduler;
import com.example.mysqlProducer.HelperClasses.*;
import com.example.mysqlProducer.Kafka.MysqlOffsetConsumer;
import com.example.mysqlProducer.Kafka.MysqlOffsetProducer;
import com.example.mysqlProducer.Model.AndroidLogging;
import com.example.mysqlProducer.Repository.AndroidLoggingInterface;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventTableStreamerScheduler {

    private final Map<TopicPartition, Long> partitionToUncommittedOffsetMap = new HashMap<TopicPartition, Long>();
    private Long lastId = 0L;

    private final Logger logger = LoggerFactory.getLogger(MysqlOffsetConsumer.class);
    @Autowired
    private AndroidLoggingInterface androidLoggingInterface;

    @Autowired
    private AppConfig appConfig;

    @Scheduled(fixedRate = 50000)
    public void reportCurrentTime() {
       logger.info("Scheduler Started ::");
        try
        {
            //Consumer to get mysql offset
            KafkaConsumer<String, String> consumer = MysqlOffsetConsumer.getConsumer(appConfig.mysqlOffSetTopic,appConfig.mysqlOffSetTopicConsumerGroup,appConfig.bootstrapServer);

            //Producer to set mysql offset
            KafkaProducer<String,String> mysqlOffsetProducer =  MysqlOffsetProducer.getProducer(appConfig.bootstrapServer);

            //Producer to dumb data in elastic sink topic
            KafkaProducer<String,String> elasticSinkProducer =  MysqlOffsetProducer.getProducer(appConfig.bootstrapServer);

            ConsumerRecords<String, String> records = consumer.poll(1000);
            logger.info("records count of mysql offset topic :::" + records.count());

            //ideally records count should be just 1 . if greater than 1 means there was failure at some point
            if(records.count() > 1)
            {
                int size = records.count();
                int i = 1;
                for (ConsumerRecord record: records)
                {
                    i =  i + 1;
                    if(i == size)
                    {
                        produceMysqlRecords(record,consumer,mysqlOffsetProducer,elasticSinkProducer);
                    }
                    else
                    {
                        dumpFailureRecordsInElasticSearch(record,consumer,mysqlOffsetProducer,elasticSinkProducer);
                    }
                }

            }
            else
            {
                //code is working like charm
                if(!records.isEmpty())
                {
                    records.forEach(record -> {
                        produceMysqlRecords(record,consumer,mysqlOffsetProducer,elasticSinkProducer);
                    });
                }
                else
                {
                    produceMysqlRecordsForTheFirstTime(0L, consumer, mysqlOffsetProducer,elasticSinkProducer);
                }
            }

            //Closing all the producers and consumers
            logger.info("closing the elastic search sink producer");
            MysqlOffsetProducer.close(elasticSinkProducer);
            logger.info("elastic search producer closed successfully");

            logger.info("closing the mysql offset producer");
            MysqlOffsetProducer.close(mysqlOffsetProducer);
            logger.info("mysql offset producer closed successfully");

            logger.info("closing the consumer");
            MysqlOffsetConsumer.close(consumer);
            logger.info("consumer closed successfully");

        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
        }
    }

    private void produceMysqlRecords(ConsumerRecord<String, String> record,KafkaConsumer<String, String> consumer,KafkaProducer<String,String> mysqlOffsetProducer,KafkaProducer<String,String> elasticSinkProducer)
    {
        try
        {
            logger.info("in produceMysqlRecords function");
            MysqlOffSet currentObj = GsonHelper.getMysqlOffsetObjectFromString(record.value());
            this.lastId = currentObj.getId();

            logger.info("inserting  top 1000 rows in elastic-sink-topic");
            List<AndroidLogging> androidLoggingList = androidLoggingInterface.getListOfLogs(lastId);
            logger.info("androidLoggingList count of records ::" + androidLoggingList.size());

            if(androidLoggingList.size() > 0)
            {
                logger.info("Forming Elastic Search object to put in the topic sink");
                for (AndroidLogging element : androidLoggingList)
                {
                    String Stringified_ElasticDocumentEntity = GsonHelper.getJsonStringObject(element);
                    logger.info("Stringified Elastic Document :::" + Stringified_ElasticDocumentEntity);

                    logger.info("dumping data to elastic sink topic from elastic sink producer");
                    MysqlOffsetProducer.sendMessage(elasticSinkProducer,Stringified_ElasticDocumentEntity,appConfig.elasticSearchSinkTopic);
                }
                logger.info("finished producing data into elastic_sink_topic  for this process");
                //put last record id into the mysql offset
                //Put the lastId record within the topic so that i can pick from later
                AndroidLogging lastElement = androidLoggingList.get(androidLoggingList.size() - 1);
                MysqlOffSet mysqlOffSetObject = new MysqlOffSet();
                mysqlOffSetObject.setId(lastElement.id);
                Instant now = Instant.now(); //current date
                //  Instant before = now.minus(Duration.ofDays(7));
                long currentTimeStamp = now.getEpochSecond();
                mysqlOffSetObject.setEpocTime(currentTimeStamp);
                //Serializing mysql offset object to string to store in the topics
                String Stringified_mysqlOffSetObject = GsonHelper.getJsonStringObject(mysqlOffSetObject);
                logger.info("Sending message to the mysql_offset_topic ::" + Stringified_mysqlOffSetObject);
                MysqlOffsetProducer.sendMessage(mysqlOffsetProducer,Stringified_mysqlOffSetObject,appConfig.mysqlOffSetTopic);
                logger.info("Message send to the  topic");

                //Get record offset and partition for manually committing the offset
                TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                partitionToUncommittedOffsetMap.put(tp, record.offset());
                commitOffset(consumer);
            }
        }
        catch (Exception e)
        {
            logger.error("Occured message while converting to mysql offset object");
            logger.error(e.getMessage());
        }

    }

    private void produceMysqlRecordsForTheFirstTime(Long lastId,KafkaConsumer<String, String> consumer,KafkaProducer<String,String> mysqlOffsetProducer,KafkaProducer<String,String> elasticSinkProducer)
    {
        try
        {
            logger.info("in produceMysqlRecordsForTheFirstTime function");
            logger.info("inserting  top 1000 rows in elastic-sink-topic");
            List<AndroidLogging> androidLoggingList = androidLoggingInterface.getListOfLogs(lastId);
            logger.info("androidLoggingList count of records ::" + androidLoggingList.size());

            if(androidLoggingList.size() > 0)
            {
                logger.info("Forming Elastic Search object to put in the topic sink");
                for (AndroidLogging element : androidLoggingList)
                {
                    String Stringified_ElasticDocumentEntity = GsonHelper.getJsonStringObject(element);
                    logger.info("Stringified Elastic Document :::" + Stringified_ElasticDocumentEntity);

                    logger.info("dumping data to elastic sink topic from elastic sink producer");
                    MysqlOffsetProducer.sendMessage(elasticSinkProducer,Stringified_ElasticDocumentEntity,appConfig.elasticSearchSinkTopic);
                }
                logger.info("finished producing data into elastic_sink_topic  for this process");
                //put last record id into the mysql offset
                //Put the lastId record within the topic so that i can pick from later
                AndroidLogging lastElement = androidLoggingList.get(androidLoggingList.size() - 1);
                MysqlOffSet mysqlOffSetObject = new MysqlOffSet();
                mysqlOffSetObject.setId(lastElement.id);
                Instant now = Instant.now(); //current date
                //  Instant before = now.minus(Duration.ofDays(7));
                long currentTimeStamp = now.getEpochSecond();
                mysqlOffSetObject.setEpocTime(currentTimeStamp);
                //Serializing mysql offset object to string to store in the topics
                String Stringified_mysqlOffSetObject = GsonHelper.getJsonStringObject(mysqlOffSetObject);
                logger.info("Sending message to the mysql_offset_topic ::" + Stringified_mysqlOffSetObject);
                MysqlOffsetProducer.sendMessage(mysqlOffsetProducer,Stringified_mysqlOffSetObject,appConfig.mysqlOffSetTopic);
                logger.info("Message send to the  topic");
                consumer.commitSync();
            }


        }
        catch (Exception e)
        {
            logger.error("Exception occured in produceMysqlRecordsForTheFirstTime function");
            logger.error(e.getMessage());
        }
    }

    //this function ensures that those records which were not processed due  to some failure gets dumped into elastic sink topic
    private void dumpFailureRecordsInElasticSearch(ConsumerRecord<String, String> record,KafkaConsumer<String, String> consumer,KafkaProducer<String,String> mysqlOffsetProducer,KafkaProducer<String,String> elasticSinkProducer)
    {
        try
        {
            logger.info("in dumpFailureRecordsInElasticSearch function");
            MysqlOffSet currentObj = GsonHelper.getMysqlOffsetObjectFromString(record.value());
            this.lastId = currentObj.getId();

            logger.info("inserting  top 1000 rows in elastic-sink-topic");
            List<AndroidLogging> androidLoggingList = androidLoggingInterface.getListOfLogs(lastId);
            logger.info("androidLoggingList count of records ::" + androidLoggingList.size());

            if(androidLoggingList.size() > 0)
            {
                logger.info("Forming Elastic Search object to put in the topic sink");
                for (AndroidLogging element : androidLoggingList)
                {
                    String Stringified_ElasticDocumentEntity = GsonHelper.getJsonStringObject(element);
                    logger.info("Stringified Elastic Document :::" + Stringified_ElasticDocumentEntity);

                    logger.info("dumping data to elastic sink topic from elastic sink producer");
                    MysqlOffsetProducer.sendMessage(elasticSinkProducer,Stringified_ElasticDocumentEntity,appConfig.elasticSearchSinkTopic);
                }
                logger.info("finished producing data into elastic_sink_topic  for this process");
                //Get record offset and partition for manually committing the offset
                TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                partitionToUncommittedOffsetMap.put(tp, record.offset());
                commitOffset(consumer);
            }
        }
        catch(Exception e)
        {
            logger.error("An error occured in dumpFailureRecordsInElasticSearch method");
            logger.error(e.getMessage());
        }
    }

    private void commitOffset(KafkaConsumer<String, String> consumer)
    {
        Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = new HashMap<>();
        for (Map.Entry<TopicPartition, Long> e : partitionToUncommittedOffsetMap.entrySet()) {
            partitionToMetadataMap.put(e.getKey(), new OffsetAndMetadata(e.getValue() + 1));
        }
        consumer.commitSync(partitionToMetadataMap);
        partitionToUncommittedOffsetMap.clear();
    }
}
