package com.vinilemess.parallelgraphqlfetchingpoc.usertransaction.user;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final MongoTemplate mongoTemplate;

    public UserRepository(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public User findUserByTransactionSignature(final String transactionSignature) {
        return mongoTemplate.findOne(Query.query(Criteria.where("transactionSignature").is(transactionSignature)), User.class);
    }
}
