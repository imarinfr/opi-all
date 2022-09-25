#!/bin/bash

m2PATH="\
/Users/imarinfr/.m2/repository/org/reflections/reflections/0.9.12/reflections-0.9.12.jar:\
/Users/imarinfr/.m2/repository/es/optocom/jovp/jovp/0.1.0-SNAPSHOT/jovp-0.1.0-SNAPSHOT.jar:\
/Users/imarinfr/.m2/repository/com/google//code/gson/gson/2.8.6/gson-2.8.6.jar"

javadoc \
    -d /Users/imarinfr/src/opi-all/doc \
    --class-path $m2PATH \
    core/src/main/java/org/lei/opi/core/*.java \
    core/src/main/java/org/lei/opi/core/definitions/*.java \
    rgen/src/main/java/org/lei/opi/rgen/*.java
    #-taglet org.lei.opi.core.ParamTaglet \
    #-tagletpath ../../../target/classes/ \
    #-docletpath ../../../target/classes/:$m2PATH \
    #-doclet org.lei.opi.core.DocFormatter \
