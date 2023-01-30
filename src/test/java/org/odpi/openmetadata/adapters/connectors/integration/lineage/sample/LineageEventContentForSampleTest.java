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

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *  Test of the event parsing into the EventContent object
 */
public class LineageEventContentForSampleTest
{

    @Test
    void testEventContent() throws IOException, ConnectorCheckedException {
        String textPath = "src/test/resources/Sample1.json";
        LineageEventContentforSample eventContent = getLineageEventContentforSample(textPath);

        assertTrue("TestRes".equals(eventContent.getProcessTechnicalName()));
        assertTrue("1234567890".equals(eventContent.getProcessQualifiedName()));

        List<LineageEventContentforSample.AssetFromJSON> inputAssets = eventContent.getInputAssets();
        List<LineageEventContentforSample.AssetFromJSON> outputAssets = eventContent.getOutputAssets();

        assertTrue(inputAssets.size() ==1);

        LineageEventContentforSample.AssetFromJSON inputAsset =inputAssets.get(0);
        assertTrue(inputAsset.getTypeName().equals("DataSet"));
        assertTrue(inputAsset.getDisplayName().equals("Foo"));
        assertTrue(inputAsset.getQualifiedName().equals("C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37"));
        assertTrue(eventContent.getFormulaForInputAsset(inputAsset.getQualifiedName()).equals("select * from foo;"));
        LineageEventContentforSample.AssetFromJSON outputAsset =outputAssets.get(0);
        assertTrue(outputAsset.getTypeName().equals("KafkaTopic"));
        assertTrue(outputAsset.getDisplayName().equals("Kundendaten"));
        assertTrue(outputAsset.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten"));
        LineageEventContentforSample.EventTypeFromJSON eventType =outputAsset.getEventTypes().get(0);
        assertTrue(eventType.getTechnicalName().equals("Person"));
        assertTrue(eventType.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person"));
        List<LineageEventContentforSample.Attribute> attributes = eventType.getAttributes();
        assertTrue(attributes != null);
        assertTrue(attributes.size() == 5);

        LineageEventContentforSample.Attribute firstNameAttr = attributes.get(0);
        assertTrue(firstNameAttr.getDisplayName().equals("firstName"));
        assertTrue(firstNameAttr.getDescription().equals("The person's first name."));
        assertTrue(firstNameAttr.getType().equals("string"));
        assertTrue(firstNameAttr.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~firstName"));

        LineageEventContentforSample.Attribute lastNameAttr = attributes.get(1);
        assertTrue(lastNameAttr.getDisplayName().equals("lastName"));
        assertTrue(lastNameAttr.getDescription().equals("The person's last name."));
        assertTrue(lastNameAttr.getType().equals("string"));
        assertTrue(lastNameAttr.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~lastName"));

        LineageEventContentforSample.Attribute ageAttr = attributes.get(2);
        assertTrue(ageAttr.getDisplayName().equals("age"));
        assertTrue(ageAttr.getDescription().equals("Age in years which must be equal to or greater than zero."));
        assertTrue(ageAttr.getType().equals("integer"));
        assertTrue(ageAttr.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~age"));

        LineageEventContentforSample.Attribute derivedAgeAttr = attributes.get(3);
        assertTrue(derivedAgeAttr.getDisplayName().equals("derivedAge"));
        assertTrue(derivedAgeAttr.getDescription().equals("Derived Age."));
        assertTrue(derivedAgeAttr.getType().equals("integer"));
        assertTrue(derivedAgeAttr.getFormula().equals("test-formula"));
        assertTrue(derivedAgeAttr.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~derivedAge"));

        LineageEventContentforSample.Attribute derivedAgeAttr2 = attributes.get(4);
        assertTrue(derivedAgeAttr2.getDisplayName().equals("derivedAge2"));
        assertTrue(derivedAgeAttr2.getDescription().equals("Derived Age."));
        assertTrue(derivedAgeAttr2.getType().equals("integer"));
        assertTrue(derivedAgeAttr2.getFormula().equals("test-formula2"));
        assertTrue(derivedAgeAttr2.getQualifiedName().equals("vertriebskunde-services.agree-kundendaten~Person~derivedAge2"));

    }



    @Test
    void testBadlyFormedEventContent()  {
        //  assertTrue(false,"Bad File name is " + textPath);
        boolean passed = testBadEvent( "src/test/resources/badly-formed-events/notjson.txt",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-001");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/empty.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-001");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/EmptyInput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/InputWithEmptyObject.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-005");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/topid.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/ValidInputNoOutput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-003");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/ValidInputEmptyOutput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-003");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/ValidInputOutputEmptyAsset.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-005");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/ValidOutputNoInput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/ValidOutputEmptyInput.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002");
        assertTrue(passed);
        passed = testBadEvent( "src/test/resources/badly-formed-events/ValidOutputInputHasOneEmptyObject.json",
                "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-005");
        assertTrue(passed);


    }

    /**
     * this method is coded this way, because the build machine is sensitive to case wirh the file names, but local
     * testing on the Mac is insensitive to case. If it fails on the build machine we only get the line number where the assert fails
     * so the asserts are done in the calling code, so it is possible to determine which file is incorrect from the build
     * machine pr output.
     * @param textPath path to file
     * @param expectedMsg expected message
     * @return return which it passed.
     */
    boolean testBadEvent(String textPath, String expectedMsg)  {
        boolean passed = true;
        Path path = Paths.get(textPath);

        String content = null;
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            passed =false;

        }
        if (content !=null) {
            try {
                new LineageEventContentforSample(content, "unit test badly formed");
                passed = false;
            } catch (ConnectorCheckedException e) {
                assertTrue(e.getMessage().contains(expectedMsg), "File " + textPath + ". Got " + e.getMessage() + ", expected " + expectedMsg);

            }
        }
        return passed;

    }

    private static LineageEventContentforSample getLineageEventContentforSample(String textPath) throws IOException, ConnectorCheckedException {
        Path path = Paths.get(textPath);
        String content = Files.readString(path);
        return new LineageEventContentforSample(content, "unit test" );
    }

}


