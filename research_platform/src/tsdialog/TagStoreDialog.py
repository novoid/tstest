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

from PyQt4.QtCore import QObject, SIGNAL, Qt
from PyQt4.QtGui import QTextBrowser, QDialog, QLabel, QVBoxLayout
from PyQt4.Qt import QApplication
from tswidget.TagStoreWidget import TagStoreCompleter, TagStoreLineEdit
from tscommon.TagStoreCommon import TagStoreTagHandler
gettext.install("tagstore", "/Users/chris/Documents/workspace/tasgStore_SVN/locale/")

class TagStoreDialog(QDialog):
    '''
    class to build a simple dialog for testing purpose
    '''

    def __init__(self, fileName, parent = None):
        super(TagStoreDialog, self).__init__(parent)

        self.tag_line = TagStoreLineEdit(_("Enter your tags"))
        self.tag_line.selectAll()
        
        ## get the tags from the tagfile
        tagProvider = TagStoreTagHandler(sys.argv)
        wList = tagProvider.get_all_tags()

        completer = TagStoreCompleter(wList, self.tag_line);
        completer.setCaseSensitivity(Qt.CaseInsensitive)
        QObject.connect(self.tag_line, SIGNAL("text_changed(PyQt_PyObject, PyQt_PyObject)"), completer.update)
        QObject.connect(completer, SIGNAL("activated(QString)"), self.tag_line.complete_text)
        
        fileLabel = QLabel(_("Add tag to new file: %s" % (fileName)))

        ## prepare a label for presenting the recently used tags 
        recentLabel = QLabel(_("Recently used tags:"))
        recentString = ""
        for tag in tagProvider.get_recent_tags():
            if len(recentString) == 0:
                recentString = tag
            else:
                recentString = recentString + ", " + tag
                
        recentValueLabel = QLabel(recentString)
        
        ## prepare a label for presenting the mot popular tags incl. the usage count 
        popularLabel = QLabel(_("Most popular Tags:"))
        tagString = ""
        for tagObject in tagProvider.get_popular_tags():
            if len(tagString) == 0:
                tagString = tagObject.__str__()
            else:
                tagString = tagString + ", " + tagObject.__str__()
        popularValueLabel = QLabel(tagString)
        
        
        completer.setWidget(self.tag_line)

        #self.tag_line.setCompleter(completer);

        layout = QVBoxLayout()
        layout.addWidget(fileLabel)
        layout.addWidget(recentLabel)
        layout.addWidget(recentValueLabel)
        layout.addWidget(popularLabel)
        layout.addWidget(popularValueLabel)
        layout.addWidget(self.tag_line)
        
        self.setLayout(layout)

        self.tag_line.setFocus()
        #self.connect(self.tag_line, SIGNAL("returnPressed()"), self.updateUi)
        self.setWindowTitle(_("Hello PyQt"))
        
## end