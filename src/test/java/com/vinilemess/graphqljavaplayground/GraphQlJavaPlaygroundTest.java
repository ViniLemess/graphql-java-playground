package com.vinilemess.graphqljavaplayground;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.vinilemess.graphqljavaplayground.api.mock.usertransaction.Transaction;
import com.vinilemess.graphqljavaplayground.api.mock.usertransaction.User;
import com.vinilemess.graphqljavaplayground.graphql.client.GraphQlAttributePath;
import com.vinilemess.graphqljavaplayground.graphql.client.GraphQlClient;
import com.vinilemess.graphqljavaplayground.graphql.client.result.GraphQlError;
import com.vinilemess.graphqljavaplayground.graphql.client.result.GraphQlError.Extensions;
import com.vinilemess.graphqljavaplayground.graphql.client.result.GraphQlError.Location;
import com.vinilemess.graphqljavaplayground.graphql.client.result.GraphQlResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.wiremock.spring.EnableWireMock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableWireMock
class GraphQlJavaPlaygroundTest {

    private static final String FETCH_USER_TRANSACTIONS_QUERY = """
            query fetchUserTransactions {
              userTransactionByUserSignature(userSignature: $userSignature) {
                userSignature
                user {
                  name
                }
                transactions {
                  amount
                  dateTime
                }
              }
            }
            """;
    private static final String USER_TRANSACTIONS_JSON = """
            {
              "data": {
                "userTransactionByUserSignature": {
                  "userSignature": "userSig",
                  "user": {
                    "name": "John Doe"
                  },
                  "transactions": [
                    {
                      "amount": "10",
                      "dateTime": "2049-10-05T00:00:00"
                    }
                  ]
                }
              }
            }
            """;
    private static final String USER_TRANSACTIONS_WITH_ERRORS = """
            {
              "errors": [
                {
                  "message": "INTERNAL_ERROR for idxyz",
                  "locations": [
                    {
                      "line": 4,
                      "column": 5
                    }
                  ],
                  "path": [
                    "userTransactionByUserSignature",
                    "user"
                  ],
                  "extensions": {
                    "classification": "INTERNAL_ERROR"
                  }
                }
              ],
              "data": {
                "userTransactionByUserSignature": {
                  "userSignature": "userSig",
                  "user": null,
                  "transactions": [
                    {
                      "amount": "10",
                      "dateTime": "2049-10-05T00:00"
                    }
                  ]
                }
              }
            }
            """;
    @Value("${wiremock.server.baseUrl}")
    private String wiremockUrl;

    private GraphQlClient graphQlClient;

    @BeforeEach
    void setup() {
        graphQlClient = GraphQlClient.create(wiremockUrl);
    }

    @Test
    void shouldReturnUserTransactionsWithoutErrors() {
        stubFor(graphqlRequest().willReturn(okJson(USER_TRANSACTIONS_JSON)));

        var expectedUserTransactions = new UserTransactionsTestDto(
                "userSig",
                new User(null, "John Doe"),
                List.of(new Transaction(null, LocalDateTime.of(2049, 10, 5, 0, 0, 0), TEN))
        );

        var result = graphQlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                .execute()
                .getResult()
                .as(UserTransactionsTestDto.class);

        assertEquals(expectedUserTransactions, result);
    }

    @Test
    void whenRequestIsSentWithHeaderShouldReturnUserTransactions() {
        stubFor(graphqlRequest()
                .withHeader("test", equalTo("testing"))
                .willReturn(okJson(USER_TRANSACTIONS_JSON))
        );

        var expectedUserTransactions = new UserTransactionsTestDto(
                "userSig",
                new User(null, "John Doe"),
                List.of(new Transaction(null, LocalDateTime.of(2049, 10, 5, 0, 0, 0), TEN))
        );

        var result = graphQlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                .header("test", "testing")
                .execute()
                .getResult()
                .as(UserTransactionsTestDto.class);

        assertEquals(expectedUserTransactions, result);
    }

    @Test
    void whenRequestIsSentWithHeadersShouldReturnUserTransactions() {
        stubFor(graphqlRequest()
                .withHeader("test", equalTo("testing"))
                .withHeader("tester", equalTo("junit"))
                .willReturn(okJson(USER_TRANSACTIONS_JSON))
        );

        var expectedUserTransactions = new UserTransactionsTestDto(
                "userSig",
                new User(null, "John Doe"),
                List.of(new Transaction(null, LocalDateTime.of(2049, 10, 5, 0, 0, 0), TEN))
        );

        final var headers = new HttpHeaders();
        headers.add("test", "testing");
        headers.add("tester", "junit");

        var result = graphQlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                .headers(headers)
                .execute()
                .getResult()
                .as(UserTransactionsTestDto.class);

        assertEquals(expectedUserTransactions, result);
    }

    @Test
    void shouldExecuteHandlerFunctionWhenGraphqlResponseReturns4xxStatusCode() {
        stubFor(graphqlRequest().willReturn(aResponse().withStatus(400)));

        final var runtimeException = new RuntimeException("4xx test error");

        final var exception = assertThrows(RuntimeException.class, () ->
                graphQlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                        .execute()
                        .onStatus4xx((httpRequest, clientHttpResponse) -> {
                            throw runtimeException;
                        })
                        .getResult()
        );

        assertEquals(runtimeException, exception);
    }

    @Test
    void shouldExecuteHandlerFunctionWhenGraphqlResponseReturns5xxStatusCode() {
        stubFor(graphqlRequest().willReturn(aResponse().withStatus(500)));

        final var runtimeException = new RuntimeException("5xx test error");

        final var exception = assertThrows(RuntimeException.class, () ->
                graphQlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                        .execute()
                        .onStatus5xx((httpRequest, clientHttpResponse) -> {
                            throw runtimeException;
                        })
                        .getResult()
        );

        assertEquals(runtimeException, exception);
    }

    @Test
    void shouldReturnGraphQlResultWithErrorsAndPartialData() {
        stubFor(graphqlRequest().willReturn(okJson(USER_TRANSACTIONS_WITH_ERRORS)));

        var expectedUserTransactions = new UserTransactionsTestDto(
                "userSig",
                null,
                List.of(new Transaction(null, LocalDateTime.of(2049, 10, 5, 0, 0, 0), TEN))
        );
        var expectedErrors = List.of(GraphQlError.builder()
                .path(List.of("userTransactionByUserSignature", "user"))
                .message("INTERNAL_ERROR for idxyz")
                .locations(List.of(new Location(4, 5)))
                .extensions(new Extensions("INTERNAL_ERROR"))
                .build());

        var result = graphQlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                .execute()
                .getResult();

        assertEquals(expectedUserTransactions, result.as(UserTransactionsTestDto.class));
        assertEquals(expectedErrors, result.errors());
    }

    @Test
    void shouldExecuteOnErrorHookWhenGraphqlResultReturnsWithErrors() {
        stubFor(graphqlRequest().willReturn(okJson(USER_TRANSACTIONS_WITH_ERRORS)));

        var exception = assertThrows(RuntimeException.class, () ->
                graphQlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                        .execute()
                        .doOnError(graphqlResult -> {
                            throw new RuntimeException("errors :D");
                        })
                        .getResult()
        );

        assertEquals("errors :D", exception.getMessage());
    }
    
    @GraphQlAttributePath("userTransactionByUserSignature")
    private record UserTransactionsTestDto(String userSignature, User user, List<Transaction> transactions) {
    }

    private static MappingBuilder graphqlRequest() {
        return post("/graphql")
                .withRequestBody(matchingJsonPath("$"))
                .withRequestBody(matchingJsonPath("$.query", matching("^(?!\\s*$).+")));
    }
}
