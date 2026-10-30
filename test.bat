@echo off
echo ========================================================
echo Running CAMPUS-LOST-AND-FOUND Test Suite...
echo ========================================================

REM Create bin directory if not present
if not exist "bin" mkdir bin

REM Compile source and test files
echo Compiling source and test files...
javac -cp ".;lib/mysql-connector-j-26.7.0.jar" -d bin src\db\*.java src\model\*.java src\dao\*.java src\ui\*.java src\Main.java test\*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Test compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

REM Run automated test suite
echo Executing TestSuite...
java -cp ".;bin;lib/mysql-connector-j-26.7.0.jar" TestSuite

pause
