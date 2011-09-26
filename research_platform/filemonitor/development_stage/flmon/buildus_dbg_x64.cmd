@echo on

set CHK=chk

set WDM_ROOT=C:\WinDDK\7600.16385.1\
set REMOTE_IP=192.168.0.201
set BASE_PROJ_DRIVE=d:
set BASE_PROJ_PATH=D:\Daten\Projekte\FileMonitor\flmon

call %WDM_ROOT%bin\setenv.bat %WDM_ROOT% %CHK% x64 WIN7

cd %BASE_PROJ_PATH%
%BASE_PROJ_DRIVE%


call build

call postbuild.cmd