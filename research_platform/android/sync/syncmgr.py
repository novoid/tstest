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
import os
import time
import shutil
import urllib
from datetime import date

from optparser import OptionParser
from logparser import LogParser
from logentry import LogEntry
from hashsum import HashsumGenerator


"""
this class is responsible for syncing a tagstore to android store
"""
class SyncManager:

    def __init__(self, option_parser):
        """
        constructor
        """
        
        #
        # initialize member variables
        #
        self.__option_parser = option_parser
        self.__source_parser = None
        self.__target_parser = None

    def initialize(self):
        """
        initializes the sync manager
        """

        #
        # get source tagstore directory
        #
        source_dir = self.__option_parser.get_source_directory() \
                     + "/.tagstore/store.tgs"

        #
        # check if file exists
        #
        tagstore_exists = os.path.exists(source_dir)
        if tagstore_exists == 0:
            print "error: invalid tagstore directory " + source_dir
            return False
    
        #
        # new log parser to parse tagstore log
        #
        self.__source_parser = LogParser(source_dir)
        log_init = self.__source_parser.initialize()
        if log_init == False:
            print "error: failed to initialize the parser"
            return False

        #
        # append target path to target store
        #
        target_dir = self.__option_parser.get_target_directory() \
                     + "/.tagstore/store.tgs"
    
        #
        # check if file exists
        #
        target_exists = os.path.exists(target_dir)
        if target_exists == 0:
            print "Error: invalid target path " + target_dir
            return False

        #
        # new log parser to parse android log
        #
        self.__target_parser = LogParser(target_dir)
        log_init = self.__target_parser.initialize()
        if log_init == False:
            print "Error: failed to initialize the parser with path: " \
                  + android_dir
            return False
    
        #
        # done
        #
        return True

    def sync_tagstores(self):
        """
        synchronizes the tagstore with the android store
        """
        
        #
        # get log entries from the source tagstore
        #
        source_entries = self.__source_parser.get_log_entries()

        #
        # get log entries from the target tagstore
        #
        target_entries = self.__target_parser.get_log_entries()

        #
        # go through all entries
        #
        for k, v in source_entries:
            #
            # check if target android store has also a file with that name
            #
            same_file_entry = self.__find_file_name(target_entries, 
                                                  urllib.url2pathname(v.get_file_name()))

            #
            # is there an entry with the same file
            #
            if same_file_entry is None:
                #
                # file is not present
                #
                print " no file entry "
                self.__do_copy_entry(v)
                continue

            #
            # check if file still exists
            #
            file_name = self.__option_parser.get_target_directory() + "/" + urllib.url2pathname(same_file_entry.get_file_name())
            file_exists = os.path.exists(file_name)
            if not file_exists:
                #
                # file not present, copy it
                #
                self.__do_copy_entry(v)
                continue

            #
            # calculate hashsum of both entries
            # FIXME: hardcoded storage path
            #
            source_hash_sum = self.__get_hash_sum(v, 
                                                 self.__option_parser.get_source_directory() + "/Ablage")
            target_hash_sum = self.__get_hash_sum(same_file_entry, 
                                                  self.__option_parser.get_target_directory())

            if source_hash_sum == target_hash_sum:
                #
                # files are identical
                #
                print "File " + urllib.url2pathname(v.get_file_name()) + " is up2date..."
                continue

            #
            # get modification time
            # FIXME: hardcoded storage path
            #
            source_time = self.__get_modification_time(v, self.__option_parser.get_source_directory() + "/Ablage")
            target_time = self.__get_modification_time(same_file_entry, self.__option_parser.get_target_directory())

            #
            # convert to printable strings
            #
            source_date_string = time.ctime(source_time)
            target_date_string = time.ctime(target_time)

            if source_time > target_time:
                #
                # updating file
                #
                self.__do_copy_entry(v)
            else:
                print "Target file: " + urllib.url2pathname(v.get_file_name()) + " is newer than old file"
                print "Source time: " + source_date_string
                print "Target time: " + target_date_string

        #
        # write an updated log
        #
        self.__target_parser.write_log_entries(True)

        #
        # done
        #
        return True

    def __do_copy_entry(self, entry):
        """
        copies a file to target tagstore store
        """

        #
        # FIXME: hardcoded storage path
        #
        source_path = self.__option_parser.get_source_directory() + "/Ablage/" + urllib.url2pathname(entry.get_file_name())
        
        #
        # normalize path
        #
        source_path = os.path.normpath(source_path)

        #
        # get target path
        # FIXME: hardcoded storage path
        #
        target_path = self.__option_parser.get_target_directory() + "/Ablage/"

        #
        # create target directory if it does not exist
        #
        if not os.path.exists(target_path):
            os.makedirs(target_path)

        #
        # copy file
        #
        shutil.copy2(source_path, target_path)

        #
        # construct new log entry
        #
        new_entry = LogEntry()

        #
        # FIXME: hardcoded storage path
        # FIXME: tagstore logs don't include the directory
        #
        new_entry.set_file_name("Ablage/" + entry.get_file_name())
        new_entry.set_tags(entry.get_tags())
        new_entry.set_time_stamp(entry.get_time_stamp())
        new_entry.set_hash_sum(self.__get_hash_sum(new_entry, self.__option_parser.get_target_directory() + "/"))

        #
        # add log entry
        #
        self.__target_parser.add_log_entry(new_entry)

        #
        # informal debug print
        #
        print "Syncing " + source_path + " to " + target_path

    def __get_modification_time(self, entry, directory):
        """
        returns the last modification time
        """

        #
        # get full path of entry
        #
        file_name = directory + "/" + urllib.url2pathname(entry.get_file_name())

        #
        # get modification time
        #
        mod_time = os.path.getmtime(file_name)
        #print file_name + time.ctime(mod_time)

        #
        # convert to date object
        #
        #result = date.fromtimestamp(mod_time)

        #
        # done
        #
        return mod_time

    def __find_file_name(self, entries, file_name):
        """
        searches the list for a file which has the same file name
        """

        #
        # loop all entries until one is found
        #
        for k,v in entries:
            entry_file_name = urllib.url2pathname(v.get_file_name())

            #
            # remove path
            #
            position  = entry_file_name.rfind("\\")
            entry_file_name = entry_file_name[position+1:len(entry_file_name)]

            #print entry_file_name + " expected: " + file_name
            if entry_file_name == file_name:
                return v

        return None

    def __get_hash_sum(self, entry, directory):
        """
        calculates the hashsum of a file
        """

        #
        # get full path of entry
        #
        file_name = directory + "/" + urllib.url2pathname(entry.get_file_name())

        #
        # construct hashsum generator
        #
        generator = HashsumGenerator(file_name)

        #
        # calculate hashsum
        #
        hashsum = generator.calculate_sha1_hashsum()

        #
        # done
        #
        return hashsum


## end

