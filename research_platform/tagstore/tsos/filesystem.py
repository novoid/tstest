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
        self.__IGNORED_FILE_PREFIXES = ["~$"]
        self.__IGNORED_DIR_PREFIXES = ["."]
        self.__IGNORED_EXTENTIONS = [".lnk"]
    
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

    def create_link(self, source, link_path):
        """
        creates a symbolic link on Linux and Mac, a .lnk link at Windows file systems
        pointing to source, named link_name." 
        source -> the original file/dir
        link_path -> the path to the link
        """
        source = unicode(source)
        link_path = unicode(link_path)
        self.__log.debug("creating link --- %s --- with the path: %s" % (link_path, source))
        if source.find(":/") == -1:
            source = source.replace(":", ":/")
        if link_path.find(":/") == -1:
            link_path = link_path.replace(":", ":/")
        self.file_system.create_link(source, link_path)
        
    def create_file(self, file_path):
        """
        creates a file at the file system
        """
        file = open(file_path, "w")
        file.close()
        
    def inode_shortage(self, file_path):
        """
        returns True, if the free number of inodes (non-root) < 10% of all available
        """
        return False
        #st=os.stat(file_path)
        #print "file size", "=>",st[stat.ST_SIZE]
        #print "inode number", "=>",st[stat.ST_INO]
        #print "device inode resides on", "=>",st[stat.ST_DEV]
        #print "number of links to this inode", "=>",st[stat.ST_NLINK]
        
#        fd = os.open(file_path, os.O_RDWR|os.O_CREAT )
#        info = os.fstatvfs(fd)
        info = os.statvfs(file_path)    #try this instead of opening file
        max = info.f_files
        free = info.f_ffree
#        os.close(fd)
        print max
        print free
        
#        if free*10 > max:
#            return False
#        return True
        
        
## end