/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage;


import org.junit.jupiter.api.Test;
import org.odpi.openmetadata.accessservices.assetmanager.metadataelements.*;
import org.odpi.openmetadata.accessservices.assetmanager.properties.SchemaAttributeProperties;
import org.odpi.openmetadata.accessservices.assetmanager.properties.SchemaTypeProperties;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.integrationservices.lineage.connector.LineageIntegratorContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *  Test of the event parsing into the EventContent object
 */
public class EventProcessorTest
{

    @Test
    void testEventProcessor() throws IOException, ConnectorCheckedException, InvalidParameterException, PropertyServerException, UserNotAuthorizedException {
        String textPath = "src/test/resources/Sample1.json";
        Path path = Paths.get(textPath);
        String content = Files.readString(path);
        EventContent eventContent = new EventContent(content);

        LineageIntegratorContext mockContext = new MockLineageIntegratorContext();
        EventProcessor eventProcessor = new EventProcessor(mockContext);
        eventProcessor.processEvent(eventContent);
        List<DataAssetElement> assetList = mockContext.getDataAssetsByName("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        DataAssetElement inputDataAssetElement = assetList.get(0);
        assertTrue(inputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Foo"));
        assertTrue(inputDataAssetElement.getElementHeader().getGUID() != null);


        assetList = mockContext.getDataAssetsByName("vertriebskunde-services.agree-kundendaten",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        DataAssetElement outputDataAssetElement = assetList.get(0);
        assertTrue(outputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Kundendaten"));
        assertTrue(outputDataAssetElement.getElementHeader().getGUID() != null);

        List<ProcessElement>  processElementList = mockContext.getProcessesByName("1234567890", 0 , 1000,new Date());
        assertTrue(processElementList != null);
        assertTrue(!processElementList.isEmpty());
        ProcessElement processElement = processElementList.get(0);
        assertTrue("1234567890".equals(processElement.getProcessProperties().getQualifiedName()));
        assertTrue("TestRes".equals(processElement.getProcessProperties().getDisplayName()));

        List<DataFlowElement> dataFlowsIn= ((MockLineageIntegratorContext)mockContext).getDataflows(inputDataAssetElement.getElementHeader().getGUID(), processElement.getElementHeader().getGUID(), new Date());
        assertTrue(dataFlowsIn !=null);
        assertTrue(!dataFlowsIn.isEmpty());
        assertTrue(dataFlowsIn.size() ==1);
        DataFlowElement dataFlowElement = dataFlowsIn.get(0);
        assertTrue(dataFlowElement.getDataFlowProperties().getFormula().equals("select * from foo;"));
        assertTrue(dataFlowElement.getDataFlowHeader().getGUID() !=null);
        List<DataFlowElement> dataFlowsOut= ((MockLineageIntegratorContext)mockContext).getDataflows(processElement.getElementHeader().getGUID(), outputDataAssetElement.getElementHeader().getGUID(), new Date());
        assertTrue(dataFlowsOut !=null);
        assertTrue(!dataFlowsOut.isEmpty());
        assertTrue(dataFlowsOut.size() ==1);
        dataFlowElement = dataFlowsOut.get(0);
        assertTrue(dataFlowElement.getDataFlowProperties().getFormula() == null);
        assertTrue(dataFlowElement.getDataFlowHeader().getGUID() !=null);

        // check that the schematype and schema attributes are created

        SchemaTypeElement schemaTypeElement = mockContext.getSchemaTypeForElement(outputDataAssetElement.getElementHeader().getGUID(), "KafkaTopic", new Date());

        assertTrue(schemaTypeElement != null);
        String schemaTypeElementGUID = schemaTypeElement.getElementHeader().getGUID();
        assertTrue(schemaTypeElementGUID !=null);
        SchemaTypeProperties schemaTypeProperties = schemaTypeElement.getSchemaTypeProperties();
        assertTrue(schemaTypeProperties !=null);
        assertTrue(schemaTypeProperties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person"));
        assertTrue(schemaTypeProperties.getDisplayName().equals("Person"));
        List<SchemaAttributeElement>  attributes = mockContext.getNestedSchemaAttributes(schemaTypeElementGUID, 0, 1000, new Date());

        assertTrue(attributes != null);
        assertTrue(attributes.size() == 3);
        boolean firstNameFound = false;
        boolean lastNameFound = false;
        boolean ageFound = false;
        for (SchemaAttributeElement attribute: attributes) {
            SchemaAttributeProperties properties = attribute.getSchemaAttributeProperties();
            if (properties.getDisplayName().equals("firstName")) {
                assertTrue(properties.getDescription().equals("The person's first name."));
                assertTrue( properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~firstName"));
                assertTrue(properties.getTypeName().equals("string"));
                firstNameFound = true;
            } else   if (properties.getDisplayName().equals("lastName")) {
                assertTrue( properties.getDescription().equals("The person's last name."));
                assertTrue( properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~lastName"));
                assertTrue(properties.getTypeName().equals("string"));
                lastNameFound = true;
            } else   if (properties.getDisplayName().equals("age")) {
                assertTrue(properties.getDescription().equals("Age in years which must be equal to or greater than zero."));
                assertTrue(properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~age"));
                assertTrue(properties.getTypeName().equals("integer"));
                ageFound = true;
            }
        }
        assertTrue(firstNameFound);
        assertTrue(lastNameFound);
        assertTrue(ageFound);








        // test update asset display Name

        textPath = "src/test/resources/Sample2-update-asset-displayNames.json";
        path = Paths.get(textPath);
        content = Files.readString(path);
        eventContent = new EventContent(content);

        eventProcessor = new EventProcessor(mockContext);
        eventProcessor.processEvent(eventContent);
        assetList = mockContext.getDataAssetsByName("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        inputDataAssetElement = assetList.get(0);
        assertTrue(inputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Foo-2"));
        assertTrue(inputDataAssetElement.getElementHeader().getGUID() != null);


        assetList = mockContext.getDataAssetsByName("vertriebskunde-services.agree-kundendaten",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        outputDataAssetElement = assetList.get(0);
        assertTrue(outputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Kundendaten-2"));
        assertTrue(outputDataAssetElement.getElementHeader().getGUID() != null);


        processElementList = mockContext.getProcessesByName("1234567890", 0 , 1000,new Date());
        assertTrue(processElementList != null);
        assertTrue(!processElementList.isEmpty());
        processElement = processElementList.get(0);
        assertTrue("1234567890".equals(processElement.getProcessProperties().getQualifiedName()));
        assertTrue("TestRes-2".equals(processElement.getProcessProperties().getDisplayName()));






    }
}


