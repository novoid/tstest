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

class FileSystem():

    def __init__(self):
        pass
    
    def create_link(self, source, link_name):
        """
        creates a symbolic link with given link_name using a relative path to the source
        """
        ## create relative link from given source paths
        prefix_length = len(source.split("/")[0:-2])
        rel_source = "/".join(source.split("/")[prefix_length:])
        steps_back  = len(link_name.split("/")) - prefix_length
        for step in range(1, steps_back):
            rel_source = "../" + rel_source

        ## create relative symlink
        os.symlink(rel_source, link_name)

    def inode_shortage(self, file_path):
        """
        returns True, if the free number of inodes (non-root) < 10% of all available
        """
        info = os.statvfs(file_path)
        max = int(info.f_files)
        free = int(info.f_ffree)
        
        if free*10 >= max:
            return False
        return True
        
              
## end