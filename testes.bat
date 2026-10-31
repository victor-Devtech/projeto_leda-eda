@echo off
setlocal enabledelayedexpansion

if not exist out\testes mkdir out\testes

set SOURCES=
for /r src %%f in (*.java) do (
    set SOURCES=!SOURCES! "%%f"
)
for /r test %%f in (*.java) do (
    set SOURCES=!SOURCES! "%%f"
)

javac -encoding UTF-8 -d out\testes %SOURCES%
if errorlevel 1 exit /b 1

java -Xmx4g -cp out\testes br.edu.uepb.eda.testes.SuiteTestes
