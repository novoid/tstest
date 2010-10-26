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

from PyQt4 import QtCore
from tscore.tsconstants import TsConstants
from tscore.enums import EDateStampFormat, ECategorySetting

class ConfigWrapper(QtCore.QObject):

    __pyqtSignals__ = ("changed()")

    GROUP_STORE_NAME = "store"
    KEY_STORE_ID = "store_id"

    def __init__(self, config_file_path):
        """
        constructor
        """
        QtCore.QObject.__init__(self)
        
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.addPath(config_file_path)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("fileChanged(QString)"), self.__file_changed)
        self.__settings = QtCore.QSettings(config_file_path, QtCore.QSettings.IniFormat)
    
    @staticmethod
    def create_store_config_file(file_path):
        """
        create a new tags file structure in the given file
        this  method has to be used in a static way
        the file must be opened with write permission
        """
        file = open(file_path, "w")
        file.write("[store]\n")
        file.write("store_id=0\n\n")
        file.write("[settings]\n")
        file.write("datestamp_format=1\n")
        file.write("show_category_line=1\n")
        file.write("category_mandatory=true\n")
        
        file.close()

    @staticmethod
    def create_app_config_file(file_path):
        """
        create a new application config file structure in the given file
        this  method has to be used in a static way
        the file must be opened with write permission
        """
        file = open(file_path, "w")
        
        file.write("[settings]\n")
        file.write("store_config_directory=%s\n" % TsConstants.DEFAULT_STORE_CONFIG_DIR)
        file.write("store_config_filename=%s\n" % TsConstants.DEFAULT_STORE_CONFIG_FILENAME)
        file.write("store_tags_filename=%s\n" % TsConstants.DEFAULT_STORE_TAGS_FILENAME)
        file.write("store_vocabulary_filename=%s\n" % TsConstants.DEFAULT_STORE_VOCABULARY_FILENAME)
        
        file.write("tag_separator=\"%s\"\n" % TsConstants.DEFAULT_TAG_SEPARATOR)
        file.write("supported_languages=\"en,de\"\n")
        file.write("datestamp_format=0\n")
        file.write("show_category_line=1\n")
        file.write("category_mandatory=false\n")
        file.write("current_language=de\n")
        file.write("expiry_prefix=expiration\n")
        file.write("max_tags=5\n")
        file.write("num_popular_tags=5\n")
        file.write("num_recent_tags=5\n")
        file.write("[stores]\n")
        
        file.close()
        
    def set_store_id(self, id):
        """
        writes the stores id to the configuration file
        """
        self.__settings.beginGroup(self.GROUP_STORE_NAME)
        self.__settings.setValue("store_id", id)
        self.__settings.endGroup()
    
    def get_store_id(self):
        """
        returns the store id of the current file to identify the store during rename
        """
        self.__settings.beginGroup(self.GROUP_STORE_NAME)
        id = unicode(self.__settings.value(self.KEY_STORE_ID, "").toString())
        self.__settings.endGroup()
        return id

    def __file_changed(self):
        """
        event handler: called when file was changed
        """
        self.__settings.sync()
        self.emit(QtCore.SIGNAL("changed()"))
        
    def get_store_config_directory(self):
        """
        returns the parameter: stores config directory name
        """
        directory = self.__get_setting("store_config_directory")
        return directory.strip("/")
        
    def get_store_tagsfile_name(self):
        """
        returns the parameter: stores config file name
        """
        config_file = self.__get_setting("store_tags_filename")
        return config_file.strip("/")

    def get_store_configfile_name(self):
        """
        returns the parameter: stores config file name
        """
        config_file = self.__get_setting("store_config_filename")
        return config_file.strip("/")
        
    def get_store_vocabularyfile_name(self):
        """
        returns the parameter: stores vocabulary file name
        """
        config_file = self.__get_setting("store_vocabulary_filename")
        return config_file.strip("/")
        
    def get_tag_seperator(self):
        """
        returns the parameter: tag separator for user interface 
        """
        return self.__get_setting("tag_separator")

    def get_supported_languages(self):
        """
        returns a list of all supported languages
        """
        lang_string = self.__get_setting(TsConstants.SETTING_SUPPORTED_LANGUAGES)
        return lang_string.split(",")

    def set_expiry_prefix(self, setting_value):
        self.__put_setting(TsConstants.SETTING_EXPIRY_PREFIX, setting_value)
        
    def get_expiry_prefix(self):
        """
        returns the prefix to be used for marking the expiration dates of items
        """
        return self.__get_setting(TsConstants.SETTING_EXPIRY_PREFIX)
        
        
    def get_show_datestamp(self):
        """
        returns "True" or "False" in case date-stamps are requested
        """
        if self.__get_setting(TsConstants.SETTING_AUTO_DATESTAMP) == "true":
            return True
        else:    
            return False
    
    def get_datestamp_format(self):
        """
        returns the timestamp setting that should be used for tagging
        """
        setting_value = self.__get_setting(TsConstants.SETTING_DATESTAMP_FORMAT)        
        if setting_value == "":
            return 0
        return int(setting_value.strip())
    
    def set_datestamp_format(self, setting_value):
        self.__put_setting(TsConstants.SETTING_DATESTAMP_FORMAT, setting_value)

    ## TODO: REMOVE ME - not needed anymore
    def get_show_category_line(self):
        """
        returns the enum code if the category line should be enabled in the tag dialog
        """
        
        setting_value = self.__get_setting(TsConstants.SETTING_SHOW_CATEGORY_LINE)
        if setting_value == "":
            return 0
        return int(setting_value.strip())

    def set_show_category_line(self, setting_value):
        self.__put_setting(TsConstants.SETTING_SHOW_CATEGORY_LINE, setting_value)

    def set_category_mandatory(self, setting_value):
        self.__put_setting(TsConstants.SETTING_CATEGORY_MANDATORY, setting_value)

    def get_category_mandatory(self):
        """
        returns True if the category line should be enabled in the tag dialog
        """
        if self.__get_setting(TsConstants.SETTING_CATEGORY_MANDATORY) == "true":
            return True
        else:    
            return False
    
    def __put_setting(self, setting_name, setting_value):
        """
        writes a new value to a specified setting
        """
        self.__settings.beginGroup("settings")
        value = self.__settings.setValue(setting_name, setting_value)
        self.__settings.endGroup()
        
    def __get_setting(self, setting_name):
        """
        helper method to switch directly to the settings group of the config file
        """
        self.__settings.beginGroup("settings")
        value = unicode(self.__settings.value(setting_name, "").toString())
        self.__settings.endGroup()
        return value.strip()
    
    def get_num_recent_tags(self):
        """
        returns the number of recent tags shown to the user
        """
        number = self.__get_setting("num_recent_tags")
        if number == "":
            return 0
        return int(number.strip())
        
    def get_num_popular_tags(self):
        """
        returns the number of popular tags shown to the user
        """
        number = self.__get_setting("num_popular_tags")
        if number == "":
            return 0
        return int(number.strip())
        
    def get_max_tags(self):
        """
        returns the parameter: max_tags: the max allowed number of tags to enter 
        """
        number = self.__get_setting("max_tags")
        if number == "":
            return 0
        return int(number.strip())

    def get_store_path(self, id):
        """
        returns the path of a given store id
        """
        self.__settings.beginGroup("stores")
        path = unicode(self.__settings.value(id, "").toString())
        self.__settings.endGroup()
        return path.rstrip("/")

    def get_stores(self):
        """
        returns a list of store objects (directories) including the stores id and path
        """
        self.__settings.beginGroup("stores")
        store_ids = self.__settings.childKeys()
        stores = []
        for id in store_ids:
            path = unicode(self.__settings.value(id, "").toString()).rstrip("/")
            stores.append(dict(id=unicode(id), path=path))
        self.__settings.endGroup()
        return stores
    
    def get_store_ids(self):
        """
        returns a list of all stores ids
        """
        self.__settings.beginGroup("stores")
        store_ids = self.__settings.childKeys()
        self.__settings.endGroup()
        stores = []
        for id in store_ids:
            stores.append(unicode(id))
        return stores
    
    def rename_store(self, id, new_name):
        """
        resets a stores path
        """
        self.__settings.beginGroup("stores")
        self.__settings.setValue(id, new_name)
        self.__settings.endGroup()

    def remove_store(self, id):
        """
        removes a store entry from the config file
        """
        self.__settings.beginGroup("stores")
        self.__settings.remove(id)
        self.__settings.endGroup()
    
    def add_new_store(self, store_path):
        """
        just write the new store-path to the config file
        the store structure will be created automatically
        returns the newly created store_id
        """
        ## get the highest id
        new_id_int = 1 
        id_list = self.get_store_ids()
        if(len(id_list) > 0):
            max_id = max(self.get_store_ids())
            max_id_int = int(max_id.strip())
            new_id_int = max_id_int + 1
        
        self.__settings.beginGroup("stores")
        self.__settings.setValue(str(new_id_int), store_path)
        self.__settings.endGroup()
        
        return str(new_id_int)

## end