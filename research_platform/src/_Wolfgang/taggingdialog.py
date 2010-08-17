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
        self.__lineEdit.setCursorPosition(position
                                          )

class TaggingDialog(QtGui.QWidget):  

    __pyqtSignals__ = ("tagTextEdited(QString)",
                       "tagCompletionActivated(QString)",
                       "tagPreferenceActivated(QString)",
                       "categoryTextEdited(QString)",
                       "categoryCompletionActivated(QString)",
                       "categoryPreferenceActivated(QString)",
                       "confirmButtonPressed()", 
                       "cancelButtonPressed()", 
                       "cancelAllButtonPressed()")

    def __init__(self, parent=None):  
        QtGui.QWidget.__init__(self, parent)

        self.setFixedSize(298, 355)
        self.setAcceptDrops(False)
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap("images/icon.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.setWindowIcon(icon)
        self.setWindowFlags(QtCore.Qt.WindowTitleHint | QtCore.Qt.CustomizeWindowHint | QtCore.Qt.WindowCloseButtonHint)

        self.__tagstoreImage = QtGui.QLabel(self)
        self.__tagstoreImage.setGeometry(QtCore.QRect(95, 3, 201, 95))
        self.__tagstoreImage.setStyleSheet("QLabel {background-image: url(images/logo.png)}")
                
        self.__currentOperationLabel = QtGui.QLabel(self)
        self.__currentOperationLabel.setGeometry(QtCore.QRect(10, 80, 151, 16))
        font = QtGui.QFont()
        font.setPointSize(10)
        font.setWeight(75)
        font.setBold(True)
        self.__currentOperationLabel.setFont(font)
        
        self.__storeDescriptionLabel = QtGui.QLabel(self)
        self.__storeDescriptionLabel.setGeometry(QtCore.QRect(10, 110, 46, 15))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.__storeDescriptionLabel.setFont(font)

        self.__storeNameLabel = QtGui.QLabel(self)
        self.__storeNameLabel.setGeometry(QtCore.QRect(60, 110, 230, 15))
        self.__storeNameLabel.setFont(font)
        
        self.__fileDescriptionLabel = QtGui.QLabel(self)
        self.__fileDescriptionLabel.setGeometry(QtCore.QRect(10, 140, 46, 15))
        self.__fileDescriptionLabel.setFont(font)
        
        self.__fileNameLabel = QtGui.QLabel(self)
        self.__fileNameLabel.setGeometry(QtCore.QRect(60, 140, 230, 15))
        
        self.__tagMaxLabel = QtGui.QLabel(self)
        self.__tagMaxLabel.setGeometry(QtCore.QRect(10, 180, 300, 15))
        self.__tagMaxLabel.setFont(font)
        
        self.__tagCombobox = LineEditCompleter(self)
        self.__tagCombobox.setGeometry(QtCore.QRect(10, 200, 279, 22))
        self.__tagCombobox.setFont(font)
        self.__tagCombobox.setFocus()
        self.__tagCombobox.connect(self.__tagCombobox, QtCore.SIGNAL("textEdited(QString)"), self, QtCore.SIGNAL("tagTextEdited(QString)"))
        self.__tagCombobox.connect(self.__tagCombobox, QtCore.SIGNAL("completionActivated(QString)"), self, QtCore.SIGNAL("tagCompletionActivated(QString)"))
        self.__tagCombobox.connect(self.__tagCombobox, QtCore.SIGNAL("preferenceActivated(QString)"), self, QtCore.SIGNAL("tagPreferenceActivated(QString)"))

        self.__categoryLabel = QtGui.QLabel(self)
        self.__categoryLabel.setGeometry(QtCore.QRect(10, 230, 300, 15))
        self.__categoryLabel.setFont(font)
        
        self.__categoryCombobox = LineEditCompleter(self)
        self.__categoryCombobox.setGeometry(QtCore.QRect(10, 250, 279, 22))
        self.__categoryCombobox.setFont(font)
        self.__categoryCombobox.connect(self.__categoryCombobox, QtCore.SIGNAL("textEdited(QString)"), self, QtCore.SIGNAL("categoryTextEdited(QString)"))
        self.__categoryCombobox.connect(self.__categoryCombobox, QtCore.SIGNAL("completionActivated(QString)"), self, QtCore.SIGNAL("categoryCompletionActivated(QString)"))
        self.__categoryCombobox.connect(self.__categoryCombobox, QtCore.SIGNAL("preferenceActivated(QString)"), self, QtCore.SIGNAL("categoryPreferenceActivated(QString)"))
        self.__categoryCombobox.setEnabled(False)
        
        self.__horizontalLayoutWidget = QtGui.QWidget(self)
        self.__horizontalLayoutWidget.setGeometry(QtCore.QRect(0, 310, 299, 40))
        self.__horizontalLayout = QtGui.QHBoxLayout(self.__horizontalLayoutWidget)
        self.__horizontalLayout.setSizeConstraint(QtGui.QLayout.SetDefaultConstraint)
        self.__horizontalLayout.setContentsMargins(-1, -1, -1, -1)
        spacerItem = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.__horizontalLayout.addItem(spacerItem)
        self.__confirmButton = QtGui.QPushButton(self.__horizontalLayoutWidget)
        self.__confirmButton.connect(self.__confirmButton, QtCore.SIGNAL("clicked()"), self, QtCore.SIGNAL("confirmButtonPressed()"))
        self.__horizontalLayout.addWidget(self.__confirmButton)
        self.__cancelButton = QtGui.QPushButton(self.__horizontalLayoutWidget)
        self.__cancelButton.connect(self.__cancelButton, QtCore.SIGNAL("clicked()"), self, QtCore.SIGNAL("cancelButtonPressed()"))
        self.__horizontalLayout.addWidget(self.__cancelButton)
        self.__cancelAllButton = QtGui.QPushButton(self.__horizontalLayoutWidget)
        self.__cancelAllButton.connect(self.__cancelAllButton, QtCore.SIGNAL("clicked()"), self, QtCore.SIGNAL("cancelAllButtonPressed()"))
        self.__cancelAllButton.setEnabled(False)
        self.__horizontalLayout.addWidget(self.__cancelAllButton)

        self.retranslateUserInterface()
        QtCore.QMetaObject.connectSlotsByName(self)

    def retranslateUserInterface(self):
        self.setWindowTitle(QtGui.QApplication.translate("tagWidget", "Tag It", None, QtGui.QApplication.UnicodeUTF8))
        self.__currentOperationLabel.setText(QtGui.QApplication.translate("tagWidget", "New Item Added:", None, QtGui.QApplication.UnicodeUTF8))
        self.__storeDescriptionLabel.setText(QtGui.QApplication.translate("tagWidget", "Store:", None, QtGui.QApplication.UnicodeUTF8))
        self.__fileDescriptionLabel.setText(QtGui.QApplication.translate("tagWidget", "File:", None, QtGui.QApplication.UnicodeUTF8))
        self.__tagMaxLabel.setText(QtGui.QApplication.translate("tagWidget", "Tags (max 3):", None, QtGui.QApplication.UnicodeUTF8))
        self.__tagCombobox.setToolTip(QtGui.QApplication.translate("tagWidget", "Please enter tags here!", None, QtGui.QApplication.UnicodeUTF8))
        self.__categoryLabel.setText(QtGui.QApplication.translate("tagWidget", "Categories:", None, QtGui.QApplication.UnicodeUTF8))
        self.__confirmButton.setToolTip(QtGui.QApplication.translate("tagWidget", "confirm entered tags", None, QtGui.QApplication.UnicodeUTF8))
        self.__confirmButton.setText(QtGui.QApplication.translate("tagWidget", "Confirm", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancelButton.setToolTip(QtGui.QApplication.translate("tagWidget", "cancel current operation", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancelButton.setText(QtGui.QApplication.translate("tagWidget", "Cancel", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancelAllButton.setToolTip(QtGui.QApplication.translate("tagWidget", "cancel all pending operations", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancelAllButton.setText(QtGui.QApplication.translate("tagWidget", "Cancel All", None, QtGui.QApplication.UnicodeUTF8))

    def show(self, center=False):
        if center:
            self.center()
        super(TaggingDialog, self).show()
               
    def setStoreLabelText(self, currentStoreName):
        self.__storeNameLabel.setText(currentStoreName)
        
    def setFileLabelText(self, currentFileName):
        self.__fileNameLabel.setText(currentFileName)
        
    def setTagPreferences(self, itemList):
        self.__tagCombobox.setPreferences(itemList)
        
    def setTagLookupList(self, itemList):
        self.__tagCombobox.setLookupList(itemList)
    
    def setTagCompletionPrefix(self, prefix):
        self.__tagCombobox.setCompletionPrefix(prefix)

    def setTagText(self, text):
        return self.__tagCombobox.setText(text)
    
    def tagText(self):
        return self.__tagCombobox.text()
    
    def tagCursorPosition(self):
        return self.__tagCombobox.cursorPosition()
    
    def setTagCursorPosition(self, position):
        self.__tagCombobox.setCursorPosition(position)
        
    def setCategoryPreferences(self, itemList):
        self.__categoryCombobox.setPreferences(itemList)
        
    def setCategoryLookupList(self, itemList):
        self.__categoryCombobox.setLookupList(itemList)
    
    def setCategoryCompletionPrefix(self, prefix):
        self.__categoryCombobox.setCompletionPrefix(prefix)

    def categoryText(self):
        return self.__categoryCombobox.text()
    
    def categoryCursorPosition(self):
        return self.__categoryCombobox.cursorPosition()
    
    def setCategoryCursorPosition(self, position):
        self.__categoryCombobox.setCursorPosition(position)
        
    def setCancelAllButtonEnabled(self, enabled=True):
        self.__cancelAllButton.setEnabled(enabled)

    def center(self):
        screen = QtGui.QDesktopWidget().screenGeometry()
        size =  self.geometry()
        self.move((screen.width()-size.width())/2, (screen.height()-size.height())/2)


## end        
        
  
