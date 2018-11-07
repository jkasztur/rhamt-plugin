#!/usr/bin/env bash

mvnDebug -Dmaven.test.skip=true -DskipTests=true clean hpi:run
