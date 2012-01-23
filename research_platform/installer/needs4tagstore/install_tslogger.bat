REM tslogger-installer for windows start
REM install flmonflt-driver
pause
rundll32 syssetup,SetupInfObjectInstallAction DefaultInstall 128 .\tagstore_logger\driver\flmonflt.inf
REM flmonflt-driver installed
REM next step
REM register flmonflt-driver
pause
regedit /s .\tagstore_logger\driver\flmon_driver.reg
REM flmonflt-driver registered
REM next step
REM start flmonflt-driver
pause
sc start flmonflt
REM flmonflt-driver started
REM next step
REM install visual c++ 2010 redistributable package
pause
vcredist_x86.exe
REM visual c++ 2010 redistributable package installed
REM next step
REM install FileMonitorWatcher-service
pause
sc create FileMonitorWatcher start= auto DisplayName= "Tagstore Filesystem Monitoring Watcher" binpath= c:\tagstore_svn\needs4tagstore\tagstore_logger\win32service\flmonsvc.exe
REM FileMonitorWatcher-service installed
REM next step
REM register FileMonitorWatcher
pause
regedit /s .\tagstore_logger\win32service\tsmonservice_x86.reg
REM FileMonitorWatcher registered
REM next step
REM start FileMonitorWatcher
pause
sc start FileMonitorWatcher
REM FileMonitorWatcher started
REM tslogger-installer for windows finished 
pause
