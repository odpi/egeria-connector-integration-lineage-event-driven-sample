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
    private String qualifiedNamePrefix = "";
    private String qualifiedNameSeparator = ".";

    /**
     * The prefix for the qualifiedName. Will be prepended when querying the qualifiedName.
     *
     * @return String the prefix for the qualifiedName
     */
    public String getQualifiedNamePrefix() {
        return qualifiedNamePrefix;
    }

    /**
     * Set the prefix for the qualifiedName. This will be prepended when querying the qualifiedName.
     *
     * @param qualifiedNamePrefix the prefix for the qualifiedName
     */
    public void setQualifiedNamePrefix(String qualifiedNamePrefix) {
        this.qualifiedNamePrefix = qualifiedNamePrefix;
    }

    /**
     * The separator between the qualifiedName prefix and the qualifiedName.
     *
     * @return String separator
     */
    public String getQualifiedNameSeparator() {
        return qualifiedNameSeparator;
    }

    /**
     * Set the separator used between the qualifiedName and its prefix.
     *
     * @param qualifiedNameSeparator the separator
     */
    public void setQualifiedNameSeparator(String qualifiedNameSeparator) {
        this.qualifiedNameSeparator = qualifiedNameSeparator;
    }


    /**
     * Returns the stored qualified name property for the asset.
     * If no qualified name is available then the empty string is returned.
     *
     * @return string qualifiedName
     */
    public String getQualifiedName() {
        return (this.qualifiedNamePrefix.equals( "" ) ) ? qualifiedName : qualifiedNamePrefix.concat(qualifiedNameSeparator).concat(qualifiedName);
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
