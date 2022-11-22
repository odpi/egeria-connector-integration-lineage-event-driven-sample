/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage;


import org.junit.jupiter.api.Test;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *  Test of the event parsing into the EventContent object
 */
public class EventContentTest
{

    @Test
    void testEventContent() throws IOException, ConnectorCheckedException {
        String textPath = "src/test/resources/Sample1.json";
        Path path = Paths.get(textPath);
        String content = Files.readString(path);
        EventContent eventContent = new EventContent(content);

        assertTrue("TestRes".equals(eventContent.getProcessDisplayName()));
        assertTrue("1234567890".equals(eventContent.getProcessQualifiedName()));

        List<EventContent.AssetFromJSON> inputAssets = eventContent.getInputAssets();
        List<EventContent.AssetFromJSON> outputAssets = eventContent.getOutputAssets();

        assertTrue(inputAssets.size() ==1);

        EventContent.AssetFromJSON inputAsset =inputAssets.get(0);
        assertTrue(inputAsset.getTypeName().equals("DataSet"));
        assertTrue(inputAsset.getDisplayName().equals("Foo"));
        assertTrue(inputAsset.getQualifiedName().equals("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37"));
        assertTrue(eventContent.getSQLForInputAsset(inputAsset.getQualifiedName()).equals("select * from foo;"));
        EventContent.AssetFromJSON outputAsset =outputAssets.get(0);
        assertTrue(outputAsset.getTypeName().equals("KafkaTopic"));
        assertTrue(outputAsset.getDisplayName().equals("Kundendaten"));
        assertTrue(outputAsset.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten"));
        EventContent.EventTypeFromJSON eventType =outputAsset.getEventType();
        assertTrue(eventType!=null);
        assertTrue(eventType.getDisplayName().equals("Person"));
        assertTrue(eventType.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person"));
        List<EventContent.Attribute> attributes = eventType.getAttributes();
        assertTrue(attributes != null);
        assertTrue(attributes.size() == 3);

        EventContent.Attribute firstNameAttr = attributes.get(0);
        assertTrue(firstNameAttr.getName().equals("firstName"));
        assertTrue(firstNameAttr.getDescription().equals("The person's first name."));
        assertTrue(firstNameAttr.getType().equals("string"));
        assertTrue(firstNameAttr.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~firstName"));

        EventContent.Attribute lastNameAttr = attributes.get(1);
        assertTrue(lastNameAttr.getName().equals("lastName"));
        assertTrue(lastNameAttr.getDescription().equals("The person's last name."));
        assertTrue(lastNameAttr.getType().equals("string"));
        assertTrue(lastNameAttr.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~lastName"));

        EventContent.Attribute ageAttr = attributes.get(2);
        assertTrue(ageAttr.getName().equals("age"));
        assertTrue(ageAttr.getDescription().equals("Age in years which must be equal to or greater than zero."));
        assertTrue(ageAttr.getType().equals("integer"));
        assertTrue(ageAttr.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~age"));

    }
}


