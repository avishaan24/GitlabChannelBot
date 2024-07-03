package com.microsoftTeams.bot.models;

import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "teamChannel")
@SuppressWarnings("unused")
public class TeamChannel {

    @Id
    private String id;

    private String teamsChannelId;

    private String serviceUrl;

    private List<String> emails;

    private List<TeamsChannelAccount> members;

    public TeamChannel() {
    }

    public TeamChannel(String teamsChannelId, String serviceUrl, List<TeamsChannelAccount> members) {
        this.teamsChannelId = teamsChannelId;
        this.serviceUrl = serviceUrl;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamsChannelId() {
        return teamsChannelId;
    }

    public void setTeamsChannelId(String teamsChannelId) {
        this.teamsChannelId = teamsChannelId;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<TeamsChannelAccount> getMembers() {
        return members;
    }

    public void setMembers(List<TeamsChannelAccount> members) {
        this.members = members;
    }
}
