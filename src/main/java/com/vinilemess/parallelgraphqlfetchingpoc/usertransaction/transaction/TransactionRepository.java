package com.vinilemess.parallelgraphqlfetchingpoc.usertransaction.transaction;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionRepository {

    private final MongoTemplate mongoTemplate;

    public TransactionRepository(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Transaction> findByUserSignature(final String userSignature) {
        return mongoTemplate.find(Query.query(Criteria.where("userSignature").is(userSignature)), Transaction.class);
    }
}
