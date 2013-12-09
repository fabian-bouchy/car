#!/usr/bin/env bash
ant jar
java -Xmx2048m -jar bobby.jar server salleE200.json
