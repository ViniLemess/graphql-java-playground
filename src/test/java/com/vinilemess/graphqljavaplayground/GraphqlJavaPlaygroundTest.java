package com.vinilemess.graphqljavaplayground;

import com.vinilemess.graphqljavaplayground.api.mock.usertransaction.Transaction;
import com.vinilemess.graphqljavaplayground.api.mock.usertransaction.User;
import com.vinilemess.graphqljavaplayground.graphql.client.GraphqlClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Value("${wiremock.server.baseUrl}")
    private String wiremockUrl;

    private GraphqlClient graphqlClient;

    @BeforeEach
    void setup() {
        graphqlClient = new GraphqlClient(String.format(wiremockUrl));
        stubFor(post("/graphql").willReturn(okJson("""
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
                """)));
    }

    @Test
    void shouldReturnUserTransactionsWithoutErrors() {
        var expectedUserTransactions = new UserTransactionsTestDto(
                "userSig",
                new User(null, "John Doe"),
                List.of(new Transaction(null, LocalDateTime.of(2049, 10, 5, 0, 0, 0), TEN))
        );

        var result = graphqlClient.query("""
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
                """, Map.of("userSignature", "userSig"))
                .execute()
                .getResult()
                .as(UserTransactionsTestDto.class, "userTransactionByUserSignature");

        assertEquals(expectedUserTransactions, result);
    }

    private record UserTransactionsTestDto(String userSignature, User user, List<Transaction> transactions) {
    }
}
