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
from PyQt4.QtCore import QVariant, QSettings, QString


class TagWrapper():

    __pyqtSignals__ = ("changed()", )
    
    GROUP_STORE_NAME = "store"
    GROUP_STORE_ID_NAME = "store_id"
    GROUP_FILES_NAME = "files"
    GROUP_CATEGORIES_NAME = "categories"
    
    KEY_TAGS = "tags"
    KEY_TIMESTAMP = "timestamp"
    KEY_CATEGORY = "category"

    def __init__(self, file_path, store_id=None):
        """
        constructor
        """
        self.__settings = QSettings(file_path, QSettings.IniFormat)
        
        for file in self.__get_file_list():
            print file["filename"]+": "+file["tags"]+": "+file["timestamp"]

        ## this code shows how to generate a timestamp, cast it to string ans back to time
        ##x = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())   #time.gmtime()
        ##print x
        ##y = time.strptime(x, "%Y-%m-%d %H:%M:%S")    ##time object    ##string
        ##print "new=" + time.strftime("%Y-%m-%d %H:%M:%S", y)
 
    def __get_file_list(self):
        self.__settings.beginGroup("files")
        files = self.__settings.childGroups()
        file_list = []
        for file in files:
            tags = unicode(self.__settings.value(file + "/" + TagWrapper.KEY_TAGS, "").toString())
            timestamp = unicode(self.__settings.value(file + "/" + TagWrapper.KEY_TIMESTAMP, "").toString())
            file_list.append(dict(filename=file, tags=tags, timestamp=timestamp))
        return sorted(file_list, key=lambda k:k["timestamp"], reverse=True)
        
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
        self.__tag_file_handler.beginGroup(TagWrapper.GROUP_FILES_NAME)
#        children_list = self.__tag_file_handler.childKeys()
        children_list = self.__tag_file_handler.childKeys()
        
        # do not allow 
        if no_of_files > children_list.count():
            no_of_files = children_list.count()
        
        for child_index in range(no_of_files):
            print unicode(children_list[child_index])
            #child_value = self.__tag_file_handler.value(child_index)
        
    
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
        self.__tag_file_handler.beginGroup(TagWrapper.GROUP_FILES_NAME)
        if self.__tag_file_handler.contains(file_name):
            ## remove the key/value pair
            ## to add it as new at thebottom of the files-group
            self.__tag_file_handler.remove(file_name)
            # manually synchronize to write the changes to the file
            self.__tag_file_handler.sync()
        ## concat the tag list to a string representation
        tag_string = ""
        for tag in tag_list:
            if tag_string == "":
                tag_string = tag
            else:
                tag_string = tag_string + "," + tag

        self.__tag_file_handler.setValue(file_name+"/tags", tag_string)
        self.__tag_file_handler.setValue(file_name+"/timestamp", date.today())
        self.__tag_file_handler.endGroup()    
    
    def rename_file(self, old_file_name, new_file_name):
        """
        renames an existing file
        """
        self.__tag_file_handler.beginGroup(TagWrapper.GROUP_FILES_NAME)
        if self.__tag_file_handler.contains(old_file_name):
            old_val = self.__tag_file_handler.value(old_file_name)
            self.__tag_file_handler.remove(old_file_name)
            self.__tag_file_handler.setValue(new_file_name, old_val)
        self.__tag_file_handler.endGroup()
    
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