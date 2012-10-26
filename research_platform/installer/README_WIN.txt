
Installer settings for windows installer:


Files:
-added file: README_WIN.txt

Advanced Settings: (another 2 Post-installation actions required)

- Create directory: 
${user_home_directory}\AppData\Local\tagstore\

- Copy file:
${installdir}\tsresources\conf\tagstore.cfg
to:
${user_home_directory}\AppData\Local\tagstore\tagstore.cfg


