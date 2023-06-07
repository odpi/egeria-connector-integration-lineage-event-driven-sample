/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.beans.AssetBean;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.beans.EventBean;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.beans.SchemaBean;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.ffdc.LineageEventSampleConnectorErrorCode;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;

import java.util.*;

/**
 * The EventContent is a java representation of the event json. It means that the connector can
 * retrieve elements like assets without needing to handle the json. It uses the beans to deserialise
 * the json.
 */
public class LineageEventContentforSample {

    private String teamName;
    private String processTechnicalName = null;
    private String processQualifiedName = null;

    private String processDescription = null;

    private List<AssetFromJSON> inputAssets = new ArrayList<>();

    private List<AssetFromJSON> outputAssets = new ArrayList<>();

    private Map<String, String> inputAssetFormulaMap = new HashMap<>();

    private String topicNamespace;

    static final String SEPARATOR = "~";
    static final String TOPIC_SEPARATOR = ".";

    protected LineageEventContentforSample(String jsonString, String connectorName, String topicNamespace) throws ConnectorCheckedException {
        String methodName = "LineageEventContentforSample -constructor";

        this.topicNamespace = topicNamespace;

        // process json

        ObjectMapper om = new ObjectMapper();
        EventBean eventBean = null;
        try {

            eventBean = om.readValue(jsonString, EventBean.class);

        } catch (JsonProcessingException error) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_JSON.getMessageDefinition(connectorName,
                    jsonString
                    , error.getClass().getName(), error.getMessage()),
                    this.getClass().getName(),
                    methodName,
                    error);
        }

        this.processQualifiedName = eventBean.getQualifiedName();
        if (this.processQualifiedName == null || this.processQualifiedName.length() == 0) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_NO_PROCESS_ID.getMessageDefinition(connectorName,
                    jsonString),
                    this.getClass().getName(),
                    methodName);
        }
        this.processTechnicalName = eventBean.getTechnicalName();
        if (this.processTechnicalName == null || this.processTechnicalName.length() == 0) {
            this.processTechnicalName = this.processQualifiedName;
        }
        this.processDescription = eventBean.getDescription();
        this.teamName = eventBean.getTeam();
        List<AssetBean> inputAssetBeans = eventBean.getInput();
        if (inputAssetBeans == null || inputAssetBeans.size() == 0) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_NO_INPUT.getMessageDefinition(connectorName,
                    jsonString),
                    this.getClass().getName(),
                    methodName);
        }
        for (AssetBean inputAssetBean : inputAssetBeans) {
            String qualifiedName = inputAssetBean.getQualifiedName();
            if (qualifiedName == null || qualifiedName.length() == 0) {
                throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_INPUT_ASSET_HAS_NO_ID.getMessageDefinition(connectorName,
                        jsonString),
                        this.getClass().getName(),
                        methodName);
            }
            String displayName = inputAssetBean.getName();

            String type = inputAssetBean.getType();
            String value = inputAssetBean.getValue();
            if (type != null && value != null) {
                inputAssetFormulaMap.put(qualifiedName, type + " - " + value);
            }
            AssetFromJSON assetFromJSON = new AssetFromJSON(displayName, qualifiedName, "DataSet");

            inputAssets.add(assetFromJSON);
        }

        List<AssetBean> outputAssetBeans = eventBean.getOutput();
        if (outputAssetBeans == null || outputAssetBeans.size() == 0) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_NO_OUTPUT.getMessageDefinition(connectorName,
                    jsonString),
                    this.getClass().getName(),
                    methodName);
        }
        for (AssetBean outputAssetBean : outputAssetBeans) {
            String qualifiedName = outputAssetBean.getQualifiedName();
            if (qualifiedName == null || qualifiedName.length() == 0) {
                throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_INPUT_ASSET_HAS_NO_ID.getMessageDefinition(connectorName,
                        jsonString),
                        this.getClass().getName(),
                        methodName);
            }
            if (topicNamespace != null && topicNamespace.length() != 0) {
                qualifiedName = topicNamespace.concat(TOPIC_SEPARATOR).concat(qualifiedName);
            }
            String displayName = outputAssetBean.getName();
            List<SchemaBean> schemaBeans = outputAssetBean.getSchemas();

            List<EventTypeFromJSON> eventTypesFromJSON = new ArrayList<>();
            for (SchemaBean schemaBean : schemaBeans) {
                String outputEventTypeDisplayName = schemaBean.getDisplayName();
                String outputEventTypeQualifiedName = qualifiedName + SEPARATOR + outputEventTypeDisplayName;
                List<Attribute> outputAttributes = getAttributes(schemaBean.getProperties(), null, outputEventTypeQualifiedName);
                EventTypeFromJSON eventTypeFromJSON = new EventTypeFromJSON(outputEventTypeDisplayName, outputEventTypeQualifiedName, outputAttributes);
                eventTypesFromJSON.add(eventTypeFromJSON);
            }

            AssetFromJSON assetFromJSON = new AssetFromJSON(displayName, qualifiedName, "KafkaTopic", eventTypesFromJSON);

            outputAssets.add(assetFromJSON);
        }
    }

    private List<Attribute> getAttributes(JsonNode properties, String parentDisplayName, String outputEventTypeQualifiedName) {
        List<Attribute> outputAttributes = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> nodes = properties.fields();
        while (nodes.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodes.next();
            String attributeDisplayName = entry.getKey();
            JsonNode attributeNode = entry.getValue();
            //assume key can't be null.
            String attributeQualifiedName;
            if (parentDisplayName == null) {
                attributeQualifiedName = outputEventTypeQualifiedName + SEPARATOR + attributeDisplayName;
            } else {
                attributeQualifiedName = outputEventTypeQualifiedName + SEPARATOR + parentDisplayName + SEPARATOR + attributeDisplayName;
            }
            String attributeType = null;
            List<Attribute> nestedAttributes = null;
            if (attributeNode.has("type")) {
                JsonNode attributeTypeNode = attributeNode.get("type");
                attributeType = attributeTypeNode.asText();
                if ("object".equals(attributeType) &&
                        attributeNode.has("properties")) {
                    nestedAttributes = getAttributes(attributeNode.get("properties"), attributeDisplayName, outputEventTypeQualifiedName);
                }
            }
            String attributeDescription = null;
            if (attributeNode.has("description")) {
                JsonNode attributeDescriptionObject = attributeNode.get("description");
                attributeDescription = attributeDescriptionObject.asText();

            }
            String attributeFormula = null;
            if (attributeNode.has("formula")) {
                JsonNode attributeFormulaObject = attributeNode.get("formula");
                attributeFormula = attributeFormulaObject.asText();

            }
            Attribute outputAttribute = new Attribute(attributeDisplayName, attributeQualifiedName, attributeType, attributeDescription, attributeFormula);
            if (nestedAttributes != null) {
                outputAttribute.setNestedAttributes(nestedAttributes);
            }
            outputAttributes.add(outputAttribute);
        }
        return outputAttributes;
    }

    //getters and setters
    public String getProcessTechnicalName() {
        return processTechnicalName;
    }

    public String getProcessDescription() {
        return processDescription;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getProcessQualifiedName() {
        return processQualifiedName;
    }

    public List<AssetFromJSON> getInputAssets() {
        return inputAssets;
    }

    public List<AssetFromJSON> getOutputAssets() {
        return outputAssets;
    }

    public String getFormulaForInputAsset(String qualifiedName) {
        return inputAssetFormulaMap.get(qualifiedName);
    }

    static class AssetFromJSON {
        private String displayName;
        private String qualifiedName;
        private String typeName;

        private List<EventTypeFromJSON> eventTypes;

        protected AssetFromJSON(String displayName, String qualifiedName, String typeName) {
            this.displayName = displayName;
            this.qualifiedName = qualifiedName;
            this.typeName = typeName;
        }

        protected AssetFromJSON(String displayName, String qualifiedName, String typeName, List<EventTypeFromJSON> eventTypes) {
            this(displayName, qualifiedName, typeName);
            this.eventTypes = eventTypes;
        }


        public String getDisplayName() {
            return displayName;
        }

        public String getQualifiedName() {
            return qualifiedName;
        }

        public String getTypeName() {
            return typeName;
        }

        public List<EventTypeFromJSON> getEventTypes() {
            return eventTypes;
        }
    }

    static class Attribute {
        private final String displayName;
        private final String qualifiedName;
        private final String type;
        private final String description;
        private final String formula;
        private List<Attribute> nestedAttributes;


        protected Attribute(String displayName, String qualifiedName, String type, String description) {
            this(displayName, qualifiedName, type, description, null);
        }

        protected Attribute(String displayName, String qualifiedName, String type, String description, String formula) {
            this.displayName = displayName;
            this.description = description;
            this.type = type;
            this.qualifiedName = qualifiedName;
            this.formula = formula;
            this.nestedAttributes = new ArrayList<>();
        }


        protected String getDisplayName() {
            return displayName;
        }

        public String getQualifiedName() {
            return qualifiedName;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getFormula() {
            return formula;
        }

        public List<Attribute> getNestedAttributes() {
            return nestedAttributes;
        }

        public void setNestedAttributes(List<Attribute> nestedAttributes) {
            this.nestedAttributes = nestedAttributes;
        }
    }

    static class EventTypeFromJSON {
        private String technicalName;
        private String qualifiedName;
        private List<Attribute> attributes = null;

        protected EventTypeFromJSON(String technicalName, String qualifiedName, List<Attribute> attributes) {
            this.technicalName = technicalName;
            this.qualifiedName = qualifiedName;
            this.attributes = attributes;
        }

        public String getTechnicalName() {
            return technicalName;
        }

        public String getQualifiedName() {
            return qualifiedName;
        }

        public List<Attribute> getAttributes() {
            return attributes;
        }
    }

}
