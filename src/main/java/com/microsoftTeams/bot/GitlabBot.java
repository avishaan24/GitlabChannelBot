package com.microsoftTeams.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.builder.teams.TeamsInfo;
import com.microsoftTeams.bot.helpers.Channel;
import com.microsoftTeams.bot.models.TeamChannel;
import com.microsoftTeams.bot.repository.TeamChannelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * This class implements the functionality of the Bot.
 */

@SuppressWarnings("unused")
public class GitlabBot extends TeamsActivityHandler {

    private static final Logger logger = LoggerFactory.getLogger(GitlabBot.class);

    @Autowired
    private TeamChannelRepository teamChannelRepository;


    public GitlabBot() {
    }

    @Override
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        Channel channel = getChannelDetails(turnContext);
        TeamsInfo.getMembers(turnContext)
                .thenApply(members -> members.stream()
                        .filter(member -> !member.getName().equals("Bot")) // Filter out the bot
                        .collect(Collectors.toList()))
                .thenAccept(members -> {
                    // Find or create TeamChannel entity based on teamsChannelId
                    TeamChannel teamChannel = teamChannelRepository.findByTeamsChannelId(channel.getTeam().getId());
                    if (teamChannel == null) {
                        // Create new TeamChannel entity and save it
                        TeamChannel teamChannelAdd = new TeamChannel(channel.getTeam().getId(), turnContext.getActivity().getServiceUrl(), members);
                        teamChannelRepository.save(teamChannelAdd);
                    } else {
                        // Update existing TeamChannel entity and save it
                        teamChannel.setMembers(members);
                        teamChannelRepository.save(teamChannel);
                    }
                });
        return super.onConversationUpdateActivity(turnContext);
    }

    private Channel getChannelDetails(TurnContext turnContext){
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            // Serialize to JSON string
            json = objectMapper.writeValueAsString(turnContext.getActivity().getChannelData());
        } catch (JsonProcessingException e) {
            logger.error("A JsonProcessingException occurred : {}", e.getMessage(), e);// Handle the exception according to your application's error handling strategy
        }
        AtomicReference<Channel> channelLocal = new AtomicReference<>();
        try{
            // Deserialize from JSON string and map with Channel class
            channelLocal.set(objectMapper.readValue(json, Channel.class));
        }catch (JsonProcessingException e){
            logger.error("A JsonProcessingException occurred : {}", e.getMessage(), e);
        }
        return channelLocal.get();
    }
}
