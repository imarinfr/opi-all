@echo off
:: Collect all files for jpacakge commands into input folders
:: and then run jpackage

set input="C:\Users\imo vifa\Desktop\opi3\input"


REM --------- OpiJovp ----------------------------
echo "Creating OpiJovp app"
if exist OpiJovp\ (
    echo "********* OpiJovp folder exists, skipping"
) else (

    rmdir %input% /S /Q
    mkdir %input%
    copy "c:\Users\imo vifa\.m2\repository\org\lei\opi\core\0.2.0\core-0.2.0.jar" %input%
    copy "c:\Users\imo vifa\.m2\repository\org\lei\opi\opiJovp\0.2.0\opiJovp-0.2.0.jar" %input%
    copy "c:\Users\imo vifa\.m2\repository\es\optocom\jovp\jovp\0.1.0-SNAPSHOT\jovp-0.1.0-SNAPSHOT.jar" %input%
    xcopy "c:\Users\imo vifa\Desktop\opi3\lwjgl" %input%\lwjgl /S

    jpackage --type app-image -n OpiJovp -i %input% --main-jar opiJovp-0.2.0.jar ^
            --win-console --arguments 51234 ^
            --icon "c:\Users\imo vifa\Desktop\opi3\opi_JOVP_logo.ico" ^
            --java-options '-Djava.library.path=$APPDIR' 
    ::          --java-options '-cp' ^
    ::          --java-options '$APPDIR\*.jar;$APPDIR\lwjgl'

    ::set a="C:\Users\imo vifa\Desktop\opi3\imo_invGamma.json"
    ::dir %a% > nul || ( echo DISASTER: Missing %a% ; exit /b 1)
    ::copy %a% OpiJovp
)


REM --------- OpiMonitor ----------------------------
echo "Creating OpiMonitor app"
if exist OpiMonitor\ (
    echo "********* OpiMonitor folder exists, skipping"
) else (

    rmdir %input% /S /Q
    mkdir %input%

    copy "c:\Users\imo vifa\.m2\repository\org\lei\opi\core\0.2.0\core-0.2.0.jar" %input%
    copy "c:\Users\imo vifa\.m2\repository\org\lei\opi\monitor\0.2.0\monitor-0.2.0.jar" %input%

    jpackage --type app-image -n OpiMonitor -i %input% --main-jar monitor-0.2.0.jar ^
            --main-class org.lei.opi.monitor.Monitor ^
            --win-console --arguments "--cli 50001 ImoVifa" ^
            --icon "c:\Users\imo vifa\Desktop\opi3\opi_logo.ico" ^
            --java-options '-Djava.library.path=$APPDIR' ^
            --add-modules javafx.controls,javafx.fxml,javafx.swing ^
            --module-path "c:\Users\imo vifa\Desktop\opi3\javafx-jmods-22.0.1"
)