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


class TagWrapper():

    __pyqtSignals__ = ("changed()", )

    def __init__(self, file_path, store_id = 0):
        """
        constructor
        """
        self.__settings = QtCore.QSettings(file_path, QtCore.QSettings.IniFormat)
        if store_id != 0:
            self.__create_file(store_id)
            
    def __create_file(self, store_id):
        """
        creates the default file structure in a given file
        """
        pass
                
    def set_file_path(self, file_path):
        """
        sets the internal path to the source file
        """
        self.__settings = QtCore.QSettings(file_path, QtCore.QSettings.IniFormat)

    def get_store_id(self):
        """
        returns the store id of the current file
        """
        self.__settings.beginGroup("store")
        id = unicode(self.__settings.value("store_id", "0").toString())
        self.__settings.endGroup()
        return id
    
    def get_all_tags(self):
        """
        returns all tags of the current store
        """
        pass
    
    def get_recent_tags(self, number):
        """
        returns a defined number of recently entered tags
        """
        pass
    
    def get_recent_files_tags(self, number):
        """
        returns all tags of a number of recently entered files
        """
        pass
    
    def get_popular_tags(self, number):
        """
        returns a defined number of the most popular tags
        """
        pass
    
    def set_file(self, file_name, tag_list):
        """
        rewrites the tags of an existing file or
        adds a file an its tags to the config file
        """
        pass    
    
    def rename_file(self, old_file_name, new_file_name):
        """
        renames an existing file
        """
        pass    
    
    def remove_file(self, file_name):
        """
        removes a given file from the config file
        """
        pass    
    
    def rename_tag(self, old_tag_name, new_tag_name):
        """
        renames all tags in the store
        """
        pass    

    def remove_tag(self, tag_name):
        """
        removes the given tag from all file entries
        """
        pass    


## end