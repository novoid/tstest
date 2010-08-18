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
Copyright (c) 2009 John Schember
Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:
 
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
'''

from PyQt4.QtCore import QObject, SIGNAL
from PyQt4.QtGui import QLineEdit, QCompleter, QStringListModel
import logging
class TagStoreLineEdit(QLineEdit):
    '''
    
    '''

    def __init__(self, *args):
        QLineEdit.__init__(self, *args)
        
        # listen for text changes -> if signal is send -> call the text_changed() method 
        QObject.connect(self, SIGNAL("textChanged(QString)"), self.text_changed)
        
        # TODO: use the tag separator defined in the configfile
        self.tag_separator = ', '
    
    def text_changed(self, text):
        
        all_text = unicode(text)
        
        text = all_text[:self.cursorPosition()]

        prefix = text.split(self.tag_separator)[-1].strip()
        
        text_tags = []
        for t in all_text.split(self.tag_separator):
            t1 = unicode(t).strip()
            if t1 != "":
                text_tags.append(t)
        text_tags = list(set(text_tags))
        self.emit(SIGNAL("text_changed(PyQt_PyObject, PyQt_PyObject)"), text_tags, prefix)

    def complete_text(self, text):
        self.log = logging.getLogger('TagStoreLogger')
        self.log.info("logging from widget: %s" % id(self.log))
        cursor_pos = self.cursorPosition()
        before_text = unicode(self.text())[:cursor_pos]
        after_text = unicode(self.text())[cursor_pos:]
        prefix_len = len(before_text.split(self.tag_separator)[-1].strip())

        self.setText('%s%s, %s' % (before_text[:cursor_pos - prefix_len], text,
            after_text))
        self.setCursorPosition(cursor_pos - prefix_len + len(text) + 2)   

class TagStoreCompleter(QCompleter):
    
    def __init__(self, tagList, parent):
        QCompleter.__init__(self, tagList, parent)
        self.tagList = set(tagList)
        
    def update(self, text_tags, completion_prefix):
        ## remove the already used tags from the available list
        tags = list(self.tagList.difference(text_tags))

        model = QStringListModel(tags, self)
        self.setModel(model)
        self.setCompletionPrefix(completion_prefix)
        if completion_prefix.strip() != '':
            ## use the default completion algorithm
            self.complete()
    
    def set_taglist(self, tagList):
        self.tagList = set(tagList)
## end
