@echo off
set GRADLE_VERSION=8.9
set DIST_NAME=gradle-%GRADLE_VERSION%-bin
set BASE=%USERPROFILE%\.gradle\wrapper\manual
set DEST=%BASE%\%DIST_NAME%
if not exist "%DEST%\bin\gradle.bat" (
  if not exist "%BASE%" mkdir "%BASE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/%DIST_NAME%.zip' -OutFile '%BASE%\%DIST_NAME%.zip'; Expand-Archive -Force '%BASE%\%DIST_NAME%.zip' '%BASE%'; Remove-Item '%BASE%\%DIST_NAME%.zip'"
)
call "%DEST%\bin\gradle.bat" %*
