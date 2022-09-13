#!/bin/bash


m2PATH="\
/Users/aturpin/.m2/repository/org/reflections/reflections/0.9.12/reflections-0.9.12.jar:\
/Users/aturpin/.m2/repository/com/google//code/gson/gson/2.8.6/gson-2.8.6.jar"

(cd opi-core/src/main/java ; 
javadoc \
    -d /Users/aturpin/src/opi-all/doc \
    --class-path $m2PATH \
    -taglet org.lei.opi.core.ParamTaglet \
    -tagletpath ../../../target/classes/ \
    org/lei/opi/core/*.java org/lei/opi/core/structures/*.java
    #-docletpath ../../../target/classes/:$m2PATH \
    #-doclet org.lei.opi.core.DocFormatter \
)
