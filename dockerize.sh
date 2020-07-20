#!/bin/bash
set -e
gradle build
docker build -t vahidmostofi/micromuck:dev-v1 --build-arg JAR_FILE=build/libs/micromuck-0.0.1-SNAPSHOT.jar .