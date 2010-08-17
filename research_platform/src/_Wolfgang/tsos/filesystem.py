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
if sys.platform[:3] == "win":
    from windows import FileSystem
elif sys.platform == "darwin":
    from osx import FileSystem
else:
    from linux import FileSystem


class FileSystemWrapper():

    def __init__(self):
        self.file_system = FileSystem()
    
    def pathExists(self, path):
        return os.path.exists(path)
    
    def find(self, inPath, searchPath):
        for file in os.listdir(inPath):
            if os.path.exists(inPath + "/" + file + "/" + searchPath):
                return inPath + "/" + file
        return ""

    def rename(self, oldName, newName):
        os.rename(oldName, newName)

        
## end