/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.beans.EventBean;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *  Test of the event parsing into the EventContent object
 */
public class EventBeanTest
{

    @Test
    void testEventProcessor() throws IOException, ConnectorCheckedException, InvalidParameterException, PropertyServerException, UserNotAuthorizedException {
        String textPath = "src/test/resources/Sample1.json";
        Path path = Paths.get(textPath);
        String content = Files.readString(path);
        ObjectMapper om = new ObjectMapper();
        EventBean eb = om.readValue(content, EventBean.class);
        assertTrue(eb != null);
    }
}


