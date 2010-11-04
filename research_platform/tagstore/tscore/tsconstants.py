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

class TsConstants(object):
    """
    class for providing internal used constants
    just do provide a single-point of access
    """
    
    LOGGER_NAME = "TagStoreLogger"
    LOG_FILENAME = "tagstore.log"
    LOG_BACKUP_COUNT = 5
    LOG_FILESIZE = 524288
    ## the percentage of inodes which must be left free 
    INODE_THRESHOLD = 10
    
    CONFIG_PATH = "./tsresources/conf/tagstore.cfg"

    #STORE_STORAGE_DIR_EN = "storage"#,Ablage"
    #STORE_DESCRIBING_NAVIGATION_DIR_EN = "navigation"#,Navigation"
    #STORE_CATEGORIZING_NAVIGATION_DIR_EN = "categorization"#,Kategorisierung"
    #STORE_EXPIRED_DIR_EN = "expired_items"#abgelaufene_Daten"
    
    SETTING_SUPPORTED_LANGUAGES = "supported_languages"
    SETTING_AUTO_DATESTAMP = "automatic_datestamp"
    SETTING_TAG_SEPARATOR = "tag_separator"
    
    SETTING_DATESTAMP_FORMAT = "datestamp_format"
    
    SETTING_CATEGORY_MANDATORY = "category_mandatory"
    SETTING_SHOW_CATEGORY_LINE = "show_category_line"
    ## this constant is NOT used at the config file - it is a "gui setting name"
    SETTING_CATEGORY_VOCABULARY = "category_vocabulary"
    SETTING_DESC_TAGS = "describing_tags"
    SETTING_CAT_TAGS = "categorizing_tags"
    
    SETTING_EXPIRY_PREFIX = "expiry_prefix"
    
    DATESTAMP_FORMAT_DAY = "%Y-%m-%d"
    DATESTAMP_FORMAT_MONTH = "%Y-%m"
    
    DEFAULT_EXPIRY_PREFIX = "exp_"
    DEFAULT_STORE_CONFIG_DIR = ".tagstore"
    DEFAULT_STORE_CONFIG_FILENAME = "store.cfg"
    DEFAULT_STORE_TAGS_FILENAME = "store.tgs"
    DEFAULT_STORE_VOCABULARY_FILENAME = "vocabulary.txt"
    DEFAULT_RECENT_TAGS = 5
    DEFAULT_POPULAR_TAGS = 5
    DEFAULT_MAX_TAGS = 3
    DEFAULT_TAG_SEPARATOR = ","
    DEFAULT_SUPPORTED_LANGUAGES = ["en", "de"]
    
    def __init__(self):
        pass
    
## end