@echo on

set CHK=chk
set BUILD_OTIONS=/w /b 
set WDM_ROOT=D:\WinDDK\7600.16385.1\
set REMOTE_IP=192.168.0.201
set BASE_PROJ_DRIVE=d:
set BASE_PROJ_PATH=D:\Daten\Projekte\FileMonitor\flmonflt

call %WDM_ROOT%bin\setenv.bat %WDM_ROOT% %CHK% x64 WIN7

cd %BASE_PROJ_PATH%
%BASE_PROJ_DRIVE%


call build %BUILD_OTIONS%

copy %BASE_PROJ_PATH%\objchk_win7_amd64\amd64\flmonflt.sys \\%REMOTE_IP%\drivers\flmonflt.sys
copy %BASE_PROJ_PATH%\objchk_win7_amd64\amd64\flmonflt.pdb \\%REMOTE_IP%\drivers\flmonflt.pdb