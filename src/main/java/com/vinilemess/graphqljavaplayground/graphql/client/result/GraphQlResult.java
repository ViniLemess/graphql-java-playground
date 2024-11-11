package com.vinilemess.graphqljavaplayground.graphql.client.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinilemess.graphqljavaplayground.graphql.client.GraphQlAttributePath;
import com.vinilemess.graphqljavaplayground.graphql.client.ObjectMapperUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;

public record GraphQlResult(LinkedHashMap<Object, Object> data, Collection<GraphQlError> errors) {

    private static final ObjectMapper objectMapper = ObjectMapperUtils.createObjectMapper();

    public <T> T as(final Class<T> clazz) {
        final String attributePath = getAttributePathFromAnnotation(clazz);
        return objectMapper.convertValue(data.get(attributePath), clazz);
    }

    public <T> T as(final Class<T> clazz, final ObjectMapper objectMapper) {
        final String attributePath = getAttributePathFromAnnotation(clazz);
        return objectMapper.convertValue(data.get(attributePath), clazz);
    }

    private <T> String getAttributePathFromAnnotation(final Class<T> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(GraphQlAttributePath.class))
                .map(GraphQlAttributePath::value)
                .orElseThrow(() -> new IllegalArgumentException("Class " + clazz.getName() + " must have a GraphQlAttributePath annotation"));
    }
}
