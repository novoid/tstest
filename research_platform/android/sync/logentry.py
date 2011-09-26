# -*- coding: utf-8 -*-

## this file is part of tagstore, an alternative way of storing and retrieving information
## Copyright (C) 2011  Karl Voit, Johannes Anderwald
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

"""
this class is used to store the details of log entry of an [android] tagstore log file entry
"""
class LogEntry:

    def __init__(self):
        """
        constructor
        """
        self.__file_name = ""
        self.__time_stamp = ""
        self.__tags = ""
        self.__hash_sum = ""

    def set_file_name(self, file_name):
        """
        sets the file name
        """
        self.__file_name = file_name

    def get_file_name(self):
        """
        returns the file name
        """
        
        return self.__file_name

    def set_time_stamp(self, time_stamp):
        """
        sets the time stamp
        """
        self.__time_stamp = time_stamp

    def get_time_stamp(self):
        """
        returns the time stamp
        """
        return self.__time_stamp

    def set_tags(self, tags):
        """
        sets the tags
        """
        self.__tags = tags

    def get_tags(self):
        """
        returns the tags
        """
        return self.__tags

    def set_hash_sum(self, hash_sum):
        """
        sets the hash sum
        """
        self.__hash_sum = hash_sum

    def get_hash_sum(self):
        """
        returns the hash sum
        """
        return self.__hash_sum

## end

