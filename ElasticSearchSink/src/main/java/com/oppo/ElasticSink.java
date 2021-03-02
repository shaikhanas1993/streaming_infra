package com.oppo;

import com.oppo.Entity.AndroidLogging;
import com.oppo.Entity.Tag;
import com.oppo.Helper.AndroidLoggingRdbmsEntity;
import com.oppo.Helper.AppConfig;
import com.oppo.Helper.GsonHelper;
import com.oppo.Kafka.ElasticSearchConsumer;
import com.oppo.Kafka.ElasticSearchProducer;
import com.oppo.Repository.AndroidLoggingRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ElasticSink implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSink.class);
    private ExecutorService executor;
    private KafkaConsumer<String,String> elasticSearchConsumer = null;
    private int numberOfThreads = 100;

    @Autowired
    private AndroidLoggingRepository androidLoggingRepository;
    @Autowired
    private AppConfig appConfig;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting kafka consumer Application");
        //Create a threadpool
        executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
       elasticSearchConsumer = ElasticSearchConsumer.getConsumer(appConfig.elasticSinkTopic,appConfig.elasticSinkConsumerGroup,appConfig.bootstrapServer);

        try
        {
            while (true)
            {
                ConsumerRecords<String, String> records = elasticSearchConsumer.poll(1000);
                logger.info("--------------------------------------------------------------");
                logger.info("the records of the data holding the topic ::" + records.count());
                logger.info("--------------------------------------------------------------");

                records.forEach(item ->{
                    executor.submit(new KafkaRecordHandler(item,androidLoggingRepository,appConfig));
                });
                logger.info("---------- Commit Async Called -----------------");
                elasticSearchConsumer.commitAsync();
                logger.info("---------------------------");
            }

        }
        catch (Exception e)
        {
            logger.error("Exception occured");
            logger.error(e.getMessage());
        }
        finally {
            logger.info("finally called:::::::::::::::::::");
            ElasticSearchConsumer.close(elasticSearchConsumer);
        }
    }


    @PreDestroy
    public void onExit() {
        logger.info("###STOP FROM THE LIFECYCLE###");
    }

}


class KafkaRecordHandler implements Runnable {
    private final Map<TopicPartition, Long> partitionToUncommittedOffsetMap = new HashMap<TopicPartition, Long>();
    private static final Logger logger = LoggerFactory.getLogger(ElasticSink.class);

    private ConsumerRecord<String, String> record;
    private AndroidLoggingRepository androidLoggingRepository;
    private  AppConfig appConfig;
    public KafkaRecordHandler(ConsumerRecord<String, String> record,AndroidLoggingRepository androidLoggingRepository,AppConfig appConfig){
        this.record = record;
        this.androidLoggingRepository = androidLoggingRepository;
    }

    @Override
    public void run() {
        try
        {
            logger.info("getting value from the topic :::" + record.value());
            AndroidLoggingRdbmsEntity entity = GsonHelper.getAndoridLoggingRdbmsEntityFromString(record.value());
            logger.info("android entity ::" + entity.toString());


            logger.info("forming entity that has to be dumped in to elastic search");
            AndroidLogging androidLogging = new AndroidLogging();
//            UUID uuid = UUID.randomUUID();
////            androidLogging.setId(uuid.toString());
            androidLogging.setMysqlId(entity.getId());
            androidLogging.setEventId(entity.getEventId());
            androidLogging.setValue_of_event(entity.getValue());
            androidLogging.setUserId(entity.getUserId());
            androidLogging.setLocalIpAddress(entity.getLocalIpAddress());
            androidLogging.setEndPoint(entity.getEndPoint());
            androidLogging.setDateCreated(entity.getDateCreated());
            androidLogging.setLastUpdated(entity.getLastUpdated());

            try
            {
                logger.info("Trying to convert tag string to tag entity");
                String tagString = entity.getTag();
                Tag tagEntity = GsonHelper.getTagObjectFromString(tagString);
                androidLogging.setSerial(tagEntity.getSERIAL());
                androidLogging.setAndroidId(tagEntity.getANDROIDID());
                androidLogging.setModel(tagEntity.getMODEL());
                androidLogging.setRelease(tagEntity.getRELEASE());
                androidLogging.setTagId(tagEntity.getID());
                logger.info("tag string converted to tag entity successfully");
            }
            catch (Exception e)
            {
                logger.error("error while converting tag string to entity");
                logger.error(e.getMessage());
            }


            logger.info("Android logging entity to be dumped in elastic search ::" + androidLogging.toString());

            try {
                logger.info("inserting into elastic search ...");
                String result =  androidLoggingRepository.createProfileDocument(androidLogging);
                logger.info("inserted into successfully ::" + result);
                //Get record offset and partition for manually committing the offset
//                logger.info("committing the message by increasing the offset by 1");
//                TopicPartition tp = new TopicPartition(record.topic(), record.partition());
//                partitionToUncommittedOffsetMap.put(tp, record.offset());
//                commitOffset(elasticSearchConsumer);
//                logger.info("offset committed successfully.....");
            }
            catch (Exception e)
            {
                logger.info("failed to insert into elastic search");
                logger.error(e.getMessage());
                try
                {
                    logger.info("-----------------Send failed messages to the topic for later processing------------------------------");
                    KafkaProducer<String,String> producer = ElasticSearchProducer.getProducer(appConfig.bootstrapServer);
                    ElasticSearchProducer.sendMessage(producer,record.value(),appConfig.elasticSinkFailedTopic);
                    ElasticSearchProducer.close(producer);
                    logger.info("----------------Failed Message Send-------------------------------");
                }catch (Exception ex)
                {
                    logger.error("Exception occured while sending failed messages");
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error while converting string value to android logging rdbms entityt");
            logger.error(e.getMessage());
        }
    }

//    private void commitOffset(KafkaConsumer<String, String> consumer)
//    {
//        Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = new HashMap<>();
//        for (Map.Entry<TopicPartition, Long> e : partitionToUncommittedOffsetMap.entrySet()) {
//            partitionToMetadataMap.put(e.getKey(), new OffsetAndMetadata(e.getValue() + 1));
//        }
//        consumer.commitSync(partitionToMetadataMap);
//        partitionToUncommittedOffsetMap.clear();
//    }
}
