@echo off


:: From https://stackoverflow.com/questions/5534324/how-to-run-multiple-programs-using-batch-file

echo Starting OpiJovp
cd jovp
::: START /B run_OpiJovp.bat
START /B java @..\jopts.txt org.lei.opi.jovp.OpiJovp 51234
cd ..

::FOR /L %%i IN (1,1,15) DO (
::    (TASKLIST | FINDSTR /I "jovp") && GOTO :startMonitor
::    FOR /F %%i IN ('TASKLIST ^| FINDSTR /I "jovp"') DO set t=%%i
::    echo %t%
::)
::ECHO Timeout waiting for OpiJovp to start
::GOTO :EOF

ECHO Waiting for opiJovp to start
TIMEOUT /t 5

:startMonitor
echo Starting Monitor
cd monitor
START /B run_monitor.bat
cd ..