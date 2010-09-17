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

import os
from stat import *

class FileSystem():

    def __init__(self):
        pass
    
    def create_link(self, source, link_name):
        """
        creates a symbolic link with given link_name using a relative path to the source
        """
        ## create relative link from given source paths
        prefix = "/".join(source.split("/")[0:-2])
        rel_source = source.lstrip(prefix)
        steps_back  = len(link_name.lstrip(prefix).split("/"))
        for step in range(1, steps_back):
            rel_source = "../" + rel_source

        ## create relative symlink
        os.symlink(rel_source, link_name)

    def remove_link(self, link):
        """
        removes a windows .lnk link from file system and empty folders as well
        """
        if os.path.exists(link):
            os.remove(link)
        ## delete folder if empty
        link = unicode(link)
        parent_path = "/".join(link.split("/")[:-1])
        if len(os.listdir(parent_path)) == 0: #empty
            os.rmdir(parent_path)        
        
    def inode_shortage(self, file_path):
        """
        returns True, if the free number of inodes (non-root) < 10% of all available
        """
        max = os.stat(file_path)[ST_DEV]
        free = max - os.stat(file_path)[ST_INO]
        
        if free*10 >= max:
            return False
        return True
        
                      
## end 