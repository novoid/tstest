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

#import time #for performance tests only
import datetime
import re
import logging.handlers
from PyQt4 import QtCore
from tsos.filesystem import FileSystemWrapper
from tscore.tagwrapper import TagWrapper
from tscore.enums import EFileType, EFileEvent, EOS, EConflictType,\
    ECategorySetting
from tscore.pendingchanges import PendingChanges
from tscore.exceptions import StoreInitError, StoreTaggingError,\
    NameInConflictException, InodeShortageException
from tscore.configwrapper import ConfigWrapper
from tscore.vocabularywrapper import VocabularyWrapper
from tscore.tsconstants import TsConstants
from tscore.loghelper import LogHelper
#from tscore.tsconstants import TsConstants

class Store(QtCore.QObject):

    __pyqtSignals__ = ("removed(PyQt_PyObject)",
                       "renamed(PyQt_PyObject, QString)",
                       "file_renamed(PyQt_PyObject, QString, QString)",
                       "file_removed(PyQt_PyObject, QString)",
                       "pending_operations_changed(PyQt_PyObject)")

    def __init__(self, id, path, config_file_name, tags_file, vocabulary_file, storage_dir_list, describing_nav_dir_list, categorising_nav_dir_list, expiry_dir_list, expiry_prefix):
        """
        constructor
        """
        QtCore.QObject.__init__(self)

        self.__log = None

        self.__file_system = FileSystemWrapper(self.__log)
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("directoryChanged(QString)"), self.__directory_changed)
        self.__tag_wrapper = None
        self.__store_config_wrapper = None
        self.__pending_changes = PendingChanges()
        
        self.__id = unicode(id)
        self.__path = unicode(path)
        self.__config_file_name = unicode(config_file_name)
        self.__tags_file_name = unicode(tags_file)
        self.__vocabulary_file_name = unicode(vocabulary_file)
        
        self.__storage_dir_list = storage_dir_list
        self.__describing_nav_dir_list = describing_nav_dir_list
        self.__categorising_nav_dir_list = categorising_nav_dir_list
        self.__expiry_dir_list = expiry_dir_list
        self.__expiry_prefix = unicode(expiry_prefix)
        
        self.__storage_dir_name = self.trUtf8("storage")
        self.__describing_nav_dir_name = self.trUtf8("descriptions")
        self.__categorising_nav_dir_name = self.trUtf8("categories")
        self.__expiry_dir_name = self.trUtf8("expired_items")
        #self.__parent_path = None
        #self.__name = None
        #self.__config_path = None
        #self.__watcher_path = None
        #self.__describing_nav_path = None
        #self.__config_path = self.__path + "/" + self.__config_file_name
        #self.__watcher_path = self.__path + "/" + self.__storage_dir_name
        #self.__describing_nav_path = self.__path + "/" + self.__describing_nav_dir_name
        if self.__file_system.path_exists(self.__path + "/" + self.__tags_file_name):
            self.__tag_wrapper = TagWrapper(self.__path + "/" + self.__tags_file_name)
        if self.__file_system.path_exists(self.__path + "/" + self.__config_file_name):
            self.__store_config_wrapper = ConfigWrapper(self.__path + "/" + self.__config_file_name)

        if self.__path.find(":/") == -1:
            self.__path = self.__path.replace(":", ":/")
        self.__name = unicode(self.__path.split("/")[-1])
        self.__parent_path = unicode(self.__path[:len(self.__path)-len(self.__name)-1])

    def init(self):
        """
        init is called after event listeners were added to the store instance
        """
        ## throw exception if store directory does not exist
        if not self.__file_system.path_exists(self.__path):
            ## look for renamed or removed store folder
            self.__handle_renamed_removed_store()
        if not self.__file_system.path_exists(self.__path):
            #print self.__path
            raise StoreInitError, self.trUtf8("The specified store directory does not exist!")
            return
        
        ## look for store/describing_nav/categorising_nav/expire directories names (all languages) if they do not exist
        if not self.__file_system.path_exists(self.__path + "/" + self.__storage_dir_name):
            for dir in self.__storage_dir_list:
                if self.__file_system.path_exists(self.__path + "/" + dir):
                    self.__storage_dir_name = unicode(dir)
        if not self.__file_system.path_exists(self.__path + "/" + self.__describing_nav_dir_name):
            for dir in self.__describing_nav_dir_list:
                if self.__file_system.path_exists(self.__path + "/" + dir):
                    self.__describing_nav_dir_name = unicode(dir)
        if not self.__file_system.path_exists(self.__path + "/" + self.__categorising_nav_dir_name):
            for dir in self.__categorising_nav_dir_list:
                if self.__file_system.path_exists(self.__path + "/" + dir):
                    self.__categorising_nav_dir_name = unicode(dir)
        if not self.__file_system.path_exists(self.__path + "/" + self.__expiry_dir_name):
            for dir in self.__expiry_dir_list:
                if self.__file_system.path_exists(self.__path + "/" + dir):
                    self.__expiry_dir_name = unicode(dir)
        
        ## built stores directories and config file if they currently not exist (new store)
        self.__file_system.create_dir(self.__path + "/" + self.__storage_dir_name)
        self.__file_system.create_dir(self.__path + "/" + self.__describing_nav_dir_name)
        self.__file_system.create_dir(self.__path + "/" + self.__categorising_nav_dir_name)
        self.__file_system.create_dir(self.__path + "/" + self.__expiry_dir_name)
        self.__file_system.create_dir(self.__path + "/" + self.__config_file_name.split("/")[0])
        ## create config/vocabulary files if they don't exist
        if not self.__file_system.path_exists(self.__path + "/" + self.__config_file_name):
            ConfigWrapper.create_store_config_file(self.__path + "/" + self.__config_file_name)
        if not self.__file_system.path_exists(self.__path + "/" + self.__tags_file_name):
            TagWrapper.create_tags_file(self.__path + "/" + self.__tags_file_name)
        if not self.__file_system.path_exists(self.__path + "/" + self.__vocabulary_file_name):
            self.__vocabulary_wrapper = VocabularyWrapper.create_vocabulary_file(self.__path + "/" + self.__vocabulary_file_name)

        self.__init_store()

        
    def __init_store(self):
        """
        initializes the store paths, config reader, file system watcher without instantiation of a new object
        """
        self.__name = self.__path.split("/")[-1]
        self.__parent_path = self.__path[:len(self.__path)-len(self.__name)-1]
        self.__tags_file_path = self.__path + "/" + self.__tags_file_name
        self.__config_path = self.__path + "/" + self.__config_file_name
        self.__vocabulary_path = self.__path + "/" + self.__vocabulary_file_name #TsConstants.STORE_CONFIG_DIR + "/" + TsConstants.STORE_VOCABULARY_FILENAME
        self.__watcher_path = self.__path + "/" + self.__storage_dir_name
        self.__describing_nav_path = self.__path + "/" + self.__describing_nav_dir_name
        self.__categorising_nav_path = self.__path + "/" + self.__categorising_nav_dir_name
        config_file_name = unicode(self.__config_path.split("/")[-1])
        self.__temp_progress_path = unicode(self.__config_path[:len(self.__config_path)-len(config_file_name)-1])
        
        self.__tag_wrapper = TagWrapper(self.__tags_file_path)
        ## update store id to avoid inconsistency
        config_wrapper = ConfigWrapper(self.__path + "/" + self.__config_file_name)#self.__tags_file_name)
        config_wrapper.set_store_id(self.__id)
        self.__vocabulary_wrapper = VocabularyWrapper(self.__vocabulary_path)
        self.connect(self.__vocabulary_wrapper, QtCore.SIGNAL("changed"), self.__handle_vocabulary_changed)
        self.__store_config_wrapper = ConfigWrapper(self.__config_path)
        self.connect(self.__store_config_wrapper, QtCore.SIGNAL("changed()"), self.__handle_store_config_changed)
        
        self.__watcher.addPath(self.__parent_path)
        self.__watcher.addPath(self.__watcher_path)
        ## handle offline changes
        self.__handle_unfinished_operation()
        self.__handle_file_expiry()
        self.__handle_file_changes(self.__watcher_path)
        
        ## all necessary files and dirs should have been created now - so init the logger
        self.__log = LogHelper.get_store_logger(self.__path, logging.INFO) 
    
    def __handle_store_config_changed(self):
        self.emit(QtCore.SIGNAL("store_config_changed"), self)

    def __handle_vocabulary_changed(self):
        self.emit(QtCore.SIGNAL("vocabulary_changed"), self)
        
#    def handle_offline_changes(self):
#        """
#        called after store and event-handler are created to handle (offline) modifications
#        """
#        self.__handle_file_changes(self.__watcher_path)

    def set_path(self, path, config_file=None, tags_file=None, vocabulary_file=None):
        """
        resets the stores path and config path (called if application config changes)
        """
        if self.__path == unicode(path) and (config_file is None or self.__config_file_name == unicode(config_file)):
            exit
        ## update changes
        self.__watcher.removePaths([self.__parent_path, self.__watcher_path])
        self.__path = unicode(path)
        if config_file is not None:
            self.__config_file_name = unicode(config_file)
        if tags_file is not None:
            self.__tags_file_name = unicode(tags_file)
        if vocabulary_file is not None:
            self.__vocabulary_file_name = unicode(vocabulary_file)
        self.__init_store()
        
    def __handle_renamed_removed_store(self):
        """
        searches the parents directory for renamed or removed stores
        """
        #print self.__parent_path
        #print self.__config_file_name
        #print ".."
        config_paths = self.__file_system.find_files(self.__parent_path, self.__config_file_name)
        #print "config paths: " + ",".join(config_paths)
        new_name = ""
        for path in config_paths:
            reader = ConfigWrapper(path)
            #print "found: " + path
            #print self.__id + ", " + reader.get_store_id()
            
            if self.__id == reader.get_store_id():
                new_name = path.split("/")[-3]
                #print "new name: " + new_name

        if new_name == "":      ## removed
            ## delete describing_nav directors
            #self.remove()
            self.emit(QtCore.SIGNAL("removed(PyQt_PyObject)"), self)
        else:                   ## renamed
            self.__path = self.__parent_path + "/" + new_name
            self.__describing_nav_path = self.__path + "/" + self.__describing_nav_dir_name
            self.__categorising_nav_path = self.__path + "/" + self.__categorising_nav_dir_name
            ## update all links in windows: absolute links only
            #if self.__file_system.get_os() == EOS.Windows:
                #print "rebuild"
                #self.rebuild()
            #print "emit: " + self.__parent_path + "/" + new_name
            self.emit(QtCore.SIGNAL("renamed(PyQt_PyObject, QString)"), self, self.__parent_path + "/" + new_name)
    
    def __directory_changed(self, path):
        """
        handles directory changes of the stores directory and its parent directory 
        and finds out if the store itself was renamed/removed
        """
        if path == self.__parent_path:
            if not self.__file_system.path_exists(self.__path):
                ## store itself was changed: renamed, moved or deleted
                self.__watcher.removePath(self.__parent_path)
                self.__handle_renamed_removed_store()
        else:
            ## files or directories in the store directory have been changed
            self.__handle_file_changes(self.__watcher_path)

    def __handle_unfinished_operation(self):
        """
        looks for a opInProgress.tmp file to find out if the last operation was finished correctly
        this file is created before an operation starts and deleted afterwards
        """
        if self.__file_system.path_exists(self.__temp_progress_path + "/" + "opInProgress.tmp"):
            self.rebuild()
            
    def __create_inprogress_file(self):
        """
        creates a temporary file during an operation in progress to handle operation interruption
        """
        self.__file_system.create_file(self.__temp_progress_path + "/" + "opInProgress.tmp")

    def __remove_inprogress_file(self):
        """
        removes the temporary file after the operation succeeded
        """
        self.__file_system.remove_file(self.__temp_progress_path + "/" + "opInProgress.tmp")
        
    def __handle_file_expiry(self):
        """
        looks for expired items and moves & renames them to filename including tags in the expiry_directory
        """
        expiry_date_files = self.__tag_wrapper.get_files_with_expiry_tags(self.__expiry_prefix)
        now = datetime.datetime.now()
        for file in expiry_date_files:
            file_extension = "." + file["filename"].split(".")[-1]
            file_name = file["filename"][:len(file_extension)]
            if int(file["exp_year"]) < now.year or (int(file["exp_year"]) == now.year and int(file["exp_month"]) < now.month):
                new_filename = file_name + " - " + "; ".join(file["category"]) + " - " + "; ".join(file["tags"]) + file_extension
                self.__file_system.rename_file(self.__watcher_path + "/" + file["filename"], self.__path + "/" + self.__expiry_dir_name + "/" + new_filename)
                      
    def __handle_file_changes(self, path):
        """
        handles the stores file and dir changes to find out if a file/directory was added, renamed, removed
        """
        
        ## this method does not handle the renaming or deletion of the store directory itself (only childs)
        existing_files = set(self.__file_system.get_files(path))
        existing_dirs = set(self.__file_system.get_directories(path))
        config_files = set(self.__tag_wrapper.get_files())
        captured_added_files = set(self.__pending_changes.get_added_names())
        captured_removed_files = set(self.__pending_changes.get_removed_names())

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
        if self.__file_system.is_directory(self.__watcher_path + "/" + unicode(item)):
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
    
    def remove(self):
        """
        removes all directories and links in the stores describing_nav path
        """
        self.__file_system.delete_dir_content(self.__describing_nav_path)
        self.__file_system.delete_dir_content(self.__categorising_nav_path)
    
    def rebuild(self):
        """
        removes and rebuilds all links in the describing_nav path
        """
        self.__log.info("START rebuild progress")
        self.__create_inprogress_file()
        self.remove()
        for file in self.__tag_wrapper.get_files():
            describing_tag_list = self.__tag_wrapper.get_file_tags(file)
            categorising_tag_list = self.__tag_wrapper.get_file_categories(file)
            self.add_item_with_tags(file, describing_tag_list, categorising_tag_list)
        self.__remove_inprogress_file()
        self.__log.info("rebuild progress END")
    
    def rename_file(self, old_file_name, new_file_name):
        """
        renames an existing file: links and config settings 
        """
        self.__log.info("renaming: %s to %s" % (old_file_name, new_file_name))
        self.__create_inprogress_file()
        if self.__tag_wrapper.file_exists(old_file_name):
            describing_tag_list = self.__tag_wrapper.get_file_tags(old_file_name)
            categorising_tag_list = self.__tag_wrapper.get_file_categories(old_file_name)
            self.remove_file(old_file_name)
            self.add_item_with_tags(new_file_name, describing_tag_list, categorising_tag_list)

            self.__pending_changes.remove(old_file_name)
            self.__pending_changes.remove(new_file_name)
        else:
            self.__pending_changes.edit(old_file_name, new_file_name)
            self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
        self.__remove_inprogress_file()
        
    def remove_file(self, file_name):
        """
        removes a file: links and config settings 
        """
        self.__log.info("remove file: %s" % file_name)
        self.__create_inprogress_file()
        self.__pending_changes.remove(file_name)
        if self.__tag_wrapper.file_exists(file_name):
            describing_tag_list = self.__tag_wrapper.get_file_tags(file_name)
            categorising_tag_list = self.__tag_wrapper.get_file_categories(file_name)
            self.__delete_links(file_name, describing_tag_list, self.__describing_nav_path)
            self.__delete_links(file_name, categorising_tag_list, self.__categorising_nav_path)
            self.__tag_wrapper.remove_file(file_name)
        else:
            self.emit(QtCore.SIGNAL("pending_operations_changed(PyQt_PyObject)"), self)
        self.__remove_inprogress_file()
        
    def __delete_links(self, file_name, tag_list, current_path):
        """
        deletes all links to the given file
        """
        self.__create_inprogress_file()
        for tag in tag_list:
            recursive_list = [] + tag_list
            recursive_list.remove(tag)
            self.__delete_links(file_name, recursive_list, current_path + "/" + tag)
            self.__file_system.remove_link(current_path + "/" + tag + "/" + file_name)
        self.__remove_inprogress_file()
    
    def change_expiry_prefix(self, new_prefix):
        """
        changes the expiry prefix and all existing expiry tags
        """
        ## handle changes only
        if self.__expiry_prefix == new_prefix:  
            return

        self.__log.info("changing the expiry prefix from %s to %s" % (self.__expiry_prefix, new_prefix))
        
        self.__create_inprogress_file()
        expiry_date_files = self.__tag_wrapper.get_files_with_expiry_tags(self.__expiry_prefix)
        
        for file in expiry_date_files:
            for tag in file["tags"]:
                match = re.match("^(" + self.__expiry_prefix + ")([0-9]{4})(-)([0-9]{2})", tag)
                if match:
                    self.rename_tag(tag, tag.replace(self.__expiry_prefix, new_prefix))
            
            for tag in file["category"]:
                match = re.match("^(" + self.__expiry_prefix + ")([0-9]{4})(-)([0-9]{2})", tag)
                if match:
                    self.rename_tag(tag, tag.replace(self.__expiry_prefix, new_prefix))

        ## set new prefix
        self.__expiry_prefix = new_prefix
        self.__remove_inprogress_file()
    
    def get_tags(self):
        """
        returns a list of all describing  tags
        """
        return self.__tag_wrapper.get_all_tags()

    def get_categorizing_tags(self):
        """
        returns a list of categorizing all tags
        """
        return self.__tag_wrapper.get_all_categorizing_tags()

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

    def get_popular_categories(self, number):
        """
        returns a given number of the most popular tags
        """
        return self.__tag_wrapper.get_popular_categories(number)

    def get_recent_categories(self, number):
        """
        returns a given number of recently entered tags
        """
        return self.__tag_wrapper.get_recent_categories(number)

    def get_show_category_line(self):
        return self.__store_config_wrapper.get_show_category_line()

    def is_controlled_vocabulary(self):
        """
        return True if there is just controlled vocabulary allowed in the second tagline 
        """
        setting = self.__store_config_wrapper.get_show_category_line()
        if setting == ECategorySetting.ENABLED_ONLY_PERSONAL or setting == ECategorySetting.ENABLED_SINGLE_CONTROLLED_TAGLINE:
            return True
        return False
    
    def get_datestamp_format(self):
        return self.__store_config_wrapper.get_datestamp_format()
    
    def get_category_mandatory(self):
        return self.__store_config_wrapper.get_category_mandatory()

    def __name_in_conflict(self, file_name, describing_tag_list, categorising_tag_list):
        """
        checks for conflicts and returns the result as boolean
        """
        #TODO: extend functionality: have a look at #18 (Wiki)
        existing_files = self.__tag_wrapper.get_files()
        existing_tags = self.__tag_wrapper.get_all_tags()
        tag_list = list(set(describing_tag_list) | set(categorising_tag_list))
        
        if file_name in existing_tags:
            return [file_name, EConflictType.FILE]
        for tag in tag_list:
            if tag in existing_files:
                return [tag, EConflictType.TAG]
        return ["", None]
         
    def add_item_with_tags(self, file_name, describing_tag_list, categorising_tag_list=None):
        """
        adds tags to the given file, resets existing tags
        """
        #TODO: if file_name already in config, delete missing tags and recreate whole link structure
        #existing tags will not be recreated in windows-> linux, osx???
        
        
        self.__log.info("add item with tags to navigation: itemname: %s" % file_name)
        self.__log.info("describing tags: %s" % describing_tag_list)
        self.__log.info("categorizing tags: %s" % categorising_tag_list)
        ## throw error if inodes run short
        if self.__file_system.inode_shortage(self.__config_path):
            self.__log.error("inode threshold has exceeded")
            raise InodeShortageException(TsConstants.INODE_THRESHOLD)
        ## throw error if item-names and tag-names (new and existing) are in conflict
        conflict = self.__name_in_conflict(file_name, describing_tag_list, categorising_tag_list)
        if conflict[0] != "":
            self.__log.error("name_in_conflict_error: %s, %s" % (conflict[0], conflict[1]))
            raise NameInConflictException(conflict[0], conflict[1])
        ## ignore multiple tags
        describing_tags = list(set(describing_tag_list))
        categorising_tags = []
        if categorising_tag_list is not None:
            categorising_tags = list(set(categorising_tag_list))

        ## scalability test
        ## start = time.clock()
        #try:
        self.__create_inprogress_file()
        self.__build_store_navigation(file_name, describing_tags, self.__describing_nav_path)
        self.__build_store_navigation(file_name, categorising_tags, self.__categorising_nav_path)
        #except:
        #    raise Exception, self.trUtf8("An error occurred during building the navigation path(s) and links!")
        #try:
        self.__tag_wrapper.set_file(file_name, describing_tags, categorising_tags)
        self.__pending_changes.remove(file_name)
        self.__remove_inprogress_file()
        #except:
        #    raise Exception, self.trUtf8("An error occurred during saving file and tags to configuration file!")
        ## scalability test
        ## print "number of tags: " + str(len(tags)) + ", time: " + str(time.clock()-start)
        
    def __build_store_navigation(self, link_name, tag_list, current_path):
        """
        builds the whole directory and link-structure (describing & categorising nav path) inside a stores filesystem
        """
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
        self.__create_inprogress_file()
        ##get all affected files per tag
        files = self.__tag_wrapper.get_files_with_tag(old_tag_name)
        self.delete_tags([old_tag_name])
        for file in files:
            if old_tag_name in file["tags"]:
                file["tags"].append(new_tag_name)
                file["tags"].remove(old_tag_name)
            if old_tag_name in file["category"]:
                file["category"].append(new_tag_name)
                file["category"].remove(old_tag_name)
            self.add_item_with_tags(file["filename"], file["tags"], file["category"]) 
        self.__remove_inprogress_file()
        
    def delete_tags(self, tag_list):
        """
        delete tags inside the store
        """
        self.__create_inprogress_file()
        for tag_name in tag_list:
            ##get all affected files per tag
            files = self.__tag_wrapper.get_files_with_tag(tag_name)
            for file in files:
                self.__delete_tag_folders(tag_name, file["tags"], self.__describing_nav_path)
                self.__delete_tag_folders(tag_name, file["category"], self.__categorising_nav_path)
            ##remove tag from config file
            self.__tag_wrapper.remove_tag(tag_name)
        self.__remove_inprogress_file()
        
    def __delete_tag_folders(self, affected_tag, tag_list, current_path):
        """
        recursive function to delete the tag directories within the describing_nav structure
        """
        if affected_tag not in tag_list:
            return
        self.__file_system.delete_dir(current_path + "/" + affected_tag)
        diff_list = [] + tag_list
        diff_list.remove(affected_tag)
        for tag in diff_list:
            recursive_list = [] + tag_list
            recursive_list.remove(tag)
            self.__delete_tag_folders(affected_tag, recursive_list, current_path + "/" + tag)
        
    def get_controlled_vocabulary(self):
        """
        returns a predefined list of allowed strings (controlled vocabulary) to be used for categorizing
        """
        return self.__vocabulary_wrapper.get_vocabulary()
    
    def set_controlled_vocabulary(self, vocabulary_set):
        self.__vocabulary_wrapper.set_vocabulary(vocabulary_set)
        

## end
