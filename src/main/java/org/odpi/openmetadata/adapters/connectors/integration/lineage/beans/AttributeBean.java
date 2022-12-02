/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * The EventContent is a java representation of the event json. It means that the connector can
 * retrieve elements like assets without needing to parse the json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeBean {

    @JsonProperty("Type")
    private String type;
    @JsonProperty("Description")
    private String description;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
