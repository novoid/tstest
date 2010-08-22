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
from PyQt4 import QtCore, QtGui
from tsgui.taggingdialog import TaggingDialog
from tscore.configwrapper import ConfigWrapper
from tscore.store import Store


# path to the config file
CONFIG_PATH = "../conf/tagstore.cfg"
    
class Tagstore():

    def __init__(self, parent=None):
        """ 
        initializes the configuration. This method is called every time the config file changes
        """

        ## global settings/defaults (only used if reading config file failed or invalid!)
        self.STORE_CONFIG_DIR = ".tagstore"
        self.STORE_CONFIG_FILE_NAME = "store.tgs"
        self.TAG_SEPERATOR = ","
        self.STORES = []
        
        ## init configurations
        self.__config_file = None
        self.__init_configurations()
        
        ## init UI
        self.__tagging_dialog = TaggingDialog()
        self.__update_tag_list()
        
        self.__tagging_dialog.connect(self.__tagging_dialog, QtCore.SIGNAL("tag_text_edited(QString)"), self.tag_text_edited)
        self.__tagging_dialog.connect(self.__tagging_dialog, QtCore.SIGNAL("tag_completion_activated(QString)"), self.tag_completion_activated)
        self.__tagging_dialog.connect(self.__tagging_dialog, QtCore.SIGNAL("tag_preference_activated(QString)"), self.tag_preference_activated)
        self.__tagging_dialog.connect(self.__tagging_dialog, QtCore.SIGNAL("category_text_edited(QString)"), self.category_text_edited)
        self.__tagging_dialog.connect(self.__tagging_dialog, QtCore.SIGNAL("category_completion_activated(QString)"), self.category_completion_activated)

        self.__tagging_dialog.connect(self.__tagging_dialog, QtCore.SIGNAL("confirm_button_pressed()"), self.confirm_button_pressed)
        self.__tagging_dialog.connect(self.__tagging_dialog, QtCore.SIGNAL("cancel_button_pressed()"), self.cancel_button_pressed)
        self.__tagging_dialog.connect(self.__tagging_dialog, QtCore.SIGNAL("cancel_all_button_pressed()"), self.cancel_all_button_pressed)

#test getter/setter        
        self.__tagging_dialog.set_store_label_text("store name")
        self.__tagging_dialog.set_file_label_text("current file")
        self.__tagging_dialog.show()
        
#test end

    def __init_configurations(self):
        """
        initializes the configuration. This method is called every time the config file changes
        """
    
        ## reload config file
        self.__config_file = ConfigWrapper(CONFIG_PATH)
        self.__config_file.connect(self.__config_file, QtCore.SIGNAL("changed()"), self.__init_configurations)
        tag_seperator = self.__config_file.get_tag_seperator()
        if tag_seperator.strip() != "":
            self.TAG_SEPERATOR = tag_seperator
            
        config_dir = self.__config_file.get_store_config_directory()
        if config_dir != "":
            self.STORE_CONFIG_DIR = config_dir
        config_file_name = self.__config_file.get_store_configfile_name()
        if config_file_name != "":
            self.STORE_CONFIG_FILE_NAME = config_file_name
            
        ## get stores from config file         
        config_store_items = self.__config_file.get_stores()
        config_store_ids = self.__config_file.get_store_ids()

        deleted_stores = []
        for store in self.STORES:
            id = store.get_id()
            if id in config_store_ids:
            ## update changed stores
                store.set_path(self.__config_file.get_store_path(id), self.STORE_CONFIG_DIR + "/" + self.STORE_CONFIG_FILE_NAME)
                config_store_ids.remove(id)             ## remove already updated items
                #print "updated: " + id
            else:
            ## remove deleted stores
                deleted_stores.append(store)
                #print "remove: " + id
        ## update deleted stores from global list after iterating through it
        for store in deleted_stores:
            self.STORES.remove(store)
        
        ## add new stores
        for store_item in config_store_items:
            if store_item["id"] in config_store_ids:    ## new
                store = Store(store_item["id"],store_item["path"], self.STORE_CONFIG_DIR + "/" + self.STORE_CONFIG_FILE_NAME)
                store.connect(store, QtCore.SIGNAL("removed(PyQt_PyObject)"), self.store_removed)
                store.connect(store, QtCore.SIGNAL("renamed(PyQt_PyObject, QString)"), self.store_renamed)
                store.connect(store, QtCore.SIGNAL("file_renamed(PyQt_PyObject, QString, QString)"), self.file_renamed)
                store.connect(store, QtCore.SIGNAL("file_removed(PyQt_PyObject, QString)"), self.file_removed)
                store.connect(store, QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self.pending_file_operations)
                ## handle offline changes
                store.handle_offline_changes()
                self.STORES.append(store)
            
    def store_removed(self, store):
        """
        event handler of the stores remove event
        """
        self.__config_file.remove_store(store.get_id())
        ## __init_configuration is called due to config file changes
        
    def store_renamed(self, store, new_path):
        """
        event handler of the stores rename event
        """
        self.__config_file.rename_store(store.get_id(), new_path)
        ## __init_configuration is called due to config file changes
        
    def file_renamed(self, store, old_file_name, new_file_name):
        """
        event handler for: file renamed
        """
        #print "file renamed: " + old_file_name + " -> " + new_file_name
        store.rename_file(old_file_name, new_file_name)
        
    def file_removed(self, store, file_name):
        """
        event handler for: file renamed
        """
        #print "file removed: " + file_name
        store.remove_file(file_name)
        
    def pending_file_operations(self, store):
        """
        event handler: handles all operations with user interaction
        """
        print "pending operation handler:"
        print  store.get_pending_changes().files_to_string()
        
    def tag_text_edited(self, text):
        """
        event handler of the text_changed event: this is triggered when typing text
        """
        self.__update_tag_list()
        cursor_left_text = unicode(text)[:self.__tagging_dialog.get_tag_cursor_position()]
        lookup_prefix = cursor_left_text.split(self.TAG_SEPERATOR)[-1].strip()
        self.__tagging_dialog.set_tag_completion_prefix(lookup_prefix)
        
    def tag_completion_activated(self, text):
        """
        event handler: triggered if a text was selected from the completer during typing
        """
        self.__update_tag_list()
        cursor_pos = self.__tagging_dialog.get_tag_cursor_position()
        current_text = self.__tagging_dialog.get_tag_text()
        cursor_left_text = unicode(current_text)[:cursor_pos]
        cursor_right_text = unicode(current_text)[cursor_pos:]
        
        if cursor_right_text.lstrip().startswith(self.TAG_SEPERATOR):
            cursor_right_text = " " + cursor_right_text.lstrip()
        else:
            cursor_right_text = self.TAG_SEPERATOR + " " + cursor_right_text.lstrip()
        prefix_length = len(cursor_left_text.split(self.TAG_SEPERATOR)[-1])

        left_text = cursor_left_text[:cursor_pos - prefix_length]
        if left_text != "":
            left_text += " "#cursor_left_text = cursor_left_text[:cursor_pos - prefix_length] + " "
        self.__tagging_dialog.set_tag_text(left_text + text + cursor_right_text)
        self.__tagging_dialog.set_tag_cursor_position(len(left_text) + len(text) + len(self.TAG_SEPERATOR) + 1)   
        
    def tag_preference_activated(self, text):
        """
        event handler: triggered if a text was selected from the dropdown (right side)
        """
        self.__update_tag_list()
        current_text = self.__tagging_dialog.get_tag_text()
        if current_text.strip() == "":
            self.__tagging_dialog.set_tag_text(text + self.TAG_SEPERATOR + " ")
        elif current_text.rstrip().endswith(self.TAG_SEPERATOR):
            self.__tagging_dialog.set_tag_text(current_text.rstrip() + " " + text + self.TAG_SEPERATOR + " ")
        else:
            self.__tagging_dialog.set_tag_text(current_text.rstrip() + self.TAG_SEPERATOR + " " + text + self.TAG_SEPERATOR + " ")
        
    def __update_tag_list(self):
        """
        loads available items into the tag control 
        """
        used_tags = self.__tagging_dialog.get_tag_text().split(self.TAG_SEPERATOR)
        
        #zentales tagDictionary: einlesen der config, einlesen
        self.__tagging_dialog.set_tag_lookup_list(['aa','abb','xaa b','cabb','daa','abb','abc'])
        self.__tagging_dialog.set_tag_preferences(['f','g','h'])
        self.__tagging_dialog.set_category_lookup_list(['aa','abb','xaa b','cabb','daa','abb','abc'])
        
    def category_text_edited(self, text):
        """
        event handler of the text_changed event: this is triggered when typing text
        """
        self.__update_tag_list()
        cursor_left_text = unicode(text)[:self.__tagging_dialog.get_category_cursor_position()]
        lookup_prefix = cursor_left_text.split(self.TAG_SEPERATOR)[-1].strip()
        self.__tagging_dialog.set_category_completion_prefix(lookup_prefix)
        
    def category_completion_activated(self, text):
        """
        event handler: triggered if a text was selected from the completer during typing
        """
        self.__update_tag_list()
        cursor_pos = self.__tagging_dialog.get_category_cursor_position()
        current_text = self.__tagging_dialog.get_category_text()
        cursor_left_text = unicode(current_text)[:cursor_pos]
        cursor_right_text = unicode(current_text)[cursor_pos:]
        
        if cursor_right_text.lstrip().startswith(self.TAG_SEPERATOR):
            cursor_right_text = " " + cursor_right_text.lstrip()
        else:
            cursor_right_text = self.TAG_SEPERATOR + " " + cursor_right_text.lstrip()
        prefix_length = len(cursor_left_text.split(self.TAG_SEPERATOR)[-1])

        left_text = cursor_left_text[:cursor_pos - prefix_length]
        if left_text != "":
            left_text += " "#cursor_left_text = cursor_left_text[:cursor_pos - prefix_length] + " "
        self.__tagging_dialog.set_category_text(left_text + text + cursor_right_text)
        self.__tagging_dialog.set_category_cursor_position(len(left_text) + len(text) + len(self.TAG_SEPERATOR) + 1)   
        
    def confirm_button_pressed(self):
        """
        """
        print "confirm"
 
    def cancel_button_pressed(self):
        """
        """
        print "cancel"
        
    def cancel_all_button_pressed(self):
        """
        """
        print "cancelAll"
        
        
if __name__ == '__main__':  
  
    tagstore = QtGui.QApplication(sys.argv)
    tag_widget = Tagstore()
    sys.exit(tagstore.exec_())


## end    