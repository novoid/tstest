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
        self.__log.debug("creating dir with the path: %s" % path_name)
        ## os.mkdir supports unix AND linux systems
        ## TODO: check if on windows, slash (http://www.wilsdomain.com/wp-content-photos/images/Slash-Saul-Hudson.jpg) has to be exchanged with backslash (\)
        os.mkdir(path_name)

    def create_link(self, source, link_name):
        """
        creates a symbolic link on Linux and Mac, a .lnk link at Windows file systems
        pointing to source, named link_name." 
        source -> the original file/dir
        link_name -> the name of the link
        """
        self.__log.debug("creating link --- %s --- with the path: %s" % (link_name, source))
        if link_name.find(":/") == -1:
            sname = link_name.replace(":", ":/")
        if source.find(":/") == -1:
            ssource = source.replace(":", ":/")
        self.file_system.create_link(ssource, sname)
        
## end