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
from tscore.tagwrapper import TagWrapper


class Test(unittest.TestCase):

    TAGFILE_NAME = "../../test/.TESTSUITE_TAGFILE"

    def setUp(self):
        pass

    def tearDown(self):
        pass


    def test_tagwrapper_write_and_get(self):
        
        tag_wrapper = self.get_fresh_tagwrapper()
        ## write new files to the tagfile, use one existing file, and a new one
        tag_wrapper.set_file("testfile.txt", ["test", "fest", "klest"])
        tag_wrapper.set_file("second_new_testfile", ["gnusmas", "fest", "DA", "tagger"])
        assert(tag_wrapper.exists_file("second_new_testfile"))
        assert(not tag_wrapper.exists_file("second"))


    def test_tagwrapper_popular(self):
        
        tag_wrapper = self.get_fresh_tagwrapper()
        
        tag_list = tag_wrapper.get_popular_tags(5)
        ## the tag with the name "DA" must be the most popular one
        assert(tag_list[0] == "MT")
        assert(tag_list[1] == "TUG")

    def test_tagwrapper_rename_file(self):
        tag_wrapper = self.get_fresh_tagwrapper()
        tag_wrapper.rename_file("futukaka", "testfile.txt")
        tag_wrapper.rename_file("testfile.txt", "futukaka")
        
    def test_tagwrapper_recent(self):
        tag_wrapper = self.get_fresh_tagwrapper()
        
        tag_string = tag_wrapper.get_recent_files_tags(10)
        print tag_string
        

    def test_configreader(self):
        pass
    
    def get_fresh_tagwrapper(self):
        ## at first create a new and clean tagfile in the current directory ...
        self.create_initial_tagfile()
        return TagWrapper(Test.TAGFILE_NAME)
    
    def create_initial_tagfile(self):
        """ helper method to create an empty tagfile
        for testing the tagwrapper
        """
        
        filename = Test.TAGFILE_NAME
        file = open(filename, "w")
        #file.writelines(content)
        
        id = "007"
        
        file.write("[store]\n")
        file.write("id=%s\n" % id)
        file.write("[files]\n")

        file.write("testfile.txt/tags=\"tag,tagger,dagger,MT\"\n")
        file.write("testfile.txt/timestamp=\"2010-08-19 18:14:55\"\n")
        file.write("masterthesis.tex/tags=\"MT,TUG,tagstore\"\n")
        file.write("masterthesis.tex/timestamp=\"2010-08-19 18:14:50\"\n")
        file.write("timesheet.csv/tags=\"MT,TUG,controll\"\n")
        file.write("timesheet.csv/timestamp=\"2010-08-19 18:14:55\"\n")
        #file.write("testfile.txt/tags=\"tag,tagger,dagger,MT\"\n")
        #file.write("masterthesis.tex/tags=\"MT,TUG,tagstore\"\n")
        #file.write("timesheet.csv/tags=\"MT,TUG,controll\"\n")
        file.write("[categories]\n")
        file.close()
    

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
    
## end