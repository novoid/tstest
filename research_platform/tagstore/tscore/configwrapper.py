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

class ConfigWrapper(QtCore.QObject):

    __pyqtSignals__ = ("changed()", )

    def __init__(self, config_file_path):
        """
        constructor
        """
        QtCore.QObject.__init__(self)
        
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.addPath(config_file_path)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("fileChanged(QString)"), self.__file_changed)
        self.__settings = QtCore.QSettings(config_file_path, QtCore.QSettings.IniFormat)
        
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
        
    def get_store_configfile_name(self):
        """
        returns the parameter: stores config file name
        """
        config_file = self.__get_setting("store_config_filename")
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

    def get_show_datestamp(self):
        """
        returns "on" or "off" in case date-stamps are requested
        """
        #TODO: @chris: maybe you should change the return value to boolean
        return self.__get_setting(TsConstants.SETTING_AUTO_DATESTAMP)
    
    def get_datestamp_format(self):
        """
        returns the ISO timestamp format that should be used for tagging
        """
        return self.__get_setting(TsConstants.SETTING_DATESTAMP_FORMAT)
    
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
        return int(number.strip())
        
    def get_num_popular_tags(self):
        """
        returns the number of popular tags shown to the user
        """
        number = self.__get_setting("num_popular_tags")
        return int(number.strip())
        
    def get_max_tags(self):
        """
        returns the parameter: max_tags: the max allowed number of tags to enter 
        """
        number = self.__get_setting("max_tags")
        return int(number.strip())

    def get_store_path(self, id):
        """
        returns the path of a given store id
        """
        self.__settings.beginGroup("stores")
        path = unicode(self.__settings.value(id, "").toString())
        self.__settings.endGroup()
        return path.strip("/")

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


## end