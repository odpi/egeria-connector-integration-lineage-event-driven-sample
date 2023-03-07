FROM quay.io/ibmgaragecloud/gradle:jdk11 as build

ARG GRADLE_OPTS=""

# Copy files
COPY gradle gradle
COPY settings.gradle .
COPY gradlew .
COPY build.gradle .
COPY src src
COPY gradle.properties .

# Build application and test it
RUN ./gradlew assemble --no-daemon && \
	./gradlew testClasses --no-daemon

FROM quay.io/odpi/egeria:latest

ARG CONNECTOR_NAME="egeria-connector-integration-lineage-sample"
ARG CONNECTOR_VERSION="1.0-SNAPSHOT"
ARG CONNECTOR_DESCRIPTION="Egeria with the sample integration lineage event driven connector"
ARG CONNECTOR_DOCUMENTATION="https://github.com/odpi/egeria-connector-integration-lineage-event-driven-sample"

# Labels from https://github.com/opencontainers/image-spec/blob/master/annotations.md#pre-defined-annotation-keys (with additions prefixed    ext)
# We should inherit all the base labels from the egeria image and only overwrite what is necessary.
LABEL org.opencontainers.image.description = "${CONNECTOR_DESCRIPTION}"
LABEL org.opencontainers.image.documentation = "${CONNECTOR_DOCUMENTATION}"

ENV CONNECTOR_VERSION ${CONNECTOR_VERSION}

# This assumes we only have one uber jar (ensure old versions cleaned out beforehand). Avoids having to pass connector version
COPY --from=build /home/gradle/build/libs/${CONNECTOR_NAME}-${CONNECTOR_VERSION}.jar /deployments/server/lib/
