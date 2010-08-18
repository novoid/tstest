#!/usr/bin/env python
# -*- coding: iso-8859-15 -*-
## this file is part of tagstore, an alternative way of storing and retrieving information
## Copyright (C) 2010  Karl Voit, [your name]
##
## This program is free software; you can redistribute it and/or modify it under the terms
## of the GNU General Public License as published by the Free Software Foundation; either
## version 3 of the License, or (at your option) any later version.
##
## This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
## without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
## See the GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License along with this program;
## if not, see <http://www.gnu.org/licenses/>.

import sys
import logging.handlers
from PyQt4.Qt import QApplication
from tscommon.TagStoreCommon import TagStoreConstants, TagStoreConfigReader, TagStoreTagHandler
from tsdialog.TagStoreDialog import TagStoreDialogController

class TagStore(object):
    
    configReader = TagStoreConfigReader(TagStoreConstants.CONFIG_PATH)
    
    def __init__(self):
        self.init_logger()
        self.log.info(_("starting TagStore main runtime"))
        
        self.init_tagstores()
        #self.start_tag_dialog()
    
    def init_logger(self):
        '''
        create a logger object with appropriate settings
        '''
        # TODO create a class for doing this
        LOG_FILENAME = TagStoreConstants.LOG_FILENAME
        self.log = logging.getLogger('TagStoreLogger')
        self.log.setLevel(logging.DEBUG)

        #logging.basicConfig(level=logging.INFO)
        formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")

        ## create console handler and set level to debug
        ch = logging.StreamHandler()
        ch.setLevel(logging.DEBUG)

        ## add formatter to ch
        ch.setFormatter(formatter)
        
        ## add ch to logger
        self.log.addHandler(ch)

        # create filehandler
        handler = logging.handlers.RotatingFileHandler(LOG_FILENAME, maxBytes=1024, backupCount=5)
        handler.setFormatter(formatter)
        self.log.addHandler(handler)        
        
        self.log.info("log initialized now %s" % id(self.log))
    
    def init_tagstores(self):
        '''
        get the tagstores and watchdirs
        start a watchdaemon for each
        '''
        self.log.info("initializing tagstores")
        tagStoreDirs = self.get_tagstore_dirs_from_config()
        for dir in tagStoreDirs:
            # TODO check if the store is indeed a dir
            ## start a new watcher for each
            self.start_daemon(dir)
            
    
    def get_tagstore_dirs_from_config(self):
        '''
        check how much tagstores are configured
        '''
        dirString = self.configReader.get_value(TagStoreConstants.TAG_SECTION_GENERAL, TagStoreConstants.CONFIG_FILEDIR)
        return dirString.split(",")
    
    def start_daemon(self, dir):
        '''
        start a watchdaemon for the given directory
        '''
        self.log.info("starting new DAEMON: %s" % dir)
    
    def start_tag_dialog(self, tagDir, file):
        #app = QApplication(sys.argv)
        tagProvider = TagStoreTagHandler(tagDir + "/" + TagStoreConstants.TAG_FILE_NAME)
        
        dialogController = TagStoreDialogController(tagProvider)
        dialogController.show_dialog(file)
        #app.exec_()
        
if __name__ == '__main__':
    store = TagStore()
## end