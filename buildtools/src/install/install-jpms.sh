#!/usr/bin/env bash
./gradlew --version
./gradlew --stacktrace --warning-mode=all templates:build
./gradlew --stacktrace --warning-mode=all generator:build