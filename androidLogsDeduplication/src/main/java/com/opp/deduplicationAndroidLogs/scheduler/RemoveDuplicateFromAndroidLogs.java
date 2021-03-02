package com.opp.deduplicationAndroidLogs.scheduler;

import com.opp.deduplicationAndroidLogs.service.DuplicationDocumentFinderService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class RemoveDuplicateFromAndroidLogs {
    @Autowired
    private DuplicationDocumentFinderService duplicationDocumentFinderService;
    private static final Logger logger = LoggerFactory.getLogger(RemoveDuplicateFromAndroidLogs.class);
    private ExecutorService executor;
    private static final  int numberOfThreads = 100;

    @Scheduled(fixedRate = 50000)
    public void findDuplicates(){
        //creating a threadpool for later use
        executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());

        //https://stackoverflow.com/questions/49366324/how-to-find-all-duplicate-documents-in-elasticsearch
        //https://stackoverflow.com/questions/42985767/elasticsearch-aggregation-with-java

        try
        {
//            SearchResponse searchResponse = duplicationDocumentFinderService.findMaximumMatchCount();
//            Cardinality maximum_match_counts = searchResponse.getAggregations().get("maximum_match_counts");
//            long size = maximum_match_counts.getValue() > 0 ? maximum_match_counts.getValue() : 100000L;
            SearchResponse duplicateSearchResponse = duplicationDocumentFinderService.findDuplicateDocuments();
            logger.info("Query results:");
            Terms contractSums = duplicateSearchResponse.getAggregations().get("duplicates");

            for (Terms.Bucket bucket : contractSums.getBuckets()) {
                executor.submit(new ElasticDuplicateHandler(bucket,duplicationDocumentFinderService));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }
}

class ElasticDuplicateHandler implements Runnable{
    private Terms.Bucket bucket;
    private DuplicationDocumentFinderService duplicationDocumentFinderService;
private static final Logger logger = LoggerFactory.getLogger(ElasticDuplicateHandler.class);
    public ElasticDuplicateHandler(Terms.Bucket bucket,DuplicationDocumentFinderService duplicationDocumentFinderService)
    {
        this.bucket = bucket;
        this.duplicationDocumentFinderService = duplicationDocumentFinderService;
    }

    @Override
    public void run()
    {
        //find all documents by mysqlId;
        try
        {
            SearchResponse searchResponse = duplicationDocumentFinderService.findDocumentIdByMysqlId((long)bucket.getKey(),(int)bucket.getDocCount());
            SearchHit[] searchHit = searchResponse.getHits().getHits();
            int  length = searchHit.length;
            int i = 1;
            List<String> idsTodelete = new ArrayList<>();
            for (SearchHit hit : searchHit){
                //deleting all the documents except the last one

                if(i < length)
                {
                    idsTodelete.add(hit.getId());
                }
                i = i + 1;
            }

            if(idsTodelete.size() > 0)
            {
                logger.info("Deleting in batched the first hit");
                duplicationDocumentFinderService.bulkDeleteAndroidDocuments(idsTodelete);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }
}

