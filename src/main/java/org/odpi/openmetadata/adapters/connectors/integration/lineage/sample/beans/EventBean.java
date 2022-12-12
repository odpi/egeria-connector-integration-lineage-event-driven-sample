/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.beans;

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
    /**
     * Returns the stored qualified name property for the metadata entity.
     * If no qualified name is available then the empty string is returned.
     *
     * @return string qualifiedName
     */
    public String getQualifiedName() {
        return qualifiedName;
    }
    /**
     * Returns the stored technical name property for the event.
     * If no technical name is available then null is returned.
     *
     * @return String name
     */
    public String getTechnicalName() {
        return technicalName;
    }
    /**
     * Return the description of the event.
     *
     * @return string description
     */
    public String getDescription() {
        return description;
    }
    /**
     * Return the team associated with the event.
     *
     * @return string team name
     */
    public String getTeam() {
        return team;
    }

    /**
     * Return a list of input asset beans
     * @return a list of AssetBeans
     */
    public List<AssetBean> getInput() {
        return input;
    }
    /**
     * Set a list of input asset beans
     * @param input list of AssetBeans
     */
    public void setInput(List<AssetBean> input) {
        this.input = input;
    }
    /**
     * Return a list of output asset beans
     * @return a list of AssetBeans
     */
    public List<AssetBean> getOutput() {
        return output;
    }
    /**
     * Set a list of output asset beans
     * @param output list of AssetBeans
     */
    public void setOutput(List<AssetBean> output) {
        this.output = output;
    }
}
