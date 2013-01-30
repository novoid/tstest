REM stop windows-tagstore and start new instance
@echo off
taskkill /f /im pythonw.exe
start tagstore.pyw