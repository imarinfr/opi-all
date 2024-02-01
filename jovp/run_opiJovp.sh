#!/bin/bash

CORE="$M2_HOME/repository/org/lei/opi/core/0.2.0/core-0.2.0.jar"
OPIJ="$M2_HOME/repository/org/lei/opi/opiJovp/0.2.0/opiJovp-0.2.0.jar"
JOVP="$M2_HOME/repository/es/optocom/jovp/jovp/0.1.0-SNAPSHOT/jovp-0.1.0-SNAPSHOT.jar"

#mvn install -f /Users/aturpin/src/opi-all/jovp/pom.xml
#mvn install -f /Users/aturpin/src/opi-all/core/pom.xml

#CP=`find $M2_HOME -name "*.jar" | paste -sd ":" - `
#CP="$CP".

#echo $CP

j="/Library/Java/JavaVirtualMachines/jdk-17.0.5.jdk/Contents/Home/bin/java"


CP="$CORE:
    $OPIJ:
    $JOVP:
    $M2_HOME/repository/org/reflections/reflections/0.9.12/reflections-0.9.12.jar:
    $M2_HOME/repository/org/javassist/javassist/3.26.0-GA/javassist-3.26.0-GA.jar:
    $M2_HOME/repository/org/openjfx/javafx-controls/18.0.2/javafx-controls-18.0.2.jar:
    $M2_HOME/repository/org/openjfx/javafx-controls/18.0.2/javafx-controls-18.0.2-mac-aarch64.jar:
    $M2_HOME/repository/org/openjfx/javafx-graphics/18.0.2/javafx-graphics-18.0.2.jar:
    $M2_HOME/repository/org/openjfx/javafx-graphics/18.0.2/javafx-graphics-18.0.2-mac-aarch64.jar:
    $M2_HOME/repository/org/openjfx/javafx-base/18.0.2/javafx-base-18.0.2.jar:
    $M2_HOME/repository/org/openjfx/javafx-base/18.0.2/javafx-base-18.0.2-mac-aarch64.jar:
    $M2_HOME/repository/org/openjfx/javafx-fxml/18.0.2/javafx-fxml-18.0.2.jar:
    $M2_HOME/repository/org/openjfx/javafx-fxml/18.0.2/javafx-fxml-18.0.2-mac-aarch64.jar:
    $M2_HOME/repository/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar:
    $M2_HOME/repository/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar:
    $M2_HOME/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-assimp/3.3.1/lwjgl-assimp-3.3.1.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-glfw/3.3.1/lwjgl-glfw-3.3.1.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-stb/3.3.1/lwjgl-stb-3.3.1.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-shaderc/3.3.1/lwjgl-shaderc-3.3.1.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-vulkan/3.3.1/lwjgl-vulkan-3.3.1.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1-natives-macos-arm64.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-assimp/3.3.1/lwjgl-assimp-3.3.1-natives-macos-arm64.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-glfw/3.3.1/lwjgl-glfw-3.3.1-natives-macos-arm64.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-stb/3.3.1/lwjgl-stb-3.3.1-natives-macos-arm64.jar:
    $M2_HOME/repository/org/lwjgl/lwjgl-shaderc/3.3.1/lwjgl-shaderc-3.3.1-natives-macos-arm64.jar:
    $M2_HOME/repository/org/joml/joml/1.10.4/joml-1.10.4.jar:
    $M2_HOME/repository/io/github/java-native/jssc/2.9.4/jssc-2.9.4.jar:
    $M2_HOME/repository/org/scijava/native-lib-loader/2.3.6/native-lib-loader-2.3.6.jar:
    $M2_HOME/repository/org/slf4j/slf4j-simple/2.0.3/slf4j-simple-2.0.3.jar:
    $M2_HOME/repository/org/slf4j/slf4j-api/2.0.3/slf4j-api-2.0.3.jar
"

CP=$(echo $CP | tr -d '\n' | tr -d ' ')

ARGS="-Dorg.lwjgl.vulkan.libname=$VULKAN_SDK/lib/libvulkan.dylib -XstartOnFirstThread"

echo "$j $ARGS -cp $CP org.lei.opi.jovp.OpiJovp 51234"
(cd .. ;
$j $ARGS -cp $CP org.lei.opi.jovp.OpiJovp 51234 
)
