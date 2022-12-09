/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright © 2021 Atruvia AG <opensource@atruvia.de> as contributor to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.ffdc;

import org.odpi.openmetadata.frameworks.auditlog.messagesets.AuditLogMessageDefinition;
import org.odpi.openmetadata.frameworks.auditlog.messagesets.AuditLogMessageSet;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLogRecordSeverity;


/**
 * The EventSchemaIntegrationConnectorAuditCode is used to define the message content for the OMRS Audit Log.
 * <p>
 * The 5 fields in the enum are:
 * <ul>
 *     <li>Log Message Id - to uniquely identify the message</li>
 *     <li>Severity - is this an event, decision, action, error or exception</li>
 *     <li>Log Message Text - includes placeholder to allow additional values to be captured</li>
 *     <li>Additional Information - further parameters and data relating to the audit message (optional)</li>
 *     <li>SystemAction - describes the result of the situation</li>
 *     <li>UserAction - describes how a user should correct the situation</li>
 * </ul>
 */
public enum LineageEventSampleEventConnectorAuditCode implements AuditLogMessageSet {
    UNABLE_TO_PROCESS_EVENT("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0001",
            OMRSAuditLogRecordSeverity.EXCEPTION,
            "Exception {0} occurred in Integration connector {1} - the error message was {2}.",
            "The event is not processed. The connector continues to process events.",
            "Ensure that the events written to the topic are well formed."),
    INVALID_PARAMETER_EXCEPTION("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0002",
            OMRSAuditLogRecordSeverity.EXCEPTION,
            "Exception {0} occurred in Integration connector {1} - the error message was {2}.",
            "The system stops processing the event at the point of this exception. The connector continues to process events.",
            "Examine the Exception to determine the cause of the invalid parameter. This is likely to be an Egeria connector logic error or missing validation, if you suspect this ,then raise an issue on Git."),
    PROPERTY_SERVER_EXCEPTION("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0003",
            OMRSAuditLogRecordSeverity.EXCEPTION,
            "Exception {0} occurred in Integration connector {1} - the error message was {2}.",
            "The system stops processing the event at the point of this exception. The connector continues to process events.",
            "Examine the Exception to determine the cause of the property server error."),
    USER_NOT_AUTHORISED_EXCEPTION("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0004",
            OMRSAuditLogRecordSeverity.EXCEPTION,
            "Exception {0} occurred in Integration connector {1} - the error message was {2}.",
            "The system stops processing the event at the point of this exception. The connector continues to process events.",
            "Supply a valid userId in the configuration."),
    UNEXPECTED_EXCEPTION("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0005",
            OMRSAuditLogRecordSeverity.EXCEPTION,
            "Unexpected Exception {0} occurred in Integration connector {1} - the error message was {2}.",
            "The system stops processing the event at the point of this exception. The connector continues to process events.",
            "Look into the cause of the Exception. Raise an issue against Egeria if you suspect Egeria might be at fault."),

    PROCESSING_EVENT("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0006",
            OMRSAuditLogRecordSeverity.INFO,
            "Event to be processed  {0}.",
            "The system is about to process the event",
            "Nothing - this is for awareness that an event is being processed."),

    PROCESSED_EVENT_SUCCESSFULLY("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0007",
            OMRSAuditLogRecordSeverity.INFO,
            "Event successfully processed",
            "The system has successfully processed the event",
            "Nothing - this is for awareness that an event has been processed."),
    CREATE_ASSET_ATTEMPTED_WITH_EXISTING_QUALIFIEDNAME("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0008",
            OMRSAuditLogRecordSeverity.EXCEPTION,
            "Attempt to create an asset failed as the qualified name {0} already exists and is not associated with an asset",
            "The system stops processing this event, but carries on processing subsequent events.",
            "Investigate the reason why there is an entity with the existing qualifiedName."),
    UPDATE_ASSET_FAILED_OWNED_BY_DIFFERENT_EXTERNAL_SOURCE("LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-0009",
            OMRSAuditLogRecordSeverity.EXCEPTION,
            "Method {0} running on behalf of external source {1} ({2}) is unable to modify {3} instance {4} because " +
            "it is has metadata provenance of {5} with an externalSourceGUID of {6} and an externalSourceName of {7}",
            "The asset update does not occur and the system carries on processing the event.",
            "Investigate the reason why there is an attempt to update an asset that is owned by a different external source."),

    ;


    private final String logMessageId;
    private final OMRSAuditLogRecordSeverity severity;
    private final String logMessage;
    private final String systemAction;
    private final String userAction;


    /**
     * The constructor for EventSchemaIntegrationConnectorAuditCode expects to be passed one of the enumeration rows defined in
     * EventSchemaIntegrationConnectorAuditCode above.   For example:
     * <p>
     * EventSchemaIntegrationConnectorAuditCode   auditCode = EventSchemaIntegrationConnectorAuditCode.SERVER_NOT_AVAILABLE;
     * <p>
     * This will expand out to the 4 parameters shown below.
     *
     * @param messageId    - unique Id for the message
     * @param severity     - severity of the message
     * @param message      - text for the message
     * @param systemAction - description of the action taken by the system when the condition happened
     * @param userAction   - instructions for resolving the situation, if any
     */
    LineageEventSampleEventConnectorAuditCode(String messageId,
                                              OMRSAuditLogRecordSeverity severity,
                                              String message,
                                              String systemAction,
                                              String userAction) {
        this.logMessageId = messageId;
        this.severity = severity;
        this.logMessage = message;
        this.systemAction = systemAction;
        this.userAction = userAction;
    }


    /**
     * Retrieve a message definition object for logging.  This method is used when there are no message inserts.
     *
     * @return message definition object.
     */
    @Override
    public AuditLogMessageDefinition getMessageDefinition() {
        return new AuditLogMessageDefinition(logMessageId,
                severity,
                logMessage,
                systemAction,
                userAction);
    }


    /**
     * Retrieve a message definition object for logging.  This method is used when there are values to be inserted into the message.
     *
     * @param params array of parameters (all strings).  They are inserted into the message according to the numbering in the message text.
     * @return message definition object.
     */
    @Override
    public AuditLogMessageDefinition getMessageDefinition(String... params) {
        AuditLogMessageDefinition messageDefinition = new AuditLogMessageDefinition(logMessageId,
                severity,
                logMessage,
                systemAction,
                userAction);
        messageDefinition.setMessageParameters(params);
        return messageDefinition;
    }


    /**
     * JSON-style toString
     *
     * @return string of property names and values for this enum
     */
    @Override
    public String toString() {
        return "StrimziIntegrationConnectorAuditCode{" +
                "logMessageId='" + logMessageId + '\'' +
                ", severity=" + severity +
                ", logMessage='" + logMessage + '\'' +
                ", systemAction='" + systemAction + '\'' +
                ", userAction='" + userAction + '\'' +
                '}';
    }
}
