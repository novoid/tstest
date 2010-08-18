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

import unittest
from _Wolfgang.tscore.tagwrapper import TagWrapper


class Test(unittest.TestCase):

    TAGFILE_NAME = "../test/.TESTSUITE_TAGFILE"

    def setUp(self):
        pass

    def tearDown(self):
        pass


    def test_taghandler_write_and_get(self):
        
        tagHandler = self.get_fresh_taghandler()
        ## write new files to the tagfile, use one existing file, and a new one
        tagHandler.set_file("testfile.txt", ["test", "fest", "klest"])
        tagHandler.set_file("second_new_testfile", ["gnusmas", "fest", "DA", "tagger"])
        
        assert(tagHandler.exists_file("second_new_testfile"))


    def test_taghandler_popular(self):
        
        tagHandler = self.get_fresh_taghandler()
        
        tag_list = tagHandler.get_popular_tags(5)
        ## the tag with the name "DA" must be the most popular one
        assert(tag_list[0] == "MT")
        assert(tag_list[1] == "TUG")


    def test_configreader(self):
        pass
    
    def get_fresh_taghandler(self):
        ## at first create a new and clean tagfile in the current directory ...
        self.create_initial_tagfile()
        return TagWrapper(Test.TAGFILE_NAME)
    
    def create_initial_tagfile(self):
        """ helper method to create an empty tagfile
        for testing the taghandler
        """
        
        filename = Test.TAGFILE_NAME
        file = open(filename, "w")
        #file.writelines(content)
        
        id = "007"
        
        file.write("[store]\n")
        file.write("id=%s\n" % id)
        file.write("[files]\n")
        file.write("testfile.txt=\"tag,tagger,dagger,MT\"\n")
        file.write("masterthesis.tex=\"MT,TUG,tagstore\"\n")
        file.write("timesheet.csv=\"MT,TUG,controll\"\n")
        file.write("[categories]\n")
        file.close()
    

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
    
## end