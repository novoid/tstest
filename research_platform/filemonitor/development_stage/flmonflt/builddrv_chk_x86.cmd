@echo on

set CHK=chk
set WDM_ROOT=C:\WinDDK\7600.16385.1\
rem set REMOTE_IP=10.0.0.1
set REMOTE_IP=192.168.0.205
set BASE_PROJ_DRIVE=d:
set BASE_PROJ_PATH=D:\Daten\Projekte\Diplomarbeit\flmonflt

call %WDM_ROOT%bin\setenv.bat %WDM_ROOT% %CHK% x86 WXP

cd %BASE_PROJ_PATH%
%BASE_PROJ_DRIVE%


call build

rem copy %BASE_PROJ_PATH%\objfre_wxp_x86\i386\flmonflt.sys \\%REMOTE_IP%\drivers\flmonflt.sys
rem copy %BASE_PROJ_PATH%\objfre_wxp_x86\i386\flmonflt.pdb \\%REMOTE_IP%\drivers\flmonflt.pdb