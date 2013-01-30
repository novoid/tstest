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
Created on Jul 31, 2010
'''



class TagStoreOsWrapper(object):
    '''
    This class provides an os-independend interface for all necessary tagStore operations 
    '''

    def __init__(self, params):
        '''
        Constructor
        '''
        
    def get_configuration(self):
        pass

    def get_broken_links(self):
        pass
    
    def get_empty_dirs(self):
        pass


    def delete_broken_symlinks(self):
        pass
    
    def delete_empty_directories(self):
        pass

    def remove_metadatafile(self):
        pass

    def remove_dead_links_and_empty_directories(self):
        pass

    def delete_item(self):
        pass
## end
        