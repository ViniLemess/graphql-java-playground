package com.vinilemess.parallelgraphqlfetchingpoc.usertransaction;

import com.vinilemess.parallelgraphqlfetchingpoc.usertransaction.transaction.Transaction;
import com.vinilemess.parallelgraphqlfetchingpoc.usertransaction.transaction.TransactionRepository;
import com.vinilemess.parallelgraphqlfetchingpoc.usertransaction.user.User;
import com.vinilemess.parallelgraphqlfetchingpoc.usertransaction.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalTime;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Controller
public class UserTransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final Logger logger;

    public UserTransactionController(final TransactionRepository transactionRepository,
                                     final UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
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
            return userRepository.findUserByTransactionSignature(userTransaction.userSignature());
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
            return transactionRepository.findByUserSignature(userTransaction.userSignature());
        });
    }
}
