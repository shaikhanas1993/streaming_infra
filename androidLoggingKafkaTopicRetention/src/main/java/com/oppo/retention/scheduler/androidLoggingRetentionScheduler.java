package com.oppo.retention.scheduler;

import com.oppo.retention.helper.AppConfig;
import com.oppo.retention.helper.jsonConvertHelper;
import com.oppo.retention.pojo.MysqlOffSet;
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

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class androidLoggingRetentionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(androidLoggingRetentionScheduler.class);
    private static final Map<TopicPartition, Long> partitionToUncommittedOffsetMap = new HashMap<TopicPartition, Long>();
    private static final int MaxRententionTimeAllowed= 150;

    @Autowired
    private AppConfig appConfig;

    @Scheduled(fixedRate = 50000)
    public void retentionFunc(){

        logger.info("-------------------Started Androdi Logging Retention Scheduler-----------------------");
        try {

            //Consumer to get mysql offset
            KafkaConsumer<String, String> consumer = com.oppo.retention.kafka.KafkaConsumer.getConsumer(appConfig.mysqlOffSetTopic,appConfig.mysqlOffSetTopicConsumerGroup,appConfig.bootstrapServer);

            //Producer to set mysql offset
            KafkaProducer<String,String> mysqlOffsetProducer =  com.oppo.retention.kafka.KafkaProducer.getProducer(appConfig.bootstrapServer);

            ConsumerRecords<String, String> records = consumer.poll(1000);
            logger.info("records count of mysql offset topic :::" + records.count());

            if(records.count() > 0 )
            {
                int recordsCount = records.count();
                int i = 1;
                for ( ConsumerRecord<String,String> record:records) {
                    if(i == recordsCount){

                            //get object from jsonString
                       try{
                           MysqlOffSet mysqlOffSet = jsonConvertHelper.getMysqlObjectFromString(record.value());
                           try {
                               process(record,mysqlOffSet,consumer, mysqlOffsetProducer,appConfig);
                           }
                           catch (Exception e){
                               e.printStackTrace();
                               logger.error(e.getMessage());
                           }
                       }catch (Exception e){
                           e.printStackTrace();
                           logger.error(e.getMessage());
                       }

                    }
                    i = i + 1;
                }
            }

            logger.info("-----------End of the program -----------------");


        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }

    }

    private static void process(ConsumerRecord<String,String> record,MysqlOffSet mysqlOffSet,KafkaConsumer<String, String> consumer,KafkaProducer<String,String> mysqlOffsetProducer,AppConfig appConfig) throws Exception{
        Instant now = Instant.now(); //current date
      //  Instant before = now.minus(Duration.ofDays(7));
        long currentTimeStamp = now.getEpochSecond();
        // long timeStampOfLastMysqlOffset = before.getEpochSecond();
        long timeStampOfLastMysqlOffset =  mysqlOffSet.getEpocTime();
        //get seconds by subtraction
        long differenceInSeconds = currentTimeStamp - timeStampOfLastMysqlOffset;
        //calculate hours  from seconds
        long hoursElapsed  = TimeUnit.SECONDS.toHours(differenceInSeconds);

        //        Date dateBefore = Date.from(before);
        logger.info("---------------------------------------");
        logger.info("---------------------------------------");
        logger.info("---------------------------------------");
        logger.info( "current timestamp ::" + currentTimeStamp);
        logger.info( "before timestamp ::" + timeStampOfLastMysqlOffset);
        logger.info( "differenceInSeconds ::" + differenceInSeconds);
        logger.info( "hoursElapsed ::" + hoursElapsed);
        logger.info("---------------------------------------");
        logger.info("---------------------------------------");
        logger.info("---------------------------------------");
        //if reached max retention
        if(hoursElapsed > MaxRententionTimeAllowed)
        {
            logger.info("retention limit has exceeded....");
            logger.info("dummping the value back to the topic to extend the validity of the record in the topic");
            MysqlOffSet newEntity =  new MysqlOffSet();
            newEntity.setId(mysqlOffSet.getId());
            newEntity.setEpocTime(Instant.now().getEpochSecond());
            logger.info("stringify the objecct mysqloffset entity..........");
            String stringifiedEntity = jsonConvertHelper.getJsonStringObject(newEntity);
            logger.info("the stringified entity is ::" + stringifiedEntity);
            com.oppo.retention.kafka.KafkaProducer.sendMessage(mysqlOffsetProducer,stringifiedEntity,appConfig.mysqlOffSetTopic);
            logger.info("closing the producer....");
            com.oppo.retention.kafka.KafkaProducer.close(mysqlOffsetProducer);
            logger.info("committing the record within the topic");
            //Get record offset and partition for manually committing the offset
            TopicPartition tp = new TopicPartition(record.topic(), record.partition());
            partitionToUncommittedOffsetMap.put(tp, record.offset());
            commitOffset(consumer);
            //close the consumer
            logger.info("closing the consumer.........");
            consumer.close();
            logger.info("consumer closed successfully.........");
        }
        else
        {
           logger.info("Retention limit not reached....");
        }
    }

    private static void  commitOffset(KafkaConsumer<String, String> consumer)
    {
        Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = new HashMap<>();
        for (Map.Entry<TopicPartition, Long> e : partitionToUncommittedOffsetMap.entrySet()) {
            partitionToMetadataMap.put(e.getKey(), new OffsetAndMetadata(e.getValue() + 1));
        }
        consumer.commitSync(partitionToMetadataMap);
        partitionToUncommittedOffsetMap.clear();
    }
}
