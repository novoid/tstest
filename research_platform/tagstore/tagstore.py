#!/usr/bin/env python

# -*- coding: utf-8 -*-

## this file is part of tagstore, an alternative way of storing and retrieving information
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
from tsgui.tagdialog import TagDialogController
from tscore.configwrapper import ConfigWrapper
from tscore.store import Store
from tscore.enums import EFileEvent, EDateStampFormat, EConflictType
from tscore.tsconstants import TsConstants
from tscore.exceptions import StoreTaggingError, NameInConflictException,\
    InodeShortageException
from tagstore_manager import Administration

    
class Tagstore(QtCore.QObject):

    def __init__(self, parent=None, verbose=False, dryrun=False):
        """ 
        initializes the configuration. This method is called every time the config file changes
        """

        QtCore.QObject.__init__(self)
        
        self.DRY_RUN = dryrun        
        ## initialize localization
        locale = unicode(QtCore.QLocale.system().name())[0:2]
        self.__translator = QtCore.QTranslator()
        if self.__translator.load("ts_" + locale + ".qm", "tsresources/"):
            tagstore.installTranslator(self.__translator)
        self.CURRENT_LANGUAGE = self.trUtf8("en")
        self.SUPPORTED_LANGUAGES = TsConstants.DEFAULT_SUPPORTED_LANGUAGES
        
        ## global settings/defaults (only used if reading config file failed or invalid!)
        self.STORE_CONFIG_DIR = TsConstants.DEFAULT_STORE_CONFIG_DIR
        self.STORE_CONFIG_FILE_NAME = TsConstants.DEFAULT_STORE_CONFIG_FILENAME
        self.STORE_TAGS_FILE_NAME = TsConstants.DEFAULT_STORE_TAGS_FILENAME
        self.STORE_VOCABULARY_FILE_NAME = TsConstants.DEFAULT_STORE_VOCABULARY_FILENAME

        #get dir names for all available languages
        store_current_language = self.CURRENT_LANGUAGE 
        self.STORE_STORAGE_DIRS = []
        self.STORE_DESCRIBING_NAV_DIRS = []
        self.STORE_CATEGORIZING_NAV_DIRS = []
        self.STORE_EXPIRED_DIRS = []

        for lang in self.SUPPORTED_LANGUAGES: 
            self.change_language(lang) 
            self.STORE_STORAGE_DIRS.append(self.trUtf8("storage"))#self.STORE_STORAGE_DIR_EN))  
            self.STORE_DESCRIBING_NAV_DIRS.append(self.trUtf8("navigation"))#self.STORE_DESCRIBING_NAVIGATION_DIR_EN))  
            self.STORE_CATEGORIZING_NAV_DIRS.append(self.trUtf8("categorization"))#self.STORE_CATEGORIZING_NAVIGATION_DIR_EN)) 
            self.STORE_EXPIRED_DIRS.append(self.trUtf8("expired_items"))#STORE_EXPIRED_DIR_EN)) 
        ## reset language 
        self.change_language(store_current_language) 
     
        self.EXPIRY_PREFIX = TsConstants.DEFAULT_EXPIRY_PREFIX
        self.TAG_SEPERATOR = TsConstants.DEFAULT_TAG_SEPARATOR
        self.NUM_RECENT_TAGS = TsConstants.DEFAULT_RECENT_TAGS
        self.NUM_POPULAR_TAGS = TsConstants.DEFAULT_POPULAR_TAGS
        self.MAX_TAGS = TsConstants.DEFAULT_MAX_TAGS
        self.STORES = []
        ## dict for dialogs identified by their store id
        self.DIALOGS = {}
        
        ## init configurations
        self.__app_config_wrapper = None
        self.__log = None
        
        self.LOG_LEVEL = logging.INFO
        if verbose:
            self.LOG_LEVEL = logging.DEBUG
            
        self.__init_logger(self.LOG_LEVEL)
        self.__log.info("starting tagstore watcher")
        
        self.__init_configurations()
        
    def __init_logger(self, level):
        '''
        create a logger object with appropriate settings
        '''
        # TODO create a class for doing this
        LOG_FILENAME = TsConstants.LOG_FILENAME
        self.__log = logging.getLogger(TsConstants.LOGGER_NAME)
        self.__log.setLevel(logging.DEBUG)

        #logging.basicConfig(level=logging.INFO)
        formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")

        ## create console handler and set level
        console_handler = logging.StreamHandler()
        console_handler.setLevel(level)
        console_handler.setFormatter(formatter)

        ## create filehandler
        file_handler = logging.handlers.RotatingFileHandler(LOG_FILENAME, 
            maxBytes=TsConstants.LOG_FILESIZE, backupCount=TsConstants.LOG_BACKUP_COUNT)
        file_handler.setFormatter(formatter)

        ## add handlers to logger
        self.__log.addHandler(console_handler)
        self.__log.addHandler(file_handler)        

    def __init_configurations(self):
        """
        initializes the configuration. This method is called every time the config file changes
        """
        self.__log.info("initialize configuration")
        ## reload config file - overwrite default settings
        self.__app_config_wrapper = ConfigWrapper(TsConstants.CONFIG_PATH)
        self.__app_config_wrapper.connect(self.__app_config_wrapper, QtCore.SIGNAL("changed()"), self.__init_configurations)
        tag_seperator = self.__app_config_wrapper.get_tag_seperator()
        if tag_seperator.strip() != "":
            self.TAG_SEPERATOR = tag_seperator
        expiry_prefix = self.__app_config_wrapper.get_expiry_prefix()
        if expiry_prefix.strip() != "":
            self.EXPIRY_PREFIX = expiry_prefix
        
        self.NUM_RECENT_TAGS = self.__app_config_wrapper.get_num_recent_tags()
        self.NUM_POPULAR_TAGS = self.__app_config_wrapper.get_num_popular_tags()
        self.MAX_TAGS = self.__app_config_wrapper.get_max_tags()
            
        config_dir = self.__app_config_wrapper.get_store_config_directory()
        if config_dir != "":
            self.STORE_CONFIG_DIR = config_dir
        config_file_name = self.__app_config_wrapper.get_store_configfile_name()
        if config_file_name != "":
            self.STORE_CONFIG_FILE_NAME = config_file_name
        tags_file_name = self.__app_config_wrapper.get_store_tagsfile_name()
        if tags_file_name != "":
            self.STORE_TAGS_FILE_NAME = tags_file_name
        vocabulary_file_name = self.__app_config_wrapper.get_store_vocabularyfile_name()
        if vocabulary_file_name != "":
            self.STORE_VOCABULARY_FILE_NAME = vocabulary_file_name
        
       
#        self.SUPPORTED_LANGUAGES = self.__app_config_wrapper.get_supported_languages()
#        current_language = self.CURRENT_LANGUAGE
#        self.STORE_STORAGE_DIRS = []
#        self.STORE_NAVIGATION_DIRS = [] 
#        for lang in self.SUPPORTED_LANGUAGES:
#            self.change_language(lang)
#            self.STORE_STORAGE_DIRS.append(self.trUtf8("storage")) 
#            self.STORE_NAVIGATION_DIRS.append(self.trUtf8("navigation")) 
#        ## reset language
#        self.change_language(current_language)

        ## get stores from config file         
        config_store_items = self.__app_config_wrapper.get_stores()
        config_store_ids = self.__app_config_wrapper.get_store_ids()

        deleted_stores = []
        for store in self.STORES:
            id = store.get_id()
            if id in config_store_ids:
            ## update changed stores
                store.set_path(self.__app_config_wrapper.get_store_path(id), 
                               self.STORE_CONFIG_DIR + "/" + self.STORE_CONFIG_FILE_NAME,
                               self.STORE_CONFIG_DIR + "/" + self.STORE_TAGS_FILE_NAME,
                               self.STORE_CONFIG_DIR + "/" + self.STORE_VOCABULARY_FILE_NAME)
                store.change_expiry_prefix(self.EXPIRY_PREFIX)               
                config_store_ids.remove(id)             ## remove already updated items
            else:
            ## remove deleted stores
                deleted_stores.append(store)

        ## update deleted stores from global list after iterating through it
        for store in deleted_stores:
            self.STORES.remove(store)
            self.__log.debug("removed store: %s", store.get_name())
        
        ## add new stores
        for store_item in config_store_items:
            if store_item["id"] in config_store_ids:    ## new
                store = Store(store_item["id"], store_item["path"], 
                              self.STORE_CONFIG_DIR + "/" + self.STORE_CONFIG_FILE_NAME,
                              self.STORE_CONFIG_DIR + "/" + self.STORE_TAGS_FILE_NAME,
                              self.STORE_CONFIG_DIR + "/" + self.STORE_VOCABULARY_FILE_NAME,
                              self.STORE_STORAGE_DIRS, 
                              self.STORE_DESCRIBING_NAV_DIRS,
                              self.STORE_CATEGORIZING_NAV_DIRS,
                              self.STORE_EXPIRED_DIRS,
							  self.EXPIRY_PREFIX)

                store.connect(store, QtCore.SIGNAL("removed(PyQt_PyObject)"), self.store_removed)
                store.connect(store, QtCore.SIGNAL("renamed(PyQt_PyObject, QString)"), self.store_renamed)
                store.connect(store, QtCore.SIGNAL("file_renamed(PyQt_PyObject, QString, QString)"), self.file_renamed)
                store.connect(store, QtCore.SIGNAL("file_removed(PyQt_PyObject, QString)"), self.file_removed)
                store.connect(store, QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self.pending_file_operations)
                store.connect(store, QtCore.SIGNAL("vocabulary_changed"), self.__handle_vocabulary_changed)

                self.STORES.append(store)

                self.__log.debug("init store: %s", store.get_name())
                
#            if len(self.STORES) == len(config_store_items):

#               for store in self.STORES:
                ## create a dialogcontroller for each store ...
                ## can be accessed by its ID later on
                tmp_dialog = TagDialogController(store.get_name(), self.MAX_TAGS, self.TAG_SEPERATOR)
                format_setting = store.get_datestamp_format()

                ##check if auto datestamp is enabled
                if format_setting != EDateStampFormat.DISABLED:
                    tmp_dialog.show_datestamp(True)
                    ## set the format
                    format = None
                    if format_setting == EDateStampFormat.DAY:
                        format = TsConstants.DATESTAMP_FORMAT_DAY
                    elif format_setting == EDateStampFormat.MONTH:
                        format = TsConstants.DATESTAMP_FORMAT_MONTH
                    tmp_dialog.set_datestamp_format(format)
                        
                tmp_dialog.show_category_line(store.get_show_category_line())
                tmp_dialog.set_category_mandatory(store.get_category_mandatory())
                
                tmp_dialog.connect(tmp_dialog, QtCore.SIGNAL("tag_item"), self.tag_item_action)
                tmp_dialog.connect(tmp_dialog, QtCore.SIGNAL("handle_cancel()"), self.handle_cancel)
                tmp_dialog.connect(tmp_dialog, QtCore.SIGNAL("open_store_admin_dialog()"), self.show_admin_dialog)
                #self.DIALOGS[store.get_id()] = tmp_dialog
                self.DIALOGS[store.get_id()] = tmp_dialog
                ## call init to initialize new store instance (after adding the event handler)
                ## necessary if store was renamed during tagstore was not running (to write config)
                store.init()
                
                    

    def __handle_vocabulary_changed(self, store):
        self.__set_tag_information_to_dialog(store)
    
    def show_admin_dialog(self):
        admin_widget = Administration(verbose=verbose_mode)
        admin_widget.set_parent(self.sender().get_view())
        admin_widget.show_admin_dialog(True)
    
    def store_removed(self, store):
        """
        event handler of the stores remove event
        """
        self.__app_config_wrapper.remove_store(store.get_id())
        ## __init_configuration is called due to config file changes
        
    def store_renamed(self, store, new_path):
        """
        event handler of the stores rename event
        """
        self.__app_config_wrapper.rename_store(store.get_id(), new_path)
        ## __init_configuration is called due to config file changes
        
    def file_renamed(self, store, old_file_name, new_file_name):
        """
        event handler for: file renamed
        """
        self.__log.debug("..........file renamed %s, %s" % (old_file_name, new_file_name))
        store.rename_file(old_file_name, new_file_name)
        
    def file_removed(self, store, file_name):
        """
        event handler for: file renamed
        """
        self.__log.debug("...........file removed %s" % file_name)
        store.remove_file(file_name)
        
    def pending_file_operations(self, store):
        """
        event handler: handles all operations with user interaction
        """
        #TODO @chris: encoding problem?
        
        self.__log.info("new pending file operation added")
        
        dialog_controller = self.DIALOGS[store.get_id()]
        
        #dialog_controller.clear_store_children(store.get_name())
        dialog_controller.clear_all_items()
        
        added_list = set(store.get_pending_changes().get_items_by_event(EFileEvent.ADDED))
        added_renamed_list = set(store.get_pending_changes().get_items_by_event(EFileEvent.ADDED_OR_RENAMED))
        
        whole_list = added_list | added_renamed_list
        
        if whole_list is None or len(whole_list) == 0:
            dialog_controller.hide_dialog()
            return
        self.__log.debug("store: %s, item: %s " % (store.get_id(), store.get_pending_changes().to_string()))
        
        for item in added_list:
            dialog_controller.add_pending_item(item)
            
        self.__set_tag_information_to_dialog(store)
        dialog_controller.show_dialog()
     
    def handle_cancel(self):
        dialog_controller = self.sender()
        if dialog_controller is None or not isinstance(dialog_controller, TagDialogController):
            return
        dialog_controller.hide_dialog()
    
    def __set_tag_information_to_dialog(self, store):
        """
        convenience method for refreshing the tag data at the gui-dialog
        """
        self.__log.debug("refresh tag information on dialog")
        dialog_controller = self.DIALOGS[store.get_id()]
        dialog_controller.set_tag_list(store.get_tags())
        
        tag_set = set(store.get_popular_tags(self.NUM_POPULAR_TAGS))
        tag_set = tag_set | set(store.get_recent_tags(self.NUM_RECENT_TAGS))

        cat_set = set(store.get_popular_categories(self.NUM_POPULAR_TAGS))
        cat_set = cat_set | set(store.get_recent_categories(self.NUM_RECENT_TAGS))

        #dialog_controller.set_category_list(store.get_category_list())
        dialog_controller.set_category_list(store.get_controlled_vocabulary())
        
        ## make a list out of the set, to enable indexing, as not all tags cannot be used
        tag_list = list(tag_set)
        if len(tag_list) > self.NUM_POPULAR_TAGS:
            tag_list = tag_list[:self.NUM_POPULAR_TAGS]
        dialog_controller.set_popular_tags(tag_list)

        cat_list = list(cat_set)
        if len(cat_list) > self.NUM_POPULAR_TAGS:
            cat_list = cat_list[:self.NUM_POPULAR_TAGS]
        dialog_controller.set_popular_categories(cat_list)

        #if len(self.DIALOGS) > 1:
        dialog_controller.set_store_name(store.get_name())
    
    def tag_item_action(self, store_name, item_name, tag_list, category_list):
        """
        write the tags for the given item to the store
        """
        
        store = None
        ## find the store where the item should be saved    
        for loop_store in self.STORES:
            if store_name == loop_store.get_name():
                store = loop_store
                break
        dialog_controller = self.DIALOGS[store.get_id()]
        try:
            ## 1. write the data to the store-file
            store.add_item_with_tags(item_name, tag_list, category_list)
            self.__log.debug("added item %s to store-file", item_name)
        except NameInConflictException, e:
            c_type = e.get_conflict_type()
            c_name = e.get_conflicted_name()
            if c_type == EConflictType.FILE:
                dialog_controller.show_message(self.trUtf8("The filename - %s - is in conflict with an already existing tag" % c_name))
            elif c_type == EConflictType.TAG:
                dialog_controller.show_message(self.trUtf8("The tag - %s - is in conflict with an already existing file" % c_name))
            else:
                self.trUtf8("A tag or item is in conflict with an already existing tag/item")
            #raise
        except InodeShortageException, e:
            dialog_controller.show_message(self.trUtf8("The Number of free inodes is below the threshld of %s%" % e.get_threshold()))
            #raise
        except Exception, e:
            dialog_controller.show_message(self.trUtf8("An error occurred while tagging"))
            raise
        else:
            ## 2 remove the item in the gui
            dialog_controller.remove_item(item_name)
            ## 3. refresh the tag information of the gui
            self.__set_tag_information_to_dialog(store)
        

    def change_language(self, locale):
        """
        changes the current application language
        please notice: this method is used to find all available storage/navigation directory names
        this is why it should not be extended to call any UI update methods directly
        """
        
        ## delete current translation to switch to default strings
        tagstore.removeTranslator(self.__translator)

        ## load new translation file        
        self.__translator = QtCore.QTranslator()
        language = unicode(locale)
        if self.__translator.load("ts_" + language + ".qm", "tsresources/"):
            tagstore.installTranslator(self.__translator)
            
        ## update current language
        self.CURRENT_LANGUAGE = self.trUtf8("en")
        
if __name__ == '__main__':  
  
    ## initialize and configure the optionparser
    opt_parser = OptionParser("tagstore.py [options]")
    opt_parser.add_option("-v", "--verbose", dest="verbose", action="store_true", help="start program with detailed output")
    opt_parser.add_option("-d", "--dry-run", dest="dry_run", action="store_true", help="test-mode. actions are just written to ouput. no changes to filesystem made.")

    (options, args) = opt_parser.parse_args()
    
    verbose_mode = False
    dry_run = False
    
    if options.verbose:
        verbose_mode = True
    if options.dry_run:
        dry_run = True
    
    tagstore = QtGui.QApplication(sys.argv)
    tagstore.setApplicationName("tagstore")
    tagstore.setOrganizationDomain("www.tagstore.org")
    tagstore.UnicodeUTF8
    
    tag_widget = Tagstore(verbose=verbose_mode, dryrun=dry_run)
    tagstore.exec_()
    #sys.exit(tagstore.exec_())
    
    
## end    
