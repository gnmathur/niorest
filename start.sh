#!/bin/bash

mvn compile && mvn exec:java -Dexec.mainClass="com.gmathur.niorest.NioRest" 2>&1

