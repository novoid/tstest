
@ Echo off

cd c:\tagstore\tslogger\setup\

REM tslogger-uninstaller for windows start
REM stopping FileMonitorWatcher
sc stop FileMonitorWatcher
REM FileMonitorWatcher stopped
REM unregister FileMonitorWatcher
regedit /s c:\tagstore\tslogger\setup\uninst_tsmonservice_x86.reg
REM FileMonitorWatcher unregistered
REM uninstall FileMonitorWatcher-service
sc delete FileMonitorWatcher 
REM start= auto DisplayName= "Tagstore Filesystem Monitoring Watcher" binpath= c:\tagstore\flmonsvc.exe
REM FileMonitorWatcher-service uninstalled
REM stop flmonflt-driver
sc stop flmonflt
REM flmonflt-driver stopped
REM unregister flmonflt-driver
regedit /s C:\tagstore\tslogger\setup\uninst_flmon_driver.reg
REM flmonflt-driver unregistered

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
REM uninstall flmonflt-driver
rundll32 syssetup,SetupInfObjectInstallAction DefaultUnInstall 128 C:\tagstore\tslogger\driver\winxp\x86\flmonflt.inf
REM flmonflt-driver uninstalled
goto Exit

:WinXP_x64
echo Windows XP mit 64 Bit gefunden...
goto Exit

:Win7_x86
echo Windows 7 mit 32 Bit gefunden...
REM uninstall flmonflt-driver
rundll32 syssetup,SetupInfObjectInstallAction DefaultUnInstall 128 C:\tagstore\tslogger\driver\win7\x86\flmonflt.inf
REM flmonflt-driver uninstalled
goto Exit

:Win7_x64
echo Windows 7 mit 64 Bit gefunden...
REM uninstall flmonflt-driver
rundll32 syssetup,SetupInfObjectInstallAction DefaultUnInstall 128 C:\tagstore\tslogger\driver\win7\x64\flmonflt.inf
REM flmonflt-driver uninstalled
goto Exit

:NoVer
echo Keine gültige windows Version gefunden!!

: Exit 
REM tslogger-uninstaller for windows stopped




