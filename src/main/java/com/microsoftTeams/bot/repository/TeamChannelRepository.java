package com.microsoftTeams.bot.repository;

import com.microsoftTeams.bot.models.TeamChannel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamChannelRepository extends MongoRepository<TeamChannel, String> {

    TeamChannel findByTeamsChannelId(String teamsChannelId);
    List<TeamChannel> findByMembersEmail(String email);
}

