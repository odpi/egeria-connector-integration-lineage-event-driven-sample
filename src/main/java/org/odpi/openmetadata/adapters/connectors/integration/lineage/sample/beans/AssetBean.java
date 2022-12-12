/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AssetBean is the Bean that is used to represent the asset part of the event json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetBean {

    @JsonProperty("Id")
    private String qualifiedName;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("SQL")
    private String formula;
    // TODO change this to an array when we want to support multiple top level EventTypes
    @JsonProperty("Schemas")
    private List<SchemaBean> schemas;

    /**
     * Returns the stored qualified name property for the asset.
     * If no qualified name is available then the empty string is returned.
     *
     * @return string qualifiedName
     */
    public String getQualifiedName() {
        return qualifiedName;
    }
    /**
     * Returns the name property for the asset.
     * If no name is available then the empty string is returned.
     *
     * @return string name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * Return the formula used against the asset.
     *
     * @return String formula
     */
    public String getFormula() {
        return formula;
    }
    public List<SchemaBean> getSchemas() {
        return schemas;
    }
}
