#!/bin/bash

#
# cp all the jar files into one place for distn
#

targetDir=/Users/aturpin/src/opi-dist

#cp $M2_HOME/repository/com/anaptecs/jeaf/owalibs/org.apache.commons.cli/4.3.1/org.apache.commons.cli-4.3.1.jar $targetDir
#cp $M2_HOME/repository/org/reflections/reflections/0.9.12/reflections-0.9.12.jar $targetDir
#cp $M2_HOME/repository/org/javassist/javassist/3.26.0-GA/javassist-3.26.0-GA.jar $targetDir
#cp $M2_HOME/repository/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar $targetDir
#cp $M2_HOME/repository/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar $targetDir
#cp $M2_HOME/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-assimp/3.3.1/lwjgl-assimp-3.3.1.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-glfw/3.3.1/lwjgl-glfw-3.3.1.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-stb/3.3.1/lwjgl-stb-3.3.1.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-shaderc/3.3.1/lwjgl-shaderc-3.3.1.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-vulkan/3.3.1/lwjgl-vulkan-3.3.1.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1-natives-macos-arm64.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-assimp/3.3.1/lwjgl-assimp-3.3.1-natives-macos-arm64.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-glfw/3.3.1/lwjgl-glfw-3.3.1-natives-macos-arm64.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-stb/3.3.1/lwjgl-stb-3.3.1-natives-macos-arm64.jar $targetDir
#cp $M2_HOME/repository/org/lwjgl/lwjgl-shaderc/3.3.1/lwjgl-shaderc-3.3.1-natives-macos-arm64.jar $targetDir
#cp $M2_HOME/repository/org/joml/joml/1.10.4/joml-1.10.4.jar $targetDir
#cp $M2_HOME/repository/io/github/java-native/jssc/2.9.4/jssc-2.9.4.jar $targetDir
#cp $M2_HOME/repository/org/scijava/native-lib-loader/2.3.6/native-lib-loader-2.3.6.jar $targetDir
#cp $M2_HOME/repository/org/slf4j/slf4j-simple/2.0.3/slf4j-simple-2.0.3.jar $targetDir
#cp $M2_HOME/repository/org/slf4j/slf4j-api/2.0.3/slf4j-api-2.0.3.jar $targetDir
#cp $M2_HOME/repository/org/lei/opi/core/0.2.0/core-0.2.0.jar $targetDir

cp $M2_HOME/repository/org/lei/opi/monitor/0.2.0/monitor-0.2.0.jar $targetDir/opi-monitor-3.0.jar
cp $M2_HOME/repository/es/optocom/jovp/jovp/0.1.0-SNAPSHOT/jovp-0.1.0-SNAPSHOT.jar $targetDir

cp ../opi_settings.json $targetDir
