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
from windows import FileSystem


class Store(QtCore.QObject):

    __pyqtSignals__ = ("removed(PyQt_PyObject)",
                       "renamed(PyQt_PyObject, QString)",
                       "fileAdded(PyQt_PyObject, QString)",
                       "fileRenamed(PyQt_PyObject, QString)",
                       "fileRemoved(PyQt_PyObject, QString)")

    def __init__(self, key, path, configPath, configExtention):
        QtCore.QObject.__init__(self)

        self.__fileSystem = FileSystem()
        
        self.__key = unicode(key)
        self.__path = unicode(path)
        self.__configPath = unicode(configPath)
        self.__configExtention = unicode(configExtention)
        self.__name = self.__path.strip("/").split("/")[-1]
        self.__parentPath = self.__path.strip("/")[:len(self.__path)-len(self.__name)-1]
        self.__tagFile = self.__configPath + "/" + self.__name + "." + self.__configExtention
        self.__watcher = QtCore.QFileSystemWatcher(self)
        if self.__fileSystem.pathExists(self.__parentPath):
            self.__watcher.addPath(self.__parentPath)
        if self.__fileSystem.pathExists(self.__path):
            self.__watcher.addPath(self.__path)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("directoryChanged(QString)"), self.__directoryChanged)
        
    def __directoryChanged(self, path):
        if path == self.__parentPath:
            if not self.__fileSystem.pathExists(self.__path):
                ## store itself was changed: renamed, moved or deleted
                self.__watcher.removePath(self.__parentPath)
                renamedPath = self.__fileSystem.find(self.__parentPath, self.__tagFile)
                if renamedPath == "":    ## removed
                    self.emit(QtCore.SIGNAL("removed(PyQt_PyObject)"), self)
                else:                    ## renamed
                    self.emit(QtCore.SIGNAL("renamed(PyQt_PyObject, QString)"), self, renamedPath)
        else:
            ## files or directories in the store directory have been changed
            self.__handleFileChanges(path)
            
    def __handleFileChanges(self, path):
        ## this method does not handle the renaming or deletion of the store directory itself (only childs)
        print "new file: " + path
        #TODO: search for changes and emit events
                
    def getName(self):
        return self.__name
    
    def getConfigKey(self):
        return unicode(self.__key)
    
    def rename(self, newPath):
        self.__path = unicode(newPath)
        oldTagFile = self.__path + "/" + self.__tagFile 
        self.__name = self.__path.strip("/").split("/")[-1]
        self.__tagFile = self.__configPath + "/" + self.__name + "." + self.__configExtention
        self.__fileSystem.rename(oldTagFile, self.__path + "/" + self.__tagFile)
         
    def dispose(self):
        self.__watcher.removePaths([self.__parentPath, self.__path])
        
    def addTags(self, file, tagList):
        pass
        
    def renameTag(self, currentName, newName):
        pass
        
    def deleteTags(self, tagList):
        pass
        
    def getDictionary(self):
        pass
        
## end    