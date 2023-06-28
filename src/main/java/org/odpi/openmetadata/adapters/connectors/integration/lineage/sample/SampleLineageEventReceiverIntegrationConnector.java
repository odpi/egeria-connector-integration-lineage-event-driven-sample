/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample;

import org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.ffdc.LineageEventSampleEventConnectorAuditCode;
import org.odpi.openmetadata.frameworks.connectors.Connector;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.properties.ConnectionProperties;
import org.odpi.openmetadata.integrationservices.lineage.connector.LineageIntegratorConnector;
import org.odpi.openmetadata.integrationservices.lineage.connector.LineageIntegratorContext;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicConnector;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicListener;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an Integration connectors that processes events from a Kafka topic. Those events
 * are json, that describes design lineage flows. The output asset of the flow also has schema content.
 * This connector issues appropriate creates, updates and deletes to ensure that the metadata in Egeria matches
 * the latest values as specified in the event payload.
 */
public class SampleLineageEventReceiverIntegrationConnector extends LineageIntegratorConnector implements OpenMetadataTopicListener {
    private final Map<String, OpenMetadataTopicConnector> topicConnectors = new HashMap<>();

    private LineageIntegratorContext myContext = null;
    private String topicNamespace = "";

    public SampleLineageEventReceiverIntegrationConnector() {

    }

    @Override
    public synchronized void start() throws ConnectorCheckedException {
        super.start();

        final String methodName = "start";

        if( this.connectionProperties != null  ) {
            Map<String, Object> configurationProps = this.connectionProperties.getConfigurationProperties();
            if( configurationProps != null ) {
                this.topicNamespace = configurationProps.getOrDefault(SampleLineageEventReceiverIntegrationProvider.TOPIC_NAMESPACE, "").toString();
            }
        }

        myContext = super.getContext();

        if (myContext != null) {
            if (embeddedConnectors != null) {
                for (Connector embeddedConnector : embeddedConnectors) {
                    if (embeddedConnector instanceof OpenMetadataTopicConnector) {
                        /*
                         * Register this connector as a listener of the event bus connector.
                         */
                        OpenMetadataTopicConnector topicConnector = (OpenMetadataTopicConnector) embeddedConnector;
                        topicConnector.registerListener(this);

                        org.odpi.openmetadata.frameworks.connectors.properties.ConnectionProperties connectionProperties = topicConnector.getConnection();

                        if (connectionProperties != null) {
                            org.odpi.openmetadata.frameworks.connectors.properties.EndpointProperties endpoint = connectionProperties.getEndpoint();

                            if (endpoint != null) {
                                topicConnectors.put(endpoint.getAddress(), topicConnector);
                            }
                        }
                    }
                }
            }

            for (String topicName : topicConnectors.keySet()) {
                OpenMetadataTopicConnector topicConnector = topicConnectors.get(topicName);
                ConnectionProperties topicConnection = topicConnector.getConnection();

                /*
                 * Record the configuration
                 */
                if (auditLog != null) {
                    auditLog.logMessage(methodName,
                            LineageEventSampleEventConnectorAuditCode.KAFKA_RECEIVER_CONFIGURATION.getMessageDefinition(connectorName,
                                    topicName,
                                    topicConnection.getConnectionName()));
                }

                topicConnector.start();
            }
        }
    }

    /**
     * No function needed in refresh()
     */
    @Override
    public void refresh() {
    }

    @Override
    synchronized public void processEvent(String event) {
        String methodName = "processEvent";
        if (auditLog != null) {
            auditLog.logMessage(methodName, LineageEventSampleEventConnectorAuditCode.PROCESSING_EVENT.getMessageDefinition(event));
        }
        if (myContext != null) {
            try {
                LineageEventContentforSample eventContent = new LineageEventContentforSample(event, connectorName, topicNamespace);
                SampleLineageEventProcessor eventProcessor = new SampleLineageEventProcessor(myContext, auditLog, connectorName);
                eventProcessor.processEvent(eventContent);
                if (auditLog != null) {
                    auditLog.logMessage(methodName, LineageEventSampleEventConnectorAuditCode.PROCESSED_EVENT_SUCCESSFULLY.getMessageDefinition());
                }
            } catch (ConnectorCheckedException error) {
                if (auditLog != null) {
                    auditLog.logException(methodName,
                            LineageEventSampleEventConnectorAuditCode.UNABLE_TO_PROCESS_EVENT.getMessageDefinition(
                                    error.getClass().getName(),
                                    connectorName,
                                    error.getMessage()), error);
                }
            }
        }
    }
}
