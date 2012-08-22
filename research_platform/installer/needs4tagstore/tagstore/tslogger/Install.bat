
@ Echo off

cd c:\tagstore\tslogger\setup

if defined ProgramFiles(x86) goto x64

ver | find "5."> nul
if %ERRORLEVEL%==0 goto WinXP_x86
ver | find "6."> nul
if %ERRORLEVEL%==0 goto Win7_x86
goto NoVer

:x64
ver | find "5."> nul
if %ERRORLEVEL%==0 goto WinXP_x64
ver | find "6."> nul
if %ERRORLEVEL%==0 goto Win7_x64
goto NoVer

:WinXP_x86
echo Windows XP mit 32 Bit gefunden...
REM Copy OS dependend files to the system
copy C:\tagstore\tslogger\win32service\xp\*.* c:\tagstore
REM install flmonflt-driver
rundll32 syssetup,SetupInfObjectInstallAction DefaultInstall 128 C:\tagstore\tslogger\driver\winxp\x86\flmonflt.inf
REM flmonflt-driver installed
REM install visual c++ 2010 redistributable package
vcredist_x86_WinXP_SP3.exe
REM visual c++ 2010 redistributable package installed
goto Exit

:WinXP_x64
echo Windows XP mit 64 Bit gefunden...
goto NoVer

:Win7_x86
echo Windows 7 mit 32 Bit gefunden...
REM Copy OS dependend files to the system
copy C:\tagstore\tslogger\win32service\win7\*.* c:\tagstore
REM install flmonflt-driver
rundll32 syssetup,SetupInfObjectInstallAction DefaultInstall 128 C:\tagstore\tslogger\driver\win7\x86\flmonflt.inf
REM flmonflt-driver installed
REM install visual c++ 2010 redistributable package
vcredist_x86_Vista_7.exe
REM visual c++ 2010 redistributable package installed
goto Exit

:Win7_x64
rem !!!This would be the code, if the 64bit driver is signed/certificated!!!
rem echo Windows 7 mit 64 Bit gefunden...
rem REM Copy OS dependend files to the system
rem copy C:\tagstore\tslogger\win32service\win7\*.* c:\tagstore
rem REM install flmonflt-driver
rem rundll32 syssetup,SetupInfObjectInstallAction DefaultInstall 128 C:\tagstore\tslogger\driver\win7\x64\flmonflt.inf
rem REM flmonflt-driver installed
rem REM install visual c++ 2010 redistributable package
rem vcredist_x64_Win7_Vista_XP.exe
rem REM visual c++ 2010 redistributable package installed
goto NoVer

:NoVer
echo Keine gültige windows Version gefunden!!
goto Quit

: Exit 
REM register flmonflt-driver
regedit /s C:\tagstore\tslogger\setup\flmon_driver.reg
REM flmonflt-driver registered
REM start flmonflt-driver
sc start flmonflt
REM flmonflt-driver started
REM install FileMonitorWatcher-service
sc create FileMonitorWatcher start= auto DisplayName= "Tagstore Filesystem Monitoring Watcher" binpath= c:\tagstore\flmonsvc.exe
REM FileMonitorWatcher-service installed
REM register FileMonitorWatcher
regedit /s C:\tagstore\tslogger\setup\tsmonservice_x86.reg
REM FileMonitorWatcher registered
REM start FileMonitorWatcher
sc start FileMonitorWatcher
REM FileMonitorWatcher started
REM tslogger-installer for windows finished

:Quit