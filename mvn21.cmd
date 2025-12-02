@echo off
REM Script para ejecutar Maven con Java 21
REM Este script asegura que Maven siempre use Java 21

setlocal

REM Configurar JAVA_HOME a Java 21
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Verificar que Java 21 esta disponible
java -version 2>&1 | findstr /C:"21" >nul
if %errorlevel% neq 0 (
    echo ERROR: Java 21 no esta disponible en la ruta especificada.
    echo Por favor, verifica que Java 21 este instalado en: %JAVA_HOME%
    exit /b 1
)

REM Ejecutar Maven con todos los argumentos pasados
call "%~dp0mvnw.cmd" %*

endlocal
