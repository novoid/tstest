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

import sys
from PyQt4 import QtCore, QtGui
from tsgui.taggingdialog import TaggingDialog
from tscore.configwrapper import ConfigWrapper
from tscore.store import Store


## path to the config file
CONFIG_PATH = "settings/tagstore.cfg"
    
class Tagstore():

    def __init__(self, parent=None):

        ## global settings/defaults (only used if reading config file failed or invalid!)
        self.STORE_CONFIG_DIR = ".tagstore"
        self.STORE_CONFIG_EXT = "tgs"
        self.TAG_SEPERATOR = ", "
        self.STORES = []
        
        ## init configurations
        self.configfile = None
        self.__initConfigurations()
        
        ## init UI
        self.__taggingDialog = TaggingDialog()
        self.__updateTagList()
        
        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("tagTextEdited(QString)"), self.tagTextEdited)
        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("tagCompletionActivated(QString)"), self.tagCompletionActivated)
        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("tagPreferenceActivated(QString)"), self.tagPreferenceActivated)
        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("categoryTextEdited(QString)"), self.categoryTextEdited)
        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("categoryCompletionActivated(QString)"), self.categoryCompletionActivated)
        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("categoryPreferenceActivated(QString)"), self.categoryPreferenceActivated)

        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("confirmButtonPressed()"), self.confirmButtonPressed)
        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("cancelButtonPressed()"), self.cancelButtonPressed)
        self.__taggingDialog.connect(self.__taggingDialog, QtCore.SIGNAL("cancelAllButtonPressed()"), self.cancelAllButtonPressed)

#test getter/setter        
        self.__taggingDialog.setStoreLabelText("store name")
        self.__taggingDialog.setFileLabelText("current file")
        self.__taggingDialog.show()
#test end

    def __initConfigurations(self):
        ## reload config file
        self.configfile = ConfigWrapper(CONFIG_PATH)
        self.configfile.connect(self.configfile, QtCore.SIGNAL("changed()"), self.__initConfigurations)
        tagSeperator = self.configfile.getTagSeperator()
        if tagSeperator.strip() != "":
            self.TAG_SEPERATOR = tagSeperator
            
        configDir = self.configfile.getStoreConfigDirectory()
        if configDir != "":
            self.STORE_CONFIG_DIR = configDir
        configExt = self.configfile.getStoreConfigExtention()
        if configExt != "":
            self.STORE_CONFIG_EXT = configExt
            
        configStoreItems = self.configfile.getStores()
        ## reload stores
        for store in self.STORES:
            store.dispose()
        self.STORES = []
        for storeItem in configStoreItems:
            store = Store(storeItem["key"],storeItem["path"], self.STORE_CONFIG_DIR, self.STORE_CONFIG_EXT)
            store.connect(store, QtCore.SIGNAL("removed(PyQt_PyObject)"), self.storeRemoved)
            store.connect(store, QtCore.SIGNAL("renamed(PyQt_PyObject, QString)"), self.storeRenamed)
            self.STORES.append(store)
        #TODO: raise an error if stores have the same name

    def storeRemoved(self, store):
        self.configfile.removeStore(store.getConfigKey())
        ## __initConfiguration is called due to config file changes
        
    def storeRenamed(self, store, newPath):
        store.rename(newPath)
        self.configfile.renameStore(store.getConfigKey(), newPath)
        ## __initConfiguration is called due to config file changes
        
    def tagTextEdited(self, text):
        self.__updateTagList()
        cursorLeftText = unicode(text)[:self.__taggingDialog.tagCursorPosition()]
        lookupPrefix = cursorLeftText.split(self.TAG_SEPERATOR)[-1].strip()
        self.__taggingDialog.setTagCompletionPrefix(lookupPrefix)
        
    def tagCompletionActivated(self, text):
        self.__updateTagList()
        cursorPos = self.__taggingDialog.tagCursorPosition()
        currentText = self.__taggingDialog.tagText()
        cursorLeftText = unicode(currentText)[:cursorPos]
        cursorRightText = unicode(currentText)[cursorPos:]
        if cursorRightText.strip()[:len(self.TAG_SEPERATOR)] != self.TAG_SEPERATOR:
            cursorRightText = self.TAG_SEPERATOR + cursorRightText
        prefixLength = len(cursorLeftText.split(self.TAG_SEPERATOR)[-1].strip())

        self.__taggingDialog.setTagText(cursorLeftText[:cursorPos - prefixLength] + text + cursorRightText)
        self.__taggingDialog.setTagCursorPosition(cursorPos - prefixLength + len(text) + len(self.TAG_SEPERATOR))   
        
    def tagPreferenceActivated(self, text):
        self.__updateTagList()
        currentText = self.__taggingDialog.tagText()
        if currentText.strip() == "":
            self.__taggingDialog.setTagText(text + self.TAG_SEPERATOR)
        elif currentText.split(self.TAG_SEPERATOR)[-1].strip() == "":
            self.__taggingDialog.setTagText(currentText + text + self.TAG_SEPERATOR)
        else:
            self.__taggingDialog.setTagText(currentText + self.TAG_SEPERATOR + text + self.TAG_SEPERATOR)
        
    def __updateTagList(self):
        usedTags = self.__taggingDialog.tagText().split(self.TAG_SEPERATOR)
        
        #zentales tagDictionary: einlesen der config, einlesen
        self.__taggingDialog.setTagLookupList(['aa','abb','xaa b','cabb','daa','abb','abc'])
        self.__taggingDialog.setTagPreferences(['f','g','h'])
        
        
    def categoryTextEdited(self, text):
        pass
        
    def categoryCompletionActivated(self, text):
        pass
        
    def categoryPreferenceActivated(self, text):
        pass
                               
    def confirmButtonPressed(self):
        print "confirm"
 
    def cancelButtonPressed(self):
        print "cancel"
        
    def cancelAllButtonPressed(self):
        print "cancelAll"
        
        
if __name__ == '__main__':  
  
    tagstore = QtGui.QApplication(sys.argv)
    tagWidget = Tagstore()
    sys.exit(tagstore.exec_())


## end    