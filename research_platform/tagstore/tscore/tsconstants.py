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
    
    
    STORE_CONFIG_DIR = ".tagstore"
    STORE_CONFIG_FILENAME = "store.tgs"
    STORE_POSSIBLE_STORAGE_DIRS = "storage,Ablage"
    STORE_POSSIBLE_NAVIGATION_DIRS = "navigation,Navigation"
    
    SETTING_SUPPORTED_LANGUAGES = "supported_languages"
    SETTING_AUTO_DATESTAMP = "automatic_datestamp"
    SETTING_TAG_SEPARATOR = "tag_separator"
    SETTING_DATESTAMP_FORMAT = "datestamp_format"
    
    DATESTAMP_FORMAT = "%Y%m%d"
    
    DEFAULT_RECENT_TAGS = 5
    DEFAULT_POPULAR_TAGS = 5
    DEFAULT_MAX_TAGS = 3
    DEFAULT_TAG_SEPARATOR = ","
    DEFAULT_SUPPORTED_LANGUAGES = ["en", "de"]
    
    def __init__(self):
        pass