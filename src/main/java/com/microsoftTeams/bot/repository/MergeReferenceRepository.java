package com.microsoftTeams.bot.repository;

import com.microsoftTeams.bot.models.MergeReference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MergeReferenceRepository extends MongoRepository<MergeReference, String> {
    List<MergeReference> findByMergeRequestId(String mergeRequestId);
    MergeReference findByMergeRequestIdAndTeamsChannelId(String mergeRequestId, String teamsChannelId);
}
