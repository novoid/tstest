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


from tscore.enum import EFileEvent

class PendingChanges:
    def __init__(self):
        """
        constructor
        """
        self.__changes = dict()
        #TODO: implement a stack to handle changes in the correct order
        
#    def count(self):
#        """
#        returns the number of pending operations
#        """
#        return len(self.__changes.keys())

#    def pop(self):
#        """
#        returns and removes the first! file and event
#        """
#        if self.count() > 0:
#            file = self.__changes.keys()[0]
#            event = self.__changes[file]
#            del self.__changes[file]
#            return dict(file=file, event=event)
#        return None
        
    def files_to_string(self):
        """
        returns a comma-separated list of pending files
        """
        return ", ".join(self.__changes.keys())
        
    def add_file(self, file_name, event_enum):
        """
        adds/registers file with event
        """
        self.__changes[unicode(file_name)] = event_enum
    
    def remove_file(self, file_name):
        """
        removes file from instance
        """
        del self.__changes[unicode(file_name)]
    
    def get_all_files(self):
        """
        returns a list of all stored files
        """
        return self.__changes.keys()
    
    def get_added_files(self):
        """
        returns a list of all added files: EFileEvent = ADDED or ADDED_OR_RENAMED
        """
        files = []
        for file_name in self.__changes.keys():
            if self.__changes[file_name] == EFileEvent.ADDED or self.__changes[file_name] == EFileEvent.ADDED_OR_RENAMED:
                files.append(unicode(file_name))
        return files
    
    def get_removed_files(self):
        """
        returns a list of all removed files: EFileEvent = REMOVED or REMOVED_OR_RENAMED
        """
        files = []
        for file_name in self.__changes.keys():
            if self.__changes[file_name] == EFileEvent.REMOVED or self.__changes[file_name] == EFileEvent.REMOVED_OR_RENAMED:
                files.append(unicode(file_name))
        return files
    
    def get_files_by_event(self, event_enum):
        """
        returns a list of files filtered by given event name
        """
        files = []
        for file_name in self.__changes.keys():
            if self.__changes[file_name] == event_enum:
                files.append(unicode(file_name))
        return files
    
    
## end