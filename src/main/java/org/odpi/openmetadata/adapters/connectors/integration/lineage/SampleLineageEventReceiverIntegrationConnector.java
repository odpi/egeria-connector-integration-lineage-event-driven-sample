/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage;

import org.odpi.openmetadata.adapters.connectors.integration.openlineage.OpenLineageEventReceiverIntegrationConnector;
import org.odpi.openmetadata.adapters.connectors.integration.openlineage.ffdc.OpenLineageIntegrationConnectorAuditCode;
import org.odpi.openmetadata.frameworks.connectors.Connector;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.properties.ConnectionProperties;
import org.odpi.openmetadata.integrationservices.lineage.connector.LineageIntegratorContext;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicConnector;

import java.util.HashMap;
import java.util.Map;


public class SampleLineageEventReceiverIntegrationConnector extends OpenLineageEventReceiverIntegrationConnector {
    private final Map<String, OpenMetadataTopicConnector> topicConnectors = new HashMap<>();

    private LineageIntegratorContext                myContext       = null;

    public SampleLineageEventReceiverIntegrationConnector() {

    }

    @Override
    public synchronized void start() throws ConnectorCheckedException {
        super.start();

        final String methodName = "start";

        myContext = super.getContext();

        if (myContext != null)
        {
            // TODO do we need anything else from the config
            if (embeddedConnectors != null)
            {
                for (Connector embeddedConnector : embeddedConnectors)
                {
                    if (embeddedConnector instanceof OpenMetadataTopicConnector)
                    {
                        /*
                         * Register this connector as a listener of the event bus connector.
                         */
                        OpenMetadataTopicConnector topicConnector = (OpenMetadataTopicConnector)embeddedConnector;
                        topicConnector.registerListener(this);

                        org.odpi.openmetadata.frameworks.connectors.properties.ConnectionProperties connectionProperties = topicConnector.getConnection();

                        if (connectionProperties != null)
                        {
                            org.odpi.openmetadata.frameworks.connectors.properties.EndpointProperties endpoint = connectionProperties.getEndpoint();

                            if (endpoint != null)
                            {
                                topicConnectors.put(endpoint.getAddress(), topicConnector);
                            }
                        }
                    }
                }
            }

            for (String topicName : topicConnectors.keySet())
            {
                OpenMetadataTopicConnector topicConnector = topicConnectors.get(topicName);
                ConnectionProperties topicConnection = topicConnector.getConnection();

                /*
                 * Record the configuration
                 */
                if (auditLog != null)
                {
                    auditLog.logMessage(methodName,
                            OpenLineageIntegrationConnectorAuditCode.KAFKA_RECEIVER_CONFIGURATION.getMessageDefinition(connectorName,
                                    topicName,
                                    topicConnection.getConnectionName()));
                }

                topicConnector.start();
            }
        }
    }
    @Override
    synchronized public void processEvent(String event)
    {
        if (myContext != null)
        {
             SampleLineageEventProcessor eventProcessor = new SampleLineageEventProcessor(myContext, auditLog);
             LineageEventContentforSample eventContent = new LineageEventContentforSample(event);
             eventProcessor.processEvent(eventContent);
        }
    }
}
