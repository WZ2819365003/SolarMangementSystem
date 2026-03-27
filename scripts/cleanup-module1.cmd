@echo off
setlocal

cd /d "%~dp0.."

if exist "frontend\dist" rd /s /q "frontend\dist"
if exist "backend\target" rd /s /q "backend\target"
if exist "frontend\tests\playwright\test-results" rd /s /q "frontend\tests\playwright\test-results"
if exist "frontend\tests\playwright\output" rd /s /q "frontend\tests\playwright\output"

if exist "frontend\frontend-serve.log" del /q "frontend\frontend-serve.log"
if exist "frontend\frontend-serve.err.log" del /q "frontend\frontend-serve.err.log"
if exist "frontend\redir-test.log" del /q "frontend\redir-test.log"
if exist "frontend\redir-test.err.log" del /q "frontend\redir-test.err.log"
if exist "backend\backend-run.log" del /q "backend\backend-run.log"
if exist "backend\backend-run.err.log" del /q "backend\backend-run.err.log"

echo cleanup-module1 completed
