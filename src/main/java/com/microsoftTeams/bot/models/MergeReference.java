package com.microsoftTeams.bot.models;

import com.microsoftTeams.bot.helpers.Author;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mergeRequest")
@SuppressWarnings("unused")
public class MergeReference {
    @Id
    private String id;
    private String mergeRequestId;
    private String teamsChannelId;
    private Author author;
    private String conversationReference;
    private String projectName;
    private String url;

    public MergeReference() {
    }

    public MergeReference(String mergeRequestId, Author author, String teamsChannelId, String conversationReference) {
        this.mergeRequestId = mergeRequestId;
        this.author = author;
        this.teamsChannelId = teamsChannelId;
        this.conversationReference = conversationReference;
    }

    public MergeReference(String mergeRequestId, String projectName, String url, Author author) {
        this.mergeRequestId = mergeRequestId;
        this.projectName = projectName;
        this.url = url;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMergeRequestId() {
        return mergeRequestId;
    }

    public void setMergeRequestId(String mergeRequestId) {
        this.mergeRequestId = mergeRequestId;
    }

    public String getTeamsChannelId() {
        return teamsChannelId;
    }

    public void setTeamsChannelId(String teamsChannelId) {
        this.teamsChannelId = teamsChannelId;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getConversationReference() {
        return conversationReference;
    }

    public void setConversationReference(String conversationReference) {
        this.conversationReference = conversationReference;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
