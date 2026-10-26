@echo off
REM Compile and run the Placement Drive Tracker
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin -cp "lib\*" @sources.txt
if errorlevel 1 goto end
java -cp "bin;lib\*" com.rajesh.placement.Main
:end
del sources.txt
