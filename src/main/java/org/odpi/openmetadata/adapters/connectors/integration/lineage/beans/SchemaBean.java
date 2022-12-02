/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaBean {
    @JsonProperty("title")
    private String displayName;
    @JsonProperty("type")
    private String type;
//    Map<String,Object> properties;
//    Map<String,String> properties;  - does not desserialise
//    Map<String,Map<String, Map>> properties;
//    Map<String,Map<String,AttributeBean>> properties;
    Map<String,Map<String,Object>> properties;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Map<String, Object>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Map<String, Object>> properties) {
        this.properties = properties;
    }

}
