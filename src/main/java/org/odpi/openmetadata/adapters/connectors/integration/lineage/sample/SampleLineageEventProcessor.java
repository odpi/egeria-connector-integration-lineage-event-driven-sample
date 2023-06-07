/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample;

import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.DataAssetElement;
import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.DataFlowElement;
import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.ProcessElement;
import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.SchemaTypeElement;
import org.odpi.openmetadata.accessservices.assetmanager.properties.*;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.ffdc.LineageEventSampleEventConnectorAuditCode;
import org.odpi.openmetadata.frameworks.auditlog.AuditLog;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.integrationservices.lineage.connector.LineageIntegratorContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * This class processes an event. The code here has been extracted from the integration connector, so it is easier to unit test.
 * The event payload is a proprietary format. This sample shows that it is possible to process events containing lineage information
 * that are not the open lineage format.
 */

public class SampleLineageEventProcessor {

    public static final String EVENT_SCHEMA_ATTRIBUTE = "EventSchemaAttribute";
    public static final String PRIMITIVE_SCHEMA_TYPE = "PrimitiveSchemaType";
    private final AuditLog auditLog;
    private final String connectorName;
    private final boolean assetManagerIsHome = true;
    private LineageIntegratorContext myContext;
    private List<String> inAssetGUIDs = null;
    private List<String> outAssetGUIDs = null;


    /**
     * Constructor for SampleLineageEventProcessor
     *
     * @param myContext     LineageIntegratorContext on which we communicate with the Egeria eco-system.
     * @param auditLog      audit log
     * @param connectorName connector name
     */
    public SampleLineageEventProcessor(LineageIntegratorContext myContext, AuditLog auditLog, String connectorName) {
        this.myContext = myContext;
        this.auditLog = auditLog;
        this.connectorName = connectorName;
    }

    private static SchemaAttributeProperties getSchemaAttributeProperties(LineageEventContentforSample.Attribute attribute) {
        String attributeQualifiedName = attribute.getQualifiedName();
        String attributeDisplayName = attribute.getDisplayName();
        SchemaAttributeProperties schemaAttributeProperties = new SchemaAttributeProperties();
        schemaAttributeProperties.setQualifiedName(attributeQualifiedName);
        schemaAttributeProperties.setDisplayName(attributeDisplayName);
        schemaAttributeProperties.setTypeName(EVENT_SCHEMA_ATTRIBUTE);
        schemaAttributeProperties.setDescription(attribute.getDescription());
        PrimitiveSchemaTypeProperties primitiveSchemaTypeProperties = new PrimitiveSchemaTypeProperties();
        primitiveSchemaTypeProperties.setQualifiedName(attributeQualifiedName);
        primitiveSchemaTypeProperties.setDisplayName(attributeDisplayName);
        primitiveSchemaTypeProperties.setDataType(attribute.getType());
        primitiveSchemaTypeProperties.setFormula(attribute.getFormula());
        primitiveSchemaTypeProperties.setTypeName(PRIMITIVE_SCHEMA_TYPE);
        schemaAttributeProperties.setSchemaType(primitiveSchemaTypeProperties);
        return schemaAttributeProperties;
    }

    /**
     * Process the event.
     *
     * @param eventContent event content to process
     */
    public void processEvent(LineageEventContentforSample eventContent) {
        String methodName = "processEvent";
        try {
            // upsert in assets
            inAssetGUIDs = upsertAssets(eventContent.getInputAssets());
            // upsert out assets
            outAssetGUIDs = upsertAssets(eventContent.getOutputAssets());
            saveLineage(eventContent);

        } catch (InvalidParameterException error) {
            if (auditLog != null) {
                auditLog.logMessage(methodName,
                        LineageEventSampleEventConnectorAuditCode.INVALID_PARAMETER_EXCEPTION.getMessageDefinition(
                                error.getClass().getName(),
                                connectorName,
                                error.getMessage()));
            }
        } catch (PropertyServerException error) {
            if (auditLog != null) {
                auditLog.logMessage(methodName,
                        LineageEventSampleEventConnectorAuditCode.PROPERTY_SERVER_EXCEPTION.getMessageDefinition(
                                error.getClass().getName(),
                                connectorName,
                                error.getMessage()));
            }
        } catch (UserNotAuthorizedException error) {
            if (auditLog != null) {
                auditLog.logMessage(methodName,
                        LineageEventSampleEventConnectorAuditCode.USER_NOT_AUTHORISED_EXCEPTION.getMessageDefinition(
                                error.getClass().getName(),
                                connectorName,
                                error.getMessage()));
            }
        } catch (Exception error) {
            if (auditLog != null) {
                auditLog.logException(methodName,
                        LineageEventSampleEventConnectorAuditCode.UNEXPECTED_EXCEPTION.getMessageDefinition(
                                error.getClass().getName(),
                                connectorName,
                                error.getMessage()), error);
            }
        }
    }

    /**
     * The AssetFromJSON represents the asset as specified in the json. A list of these are supplied to be put into the
     * metadata repository. The method does na upsert, i.e. updates if the asset already exists otherwise inserts.
     * <p>
     * Because we look the asset up by name we can get more than one returned to us. This code assumes the first one is the
     * relevant one,
     *
     * @param jsonAssets json assets
     * @return a list of qualified names of the processed assets
     * @throws InvalidParameterException  invalid parameter exception
     * @throws UserNotAuthorizedException user is not authorised
     * @throws PropertyServerException    property server Exception
     */
    @SuppressWarnings("JavaUtilDate")
    public List<String> upsertAssets(List<LineageEventContentforSample.AssetFromJSON> jsonAssets) throws InvalidParameterException, UserNotAuthorizedException, PropertyServerException {
        String methodName = "upsertAssets";
        List<String> assetGUIDs = new ArrayList<>();
        for (LineageEventContentforSample.AssetFromJSON jsonAsset : jsonAssets) {
            String assetQualifiedName = jsonAsset.getQualifiedName();
            String assetGUID = null;
            List<DataAssetElement> dataAssetElements = myContext.getDataAssetsByName(assetQualifiedName, 0, 1000, null);
            DataAssetProperties assetProperties = new DataAssetProperties();
            assetProperties.setTypeName(jsonAsset.getTypeName());
            assetProperties.setQualifiedName(assetQualifiedName);
            assetProperties.setTechnicalName(jsonAsset.getDisplayName());
            if (dataAssetElements == null || dataAssetElements.isEmpty()) {
                // create asset
                try {
                    assetGUID = myContext.createDataAsset(assetManagerIsHome, assetProperties);
                } catch (InvalidParameterException error) {
                    if (error.getReportedHTTPCode() == 409 &&
                            error.getParameterName().equals("qualifiedName") &&
                            error.getReportedErrorMessageId().equals("OMAG-COMMON-409-001")
                    ) {
                        // qualifiedName already exists and is not a Data Asset.
                        if (auditLog != null) {
                            auditLog.logMessage(methodName, LineageEventSampleEventConnectorAuditCode.CREATE_ASSET_ATTEMPTED_WITH_EXISTING_QUALIFIEDNAME.getMessageDefinition(assetQualifiedName));
                        }
                        throw error;
                    }
                }
            } else {
                // asset already exists - update it
                DataAssetElement dataAssetElement = dataAssetElements.get(0);
                if (dataAssetElement.getElementHeader() != null) {
                    assetGUID = dataAssetElement.getElementHeader().getGUID();
                    try {
                        myContext.updateDataAsset(assetGUID, assetManagerIsHome, assetProperties, new Date());
                    } catch (UserNotAuthorizedException error) {
                        if (error.getReportedErrorMessageId().equals("OMAG-REPOSITORY-HANDLER-400-007")) {
                            // cannot update this asset as it is already owned by another metadata collection
                            // log and carry on processing
                            if (auditLog != null) {
                                String[] msgParams = error.getReportedErrorMessageParameters();
                                auditLog.logMessage(methodName,
                                        LineageEventSampleEventConnectorAuditCode.UPDATE_ASSET_FAILED_OWNED_BY_DIFFERENT_EXTERNAL_SOURCE.getMessageDefinition(
                                                methodName,
                                                msgParams[1],
                                                msgParams[2],
                                                msgParams[3],
                                                msgParams[4],
                                                msgParams[5],
                                                msgParams[6],
                                                msgParams[7]
                                        ));
                            }
                        }
                    }
                }
            }
            assetGUIDs.add(assetGUID);
            List<LineageEventContentforSample.EventTypeFromJSON> eventTypes = jsonAsset.getEventTypes();
            if (eventTypes != null && eventTypes.size() > 0) {
                ensureSchemaIsCatalogued(jsonAsset, assetGUID);
            }

        }
        return assetGUIDs;
    }

    /**
     * This code ensure that the schema associated with the supplied asset is appropriately catalogued.
     * The asset that is supplied is expected to be a Kafka topic and the schema is mapped
     * to event schema entities.
     * <p>
     * This method does create, update delete on EventType (the schema type) and its schema attributes.
     * Deletion of the Event type is assumed to take out any schema attributes under it.
     *
     * @param assetFromJSON - the asset from the json
     * @param assetGUID     - asset GUID
     * @throws InvalidParameterException  invalid parameter exception
     * @throws UserNotAuthorizedException user is not authorised
     * @throws PropertyServerException    property server Exception
     */

    private void ensureSchemaIsCatalogued(LineageEventContentforSample.AssetFromJSON assetFromJSON, String assetGUID) throws InvalidParameterException, PropertyServerException, UserNotAuthorizedException {
        SchemaTypeElement schemaTypeForElement = myContext.getSchemaTypeForElement(assetGUID, assetFromJSON.getTypeName(), null);
        if (schemaTypeForElement != null) {
            myContext.removeSchemaType(schemaTypeForElement.getElementHeader().getGUID(), null);
        }
        SchemaTypeChoiceProperties schemaTypeChoiceProperties = new SchemaTypeChoiceProperties();
        schemaTypeChoiceProperties.setQualifiedName(assetFromJSON.getQualifiedName() + "-EventTypes");
        schemaTypeChoiceProperties.setDisplayName(assetFromJSON.getQualifiedName() + "-EventTypes");
        schemaTypeChoiceProperties.setTypeName("EventTypeList");
        String schemaTypeChoiceGUID = myContext.createSchemaType(assetManagerIsHome, schemaTypeChoiceProperties);
        for (LineageEventContentforSample.EventTypeFromJSON eventTypeFromJSON : assetFromJSON.getEventTypes()) {
            SchemaTypeProperties schemaTypeProperties = new SchemaTypeProperties();
            schemaTypeProperties.setTypeName("EventType");
            schemaTypeProperties.setQualifiedName(eventTypeFromJSON.getQualifiedName());
            schemaTypeProperties.setDisplayName(eventTypeFromJSON.getTechnicalName());
            String schemaTypeGUID = myContext.createSchemaType(assetManagerIsHome, schemaTypeProperties);
            myContext.setupSchemaElementRelationship(assetManagerIsHome, schemaTypeGUID, schemaTypeChoiceGUID, "SchemaTypeOption", null, null);
            for (LineageEventContentforSample.Attribute attribute : eventTypeFromJSON.getAttributes()) {
                createPrimitiveSchemaAttribute(schemaTypeGUID, attribute);
            }
        }
        myContext.setupSchemaTypeParent(assetManagerIsHome, schemaTypeChoiceGUID, assetGUID, "KafkaTopic", null, null);
    }

    /**
     * This method maps the event attribute and issues the createSchemaAttribute on the context.
     *
     * @param schemaTypeGUID parent schema type guid
     * @param attribute      attribute
     * @throws InvalidParameterException  Invalid parameter
     * @throws UserNotAuthorizedException user not authorised
     * @throws PropertyServerException    property server exception
     */
    private void createPrimitiveSchemaAttribute(String schemaTypeGUID, LineageEventContentforSample.Attribute attribute) throws InvalidParameterException, UserNotAuthorizedException, PropertyServerException {
        SchemaAttributeProperties schemaAttributeProperties = getSchemaAttributeProperties(attribute);
        String schemaAttribute = myContext.createSchemaAttribute(assetManagerIsHome, schemaTypeGUID, schemaAttributeProperties, null);
        if (isObjectTypeWithNestedAttributes(attribute)) {
            for (LineageEventContentforSample.Attribute nestedAttribute : attribute.getNestedAttributes()) {
                createPrimitiveSchemaAttribute(schemaAttribute, nestedAttribute);
            }
        }
    }

    private boolean isObjectTypeWithNestedAttributes(LineageEventContentforSample.Attribute attribute) {
        return Objects.equals(attribute.getType(), "object") && !attribute.getNestedAttributes().isEmpty();
    }

    /**
     * Save the lineage. The input and assets will have been catalogued prior to this method.
     * <p>
     * This method creates a process entity then knits it to the input and output assets.
     * <p>
     * This is creating asset level lineage not column level.
     * <p>
     * The relationship between the input asset and the process is a DataFlow relationship which contains
     * the formula, which is the SQL.
     *
     * @param eventContent - representation of the event as a java object.
     * @throws InvalidParameterException  invalid parameter exception
     * @throws UserNotAuthorizedException user is not authorised
     * @throws PropertyServerException    property server Exception
     */
    private void saveLineage(LineageEventContentforSample eventContent) throws InvalidParameterException, PropertyServerException, UserNotAuthorizedException {
        String processQualifiedName = eventContent.getProcessQualifiedName();
        String processGUID;
        List<ProcessElement> processes = myContext.getProcessesByName(processQualifiedName, 0, 0, null);
        ProcessProperties processProperties = new ProcessProperties();
        processProperties.setQualifiedName(processQualifiedName);
        processProperties.setTechnicalName(eventContent.getProcessTechnicalName());
        processProperties.setTechnicalDescription(eventContent.getProcessDescription());
        // does this process already exist?
        if (processes == null || processes.isEmpty()) {
            // process does not exist
            processGUID = myContext.createProcess(assetManagerIsHome, ProcessStatus.ACTIVE, processProperties);
        } else {
            // process exists update it
            ProcessElement processElement = processes.get(0);
            processGUID = processElement.getElementHeader().getGUID();
            myContext.updateProcess(processGUID, false, processProperties, null);
        }
        for (String assetGUID : inAssetGUIDs) {
            DataFlowProperties properties = new DataFlowProperties();
            DataAssetElement dataAssetElement = myContext.getDataAssetByGUID(assetGUID, null);
            String typeValue = eventContent.getFormulaForInputAsset(dataAssetElement.getDataAssetProperties().getQualifiedName());
            if (typeValue != null) {
                properties.setFormula(typeValue);
            }
            if (dataAssetElement.getDataAssetProperties().getQualifiedName() != null) {
                properties.setQualifiedName(dataAssetElement.getDataAssetProperties().getQualifiedName());
            }
            // if there is already a dataflow - update it, if not create it
            DataFlowElement existingDataflow = myContext.getDataFlow(assetGUID, processGUID, null, null);
            if (existingDataflow == null) {
                myContext.setupDataFlow(assetManagerIsHome, assetGUID, processGUID, properties, null);

            } else {
                myContext.updateDataFlow(existingDataflow.getDataFlowHeader().getGUID(), properties, null);
            }
        }
        for (String assetGUID : outAssetGUIDs) {
            DataFlowProperties properties = new DataFlowProperties();
            DataFlowElement existingDataflow = myContext.getDataFlow(processGUID, assetGUID, null, null);
            if (existingDataflow == null) {
                myContext.setupDataFlow(assetManagerIsHome, processGUID, assetGUID, properties, null);
            } else {
                myContext.updateDataFlow(existingDataflow.getDataFlowHeader().getGUID(), properties, null);
            }
        }
    }
}
