package com.thong.util;

import com.thong.model.Product;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class Test {

    //private static final Logger LOG = Logger.getLogger(Test.class);

    public static void main(String[] args) throws IOException {
        String index = "product";
        ElasticSearch es = new ElasticSearch();

        //es.deleteIndex(index);
        //es.createIndex(index);

//        List<Product> products=  es.readFileAndImport("product.csv");
//        es.bulkInsert(products);
        List<Product> products = es.searchScroll(index, 1000);
        System.out.println("result:  " + products.size());


    }
}
