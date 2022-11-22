/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage;


import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.*;
import org.odpi.openmetadata.accessservices.assetmanager.properties.*;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.ElementHeader;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.ElementType;
import org.odpi.openmetadata.integrationservices.lineage.connector.LineageIntegratorContext;

import java.util.*;

/**
 *  mock context
 */
public class MockLineageIntegratorContext extends LineageIntegratorContext
{
    private int guidCounter=0;
    // source and target assets
    private Map<String, DataAssetElement> guidToDataAssetElementMap = new HashMap<>();
    private Map<String, DataAssetElement> qnameToDataAssetElementMap = new HashMap<>();

    //Process
    private Map<String, ProcessElement> guidToProcessElementMap = new HashMap<>();
    private Map<String, ProcessElement> qnameToProcessElementMap = new HashMap<>();
    private Map<String, DataFlowElement> guidToDataFlowElementMap = new HashMap<>();
    // key is the asset
    // private Map<String, SchemaTypeElement> guidToSchemaTypeElementMap = new HashMap<>();
    // key is the schema type
    private Map<String, SchemaTypeElement>guidToSchemaTypeMap= new HashMap<>();


    private Map<String, RelationshipElement> guidToAssetSchemaTypeMap = new HashMap<>();

    private Map<String, SchemaAttributeElement> guidToSchemaAttributeElementMap= new HashMap<>();

    private Map<String, List<SchemaAttributeElement>> schemaTypeGUIDToNestedAttributesMap = new HashMap<>();
    public MockLineageIntegratorContext() {
        super(null,null,null,null,null,
                null,null,null,null,null,null,null);

    }
    @Override
    public List<DataAssetElement> getDataAssetsByName(String name,
                                                      int    startFrom,
                                                      int    pageSize,
                                                      Date   effectiveTime)
    {
        DataAssetElement dataAssetElement = qnameToDataAssetElementMap.get(name);
        if (dataAssetElement == null) {
            return null;
        }
        List<DataAssetElement> dataAssetElementList = new ArrayList<>();
        dataAssetElementList.add(dataAssetElement);
        return dataAssetElementList;

    }
    @Override
    public String createDataAsset(boolean             assetManagerIsHome,
                                  DataAssetProperties assetProperties)
    {
        String guid = createNewGUID();
        DataAssetElement dataAssetElement = new DataAssetElement();
        dataAssetElement.setDataAssetProperties(assetProperties);
        ElementHeader elementHeader = new ElementHeader();
        elementHeader.setGUID(guid);
        dataAssetElement.setElementHeader(elementHeader);
        guidToDataAssetElementMap.put(guid, dataAssetElement);
        qnameToDataAssetElementMap.put(assetProperties.getQualifiedName(), dataAssetElement);
        return guid;
    }
    @Override
    public void updateDataAsset(String              assetGUID,
                                boolean             isMergeUpdate,
                                DataAssetProperties assetProperties,
                                Date                effectiveTime)
    {
        DataAssetElement dataAssetElement =guidToDataAssetElementMap.get(assetGUID);
        DataAssetProperties existingDataAssetProperties = dataAssetElement.getDataAssetProperties();
        String newDisplayName = assetProperties.getDisplayName();
        if (newDisplayName != null) {
            if (!newDisplayName.equals(existingDataAssetProperties.getDisplayName())) {
                existingDataAssetProperties.setDisplayName(newDisplayName);
            }
        }

        String newDescription = assetProperties.getDescription();
        if (newDescription !=null) {
            if (!newDescription.equals(existingDataAssetProperties.getDescription())) {
                existingDataAssetProperties.setDescription(newDescription);
            }
        }
    }
    @Override
    public DataAssetElement getDataAssetByGUID(String openMetadataGUID,
                                               Date   effectiveTime)
    {
        return guidToDataAssetElementMap.get(openMetadataGUID);
    }
    @Override
    public SchemaTypeElement getSchemaTypeForElement(String parentElementGUID,
                                                     String parentElementTypeName,
                                                     Date   effectiveTime) throws InvalidParameterException,
            UserNotAuthorizedException,
            PropertyServerException
    {
        SchemaTypeElement schemaTypeElement =null;
        // todo we may have to throw an exception if we do not find it
//        return guidToSchemaTypeElementMap.get(parentElementGUID);
        // go through the asset schema type relationships for a match
        for (String guid:guidToAssetSchemaTypeMap.keySet()) {
            RelationshipElement relationshipElement = guidToAssetSchemaTypeMap.get(guid);
            if (relationshipElement.getEnd1GUID().getGUID().equals(parentElementGUID)) {
                String schemaTypeGUID = relationshipElement.getEnd2GUID().getGUID();
                schemaTypeElement = guidToSchemaTypeMap.get(schemaTypeGUID);
            }
        }
        return schemaTypeElement;

    }
    @Override
    public void updateSchemaType(String               schemaTypeGUID,
                                 boolean              isMergeUpdate,
                                 SchemaTypeProperties schemaTypeProperties,
                                 Date                 effectiveTime)
    {
        SchemaTypeProperties existingProperties =guidToSchemaTypeMap.get(schemaTypeGUID).getSchemaTypeProperties();
        String newDisplayName =schemaTypeProperties.getDisplayName();
        String newDescription = schemaTypeProperties.getDescription();

        if (!newDisplayName.equals(existingProperties.getDisplayName())) {
            existingProperties.setDisplayName(newDisplayName);
        }
        if (!newDescription.equals(existingProperties.getDescription())) {
            existingProperties.setDescription(newDescription);
        }
    }

    @Override
    public String createSchemaType(boolean              assetManagerIsHome,
                                   SchemaTypeProperties schemaTypeProperties)
    {
        SchemaTypeElement schemaTypeElement= new SchemaTypeElement();
        String guid = createNewGUID();

        schemaTypeElement.setSchemaTypeProperties(schemaTypeProperties);
        ElementHeader elementHeader = new ElementHeader();
        elementHeader.setGUID(guid);
        schemaTypeElement.setElementHeader(elementHeader);
        guidToSchemaTypeMap.put(guid,schemaTypeElement);
        return guid;
    }
    @Override
    public void setupSchemaTypeParent(boolean                assetManagerIsHome,
                                      String                 schemaTypeGUID,
                                      String                 parentElementGUID,
                                      String                 parentElementTypeName,
                                      RelationshipProperties properties,
                                      Date                   effectiveTime) {

        RelationshipElement relationshipElement = new RelationshipElement();
        String guid = createNewGUID();
        ElementHeader header = new ElementHeader();
        header.setGUID(guid);
        ElementType elementType = new ElementType();
        elementType.setTypeName("AssetSchemaType");
        header.setType(elementType);
        relationshipElement.setRelationshipHeader(header);

        ElementHeader header1 = new ElementHeader();
        header1.setGUID(parentElementGUID);
        relationshipElement.setEnd1GUID(header1);
        ElementHeader header2 = new ElementHeader();
        header2.setGUID(schemaTypeGUID);
        relationshipElement.setEnd2GUID(header2);

        guidToAssetSchemaTypeMap.put(parentElementGUID, relationshipElement);
    }
    @Override
    public SchemaTypeElement getSchemaTypeByGUID(String schemaTypeGUID,
                                                 Date   effectiveTime) throws InvalidParameterException,
            UserNotAuthorizedException,
            PropertyServerException {
        return guidToSchemaTypeMap.get(schemaTypeGUID);
    }

    public List<SchemaAttributeElement>    getNestedSchemaAttributes(String parentSchemaElementGUID,
                                                                     int    startFrom,
                                                                     int    pageSize,
                                                                     Date   effectiveTime) throws InvalidParameterException,
            UserNotAuthorizedException,
            PropertyServerException
    {
        return schemaTypeGUIDToNestedAttributesMap.get(parentSchemaElementGUID);
    }
    @Override
    public String createSchemaAttribute(boolean                   assetManagerIsHome,
                                        String                    schemaElementGUID,
                                        SchemaAttributeProperties schemaAttributeProperties,
                                        Date                      effectiveTime) throws InvalidParameterException,
            UserNotAuthorizedException,
            PropertyServerException
    {
        String guid = createNewGUID();
        SchemaAttributeElement schemaAttributeElement = new SchemaAttributeElement();
        ElementHeader elementHeader =  new ElementHeader();
        elementHeader.setGUID(guid);
        schemaAttributeElement.setElementHeader(elementHeader);
        schemaAttributeElement.setSchemaAttributeProperties(schemaAttributeProperties);

        guidToSchemaAttributeElementMap.put(guid,schemaAttributeElement);

        List<SchemaAttributeElement> attributeElements = schemaTypeGUIDToNestedAttributesMap.get(schemaElementGUID);
        if (attributeElements == null) {
            attributeElements = new ArrayList<>();
        }
        attributeElements.add(schemaAttributeElement);
        schemaTypeGUIDToNestedAttributesMap.put(schemaElementGUID, attributeElements);


        return guid;
    }
    @Override
    public void updateSchemaAttribute(String                    schemaAttributeGUID,
                                      boolean                   isMergeUpdate,
                                      SchemaAttributeProperties schemaAttributeProperties,
                                      Date                      effectiveTime) throws InvalidParameterException,
            UserNotAuthorizedException,
            PropertyServerException
    {
        SchemaAttributeElement schemaAttributeElement = guidToSchemaAttributeElementMap.get(schemaAttributeGUID);
        SchemaAttributeProperties existingSchemaAttributeProperties = schemaAttributeElement.getSchemaAttributeProperties();
        String newDisplayName = schemaAttributeProperties.getDisplayName();
        if (!newDisplayName.equals(existingSchemaAttributeProperties.getDisplayName())) {
            existingSchemaAttributeProperties.setDisplayName(newDisplayName);
        }
        String newDescription = schemaAttributeProperties.getDescription();
        if (!newDescription.equals(existingSchemaAttributeProperties.getDescription())) {
            existingSchemaAttributeProperties.setDescription(newDescription);
        }



    }

    @Override
    public void removeSchemaType(String schemaTypeGUID, Date effectiveTime) throws InvalidParameterException, UserNotAuthorizedException, PropertyServerException {

    }

    @Override
    public void removeSchemaAttribute(String schemaAttributeGUID, Date effectiveTime) throws InvalidParameterException, UserNotAuthorizedException, PropertyServerException {

    }

    @Override
    public void updateProcess(String processGUID, boolean isMergeUpdate, ProcessProperties processProperties, Date effectiveTime) throws InvalidParameterException, UserNotAuthorizedException, PropertyServerException {
        ProcessElement processElement = guidToProcessElementMap.get(processGUID);
        ProcessProperties existingProcessProperties = processElement.getProcessProperties();

        String newDisplayName = processProperties.getDisplayName();
        if (!newDisplayName.equals(existingProcessProperties.getDisplayName())) {
            existingProcessProperties.setDisplayName(newDisplayName);
        }
        String newDescription = processProperties.getDescription();
        if (!newDescription.equals(existingProcessProperties.getDescription())) {
            existingProcessProperties.setDescription(newDescription);
        }

    }
    @Override
    public String createProcess(boolean           assetManagerIsHome,
                                ProcessStatus processStatus,
                                ProcessProperties processProperties) throws InvalidParameterException,
            UserNotAuthorizedException,
            PropertyServerException
    {
        String guid = createNewGUID();
        ProcessElement processElement = new ProcessElement();
        processElement.setProcessProperties(processProperties);
        ElementHeader elementHeader = new ElementHeader();
        elementHeader.setGUID(guid);
        processElement.setElementHeader(elementHeader);
        guidToProcessElementMap.put(guid, processElement);
        qnameToProcessElementMap.put(processProperties.getQualifiedName(), processElement);
        return guid;
    }
    @Override
    public List<ProcessElement>   getProcessesByName(String name,
                                                     int    startFrom,
                                                     int    pageSize,
                                                     Date   effectiveTime) throws InvalidParameterException,
            UserNotAuthorizedException,
            PropertyServerException
    {
        ProcessElement processElement = qnameToProcessElementMap.get(name);
        if (processElement == null) {
            return null;
        }
        List<ProcessElement> processElementList = new ArrayList<>();
        processElementList.add(processElement);
        return processElementList;
    }
    @Override
    public String setupDataFlow(boolean            assetManagerIsHome,
                                String             dataSupplierGUID,
                                String             dataConsumerGUID,
                                DataFlowProperties properties,
                                Date               effectiveTime)  {
        String guid = createNewGUID();
        DataFlowElement dataFlowElement = new DataFlowElement();
        dataFlowElement.setDataFlowProperties(properties);
        ElementHeader elementHeader = new ElementHeader();
        elementHeader.setGUID(guid);
        dataFlowElement.setDataFlowHeader(elementHeader);


        ElementHeader consumerElementHeader = new ElementHeader();
        consumerElementHeader.setGUID(dataConsumerGUID);
        dataFlowElement.setDataConsumer(consumerElementHeader);
        ElementHeader suppierElementHeader = new ElementHeader();
        suppierElementHeader.setGUID(dataSupplierGUID);
        dataFlowElement.setDataSupplier(suppierElementHeader);
        guidToDataFlowElementMap.put(guid, dataFlowElement);

        return guid;
    }

    /**
     * Raise issue https://github.com/odpi/egeria/issues/7122 for this to be added to the context API
     * @param dataSupplierGUID
     * @param dataConsumerGUID
     * @param effectiveTime
     * @return
     */
    public List<DataFlowElement> getDataflows(String             dataSupplierGUID,
                                              String             dataConsumerGUID,
                                              Date               effectiveTime) {
        List<DataFlowElement> dataflowElements = new ArrayList<>();
        for (String dataflowGUID:guidToDataFlowElementMap.keySet()) {
            DataFlowElement dataFlowElement = guidToDataFlowElementMap.get(dataflowGUID);
            if (dataConsumerGUID.equals(dataFlowElement.getDataConsumer().getGUID()) &&
                    dataSupplierGUID.equals(dataFlowElement.getDataSupplier().getGUID())) {
                dataflowElements.add(dataFlowElement);
            }

        }
        return dataflowElements;
    }



    private String createNewGUID() {
        return "" + guidCounter++;
    }
}


