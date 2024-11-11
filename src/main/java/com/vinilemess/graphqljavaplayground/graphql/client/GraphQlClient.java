package com.vinilemess.graphqljavaplayground.graphql.client;

import com.vinilemess.graphqljavaplayground.graphql.client.result.GraphQlResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vinilemess.graphqljavaplayground.graphql.client.GraphqlArgumentFormatter.formatQueryWithArguments;

/**
 * {@code GraphQlClient} is a client for interacting with GraphQL endpoints using spring's {@link RestClient}.
 * <p>
 * This client supports creating and sending GraphQL queries with custom headers and arguments.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre>
 * GraphQlClient client = GraphQlClient.create("http://example.com");
 * GraphQlResult result = client.query("query hello { hello }", Map.of())
 *                               .header("Authorization", "Bearer token")
 *                               .execute()
 *                               .getResult();
 * </pre>
 */
public class GraphQlClient {

    private static final String GRAPHQL_PATH = "/graphql";
    private final RestClient restClient;

    private GraphQlClient(final String url) {
        this.restClient = RestClient.create(url);
    }

    /**
     * Constructs a GraphQlClient with the specified URL.
     *
     * @param url The URL to initialize the RestClient.
     */
    public static GraphQlClient create(final String url) {
        return new GraphQlClient(url);
    }

    /**
     * Creates a new {@code GraphQlRequestSpec} with the specified GraphQL query and arguments.
     *
     * @param query the GraphQL query string
     * @param arguments the arguments to be used in the GraphQL query
     * @return a {@code GraphQlRequestSpec} object initialized with the specified query and arguments.
     */
    public GraphQlRequestSpec query(final String query, final Map<String, Object> arguments) {
        return new GraphQlRequestSpec(query, arguments);
    }

    /**
     * Represents a specification for a GraphQL request.
     * This class encapsulates the query string, headers, and arguments
     * required for making a GraphQL request.
     */
    public class GraphQlRequestSpec {
        private final String query;
        private final HttpHeaders headers;
        private final Map<String, Object> arguments;

        /**
         * Constructs a new {@code GraphQlRequestSpec} with the specified query and arguments.
         *
         * @param query The GraphQL query string.
         * @param arguments The arguments to be used in the GraphQL query.
         */
        private GraphQlRequestSpec(final String query, final Map<String, Object> arguments) {
            this.query = query;
            this.headers = new HttpHeaders();
            this.arguments = arguments;
        }

        /**
         * Adds a header to the GraphQL request specification.
         *
         * @param header the name of the header to be added
         * @param value the value of the header to be added
         * @return the current {@code GraphQlRequestSpec} object with the added header.
         */
        public GraphQlRequestSpec header(final String header, final String value) {
            this.headers.add(header, value);
            return this;
        }

        /**
         * Adds multiple headers to the GraphQL request specification.
         *
         * @param headers the headers to be added, represented as a MultiValueMap where keys are header names and values are lists of header values
         * @return the current {@code GraphQlRequestSpec} object with the added headers.
         */
        public GraphQlRequestSpec headers(final MultiValueMap<String, String> headers) {
            this.headers.addAll(headers);
            return this;
        }

        /**
         * Executes the GraphQL request using the predefined query and arguments, and returns a {@code GraphQlResponseSpec}
         * that provides methods for handling the response.
         *
         * @return a {@code GraphQlResponseSpec} object initialized with the formatted query and headers.
         */
        public GraphQlResponseSpec execute() {
            final String queryWithArguments = formatQueryWithArguments(this.query, this.arguments);

            return new GraphQlResponseSpec(queryWithArguments, this.headers);
        }
    }

    /**
     * Class representing the specification of a GraphQL response.
     * It encapsulates methods to handle and retrieve the response from a GraphQL query.
     */
    public class GraphQlResponseSpec {

        private final RestClient.ResponseSpec result;
        private Consumer<GraphQlResult> onErrorsHandler;

        /**
         * Constructs a {@code GraphQlResponseSpec} with the specified GraphQL query and HTTP headers.
         *
         * @param query   the GraphQL query to be posted
         * @param headers the HTTP headers to be included in the request
         */
        public GraphQlResponseSpec(final String query,
                                   final HttpHeaders headers) {
            this.result = restClient.post()
                    .uri(GRAPHQL_PATH)
                    .body(new GraphQlRequestBody(query, getOperationNameOrElseNull(query)).toString())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve();
            this.onErrorsHandler = graphQlResult -> {};
        }

        /**
         * Registers a handler that is invoked when the response has a 4xx HTTP status code.
         *
         * @param handler a {@code BiConsumer} that accepts the {@code HttpRequest} and {@code ClientHttpResponse}
         *                when a 4xx client error occurs.
         * @return the {@code GraphQlResponseSpec} instance for method chaining.
         */
        public GraphQlResponseSpec onStatus4xx(final BiConsumer<HttpRequest, ClientHttpResponse> handler) {
            result.onStatus(HttpStatusCode::is4xxClientError, handler::accept);
            return this;
        }

        /**
         * Registers a handler that is invoked when the response has a 5xx HTTP status code.
         *
         * @param handler a {@code BiConsumer} that accepts the {@code HttpRequest} and {@code ClientHttpResponse}
         *                when a 5xx server error occurs.
         * @return the {@code GraphQlResponseSpec} instance for method chaining.
         */
        public GraphQlResponseSpec onStatus5xx(final BiConsumer<HttpRequest, ClientHttpResponse> handler) {
            result.onStatus(HttpStatusCode::is5xxServerError, handler::accept);
            return this;
        }

        /**
         * Registers a handler that is invoked when the GraphQL response contains errors.
         *
         * @param onErrorsHandler a {@code Consumer} that processes any errors present in the {@code GraphQlResult}.
         * @return the {@code GraphQlResponseSpec} instance for method chaining.
         */
        public GraphQlResponseSpec doOnError(final Consumer<GraphQlResult> onErrorsHandler) {
            this.onErrorsHandler = onErrorsHandler;
            return this;
        }

        /**
         * Retrieves the GraphQL result from the response entity.
         * This method also processes any errors using the registered error handler.
         *
         * @return the GraphQL result as a {@code GraphQlResult} object.
         */
        public GraphQlResult getResult() {
            final var graphQlResult = result.toEntity(GraphQlResult.class).getBody();
            this.onErrorsHandler.accept(graphQlResult);
            return graphQlResult;
        }

        /**
         * Extracts the operation name from a GraphQL query string if present.
         *
         * @param query the GraphQL query string.
         * @return the extracted operation name, or null if no operation name is found or the query is blank or null.
         */
        private static String getOperationNameOrElseNull(final String query) {
            if (query == null || query.isBlank()) {
                return null;
            }

            final String operationNameRegex = "query\\s+(\\S+)\\s*\\{";
            final Pattern pattern = Pattern.compile(operationNameRegex);
            final Matcher matcher = pattern.matcher(query);

            return matcher.find() ? matcher.group(1) : null;
        }
    }
}
