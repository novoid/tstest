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
from tsos.filesystem import FileSystemWrapper

class Store(QtCore.QObject):

    __pyqtSignals__ = ("removed(PyQt_PyObject)",
                       "renamed(PyQt_PyObject, QString)",
                       "file_added(PyQt_PyObject, QString)",
                       "file_renamed(PyQt_PyObject, QString)",
                       "file_removed(PyQt_PyObject, QString)")

    def __init__(self, id, path, config_path, config_file_name):
        """
        constructor
        """
    
        QtCore.QObject.__init__(self)

        self.__file_system = FileSystemWrapper()
        
        self.__id = unicode(id)
        self.__path = unicode(path)
        self.__name = self.__path.strip("/").split("/")[-1]
        self.__config_path = unicode(config_path)
        self.__config_file_name = unicode(config_file_name)
        self.__parent_path = self.__path.strip("/")[:len(self.__path)-len(self.__name)-1]
        self.__tag_file = self.__config_path + "/" + self.__name + "." + self.__config_file_name
        self.__watcher = QtCore.QFileSystemWatcher(self)
        if self.__file_system.path_exists(self.__parent_path):
            self.__watcher.addPath(self.__parent_path)
        if self.__file_system.path_exists(self.__path):
            self.__watcher.addPath(self.__path)
        #TODO: error handling
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("directoryChanged(QString)"), self.__directory_changed)
        
    def __directory_changed(self, path):
        """
        handles directory changes of the stores directory and its parent directory and finds out if the store itself was renamed/removed
        """
        if path == self.__parent_path:
            if not self.__file_system.path_exists(self.__path):
                ## store itself was changed: renamed, moved or deleted
                self.__watcher.removePath(self.__parent_path)
                renamed_path = self.__file_system.find(self.__parent_path, self.__tag_file)
                if renamed_path == "":    ## removed
                    self.emit(QtCore.SIGNAL("removed(PyQt_PyObject)"), self)
                else:                     ## renamed
                    self.emit(QtCore.SIGNAL("renamed(PyQt_PyObject, QString)"), self, renamed_path)
        else:
            ## files or directories in the store directory have been changed
            self.__handle_file_changes(path)
            
    def __handle_file_changes(self, path):
        """
        handles the stores file changes to find out if a file was added, renamed, removed
        """
        ## this method does not handle the renaming or deletion of the store directory itself (only childs)
        print "new file: " + path
        #TODO: search for changes and emit events
                
    def get_name(self):
        """
        returns the stores name
        """
        return self.__name
    
    def get_id(self):
        """
        returns the stores id
        """
        return unicode(self.__id)
    
    def rename(self, new_path):
        """
        renames the store and related files
        """
        self.__path = unicode(new_path)
        old_tag_file = self.__path + "/" + self.__tag_file 
        self.__name = self.__path.strip("/").split("/")[-1]
        self.__tag_file = self.__config_path + "/" + self.__name + "." + self.__config_file_name
        self.__file_system.rename(old_tag_file, self.__path + "/" + self.__tag_file)
         
    def dispose(self):
        """
        removes current file system paths from the file system watcher
        """
        self.__watcher.removePaths([self.__parent_path, self.__path])
        
    def add_tags(self, file, tagList):
        """
        """
        pass
        
    def rename_tag(self, currentName, new_name):
        """
        """
        pass
        
    def delete_tags(self, tagList):
        """
        """
        pass
        
    def get_dictionary(self):
        """
        """
        pass
        

## end
