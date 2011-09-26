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

import sys

from logentry import LogEntry

"""
this class parses log files of the tagstore
"""

class LogParser:

    SETTINGS_SECTION = "[settings]"
    CONFIG_LINE = "config_format="
    FILES_SECTION = "[files]"
    SUPPORTED_VERSION = 1
    PATH_SEPERATOR = "\\"
    VALUE_SEPERATOR = "="
    KEYWORD_TAGS = "tags"
    KEYWORD_TIMESTAMP = "timestamp"
    KEYWORD_HASHSUM = "hashsum"


    def __init__(self, file_name):
        """
        constructor
        """
        
        #
        # initialize members
        #
        self.__file_dict = dict()
        self.__file_name = file_name

    def __get_line(self, log_handle):
        """
        reads a line from a file and removes last character (new line)
        """

        #
        # get line
        #
        line = log_handle.readline()

        if line != None:
            #
            # remove new line character
            #
            line = line[0:len(line)-1]

        return line

    def get_log_entries(self):
        """
        returns a list of log entries
        """
        return self.__file_dict.items()

    def add_log_entry(self, new_entry):
        """
        adds a log entry to the dictionary.
        if it already exists, it is replaced
        """
       
        #
        # store entry in dictionary
        #
        self.__file_dict[new_entry.get_file_name()] = new_entry
        
    def write_log_entries(self, write_hashsum=False):
        """
        writes all current log entries to the specified log file
        """

        #
        # open the log
        #
        log_handle = open(self.__file_name, "w")

        #
        # check if it was opened
        #
        if not log_handle:
            #
            # failed to open
            #
            print "Error: failed to open " + self.__file_name
            return False

        #
        # write common format header
        #
        log_handle.write(self.SETTINGS_SECTION + "\n")
        log_handle.write(self.CONFIG_LINE + 
                         repr(self.SUPPORTED_VERSION) + "\n")
        log_handle.write("\n")
        log_handle.write(self.FILES_SECTION + "\n")

        #
        # write entries
        #
        for k,v, in self.__file_dict.iteritems():
            self.__write_log_entry(log_handle, v, write_hashsum)


        #
        # close log handle
        #
        log_handle.close()

        #
        # done
        #
        return True

    def __write_log_entry(self, log_handle, new_entry, write_hashsum):
        """
        write a log entry to the log file
        """

        #
        # get file name
        #
        file_name = new_entry.get_file_name()

        #
        # write tags line
        #
        line = file_name + self.PATH_SEPERATOR + self.KEYWORD_TAGS + \
               self.VALUE_SEPERATOR + "\"" + new_entry.get_tags() + "\"\n"
        log_handle.write(line)
        
        #
        # write time stamp line
        #
        line = file_name + self.PATH_SEPERATOR + self.KEYWORD_TIMESTAMP +\
               self.VALUE_SEPERATOR + new_entry.get_time_stamp() + "\n"
        log_handle.write(line)

        if write_hashsum:
            #
            # write hashsum line
            #
            line = file_name + self.PATH_SEPERATOR + self.KEYWORD_HASHSUM + \
            self.VALUE_SEPERATOR + new_entry.get_hash_sum() + "\n"
            log_handle.write(line)

    def initialize(self):
        """
        initializes the log parser
        """

        #
        # opening log
        #
        log_handle = open(self.__file_name, "r")
    
        #
        # check if it was opened
        #
        if not log_handle:
            #
            # failed to open
            #
            print "Error: failed to open " + self.__file_name
            return False

        #
        # read first line
        #
        line = self.__get_line(log_handle)

        if line != self.SETTINGS_SECTION:
            #
            # file format error
            #
            print "Error: expected settings section but got " + line
            return False

        #
        # read next line
        #
        line = self.__get_line(log_handle)

        if line.startswith(self.CONFIG_LINE) == False:
            #
            # file format error
            #
            print "Error: expected config_format but got " + line
            return False

        
        #
        # extract version
        #
        version_str = line[len(self.CONFIG_LINE):len(line)]

        if self.SUPPORTED_VERSION != int(version_str):
            #
            # version mis-match
            #
            print "Error: expected " + repr(self.SUPPORTED_VERSION) +\
                  " but got " + version_str
            return False

        #
        # next line is empty
        #
        log_handle.readline()

        #
        # read next line
        #
        line = self.__get_line(log_handle)

        #
        # next line should start the files section
        #
        if line != self.FILES_SECTION:
            #
            # file format error
            #
            print "Error: expected file section but got " + line
            return False


        #
        # parse files section
        #
        result = self.__parse_files_section(log_handle)

        #
        # close log handle
        #
        log_handle.close()

        #
        # done
        #
        return result

    def __parse_files_section(self, log_handle):
        """
        parses the files section of the log file
        """

        #
        # create a log entry
        #
        entry = None
    
        #
        # read line
        #
        line = self.__get_line(log_handle)

        #
        # parse log file
        #
        while line:

            #
            # informal debug print
            #
            #print line

            #
            # find path seperator
            #
            last_slash = line.rfind(self.PATH_SEPERATOR);
            equal_character = line.find(self.VALUE_SEPERATOR)

            #
            # sanity checks
            #
            assert last_slash > 0, "invalid line"
            assert equal_character > last_slash, " invalid line"

            #
            # extract keyword from line
            #
            keyword = line[last_slash+1:equal_character]

            #
            # extract file path from line
            #
            file_path = line[0:last_slash]

            #
            # get log entry
            #
            entry = self.__file_dict.get(file_path)

            #
            # check if it already exists
            #
            if entry is None:
                #
                # create new log object
                #
                entry = LogEntry()
                
                #
                # store file name
                #
                entry.set_file_name(file_path)

                #
                # store entry in dictionary
                #
                self.__file_dict[file_path] = entry

            #
            # parse keywords
            #
            if keyword == self.KEYWORD_TAGS:

                #
                # check if there are double quotes attached
                #
                if line[equal_character+1] == "\"":
                    #
                    # remove double quotes
                    #
                    entry.set_tags(line[equal_character+2:len(line)-1])
                else:
                    #
                    # no double quotes
                    #
                    entry.set_tags(line[equal_character+1:len(line)])

                    print entry.get_tags() 
            elif keyword == self.KEYWORD_TIMESTAMP:
                entry.set_time_stamp(line[equal_character+1:len(line)])
            elif keyword == self.KEYWORD_HASHSUM:
                entry.set_hash_sum(line[equal_character+1:len(line)])
    
            #
            # get next line
            #
            line = self.__get_line(log_handle)
        
        #
        # done
        #
        return True


## end

