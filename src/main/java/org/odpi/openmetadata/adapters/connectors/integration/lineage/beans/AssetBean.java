/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetBean {

    @JsonProperty("Id")
    private String qualifiedName;
    @JsonProperty("Name")
    private String displayName;
    @JsonProperty("SQL")
    private String formula;
    @JsonProperty("Schema")
    private SchemaBean schema;

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public SchemaBean getSchema() {
        return schema;
    }

    public void setSchema(SchemaBean schema) {
        this.schema = schema;
    }
}
