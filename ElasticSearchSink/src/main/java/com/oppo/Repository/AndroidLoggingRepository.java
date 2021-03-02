package com.oppo.Repository;
//
//import com.oppo.Entity.AndroidLogging;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//
//public interface AndroidLoggingRepository extends ElasticsearchRepository<AndroidLogging, String> {}

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.Entity.AndroidLogging;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static com.oppo.Constant.ElasticSearchConstant.INDEX;
import static com.oppo.Constant.ElasticSearchConstant.IndexType;

@Service
public class AndroidLoggingRepository{
    private RestHighLevelClient client;


    private ObjectMapper objectMapper;

    @Autowired
    public AndroidLoggingRepository(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public String createProfileDocument(AndroidLogging document) throws Exception {

//        UUID uuid = UUID.randomUUID();
//        document.setId(uuid.toString());

        IndexRequest indexRequest = new IndexRequest(INDEX, IndexType, UUID.randomUUID().toString())
                .source(convertAndroidLoggingDocumentToMap(document), XContentType.JSON);

        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse.getResult().name();
    }

    private Map<String, Object> convertAndroidLoggingDocumentToMap(AndroidLogging androidLoggingDocument) {
        return objectMapper.convertValue(androidLoggingDocument, Map.class);
    }
}
