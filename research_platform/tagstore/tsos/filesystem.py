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

import sys
import os
import logging.handlers
if sys.platform[:3] == "win":
    from windows import FileSystem
elif sys.platform == "darwin":
    from osx import FileSystem
else:
    from linux import FileSystem


class FileSystemWrapper():

    def __init__(self):
        """
        constructor
        """
        self.__log = logging.getLogger("TagStoreLogger")
        
        self.file_system = FileSystem()
        self.__IGNORED_FILE_PREFIXES = ["~$", "."]
        self.__IGNORED_DIR_PREFIXES = ["."]
        self.__IGNORED_EXTENTIONS = [".lnk"]
    
    def get_os(self):
        """
        returns the systems os as enumerable EOS
        """
        return self.file_system.get_type()
        
    def path_exists(self, path):
        """
        returns True if given path exists, else False
        """
        return os.path.exists(path)
    
    def find_files(self, in_path, search_path):
        """
        returns a list of files including the search_path from the directory in_path
        caution: this method does not provide a hierarchical search- use os.walk() for this purpose
        """
        files = []
        if not os.path.exists(in_path):
            return files
        for file in os.listdir(in_path):
            path = in_path + "/" + file + "/" + search_path
            if os.path.exists(path):
                files.append(path)
        return files

    def get_files(self, directory):
        """
        returns a list of files found in the given directory filtered by ignore- settings
        """
        files = []
        ignored = []
        for item in os.listdir(directory):
            if os.path.isfile(directory + "/" + item):
                files.append(item)
                ## handle ignore-list
                for ext in self.__IGNORED_EXTENTIONS:
                    if item.endswith(ext):
                        ignored.append(item)
                for prefix in self.__IGNORED_FILE_PREFIXES:
                    if item.startswith(prefix):
                        ignored.append(item)
        return list(set(files) - set(ignored))
    
    def get_directories(self, directory):
        """
        returns a list of dirs found in the given directory filtered by ignore- settings
        """
        files = []
        ignored = []
        for item in os.listdir(directory):
            if os.path.isdir(directory + "/" + item):
                files.append(item)
                ## handle ignore list
                for prefix in self.__IGNORED_DIR_PREFIXES:
                    if item.startswith(prefix):
                        ignored.append(item)
        return list(set(files) - set(ignored))

    def is_directory(self, path):
        """
        returns True if the given path points to a directory
        """
        return os.path.isdir(path)
    
    def create_dir(self, path_name):
        """
        creates a directory with the given pathname at the filesystem
        """
        if not self.path_exists(path_name):
            self.__log.debug("creating dir with the path: %s" % path_name)
            os.mkdir(path_name)

    def delete_dir_content(self, path_name):
        """
        deletes the directories content without deleting the root folder as well
        """
        if self.path_exists(path_name):
            self.__log.debug("deleting dir content: %s" % path_name)
            for item in os.listdir(unicode(path_name)):
                item_path = unicode(path_name) + "/" + item
                if os.path.isdir(item_path):
                    self.delete_dir(item_path)
                elif os.path.islink(item_path):
                    os.unlink(item_path)
                else:
                    os.remove(item_path)
        
    def delete_dir(self, path_name):
        """
        deletes a given directory and its content
        """
        if self.path_exists(path_name):
            self.__log.debug("deleting dir: %s" % path_name)
            for item in os.listdir(path_name):
                item_path = path_name + "/" + item
                if os.path.isdir(item_path):
                    self.delete_dir(item_path)
                elif os.path.islink(item_path):
                    os.unlink(item_path)
                else:
                    os.remove(item_path)
            os.rmdir(path_name)
        
    def create_link(self, source, link_path):
        """
        creates a symbolic link on Linux and Mac, a .lnk link at Windows file systems
        pointing to source, named link_name." 
        source -> the original file/dir
        link_path -> the path to the link
        """
        source = unicode(source)
        link_path = unicode(link_path)
        ## at first check if the link already exists
        ##TODO @wolfgang ... tried it with a whitespace containing (dir)item - and it did not check that the path already exists
        if os.path.exists(link_path):
            self.__log.debug("link (%s) already exists ... do nothing" % link_path)
            return
            
        self.__log.debug("creating link --- %s --- with the path: %s" % (link_path, source))
        if source.find(":/") == -1:
            source = source.replace(":", ":/")
        if link_path.find(":/") == -1:
            link_path = link_path.replace(":", ":/")
        self.file_system.create_link(source, link_path)
        
#    def move_file(self, file_path, target_directory):
#        """
#        moves an existing file to another directory
#        """
#        pass
    
    def rename_file(self, src, dst):
        """
        creates a file at the file system
        """
        os.rename(src, dst)

    def create_file(self, file_path):
        """
        creates a file at the file system
        """
        file = open(file_path, "w")
        file.close()
        
    def remove_file(self, file_path):
        """
        removes a file from filesystem
        """
        if self.path_exists(file_path):
            os.remove(file_path)
        
    def remove_link(self, file_path):
        """
        removes a given link from file system and also the folder if it's empty
        """
        self.file_system.remove_link(file_path)
        
    def inode_shortage(self, file_path):
        """
        returns True, if the free number of inodes (non-root) < 10% of all available
        Caution: Windows does not support this functionality, that's why it returns False in any case
        """
        return self.file_system.inode_shortage(file_path)
        
        
## end