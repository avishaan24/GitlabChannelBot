package com.microsoftTeams.bot.helpers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Pipeline class used to store information related to pipeline from the Gitlab API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
public class Pipeline {

    @JsonProperty("status")
    private String status;

    public Pipeline() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
