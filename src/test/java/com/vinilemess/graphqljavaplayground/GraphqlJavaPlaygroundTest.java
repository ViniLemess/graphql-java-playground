package com.vinilemess.graphqljavaplayground;

import com.vinilemess.graphqljavaplayground.api.mock.usertransaction.Transaction;
import com.vinilemess.graphqljavaplayground.api.mock.usertransaction.User;
import com.vinilemess.graphqljavaplayground.graphql.client.GraphqlClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.wiremock.spring.EnableWireMock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableWireMock
class GraphqlJavaPlaygroundTest {

    public static final String FETCH_USER_TRANSACTIONS_QUERY = """
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
    public static final String USER_TRANSACTIONS_JSON = """
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
    @Value("${wiremock.server.baseUrl}")
    private String wiremockUrl;

    private GraphqlClient graphqlClient;

    @BeforeEach
    void setup() {
        graphqlClient = GraphqlClient.create(wiremockUrl);
    }

    @Test
    void shouldReturnUserTransactionsWithoutErrors() {
        stubFor(post("/graphql").willReturn(okJson(USER_TRANSACTIONS_JSON)));

        var expectedUserTransactions = new UserTransactionsTestDto(
                "userSig",
                new User(null, "John Doe"),
                List.of(new Transaction(null, LocalDateTime.of(2049, 10, 5, 0, 0, 0), TEN))
        );

        var result = graphqlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                .execute()
                .getResult()
                .as(UserTransactionsTestDto.class, "userTransactionByUserSignature");

        assertEquals(expectedUserTransactions, result);
    }

    @Test
    void whenRequestIsSendWithHeaderShouldReturnUserTransactions() {
        stubFor(post("/graphql")
                .withHeader("test", equalTo("testing"))
                .willReturn(okJson(USER_TRANSACTIONS_JSON))
        );

        var expectedUserTransactions = new UserTransactionsTestDto(
                "userSig",
                new User(null, "John Doe"),
                List.of(new Transaction(null, LocalDateTime.of(2049, 10, 5, 0, 0, 0), TEN))
        );

        var result = graphqlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                .header("test", "testing")
                .execute()
                .getResult()
                .as(UserTransactionsTestDto.class, "userTransactionByUserSignature");

        assertEquals(expectedUserTransactions, result);
    }

    @Test
    void whenRequestIsSendWithHeadersShouldReturnUserTransactions() {
        stubFor(post("/graphql")
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

        var result = graphqlClient.query(FETCH_USER_TRANSACTIONS_QUERY, Map.of("userSignature", "userSig"))
                .headers(headers)
                .execute()
                .getResult()
                .as(UserTransactionsTestDto.class, "userTransactionByUserSignature");

        assertEquals(expectedUserTransactions, result);
    }

    private record UserTransactionsTestDto(String userSignature, User user, List<Transaction> transactions) {
    }
}
