'''
Created on Aug 13, 2010

@author: chris
'''
import unittest
from tscommon.TagStoreCommon import TagStoreTagHandler


class Test(unittest.TestCase):

    TAGFILE_NAME = "../../testdata/.TESTSUITE_TAGFILE"

    def setUp(self):
        pass

    def tearDown(self):
        pass


    def test_taghandler_write_and_get(self):
        
        tagHandler = self.get_fresh_taghandler()
        ## write a new file to the tagfile, use one existing tag, and one new tag
        tagHandler.handle_new_file("new_testfile", "test", "descriptive")
        tagHandler.handle_new_file("new_testfile", "NEW_TAG", "descriptive")
        
        tag = tagHandler._exists_tag("NEW_TAG")
        
        ## configparser writes the variables in lower case
        ## so compare with "new_tag"
        assert(tag == "new_tag")
        

    def test_taghandler_popular(self):
        
        tagHandler = self.get_fresh_taghandler()
        
        tagList = tagHandler.get_popular_tags()
        tagObject = tagList[0]
        
        ## the tag with the name "test" must be the most popular one
        assert(tagObject.tagName == "test")
        
        tagObject = tagList[1]
        assert(tagObject.tagName == "chest")
        

    def test_configreader(self):
        pass
    
    def get_fresh_taghandler(self):
        ## at first create a new and clean tagfile in the current directory ...
        self.create_initial_tagfile()
        return TagStoreTagHandler(Test.TAGFILE_NAME)
    
    def create_initial_tagfile(self):
        ''' helper method to create an empty tagfile
        for testing the taghandler
        '''
        
        filename = Test.TAGFILE_NAME
        file = open(filename, "w")
        #file.writelines(content)
        file.write("[descriptive]\n")
        file.write("test=testfile.txt,anothertestfile.txt,thethirdtestfile\n")
        file.write("guest=testfile.txt\n")
        file.write("chest=testfile.txt,secondtestfile\n")
        file.close()
    

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()