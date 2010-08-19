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


class ConfigWrapper(QtCore.QObject):

    __pyqtSignals__ = ("changed()", )

    def __init__(self, config_file_path):
        QtCore.QObject.__init__(self)
        
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.addPath(config_file_path)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("fileChanged(QString)"), self.__file_changed)
        self.__settings = QtCore.QSettings(config_file_path, QtCore.QSettings.IniFormat)
        
    def __file_changed(self):
        self.__settings.sync()
        self.emit(QtCore.SIGNAL("changed()"))
        
    def get_store_config_directory(self):
        self.__settings.beginGroup("settings")
        directory = unicode(self.__settings.value("store_config_directory", ".ts").toString())
        self.__settings.endGroup()
        return directory
        
    def get_store_configfile_name(self):
        self.__settings.beginGroup("settings")
        extention = unicode(self.__settings.value("store_config_extention", "tgs").toString())
        self.__settings.endGroup()
        return extention
        
    def get_tag_seperator(self):
        self.__settings.beginGroup("settings")
        seperator = unicode(self.__settings.value("tag_seperator", ", ").toString())
        self.__settings.endGroup()
        return seperator
        
    def get_stores(self):
        self.__settings.beginGroup("stores")
        storeKeys = self.__settings.childKeys()
        self.__settings.endGroup()
        stores = []
        for id in storeKeys:
            stores.append(dict(id=id, path=unicode(self.__settings.value(id, "").toString())))
        return stores
    
    def rename_store(self, id, new_name):
        self.__settings.setValue(id, new_name)

    def remove_store(self, id):
        self.__settings.remove(id)


## end