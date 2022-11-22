/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage;

import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.DataAssetElement;
import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.SchemaAttributeElement;
import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.SchemaTypeElement;
import org.odpi.openmetadata.accessservices.assetmanager.properties.*;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.integrationservices.lineage.connector.LineageIntegratorContext;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicConnector;

import java.util.*;


@SuppressWarnings("JavaUtilDate")
public class EventProcessor  {

    private LineageIntegratorContext                myContext;
    private  List<String> inAssetGUIDs = null;
    private  List<String> outAssetGUIDs = null;



    public EventProcessor(LineageIntegratorContext  myContext ) {
        this.myContext = myContext;
    }

    public void processEvent(EventContent eventContent)
    {
        try {
            // upsert in assets
            inAssetGUIDs = upsertAssets(eventContent.getInputAssets() );
            // upsert out assets
            outAssetGUIDs = upsertAssets(eventContent.getOutputAssets());
            saveLineage(eventContent);

        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        } catch (PropertyServerException e) {
            throw new RuntimeException(e);
        } catch (UserNotAuthorizedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The AssetFromJSON represents the asset as specified in the json. Alist of these are supplied to be put into the
     * metadata repository. The method does na upsert, i.e. updates if the asset already exists otherwise inserts.
     *
     * Because we look the asset up by name we can get more than one returned to us. This code assumes the first one is the
     * relevant one,
     *
     * @param jsonAssets json assets
     * @return a list of qualified names of the processed assets
     * @throws InvalidParameterException invalid parameter exception
     * @throws UserNotAuthorizedException user is not authorised
     * @throws PropertyServerException property server Exception
     */
    public List<String> upsertAssets(  List<EventContent.AssetFromJSON> jsonAssets) throws InvalidParameterException, UserNotAuthorizedException, PropertyServerException {

        List<String> assetGUIDs = new ArrayList<>();
        for (EventContent.AssetFromJSON jsonAsset:jsonAssets) {
            String assetQualifiedName = jsonAsset.getQualifiedName();
            String assetGUID = null;
            List<DataAssetElement> dataAssetElements = myContext.getDataAssetsByName(assetQualifiedName, 0, 1000, new Date());

            if (dataAssetElements == null || dataAssetElements.isEmpty()) {
                // create asset

                DataAssetProperties assetProperties = new DataAssetProperties();
                assetProperties.setTypeName(jsonAsset.getTypeName());
                assetProperties.setQualifiedName(assetQualifiedName);
                assetProperties.setDisplayName(jsonAsset.getDisplayName());
                myContext.createDataAsset(true, assetProperties);

                dataAssetElements = myContext.getDataAssetsByName(assetQualifiedName, 0, 1000, new Date());
                // choosing the first element -
                // TODO we could put up a warning if there are more than one,
                if (dataAssetElements !=null && !dataAssetElements.isEmpty()) {
                    DataAssetElement dataAssetElement=  dataAssetElements.get(0);
                    if (dataAssetElement !=null && dataAssetElement.getElementHeader() != null) {
                        assetGUID =  dataAssetElement.getElementHeader().getGUID();
                    }
                }
            } else {
                // asset already exists - update it
                DataAssetElement  dataAssetElement = dataAssetElements.get(0);
                String guid =null;
                if ( dataAssetElement.getElementHeader() != null) {
                    guid = dataAssetElement.getElementHeader().getGUID();
                    // TODO check if there is more than one and log warning
                    if (guid == null) {
                        // error
                    } else {
                        String existingDisplayName = dataAssetElement.getDataAssetProperties().getDisplayName();
                        // if the display name has changed update it .
                        if (!existingDisplayName.equals(jsonAsset.getDisplayName())) {
                            DataAssetProperties assetProperties = new DataAssetProperties();
                            assetProperties.setDisplayName(jsonAsset.getDisplayName());
                            myContext.updateDataAsset(guid, true, assetProperties, new Date());
                        }
                    }
                }
                //assetQualifiedNames.add(dataAssetElements.get(0).getDataAssetProperties().getQualifiedName());

            }
            if (assetGUID != null) {
                assetGUIDs.add(assetGUID);
                if (jsonAsset.getEventType() != null) {
                    ensureSchemaIsCatalogued(jsonAsset, assetGUID);
                }
            } else {
                //error
            }

        }
        // remember asset
        return assetGUIDs;
    }

    /**
     * This code ensure that the schema associated with the supplied asset is appropriately catalogued.
     * The asset that is supplied is expected to be a Kafka topic and the schema is mapped
     * to event schema entities.
     *
     * This method does create, update delete on EventType (the schema type) and its schema attributes.
     * Deletion of the Event type is assumed to take out any schema attributes under it.
     * @param assetFromJSON - the asset from the json
     * @param assetGUID - asset GUID
     * @throws InvalidParameterException invalid parameter exception
     * @throws UserNotAuthorizedException user is not authorised
     * @throws PropertyServerException property server Exception
     */

    private void ensureSchemaIsCatalogued(EventContent.AssetFromJSON assetFromJSON, String assetGUID) throws InvalidParameterException, PropertyServerException, UserNotAuthorizedException {
        EventContent.EventTypeFromJSON eventTypeFromJson = assetFromJSON.getEventType();


        SchemaTypeElement childSchemaType = myContext.getSchemaTypeForElement(
                assetGUID,
                assetFromJSON.getTypeName(),
                new Date());


        if (childSchemaType ==null) {
            SchemaTypeProperties schemaTypeProperties = new SchemaTypeProperties();
            schemaTypeProperties.setTypeName("EventType");
            EventContent.EventTypeFromJSON eventTypeFromJSON = assetFromJSON.getEventType();
            schemaTypeProperties.setQualifiedName(eventTypeFromJSON.getQualifiedName());
            schemaTypeProperties.setDisplayName(eventTypeFromJSON.getDisplayName());
            String schemaTypeGUID = myContext.createSchemaType( true, schemaTypeProperties);
            //link to asset
            myContext.setupSchemaTypeParent(true,schemaTypeGUID,assetGUID,"KafkaTopic",null, new Date());

            // for each schema attribute create it
            for (EventContent.Attribute attribute:eventTypeFromJSON.getAttributes()) {
                SchemaAttributeProperties schemaAttributeProperties = new SchemaAttributeProperties();
                schemaAttributeProperties.setQualifiedName(attribute.getQualifiedName());
                schemaAttributeProperties.setDisplayName(attribute.getName());
                schemaAttributeProperties.setTypeName(attribute.getType());
                schemaAttributeProperties.setDescription(attribute.getDescription());
                myContext.createSchemaAttribute(true,schemaTypeGUID,schemaAttributeProperties,new Date());

                // link to schema type
            }



        } else {
            String schemaTypeGUID = childSchemaType.getElementHeader().getGUID();
            if (childSchemaType.getElementHeader().getType().getTypeName().equals("EventType")) {
                // check whether we need to update it
                String existingQualifiedName = childSchemaType.getSchemaTypeProperties().getQualifiedName();
                String existingDisplayName = childSchemaType.getSchemaTypeProperties().getDisplayName();
                String jsonEventTypeDisplayName = eventTypeFromJson.getDisplayName();
                String jsonEventTypeQualifiedName = eventTypeFromJson.getQualifiedName();

                boolean displayNameMatch = jsonEventTypeDisplayName.equals(existingDisplayName);
                boolean qualifiedNameMatch = jsonEventTypeQualifiedName.equals(existingQualifiedName);
                if (!(displayNameMatch && qualifiedNameMatch)) {
                    // need to update
                    SchemaTypeProperties schemaTypeProperties = new SchemaTypeProperties();
                    if (!displayNameMatch) {
                        schemaTypeProperties.setQualifiedName(jsonEventTypeQualifiedName);
                    }
                    if (!qualifiedNameMatch) {
                        schemaTypeProperties.setQualifiedName(jsonEventTypeDisplayName);
                    }
                    myContext.updateSchemaType(schemaTypeGUID, true, schemaTypeProperties, new Date());
                }
                // check the schema attributes
                List<SchemaAttributeElement> existingSchemaAttributes = myContext.getNestedSchemaAttributes(schemaTypeGUID, 0, 1000, new Date());

                Map<String, SchemaAttributeElement> existingSchemaAttributesMap = new HashMap<>();
                Map<String, EventContent.Attribute> jsonAttributeMap = new HashMap<>();
                for (SchemaAttributeElement schemaAttributeElement : existingSchemaAttributes) {
                    existingSchemaAttributesMap.put(schemaAttributeElement.getSchemaAttributeProperties().getQualifiedName(), schemaAttributeElement);
                }
                for (EventContent.Attribute attribute : eventTypeFromJson.getAttributes()) {
                    jsonAttributeMap.put(attribute.getQualifiedName(), attribute);
                }

                // TODO loops to determine adds updates and deletes for schema attributes

                final Set<String> existingKeySet = existingSchemaAttributesMap.keySet();
                final Set<String> jsonKeySet = jsonAttributeMap.keySet();

                List<SchemaAttributeProperties> addSchemaAttributes = new ArrayList<>();
                List<String> deleteSchemaAttributeGUIDs = new ArrayList<>();
               // List<SchemaAttributeElement> updateSchemaAttributes = new ArrayList<>();
                for (String existingQName : existingKeySet) {
                    if (jsonKeySet.contains(existingQName)) {
                        // TODO updates to attributes
                        // String  schemaAttributeGUID ="";
//            SchemaAttributeProperties schemaAttributeProperties = new SchemaAttributeProperties();
//            myContext.updateSchemaAttribute(schemaAttributeGUID, true,schemaAttributeProperties, new Date());

                    } else {
                        deleteSchemaAttributeGUIDs.add(existingSchemaAttributesMap.get(existingQName).getElementHeader().getGUID());
                    }
                }
                for (String jsonAttributeQName : jsonKeySet) {
                    if (!existingKeySet.contains(jsonAttributeQName)) {
                        EventContent.Attribute attributeToAdd = jsonAttributeMap.get(jsonAttributeQName);
                        SchemaAttributeProperties schemaAttributeProperties = new SchemaAttributeProperties();
                        schemaAttributeProperties.setQualifiedName(jsonEventTypeQualifiedName);
                        schemaAttributeProperties.setDisplayName(attributeToAdd.getName());
                        schemaAttributeProperties.setTypeName(attributeToAdd.getType());
                        // TODO deal with minimum
                        addSchemaAttributes.add(schemaAttributeProperties);
                    }
                }
                // action the deletes
                for (String schemaAttributeGUID : deleteSchemaAttributeGUIDs) {
                    myContext.removeSchemaAttribute(schemaAttributeGUID, new Date());
                }
                // action the adds
                for (SchemaAttributeProperties schemaAttributeProperties : addSchemaAttributes) {
                    myContext.createSchemaAttribute(true, schemaTypeGUID, schemaAttributeProperties, new Date());
                }
            } else {
                // delete - this should cascade and delete any children.
                myContext.removeSchemaType(schemaTypeGUID, new Date());
            }
        }

    }

    /**
     * Save the lineage. The input and assets will have been catalogued prior to this method.
     *
     * This method creates a process entity then knits it to the input and output assets.
     *
     * This is creating asset level lineage not column level.
     *
     * The relationship between the input asset and the process is a DataFlow relationship which contains
     * the formula, which is the SQL.
     *
     * @param eventContent - representation of the event as a java object.
     * @throws InvalidParameterException invalid parameter exception
     * @throws UserNotAuthorizedException user is not authorised
     * @throws PropertyServerException property server Exception
     */
    private void saveLineage(EventContent eventContent) throws InvalidParameterException, PropertyServerException, UserNotAuthorizedException {
        ProcessProperties processProperties = new ProcessProperties();
        processProperties.setQualifiedName(eventContent.getProcessQualifiedName());
        processProperties.setDisplayName(eventContent.getProcessDisplayName());
        String processGUID = myContext.createProcess(true, ProcessStatus.ACTIVE,processProperties);

        for (String assetGUID :inAssetGUIDs) {
            DataFlowProperties properties = new DataFlowProperties();
            DataAssetElement dataAssetElement = myContext.getDataAssetByGUID(assetGUID, new Date());

            String sql = eventContent.getSQLForInputAsset(dataAssetElement.getDataAssetProperties().getQualifiedName());
            if (sql != null) {
                properties.setFormula(sql);
            }
            myContext.setupDataFlow(true,dataAssetElement.getElementHeader().getGUID(), processGUID, properties, new Date());
        }
        for (String assetGUID :outAssetGUIDs) {

            DataFlowProperties properties = new DataFlowProperties();
            DataAssetElement dataAssetElement = myContext.getDataAssetByGUID(assetGUID, new Date());
            myContext.setupDataFlow(true, processGUID, dataAssetElement.getElementHeader().getGUID(), properties, new Date());
        }
    }
}
