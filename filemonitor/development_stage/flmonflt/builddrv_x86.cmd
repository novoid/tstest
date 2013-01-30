@echo on

rem set OPSYS=WIN7
set OPSYS=WXP

set CHK=fre
set BUILD_OTIONS=/w /b 
set WDM_ROOT=D:\WinDDK\7600.16385.1\
set REMOTE_IP=192.168.0.202
set BASE_PROJ_DRIVE=d:
set BASE_PROJ_PATH=D:\Daten\Projekte\FileMonitor\flmonflt

call %WDM_ROOT%bin\setenv.bat %WDM_ROOT% %CHK% x86 %OPSYS%

cd %BASE_PROJ_PATH%
%BASE_PROJ_DRIVE%


call build %BUILD_OTIONS%

if %OPSYS% == WIN7 (
	copy %BASE_PROJ_PATH%\objfre_win7_x86\i386\flmonflt.sys \\%REMOTE_IP%\drivers\flmonflt.sys
	copy %BASE_PROJ_PATH%\objfre_win7_x86\i386\flmonflt.pdb \\%REMOTE_IP%\drivers\flmonflt.pdb
) ELSE (
	copy %BASE_PROJ_PATH%\objfre_wxp_x86\i386\flmonflt.sys \\%REMOTE_IP%\drivers\flmonflt.sys
	copy %BASE_PROJ_PATH%\objfre_wxp_x86\i386\flmonflt.pdb \\%REMOTE_IP%\drivers\flmonflt.pdb
)