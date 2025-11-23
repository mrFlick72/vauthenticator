#!/bin/bash
mvn clean install -DskipTests

cd src/main/frontend
npm run-script build
