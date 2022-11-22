/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * The EventContent is a java representation of the event json. It means that the connector can
 * retrieve elements like assets without needing to parse the json.
 */
public class EventContent {

    private String teamName;
    private String processDisplayName = null;
    private String processQualifiedName = null;

    private List<AssetFromJSON> inputAssets = new ArrayList<>();

    private List<AssetFromJSON> outputAssets = new ArrayList<>();

    private Map<String, String> inputAssetSQLMap = new HashMap<>();

    static final String SEPARATOR = "~";

    protected EventContent(String jsonString) {
       // String methodName = "EventContent";
        // process json
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = null;
        try {
            root = mapper.readTree(jsonString);
        } catch (JsonProcessingException error) {
            throw new RuntimeException(error);
            //todo
//            throw new ConnectorCheckedException(StrimziIntegrationConnectorErrorCode.ERROR_PARSING_REST_RESPONSE.getMessageDefinition(connectorName,
//                    targetURL, error.getClass().getName(), error.getMessage()),
//                    this.getClass().getName(),
//                    methodName,
//                    error);
        }

        JsonNode inputNodes = root.path("Input");
        JsonNode outputNodes = root.path("Output");

        this.processDisplayName = root.path("Name").textValue();
        this.processQualifiedName = root.path("Id").textValue();
        this.teamName = root.path("Team").textValue();
        if (inputNodes.isArray()) {
            inputNodes.size();
            for (int i = 0; i < inputNodes.size(); i++) {
                JsonNode inputNode = inputNodes.get(i);
                if (inputNode.isObject()) {
                    String qualifiedName = inputNode.path("Id").textValue();
                    String displayName = inputNode.path("Name").textValue();
                    String sql = inputNode.path("SQL").textValue();
                    if (sql != null) {
                        inputAssetSQLMap.put(qualifiedName,sql);
                    }
                    AssetFromJSON assetFromJSON = new AssetFromJSON(displayName, qualifiedName, "DataSet");

                    inputAssets.add(assetFromJSON);
                } else {
                    //error
                }
            }
        }
        if (outputNodes.isArray()) {
            outputNodes.size();
            for (int i = 0; i < outputNodes.size(); i++) {
                JsonNode outputNode = outputNodes.get(i);
                if (outputNode.isObject()) {
                    String assetQualifiedName = outputNode.path("Id").textValue();
                    String assetDisplayName = outputNode.path("Name").textValue();
                    JsonNode schemaNode = outputNode.path("Schema");

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
                            String attributeQualifiedName =outputEventTypeQualifiedName + SEPARATOR + propertyName;
                            Attribute attribute = new Attribute(propertyName, attributeQualifiedName, type, description);
                            outputAttributes.add(attribute);
                        }
                    }
                    EventTypeFromJSON eventType = new EventTypeFromJSON(outputEventTypeDisplayName, outputEventTypeQualifiedName, outputAttributes);
                    AssetFromJSON outputAsset = new AssetFromJSON(assetDisplayName, assetQualifiedName, "KafkaTopic", eventType);
                    outputAssets.add(outputAsset);
                } else {
                    //error
                }
            }
        } else {
            // error not array
        }
    }
     //getters and setters
    public String getProcessDisplayName() {
        return processDisplayName;
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

    class Attribute {
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
