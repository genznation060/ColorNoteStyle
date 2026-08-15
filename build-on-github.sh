#!/bin/sh
set -e
gradle wrapper --gradle-version 8.11.1
./gradlew assembleDebug
