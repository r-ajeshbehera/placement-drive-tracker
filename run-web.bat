@echo off
REM Compile and start the WEB version of the Placement Drive Tracker
REM The browser opens by itself (http://localhost:8085, or the next free port)
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin -cp "lib\*" @sources.txt
if errorlevel 1 goto end
java -cp "bin;lib\*" com.rajesh.placement.web.WebServer
:end
del sources.txt
