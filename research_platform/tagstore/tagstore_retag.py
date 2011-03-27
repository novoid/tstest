#!/usr/bin/env python

# -*- coding: utf-8 -*-

## this file is part of tagstore_tag, an alternative way of storing and retrieving information
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
from tscore.loghelper import LogHelper
from tsgui.tagdialog import TagDialogController
from tscore.enums import EDateStampFormat, EConflictType, EFileEvent
from tscore.exceptions import NameInConflictException, InodeShortageException

class TagController(QtCore.QObject):

    def __init__(self, application, store_path, item_name = "", retag_mode = False, verbose = False):
        QtCore.QObject.__init__(self)
        
        self.__log = None
        self.__main_config = None
        self.__tag_dialog = None
        self.__store = None
        
        self.__store_path = store_path
        self.__item_name = item_name
        self.__retag_mode = retag_mode
        
        self.__store_config = ConfigWrapper(self.__store_path)
        # the main application which has the translator installed
        self.__application = application

        self.LOG_LEVEL = logging.INFO
        if verbose:
            self.LOG_LEVEL = logging.DEBUG
            

        self.STORE_CONFIG_DIR = TsConstants.DEFAULT_STORE_CONFIG_DIR
        self.STORE_CONFIG_FILE_NAME = TsConstants.DEFAULT_STORE_CONFIG_FILENAME
        self.STORE_TAGS_FILE_NAME = TsConstants.DEFAULT_STORE_TAGS_FILENAME
        self.STORE_VOCABULARY_FILE_NAME = TsConstants.DEFAULT_STORE_VOCABULARY_FILENAME
        
        locale = unicode(QtCore.QLocale.system().name())[0:2]
        self.__translator = QtCore.QTranslator()
        if self.__translator.load("ts_" + locale + ".qm", "tsresources/"):
#            tagstore_tag.installTranslator(self.__translator)
            self.__application.installTranslator(self.__translator)
        #get dir names for all available languages
        self.CURRENT_LANGUAGE = self.trUtf8("en")
        self.STORE_STORAGE_DIRS = []
        self.STORE_DESCRIBING_NAV_DIRS = []
        self.STORE_CATEGORIZING_NAV_DIRS = []
        self.STORE_EXPIRED_DIRS = []
        self.SUPPORTED_LANGUAGES = TsConstants.DEFAULT_SUPPORTED_LANGUAGES
        self.__store_dict = {}
        
        self.__log = LogHelper.get_app_logger(self.LOG_LEVEL)
        self.__init_configuration()
        
    def __init_configuration(self):
        """
        initializes the configuration. This method is called every time the config file changes
        """
        self.__log.info("initialize configuration")
        self.__main_config = ConfigWrapper(TsConstants.CONFIG_PATH)
        
        self.__prepare_store_params()
        
        self.CURRENT_LANGUAGE = self.__main_config.get_current_language();
        self.change_language(self.CURRENT_LANGUAGE)
        #self.__main_config.connect(self.__main_config, QtCore.SIGNAL("changed()"), self.__init_configuration)

        self.__store = Store(self.__store_config.get_store_id(), self.__store_path, 
              self.STORE_CONFIG_DIR + "/" + self.STORE_CONFIG_FILE_NAME,
              self.STORE_CONFIG_DIR + "/" + self.STORE_TAGS_FILE_NAME,
              self.STORE_CONFIG_DIR + "/" + self.STORE_VOCABULARY_FILE_NAME,
              self.STORE_STORAGE_DIRS, 
              self.STORE_DESCRIBING_NAV_DIRS,
              self.STORE_CATEGORIZING_NAV_DIRS,
              self.STORE_EXPIRED_DIRS,
              self.__main_config.get_expiry_prefix())
        self.__store.init()
        
        if self.__tag_dialog is None:
            self.__tag_dialog = TagDialogController(self.__store.get_name(), self.__main_config.get_max_tags(), self.__main_config.get_tag_seperator())
            self.__tag_dialog.get_view().setModal(True)
            #self.__tag_dialog.set_parent(self.sender().get_view())
            self.__tag_dialog.connect(self.__tag_dialog, QtCore.SIGNAL("tag_item"), self.__tag_item_action)
            self.__tag_dialog.connect(self.__tag_dialog, QtCore.SIGNAL("handle_cancel()"), self.__handle_tag_cancel)


        ## configure the tag dialog with the according settings
        format_setting = self.__store.get_datestamp_format()

        ## check if auto datestamp is enabled
        if format_setting != EDateStampFormat.DISABLED:
            self.__tag_dialog.show_datestamp(True)
            ## set the format
            format = None
            if format_setting == EDateStampFormat.DAY:
                format = TsConstants.DATESTAMP_FORMAT_DAY
            elif format_setting == EDateStampFormat.MONTH:
                format = TsConstants.DATESTAMP_FORMAT_MONTH
            self.__tag_dialog.set_datestamp_format(format)
        
        self.__tag_dialog.show_category_line(self.__store.get_show_category_line())
        self.__tag_dialog.set_category_mandatory(self.__store.get_category_mandatory()) 
        
        if self.__retag_mode:
            self.__handle_retag_mode()
            
        self.__set_tag_information_to_dialog(self.__store)
        self.__tag_dialog.show_dialog()
    
    def __handle_tag_rename(self, store_name, file_name, new_describing_tags, new_categorizing_tags):
        
        ## first of all remove the old references
        self.__store.remove_file(file_name)
        ## now create the new navigation structure
        self.__store.add_item_with_tags(file_name, new_describing_tags, new_categorizing_tags)
    
    def set_application(self, application):
        """
        if the manager is called from another qt application (e.g. tagstore.py)
        you must set the calling application here for proper i18n
        """
        self.__application = application
        
    def __handle_retag_mode(self):
        
        self.__tag_dialog.set_retag_mode()

        ## remove the tag command        
        self.__tag_dialog.disconnect(self.__tag_dialog, QtCore.SIGNAL("tag_item"), self.__tag_item_action)
        ## reconnect the signal to the re-tag action and not the default tag action
        self.__tag_dialog.connect(self.__tag_dialog, QtCore.SIGNAL("tag_item"), self.__handle_tag_rename)
        
        cat_content = ""
        cat_tags = self.__store.get_describing_tags_for_item(self.__item_name)
        for tag in cat_tags:
            if cat_content == "":
                cat_content = tag
            else:
                cat_content = "%s%s%s" %(cat_content,", ", tag) 
        self.__tag_dialog.set_describing_line_content(cat_content)
        desc_content = ""
        desc_tags = self.__store.get_categorizing_tags_for_item(self.__item_name)
        for tag in desc_tags:
            if desc_content == "":
                desc_content = tag
            else:
                desc_content = "%s%s%s" %(desc_content,", ", tag) 
        self.__tag_dialog.set_category_line_content(desc_content)
        
        self.__tag_dialog.add_pending_item(self.__item_name)

    def __set_tag_information_to_dialog(self, store):
        """
        convenience method for setting the tag data at the gui-dialog
        """
        self.__tag_dialog.set_tag_list(store.get_tags())
        
        num_pop_tags = self.__main_config.get_num_popular_tags()
        
        tag_set = set(store.get_popular_tags(self.__main_config.get_max_tags()))
        tag_set = tag_set | set(store.get_recent_tags(num_pop_tags))

        cat_set = set(store.get_popular_categories(num_pop_tags))
        cat_set = cat_set | set(store.get_recent_categories(num_pop_tags))

        cat_list = list(cat_set)
        if store.is_controlled_vocabulary():
            allowed_set = set(store.get_controlled_vocabulary())
            self.__tag_dialog.set_category_list(list(allowed_set))

            ## just show allowed tags - so make the intersection of popular tags ant the allowed tags
            cat_list = list(cat_set.intersection(allowed_set)) 
        else:
            self.__tag_dialog.set_category_list(store.get_categorizing_tags())
            
        if len(cat_list) > num_pop_tags:
            cat_list = cat_list[:num_pop_tags]
        self.__tag_dialog.set_popular_categories(cat_list)
        
        ## make a list out of the set, to enable indexing, as not all tags cannot be used
        tag_list = list(tag_set)
        if len(tag_list) > num_pop_tags:
            tag_list = tag_list[:num_pop_tags]
        self.__tag_dialog.set_popular_tags(tag_list)
        
        self.__tag_dialog.set_item_list(store.get_pending_changes().get_items_by_event(EFileEvent.ADDED))
        
        #added_list = set(store.get_pending_changes().get_items_by_event(EFileEvent.ADDED))
        #for item in added_list:
        #    self.__tag_dialog.add_pending_item(item)

        self.__tag_dialog.set_store_name(store.get_name())
    
    def __tag_item_action(self, item_name, tag_list, category_list):
        """
        the "tag!" button in the re-tag dialog has been clicked
        """
        try:
            ## 1. write the data to the store-file
            self.__store.add_item_with_tags(item_name, tag_list, category_list)
            self.__log.debug("added item %s to store-file", item_name)
        except NameInConflictException, e:
            c_type = e.get_conflict_type()
            c_name = e.get_conflicted_name()
            if c_type == EConflictType.FILE:
                self.__tag_dialog.show_message(self.trUtf8("The filename - %s - is in conflict with an already existing tag. Please rename!" % c_name))
            elif c_type == EConflictType.TAG:
                self.__tag_dialog.show_message(self.trUtf8("The tag - %s - is in conflict with an already existing file" % c_name))
            else:
                self.trUtf8("A tag or item is in conflict with an already existing tag/item")
            #raise
        except InodeShortageException, e:
            self.__tag_dialog.show_message(self.trUtf8("The Number of free inodes is below the threshold of %s%" % e.get_threshold()))
            #raise
        except Exception, e:
            self.__tag_dialog.show_message(self.trUtf8("An error occurred while tagging"))
            raise
        else:
            ## 2 remove the item in the gui
            self.__tag_dialog.remove_item(item_name)
            self.__tag_dialog.hide_dialog()
    
    def __handle_tag_cancel(self):
        """
        the "postpone" button in the re-tag dialog has been clicked
        """
        self.__tag_dialog.hide_dialog()
    
    def show_tag_dialog(self, show):
        self.__tag_dialog.show_dialog()
    
    def set_parent(self, parent):
        """
        set the parent for the admin-dialog if there is already a gui window
        """
        self.__admin_dialog.set_parent(parent)
    
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
    
if __name__ == '__main__':  
  
    ## initialize and configure the optionparser
    usage = "\nThis program opens a dialog used for tagging an item placed in a tagstore directory."
    opt_parser = OptionParser("tagstore_tag.py -s <store> -i <itemname>")
    opt_parser.add_option("-s", "--store", dest="store_path", help="absolute path to the store home dir")
    opt_parser.add_option("-i", "--item", dest="itemname", help="the name of the item to be tagged")
    #opt_parser.add_option("-r", "--retag", dest="retag", action="store_true", help="use this option if the item has to be retagged")
    opt_parser.add_option("-v", "--verbose", dest="verbose", action="store_true", help="start programm with detailed output")

    (options, args) = opt_parser.parse_args()
    
    verbose_mode = False
    retag_mode = False
    dry_run = False
    
    store_name = None
    item_name = None
    
    if options.verbose:
        verbose_mode = True
    #if options.retag:
    #    retag_mode = True
    if options.store_path:
        store_path = options.store_path
    else:
        print "no store name given"
        sys.exit()
    if options.itemname:
        item_name = options.itemname
    else:
        print "no item name given"
        sys.exit()
        
    print "opening store: %s for item: %s" % (store_path, item_name)
    
    tagstore_tag = QtGui.QApplication(sys.argv)
    tagstore_tag.setApplicationName("tagstore_retag")
    tagstore_tag.setOrganizationDomain("www.tagstore.org")
    tagstore_tag.UnicodeUTF8
    
#    admin_widget = TagController(tagstore_tag, store_path, item_name, retag_mode, verbose_mode)
    admin_widget = TagController(tagstore_tag, store_path, item_name, True, verbose_mode)
    admin_widget.show_tag_dialog(True)
    tagstore_tag.exec_()
## end