#!/bin/bash

#m2PATH="/Users/aturpin/.m2/repository/org/*"
m2PATH="/Users/aturpin/.m2/repository/org/lei"

echo $m2PATH

javadoc \
    -d /Users/aturpin/src/opi-all/doc \
    --class-path $m2PATH \
    core/src/main/java/org/lei/opi/core/*.java \
    core/src/main/java/org/lei/opi/core/definitions/*.java \
    rgen/src/main/java/org/lei/opi/rgen/*.java
    #-taglet org.lei.opi.core.ParamTaglet \
    #-tagletpath ../../../target/classes/ \
    #-docletpath ../../../target/classes/:$m2PATH \
    #-doclet org.lei.opi.core.DocFormatter \
