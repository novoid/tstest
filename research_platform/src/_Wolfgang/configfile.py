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


class ConfigFile(QtCore.QObject):

    __pyqtSignals__ = ("changed()", )

    def __init__(self, configfilePath):
        QtCore.QObject.__init__(self)
        
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.addPath(configfilePath)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("fileChanged(QString)"), self.__fileChanged)
        self.__settings = QtCore.QSettings(configfilePath, QtCore.QSettings.IniFormat)
        self.__settings.setValue("FileName.txt", "tag1, tAG, ASD")
        
    def __fileChanged(self):
        self.__settings.sync()
        self.emit(QtCore.SIGNAL("changed()"))
        
    def getStoreConfigDirectory(self):
        self.__settings.beginGroup("settings")
        directory = unicode(self.__settings.value("store_config_directory", ".ts").toString())
        self.__settings.endGroup()
        return directory
        
    def getStoreConfigExtention(self):
        self.__settings.beginGroup("settings")
        extention = unicode(self.__settings.value("store_config_extention", "tgs").toString())
        self.__settings.endGroup()
        return extention
        
    def getTagSeperator(self):
        self.__settings.beginGroup("settings")
        seperator = unicode(self.__settings.value("tag_seperator", ", ").toString())
        self.__settings.endGroup()
        return seperator
        
    def getStores(self):
        self.__settings.beginGroup("stores")
        storeKeys = self.__settings.childKeys()
        stores = []
        for key in storeKeys:
            stores.append(dict(key=key, path=unicode(self.__settings.value(key, "").toString())))
        return stores
    
    def renameStore(self, key, newName):
        self.__settings.setValue(key, newName)

    def removeStore(self, key):
        self.__settings.remove(key)


## end