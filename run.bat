@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java
if errorlevel 1 (
  echo Compilation failed. Check the Java errors above.
  pause
  exit /b 1
)
java -cp out ResumeScreeningSystem
pause
