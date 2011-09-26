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

from optparser import OptionParser
from syncmgr import SyncManager


def main(*args):

    #
    # construct new option parser
    #
    option_parser = OptionParser(sys.argv)

    #
    # check if all arguments were passed
    #
    if option_parser.get_source_directory() is None or \
       option_parser.get_target_directory() is None:
       print "Error: usage is tagsync.py " + OptionParser.ARGUMENT_SOURCE + "<source path> " + OptionParser.ARGUMENT_TARGET + "<target_path>"
       sys.exit(-1)

    #
    # construct sync manager
    #
    sync_mgr = SyncManager(option_parser)

    #
    # initialize the sync manager
    #
    sync_init = sync_mgr.initialize()
    if sync_init == False:
        print "Error: failed to initialize sync manager"
        sys.exit(-1)

    #
    # now sync the stores
    #
    print "Syncing tagstores..."

    result = sync_mgr.sync_tagstores()
    
    print "---------------------------"
    print "Success: " + repr(result)

    return 0

if __name__ == "__main__":
    main(*sys.argv)


## end

