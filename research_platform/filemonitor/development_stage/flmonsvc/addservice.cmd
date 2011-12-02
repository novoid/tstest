
set FLSVCBIN=\"D:\Daten\Projekte\FileMonitor\Debug\flmonsvc.exe\"

rem set FLSVCBIN=\"C:\userspace\flmonsvc.exe\" C:\userspace\tagstore.cfg C:\userspace\tswservice.log
set FLSVCDISPLAYNAME=Tagstore Filesystem Monitoring Watcher

rem DEBUG
sc create FileMonitorWatcher binpath= "%FLSVCBIN%" DisplayName= "%FLSVCDISPLAYNAME%"

rem RELEASE
rem sc create FileMonitorWatcher binpath= "%FLSVCBIN%"

pause