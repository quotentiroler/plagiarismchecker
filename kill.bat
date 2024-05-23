@echo off
for /f "tokens=1" %%i in ('jps -l ^| findstr /i PlagCheckApp') do set PID=%%i
if defined PID (
    echo Killing PID: %PID%
    taskkill /F /PID %PID%
) else (
    echo No running PlagCheckApp found.
)