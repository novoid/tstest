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

import hashlib

"""
this class is used to calculate the SHA-1 hashsum of a file
"""
class HashsumGenerator:

    def __init__(self, filename):
        """
        constructor
        """

        #
        # store file name
        #
        self.__filename = filename

    def calculate_sha1_hashsum(self):
        """
        calculates the sha1 hashsum of the contents of the file
        """

        #
        # open file handle
        #
        file_handle = open(self.__filename, 'rb')

        if file_handle == 0:
            return ""

        #
        # instantiate hashlib object
        #
        message_digest = hashlib.sha1()

        #
        # read the file into the hashlib object
        #
        message_digest.update(file_handle.read())

        #
        # calculate hex digest
        #
        result = message_digest.hexdigest()

        #
        # return result
        #
        return result


## end

