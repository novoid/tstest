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

import time #for performance test only
import logging.handlers
from sets import Set
from PyQt4 import QtCore
from tsos.filesystem import FileSystemWrapper
from tscore.tagwrapper import TagWrapper
from tscore.enums import EFileType, EFileEvent
from tscore.pendingchanges import PendingChanges
from tscore.exceptions import StoreInitError, StoreTaggingError

class Store(QtCore.QObject):

    __pyqtSignals__ = ("removed(PyQt_PyObject)",
                       "renamed(PyQt_PyObject, QString)",
                       "file_renamed(PyQt_PyObject, QString, QString)",
                       "file_removed(PyQt_PyObject, QString)",
                       "pending_operations_changed(PyQt_PyObject)")

    def __init__(self, id, path, config_file, storage_dir_list, navigation_dir_list):
        """
        constructor
        """
        QtCore.QObject.__init__(self)

        self.__log = logging.getLogger("TagStoreLogger")

        self.__file_system = FileSystemWrapper()
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("directoryChanged(QString)"), self.__directory_changed)
        self.__tag_wrapper = None
        self.__pending_changes = PendingChanges()
        
        self.__id = unicode(id)
        self.__path = unicode(path)
        self.__config_file = unicode(config_file)
        self.__storage_dir_name = self.trUtf8("storage")
        self.__navigation_dir_name = self.trUtf8("navigation")
        self.__parent_path = None
        self.__name = None
        self.__config_path = None
        self.__watcher_path = None
        self.__navigation_path = None

        ## throw exception if store directory does not exist
        if self.__path.find(":/") == -1:
            self.__path = self.__path.replace(":", ":/")
        if not self.__file_system.path_exists(self.__path):
            raise StoreInitError, self.trUtf8("The specified store directory does not exist!")
        
        ## look for store/navigation directories names (all languages) if they do not exist
        if not self.__file_system.path_exists(self.__path + "/" + self.__storage_dir_name):
            for dir in storage_dir_list:
                if self.__file_system.path_exists(self.__path + "/" + dir):
                    self.__storage_dir_name = unicode(dir)
        if not self.__file_system.path_exists(self.__path + "/" + self.__navigation_dir_name):
            for dir in navigation_dir_list:
                if self.__file_system.path_exists(self.__path + "/" + dir):
                    self.__navigation_dir_name = unicode(dir)
        
        ## built stores directories and config file if they currently not exist (new store)
        self.__file_system.create_dir(self.__path + "/" + self.__storage_dir_name)
        self.__file_system.create_dir(self.__path + "/" + self.__navigation_dir_name)
        self.__file_system.create_dir(self.__path + "/" + self.__config_file.split("/")[0])
        if not self.__file_system.path_exists(self.__path + "/" + self.__config_file):
            self.__file_system.create_file(self.__path + "/" + self.__config_file)
        
        self.__init_store()
        
    def __init_store(self):
        """
        initializes the store paths, config reader, file system watcher without instantiation of a now object
        """
        self.__name = self.__path.split("/")[-1]
        self.__parent_path = self.__path[:len(self.__path)-len(self.__name)-1]
        self.__config_path = self.__path + "/" + self.__config_file
        self.__watcher_path = self.__path + "/" + self.__storage_dir_name
        self.__navigation_path = self.__path + "/" + self.__navigation_dir_name
        self.__tag_wrapper = TagWrapper(self.__config_path)
        
        self.__watcher.addPath(self.__parent_path)
        self.__watcher.addPath(self.__watcher_path)
        
    def handle_offline_changes(self):
        """
        called after store and event-handler are created to handle (offline) modifications
        """
        self.__handle_file_changes(self.__watcher_path)

    def set_path(self, path, config_file=None):
        """
        resets the stores path and config path (called if application config changes)
        """
        if self.__path == unicode(path) and (config_file is None or self.__config_file == unicode(config_file)):
            exit
        ## update changes
        self.__watcher.removePaths([self.__parent_path, self.__watcher_path])
        self.__path = unicode(path)
        if config_file is not None:
            self.__config_file = unicode(config_file)
        self.__init_store()
        
    def __directory_changed(self, path):
        """
        handles directory changes of the stores directory and its parent directory 
        and finds out if the store itself was renamed/removed
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
            self.__handle_file_changes(self.__watcher_path)
            
    def __handle_file_changes(self, path):
        """
        handles the stores file and dir changes to find out if a file/directory was added, renamed, removed
        """
         
        ## this method does not handle the renaming or deletion of the store directory itself (only childs)
        existing_files = Set(self.__file_system.get_files(path))
        existing_dirs = Set(self.__file_system.get_directories(path))
        config_files = Set(self.__tag_wrapper.get_files())
        captured_added_files = Set(self.__pending_changes.get_added_names())
        captured_removed_files = Set(self.__pending_changes.get_removed_names())

        data_files = (config_files | captured_added_files) - captured_removed_files 
        added = list((existing_files | existing_dirs) - data_files)
        removed = list(data_files - (existing_files | existing_dirs))
    
        if len(added) == 1 and len(removed) == 1:
            self.__pending_changes.register(removed[0], self.__get_type(removed[0]), EFileEvent.REMOVED_OR_RENAMED)
            self.__pending_changes.register(added[0], self.__get_type(added[0]), EFileEvent.ADDED_OR_RENAMED)
            self.emit(QtCore.SIGNAL("file_renamed(PyQt_PyObject, QString, QString)"), self, removed[0], added[0])
        else:
            if len(removed) > 0:
                if len(added) == 0:
                    for item in removed:
                        self.__pending_changes.register(item, self.__get_type(item), EFileEvent.REMOVED)
                        self.emit(QtCore.SIGNAL("file_removed(PyQt_PyObject, QString)"), self, item)
                else:
                    for item in removed:
                        self.__pending_changes.register(item, self.__get_type(item), EFileEvent.REMOVED_OR_RENAMED)
                        self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
            if len(added) > 0:
                if len(removed) == 0:
                    for item in added:
                        self.__pending_changes.register(item, self.__get_type(item), EFileEvent.ADDED)
                        self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
                else:
                    for item in added:
                        self.__pending_changes.register(item, self.__get_type(item), EFileEvent.ADDED_OR_RENAMED)
                        self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)                
        
    def __get_type(self, item):
        """
        returns the items type to be stored in pending_changes
        """
        if self.__file_system.is_directory(self.__watcher_path + "/" + item):
            return EFileType.DIRECTORY
        return EFileType.FILE
    
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
        if self.__tag_wrapper.file_exists(old_file_name):
            self.__tag_wrapper.rename_file(old_file_name, new_file_name)
            self.__pending_changes.remove(old_file_name)
            self.__pending_changes.remove(new_file_name)
        else:
            self.__pending_changes.edit(old_file_name, new_file_name)
            self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
        
    def remove_file(self, file_name):
        """
        removes a file: links and config settings 
        """
        #TODO: handle links: delete links and empty directories
        self.__pending_changes.remove(file_name)
        if self.__tag_wrapper.file_exists(file_name):
            self.__tag_wrapper.remove_file(file_name)
        else:
            self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
        
    def get_tags(self):
        """
        returns a list of all tags
        """
        return self.__tag_wrapper.get_all_tags()

    def get_recent_tags(self, number):
        """
        returns a given number of recently entered tags
        """
        return self.__tag_wrapper.get_recent_tags(number)
    
    def get_popular_tags(self, number):
        """
        returns a given number of the most popular tags
        """
        return self.__tag_wrapper.get_popular_tags(number)
        
    def add_item_with_tags(self, file_name, tag_list):
        """
        adds tags to the given file, resets existing tags
        """
        ## throw error if inodes run short
        if self.__file_system.inode_shortage(self.__config_path):
            raise Error, self.trUtf8("Number of inodes < 10%! Tagging has not been carried out!")
        ## ignore multiple tags
        tags = list(set(tag_list))
        print "taglist: " + ", ".join(tags)
        ## scalability test
        start = time.clock()

        self.__build_store_navigation(file_name, tags, self.__navigation_path)
        self.__tag_wrapper.set_file(file_name, tags)
        self.__pending_changes.remove(file_name)

        ## scalability test
        print "number of tags: " + str(len(tags)) + ", time: " + str(time.clock()-start)
        
    def __build_store_navigation(self, link_name, tag_list, current_path):
        """
        builds the whole directory and link-structure (navigation path) inside a stores filesystem
        """
        ## recursive break- condition
        if tag_list == []:
            return
        ## create directories and links + recursive calls
        link_source = self.__watcher_path + "/" + link_name
        for tag in tag_list:
            self.__file_system.create_dir(current_path + "/" + tag)
            self.__file_system.create_link(link_source, current_path + "/" + tag + "/" + link_name)
            recursive_list = [] + tag_list
            recursive_list.remove(tag)
            self.__build_store_navigation(link_name, recursive_list, current_path + "/" + tag)
    
    def rename_tag(self, old_tag_name, new_tag_name):
        """
        renames a tag inside the store 
        """
        #TODO: rename directories, update links
        self.__tag_wrapper.rename_tag(old_tag_name, new_tag_name)
        
    def delete_tags(self, tag_list):
        """
        delete tags inside the store
        """
        for tag_name in tag_list:
            self.__tag_wrapper.remove_tag(tag_name)
        #TODO: directory & link changes
        pass
        
    def get_vocabulary_list(self):
        """
        returns a predefined list of allowed strings to be used for categorizing
        """
        pass
        

## end
