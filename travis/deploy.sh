#!/usr/bin/env bash

set -ev

test "${TRAVIS_PULL_REQUEST}" == "false"
mvn deploy --settings travis/settings.xml
