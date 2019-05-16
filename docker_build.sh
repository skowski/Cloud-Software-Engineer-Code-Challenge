#!/bin/bash

docker volume create --name gradle-cache
docker run --rm -v gradle-cache:/home/gradle/.gradle -v "$PWD":/home/gradle/project -w /home/gradle/project gradle:4.10.2-jdk8-alpine gradle shadowJar
ls -ltrh ./build/libs
