@echo off
setlocal

powershell -ExecutionPolicy Bypass -File "%~dp0push-current-branch.ps1" %*

