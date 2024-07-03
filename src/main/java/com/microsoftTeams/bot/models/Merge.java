package com.microsoftTeams.bot.models;

import com.microsoftTeams.bot.helpers.Author;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "merge")
@SuppressWarnings("unused")
public class Merge {

    @Id
    private String id;

    private String mergeRequestId;
    private String projectName;
    private String title;
    private String url;
    private Author author;
    private String lastCommitId;

    public Merge(String mergeRequestId, String projectName, String title, String url, Author author, String lastCommitId) {
        this.mergeRequestId = mergeRequestId;
        this.projectName = projectName;
        this.title = title;
        this.url = url;
        this.author = author;
        this.lastCommitId = lastCommitId;
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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }
}
