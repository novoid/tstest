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
from sets import Set
from tsos.filesystem import FileSystemWrapper
from tscore.tagwrapper import TagWrapper
from tscore.enum import EFileEvent
from tscore.pendingchanges import PendingChanges

class Store(QtCore.QObject):

    __pyqtSignals__ = ("removed(PyQt_PyObject)",
                       "renamed(PyQt_PyObject, QString)",
                       "file_renamed(PyQt_PyObject, QString, QString)",
                       "file_removed(PyQt_PyObject, QString)",
                       "pending_operations_changed(PyQt_PyObject)")

    def __init__(self, id, path, config_file):
        """
        constructor
        """
        QtCore.QObject.__init__(self)

        self.__file_system = FileSystemWrapper()
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("directoryChanged(QString)"), self.__directory_changed)
        self.__config_wrapper = None
        self.__pending_changes = PendingChanges()
        
        self.__id = unicode(id)
        self.__path = unicode(path)
        self.__config_file = unicode(config_file)
        self.__parent_path = None
        self.__name = None
        self.__config_path = None
        self.__init_store()

    def __init_store(self):
        """
        initializes the store paths, config reader, file system watcher without instantiation of a now object
        """
        self.__name = self.__path.split("/")[-1]
        self.__parent_path = self.__path[:len(self.__path)-len(self.__name)-1]
        self.__config_path = self.__path + "/" + self.__config_file
        #TODO: create store directories if directory currently not exists (new store)
        #TODO: create config directories and file if file currently not exists (new store)
        #if not self.__file_system.path_exists(self.__config_path):
        self.__config_wrapper = TagWrapper(self.__config_path)
        
        self.__watcher.addPath(self.__parent_path)
        self.__watcher.addPath(self.__path)
        #TODO: error handling?
        
    def handle_offline_changes(self):
        """
        called after store and event-handler are created to handle (offline) modifications
        """
        self.__handle_file_changes(self.__path)

    def set_path(self, path, config_file=None):
        """
        resets the stores path and config path (called if application config changes)
        """
        if self.__path == unicode(path) and (config_file is None or self.__config_file == unicode(config_file)):
            exit
        ## update changes
        self.__watcher.removePaths([self.__parent_path, self.__path])
        self.__path = unicode(path)
        if config_file is not None:
            self.__config_file = unicode(config_file)
        self.__init_store()
        
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
                    #print "removed: " + self.__id
                    self.emit(QtCore.SIGNAL("removed(PyQt_PyObject)"), self)
                else:                   ## renamed
                    #print "renamed: " + self.__id
                    self.emit(QtCore.SIGNAL("renamed(PyQt_PyObject, QString)"), self, self.__parent_path + "/" + new_name)
        else:
            ## files or directories in the store directory have been changed
            self.__handle_file_changes(path)
            
    def __handle_file_changes(self, path):
        """
        handles the stores file changes to find out if a file was added, renamed, removed
        """
        ## this method does not handle the renaming or deletion of the store directory itself (only childs)
        existing_files = Set(self.__file_system.get_files(path))
        config_files = Set(self.__config_wrapper.get_files())
        captured_added_files = Set(self.__pending_changes.get_added_files())
        captured_removed_files = Set(self.__pending_changes.get_removed_files())
        
        data_files = (config_files | captured_added_files) - captured_removed_files 
        added_files = list(existing_files - data_files)
        removed_files = list(data_files - existing_files)
    
        if len(added_files) == 1 and len(removed_files) == 1:
            self.__pending_changes.add_file(removed_files[0], EFileEvent.REMOVED_OR_RENAMED)
            self.__pending_changes.add_file(added_files[0], EFileEvent.ADDED_OR_RENAMED)
            self.emit(QtCore.SIGNAL("file_renamed(PyQt_PyObject, QString, QString)"), self, removed_files[0], added_files[0])
        else:
            if len(removed_files) > 0:
                if len(added_files) == 0:
                    for file in removed_files:
                        self.__pending_changes.add_file(file, EFileEvent.REMOVED)
                        self.emit(QtCore.SIGNAL("file_removed(PyQt_PyObject, QString)"), self, file)
                else:
                    for file in removed_files:
                        self.__pending_changes.add_file(file, EFileEvent.REMOVED_OR_RENAMED)
                        self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
            if len(added_files) > 0:
                if len(removed_files) == 0:
                    for file in added_files:
                        self.__pending_changes.add_file(file, EFileEvent.ADDED)
                        self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
                else:
                    for file in added_files:
                        self.__pending_changes.add_file(file, EFileEvent.ADDED_OR_RENAMED)
                        self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)                
        
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
    
    def get_pending_changes(self):
        """
        returns the stores unhandled changes 
        """
        return self.__pending_changes
    
    def rename_file(self, old_file_name, new_file_name):
        """
        renames an existing file: links and config settings 
        """
        #TODO: changing links: names and targets
        if self.__config_wrapper.file_exists(old_file_name):
            self.__config_wrapper.rename_file(old_file_name, new_file_name)
            self.__pending_changes.remove_file(old_file_name)
            self.__pending_changes.remove_file(new_file_name)
        else:
            self.__pending_changes.remove_file(old_file_name)
            self.__pending_changes.add_file(new_file_name, EFileEvent.ADDED)
            self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
        
    def remove_file(self, file_name):
        """
        removes a file: links and config settings 
        """
        #TODO: handle links: delete links and empty directories
        self.__pending_changes.remove_file(file_name)
        if self.__config_wrapper.file_exists(file_name):
            self.__config_wrapper.remove_file(file_name)
        else:
            self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
        
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
