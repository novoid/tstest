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
from PyQt4 import QtCore, QtGui
from optparse import OptionParser
from tscore.configwrapper import ConfigWrapper
from tscore.loghelper import LogHelper
from tscore.store import Store
from tscore.tsconstants import TsConstants
import logging.handlers
import sys

class Synchronizer(QtCore.QObject):

    def __init__(self, application, verbose, standalone=False):
        QtCore.QObject.__init__(self)
        
        self.__log = None
        self.__main_config = None
        self.__store_object = None

        self.__verbose_mode = verbose
        self.__standalone = standalone
        
        # the main application which has the translator installed
        self.__application = application

        self.LOG_LEVEL = logging.INFO
        if verbose:
            self.LOG_LEVEL = logging.DEBUG
            

        self.STORE_CONFIG_DIR = TsConstants.DEFAULT_STORE_CONFIG_DIR
        self.STORE_CONFIG_FILE_NAME = TsConstants.DEFAULT_STORE_CONFIG_FILENAME
        self.STORE_TAGS_FILE_NAME = TsConstants.DEFAULT_STORE_TAGS_FILENAME
        self.STORE_VOCABULARY_FILE_NAME = TsConstants.DEFAULT_STORE_VOCABULARY_FILENAME
        
        self.__system_locale = unicode(QtCore.QLocale.system().name())[0:2]
        self.__translator = QtCore.QTranslator()
        if self.__translator.load("ts_" + self.__system_locale + ".qm", "tsresources/"):
            self.__application.installTranslator(self.__translator)
        # "en" is automatically translated to the current language e.g. en -> de
        self.CURRENT_LANGUAGE = self.__get_locale_language()
        #dir names for all available languages
        self.STORE_STORAGE_DIRS = []
        self.STORE_DESCRIBING_NAV_DIRS = []
        self.STORE_CATEGORIZING_NAV_DIRS = []
        self.STORE_EXPIRED_DIRS = []
        self.STORE_NAVIGATION_DIRS = []
        self.SUPPORTED_LANGUAGES = TsConstants.DEFAULT_SUPPORTED_LANGUAGES
        self.__store_dict = {}
        
        # catch all "possible" dir-names
        for lang in self.SUPPORTED_LANGUAGES: 
            self.change_language(lang) 
            self.STORE_STORAGE_DIRS.append(self.trUtf8("storage"))#self.STORE_STORAGE_DIR_EN))  
            self.STORE_DESCRIBING_NAV_DIRS.append(self.trUtf8("descriptions"))#self.STORE_DESCRIBING_NAVIGATION_DIR_EN))  
            self.STORE_CATEGORIZING_NAV_DIRS.append(self.trUtf8("categories"))#self.STORE_CATEGORIZING_NAVIGATION_DIR_EN)) 
            self.STORE_EXPIRED_DIRS.append(self.trUtf8("expired_items"))#STORE_EXPIRED_DIR_EN))
            self.STORE_NAVIGATION_DIRS.append(self.trUtf8("navigation")) 
        
        self.__log = LogHelper.get_app_logger(self.LOG_LEVEL)
        self.__init_configuration()
        
    def __init_configuration(self):
        """
        initializes the configuration. This method is called every time the config file changes
        """
        self.__log.info("initialize configuration")
        if self.__main_config is None:
            self.__main_config = ConfigWrapper(TsConstants.CONFIG_PATH)
            #self.connect(self.__main_config, QtCore.SIGNAL("changed()"), self.__init_configuration)
        
        self.CURRENT_LANGUAGE = self.__main_config.get_current_language();
        
        if self.CURRENT_LANGUAGE is None or self.CURRENT_LANGUAGE == "":
            self.CURRENT_LANGUAGE = self.__get_locale_language()
        
        # switch back to the configured language
        self.change_language(self.CURRENT_LANGUAGE)

        self.__prepare_store_params()
        self.__init_store_object()
        
    
    def __handle_synchronization(self, store_name):
        """
        do all the necessary synchronization stuff here ...
        """
        store_to_sync = self.__store_dict[str(store_name)]
        print "####################"
        print "synchronize " + store_name
        print "####################"
        
        store_to_sync.add_item_list_with_tags(["item_one", "item_two"], ["be", "tough"])
    
    def __get_locale_language(self):
        """
        returns the translation of "en" in the system language
        """
        return self.trUtf8("en")
    
    def set_application(self, application):
        """
        if the manager is called from another qt application (e.g. tagstore.py)
        you must set the calling application here for proper i18n
        """
        self.__application = application
    
    def __prepare_store_params(self):
        """
        initialzes all necessary params for creating a store object
        """
        
        for lang in self.SUPPORTED_LANGUAGES: 
            #self.change_language(lang) 
            self.STORE_STORAGE_DIRS.append(self.trUtf8("storage"))  
            self.STORE_DESCRIBING_NAV_DIRS.append(self.trUtf8("navigation"))  
            self.STORE_CATEGORIZING_NAV_DIRS.append(self.trUtf8("categorization")) 
            self.STORE_EXPIRED_DIRS.append(self.trUtf8("expired_items")) 
        ## reset language 
        #self.change_language(store_current_language) 
        
        config_dir = self.__main_config.get_store_config_directory()
        if config_dir != "":
            self.STORE_CONFIG_DIR = config_dir
        config_file_name = self.__main_config.get_store_configfile_name()
        if config_file_name != "":
            self.STORE_CONFIG_FILE_NAME = config_file_name
        tags_file_name = self.__main_config.get_store_tagsfile_name()
        if tags_file_name != "":
            self.STORE_TAGS_FILE_NAME = tags_file_name
        vocabulary_file_name = self.__main_config.get_store_vocabularyfile_name()
        if vocabulary_file_name != "":
            self.STORE_VOCABULARY_FILE_NAME = vocabulary_file_name
    
    def change_language(self, locale):
        """
        changes the current application language
        please notice: this method is used to find all available storage/navigation directory names
        this is why it should not be extended to call any UI update methods directly
        """
        
        ## delete current translation to switch to default strings
        self.__application.removeTranslator(self.__translator)

        ## load new translation file        
        self.__translator = QtCore.QTranslator()
        language = unicode(locale)
        if self.__translator.load("ts_" + language + ".qm", "tsresources/"):
            self.__application.installTranslator(self.__translator)
            
        ## update current language
#        self.CURRENT_LANGUAGE = self.trUtf8("en")
        self.CURRENT_LANGUAGE = self.trUtf8(locale)
    
    def __init_store_object(self):
        store_item = self.__main_config.get_syncronizable_store()
        ## use the store name as identifier in the dictionary.
        ## the admindialog just provides store names instead of ids later on
        store_name = store_item["path"].split("/").pop()
        self.__store_object = Store(store_item["id"], store_item["path"], 
              self.STORE_CONFIG_DIR + "/" + self.STORE_CONFIG_FILE_NAME,
              self.STORE_CONFIG_DIR + "/" + self.STORE_TAGS_FILE_NAME,
              self.STORE_CONFIG_DIR + "/" + self.STORE_VOCABULARY_FILE_NAME,
              self.STORE_NAVIGATION_DIRS,
              self.STORE_STORAGE_DIRS, 
              self.STORE_DESCRIBING_NAV_DIRS,
              self.STORE_CATEGORIZING_NAV_DIRS,
              self.STORE_EXPIRED_DIRS,
              self.__main_config.get_expiry_prefix())
        self.__store_object.init()
        self.connect(self.__store_object, QtCore.SIGNAL("store_synchronize_end"), self.__handle_sync_end)
        
    def __handle_sync_end(self):
        """
        does the work left when synchronizing is done
        """
        print "doing cleanup work"
        self.__log.info("**********************")
        self.__log.info("Synchronizing DONE")
        self.__log.info("**********************")
    
    def synchronize(self):
        """
        the main method for synchronizing the specified store with an android phone
        STEPS:
        1. get store information from andoid
        2. get store information from synchronizable store on local machine
        3. compute deltas
        4. create Work-In-Progress file at storage of local machine (use the PidHelper to get the current PID)
        5. copy new files to storage dir
        6. add item names and tags to store
        7. UPDATE the android tagstore system (don't know yet how to do this ... ^^ )
        """
        
        self.__log.info("**********************")
        self.__log.info("START synchronizing")
        self.__log.info("**********************")
        
        ## IMPORTANT: write the work-in-progress file in the storage directory
        self.__log.info("PLZ IMPLEMENT ME!!")
        ## the following line should be used to add new items with tags to the store
        ##self.__store_object.silent_add_item_list_with_tags(file_name_list, describing_tag_list, categorising_tag_list)
        
            
if __name__ == '__main__':  
  
    ## initialize and configure the optionparser
    opt_parser = OptionParser("tagstore_synchronizer.py [options]")
    opt_parser.add_option("-v", "--verbose", dest="verbose", action="store_true", help="start programm with detailed output")

    (options, args) = opt_parser.parse_args()
    
    verbose_mode = False
    dry_run = False
    
    if options.verbose:
        verbose_mode = True
    
    tagstore_synchronizer = QtGui.QApplication(sys.argv)
    tagstore_synchronizer.setApplicationName("tagstore_synchronizer")
    tagstore_synchronizer.setOrganizationDomain("www.tagstore.org")
    tagstore_synchronizer.UnicodeUTF8
    
    sync_widget = Synchronizer(tagstore_synchronizer, verbose=verbose_mode, standalone=True)
    sync_widget.synchronize()
    tagstore_synchronizer.exec_()
## end