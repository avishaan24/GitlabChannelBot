package com.microsoftTeams.bot.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Project class which is used to store project details from which the event belongs
 */

@SuppressWarnings("unused")
public class Project {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("web_url")
    private String webUrl;

    public Project() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
