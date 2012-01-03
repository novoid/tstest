@echo on


set CHK=fre
set WDM_ROOT=D:\WinDDK\7600.16385.1\
set REMOTE_IP=192.168.0.201
set BASE_PROJ_DRIVE=d:
set BASE_PROJ_PATH=D:\Daten\Projekte\FileMonitor\flmonflt

call %WDM_ROOT%bin\setenv.bat %WDM_ROOT% %CHK% x64 WIN7

cd %BASE_PROJ_PATH%
%BASE_PROJ_DRIVE%


call build

copy %BASE_PROJ_PATH%\objfre_win7_amd64\amd64\filemonwdm.sys \\%REMOTE_IP%\drivers\filemonwdm.sys
copy %BASE_PROJ_PATH%\objfre_win7_amd64\amd64\filemonwdm.pdb \\%REMOTE_IP%\drivers\filemonwdm.pdb