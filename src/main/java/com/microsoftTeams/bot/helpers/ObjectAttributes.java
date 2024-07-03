package com.microsoftTeams.bot.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ObjectAttributes which is used to store details about the webhook notification from Gitlab
 */

@SuppressWarnings("unused")
public class ObjectAttributes {
    @JsonProperty("author_id")
    private String authorId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("iid")
    private Long iid;

    @JsonProperty("state")
    private String state;

    @JsonProperty("title")
    private String title;

    @JsonProperty("last_commit")
    private LastCommit lastCommit;

    @JsonProperty("note")
    private String note;

    @JsonProperty("noteable_type")
    private String noteableType;

    @JsonProperty("noteable_id")
    private String noteableId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("status")
    private String status;

    @JsonProperty("ref")
    private String ref;

    @JsonProperty("merge_status")
    private String mergeStatus;

    @JsonProperty("sha")
    private String sha;

    @JsonProperty("head_pipeline_id")
    private String headPipelineId;

    @JsonProperty("detailed_merge_status")
    private String detailedMergeStatus;

    @JsonProperty("action")
    private String action;

    public ObjectAttributes() {
    }

    public String getMergeStatus() {
        return mergeStatus;
    }

    public void setMergeStatus(String mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String  id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNoteableType() {
        return noteableType;
    }

    public void setNoteableType(String noteableType) {
        this.noteableType = noteableType;
    }

    public LastCommit getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(LastCommit lastCommit) {
        this.lastCommit = lastCommit;
    }

    public String getNoteableId() {
        return noteableId;
    }

    public void setNoteableId(String noteableId) {
        this.noteableId = noteableId;
    }

    public String getHeadPipelineId() {
        return headPipelineId;
    }

    public void setHeadPipelineId(String headPipelineId) {
        this.headPipelineId = headPipelineId;
    }

    public Long getIid() {
        return iid;
    }

    public void setIid(Long iid) {
        this.iid = iid;
    }

    public String getDetailedMergeStatus() {
        return detailedMergeStatus;
    }

    public void setDetailedMergeStatus(String detailedMergeStatus) {
        this.detailedMergeStatus = detailedMergeStatus;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
