#!/bin/bash


m2PATH="\
/Users/aturpin/.m2/repository/org/reflections/reflections/0.9.12/reflections-0.9.12.jar:\
/Users/aturpin/.m2/repository/com/google//code/gson/gson/2.8.6/gson-2.8.6.jar"

javadoc \
    -d /Users/aturpin/src/opi-all/doc \
    --class-path $m2PATH \
    opi-core/src/main/java/org/lei/opi/core/*.java \
    opi-core/src/main/java/org/lei/opi/core/structures/*.java \
    opi-rgen/src/main/java/org/lei/opi/rgen/*.java
    #-taglet org.lei.opi.core.ParamTaglet \
    #-tagletpath ../../../target/classes/ \
    #-docletpath ../../../target/classes/:$m2PATH \
    #-doclet org.lei.opi.core.DocFormatter \
