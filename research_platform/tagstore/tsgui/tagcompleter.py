#!/usr/bin/env python
from tscore.specialcharhelper import SpecialCharHelper

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
import time
import logging
import re
from PyQt4 import QtCore, QtGui
from PyQt4.QtCore import Qt, QObject, SIGNAL
from PyQt4.QtGui import QLineEdit, QCompleter, QStringListModel, QWidget
from tscore.tsconstants import TsConstants

class TagCompleterWidget(QObject):
    """
    widget in lineEdit-style with integrated qcompleter
    """ 
    def __init__(self, max_tags, tag_list=None, parent=None, separator=",", show_datestamp=False):
        
        QWidget.__init__(self, parent)
        
        self.__max_tags = max_tags
        self.__tag_separator = separator
        self.__tag_list = tag_list
        self.__parent = parent
        self.__tag_line = QLineEdit(self.__parent)
        self.__show_datestamp = show_datestamp
        self.__datestamp_format = TsConstants.DATESTAMP_FORMAT_DAY
        
        ## flag, if the line should be checked of emptiness
        self.__check_not_empty = False
        self.__check_tag_limit = False
        
        self.__restricted_vocabulary = False
        
        ## the latest activated suggestion 
        self.__activated_text = None
        
        self.__completer = QCompleter(self.__tag_list, self);    
        self.__completer.setCaseSensitivity(Qt.CaseInsensitive)
        self.__completer.setWidget(self.__tag_line)
        
        self.__handle_datestamp()
        
        self.connect(self.__tag_line, SIGNAL("textChanged(QString)"), self.__text_changed)
        self.connect(self.__completer, SIGNAL("activated(QString)"), self.__text_activated)

    def __handle_datestamp(self):
        """
        if the show_datestamp flag is set to True, provide an automatic datestamp on the tagline 
        """
        if self.__show_datestamp:
            self.__tag_line.clear()
            self.__tag_line.setText(time.strftime(self.__datestamp_format))

    def set_datestamp_format(self, format):
        self.__datestamp_format = format
        self.__handle_datestamp()
        
    def show_datestamp(self, show):
        self.__show_datestamp = show
        self.__handle_datestamp()

    def clear_line(self):
        """
        clear the tagline ... 
        if auto datestamp is set to "on" a fresh stamp will be placed into the tagline 
        """
        self.__tag_line.clear()
        if self.__show_datestamp:
            self.__handle_datestamp()
    def set_check_not_empty(self, check_necessary):
        """
        set this to True, if there should be sent a signal that indicates 
        that the tagline is not empty anymore 
        """
        self.__check_not_empty = True
    
    def set_restricted_vocabulary(self, is_restricted):
        """
        use True/False to turn the restricted function on/off
        """
        self.__restricted_vocabulary = is_restricted
    
    def select_line(self):
        """
        select the tagline ... 
        """
        self.__tag_line.selectAll()
        self.__tag_line.setFocus(QtCore.Qt.OtherFocusReason)

    def __text_changed(self, text):
        all_text = unicode(text)
        
        if self.__check_not_empty:
            if all_text is not None and all_text != "":
                self.emit(QtCore.SIGNAL("line_empty"), False)
            else:
                self.emit(QtCore.SIGNAL("line_empty"), True)
        
        text = all_text[:self.__tag_line.cursorPosition()]
        
        ## remove whitespace and filter out duplicates by using a set
        tag_set = set([])
        for tag in all_text.split(self.__tag_separator):
            strip_tag = tag.strip()
            if strip_tag != "":
                tag_set.add(strip_tag)
        
        ## do not proceed if the max tag count is reached
        if len(tag_set) > self.__max_tags:
            self.emit(QtCore.SIGNAL("tag_limit_reached"), True)
            self.__check_tag_limit = True
            return
        else:
            if self.__check_tag_limit:
                self.emit(QtCore.SIGNAL("tag_limit_reached"), False)
                self.__check_tag_limit = False
        
        prefix = text.split(self.__tag_separator)[-1].strip()
        self.__update_completer(tag_set, prefix)
    
    def __update_completer(self, tag_set, completion_prefix):
        if self.__tag_list is None:
            return
        #tags = list(set(self.__tag_list).difference(tag_set))
        tags = list(self.__tag_list)

        model = QStringListModel(tags, self)
        self.__completer.setModel(model)
        self.__completer.setCompletionPrefix(completion_prefix)
        
        if self.__restricted_vocabulary:
            self.__check_vocabulary(tag_set, completion_prefix)
            
        if completion_prefix.strip() != '':
            ## use the default completion algorithm
            self.__completer.complete()
    
    def __check_vocabulary(self, tag_set, completion_prefix):
        
        not_allowed_tags_count = 0
        no_completion_found = False
        stripped_text = str(self.__tag_line.text()).strip()
        ##when a tag separator is on the last position, there should have been entered a new tag
        ##check this tag for its correctness
        if len(stripped_text) > 0:
            ##check if all written tags are allowed
            for tag in tag_set:
                if not SpecialCharHelper.is_datestamp(tag) and tag != "":
                    if unicode(tag) not in self.__tag_list:
                        not_allowed_tags_count += 1
                            
        if self.__restricted_vocabulary and self.__completer.completionCount() == 0:
            ## additionally check if the prefix equals a tag from the suggestion list
            ## this has to be done, because we do not get a completionCount > 0 if the prefix equals a given tag 
            #if completion_prefix not in self.__tag_list:
            if completion_prefix is not None and len(completion_prefix) > 0 and completion_prefix.strip() != "":
                ## just send the signal if the tag is no datestamp
                if not SpecialCharHelper.is_datestamp(completion_prefix):
                    no_completion_found = True

        ## there are tags which are not in the allowed tag_list 
        if not_allowed_tags_count > 0:
            ## it could be the case, that the user is still typing an allowed tag
            ## so check, if the completer has a possible completion
            ## if not -> send the signal 
            if no_completion_found:
                self.emit(QtCore.SIGNAL("no_completion_found"), True)
        ## everytime there is no completion found, emit the signal
        elif no_completion_found:
            self.emit(QtCore.SIGNAL("no_completion_found"), True)
        ## in this case everything is fine
        else:
            self.emit(QtCore.SIGNAL("no_completion_found"), False)
    
    def __text_activated(self, text):
        
        self.__activated_text = text
        
        cursor_pos = self.__tag_line.cursorPosition()
        before_text = unicode(self.__tag_line.text())[:cursor_pos]
        #after_text = unicode(self.__tag_line.text())[cursor_pos:]
        prefix_len = len(before_text.split(self.__tag_separator)[-1].strip())

        self.__tag_line.setText("%s%s" % (before_text[:cursor_pos - prefix_len], text))
        self.__tag_line.setCursorPosition(cursor_pos - prefix_len + len(text) + 2)
        self.emit(QtCore.SIGNAL("activated"))

    def get_tag_list(self):
        tag_string = unicode(self.__tag_line.text())
        result = set([])
        tag_list = tag_string.split(self.__tag_separator)
        for tag in tag_list:
            strip_tag = tag.strip()
            if strip_tag != "":
                result.add(strip_tag)
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
        
    def get_tag_completion_list(self):
        return self.__tag_list

    def is_empty(self):
        if self.__tag_line.text() == "":
            return True
        else:
            return False

    #def set_size(self, qrect):
    #    self.setGeometry(qrect)
    #    self.__tag_line.setGeometry(qrect)

class TsListWidget(QtGui.QListWidget):
    
    def __init__(self, parent=None):
        super(TsListWidget, self).__init__(parent)
        
    def keyPressEvent(self, event):
        ## throw a custom signal, when enter (on the keypad) or return has been hit
        
        key = event.key()
        
        if key == QtCore.Qt.Key_Return or key == QtCore.Qt.Key_Enter:
            self.emit(QtCore.SIGNAL("return_pressed"))
        ## pass the signal to the normal parent chain
        QtGui.QListWidget.keyPressEvent(self, event)

class ComboboxCompleter(QtGui.QWidget):

    __pyqtSignals__ = ("text_edited(QString)", 
                       "completion_activated(QString)", 
                       "preference_activated(QString)")

    def __init__(self, parent=None):
        """
        Constructor
        """
        QtGui.QWidget.__init__(self, parent)
        
        self.__combobox = QtGui.QComboBox(parent)
        self.__combobox.connect(self.__combobox, QtCore.SIGNAL("activated(QString)"), self, QtCore.SIGNAL("preference_activated(QString)"))
        self.__lineedit = QtGui.QLineEdit(parent)
        self.__lineedit.setStyleSheet("QLineEdit {border: none}")
        self.__completer = QtGui.QCompleter()
        self.__completer.setWidget(self.__lineedit)
        self.__completer.setCaseSensitivity(QtCore.Qt.CaseInsensitive)
        self.__completer.connect(self.__completer, QtCore.SIGNAL("activated(QString)"), self, QtCore.SIGNAL("completion_activated(QString)"))
        self.__lineedit.connect(self.__lineedit, QtCore.SIGNAL("textEdited(QString)"), self, QtCore.SIGNAL("text_edited(QString)"))
        QtCore.QMetaObject.connectSlotsByName(parent)
    
    def set_geometry(self, q_rect):
        """
        sets the controls geometry property: position, height, width
        """
        self.__combobox.setGeometry(q_rect)
        self.__lineedit.setGeometry(QtCore.QRect(q_rect.left()+1, q_rect.top()+1, q_rect.width()-20, q_rect.height()-2))
    
    def show(self):
        """
        sets the control visible
        """
        self.__combobox.show()
        self.__lineedit.show()

    def hide(self):
        """
        set the control invisible
        """
        self.__combobox.hide()
        self.__lineedit.hide()
        
    def set_enabled(self, enabled=True):
        """
        enables/disabled the control
        """
        self.__combobox.setEnabled(enabled)
        self.__lineedit.setEnabled(enabled)
        
    def set_preferences(self, list):
        """
        sets the controls dropdown (combobox) list
        """
        self.__combobox.clear()
        self.__combobox.addItems(QtCore.QStringList(list))

    def set_lookup_list(self, list):
        """
        sets the controls lookup list (completer)
        """
        self.__completer.setModel(QtGui.QStringListModel(QtCore.QStringList(list)))
## end
