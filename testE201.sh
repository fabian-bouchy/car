#!/usr/bin/env bash
git pull
ant jar
java -Xmx2048m -jar bobby.jar server salleE201.json
