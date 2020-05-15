package com.xuecheng.search;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

/**
 * @author siyang
 * @create 2020-03-22 18:04
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test1 {
    @Autowired
    RestHighLevelClient client;
    @Autowired
    RestClient restClient;
    @Test
    public void testCreateIndex() throws IOException {
//        //创建索引请求对象，并设置索引名称
//        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course");
//        //设置索引参数
//        createIndexRequest.settings(Settings.builder().put("number_of_shards",1) .put("number_of_replicas",0));
//        //设置映射
//        createIndexRequest.mapping("doc"," {\n" + " \t\"properties\": {\n" + " \"name\": {\n" + " \"type\": \"text\",\n" + " \"analyzer\":\"ik_max_word\",\n" + " \"search_analyzer\":\"ik_smart\"\n" + " },\n" + " \"description\": {\n" + " \"type\": \"text\",\n" + " \"analyzer\":\"ik_max_word\",\n" + " \"search_analyzer\":\"ik_smart\"\n" + " },\n" + " \"studymodel\": {\n" + " \"type\": \"keyword\"\n" + " },\n" + " \"price\": {\n" + " \"type\": \"float\"\n" + " }\n" + " }\n" + "}", XContentType.JSON);
//        //创建索引操作客户端
//        IndicesClient indices = client.indices();
//        //创建响应对象
//        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest);
//        //得到响应结果
//        boolean acknowledged = createIndexResponse.isAcknowledged();
//        System.out.println(acknowledged);
    }
    //搜索type下的全部记录
    @Test
    public void testSearchAll() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }
}

