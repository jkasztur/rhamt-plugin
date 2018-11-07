#!/usr/bin/env bash

mvn -Dmaven.test.skip=true -DskipTests=true clean hpi:run
