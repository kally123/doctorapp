package com.healthapp.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;

/**
 * Search Service Application.
 * 
 * Provides doctor search and discovery functionality using Elasticsearch.
 */
@SpringBootApplication(exclude = {
    R2dbcAutoConfiguration.class,
    R2dbcDataAutoConfiguration.class
})
public class SearchServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
