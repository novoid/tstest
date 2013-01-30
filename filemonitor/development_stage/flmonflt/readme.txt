sc create flmonflt type= filesys binPath= C:\Windows\System32\Drivers\flmonflt.sys
sc start flmonflt
pause
sc stop flmonflt
sc delete flmonflt