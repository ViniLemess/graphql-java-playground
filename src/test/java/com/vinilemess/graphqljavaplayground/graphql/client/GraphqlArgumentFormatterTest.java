package com.vinilemess.graphqljavaplayground.graphql.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class GraphqlArgumentFormatterTest {

    @ParameterizedTest(name = "argument: {0}, expected: {1}")
    @MethodSource("typeArguments")
    @DisplayName("should format query with accordingly: ")
    void shouldFormatQueryWithArguments(final Object argument, final String expectedResult) {
        final String query = "query { testQuery(argument: $argument) }";
        final Map<String, Object> arguments = new HashMap<>(Map.of("argument", argument));

        final String result = GraphqlArgumentFormatter.formatQueryWithArguments(query, arguments);

        assertEquals("query { testQuery(argument: %s) }".formatted(expectedResult), result);
    }

    @Test
    void shouldFormatQueryWwithNullArgument() {
        final String query = "query { testQuery(argument: $argument) }";
        final Map<String, Object> arguments = new HashMap<>();
        arguments.put("argument", null);

        final String result = GraphqlArgumentFormatter.formatQueryWithArguments(query, arguments);

        assertEquals("query { testQuery(argument: null) }", result);
    }

    public static Stream<Arguments> typeArguments() {
        return Stream.of(
                arguments(TestEnum.VALUE1, "VALUE1"),
                arguments(TestEnum.VALUE2, "VALUE2"),
                arguments(1L, "1"),
                arguments(1.5, "1.5"),
                arguments("test", "\"test\""),
                arguments(List.of(TestEnum.VALUE1, 1L, 1.5, "test"), "[VALUE1, 1, 1.5, \"test\"]"),
                arguments(new TestObject("test"), "{ test: \"test\" }")
        );
    }

    private enum TestEnum {
        VALUE1, VALUE2;
    }

    private record TestObject(String test) {

        @Override
        public String toString() {
            return "{ test: \"%s\" }".formatted(test);
        }
    }
}