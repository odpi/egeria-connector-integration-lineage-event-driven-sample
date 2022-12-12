/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.beans.AssetBean;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.beans.EventBean;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.beans.SchemaBean;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.ffdc.LineageEventSampleConnectorErrorCode;
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

    static final String SEPARATOR = "~";

    protected LineageEventContentforSample(String jsonString, String connectorName) throws ConnectorCheckedException {
        String methodName = "LineageEventContentforSample -constructor";

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
            this.processTechnicalName =this.processQualifiedName;
        }
        this.processDescription = eventBean.getDescription();
        this.teamName = eventBean.getTeam();
        List<AssetBean> inputAssetBeans =  eventBean.getInput();
        if (inputAssetBeans == null || inputAssetBeans.size() ==0) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_NO_INPUT.getMessageDefinition(connectorName,
                    jsonString),
                    this.getClass().getName(),
                    methodName);
        }
        for (AssetBean inputAssetBean:inputAssetBeans) {
            String qualifiedName = inputAssetBean.getQualifiedName();
            if (qualifiedName == null || qualifiedName.length() == 0) {
                throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_INPUT_ASSET_HAS_NO_ID.getMessageDefinition(connectorName,
                        jsonString),
                        this.getClass().getName(),
                        methodName);
            }
            String displayName = inputAssetBean.getName();

            String formula = inputAssetBean.getFormula();
            if (formula != null) {
                inputAssetFormulaMap.put(qualifiedName,formula);
            }
            AssetFromJSON assetFromJSON = new AssetFromJSON(displayName, qualifiedName, "DataSet");

            inputAssets.add(assetFromJSON);
        }

        List<AssetBean> outputAssetBeans =  eventBean.getOutput();
        if (outputAssetBeans == null || outputAssetBeans.size() ==0) {
            throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_NO_OUTPUT.getMessageDefinition(connectorName,
                    jsonString),
                    this.getClass().getName(),
                    methodName);
        }
        for (AssetBean outputAssetBean:outputAssetBeans) {
            String qualifiedName = outputAssetBean.getQualifiedName();
            if (qualifiedName == null || qualifiedName.length() == 0) {
                throw new ConnectorCheckedException(LineageEventSampleConnectorErrorCode.INVALID_EVENT_INPUT_ASSET_HAS_NO_ID.getMessageDefinition(connectorName,
                        jsonString),
                        this.getClass().getName(),
                        methodName);
            }
            String displayName = outputAssetBean.getName();
            List<SchemaBean> schemaBeans = outputAssetBean.getSchemas();

            SchemaBean schemaBean = null;
            if (schemaBeans != null && schemaBeans.size() > 0) {
                // TODO hard coding to only 1 EventType - until support for EventTypeLists is present in the context API
                schemaBean = schemaBeans.get(0);
            }
            String outputEventTypeDisplayName =  schemaBean.getDisplayName();
            String outputEventTypeQualifiedName = qualifiedName + SEPARATOR + outputEventTypeDisplayName;

            Map<String, Map<String, Object>> properties = schemaBean.getProperties();
            Set<Map.Entry<String, Map<String, Object>>> propertyEntrySet = properties.entrySet();
            Iterator<Map.Entry<String, Map<String, Object>>> iter = propertyEntrySet.iterator();
            List<Attribute> outputAttributes = new ArrayList<>();
            while (iter.hasNext()) {
                Map.Entry<String, Map<String, Object>> entry = iter.next();
                String attributeDisplayName = entry.getKey();
                //assume key can't be null.
                String attributeQualifiedName = outputEventTypeQualifiedName + SEPARATOR + attributeDisplayName;
                Map attrMap = (Map) entry.getValue();
                Object attributeTypeObject =  attrMap.get("type");
                String attributeType = null;
                if (attributeTypeObject != null) {
                    attributeType = (String)attributeTypeObject;
                }
                Object attributeDescriptionObject = attrMap.get("description");
                String attributeDescription = null;
                if (attributeDescriptionObject != null) {
                    attributeDescription = (String)attributeDescriptionObject;
                }
                Object attributeFormulaObject = attrMap.get("formula");
                String attributeFormula = null;
                if (attributeFormulaObject != null) {
                    attributeFormula = (String)attributeFormulaObject;
                }
                Attribute outputAttribute = new Attribute(attributeDisplayName,attributeQualifiedName, attributeType, attributeDescription, attributeFormula);
                outputAttributes.add(outputAttribute);

            }
            EventTypeFromJSON eventTypeFromJSON = new EventTypeFromJSON(outputEventTypeDisplayName,outputEventTypeQualifiedName,outputAttributes);
            List< EventTypeFromJSON> eventTypesFromJSON = new ArrayList<>();
            eventTypesFromJSON.add(eventTypeFromJSON);

            AssetFromJSON assetFromJSON = new AssetFromJSON(displayName, qualifiedName, "KafkaTopic", eventTypesFromJSON);

            outputAssets.add(assetFromJSON);
        }

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
        private String displayName;
        private String qualifiedName;
        private String type;
        private String description;

        private String formula;

        protected Attribute(String displayName, String qualifiedName, String type, String description) {
            this(displayName, qualifiedName, type, description,null);
        }
        protected Attribute(String displayName, String qualifiedName, String type, String description, String formula) {
            this.displayName = displayName;
            this.description = description;
            this.type = type;
            this.qualifiedName = qualifiedName;
            this.formula = formula;
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
        public String getQualifiedName () {
            return qualifiedName;
        }
        public List<Attribute> getAttributes() {
            return attributes;
        }
    }

}
