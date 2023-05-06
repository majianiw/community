package com.nowcoder.community.service;

import com.nowcoder.community.dao.elaticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Date: create in 14:29 2023/3/22
 * @describe: es搜索
 */
@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    public void saveDiscussPost(DiscussPost post){
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public SearchPage<DiscussPost> searchDiscussPost(String keyword, int current,int limit) {
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        //查询
//        SearchHits<DiscussPost> search = elasticsearchTemplate.search(build, DiscussPost.class);
//        //得到查询返回的内容
//        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search, Page.empty().getPageable());
//
//        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
//        //设置一个最后需要返回的实体类集合
//        List<DiscussPost> lists = new ArrayList<>();
//        //遍历返回的内容进行处理
//        for (SearchHit<DiscussPost> searchHit : searchHits) {
//            //高亮的内容
//            Map<String, List<String>> highlightdatas = searchHit.getHighlightFields();
//
//            DiscussPost data = new DiscussPost();
//
//            // 公共字段
//            data.setId(searchHit.getContent().getId());
//            data.setUserId(searchHit.getContent().getUserId());
//            data.setTitle(searchHit.getContent().getTitle());
//            data.setContent(searchHit.getContent().getContent());
//            data.setStatus(searchHit.getContent().getStatus());
//            data.setCreateTime(searchHit.getContent().getCreateTime());
//            data.setCommentCount(searchHit.getContent().getCommentCount());
//
//
//            List<String> title = highlightdatas.get("title");
//            if (title != null) {
//                data.setTitle(title.get(0));
//            }
//            List<String> content = highlightdatas.get("content");
//            if (content != null) {
//                String contentHfStr = content.get(0);
//                data.setContent(contentHfStr);
//            }
//
//            lists.add(data);
//        }
        SearchHits<DiscussPost> search = elasticsearchTemplate.search(build, DiscussPost.class);
        //将其结果返回并进行分页
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search, Page.empty().getPageable());

        if (!page.isEmpty()) {
            for (SearchHit<DiscussPost> discussPostSearch : page) {
                DiscussPost discussPost = discussPostSearch.getContent();
                //取高亮
                List<String> title = discussPostSearch.getHighlightFields().get("title");
                if(title!=null){
                    discussPost.setTitle(title.get(0));
                }
                List<String> content = discussPostSearch.getHighlightFields().get("content");
                if(content!=null){
                    discussPost.setContent(content.get(0));
                }
            }
        }
        return page;
    }
}









