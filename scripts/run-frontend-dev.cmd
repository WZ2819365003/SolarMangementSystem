@echo off
setlocal
cd /d "%~dp0..\frontend"
call C:\Users\zhuow\tools\node-v14.16.0-win-x64\npm.cmd run serve --scripts-prepend-node-path=true
