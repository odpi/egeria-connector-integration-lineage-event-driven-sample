/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample;


import org.junit.jupiter.api.Test;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test of the event parsing into the EventContent object
 */
public class LineageEventContentForSampleTest {

    private static LineageEventContentforSample getLineageEventContentforSample(String textPath) throws IOException, ConnectorCheckedException {
        Path path = Paths.get(textPath);
        String content = Files.readString(path);
        return new LineageEventContentforSample(content, "unit test");
    }

    @Test
    void testEventContent() throws IOException, ConnectorCheckedException {
        String textPath = "src/test/resources/Sample1.json";
        LineageEventContentforSample eventContent = getLineageEventContentforSample(textPath);

        assertEquals("TestRes", eventContent.getProcessTechnicalName());
        assertEquals("1234567890", eventContent.getProcessQualifiedName());

        List<LineageEventContentforSample.AssetFromJSON> inputAssets = eventContent.getInputAssets();
        List<LineageEventContentforSample.AssetFromJSON> outputAssets = eventContent.getOutputAssets();

        assertEquals(1, inputAssets.size());

        LineageEventContentforSample.AssetFromJSON inputAsset = inputAssets.get(0);
        assertEquals("DataSet", inputAsset.getTypeName());
        assertEquals("Foo", inputAsset.getDisplayName());
        assertEquals("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37", inputAsset.getQualifiedName());
        assertEquals("select * from foo;", eventContent.getFormulaForInputAsset(inputAsset.getQualifiedName()));
        LineageEventContentforSample.AssetFromJSON outputAsset = outputAssets.get(0);
        assertEquals("KafkaTopic", outputAsset.getTypeName());
        assertEquals("Kundendaten", outputAsset.getDisplayName());
        assertEquals("vertriebskunde-services.agree-kundendaten", outputAsset.getQualifiedName());
        LineageEventContentforSample.EventTypeFromJSON eventType = outputAsset.getEventTypes().get(0);
        assertEquals("Person", eventType.getTechnicalName());
        assertEquals("vertriebskunde-services.agree-kundendaten~Person", eventType.getQualifiedName());
        List<LineageEventContentforSample.Attribute> attributes = eventType.getAttributes();
        assertNotNull(attributes);
        assertEquals(5, attributes.size());

        LineageEventContentforSample.Attribute firstNameAttr = attributes.get(0);
        assertEquals("firstName", firstNameAttr.getDisplayName());
        assertEquals("The person's first name.", firstNameAttr.getDescription());
        assertEquals("string", firstNameAttr.getType());
        assertEquals("vertriebskunde-services.agree-kundendaten~Person~firstName", firstNameAttr.getQualifiedName());

        LineageEventContentforSample.Attribute lastNameAttr = attributes.get(1);
        assertEquals("lastName", lastNameAttr.getDisplayName());
        assertEquals("The person's last name.", lastNameAttr.getDescription());
        assertEquals("string", lastNameAttr.getType());
        assertEquals("vertriebskunde-services.agree-kundendaten~Person~lastName", lastNameAttr.getQualifiedName());

        LineageEventContentforSample.Attribute ageAttr = attributes.get(2);
        assertEquals("age", ageAttr.getDisplayName());
        assertEquals("Age in years which must be equal to or greater than zero.", ageAttr.getDescription());
        assertEquals("integer", ageAttr.getType());
        assertEquals("vertriebskunde-services.agree-kundendaten~Person~age", ageAttr.getQualifiedName());

        LineageEventContentforSample.Attribute derivedAgeAttr = attributes.get(3);
        assertEquals("derivedAge", derivedAgeAttr.getDisplayName());
        assertEquals("Derived Age.", derivedAgeAttr.getDescription());
        assertEquals("integer", derivedAgeAttr.getType());
        assertEquals("test-formula", derivedAgeAttr.getFormula());
        assertEquals("vertriebskunde-services.agree-kundendaten~Person~derivedAge", derivedAgeAttr.getQualifiedName());

        LineageEventContentforSample.Attribute derivedAgeAttr2 = attributes.get(4);
        assertEquals("derivedAge2", derivedAgeAttr2.getDisplayName());
        assertEquals("Derived Age.", derivedAgeAttr2.getDescription());
        assertEquals("integer", derivedAgeAttr2.getType());
        assertEquals("test-formula2", derivedAgeAttr2.getFormula());
        assertEquals("vertriebskunde-services.agree-kundendaten~Person~derivedAge2", derivedAgeAttr2.getQualifiedName());
    }

    @Test
    void testEventContentFromHausmeinung() throws IOException, ConnectorCheckedException {
        String textPath = "src/test/resources/Sample-real-anonymous.json";
        LineageEventContentforSample eventContent = getLineageEventContentforSample(textPath);
        assertEquals("Test", eventContent.getProcessTechnicalName());
        List<LineageEventContentforSample.AssetFromJSON> outputAssets = eventContent.getOutputAssets();
        LineageEventContentforSample.AssetFromJSON outputAsset = outputAssets.get(0);
        LineageEventContentforSample.EventTypeFromJSON eventType = outputAsset.getEventTypes().get(0);
        List<LineageEventContentforSample.Attribute> attributes = eventType.getAttributes();
        assertNotNull(attributes);
        assertEquals(3, attributes.size());
        assertEquals(4, attributes.get(2).getNestedAttributes().size());
    }

    @Test
    void testBadlyFormedEventContent() {
        //  assertTrue(false,"Bad File name is " + textPath);
        boolean passed = testBadEvent("src/test/resources/badly-formed-events/notjson.txt",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-001");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/empty.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-001");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/EmptyInput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/InputWithEmptyObject.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-005");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/topid.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/ValidInputNoOutput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-003");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/ValidInputEmptyOutput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-003");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/ValidInputOutputEmptyAsset.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-005");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/ValidOutputNoInput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/ValidOutputEmptyInput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002");
        assertTrue(passed);
        passed = testBadEvent("src/test/resources/badly-formed-events/ValidOutputInputHasOneEmptyObject.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-005");
        assertTrue(passed);


    }

    /**
     * this method is coded this way, because the build machine is sensitive to case wirh the file names, but local
     * testing on the Mac is insensitive to case. If it fails on the build machine we only get the line number where the assert fails
     * so the asserts are done in the calling code, so it is possible to determine which file is incorrect from the build
     * machine pr output.
     *
     * @param textPath    path to file
     * @param expectedMsg expected message
     * @return return which it passed.
     */
    boolean testBadEvent(String textPath, String expectedMsg) {
        boolean passed = true;
        Path path = Paths.get(textPath);

        String content = null;
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            passed = false;

        }
        if (content != null) {
            try {
                new LineageEventContentforSample(content, "unit test badly formed");
                passed = false;
            } catch (ConnectorCheckedException e) {
                assertTrue(e.getMessage().contains(expectedMsg), "File " + textPath + ". Got " + e.getMessage() + ", expected " + expectedMsg);

            }
        }
        return passed;

    }

}


