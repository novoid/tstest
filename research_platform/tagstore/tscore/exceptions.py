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


class StoreInitError(Exception): pass
"""
custom error class to throw store specific error messages
this exception is thrown:
- during store initialization, if the expected store directory could not be found
"""

class StoreTaggingError(Exception): pass
"""
custom error class to throw store specific error messages during setting/updating/deleting tags
this exception is thrown:
- if a conflict occurs between tag names and link names (file names)
"""


## end