/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample;

import org.odpi.openmetadata.frameworks.connectors.ConnectorProviderBase;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.ConnectorType;

import java.util.List;


/**
 * SampleLineageEventReceiverIntegrationProvider is the connector provider for the sample lineage integration connector that extracts asset, schema and process information
 * from events.
 */
public class SampleLineageEventReceiverIntegrationProvider extends ConnectorProviderBase
{
    //org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.SampleLineageEventReceiverIntegrationProvider
    public static final String TOPIC_NAMESPACE = "topicNamespace";
    private static final String connectorTypeGUID          = "f9d2da2d-0cd3-475a-8c38-f2ee50980ee5";
    private static final String connectorTypeQualifiedName = "Lineage event sample Integration Connector";
    private static final String connectorTypeDisplayName   = "Lineage event sample Integration Connector";
    private static final String connectorTypeDescription   = "Connector maintains a list of assets schemas and processes from event payloads.";

    /**
     * Constructor used to initialize the ConnectorProvider with the Java class name of the specific
     * implementation.
     */
    public SampleLineageEventReceiverIntegrationProvider()
    {
        super();

        super.setConnectorClassName(SampleLineageEventReceiverIntegrationConnector.class.getName());

        ConnectorType connectorType = new ConnectorType();
        connectorType.setType(ConnectorType.getConnectorTypeType());
        connectorType.setGUID(connectorTypeGUID);
        connectorType.setQualifiedName(connectorTypeQualifiedName);
        connectorType.setDisplayName(connectorTypeDisplayName);
        connectorType.setDescription(connectorTypeDescription);
        connectorType.setConnectorProviderClassName(this.getClass().getName());
        connectorType.setRecognizedConfigurationProperties(List.of(TOPIC_NAMESPACE));

        super.connectorTypeBean = connectorType;
    }
}
