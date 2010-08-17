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

    __pyqtSignals__ = ("textEdited(QString)", 
                       "completionActivated(QString)", 
                       "preferenceActivated(QString)")

    def __init__(self, parent=None):
        QtGui.QWidget.__init__(self, parent)
        
        self.__comboBox = QtGui.QComboBox(parent)
        self.__comboBox.connect(self.__comboBox, QtCore.SIGNAL("activated(QString)"), self, QtCore.SIGNAL("preferenceActivated(QString)"))
        self.__lineEdit = QtGui.QLineEdit(parent)
        self.__lineEdit.setStyleSheet("QLineEdit {border: none}")
        QtCore.QMetaObject.connectSlotsByName(parent)
        self.__completer = QtGui.QCompleter()
        self.__completer.setWidget(self.__lineEdit)
        self.__completer.setCaseSensitivity(QtCore.Qt.CaseInsensitive)
        self.__completer.connect(self.__completer, QtCore.SIGNAL("activated(QString)"), self, QtCore.SIGNAL("completionActivated(QString)"))
        self.__lineEdit.connect(self.__lineEdit, QtCore.SIGNAL("textEdited(QString)"), self, QtCore.SIGNAL("textEdited(QString)"))
        QtCore.QMetaObject.connectSlotsByName(parent)
    
    def setGeometry(self, qRect):
        self.__comboBox.setGeometry(qRect)
        self.__lineEdit.setGeometry(QtCore.QRect(qRect.left()+1, qRect.top()+1, qRect.width()-20, qRect.height()-2))
    
    def show(self):
        self.__comboBox.show()
        self.__lineEdit.show()

    def hide(self):
        self.__comboBox.hide()
        self.__lineEdit.hide()
        
    def setEnabled(self, enabled=True):
        self.__comboBox.setEnabled(enabled)
        self.__lineEdit.setEnabled(enabled)

    def setFont(self, font):
        self.__lineEdit.setFont(font)
        self.__comboBox.setFont(font)
        self.__completer.popup().setFont(font)
        
    def setPreferences(self, list):
        self.__comboBox.clear()
        self.__comboBox.addItems(QtCore.QStringList(list))

    def setLookupList(self, list):
        self.__completer.setModel(QtGui.QStringListModel(QtCore.QStringList(list)))

    def setToolTip(self, string):
        self.__comboBox.setToolTip(string)
        self.__lineEdit.setToolTip(string)
        
    def setCompletionPrefix(self, prefix):
        self.__completer.setCompletionPrefix(prefix)
        if prefix.strip() != "":
            ## avoid showing popup if prefix = ""
            self.__completer.complete()
        
    def setFocus(self):
        self.__lineEdit.setFocus()
        
    def text(self):
        return unicode(self.__lineEdit.text())

    def setText(self, string):
        self.__lineEdit.setText(string)
        self.__lineEdit.setFocus()
        self.__lineEdit.setCursorPosition(len(string))

    def cursorPosition(self):
        return self.__lineEdit.cursorPosition()
    
    def setCursorPosition(self, position):
        self.__lineEdit.setCursorPosition(position)


## end