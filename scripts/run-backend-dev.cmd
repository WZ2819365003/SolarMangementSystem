@echo off
setlocal
cd /d "%~dp0..\backend"
call mvn.cmd spring-boot:run
