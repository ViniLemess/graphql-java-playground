package com.vinilemess.graphqljavaplayground.graphql.client;

import com.vinilemess.graphqljavaplayground.graphql.client.result.GraphQlResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vinilemess.graphqljavaplayground.graphql.client.GraphqlArgumentFormatter.formatQueryWithArguments;

public class GraphQlClient {

    private static final String GRAPHQL_PATH = "/graphql";
    private final RestClient restClient;

    private GraphQlClient(String url) {
        this.restClient = RestClient.create(url);
    }

    public static GraphQlClient create(final String url) {
        return new GraphQlClient(url);
    }

    public GraphQlRequestSpec query(final String query, final Map<String, Object> arguments) {
        return new GraphQlRequestSpec(query, arguments);
    }

    public class GraphQlRequestSpec {
        private final String query;
        private final HttpHeaders headers;
        private final Map<String, Object> arguments;

        private GraphQlRequestSpec(final String query, final Map<String, Object> arguments) {
            this.query = query;
            this.headers = new HttpHeaders();
            this.arguments = arguments;
        }

        public GraphQlRequestSpec header(final String header, final String value) {
            this.headers.add(header, value);
            return this;
        }

        public GraphQlRequestSpec headers(final MultiValueMap<String, String> headers) {
            this.headers.addAll(headers);
            return this;
        }

        public GraphQlResponseSpec execute() {
            final String queryWithArguments = formatQueryWithArguments(this.query, this.arguments);

            return new GraphQlResponseSpec(queryWithArguments, this.headers);
        }

        public CompletableFuture<GraphQlResponseSpec> executeAsync() {
            return CompletableFuture.supplyAsync(this::execute);
        }
    }

    public class GraphQlResponseSpec {

        private final RestClient.ResponseSpec result;
        private Consumer<GraphQlResult> onErrorsHandler;

        public GraphQlResponseSpec(final String query,
                                   final HttpHeaders headers) {
            this.result = restClient.post()
                    .uri(GRAPHQL_PATH)
                    .body(new GraphQlRequestBody(query, getOperationNameOrElseNull(query)).toString())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve();
            this.onErrorsHandler = graphQlResult -> {};
        }

        public GraphQlResponseSpec onStatus4xx(final BiConsumer<HttpRequest, ClientHttpResponse> handler) {
            result.onStatus(HttpStatusCode::is4xxClientError, handler::accept);
            return this;
        }

        public GraphQlResponseSpec onStatus5xx(final BiConsumer<HttpRequest, ClientHttpResponse> handler) {
            result.onStatus(HttpStatusCode::is5xxServerError, handler::accept);
            return this;
        }

        public GraphQlResult getResult() {
            final var graphQlResult = result.toEntity(GraphQlResult.class).getBody();
            this.onErrorsHandler.accept(graphQlResult);
            return graphQlResult;
        }

        private static String getOperationNameOrElseNull(final String query) {
            if (query == null || query.isBlank()) {
                return null;
            }

            final String operationNameRegex = "query\\s+(\\S+)\\s*\\{";
            final Pattern pattern = Pattern.compile(operationNameRegex);
            final Matcher matcher = pattern.matcher(query);

            return matcher.find() ? matcher.group(1) : null;
        }

        public GraphQlResponseSpec doOnError(final Consumer<GraphQlResult> onErrorsHandler) {
            this.onErrorsHandler = onErrorsHandler;
            return this;
        }
    }
}
