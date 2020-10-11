# Multi-stage Docker file

ARG JDK8_DOWNLOAD_URL=https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u265-b01/OpenJDK8U-jdk_x64_linux_hotspot_8u265b01.tar.gz

# Stage 1
# Get Maven base image from ICE Nexus2
FROM maven:1.8.0 AS build

# Copy the application files
WORKDIR /app
COPY src src
COPY pom.xml lombok.config ./

# Build the project
RUN mvn -B -Dmaven.test.skip=true -Dmaven.test.failure.ignore=true -Dgithook.plugin.skip=true package

# Stage 2
FROM ubuntu:16.04
COPY proxy.conf /etc/apt/apt.conf.d/proxy.conf

# Copy the build artifacts
WORKDIR /app
COPY --from=build /app/target/*.jar .

# Expose the application ports
ARG APP_PORT=${APP_PORT:-8080}
ARG MGMT_PORT=${MGMT_PORT:-8081}
EXPOSE ${APP_PORT} ${MGMT_PORT}

# Set any custom options to pass to `java`
# ENV JAVA_OPTS=

# The default entrypoint should work for most apps: it updates
# the CAs and runs the first JAR it finds in the current directory.
# If you need to override this behavior, uncomment the below line.
# COPY entrypoint.sh ./
