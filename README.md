<!-- SPDX-License-Identifier: CC-BY-4.0 -->
<!-- Copyright Contributors to the Egeria project. -->

# Egeria Open Lineage Event Receiver Integration Connector

# Still being developed - not tested or fully functional.

See https://egeria-project.org for the main Egeria Documentation.

This repo holds one sample [integration connector](https://egeria-project.org/concepts/integration-connector/?h=integration+conne) that shows how 
to listen to a topic for json payloads. For an example of the json, see the bottom of this README. This integration connector is an extension of the 
[Open Lineage Event Receiver Integration Connector](https://egeria-project.org/connectors/integration/open-lineage-event-receiver-integration-connector/).
It does not process open lineage events, instead it process json events in the form describd at the bottom of the readme.





## Event json 

The event json describes a Process , with a qualified name ("Id") and display name ("Name").
Then there is an Input array of assets and an output array of assets. In the input array are datasets, the output are Kafka topics.
The Output also describes an event schema.


### Assumptions
- no deletion of assets or processes. Lineage is always adding these
- finds are done on the qualified name and the first element returned is used.
- Only doing asset design lineage, no column lineage
- Catalog the assets, schema and process if required. This can result in 
  - deletion, creation and / or updates of EventTypes and schemaAttributes
- save the lineage flow Assets(DataSet) -> Process -> Assets(KafkaTopic)
### Questions
- dataflow relationships between the assets and process are [multi-link](https://egeria-project.org/concepts/uni-multi-link/?h=multi+link#multi-link-relationships)
So have a qualified name - question on this - can it be null if we know there is only one?
- Should be use EventSets to model multiple EventTypes?
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
