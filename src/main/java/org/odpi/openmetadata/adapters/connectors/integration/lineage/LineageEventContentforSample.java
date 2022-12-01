/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.ffdc.LineageEventSampleConnectorErrorCode;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import java.util.*;

/**
 * The EventContent is a java representation of the event json. It means that the connector can
 * retrieve elements like assets without needing to parse the json.
 */
public class LineageEventContentforSample {

    private String teamName;
    private String processDisplayName = null;
    private String processQualifiedName = null;

    private String processDescription = null;

    private List<AssetFromJSON> inputAssets = new ArrayList<>();

    private List<AssetFromJSON> outputAssets = new ArrayList<>();

    private Map<String, String> inputAssetSQLMap = new HashMap<>();

    static final String SEPARATOR = "~";

    protected LineageEventContentforSample(String jsonString, String connectorName) throws ConnectorCheckedException {
        String methodName = "LineageEventContentforSample -constructor";

        // process json
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = null;
        try {
            root = mapper.readTree(jsonString);
        } catch (JsonProcessingException error) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_JSON.getMessageDefinition(connectorName,
                    jsonString
                    , error.getClass().getName(), error.getMessage()),
                    this.getClass().getName(),
                    methodName,
                    error);
        }

        JsonNode inputNodes = root.path("Input");
        if (inputNodes == null || inputNodes.isEmpty()) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_NO_INPUT.getMessageDefinition(connectorName,
                    jsonString),
                    this.getClass().getName(),
                    methodName);
        }
        JsonNode outputNodes = root.path("Output");
        if (outputNodes == null || outputNodes.isEmpty()) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_NO_OUTPUT.getMessageDefinition(connectorName,
                    jsonString),
                    this.getClass().getName(),
                    methodName);
        }

        this.processQualifiedName = root.path("Id").textValue();
        if (this.processQualifiedName == null || this.processQualifiedName.length() == 0) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_NO_PROCESS_ID.getMessageDefinition(connectorName,
                    jsonString),
                    this.getClass().getName(),
                    methodName);
        }
        this.processDisplayName = root.path("Name").textValue();
        if (this.processDisplayName == null || this.processDisplayName.length() == 0) {
            this.processDisplayName =this.processQualifiedName;
        }
        this.processDescription = root.path("Description").textValue();
        this.teamName = root.path("Team").textValue();
        if (inputNodes.isArray()) {
            inputNodes.size();
            for (int i = 0; i < inputNodes.size(); i++) {
                JsonNode inputNode = inputNodes.get(i);
                if (inputNode.isObject()) {
                    String qualifiedName = inputNode.path("Id").textValue();
                    if (qualifiedName == null || qualifiedName.length() == 0) {
                        throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_INPUT_ASSET_HAS_NO_ID.getMessageDefinition(connectorName,
                                jsonString),
                                this.getClass().getName(),
                                methodName);
                    }
                    String displayName = inputNode.path("Name").textValue();

                    String sql = inputNode.path("SQL").textValue();
                    if (sql != null) {
                        inputAssetSQLMap.put(qualifiedName,sql);
                    }
                    AssetFromJSON assetFromJSON = new AssetFromJSON(displayName, qualifiedName, "DataSet");

                    inputAssets.add(assetFromJSON);
                } else {
                    throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_INPUT_NOT_OBJECT.getMessageDefinition(connectorName,
                            jsonString),
                            this.getClass().getName(),
                            methodName);
                }
            }
        }
        //else not required if it is an empty array ot comes though as if there is no element
        if (outputNodes.isArray()) {
            for (int i = 0; i < outputNodes.size(); i++) {
                JsonNode outputNode = outputNodes.get(i);
                if (outputNode.isObject()) {
                    String assetQualifiedName = outputNode.path("Id").textValue();
                    if (assetQualifiedName == null || assetQualifiedName.length() == 0) {
                        throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_INPUT_ASSET_HAS_NO_ID.getMessageDefinition(connectorName,
                                jsonString),
                                this.getClass().getName(),
                                methodName);
                    }
                    String assetDisplayName = outputNode.path("Name").textValue();
                    if (assetDisplayName == null || assetDisplayName.length() == 0) {
                        assetDisplayName =assetQualifiedName;
                    }
                    JsonNode schemaNode = outputNode.path("Schema");
                    AssetFromJSON outputAsset = null;
                    if (schemaNode != null ) {
                        String outputEventTypeDisplayName = schemaNode.path("title").textValue();
                        String outputEventTypeQualifiedName = assetQualifiedName + SEPARATOR + outputEventTypeDisplayName;
                        JsonNode propertiesNode = schemaNode.path("properties");
                        Iterator<String> propertyIterator = propertiesNode.fieldNames();
                        List<Attribute> outputAttributes = new ArrayList<>();
                        while (propertyIterator.hasNext()) {
                            String propertyName = propertyIterator.next();
                            JsonNode propertyNode = propertiesNode.path(propertyName);
                            if (propertyNode.isObject()) {
                                String type = propertyNode.path("type").textValue();
                                String description = propertyNode.path("description").textValue();
                                String attributeQualifiedName = outputEventTypeQualifiedName + SEPARATOR + propertyName;
                                Attribute attribute = new Attribute(propertyName, attributeQualifiedName, type, description);
                                outputAttributes.add(attribute);
                            }
                        }
                        EventTypeFromJSON eventType = new EventTypeFromJSON(outputEventTypeDisplayName, outputEventTypeQualifiedName, outputAttributes);
                       outputAsset = new AssetFromJSON(assetDisplayName, assetQualifiedName, "KafkaTopic", eventType);
                    } else {
                        // if we do not find a schema , just catalog the assets and process and lineage
                        // TODO warning
                        outputAsset = new AssetFromJSON(assetDisplayName, assetQualifiedName, "KafkaTopic");
                    }
                    outputAssets.add(outputAsset);

                } else {
                    throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_OUTPUT_NOT_OBJECT.getMessageDefinition(connectorName,
                            jsonString),
                            this.getClass().getName(),
                            methodName);
                }
            }
        }
        //else not required if it is an empty array ot comes though as if there is no element

    }
     //getters and setters
    public String getProcessDisplayName() {
        return processDisplayName;
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

    public String getSQLForInputAsset(String qualifiedName) {
        return inputAssetSQLMap.get(qualifiedName);
    }

    static class AssetFromJSON {
        private String displayName;
        private String qualifiedName;
        private String typeName;

        private EventTypeFromJSON eventType;

        protected AssetFromJSON(String displayName, String qualifiedName, String typeName) {
            this.displayName = displayName;
            this.qualifiedName = qualifiedName;
            this.typeName = typeName;
        }
        protected AssetFromJSON(String displayName, String qualifiedName, String typeName, EventTypeFromJSON eventType) {
           this(displayName, qualifiedName, typeName);
           this.eventType = eventType;
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

        public EventTypeFromJSON getEventType() {
            return eventType;
        }
    }

    static class Attribute {
        private String name;

        private String qualifiedName;
        private String type;
        private String description;

        protected Attribute(String name, String qualifiedName, String type, String description) {
            this.name = name;
            this.description =description;
            this.type = type;
            this.qualifiedName = qualifiedName;
        }


        protected String getName() {
            return name;
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

    }

    static class EventTypeFromJSON {
        private String displayName;
        private String qualifiedName;

        private List<Attribute> attributes = null;

        protected EventTypeFromJSON(String displayName, String qualifiedName, List<Attribute> attributes) {
            this.displayName = displayName;
            this.qualifiedName = qualifiedName;
            this.attributes = attributes;
        }


        public String getDisplayName () {
            return displayName;
        }

        public String getQualifiedName () {
            return qualifiedName;
        }

        public List<Attribute> getAttributes() {
            return attributes;
        }
    }

}
