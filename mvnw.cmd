@REM Maven Wrapper script for Windows
@REM This downloads and uses Maven 3.9.6 from the Apache archive to build the project.

@echo off
setlocal

set MAVEN_VERSION=3.9.6
set MAVEN_DOWNLOAD_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip
set MVN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MVN_EXE=%MVN_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd

if not exist "%MVN_EXE%" (
    echo [INFO] Maven not found. Downloading Maven %MAVEN_VERSION% from archive...
    if not exist "%MVN_DIR%" mkdir "%MVN_DIR%"
    powershell -Command "Invoke-WebRequest -Uri '%MAVEN_DOWNLOAD_URL%' -OutFile '%MVN_DIR%\maven.zip'"
    
    if exist "%MVN_DIR%\maven.zip" (
        powershell -Command "Expand-Archive -Path '%MVN_DIR%\maven.zip' -DestinationPath '%MVN_DIR%' -Force"
        del "%MVN_DIR%\maven.zip"
        echo [INFO] Maven downloaded successfully.
    ) else (
        echo [ERROR] Failed to download Maven from %MAVEN_DOWNLOAD_URL%
        exit /b 1
    )
)

"%MVN_EXE%" %*
