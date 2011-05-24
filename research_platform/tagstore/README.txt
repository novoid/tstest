####################################################
README FOR TAGSTORE
####################################################

this file is part of tagstore, an alternative way of storing and retrieving information
Copyright (C) 2010-2011  Karl Voit, Christoph Friedl, Wolfgang Wintersteller


this is a short description of how to configure and start the tagstore software 

***************************
STEP 0: prerequisites
***************************
a) open a commandline shell

b) test if python is available on your system path:

$python --version
(expected output, where 2.5 or higher is fine)
Python 2.5.0

if there is an output like this:

python: command not found

please check your system PATH variable and your python installation TODO: provide further information? 

***************************
STEP 1: create a new store
***************************
a) run the tagstore-manager file:

$/path_to_tagstore_installation/python tagstore-manager.py

OR

$/path_to_tagstore_installation/tagstore-manager.py	(make sure the executable flag is set for this file)

the manager window should appear

b) create a new store.
select the tab "Store Management"
click the button "New Tagstore" and select a directory where you want to create your new tagstore.
please make sure this directory is empty, so there can be no troubles with already existing files there. you can create a new directory directly at the selection dialog if you want to.
the selected directory should now appear in the list below the "New Tagstore" button.

c) check if the creation of the tagstore was successful:
go to the previously selected directory. check if there are following sub-directories:

storage
navigation
categorization
expired_items

d) close the manager by clicking the "Save" button in the bottom right corner. 

***************************
STEP 2: start the daemon
***************************
just run the tagstore.py file in the same way you ran the tagstore-manager file ($tagstore.py)

***************************
INFO - manually editing the store config file
***************************
additional configuration of each store can be done by manually editing the store.cfg file
it is located in the dir:
$/path_to_store/.tagstore/store.cfg

each line in this line represents a configuration parameter. just the listed parameters are ment to by changed manually:

* show_category_line

there are 4 different settings:

0 ... show just the describing tagline 
1 ... show the describing tagline AND a categorizing tagline - freely selectable categorizing tags 
2 ... show the describing tagline AND a categorizing tagline - only restricted vocabulary is allowed
3 ... show just the categorizing tagline - only restricted vocabulary is allowed

* category_mandatory

true ... if there is a categorizing tagline available there MUST be at least one tag provided
false ... the categorizing tagline can be left empty

***************************
INFO - manually editing the application config file
***************************
general configuration of the tagstore application can be done by manually editing the tagstore.cfg file
it is located in the dir:
$/path_to_tagstore_installation/tsresources/conf/store.cfg

* max_tags

set the number of tags allowed to be used for tagging a single item
up to 6 tags can be computed within a reasonable time
WARNING: using higher numbers of tags can lead to incredible high computation time.

* num_popular_tags

set the number of recent/popular describing tags which should be shown as decision help

in the same directory where this file lies, there is a file called tagstore.py. 
run "tagstore.py -h" for displaying optional starting parameters.