package com.vinilemess.graphqljavaplayground.graphql.client;

/**
 * Represents the body of a GraphQL request.
 *
 * This record contains the GraphQL query and the operation name associated with the request.
 * It provides a string representation of the request body in JSON format.
 *
 * @param query          the GraphQL query as a string
 * @param operationName  the name of the GraphQL operation
 */
public record GraphQlRequestBody(String query, String operationName) {

    @Override
    public String toString() {
        return """
                {
                  "query": %s,
                  "operationName": %s
                }
                """.formatted(query, operationName);
    }
}
