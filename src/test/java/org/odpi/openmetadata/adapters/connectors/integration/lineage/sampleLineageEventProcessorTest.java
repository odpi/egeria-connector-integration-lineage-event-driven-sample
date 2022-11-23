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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *  Test of the event parsing into the EventContent object
 */
public class sampleLineageEventProcessorTest
{

    @Test
    void testEventProcessor() throws IOException, ConnectorCheckedException, InvalidParameterException, PropertyServerException, UserNotAuthorizedException {
        String textPath = "src/test/resources/Sample1.json";
        Path path = Paths.get(textPath);
        String content = Files.readString(path);
        LineageEventContentforSample eventContent = new LineageEventContentforSample(content);

        LineageIntegratorContext mockContext = new MockLineageIntegratorContext();
        SampleLineageEventProcessor eventProcessor = new SampleLineageEventProcessor(mockContext);
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

        // test update assets and processes

        textPath = "src/test/resources/Sample2-update-assets-and-process.json";
        path = Paths.get(textPath);
        content = Files.readString(path);
        eventContent = new LineageEventContentforSample(content);

        eventProcessor = new SampleLineageEventProcessor(mockContext);
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
        assertTrue("TestResource-2".equals(processElement.getProcessProperties().getDescription()));

        // replace assets

        textPath = "src/test/resources/Sample3-replace-assets.json";
        path = Paths.get(textPath);
        content = Files.readString(path);
        eventContent = new LineageEventContentforSample(content);

        eventProcessor = new SampleLineageEventProcessor(mockContext);
        eventProcessor.processEvent(eventContent);
        assetList = mockContext.getDataAssetsByName("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37-3",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        inputDataAssetElement = assetList.get(0);
        assertTrue(inputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Foo-3"));
        assertTrue(inputDataAssetElement.getElementHeader().getGUID() != null);


        assetList = mockContext.getDataAssetsByName("vertriebskunde-services.agree-kundendaten-3",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        outputDataAssetElement = assetList.get(0);
        assertTrue(outputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Kundendaten-3"));
        assertTrue(outputDataAssetElement.getElementHeader().getGUID() != null);


        processElementList = mockContext.getProcessesByName("1234567890", 0 , 1000,new Date());
        assertTrue(processElementList != null);
        assertTrue(!processElementList.isEmpty());
        processElement = processElementList.get(0);
        assertTrue("1234567890".equals(processElement.getProcessProperties().getQualifiedName()));
        assertTrue("TestRes-3".equals(processElement.getProcessProperties().getDisplayName()));
        assertTrue("TestResource-3".equals(processElement.getProcessProperties().getDescription()));

        // add delete and update attributes

        textPath = "src/test/resources/Sample4-update-attributes.json";
        path = Paths.get(textPath);
        content = Files.readString(path);
        eventContent = new LineageEventContentforSample(content);

        eventProcessor = new SampleLineageEventProcessor(mockContext);
        eventProcessor.processEvent(eventContent);
        assetList = mockContext.getDataAssetsByName("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37-3",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        inputDataAssetElement = assetList.get(0);
        assertTrue(inputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Foo-3"));
        assertTrue(inputDataAssetElement.getElementHeader().getGUID() != null);


        assetList = mockContext.getDataAssetsByName("vertriebskunde-services.agree-kundendaten-3",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        outputDataAssetElement = assetList.get(0);
        assertTrue(outputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Kundendaten-3"));
        assertTrue(outputDataAssetElement.getElementHeader().getGUID() != null);


        processElementList = mockContext.getProcessesByName("1234567890", 0 , 1000,new Date());
        assertTrue(processElementList != null);
        assertTrue(!processElementList.isEmpty());
        processElement = processElementList.get(0);
        assertTrue("1234567890".equals(processElement.getProcessProperties().getQualifiedName()));
        assertTrue("TestRes-3".equals(processElement.getProcessProperties().getDisplayName()));
        assertTrue("TestResource-3".equals(processElement.getProcessProperties().getDescription()));
        schemaTypeElement = mockContext.getSchemaTypeForElement(outputDataAssetElement.getElementHeader().getGUID(), "KafkaTopic", new Date());
        schemaTypeProperties = schemaTypeElement.getSchemaTypeProperties();

        assertTrue(schemaTypeElement != null);
        schemaTypeElementGUID = schemaTypeElement.getElementHeader().getGUID();
        assertTrue(schemaTypeElementGUID !=null);
        assertTrue(schemaTypeProperties !=null);
        assertTrue(schemaTypeProperties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person"));
        assertTrue(schemaTypeProperties.getDisplayName().equals("Person"));
        attributes = mockContext.getNestedSchemaAttributes(schemaTypeElementGUID, 0, 1000, new Date());

        assertTrue(attributes != null);
        assertTrue(attributes.size() == 3);
        firstNameFound = false;
        boolean middleNameFound = false;
        ageFound = false;
        for (SchemaAttributeElement attribute: attributes) {
            SchemaAttributeProperties properties = attribute.getSchemaAttributeProperties();
            if (properties.getDisplayName().equals("firstName")) {
                assertTrue(properties.getDescription().equals("The person's first name or name to be called."));
                assertTrue( properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person~firstName"));
                assertTrue(properties.getTypeName().equals("string"));
                firstNameFound = true;
            } else   if (properties.getDisplayName().equals("middleName")) {
                assertTrue( properties.getDescription().equals("The person's middle name."));
                assertTrue( properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person~middleName"));
                assertTrue(properties.getTypeName().equals("string"));
                middleNameFound = true;
            } else   if (properties.getDisplayName().equals("age")) {
                assertTrue(properties.getDescription().equals("Age as a string to test type change."));
                assertTrue(properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person~age"));
                assertTrue(properties.getTypeName().equals("string"));
                ageFound = true;
            }
        }
        assertTrue(firstNameFound);
        assertTrue(middleNameFound);
        assertTrue(ageFound);



        // Replace the event type
        List<SchemaTypeElement> schemaTypeElementList = mockContext.getSchemaTypeByName("vertriebskunde-services.agree-kundendaten-3~Person",0,1000,new Date());
        assert(schemaTypeElementList.size() == 1);
        textPath = "src/test/resources/Sample5-replace-event-type.json";
        path = Paths.get(textPath);
        content = Files.readString(path);
        eventContent = new LineageEventContentforSample(content);

        eventProcessor = new SampleLineageEventProcessor(mockContext);
        eventProcessor.processEvent(eventContent);
        schemaTypeElementList = mockContext.getSchemaTypeByName("vertriebskunde-services.agree-kundendaten-3~Person",0,1000,new Date());
        assert(schemaTypeElementList.size() == 0);
        assetList = mockContext.getDataAssetsByName("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37-3",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        inputDataAssetElement = assetList.get(0);
        assertTrue(inputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Foo-3"));
        assertTrue(inputDataAssetElement.getElementHeader().getGUID() != null);


        assetList = mockContext.getDataAssetsByName("vertriebskunde-services.agree-kundendaten-3",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        outputDataAssetElement = assetList.get(0);
        assertTrue(outputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Kundendaten-3"));
        assertTrue(outputDataAssetElement.getElementHeader().getGUID() != null);


        processElementList = mockContext.getProcessesByName("1234567890", 0 , 1000,new Date());
        assertTrue(processElementList != null);
        assertTrue(!processElementList.isEmpty());
        processElement = processElementList.get(0);
        assertTrue("1234567890".equals(processElement.getProcessProperties().getQualifiedName()));
        assertTrue("TestRes-3".equals(processElement.getProcessProperties().getDisplayName()));
        assertTrue("TestResource-3".equals(processElement.getProcessProperties().getDescription()));
        schemaTypeElement = mockContext.getSchemaTypeForElement(outputDataAssetElement.getElementHeader().getGUID(), "KafkaTopic", new Date());
        schemaTypeProperties = schemaTypeElement.getSchemaTypeProperties();
        assertTrue(schemaTypeElement != null);
        schemaTypeElementGUID = schemaTypeElement.getElementHeader().getGUID();
        assertTrue(schemaTypeElementGUID !=null);
        assertTrue(schemaTypeProperties !=null);
        assertTrue(schemaTypeProperties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person-2"));
        assertTrue(schemaTypeProperties.getDisplayName().equals("Person-2"));
        attributes = mockContext.getNestedSchemaAttributes(schemaTypeElementGUID, 0, 1000, new Date());

        assertTrue(attributes != null);
        assertTrue(attributes.size() == 3);
        firstNameFound = false;
        middleNameFound = false;
        ageFound = false;
        for (SchemaAttributeElement attribute: attributes) {
            SchemaAttributeProperties properties = attribute.getSchemaAttributeProperties();
            if (properties.getDisplayName().equals("firstName")) {
                assertTrue(properties.getDescription().equals("The person's first name or name to be called."));
                assertTrue( properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person-2~firstName"));
                assertTrue(properties.getTypeName().equals("string"));
                firstNameFound = true;
            } else   if (properties.getDisplayName().equals("middleName")) {
                assertTrue( properties.getDescription().equals("The person's middle name."));
                assertTrue( properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person-2~middleName"));
                assertTrue(properties.getTypeName().equals("string"));
                middleNameFound = true;
            } else   if (properties.getDisplayName().equals("age")) {
                assertTrue(properties.getDescription().equals("Age as a string to test type change."));
                assertTrue(properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person-2~age"));
                assertTrue(properties.getTypeName().equals("string"));
                ageFound = true;
            }
        }
        assertTrue(firstNameFound);
        assertTrue(middleNameFound);
        assertTrue(ageFound);

        //Sample6-update-SQL.json
        textPath = "src/test/resources/Sample6-update-SQL.json";
        path = Paths.get(textPath);
        content = Files.readString(path);
        eventContent = new LineageEventContentforSample(content);

        eventProcessor = new SampleLineageEventProcessor(mockContext);
        eventProcessor.processEvent(eventContent);

        assetList = mockContext.getDataAssetsByName("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37-3",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        inputDataAssetElement = assetList.get(0);
        assertTrue(inputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Foo-3"));
        assertTrue(inputDataAssetElement.getElementHeader().getGUID() != null);

        dataFlowsIn= ((MockLineageIntegratorContext)mockContext).getDataflows(inputDataAssetElement.getElementHeader().getGUID(), processElement.getElementHeader().getGUID(), new Date());
        assertTrue(dataFlowsIn !=null);
        assertTrue(!dataFlowsIn.isEmpty());
        assertTrue(dataFlowsIn.size() ==1);
        dataFlowElement = dataFlowsIn.get(0);
        assertTrue(dataFlowElement.getDataFlowProperties().getFormula().equals("select * from foo-3;"));


        assetList = mockContext.getDataAssetsByName("vertriebskunde-services.agree-kundendaten-3",0, 1000,new Date());
        assertTrue(assetList!= null);
        assertTrue(assetList.size() ==1);
        outputDataAssetElement = assetList.get(0);
        assertTrue(outputDataAssetElement.getDataAssetProperties().getDisplayName().equals("Kundendaten-3"));
        assertTrue(outputDataAssetElement.getElementHeader().getGUID() != null);


        processElementList = mockContext.getProcessesByName("1234567890", 0 , 1000,new Date());
        assertTrue(processElementList != null);
        assertTrue(!processElementList.isEmpty());
        processElement = processElementList.get(0);
        assertTrue("1234567890".equals(processElement.getProcessProperties().getQualifiedName()));
        assertTrue("TestRes-3".equals(processElement.getProcessProperties().getDisplayName()));
        assertTrue("TestResource-3".equals(processElement.getProcessProperties().getDescription()));
        schemaTypeElement = mockContext.getSchemaTypeForElement(outputDataAssetElement.getElementHeader().getGUID(), "KafkaTopic", new Date());
        schemaTypeProperties = schemaTypeElement.getSchemaTypeProperties();
        assertTrue(schemaTypeElement != null);
        schemaTypeElementGUID = schemaTypeElement.getElementHeader().getGUID();
        assertTrue(schemaTypeElementGUID !=null);
        assertTrue(schemaTypeProperties !=null);
        assertTrue(schemaTypeProperties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person-2"));
        assertTrue(schemaTypeProperties.getDisplayName().equals("Person-2"));
        attributes = mockContext.getNestedSchemaAttributes(schemaTypeElementGUID, 0, 1000, new Date());

        assertTrue(attributes != null);
        assertTrue(attributes.size() == 3);
        firstNameFound = false;
        middleNameFound = false;
        ageFound = false;
        for (SchemaAttributeElement attribute: attributes) {
            SchemaAttributeProperties properties = attribute.getSchemaAttributeProperties();
            if (properties.getDisplayName().equals("firstName")) {
                assertTrue(properties.getDescription().equals("The person's first name or name to be called."));
                assertTrue( properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person-2~firstName"));
                assertTrue(properties.getTypeName().equals("string"));
                firstNameFound = true;
            } else   if (properties.getDisplayName().equals("middleName")) {
                assertTrue( properties.getDescription().equals("The person's middle name."));
                assertTrue( properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person-2~middleName"));
                assertTrue(properties.getTypeName().equals("string"));
                middleNameFound = true;
            } else   if (properties.getDisplayName().equals("age")) {
                assertTrue(properties.getDescription().equals("Age as a string to test type change."));
                assertTrue(properties.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten-3~Person-2~age"));
                assertTrue(properties.getTypeName().equals("string"));
                ageFound = true;
            }
        }
        assertTrue(firstNameFound);
        assertTrue(middleNameFound);
        assertTrue(ageFound);

    }
}


