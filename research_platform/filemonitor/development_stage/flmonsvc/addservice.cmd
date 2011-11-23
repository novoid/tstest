
rem set FLSVCBIN=\"Z:\florian auf meinem Mac\Documents\Visual Studio 2010\Projects\FileMonitor\Debug\flmonsvc.exe\"
set FLSVCBIN=\"C:\flmonsvc.exe\"
set FLSVCDISPLAYNAME=Tagstore Filesystem Monitoring Watcher

rem DEBUG
sc create FileMonitorWatcher binpath= "%FLSVCBIN%" DisplayName= "%FLSVCDISPLAYNAME%"

rem RELEASE
rem sc create FileMonitorWatcher binpath= "%FLSVCBIN%"

pause