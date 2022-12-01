/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright © 2021 Atruvia AG <opensource@atruvia.de> as contributor to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.ffdc;

import org.odpi.openmetadata.frameworks.auditlog.messagesets.ExceptionMessageDefinition;
import org.odpi.openmetadata.frameworks.auditlog.messagesets.ExceptionMessageSet;

/**
 * The EventSchemaIntegrationConnectorErrorCode is used to define first failure data capture (FFDC) for errors that occur when working with
 * the Event Schema monitor integration connector.  It is used in conjunction with both Checked and Runtime (unchecked) exceptions.
 * <p>
 * The 5 fields in the enum are:
 * <ul>
 *     <li>HTTP Error Code - for translating between REST and JAVA - Typically the numbers used are:</li>
 *     <li><ul>
 *         <li>500 - internal error</li>
 *         <li>400 - invalid parameters</li>
 *         <li>404 - not found</li>
 *         <li>409 - data conflict errors - eg item already defined</li>
 *     </ul></li>
 *     <li>Error Message Id - to uniquely identify the message</li>
 *     <li>Error Message Text - includes placeholder to allow additional values to be captured</li>
 *     <li>SystemAction - describes the result of the error</li>
 *     <li>UserAction - describes how a consumer should correct the error</li>
 * </ul>
 */
public enum LineageEventSampleConnectorErrorCode implements ExceptionMessageSet {

    INVALID_EVENT_JSON(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-001",
            "The {0} integration connector has been sent an event that could not be parsed into json. The badly formed event is:  {1}",
            "The connector requires valid json to be able to process the event.",
            "Supply a well formed json event."),
    INVALID_EVENT_NO_INPUT(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-002",
            "The {0} integration connector has been sent an event that had no top level Input element. The badly formed event is:  {1}",
            "The connector requires events to have at least one top level Input.",
            "Supply a json event with at least one top level Input element."),
    INVALID_EVENT_NO_OUTPUT(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-003",
            "The {0} integration connector has been sent an event that had no top level Output element. The badly formed event is:  {1}",
            "The connector requires events to have at least one top level Output.",
            "Supply a json event with at least one top level Output element."),

    INVALID_EVENT_NO_PROCESS_ID(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-004",
            "The {0} integration connector has been sent an event that has no top level Id element. The badly formed event is:  {1}",
            "The connector requires events to have at least one top level Id.",
            "Supply a json event with at least one top level Id element."),
    INVALID_EVENT_INPUT_ASSET_HAS_NO_ID(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-005",
            "The {0} integration connector has been sent an event with an input asset without an Id element. The badly formed event is:  {1}",
            "The connector requires input assets in events to have an Id.",
            "Supply a json event with input assets that have an Id."),
    INVALID_EVENT_OUTPUT_ASSET_HAS_NO_ID(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-006",
            "The {0} integration connector has been sent an event with an output asset without an Id element. The badly formed event is:  {1}",
            "The connector requires output assets in events to have an Id.",
            "Supply a json event with output assets that have an Id."),
    INVALID_EVENT_NO_INPUT_ASSET_ARRAY(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-007",
            "The {0} integration connector has been sent an event with an Input that is not an array. The badly formed event is:  {1}",
            "The connector requires an array of Input  assets.",
            "Supply a json event with an array of Input assets."),
    INVALID_EVENT_NO_OUTPUT_ASSET_ARRAY(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-008",
            "The {0} integration connector has been sent an event with an Output that is not an array. The badly formed event is:  {1}",
            "The connector requires an array of Output  assets.",
            "Supply a json event with an array of Output assets."),
    INVALID_EVENT_INPUT_NOT_OBJECT(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-009",
            "The {0} integration connector has been sent an event with an Input element that is not an object. The badly formed event is:  {1}",
            "The connector requires Input array content to be Objects.",
            "Supply a json event with an array of Input Objects."),
    INVALID_EVENT_OUTPUT_NOT_OBJECT(400, "LINEAGE_SAMPLE-INTEGRATION-CONNECTOR-400-010",
            "The {0} integration connector has been sent an event with an Output element that is not an object. The badly formed event is:  {1}",
            "The connector requires Output array content to be Objects.",
            "Supply a json event with an array of Output Objects.");



    @SuppressWarnings("ImmutableEnumChecker")
    private final ExceptionMessageDefinition messageDefinition; 


    /**
     * The constructor for EventSchemaIntegrationConnectorErrorCode expects to be passed one of the enumeration rows defined in
     * EventSchemaIntegrationConnectorErrorCode above.   For example:
     * <p>
     * EventSchemaIntegrationConnectorErrorCode   errorCode = EventSchemaIntegrationConnectorErrorCode.ERROR_SENDING_EVENT;
     * <p>
     * This will expand out to the 5 parameters shown below.
     *
     * @param httpErrorCode  error code to use over REST calls
     * @param errorMessageId unique Id for the message
     * @param errorMessage   text for the message
     * @param systemAction   description of the action taken by the system when the error condition happened
     * @param userAction     instructions for resolving the error
     */
    LineageEventSampleConnectorErrorCode(int httpErrorCode, String errorMessageId, String errorMessage, String systemAction, String userAction) {
        this.messageDefinition = new ExceptionMessageDefinition(httpErrorCode,
                errorMessageId,
                errorMessage,
                systemAction,
                userAction);
    }


    /**
     * Retrieve a message definition object for an exception.  This method is used when there are no message inserts.
     *
     * @return message definition object.
     */
    @Override
    public ExceptionMessageDefinition getMessageDefinition() {
        return messageDefinition;
    }


    /**
     * Retrieve a message definition object for an exception.  This method is used when there are values to be inserted into the message.
     *
     * @param params array of parameters (all strings).  They are inserted into the message according to the numbering in the message text.
     * @return message definition object.
     */
    @Override
    public ExceptionMessageDefinition getMessageDefinition(String... params) {
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
        return "StrimziIntegrationConnectorErrorCode{" +
                "messageDefinition=" + messageDefinition +
                '}';
    }
}
