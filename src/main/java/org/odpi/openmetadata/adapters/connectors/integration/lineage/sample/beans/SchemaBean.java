/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
/**
 * SchemaBean is the Bean that is used to represent the schema part of the event json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaBean {
    @JsonProperty("title")
    private String displayName;
    @JsonProperty("type")
    private String type;

    Map<String,Map<String,Object>> properties;
    /**
     * Returns the stored display name property for the schema.
     * If no display name is available then null is returned.
     *
     * @return String name
     */
    public String getDisplayName() {
        return displayName;
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

    /**
     * Get the properties for this Schema
     * @return the properties
     */

    public Map<String, Map<String, Object>> getProperties() {
        return properties;
    }

    /**
     * Set the properties for this Schema (the attributes)
     * @param properties properties associated with the schema
     */
    public void setProperties(Map<String, Map<String, Object>> properties) {
        this.properties = properties;
    }
}
