/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
/**
 * AssetBean is the Bean that is used to represent the event json. It is the top level java class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventBean {

    @JsonProperty("Id")
    private String qualifiedName;
    @JsonProperty("Name")
    private String technicalName;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("Team")
    private String team;
    @JsonProperty("Input")
    private List<AssetBean> input = new ArrayList<AssetBean>();
    @JsonProperty("Output")
    private List<AssetBean> output = new ArrayList<AssetBean>();

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public List<AssetBean> getInput() {
        return input;
    }

    public void setInput(List<AssetBean> input) {
        this.input = input;
    }

    public List<AssetBean> getOutput() {
        return output;
    }

    public void setOutput(List<AssetBean> output) {
        this.output = output;
    }
}
