@echo off
echo ========================================================
echo Starting CAMPUS-LOST-AND-FOUND Desktop Application...
echo ========================================================

REM Create bin directory if not present
if not exist "bin" mkdir bin

REM Compile source files
echo Compiling Java source files...
javac -cp ".;lib/mysql-connector-j-26.7.0.jar" -d bin src\db\*.java src\model\*.java src\dao\*.java src\ui\*.java src\Main.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please check Java JDK installation.
    pause
    exit /b %ERRORLEVEL%
)

REM Run application
echo Launching Application...
java -cp ".;bin;lib/mysql-connector-j-26.7.0.jar" Main

pause
