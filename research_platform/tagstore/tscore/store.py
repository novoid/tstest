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

import logging.handlers
from sets import Set
from PyQt4 import QtCore
from tsos.filesystem import FileSystemWrapper
from tscore.tagwrapper import TagWrapper
from tscore.enum import EFileType, EFileEvent
from tscore.pendingchanges import PendingChanges

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
        self.__storage_dir_name = None
        self.__navigation_dir_name = None
        self.__parent_path = None
        self.__name = None
        self.__config_path = None
        self.__watcher_path = None
        self.__navigation_path = None
        
        ## set directories names -> can not be changed at runtime
        for dir in storage_dir_list:
            if self.__file_system.path_exists(self.__path + "/" + dir):
                self.__storage_dir_name = unicode(dir)
        for dir in navigation_dir_list:
            if self.__file_system.path_exists(self.__path + "/" + dir):
                self.__navigation_dir_name = unicode(dir)
        
        self.__init_store()
        #test_ localization
        #print "storage: " + self.__storage_dir_name
        #print "navigation: " + self.__navigation_dir_name
        
        #storage = self.trUtf8("storage")
        #navigation = self.trUtf8("navigation")
        #print "storage= "+storage
        #print "navigation= "+navigation
        
        #for dir in storage_dir_list:
        #    print "storage: " + dir
        #for dir in navigation_dir_list:
        #    print "navigation: " + dir
        #test end

    def __init_store(self):
        """
        initializes the store paths, config reader, file system watcher without instantiation of a now object
        """
        self.__name = self.__path.split("/")[-1]
        self.__parent_path = self.__path[:len(self.__path)-len(self.__name)-1]
        self.__config_path = self.__path + "/" + self.__config_file
        self.__watcher_path = self.__path + "/" + self.__storage_dir_name
        self.__navigation_path = self.__path + "/" + self.__navigation_dir_name
        #TODO: create store directories if directory currently not exists (new store)
        #TODO: create config directories and file if file currently not exists (new store)
        #if not self.__file_system.path_exists(self.__config_path):
        self.__tag_wrapper = TagWrapper(self.__config_path)
        
        self.__watcher.addPath(self.__parent_path)
        self.__watcher.addPath(self.__watcher_path)
        #TODO: error handling?
        
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
        #test
        #print "added: " + ", ".join(list(captured_added_files))
        #print "removed: " + ", ".join(list(captured_removed_files))
        # test end
        data_files = (config_files | captured_added_files) - captured_removed_files 
        added = list((existing_files | existing_dirs) - data_files)
        removed = list(data_files - (existing_files | existing_dirs))
    
        type = EFileType.FILE   
        #if self.__file_system.is_directory():
        #    type = EFileType.
        ##TODO replace this with either DIR or FILE depending on the items type
        if len(added) == 1 and len(removed) == 1:
            self.__pending_changes.register(removed[0], type, EFileEvent.REMOVED_OR_RENAMED)
            self.__pending_changes.register(added[0], type, EFileEvent.ADDED_OR_RENAMED)
            self.emit(QtCore.SIGNAL("file_renamed(PyQt_PyObject, QString, QString)"), self, removed[0], added[0])
        else:
            if len(removed) > 0:
                if len(added) == 0:
                    for item in removed:
                        self.__pending_changes.register(item, type, EFileEvent.REMOVED)
                        self.emit(QtCore.SIGNAL("file_removed(PyQt_PyObject, QString)"), self, item)
                else:
                    for item in removed:
                        self.__pending_changes.register(item, type, EFileEvent.REMOVED_OR_RENAMED)
                        self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
            if len(added) > 0:
                if len(removed) == 0:
                    for item in added:
                        self.__pending_changes.register(item, type, EFileEvent.ADDED)
                        self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
                else:
                    for item in added:
                        self.__pending_changes.register(item, type, EFileEvent.ADDED_OR_RENAMED)
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
    
    def __map_tags_to_filsystem(self, item_name, tag_list):
        """
        does all necessary steps vor creating new directories and links according to the tag list
        """
        self.__log.debug("store: %s -> create file system structure for item: %s" % (self.__name, item_name))
        
        item_name = unicode(item_name)
        
        ## create the real directories at first
        for tag in tag_list:
            tag_path = self.__navigation_path + "/" + tag
            if not self.__file_system.path_exists(tag_path):
                self.__file_system.create_dir(tag_path)
            else:
                self.__log.debug("path: --- %s --- already exists. do nothing" % tag_path)
            
            ## TODO: check out how to find working relatice paths for links 
            ##  put the link to the item into the dir
            src_path = self.__watcher_path + "/" + item_name
            link_name = tag_path + "/" + item_name
            self.__file_system.create_link(src_path, link_name)
        ## create the links between the dirs
        for tag in tag_list:
            tag_path = self.__path + "/" + self.__navigation_dir_name  + "/" + tag
            link_list = set(tag_list)
            link_list.remove(tag)
            for link in link_list:
                ## e.g. tag is "foo" and link is "bar"
                ## target: path_to_store/foo/bar
                ## source: path_to_store/bar
                target = tag_path + "/" + link
                if not self.__file_system.path_exists(target):
                    source = self.__path + "/" + link
                    self.__file_system.create_link(source, target)
                else:
                    self.__log.debug("path: --- %s --- already exists. do nothing" % target)
        #TODO: Change paths?
    
    def add_item_with_tags(self, file_name, tag_list):
        """
        adds tags to the given file, resets existing tags
        """
        #TODO: test this functionality due to path changes
        #self.__map_tags_to_filsystem(file_name, tag_list)
        self.__tag_wrapper.set_file(file_name, tag_list)
        self.__pending_changes.remove(file_name)
        
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
