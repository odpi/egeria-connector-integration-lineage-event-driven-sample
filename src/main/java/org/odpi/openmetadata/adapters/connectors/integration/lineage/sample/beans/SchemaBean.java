/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * SchemaBean is the Bean that is used to represent the schema part of the event json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaBean {
    @JsonProperty("title")
    private String displayName;
    @JsonProperty("type")
    private String type;

    private JsonNode properties;

    /**
     * Returns the stored display name property for the schema.
     * If no display name is available then null is returned.
     *
     * @return String name
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the stored open metadata type name for this schema type.
     *
     * @return String name
     */
    public String getType() {
        return type;
    }

    /**
     *  Set the stored open metadata type name for this schema type
     * @param type type name
     */

    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getProperties() {
        return properties;
    }

    public void setProperties(JsonNode properties) {
        this.properties = properties;
    }
}
