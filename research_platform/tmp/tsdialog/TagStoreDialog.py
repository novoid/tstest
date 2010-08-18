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
Created on Aug 5, 2010
'''

import sys
import gettext
import logging
from PyQt4.QtCore import QObject, SIGNAL, Qt
from PyQt4.QtGui import QTextBrowser, QDialog, QLabel, QVBoxLayout, QMessageBox
from PyQt4.Qt import QApplication
from tswidget.TagStoreWidget import TagStoreCompleter, TagStoreLineEdit
from tscommon.TagStoreCommon import TagStoreTagHandler
gettext.install("tagstore", "/Users/chris/Documents/workspace/tasgStore_SVN/locale/")

class TagStoreDialog(QDialog):
    '''
    class to build a simple dialog for testing purpose
    '''

    def __init__(self, fileName, controller, parent = None):
        super(TagStoreDialog, self).__init__(parent)

        self.set_controller(controller)

        self.tagLine = TagStoreLineEdit()
        self.tagLine.selectAll()
        
        # initially use an empty list
        wList = ""

        self.completer = TagStoreCompleter(wList, self.tagLine);
        self.completer.setCaseSensitivity(Qt.CaseInsensitive)
        QObject.connect(self.tagLine, SIGNAL("text_changed(PyQt_PyObject, PyQt_PyObject)"), self.completer.update)
        QObject.connect(self.completer, SIGNAL("activated(QString)"), self.tagLine.complete_text)
        
        fileLabel = QLabel(_("Add tag to new file: %s" % (fileName)))

        ## prepare a label for presenting the recently used tags 
        recentLabel = QLabel(_("Recently used tags:"))
                
        self.recentValueLabel = QLabel("")
        
        ## prepare a label for presenting the mot popular tags incl. the usage count 
        popularLabel = QLabel(_("Most popular Tags:"))
        
        self.popularValueLabel = QLabel("")
        
        
        self.completer.setWidget(self.tagLine)

        #self.tagLine.setCompleter(completer);

        layout = QVBoxLayout()
        layout.addWidget(fileLabel)
        layout.addWidget(recentLabel)
        layout.addWidget(self.recentValueLabel)
        layout.addWidget(popularLabel)
        layout.addWidget(self.popularValueLabel)
        layout.addWidget(self.tagLine)
        
        self.setLayout(layout)

        self.tagLine.setFocus()
        #self.connect(self.tagLine, SIGNAL("returnPressed()"), self.updateUi)
        self.setWindowTitle(_("Tagstore Dialog"))
        
    def set_controller(self, controller):
        self.__controller = controller
        
    def get_controller(self):
        return self.__controller    
    
    def set_recent_tags(self, tagString):
        self.recentValueLabel.setText(tagString)
        
    def set_popular_tags(self, tagString):
        self.popularValueLabel.setText(tagString)
        
    def closeEvent(self, event):
        ''' reimplementation of the closeEvent method
        check if there are tags provided by the user before quitting the dialog 
        '''
        if self.tagLine.text() == "":
            if not self.okToClose():
                event.ignore()
        else:
            self.handle_tag_writing()
        
    def okToClose(self):
        reply = QMessageBox.question(self, _("No tag provided"), _("Sure you want to close the dialog?"),
                                     QMessageBox.Yes|QMessageBox.No)
        
        if reply == QMessageBox.Yes:
            return True
        
        return False
    
    def handle_tag_writing(self):
        ''' do the necessary actions 
        to write tags of the textfield to
        '''
        self.get_controller().write_tags(self.tagLine.text())
        
    def set_taglist(self, newTagList):
        self.completer.set_taglist(newTagList)

class TagStoreDialogController(object):
    ''' class for doing the logic of the dialog gui
    load/write tags ...
    ''' 
    
    def __init__(self, tagProvider):
        self.log = logging.getLogger('TagStoreLogger')
#        self.tagProvider = TagStoreTagHandler(sys.argv)
        self.tagProvider = tagProvider
        
    def show_dialog(self, fileName):
        ''' provide the dialog gui with new tag information '''
        self.fileName = fileName
        app = QApplication(sys.argv)
        dialog = TagStoreDialog(fileName, self)
        tagList = self.tagProvider.get_all_tags()
        
        dialog.set_taglist(tagList)
        dialog.set_popular_tags(self.get_popular_tags())
        dialog.set_recent_tags(self.get_recent_tags())
        
        dialog.show()
        app.exec_()
        #sys.exit(app.exec_())
        
    def get_recent_tags(self):
        recentString = ""
        
        for tag in self.tagProvider.get_recent_tags():
            if len(recentString) == 0:
                recentString = tag
            else:
                recentString = recentString + ", " + tag
        return recentString
    
    def get_popular_tags(self):
        tagString = ""
        for tagObject in self.tagProvider.get_popular_tags():
            if len(tagString) == 0:
                tagString = tagObject.__str__()
            else:
                tagString = tagString + ", " + tagObject.__str__()
        return tagString
    
    def write_tags(self, tagString):
        ''' use the taghandler to write these tags to the tagfile
        also inform the TagStoreLogic to do the link-handling
        '''
        self.log.info("doing the tag-writing magic ...write tags: %s" % tagString)
        for tag in tagString.split(","):
            self.tagProvider.handle_new_file(self.fileName, str(tag).strip())
## end