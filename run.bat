@echo off
setlocal enabledelayedexpansion

set MODO=%1
if "%MODO%"=="" set MODO=--tudo

if not exist out mkdir out

set SOURCES=
for /r src %%f in (*.java) do (
    set SOURCES=!SOURCES! "%%f"
)

javac -encoding UTF-8 -d out %SOURCES%
if errorlevel 1 exit /b 1

java -Xmx4g -cp out br.edu.uepb.eda.Main %MODO%
