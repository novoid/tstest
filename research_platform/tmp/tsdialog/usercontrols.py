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

from PyQt4 import QtCore, QtGui


class LineEditCompleter(QtGui.QWidget):

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
    
    def set_geometry(self, qRect):
        """
        sets the controls geometry property: position, height, width
        """
        self.__combobox.setGeometry(qRect)
        self.__lineedit.setGeometry(QtCore.QRect(qRect.left()+1, qRect.top()+1, qRect.width()-20, qRect.height()-2))
    
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

    def set_font(self, font):
        """
        sets the controls font
        """
        self.__lineedit.setFont(font)
        self.__combobox.setFont(font)
        self.__completer.popup().setFont(font)
        
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

    def set_tool_tip(self, string):
        """
        sets the controls tooltip text
        """
        self.__combobox.setToolTip(string)
        self.__lineedit.setToolTip(string)
        
    def set_completion_prefix(self, prefix):
        """
        sets the prefix of the controls lookup functionality
        """
        self.__completer.setCompletionPrefix(prefix)
        if prefix.strip() != "":
            ## avoid showing popup if prefix = ""
            self.__completer.complete()
        
    def set_focus(self):
        """
        sets the control focused
        """
        self.__lineedit.setFocus()
        
    def get_text(self):
        """
        returns the controls text
        """
        return unicode(self.__lineedit.text())

    def set_text(self, string):
        """
        sets the controls text
        """
        self.__lineedit.setText(string)
        self.__lineedit.setFocus()
        self.__lineedit.setCursorPosition(len(string))

    def get_cursor_position(self):
        """
        returns the controls cursor position
        """
        return self.__lineedit.cursorPosition()
    
    def set_cursor_position(self, position):
        """
        sets the controls cursor position
        """
        self.__lineedit.setCursorPosition(position)


## end