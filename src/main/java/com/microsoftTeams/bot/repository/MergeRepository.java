package com.microsoftTeams.bot.repository;

import com.microsoftTeams.bot.models.Merge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MergeRepository extends MongoRepository<Merge, String> {
    Merge findByMergeRequestId(String mergeRequestId);
}
