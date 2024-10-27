package com.vinilemess.graphqljavaplayground.graphql.client;

public record GraphQlRequestBody(String query, String operationName) {

    @Override
    public String toString() {
        return """
                "query": %s,
                "operationName": %s
                """.formatted(query, operationName);
    }
}
