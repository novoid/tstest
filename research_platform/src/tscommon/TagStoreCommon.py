#!/usr/bin/env python

# -*- coding: iso-8859-15 -*-
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
import ConfigParser

class TagStoreConstants(object):
    '''
    Class for providing constants in a static way within the whole application 
    '''
    
    ## name of the file where tag information is stored
    TAG_FILE_NAME = ".tag_dictionary"

    ## default path to the config file
    CONFIG_PATH = "../conf/tagstore.cfg"

    ## use this constants when accessing data from the config file
    CONFIG_FILEDIR = "FILEDIR"
    CONFIG_METADATADIR = "METADATADIR"
    CONFIG_TAGDIR = "TAGDIR"

    ## defines how much tags are presented to the user
    NR_OF_POPULAR_TAGS = 4
    RECENTLY_USED_TAGS = 5

    ## constants for the sections used in the config file
    TAG_SECTION_GENERAL = "General"
    TAG_SECTION_RECENT = "Recent"
    TAG_FILE_SEPARATOR = ","

    def __init__(self, params):
        '''
        Constructor
        '''


class TagStoreConfigReader(object):
    '''
    Class for conveniently accessing config parameters
    '''

    def __init__(self, params):
        '''
        Create the pasrser object and read the config file
        '''
        ## read the config file
        self.parser = ConfigParser.SafeConfigParser()
        self.parser.read(TagStoreConstants.CONFIG_PATH)
    
    # TODO: make a static method
    def get_value(self, section, key):
        return self.parser.get(section, key)
    
    def get_sections(self):
        return self.parser.sections()
    
    def get_items_of_section(self, section):
        return self.parser.items(section)


class TagStoreTagHandler(object):
    '''
    Class for all required tag operations, like get_tags, get_latest_tags, get_most_popular_tags, ...
    '''

    def __init__(self, params):
        '''
        Constructor
        '''
        config = TagStoreConfigReader(sys.argv)
        self.tagFilePath = config.get_value(TagStoreConstants.TAG_SECTION_GENERAL, TagStoreConstants.CONFIG_FILEDIR) + "/" + TagStoreConstants.TAG_FILE_NAME

        ## read the tagfile as a config file
        self.tagParser = ConfigParser.SafeConfigParser()
        self.tagParser.read(self.tagFilePath)
        
    def get_all_tags(self):
        tags = []
        ## step through all sections except "recent"
        for section in self.tagParser.sections():
            for element in self.tagParser.items(section):
                tags.append(element[0])
        return tags
        
    def get_recent_tags(self):
        ## get all items of section "Recent"
        recentSection = self.tagParser.items(TagStoreConstants.TAG_SECTION_RECENT)
        tags = []
        for element in recentSection:
            tags.append(element[0])
        
        return tags
        
    
    def get_popular_tags(self):
        '''
        this method returns the n (n is defined in TagStoreConstants) most popular tags from the tagfile
        returns a list of TagCount objects which provide the tagname AND the count of 
        how often this tag is used
        '''
        
        ## this list is used to store the most used tags
        ## initialized with zeros 
        tags = [TagCount("dummy", 0)]
        
        for section in self.tagParser.sections():
            for element in self.tagParser.items(section):
                tag = element[0]
                if len(element[1]) > 0:
                    fileString = element[1]
                    fileList = fileString.split(TagStoreConstants.TAG_FILE_SEPARATOR)
                    ## get the count of files assigned to the tag 
                    fileCount = len(fileList)
                    tagCountObject = TagCount(tag, fileCount)
                    ## iterate the current tag-counts and replace elements if necessary
                    for currentCountObject in tags:
                        if fileCount >= currentCountObject.tagCount:
                            pos = tags.index(currentCountObject)
                            tags.insert(pos, tagCountObject)
                            ## just pop the last value, if the list is too long  
                            if len(tags) > TagStoreConstants.NR_OF_POPULAR_TAGS:
                                tags.pop()
                            break
        return tags
    
        def write_new_tag(self, section, tagName):
            '''
            write a new tag to the given section
            '''
            pass
        
        def add_file(self, tagName, fileName):
            '''
            add a filename to the given tagName
            '''
            pass
        def remove_file(self, tagName, fileName):
            '''
            remove a filename from the given tagName
            '''
            pass
        
class TagCount(object):
    '''
    object for storing a tagname and the count of its associations
    '''
    
    def __init__(self, tagName, tagCount):
        self.tagName = tagName
        self.tagCount = tagCount
        
    def __str__(self):
        return self.tagName + " (%s)" % (self.tagCount)
## end