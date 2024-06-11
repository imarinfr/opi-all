#!/bin/bash

# Usage: run_monitor.sh [D(isplay) | I(moVifa) | E(cho) ]

if [ $# == 0 ] ; then
    machine=""
else
    case $1 in
      D | Display)
        machine="--cli 50001 Display"
        ;;
      I | ImoVifa)
        machine="--cli 50001 ImoVifa "
        ;;
      E | Echo)
        machine="--cli 50001 Echo"
        ;;
      *)
        machine=""
        ;;
    esac
fi


#mvn clean -f /Users/aturpin/src/opi-all/monitor/pom.xml
#mvn install -f /Users/aturpin/src/opi-all/monitor/pom.xml


CP="
$M2_HOME/repository/com/anaptecs/jeaf/owalibs/org.apache.commons.cli/4.3.1/org.apache.commons.cli-4.3.1.jar:
$M2_HOME/repository/org/reflections/reflections/0.9.12/reflections-0.9.12.jar:
$M2_HOME/repository/org/javassist/javassist/3.26.0-GA/javassist-3.26.0-GA.jar:
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
$M2_HOME/repository/org/slf4j/slf4j-api/2.0.3/slf4j-api-2.0.3.jar:
$M2_HOME/repository/org/lei/opi/core/0.2.0/core-0.2.0.jar:
$M2_HOME/repository/org/lei/opi/monitor/0.2.0/monitor-0.2.0.jar:
$M2_HOME/repository/es/optocom/jovp/jovp/0.1.0-SNAPSHOT/jovp-0.1.0-SNAPSHOT.jar
"
#$M2_HOME/repository/com/nativelibs4java/bridj/0.7.0/bridj-0.7.0.jar:
#$M2_HOME/repository/com/github/sarxos/webcam-capture/0.3.12/webcam-capture-0.3.12.jar:

CP=$(echo $CP | tr -d ' ')

echo $CP 

(cd .. ;  # change directory to where opi_settings.json live

    /usr/bin/env java\
        -cp $CP\
        --module-path "/Library/Java/javafx-sdk-20/lib"\
        --add-modules javafx.controls,javafx.fxml,javafx.swing\
        org.lei.opi.monitor.Monitor $machine
        #org.lei.opi.monitor.Monitor --cli 50001 Display
        #org.lei.opi.monitor.Monitor --cli 50001 ImoVifa
        #org.lei.opi.monitor.Monitor --cli 50001 Echo
)
