package com.thong.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thong.model.Product;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.slice.SliceBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ElasticSearch {

    private static RestHighLevelClient client;
    private ObjectMapper objectMapper = new ObjectMapper();


    public ElasticSearch() {
        getClient();
    }

    private void getClient() {
        if (client == null) {
            client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("localhost", 9200, "http")));
        }
    }


    public void createIndex(String indexName) {
        CreateIndexRequest request = new CreateIndexRequest(indexName);

        // index settings
        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );

        request.mapping(
                "{\n" +
                        "   \"properties\":{\n" +
                        "      \"name\":{\n" +
                        "         \"type\":\"text\"\n" +
                        "      },\n" +
                        "      \"msdn\":{\n" +
                        "         \"type\":\"keyword\"\n" +
                        "      },\n" +
                        "      \"age\":{\n" +
                        "         \"type\":\"integer\"\n" +
                        "      }\n" +
                        "   }\n" +
                        "}",
                XContentType.JSON);

        request.alias(new Alias(indexName + "_alias"));
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public List<Product> readFileAndImport(String file) throws IOException {
        List<Product> products = new ArrayList<Product>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                Product product = new Product();
                product.setName("viettel");
                product.setMsdn(line);
                product.setAge(Integer.valueOf(RandomStringUtils.randomNumeric(2)));
                products.add(product);

                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void bulkInsert(List bookList) {
        BulkRequest bulkRequest = new BulkRequest();
        long countBulk = 0;
        try {
            for (Object product : bookList) {
                IndexRequest indexRequest = new IndexRequest("product").
                        source(objectMapper.convertValue(product, Map.class));
                bulkRequest.add(indexRequest);
                countBulk++;
                if (countBulk % 10000 == 0) {
                    client.bulk(bulkRequest, RequestOptions.DEFAULT);
                    bulkRequest = new BulkRequest();
                }
            }
            if (countBulk > 0 && bulkRequest.numberOfActions() > 0) {
                client.bulk(bulkRequest, RequestOptions.DEFAULT);
            }

        } catch (IOException e) {
            System.out.println(e);
        }


        System.out.println("OKKKKKK");
    }

    public boolean deleteIndex(String indexName) {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        try {
            AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
            return deleteIndexResponse.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<Product> convertSearchHitToObject(SearchHit[] searchHit, Class < ?>cls){
        List<Product> products = new ArrayList<Product>();
        for (SearchHit hit : searchHit) {
            String jsonString = hit.getSourceAsString();
            try {
                Product p = (Product) objectMapper.readValue(jsonString, cls);
                products.add(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return products;

    }

    public List<Product> searchScroll(String index,final int batchSize) {
        Instant start = Instant.now();
        // time passes

        int numSlice = 3;
        SearchHit[] resultHits = IntStream.range(0, numSlice).parallel().mapToObj(i -> {
            // The Scroll API can be used to retrieve a large number of results from a search request
            SearchHit[] totalHits = new SearchHit[0];

            final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
            SearchRequest searchRequest = new SearchRequest(index);
            searchRequest.scroll(scroll);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchSourceBuilder.slice(new SliceBuilder(i, numSlice));
            searchSourceBuilder.size(batchSize);
            searchRequest.source(searchSourceBuilder);
            try {

                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                String scrollId = searchResponse.getScrollId();
                SearchHit[] searchHits = searchResponse.getHits().getHits();
                totalHits = ArrayUtils.addAll(totalHits, searchHits);

                while (searchHits != null && searchHits.length > 0) {

                    SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                    scrollRequest.scroll(scroll);
                    try {
                        searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    scrollId = searchResponse.getScrollId();
                    searchHits = searchResponse.getHits().getHits();
                    totalHits = ArrayUtils.addAll(totalHits, searchHits);
                }

                ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
                clearScrollRequest.addScrollId(scrollId);
                client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);


            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return totalHits;
        }).reduce(new SearchHit[0], ArrayUtils::addAll);


        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);

        System.out.println("timeElapsed: " + timeElapsed.getSeconds());

        return convertSearchHitToObject(resultHits, Product.class);




    }
}
