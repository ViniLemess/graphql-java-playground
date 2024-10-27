package com.vinilemess.graphqljavaplayground.graphql.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vinilemess.graphqljavaplayground.graphql.client.result.GraphqlResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNullElse;

public class GraphqlClient {

    private static final String GRAPHQL_PATH = "/graphql";
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GraphqlClient(final @Value("${graphql-api.url}") String url) {
        this.restClient = RestClient.create(url);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public <T> GraphqlRequestBuilder<T> query(final String query, final Map<String, Object> arguments) {
        return new GraphqlRequestBuilder<>(query, arguments);
    }

    public class GraphqlRequestBuilder<T> {
        private final String query;
        private final HttpHeaders headers;
        private final Map<String, Object> arguments;

        private GraphqlRequestBuilder(final String query, final Map<String, Object> arguments) {
            this.query = query;
            this.headers = new HttpHeaders();
            this.arguments = arguments;
        }

        public GraphqlRequestBuilder<T> header(final String header, final String value) {
            this.headers.add(header, value);
            return this;
        }

        public GraphqlRequestBuilder<T> headers(final MultiValueMap<String, String> headers) {
            this.headers.addAll(headers);
            return this;
        }

        public GraphqlExecution<T> execute() {
            final String queryWithArguments = formatQueryWithArguments(this.query, this.arguments);

            return new GraphqlExecution<>(queryWithArguments, this.headers);
        }

        private String formatQueryWithArguments(final String query,
                                                final Map<String, Object> arguments) {
            return arguments.entrySet()
                    .stream()
                    .reduce(query, (q, entry) -> {
                        final String argumentValue = requireNonNullElse(entry.getValue(), "null").toString();
                        final String argumentPlaceholder = "$%s".formatted(entry.getKey());
                        return q.replace(argumentPlaceholder, argumentValue);
                    }, (q1, q2) -> q1);
        }
    }

    public class GraphqlExecution<T> {

        private final ResponseEntity<GraphqlResult> result;

        public GraphqlExecution(final String query,
                                final HttpHeaders headers) {
            result = restClient.post()
                    .uri(GRAPHQL_PATH)
                    .body(new GraphqlRequestBody(query, getOperationNameOrElseNull(query)).toString())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .toEntity(GraphqlResult.class);
        }

        public T retriveAs(final Class<?> clazz) {
            return objectMapper.convertValue(result.getBody().data(), (Class<T>) clazz);
        }

        public GraphqlResult getResult() {
            return result.getBody();
        }

        private static String getOperationNameOrElseNull(final String query) {
            if (query == null || query.isBlank()) {
                return null;
            }

            final String patternString = "query\\s+(\\S+)\\s*\\{";
            final Pattern pattern = Pattern.compile(patternString);
            final Matcher matcher = pattern.matcher(query);

            return matcher.find() ? matcher.group(1) : null;
        }
    }
}
