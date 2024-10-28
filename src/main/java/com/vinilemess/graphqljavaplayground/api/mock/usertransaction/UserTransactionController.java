package com.vinilemess.graphqljavaplayground.api.mock.usertransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Controller
public class UserTransactionController {

    private final Logger logger;

    public UserTransactionController() {
        this.logger = LoggerFactory.getLogger(UserTransactionController.class);
    }

    @QueryMapping(name = "userTransactionByUserSignature")
    public CompletableFuture<UserTransaction> findUserTransactionByUserSignature(@Argument(name = "userSignature") final String userSignature) {
        logger.info("Fetching user transactions at {}", LocalTime.now());
        return supplyAsync(() -> new UserTransaction(userSignature));
    }

    @SchemaMapping(typeName = "UserTransaction", field = "user")
    public CompletableFuture<User> findUserByTransactionSignature(final UserTransaction userTransaction) {
        return supplyAsync(() -> {
            logger.info("Fetching user at {}", LocalTime.now());
            throw new RuntimeException("");
//            return new User("id", "John Doe");
        });
    }

    @SchemaMapping(typeName = "UserTransaction", field = "transactions")
    public CompletableFuture<Collection<Transaction>> findTransactionsByUserSignature(final UserTransaction userTransaction) {
        return supplyAsync(() -> {
            try {
                // sleep for 5 seconds to test concurrency
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Fetching transactions at {}", LocalTime.now());
            return List.of(new Transaction("id", LocalDateTime.of(2049, 10, 5, 0, 0, 0), BigDecimal.TEN));
        });
    }
}
