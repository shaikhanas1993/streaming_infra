package com.opp.deduplicationAndroidLogs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opp.deduplicationAndroidLogs.document.AndroidLogging;
import com.opp.deduplicationAndroidLogs.scheduler.RemoveDuplicateFromAndroidLogs;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.opp.deduplicationAndroidLogs.constant.Constant.INDEX;
import static com.opp.deduplicationAndroidLogs.constant.Constant.IndexType;

@Service
public class DuplicationDocumentFinderService {

    private RestHighLevelClient client;
    private ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(DuplicationDocumentFinderService.class);

    @Autowired
    public DuplicationDocumentFinderService(RestHighLevelClient client, ObjectMapper objectMapper)
    {
        this.client = client;
        this.objectMapper = objectMapper;
    }


    public SearchResponse findMaximumMatchCount() throws Exception
    {
        //build search request
        SearchRequest searchRequest = buildSearchRequest(INDEX,IndexType);
        //create instance of search source builder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //write query with the required operator
        ///searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //set size of the result
        searchSourceBuilder.size(0);
        CardinalityAggregationBuilder aggregation = AggregationBuilders
                .cardinality("maximum_match_counts")
                .precisionThreshold(100)
                .field("mysqlId");

        searchSourceBuilder.aggregation(aggregation);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;
   }

   public SearchResponse findDuplicateDocuments() throws Exception
   {
       //build search request
       SearchRequest searchRequest = buildSearchRequest(INDEX,IndexType);
       //create instance of search source builder
       SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
       //set size of the result
//       searchSourceBuilder.size(maximumMatchCount);
       searchSourceBuilder.aggregation(AggregationBuilders.terms("duplicates").field("mysqlId").size(1000000).minDocCount(2));
       searchRequest.source(searchSourceBuilder);
       SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
       return searchResponse;
   }

   public SearchResponse findDocumentIdByMysqlId(long mysqlId,int size) throws IOException {
       //build search request
       SearchRequest searchRequest = buildSearchRequest(INDEX,IndexType);
       //create instance of search source builder
       SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
       //set size of the result
       searchSourceBuilder.size(size);
       searchSourceBuilder.query(QueryBuilders.termQuery("mysqlId",mysqlId));
       searchRequest.source(searchSourceBuilder);
       SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
       return searchResponse;
   }

    public List<AndroidLogging> findAll() throws Exception {


        SearchRequest searchRequest = buildSearchRequest(INDEX,IndexType);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse =
                client.search(searchRequest, RequestOptions.DEFAULT);

        return getSearchResult(searchResponse);
    }

    private List<AndroidLogging> getSearchResult(SearchResponse response) {

        SearchHit[] searchHit = response.getHits().getHits();

        List<AndroidLogging> profileDocuments = new ArrayList<>();

        for (SearchHit hit : searchHit){
            profileDocuments
                    .add(objectMapper
                            .convertValue(hit
                                    .getSourceAsMap(), AndroidLogging.class));
        }

        return profileDocuments;
    }

    private SearchRequest buildSearchRequest(String index, String type) {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        searchRequest.types(type);

        return searchRequest;
    }

    private Map<String, Object> convertProfileDocumentToMap(AndroidLogging androidLoggingDocument) {
        return objectMapper.convertValue(androidLoggingDocument, Map.class);
    }

    private AndroidLogging convertMapToProfileDocument(Map<String, Object> map){
        return objectMapper.convertValue(map,AndroidLogging.class);
    }

    public String deleteAndroidLoggingDocument(String id) throws Exception {

        DeleteRequest deleteRequest = new DeleteRequest(INDEX, IndexType, id);
        DeleteResponse response = client.delete(deleteRequest,RequestOptions.DEFAULT);

        return response
                .getResult()
                .name();
    }

    public void bulkDeleteAndroidDocuments(List<String> idsToDelete) throws Exception {

        DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX);
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
//        for(String _id: idsToDelete){
//            qb.must(QueryBuilders.termsQuery("_id",_id));
//        }
        qb.must(QueryBuilders.termsQuery("_id",idsToDelete));

        logger.info(qb.toString(true));

        request.setQuery(qb);
        request.setConflicts("proceed");
        client.deleteByQueryAsync(request,RequestOptions.DEFAULT,new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkResponse) {
                logger.info("total deleted docs ::" + bulkResponse.getStatus().getDeleted());
            }

            @Override
            public void onFailure(Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        });
    }

}
