#!/usr/bin/env python

# -*- coding: utf-8 -*-

## this file is part of tagstore_admin, an alternative way of storing and retrieving information
## Copyright (C) 2010  Karl Voit, Christoph Friedl, Wolfgang Wintersteller
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
from optparse import OptionParser
from PyQt4 import QtCore, QtGui
from tscore.store import Store
from tscore.configwrapper import ConfigWrapper
from tscore.tsconstants import TsConstants
from tsgui.admindialog import StorePreferencesController

class Administration(QtCore.QObject):

    def __init__(self, verbose):
        
        self.__log = None
        self.__main_config = None
        self.__admin_dialog = None
        
        self.LOG_LEVEL = logging.INFO
        if verbose:
            self.LOG_LEVEL = logging.DEBUG
            
        self.__init_logger(self.LOG_LEVEL)
        self.__init_configuration()
        
    def __init_logger(self, log_level):
        LOG_FILENAME = TsConstants.LOG_FILENAME
        self.__log = logging.getLogger(TsConstants.LOGGER_NAME)
        self.__log.setLevel(logging.DEBUG)

        #logging.basicConfig(level=logging.INFO)
        formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")

        ## create console handler and set level
        console_handler = logging.StreamHandler()
        console_handler.setLevel(log_level)
        console_handler.setFormatter(formatter)

        ## create filehandler
        file_handler = logging.handlers.RotatingFileHandler(LOG_FILENAME, 
            maxBytes=TsConstants.LOG_FILESIZE, backupCount=TsConstants.LOG_BACKUP_COUNT)
        file_handler.setFormatter(formatter)

        ## add handlers to logger
        self.__log.addHandler(console_handler)
        self.__log.addHandler(file_handler)
    
    def __init_configuration(self):
        """
        initializes the configuration. This method is called every time the config file changes
        """
        self.__log.info("initialize configuration")
        self.__main_config = ConfigWrapper(TsConstants.CONFIG_PATH)
        self.__main_config.connect(self.__main_config, QtCore.SIGNAL("changed()"), self.__init_configuration)
    
    def show_admin_dialog(self, show):
        if self.__admin_dialog is None:
            self.__admin_dialog = StorePreferencesController()
            self.__admin_dialog.set_main_config(self.__main_config)
            self.__admin_dialog.set_store_list(self.__main_config.get_stores())

            self.connect(self.__admin_dialog, QtCore.SIGNAL("create_new_store"), self.__handle_new_store)
        
        self.__admin_dialog.show_dialog()
        
    def __handle_new_store(self, dir):
        """
        create new store at given directory
        """
        ## get all store ids
        id_list = self.__main_config.get_store_ids()
        new_store = Store(id_list)
        
if __name__ == '__main__':  
  
    ## initialize and configure the optionparser
    opt_parser = OptionParser("tagstore_admin.py [options]")
    opt_parser.add_option("-v", "--verbose", dest="verbose", action="store_true", help="start programm with detailed output")

    (options, args) = opt_parser.parse_args()
    
    verbose_mode = False
    dry_run = False
    
    if options.verbose:
        verbose_mode = True
    
    tagstore_admin = QtGui.QApplication(sys.argv)
    tagstore_admin.setApplicationName("tagstore_admin")
    tagstore_admin.setOrganizationDomain("www.tagstore_admin.org")
    tagstore_admin.UnicodeUTF8
    
    admin_widget = Administration(verbose=verbose_mode)
    admin_widget.show_admin_dialog(True)
    tagstore_admin.exec_()
## end