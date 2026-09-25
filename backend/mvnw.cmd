@echo off
setlocal

set MAVEN_DIR=%~dp0.mvn\apache-maven-3.9.6

if not exist "%MAVEN_DIR%\bin\mvn.cmd" (
    echo Downloading Apache Maven 3.9.6...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip', '%~dp0.mvn\maven.zip'); Expand-Archive -Path '%~dp0.mvn\maven.zip' -DestinationPath '%~dp0.mvn' -Force; Remove-Item '%~dp0.mvn\maven.zip'"
)

"%MAVEN_DIR%\bin\mvn.cmd" %*
