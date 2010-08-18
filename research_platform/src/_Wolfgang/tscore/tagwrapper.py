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

from PyQt4.QtCore import QVariant, QSettings, QString


class TagWrapper():

    __pyqtSignals__ = ("changed()", )
    
    GROUP_STORE_NAME = "store"
    GROUP_STORE_ID_NAME = "store_id"
    GROUP_FILES_NAME = "files"
    GROUP_CATEGORIES_NAME = "categories"
    

    def __init__(self, file_path, store_id = None):
        """
        constructor
        """
        if store_id is not None:
            self.__create_file_structure(file_path, store_id)
        self.__tag_file_handler = QSettings(file_path, QSettings.IniFormat)
            
        self.__tag_dict = {}
        self.__create_tag_dict()
            
    def __create_file_structure(self, file_path, store_id):
        """
        creates the default file structure in a given file
        """
        file = open(file_path, "w")
        file.write("[store]\n")
        file.write("id=%s\n" % store_id)
        file.write("[files]\n")
        file.write("testfile.txt=\"tag,tagger,dagger,TUG,TUG,DA\"\n")
        file.write("diplomarbeit.tex=\"DA,TUG,tagstore\"\n")
        file.write("[categories]\n")
        file.close()
                
    def __create_tag_dict(self):
        """
        iterates through all files and creates a dictionary with tags + count
        """
        self.__tag_file_handler.beginGroup(TagWrapper.GROUP_FILES_NAME)

        ## iterate all files and their tags .. with each new tag create a dict entry
        ## if the tag already exists -> increment the count         
        ## key = tagname value = count
        for child in self.__tag_file_handler.childKeys():
            child_value = self.__tag_file_handler.value(child)
            tag_list_string = unicode(child_value.toString())
            for tag in tag_list_string.split(","):
                if tag in self.__tag_dict:
                    self.__tag_dict[tag] += 1
                else:
                    self.__tag_dict[tag] = 1
        
        self.__tag_file_handler.endGroup()
        
    def set_file_path(self, file_path):
        """
        sets the internal path to the source file
        """
        ## TODO maybe initialize the whole new object?
        self.__tag_file_handler = QSettings(file_path, QSettings.IniFormat)

    def get_store_id(self):
        """
        returns the store id of the current file
        """
        return unicode(self.__tag_file_handler.value(TagWrapper.GROUP_STORE_ID_NAME+"/"+TagWrapper.GROUP_STORE_ID_NAME, "0").toString())
    
    def get_all_tags(self):
        """
        returns all tags of the current store
        """
        pass
    
    def get_recent_tags(self, no_of_tags):
        """
        returns a defined number of recently entered tags
        """
        pass
    
    def get_recent_files_tags(self, no_of_files):
        """
        returns all tags of a number of recently entered files
        """
        pass
    
    def get_popular_tags(self, no_of_tags):
        """
        returns a defined number of the most popular tags
        """
        
        ## dictionaries cannot be sorted by value
        ## at first create a sorted list of the dictionary
        sorted_list = sorted(self.__tag_dict, key=self.__tag_dict.get, reverse=True)
        return sorted_list[0:no_of_tags]
    
    def set_file(self, file_name, tag_list):
        """
        adds a file and its tags to the config file
        or overrides the tags of an existing file
        assumption: file_name and tag_list do not have to be checked
        for lower case upper case inconsistencies 
        """
        
        ## concat the tag list to a string representation
        tag_string = ""
        for tag in tag_list:
            if tag_string == "":
                tag_string = tag
            else:
                tag_string = tag_string + "," + tag

        self.__tag_file_handler.setValue(TagWrapper.GROUP_FILES_NAME+"/"+file_name, tag_string)
    
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
    
    def exists_file(self, file_name):
        """ 
        checks if a file is already existing in the section "files"
        """
        return self.__tag_file_handler.contains(TagWrapper.GROUP_FILES_NAME+"/"+file_name)
    
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