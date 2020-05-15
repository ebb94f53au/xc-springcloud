package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author siyang
 * @create 2020-03-25 18:17
 */
@Service
public class EsCourseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsCourseService.class);
    @Value("${xuecheng.elasticsearch.course.index}")
    private String es_index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String es_type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;

    @Value("${xuecheng.elasticsearch.media.index}")
    private String es_media_index;
    @Value("${xuecheng.elasticsearch.media.type}")
    private String es_media_type;
    @Value("${xuecheng.elasticsearch.media.source_field}")
    private String source_media_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    public QueryResponseResult list(int page, int size, CourseSearchParam courseSearchParam){
        SearchRequest searchRequest = new SearchRequest(es_index);
        searchRequest.types(es_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //源字段过滤
        String[] source = source_field.split(",");
        searchSourceBuilder.fetchSource(source,new String[]{});


        //设置boolean查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //关键字
        if(!StringUtils.isEmpty(courseSearchParam.getKeyword())){
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "teachplan", "description");
            //设置占比
            multiMatchQueryBuilder.minimumShouldMatch("70%");
            //提升字段占比
            multiMatchQueryBuilder.field("name",10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        //过滤分类，等级
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //boolean查询
        searchSourceBuilder.query(boolQueryBuilder);

        //分页
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 20;
        }
        int start = (page-1)*size;
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        //请求搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse=null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //结果集处理
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        //记录总数
        long totalHits = hits.getTotalHits();

        //数据列表
        List<CoursePub> list = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            CoursePub coursePub = new CoursePub();
            //取出source
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //取出名称
            String name = (String) sourceAsMap.get("name");
            coursePub.setName(name);
            //图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //价格
            Float price = null;
            try {
                if(sourceAsMap.get("price")!=null ){
                    price = Float.parseFloat((String) sourceAsMap.get("price"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice(price);

            Float price_old = null;
            try {
                if(sourceAsMap.get("price_old")!=null ){
                    price_old = Float.parseFloat((String) sourceAsMap.get("price_old"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice_old(price_old);
            list.add(coursePub);
        }

        return new QueryResponseResult(CommonCode.SUCCESS,new QueryResult(list,totalHits));
    }

    //通过id值得到所有课程计划信息
    public Map<String, CoursePub> getallById(String id)  {
        SearchRequest searchRequest = new SearchRequest(es_index);
        searchRequest.types(es_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //精准搜索
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));

        searchRequest.source(searchSourceBuilder);

        SearchResponse search = null;
        try {
            search = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();
        Map<String, CoursePub> map =new HashMap<String, CoursePub>();
        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String courseId = (String) sourceAsMap.get("id");
            String name = (String) sourceAsMap.get("name");
            String grade = (String) sourceAsMap.get("grade");
            String charge = (String) sourceAsMap.get("charge");
            String pic = (String) sourceAsMap.get("pic");
            String description = (String) sourceAsMap.get("description");
            String teachplan = (String) sourceAsMap.get("teachplan");
            CoursePub coursePub = new CoursePub();
            coursePub.setId(courseId);
            coursePub.setName(name);
            coursePub.setPic(pic);
            coursePub.setGrade(grade);
            coursePub.setTeachplan(teachplan);
            coursePub.setDescription(description);
            map.put(courseId,coursePub);
        }
        return map;
    }
    //通过teachplanId获得媒资信息
    public QueryResponseResult getmedia(String[] teachplanIds) {
        SearchRequest searchRequest = new SearchRequest(es_media_index);
        searchRequest.types(es_media_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //源字段过滤
        String[] split = source_media_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});
        //精准查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse =null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        Map<String,CoursePub> map = new HashMap<>();
        //数据列表
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            TeachplanMediaPub teachplanMediaPub =new TeachplanMediaPub();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //取出课程计划媒资信息
            String courseid = (String) sourceAsMap.get("courseid");
            String media_id = (String) sourceAsMap.get("media_id");
            String media_url = (String) sourceAsMap.get("media_url");
            String teachplan_id = (String) sourceAsMap.get("teachplan_id");
            String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
            teachplanMediaPub.setCourseId(courseid); teachplanMediaPub.setMediaUrl(media_url);
            teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
            teachplanMediaPub.setMediaId(media_id); teachplanMediaPub.setTeachplanId(teachplan_id);
            //将数据加入列表
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        //构建返回课程媒资信息对象
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;

    }
}
