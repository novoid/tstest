@echo on

set CHK=fre

set BUILD_OTIONS=/w /b 
set WDM_ROOT=D:\WinDDK\7600.16385.1\
set REMOTE_IP=192.168.0.201
set BASE_PROJ_DRIVE=d:
set BASE_PROJ_PATH=D:\Daten\Projekte\FileMonitor\flmon

call %WDM_ROOT%bin\setenv.bat %WDM_ROOT% %CHK% x86 WIN7

cd %BASE_PROJ_PATH%
%BASE_PROJ_DRIVE%


call build %BUILD_OTIONS%

call postbuild.cmd