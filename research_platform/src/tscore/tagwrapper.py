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

import time
#from PyQt4.QtCore import QSettings
import PyQt4.QtCore


class TagWrapper():

    GROUP_STORE_NAME = "store"
    GROUP_FILES_NAME = "files"
    GROUP_CATEGORIES_NAME = "categories"
    
    KEY_STORE_ID = "store_id"
    KEY_TAGS = "tags"
    KEY_TIMESTAMP = "timestamp"
    KEY_CATEGORY = "category"

    TAG_SEPARATOR = ","
    
    def __init__(self, file_path, store_id=None):
        """
        constructor
        """
        if store_id is not None:
            self.__create_file_structure(file_path, store_id)
        self.__settings = QSettings(file_path, QSettings.IniFormat)
 
    def __create_file_structure(self, file_path, store_id):
        """
        creates the default file structure in a given file
        """
        file = open(file_path, "w")
        file.write("[store]\n")
        file.write("store_id=%s\n" % store_id)
        file.write("\n")
        file.write("[files]\n")
        file.write("\n")
        file.write("[categories]\n")
        file.close()
                
    def __get_file_list(self):
        """
        returns a list of file dictionaries sorted by timestamp descending
        """
        file_list = []
        self.__settings.beginGroup(self.GROUP_FILES_NAME)
        for file in self.__settings.childGroups():
            self.__settings.beginGroup(file)            
            tags = unicode(self.__settings.value(self.KEY_TAGS, "").toString()).split(self.TAG_SEPARATOR)
            timestamp = unicode(self.__settings.value(self.KEY_TIMESTAMP, "").toString())
            file_list.append(dict(filename=file, tags=tags, timestamp=timestamp))
            self.__settings.endGroup()
        self.__settings.endGroup()
        return sorted(file_list, key=lambda k:k[self.KEY_TIMESTAMP], reverse=True)
        
    def __get_tag_dictionary(self):
        """
        iterates through all files and creates a dictionary with tags + counter
        """
        dictionary = dict()
        self.__settings.beginGroup(self.GROUP_FILES_NAME)
        for file in self.__settings.childGroups():
            self.__settings.beginGroup(file)
            tags = unicode(self.__settings.value(self.KEY_TAGS, "").toString()).split(self.TAG_SEPARATOR)
            self.__settings.endGroup()
            for tag in tags:
                if tag in dictionary:
                    dictionary[tag] += 1
                else:
                    dictionary[tag] = 1
        self.__settings.endGroup()
        return dictionary

    def get_files(self):
        """
        returns a list of all files stored in the config file
        """
        files = []
        self.__settings.beginGroup(self.GROUP_FILES_NAME)
        for file in self.__settings.childGroups():
            files.append(unicode(file))
        self.__settings.endGroup()
        return files

    def set_file_path(self, file_path):
        """
        sets the internal path to the source file
        """
        self.__settings = QSettings(file_path, QSettings.IniFormat)

    def get_store_id(self):
        """
        returns the store id of the current file
        """
        self.__settings.beginGroup(self.GROUP_STORE_NAME)
        id = unicode(self.__settings.value(self.KEY_STORE_ID, "").toString())
        self.__settings.endGroup()
        return id
    
    def get_all_tags(self):
        """
        returns all tags of the current store sorted by name asc
        """
        dictionary = self.__get_tag_dictionary()
        return sorted(dictionary.keys())

    def get_recent_tags(self, no_of_tags):
        """
        returns a defined number of recently entered tags
        """
        tags = set()
        files = self.__get_file_list()
        position = 0
        while len(tags) < no_of_tags and position < len(files) and files[position] is not None:
            tags = tags.union(set(files[position]["tags"]))
            position +=1
        return sorted(tags)[:no_of_tags]

    def get_recent_files_tags(self, no_of_files):
        """
        returns all tags of a number of recently entered files
        """
        tags = set()
        files = self.__get_file_list()
        position = 0
        while position < no_of_files and position < len(files) and files[position] is not None:
            tags = tags.union(set(files[position]["tags"]))
            position +=1
        return sorted(tags)

    def get_popular_tags(self, no_of_tags):
        """
        returns a defined number of the most popular tags
        """
        dictionary = self.__get_tag_dictionary()
        list = sorted(dictionary.iteritems(), key=lambda (k,v): (v,k), reverse=True)
        return_list = []
        for item in list[:no_of_tags]:
            return_list.append(item[0])
        return return_list

    def set_file(self, file_name, tag_list, timestamp=time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())):
        """
        adds a file and its tags to the config file
        or overrides the tags of an existing file
        assumption: file_name and tag_list do not have to be checked
        for lower case upper case inconsistencies 
        """
        self.__settings.beginGroup(self .GROUP_FILES_NAME)
        self.__settings.beginGroup(file_name)
        self.__settings.setValue(self.KEY_TAGS, self.TAG_SEPARATOR.join(tag_list))
        self.__settings.setValue(self.KEY_TIMESTAMP, timestamp)
        self.__settings.endGroup()
        self.__settings.endGroup()

    def set_tags(self, file_name, tag_list):
        """
        resets the files tag list
        if the file does not exist it is created
        """
        if not self.file_exists(file_name):
            self.set_file(file_name, tag_list)
        else:
            self.__settings.beginGroup(self .GROUP_FILES_NAME)
            self.__settings.beginGroup(file_name)
            self.__settings.setValue(self.KEY_TAGS, self.TAG_SEPARATOR.join(tag_list))
            self.__settings.endGroup()
            self.__settings.endGroup()

    def rename_file(self, old_file_name, new_file_name):
        """
        renames an existing file
        """
        self.__settings.beginGroup(self .GROUP_FILES_NAME)
        self.__settings.beginGroup(old_file_name)            
        tags = unicode(self.__settings.value(self.KEY_TAGS, "").toString()).split(self.TAG_SEPARATOR)
        timestamp = unicode(self.__settings.value(self.KEY_TIMESTAMP, "").toString())
        self.__settings.endGroup()
        self.__settings.remove(old_file_name)
        self.__settings.endGroup()
        self.set_file(new_file_name, tags, timestamp)
        
    def remove_file(self, file_name):
        """
        removes a given file from the config file
        """
        self.__settings.beginGroup(self .GROUP_FILES_NAME)
        self.__settings.remove(file_name)
        self.__settings.endGroup()
    
    def file_exists(self, file_name):
        """ 
        checks if a file is already existing in the section "files"
        """
        self.__settings.beginGroup(self .GROUP_FILES_NAME)
        files = self.__settings.childGroups()
        self.__settings.endGroup()
        if file_name in files:
            return True
        return False
    
    def rename_tag(self, old_tag_name, new_tag_name):
        """
        renames all tags in the store
        """
        files = self.__get_file_list()
        for file in files:
            tags = file["tags"]
            if old_tag_name in tags:
                tags.remove(old_tag_name)
                tags.append(new_tag_name)
                self.set_tags(file["filename"], tags)

    def remove_tag(self, tag_name):
        """
        removes the given tag from all file entries
        """
        files = self.__get_file_list()
        for file in files:
            tags = file["tags"]
            if tag_name in tags:
                tags.remove(tag_name)
                self.set_tags(file["filename"], tags)
                

## end