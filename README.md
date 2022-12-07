<!-- SPDX-License-Identifier: CC-BY-4.0 -->
<!-- Copyright Contributors to the Egeria project. -->

# Sample Egeria Lineage Event Receiver Integration Connector

See https://egeria-project.org for the main Egeria Documentation.

This repo holds one sample [integration connector](https://egeria-project.org/concepts/integration-connector/?h=integration+conne) that shows how 
to listen to a topic for json payloads. For an example of the json, see the bottom of this README. This integration connector is an extension of the 
[Lineage Integrator Open Metadata Integration Service (OMIS)](https://egeria-project.org/services/omis/lineage-integrator/overview/), where it
catalogues lineage information from a third party technology, including process,
schemas and the data assets they are working with. See [here](https://github.com/odpi/egeria-connector-integration-lineage-event-driven-sample/tree/main/src/test/resources)
for some sample json events.

## Postman
There is a sample postman file [here](https://github.com/odpi/egeria-connector-integration-lineage-event-driven-sample/blob/main/postman/Egeria%20Integration%20Connector%20sample%20event%20Lineage.postman_collection.json) to
configure and start this connector.

## Test Utility
This is a java application that produces events for testing [here](https://github.com/odpi/egeria-connector-integration-lineage-event-driven-sample/blob/main/src/test/java/org/odpi/openmetadata/adapters/connectors/integration/lineage/utils/EventProducerUtility.java). It allows the user to easily submit any of the json files used in the junit.
It is built with the junit tests using the top level gradle file.

## Event json 

The event json describes a Process , with a qualified name ("Id") and display name ("Name").
Then there is an Input array of assets and an output array of assets. In the input array are datasets, the output are Kafka topics.
The Output also describes an event schema.


### Assumptions
- No deletion of assets or processes. Lineage is always adding these
- Where effectivity dates are specified - the current time is used
- Only doing asset design lineage, no column lineage or operwtional lineage.
- All finds are done by qualifiedName - if more than one element is returned - we use the first. 
- Catalog the assets, schema and process if required. This can result in 
  - deletion, creation and / or updates of schemaAttributes
  - deletion and creation of the Event type
- Save the lineage flow Assets(DataSet) -> Process -> Assets(KafkaTopic)
- change of the input and output assets results in removals of old dataflows as well as adding the new dataflows.
- All updates are replacements not merge- e.g. schema attributes are replaced not merged
- there is no support for update schematype (EventType) as it only has a title - which is used to derive the qualified name - so there is nothing to update.
- there is no description for schematypes or assets
- AssetManager is not used. So AssetManagerIsHome is false on call create calls.
- all updates are replacements not merges.
- if an event comes in with different input assets and or output assets, we do not remove the existing lineage relationships.
- The lineage relationships shown here as DataFlow relationships. In practice, it depends on
what the process is doing as to which lineage relationships should be created.
- The Dataflow qualified name is set to null. This is not appropriate if multiple 
supply chains create a lineage relationship between an asset and process. 
- Only one EventType is supported under the Topic. [https://github.com/odpi/egeria/issues/7134](https://github.com/odpi/egeria/issues/7134) has been raised to allow
support of EventTypeLists. This sample would need to be enhanced to make use of these new APIs.  
### Questions
- dataflow relationships between the assets and process are [multi-link](https://egeria-project.org/concepts/uni-multi-link/?h=multi+link#multi-link-relationships)
So have a qualified name - question on this - can it be null if we know there is only one?
- effectivity should be use current time or null (effective forever)
### Example json
{
"Id": "1234567890",
"Name": "TestRes",
"Description": "TestResource",
"Bounded Context": "TestResource",
"Team": "ITG",
"Input": [{
"Id": "C6B7B1B717C840F686EE2426241ED18CE1D053019534F03495E8CD644976FA37",
"Name": "Foo",
"SQL": "select * from foo;"
}],
"Output": [{
"Id": "vertriebskunde-services.agree-kundendaten",
"Name": "Kundendaten",
"Schema-type": "json-schema",
"Schema": {
"$id": "https://example.com/person.schema.json",
"$schema": "https://json-schema.org/draft/2020-12/schema",
"title": "Person",
"type": "object",
"properties": {
"firstName": {
"type": "string",
"description": "The person's first name."
},
"lastName": {
"type": "string",
"description": "The person's last name."
},
"age": {
"description": "Age in years which must be equal to or greater than zero.",
"type": "integer"
}
}
}
}]
}
