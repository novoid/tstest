@echo on

set CHK=fre
set WDM_ROOT=C:\WinDDK\7600.16385.1\
set REMOTE_IP=192.168.0.201
set BASE_PROJ_DRIVE=d:
set BASE_PROJ_PATH=D:\Daten\Projekte\Diplomarbeit\filemonwdm

call %WDM_ROOT%bin\setenv.bat %WDM_ROOT% %CHK% x86 WXP

cd %BASE_PROJ_PATH%
%BASE_PROJ_DRIVE%


call build

copy %BASE_PROJ_PATH%\objfre_wxp_x86\i386\filemonwdm.sys \\%REMOTE_IP%\drivers\filemonwdm.sys
copy %BASE_PROJ_PATH%\objfre_wxp_x86\i386\filemonwdm.pdb \\%REMOTE_IP%\drivers\filemonwdm.pdb