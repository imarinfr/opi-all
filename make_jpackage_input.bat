@echo off
:: Collect all files for jpacakge commands into input folders
:: and then run jpackage

set input="C:\Users\imo vifa\Desktop\opi3\input"

REM --------- OpiJovp ----------------------------
echo Creating OpiJovp app
if exist OpiJovp\  (
    echo ********* OpiJovp folder exists, skipping
    goto opimonitor
)
 
rmdir %input% /S /Q
mkdir %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl\3.3.1\lwjgl-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-assimp\3.3.1\lwjgl-assimp-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-glfw\3.3.1\lwjgl-glfw-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-stb\3.3.1\lwjgl-stb-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-shaderc\3.3.1\lwjgl-shaderc-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-vulkan\3.3.1\lwjgl-vulkan-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl\3.3.1\lwjgl-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-assimp\3.3.1\lwjgl-assimp-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-glfw\3.3.1\lwjgl-glfw-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-stb\3.3.1\lwjgl-stb-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-shaderc\3.3.1\lwjgl-shaderc-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\javacpp\1.5.5\javacpp-1.5.5.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\javacpp\1.5.5\javacpp-1.5.5-windows-x86_64.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\javacv\1.5.5\javacv-1.5.5.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\opencv\4.5.1-1.5.5\opencv-4.5.1-1.5.5.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\opencv\4.5.1-1.5.5\opencv-4.5.1-1.5.5-windows-x86_64.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\openblas-platform\0.3.13-1.5.5\openblas-platform-0.3.13-1.5.5.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\openblas\0.3.13-1.5.5\openblas-0.3.13-1.5.5.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\openblas\0.3.13-1.5.5\openblas-0.3.13-1.5.5-windows-x86_64.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lei\opi\monitor\0.2.0\monitor-0.2.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\lei\opi\core\0.2.0\core-0.2.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\lei\opi\opiJovp\0.2.0\opiJovp-0.2.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\es\optocom\jovp\jovp\0.1.0-SNAPSHOT\jovp-0.1.0-SNAPSHOT.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\reflections\reflections\0.9.12\reflections-0.9.12.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\javassist\javassist\3.26.0-GA\javassist-3.26.0-GA.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\com\google\code\gson\gson\2.9.0\gson-2.9.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\commons-io\commons-io\2.11.0\commons-io-2.11.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\joml\joml\1.10.4\joml-1.10.4.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\io\github\java-native\jssc\2.9.4\jssc-2.9.4.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\scijava\native-lib-loader\2.3.6\native-lib-loader-2.3.6.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\slf4j\slf4j-simple\2.0.3\slf4j-simple-2.0.3.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\slf4j\slf4j-api\2.0.3\slf4j-api-2.0.3.jar" %input%
xcopy "c:\Users\imo vifa\Desktop\opi3\lwjgl" %input%\lwjgl /S /I

jpackage --type app-image -n OpiJovp -i %input% --main-jar opiJovp-0.2.0.jar ^
        --win-console --arguments 51234 ^
        --icon "c:\Users\imo vifa\Desktop\opi3\opi_JOVP_logo.ico" ^
        --java-options '-Djava.library.path=$APPDIR'
::        --java-options '-cp' ^
::        --java-options '$APPDIR\..\..\OpiMonitor\app\*.jar;$APPDIR\lwjgl'

::set a="C:\Users\imo vifa\Desktop\opi3\imo_invGamma.json"
::dir %a% > nul || ( echo DISASTER: Missing %a% ; exit /b 1)
::copy %a% OpiJovp



REM --------- OpiMonitor ----------------------------
:opimonitor

echo Creating OpiMonitor app
if exist OpiMonitor\ (
    echo ********* OpiMonitor folder exists, skipping
    goto end
) 

rmdir %input% /S /Q
mkdir %input%

COPY "C:\Users\imo vifa\.m2\repository\org\lei\opi\core\0.2.0\core-0.2.0.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lei\opi\opiJovp\0.2.0\opiJovp-0.2.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\lei\opi\monitor\0.2.0\monitor-0.2.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\es\optocom\jovp\jovp\0.1.0-SNAPSHOT\jovp-0.1.0-SNAPSHOT.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\reflections\reflections\0.9.12\reflections-0.9.12.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\javassist\javassist\3.26.0-GA\javassist-3.26.0-GA.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\com\google\code\gson\gson\2.9.0\gson-2.9.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\commons-io\commons-io\2.11.0\commons-io-2.11.0.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl\3.3.1\lwjgl-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-assimp\3.3.1\lwjgl-assimp-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-glfw\3.3.1\lwjgl-glfw-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-stb\3.3.1\lwjgl-stb-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-shaderc\3.3.1\lwjgl-shaderc-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-vulkan\3.3.1\lwjgl-vulkan-3.3.1.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl\3.3.1\lwjgl-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-assimp\3.3.1\lwjgl-assimp-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-glfw\3.3.1\lwjgl-glfw-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-stb\3.3.1\lwjgl-stb-3.3.1-natives-windows.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\lwjgl\lwjgl-shaderc\3.3.1\lwjgl-shaderc-3.3.1-natives-windows.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\joml\joml\1.10.4\joml-1.10.4.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\io\github\java-native\jssc\2.9.4\jssc-2.9.4.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\scijava\native-lib-loader\2.3.6\native-lib-loader-2.3.6.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\slf4j\slf4j-simple\2.0.3\slf4j-simple-2.0.3.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\slf4j\slf4j-api\2.0.3\slf4j-api-2.0.3.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\javacpp\1.5.5\javacpp-1.5.5.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\javacpp\1.5.5\javacpp-1.5.5-windows-x86_64.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\javacv\1.5.5\javacv-1.5.5.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\opencv\4.5.1-1.5.5\opencv-4.5.1-1.5.5.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\opencv\4.5.1-1.5.5\opencv-4.5.1-1.5.5-windows-x86_64.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\openblas-platform\0.3.13-1.5.5\openblas-platform-0.3.13-1.5.5.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\openblas\0.3.13-1.5.5\openblas-0.3.13-1.5.5.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\bytedeco\openblas\0.3.13-1.5.5\openblas-0.3.13-1.5.5-windows-x86_64.jar" %input%
COPY "C:\Users\imo vifa\.m2\repository\org\openpnp\opencv\4.9.0-0\opencv-4.9.0-0.jar" %input%
::COPY "C:\Users\imo vifa\.m2\repository\org\openpnp\opencv\4.8.1-0\opencv-4.8.1-0.jar" %input%
XCOPY "C:\Users\imo vifa\Desktop\opi3\javafx-sdk-22.0.1" %input%\javafx-sdk-22.0.1 /S /I

jpackage --type app-image -n OpiMonitor -i %input% --main-jar monitor-0.2.0.jar ^
         --main-class org.lei.opi.monitor.Monitor ^
         --win-console --arguments "--mGUI ImoVifa" ^
         --icon "c:\Users\imo vifa\Desktop\opi3\opi_logo.ico" ^
         --java-options '-Djava.library.path=$APPDIR' ^
         --add-modules javafx.controls,javafx.fxml,javafx.swing ^
         --module-path "javafx-jmods-22.0.1;javafx-jmods-22.0.1\lib"
::         --module-path '-Djava.library.path=$APPDIR'

:end