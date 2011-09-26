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
this class is used to parse command line options for the tagstore sync utility
"""
class OptionParser:

    ARGUMENT_TARGET = "--target-path="
    ARGUMENT_SOURCE = "--source-path="

    def __init__(self, argv):
        """
        constructor
        """
        self.__argv = argv

    def get_target_directory(self):
        """
        returns the target directory
        """

        #
        # goes through all arguments and finds the target directory argument
        #
        for arg in self.__argv:
            if arg.startswith(self.ARGUMENT_TARGET):
                result = arg[len(self.ARGUMENT_SOURCE):len(arg)]
                return result
            
        return None

    def get_source_directory(self):
        """
        returns the source directory
        """

        #
        # goes through all arguments and finds the source directory argument
        #
        for arg in self.__argv:
            if arg.startswith(self.ARGUMENT_SOURCE):
                result = arg[len(self.ARGUMENT_SOURCE):len(arg)]
                return result

        return None


## end

