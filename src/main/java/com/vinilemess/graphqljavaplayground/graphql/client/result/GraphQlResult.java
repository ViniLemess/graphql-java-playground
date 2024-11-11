package com.vinilemess.graphqljavaplayground.graphql.client.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinilemess.graphqljavaplayground.graphql.client.GraphQlAttributePath;
import com.vinilemess.graphqljavaplayground.graphql.client.ObjectMapperUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Represents the result of a GraphQL query execution, including the data and any errors.
 *
 * @param data   the data returned by the GraphQL query, mapped as a LinkedHashMap.
 * @param errors the collection of errors returned by the GraphQL query, if any.
 */
public record GraphQlResult(LinkedHashMap<Object, Object> data, Collection<GraphQlError> errors) {

    /**
     * A pre-configured instance of {@link ObjectMapper} used for JSON serialization
     * and deserialization within the {@link GraphQlResult} class.
     * <p>
     * This ObjectMapper instance is configured to disable writing dates as timestamps
     * and to use the JavaTimeModule for handling Java 8 date and time types.
     */
    private static final ObjectMapper objectMapper = ObjectMapperUtils.createObjectMapper();

    /**
     * Converts the GraphQL query result to an instance of the specified class type.
     *
     * @param <T>   the type of the class to convert the result into.
     * @param clazz the class representing the type to convert the result into.
     * @return an instance of the specified class type with data populated from the GraphQL result.
     * @throws IllegalArgumentException if the specified class does not have a {@link GraphQlAttributePath} annotation.
     */
    public <T> T as(final Class<T> clazz) {
        final String attributePath = getAttributePathFromAnnotation(clazz);
        return objectMapper.convertValue(data.get(attributePath), clazz);
    }

    /**
     * Converts the GraphQL query result to an instance of the specified class type using the provided instance of
     * {@link ObjectMapper}.
     *
     * @param <T>          the type of the class to convert the result into.
     * @param clazz        the class representing the type to convert the result into.
     * @param objectMapper the {@link ObjectMapper} to use for deserialization.
     * @return an instance of the specified class type with data populated from the GraphQL result.
     * @throws IllegalArgumentException if the specified class does not have a {@link GraphQlAttributePath} annotation.
     */
    public <T> T as(final Class<T> clazz, final ObjectMapper objectMapper) {
        final String attributePath = getAttributePathFromAnnotation(clazz);
        return objectMapper.convertValue(data.get(attributePath), clazz);
    }

    /**
     * Retrieves the attribute path from the {@link GraphQlAttributePath} annotation on the specified class.
     * The attribute path is used to locate the relevant data in a GraphQL response.
     *
     * @param <T>   the type of the class that has the {@link GraphQlAttributePath} annotation.
     * @param clazz the class from which to retrieve the attribute path.
     * @return the attribute path specified in the {@link GraphQlAttributePath} annotation.
     * @throws IllegalArgumentException if the specified class does not have a {@link GraphQlAttributePath} annotation.
     */
    private <T> String getAttributePathFromAnnotation(final Class<T> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(GraphQlAttributePath.class))
                .map(GraphQlAttributePath::value)
                .orElseThrow(() -> new IllegalArgumentException("Class " + clazz.getName() + " must have a GraphQlAttributePath annotation"));
    }
}
