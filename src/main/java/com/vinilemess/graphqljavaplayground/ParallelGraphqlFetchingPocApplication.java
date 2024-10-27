package com.vinilemess.graphqljavaplayground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class ParallelGraphqlFetchingPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParallelGraphqlFetchingPocApplication.class, args);
    }

}
