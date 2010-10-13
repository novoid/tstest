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
'''
Created on Oct 13, 2010
'''
import sys
from PyQt4 import QtCore

class VocabularyWrapper(QtCore.QObject):
    """
    wrapper class for reading and writing the line based vocabulary dict
    """


    def __init__(self, file_path):
        QtCore.QObject.__init__(self)
        
        self.__watcher = QtCore.QFileSystemWatcher(self)
        self.__watcher.addPath(file_path)
        self.__watcher.connect(self.__watcher,QtCore.SIGNAL("fileChanged(QString)"), self.__file_changed)
    
        self.__file_path = file_path
    
    def __file_changed(self):
        """
        event handler: called when file was changed
        """
        self.emit(QtCore.SIGNAL("changed()"))
    
    def get_vocabulary(self):
        """
        get all vocabulary stored in the vocabulary file
        returns a list
        """
        voc_list = []
        
        with open(self.__file_path, "rw") as voc_file:
            for line in voc_file:
                voc_list.append(unicode(line.strip("\n")))
        
        return voc_list
    
    def add_vocable(self, single_vocable):
        """
        add a single word to the vocabulary file
        """
        ## unsing the "with" keyword closes the file automatically 
        with open(self.__file_path, "rw") as voc_file:
            voc_file.write("%s\n" % single_vocable)

    def add_vocabulary(self, vocable_list):
        """
        add a single word to the vocabulary file
        """
        with open(self.__file_path, "rw") as voc_file:
            for vocable in vocable_list:
                voc_file.write("%s\n" % vocable)
        
    def replace_vocabulary(self, vocabule_list):
        """
        replace the current vocabulary and add the provided list of new vocabulary 
        """
        pass
## end