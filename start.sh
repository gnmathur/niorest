#!/bin/bash

export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=128m"
mvn compile && mvn exec:java -Dexec.mainClass="com.gmathur.niorest.NioRest" -DjvmArgs="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.rmi.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false" 2>&1

