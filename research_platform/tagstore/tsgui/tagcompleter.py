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

from PyQt4 import QtCore, QtGui
from PyQt4.QtCore import Qt, QObject, SIGNAL
from PyQt4.QtGui import QLineEdit, QCompleter, QStringListModel, QWidget
import logging

class TagCompleterWidget(QWidget):
    """
    widget in lineEdit-style with integrated qcompleter
    """ 
#    def __init__(self, tag_completion_list, parent=None):
    def __init__(self, tag_list=None, parent=None, separator=", "):
        
        QWidget.__init__(self, parent)
        
        self.__tag_separator = separator
        self.__tag_list = tag_list
        
        self.__tag_line = QLineEdit(self)
        
        self.__completer = QCompleter(self.__tag_list, self);    
        self.__completer.setCaseSensitivity(Qt.CaseInsensitive)
        self.__completer.setWidget(self.__tag_line)
        
        
        self.connect(self.__tag_line, SIGNAL("textEdited(QString)"), self.__text_changed)
        self.connect(self.__completer, SIGNAL("activated(QString)"), self.__text_activated)

    def __text_changed(self, text):
        all_text = unicode(text)
        text = all_text[:self.__tag_line.cursorPosition()]

        prefix = text.split(self.__tag_separator)[-1].strip()
        
        text_tags = []
        for t in all_text.split(self.__tag_separator):
            t1 = unicode(t).strip()
            if t1 != "":
                text_tags.append(t)
        text_tags = list(set(text_tags))
        self.__update_completer(text_tags, prefix)
    
    def __update_completer(self, text_tags, completion_prefix):
        tags = list(set(self.__tag_list).difference(text_tags))

        model = QStringListModel(tags, self)
        self.__completer.setModel(model)
        self.__completer.setCompletionPrefix(completion_prefix)
        if completion_prefix.strip() != '':
            ## use the default completion algorithm
            self.__completer.complete()
    
    def __text_activated(self, text):
        cursor_pos = self.__tag_line.cursorPosition()
        before_text = unicode(self.__tag_line.text())[:cursor_pos]
        after_text = unicode(self.__tag_line.text())[cursor_pos:]
        prefix_len = len(before_text.split(self.__tag_separator)[-1].strip())

        self.__tag_line.setText('%s%s, %s' % (before_text[:cursor_pos - prefix_len], text,
            after_text))
        self.__tag_line.setCursorPosition(cursor_pos - prefix_len + len(text) + 2)

    def get_tag_list(self):
        tag_string = self.__tag_line.text()
        result = []
        for tag in tag_string.split(","):
            result.append(str(tag.trimmed()))
        return result
    
    def get_tag_line(self):
        return self.__tag_line
    
    def get_completer(self):
        return self.__completer
    
    def set_enabled(self, enable):
        self.__tag_line.setEnabled(enable)
        
    def set_text(self, text):
        self.__tag_line.setText(text)
    
    def set_tag_completion_list(self, tag_list):
        self.__tag_list = tag_list
        self.__completer.setModel(QtGui.QStringListModel(QtCore.QStringList(tag_list)))
    
## end
