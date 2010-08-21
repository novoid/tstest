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
from tscore.tagwrapper import TagWrapper

class Store(QtCore.QObject):

    __pyqtSignals__ = ("removed(PyQt_PyObject)",
                       "renamed(PyQt_PyObject, QString)",
                       "file_added(PyQt_PyObject, QString)",
                       "file_renamed(PyQt_PyObject, QString)",
                       "file_removed(PyQt_PyObject, QString)")

    def __init__(self, id, path, config_file):
        """
        constructor
        """
    
        QtCore.QObject.__init__(self)

        self.__file_system = FileSystemWrapper()
        
        self.__id = unicode(id)
        self.__path = unicode(path)
        self.__name = self.__path.split("/")[-1]
        self.__config_file = unicode(config_file)
        self.__config_path = self.__path + "/" + self.__config_file
        #TODO: create store directories if directory currently not exists (new store)
        #TODO: create config directories and file if file currently not exists (new store)
        #if not self.__file_system.path_exists(self.__config_path):
        self.__config_wrapper = TagWrapper(self.__config_path)
        
        self.__parent_path = self.__path[:len(self.__path)-len(self.__name)-1]
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.addPath(self.__parent_path)
        self.__watcher.addPath(self.__path)
        #TODO: error handling?
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("directoryChanged(QString)"), self.__directory_changed)
        
    def __directory_changed(self, path):
        """
        handles directory changes of the stores directory and its parent directory and finds out if the store itself was renamed/removed
        """
        if path == self.__parent_path:
            if not self.__file_system.path_exists(self.__path):
                ## store itself was changed: renamed, moved or deleted
                self.__watcher.removePath(self.__parent_path)
                config_paths = self.__file_system.find_files(self.__parent_path, self.__config_file)
                new_name = ""
                for path in config_paths:
                    reader = TagWrapper(path)
                    if self.__id == reader.get_store_id():
                        new_name = path.split("/")[-3]

                if new_name == "":      ## removed
                    self.emit(QtCore.SIGNAL("removed(PyQt_PyObject)"), self)
                else:                   ## renamed
                    self.emit(QtCore.SIGNAL("renamed(PyQt_PyObject, QString)"), self, self.__parent_path + "/" + new_name)
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
    
    def stop_filesystem_monitoring(self):
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
