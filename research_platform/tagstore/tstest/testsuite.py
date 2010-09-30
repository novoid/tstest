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
from tscore.enums import EFileEvent, EFileType
from tscore.pendingchanges import PendingChanges
from tscore.configwrapper import ConfigWrapper

class Test(unittest.TestCase):

    TAGFILE_NAME = "../../test/.TESTSUITE_TAGFILE"

    def setUp(self):
        pass

    def tearDown(self):
        pass


    def test_tagwrapper_file_exists(self):
        tag_wrapper = self.get_fresh_tagwrapper()
        assert(tag_wrapper.file_exists("testfile.txt"))
        
    def test_tagwrapper_write_and_get(self):
        
        tag_wrapper = self.get_fresh_tagwrapper()
        ## write new files to the tagfile, use one existing file, and a new one
        tag_wrapper.set_file("testfile.txt", ["test", "fest", "klest"])
        tag_wrapper.set_file("second_new_testfile", ["gnusmas", "fest", "DA", "tagger"])
        assert(tag_wrapper.file_exists("second_new_testfile"))
        assert(not tag_wrapper.file_exists("second"))


    def test_tagwrapper_popular(self):
        
        tag_wrapper = self.get_fresh_tagwrapper()
        
        tag_list = tag_wrapper.get_popular_tags(5)
        ## the tag with the name "DA" must be the most popular one
        assert(tag_list[0] == "MT")
        assert(tag_list[1] == "TUG")

    def test_tagwrapper_get_all_tags(self):
        tag_wrapper = self.get_fresh_tagwrapper()
        all_tags_list = tag_wrapper.get_all_tags()
        ## the test-tagfile has got 7 different tags
        assert(len(all_tags_list) == 7)
        

    def test_tagwrapper_remove_file_and_tags(self):
        tag_wrapper = self.get_fresh_tagwrapper()
        
        tag_wrapper.remove_file("testfile.txt")
        assert(not tag_wrapper.file_exists("testfile.txt"))
        
        tag_wrapper.remove_tag("TUG")
        assert("TUG" not in tag_wrapper.get_all_tags())
        
        
    def test_tagwrapper_rename_file_and_tag(self):
        ## rename_file method has been removed!
        ## TODO: reimplement the method or remove testcase AND substitute with a new one
        
        #tag_wrapper = self.get_fresh_tagwrapper()

        #tag_wrapper.rename_file("testfile.txt", "ricewind")
        #assert(tag_wrapper.file_exists("ricewind"))
        
        #tag_wrapper.rename_file("ricewind", "mort")
        #assert(tag_wrapper.file_exists("mort"))
        #assert(not tag_wrapper.file_exists("ricewind"))

        #tag_wrapper.rename_tag("dagger", "sword")
        #assert("sword" in tag_wrapper.get_all_tags())
        #assert("dagger" not in tag_wrapper.get_all_tags())
        pass
        
    def test_tagwrapper_recent(self):
        tag_wrapper = self.get_fresh_tagwrapper()
        
        tag_list = tag_wrapper.get_recent_files_tags(5)
        assert(len(tag_list) == 7)
        
        tag_list = tag_wrapper.get_recent_files_tags(1)
        assert("tagstore" not in tag_list)
        assert("TUG" in tag_list)
        
        tag_list = tag_wrapper.get_recent_tags(4)
        
        assert(len(tag_list) == 4)
        assert("dagger" not in tag_list)
        

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
        file.write("store_id=%s\n" % id)
        file.write("[files]\n")

        file.write("testfile.txt/tags=\"tag,tagger,dagger,MT\"\n")
        file.write("testfile.txt/timestamp=\"2010-08-20 18:14:55\"\n")
        
        file.write("masterthesis.tex/tags=\"MT,TUG,tagstore\"\n")
        file.write("masterthesis.tex/timestamp=\"2010-08-21 18:14:50\"\n")
        
        file.write("timesheet.csv/tags=\"MT,TUG,controll\"\n")
        file.write("timesheet.csv/timestamp=\"2010-08-22 18:14:55\"\n")
        
        file.write("[categories]\n")
        file.close()
    
    def test_pending_changes_append_remove(self):
        
        test = PendingChanges()
        test.register("new.txt", EFileType.FILE, EFileEvent.ADDED)
        test.register("new2.txt", EFileType.DIRECTORY, EFileEvent.ADDED_OR_RENAMED)
        test.register("new3.txt", EFileType.DIRECTORY, EFileEvent.ADDED_OR_RENAMED)
        
        assert("new.txt" == test.get_first()["file"])
        assert("new.txt" == test.get_first(True)["file"])
        assert("new2.txt" == test.get_first(True)["file"])
        
        test.register("new4.txt", EFileType.FILE, EFileEvent.ADDED)
        test.remove("new2.txt")
        test.remove("new3.txt")
        assert("new4.txt" == test.get_first(True)["file"])
        assert(None == test.get_first())
        
    def test_pending_changes_get_names(self):
        test = PendingChanges()
        test.register("new.txt", EFileType.FILE, EFileEvent.ADDED)
        test.register("new2.txt", EFileType.DIRECTORY, EFileEvent.ADDED_OR_RENAMED)
        test.register("new3.txt", EFileType.FILE, EFileEvent.REMOVED)
        test.remove("new2.txt")
        test.register("new4.txt", EFileType.FILE, EFileEvent.REMOVED)
        test.register("new3.txt", EFileType.FILE, EFileEvent.ADDED_OR_RENAMED)
        
        assert(["new.txt","new3.txt", "new4.txt"] == test.get_names())
        
        assert("new.txt" == test.get_added_names()[0])
        assert("new3.txt" == test.get_added_names()[1])
        assert("new4.txt" == test.get_removed_names()[0])

    def test_pending_changes_edit_item(self):
        test = PendingChanges()
        test.register("new.txt", EFileType.FILE, EFileEvent.ADDED)
        test.register("new2.txt", EFileType.DIRECTORY, EFileEvent.ADDED_OR_RENAMED)
        test.register("new3.txt", EFileType.FILE, EFileEvent.REMOVED)
        
        test.edit("new2.txt", "new4.txt")
        assert(["new.txt","new4.txt", "new3.txt"] == test.get_names())

    def test_configwrapper(self):
        #test = ConfigWrapper()
        pass


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
    
## end