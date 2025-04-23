#!/bin/bash
echo "Starting algorithm test with Spring profile: test-algorithm"
./mvnw spring-boot:run -Dspring-boot.run.profiles=test-algorithm
