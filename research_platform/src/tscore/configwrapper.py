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
        directory = unicode(self.__settings.value("store_config_directory", "").toString())
        self.__settings.endGroup()
        return directory.strip("/")
        
    def get_store_configfile_name(self):
        self.__settings.beginGroup("settings")
        config_file = unicode(self.__settings.value("store_config_filename", "").toString())
        self.__settings.endGroup()
        return config_file.strip("/")
        
    def get_tag_seperator(self):
        self.__settings.beginGroup("settings")
        seperator = unicode(self.__settings.value("tag_seperator", "").toString())
        self.__settings.endGroup()
        return seperator.strip()
        
    def get_stores(self):
        self.__settings.beginGroup("stores")
        store_ids = self.__settings.childKeys()
        stores = []
        for id in store_ids:
            path = unicode(self.__settings.value(id, "").toString()).strip("/")
            stores.append(dict(id=id, path=path))
        self.__settings.endGroup()
        return stores
    
    def rename_store(self, id, new_name):
        self.__settings.beginGroup("stores")
        self.__settings.setValue(id, new_name)
        self.__settings.endGroup()

    def remove_store(self, id):
        self.__settings.beginGroup("stores")
        self.__settings.remove(id)
        self.__settings.endGroup()


## end