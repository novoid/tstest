
For building tagstore packages, you will need several files from
tagstore as well as packages from external parties.

Here is a list of all possible files you might need to build an
installer for tagstore:

* External Dependencies

- install_python.bat
  - included here
- python-2.7.3.msi
  - Python 2.7, Installer for Windows
  - http://www.python.org/getit/
- PyQt-Py2.7-x86-gpl-4.9.4-1.exe
  - Python Qt, Installer for Windows
  - http://www.riverbankcomputing.co.uk/software/pyqt/download/
- pywin32-214.win32-py2.7.exe
  - Python Windows Extensions, Installer for Windows
  - http://sourceforge.net/projects/pywin32/files/pywin32/
- python-2.7-macosx10.5.dmg
  - Python 2.7, Installer for OS X
  - http://www.python.org/getit/mac/
- vcredist_x64.exe
  - https://www.microsoft.com/en-us/download/details.aspx?id=14632
- vcredist_x86.exe
  - https://www.microsoft.com/en-us/download/details.aspx?id=14632

* Dependencies from tagstore

- file logger (file monitor)
  - developed to log read-access within TagTrees for field studies,
    this tool is not stable enough for being used on Windows machines
  - if you want to test it, you might need following files from the
    "filemonitor" folder of tagstore:
    - install_tslogger.bat
    - install_tslogger_w7_x64.exe
    - install_tslogger_w7_x86.exe
    - install_tslogger_xp_x64.exe
    - install_tslogger_xp_x86.exe
