package com.vinilemess.graphqljavaplayground.graphql.client.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinilemess.graphqljavaplayground.graphql.client.ObjectMapperUtils;

import java.util.Collection;
import java.util.LinkedHashMap;

public record GraphQlResult(LinkedHashMap<Object, Object> data, Collection<GraphQlError> errors) {

    private static final ObjectMapper objectMapper = ObjectMapperUtils.createObjectMapper();

    public <T> T as(final Class<T> clazz, final String attributePath) {
        final Object data = this.data.get(attributePath);

        return objectMapper.convertValue(data, clazz);
    }
}
