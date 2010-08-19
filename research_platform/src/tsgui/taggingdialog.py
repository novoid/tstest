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
from usercontrols import LineEditCompleter, ComboboxCompleter


class TaggingDialog(QtGui.QWidget):  

    __pyqtSignals__ = ("tag_text_edited(QString)",
                       "tag_completion_activated(QString)",
                       "tag_preference_activated(QString)",
                       "category_text_edited(QString)",
                       "category_completion_activated(QString)",
                       "confirm_button_pressed()", 
                       "cancel_button_pressed()", 
                       "cancel_all_button_pressed()")

    def __init__(self, parent=None):
        """
        constructor
        """
        QtGui.QWidget.__init__(self, parent)

        self.setFixedSize(298, 355)
        self.setAcceptDrops(False)
        icon = QtGui.QIcon()
        # TODO use resource file
        icon.addPixmap(QtGui.QPixmap("../resources/images/icon.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.setWindowIcon(icon)
        self.setWindowFlags(QtCore.Qt.WindowTitleHint | QtCore.Qt.CustomizeWindowHint | QtCore.Qt.WindowCloseButtonHint)

        self.__tagstore_image = QtGui.QLabel(self)
        self.__tagstore_image.setGeometry(QtCore.QRect(95, 3, 201, 95))
        # TODO use resource file
        self.__tagstore_image.setStyleSheet("QLabel {background-image: url(../resources/images/logo.png)}")
                
        self.__current_operation_label = QtGui.QLabel(self)
        self.__current_operation_label.setGeometry(QtCore.QRect(10, 80, 151, 16))
        font = QtGui.QFont()
        font.setPointSize(10)
        font.setWeight(75)
        font.setBold(True)
        self.__current_operation_label.setFont(font)
        
        self.__store_description_label = QtGui.QLabel(self)
        self.__store_description_label.setGeometry(QtCore.QRect(10, 110, 46, 15))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.__store_description_label.setFont(font)

        self.__store_name_label = QtGui.QLabel(self)
        self.__store_name_label.setGeometry(QtCore.QRect(60, 110, 230, 15))
        self.__store_name_label.setFont(font)
        
        self.__file_description_label = QtGui.QLabel(self)
        self.__file_description_label.setGeometry(QtCore.QRect(10, 140, 46, 15))
        self.__file_description_label.setFont(font)
        
        self.__file_name_label = QtGui.QLabel(self)
        self.__file_name_label.setGeometry(QtCore.QRect(60, 140, 230, 15))
        
        self.__tag_max_label = QtGui.QLabel(self)
        self.__tag_max_label.setGeometry(QtCore.QRect(10, 180, 300, 15))
        self.__tag_max_label.setFont(font)
        
        self.__tag_combobox = ComboboxCompleter(self)
        self.__tag_combobox.set_geometry(QtCore.QRect(10, 200, 279, 22))
        self.__tag_combobox.set_font(font)
        self.__tag_combobox.set_focus()
        self.__tag_combobox.connect(self.__tag_combobox, QtCore.SIGNAL("text_edited(QString)"), self, QtCore.SIGNAL("tag_text_edited(QString)"))
        self.__tag_combobox.connect(self.__tag_combobox, QtCore.SIGNAL("completion_activated(QString)"), self, QtCore.SIGNAL("tag_completion_activated(QString)"))
        self.__tag_combobox.connect(self.__tag_combobox, QtCore.SIGNAL("preference_activated(QString)"), self, QtCore.SIGNAL("tag_preference_activated(QString)"))

        self.__category_label = QtGui.QLabel(self)
        self.__category_label.setGeometry(QtCore.QRect(10, 230, 300, 15))
        self.__category_label.setFont(font)
        
        self.__category_lineedit = LineEditCompleter(self)
        self.__category_lineedit.set_geometry(QtCore.QRect(10, 250, 279, 22))
        self.__category_lineedit.set_font(font)
        self.__category_lineedit.connect(self.__category_lineedit, QtCore.SIGNAL("text_edited(QString)"), self, QtCore.SIGNAL("category_text_edited(QString)"))
        self.__category_lineedit.connect(self.__category_lineedit, QtCore.SIGNAL("completion_activated(QString)"), self, QtCore.SIGNAL("category_completion_activated(QString)"))
        #self.__category_lineedit.set_enabled(False)
        
        self.__horizontal_layout_widget = QtGui.QWidget(self)
        self.__horizontal_layout_widget.setGeometry(QtCore.QRect(0, 310, 299, 40))
        self.__horizontal_layout = QtGui.QHBoxLayout(self.__horizontal_layout_widget)
        self.__horizontal_layout.setSizeConstraint(QtGui.QLayout.SetDefaultConstraint)
        self.__horizontal_layout.setContentsMargins(-1, -1, -1, -1)
        spacer_item = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.__horizontal_layout.addItem(spacer_item)
        self.__confirm_button = QtGui.QPushButton(self.__horizontal_layout_widget)
        self.__confirm_button.connect(self.__confirm_button, QtCore.SIGNAL("clicked()"), self, QtCore.SIGNAL("confirm_button_pressed()"))
        self.__horizontal_layout.addWidget(self.__confirm_button)
        self.__cancel_button = QtGui.QPushButton(self.__horizontal_layout_widget)
        self.__cancel_button.connect(self.__cancel_button, QtCore.SIGNAL("clicked()"), self, QtCore.SIGNAL("cancel_button_pressed()"))
        self.__horizontal_layout.addWidget(self.__cancel_button)
        self.__cancel_all_button = QtGui.QPushButton(self.__horizontal_layout_widget)
        self.__cancel_all_button.connect(self.__cancel_all_button, QtCore.SIGNAL("clicked()"), self, QtCore.SIGNAL("cancel_all_button_pressed()"))
        self.__cancel_all_button.setEnabled(False)
        self.__horizontal_layout.addWidget(self.__cancel_all_button)

        self.retranslate_user_interface()
        QtCore.QMetaObject.connectSlotsByName(self)

    def retranslate_user_interface(self):
        """
        sets the userinterface text to the current localization
        """
        self.setWindowTitle(QtGui.QApplication.translate("tagWidget", "Tag It", None, QtGui.QApplication.UnicodeUTF8))
        self.__current_operation_label.setText(QtGui.QApplication.translate("tagWidget", "New Item Added:", None, QtGui.QApplication.UnicodeUTF8))
        self.__store_description_label.setText(QtGui.QApplication.translate("tagWidget", "Store:", None, QtGui.QApplication.UnicodeUTF8))
        self.__file_description_label.setText(QtGui.QApplication.translate("tagWidget", "File:", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_max_label.setText(QtGui.QApplication.translate("tagWidget", "Tags (max 3):", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_combobox.setToolTip(QtGui.QApplication.translate("tagWidget", "Please enter tags here!", None, QtGui.QApplication.UnicodeUTF8))
        self.__category_label.setText(QtGui.QApplication.translate("tagWidget", "Categories:", None, QtGui.QApplication.UnicodeUTF8))
        self.__confirm_button.setToolTip(QtGui.QApplication.translate("tagWidget", "confirm entered tags", None, QtGui.QApplication.UnicodeUTF8))
        self.__confirm_button.setText(QtGui.QApplication.translate("tagWidget", "Confirm", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancel_button.setToolTip(QtGui.QApplication.translate("tagWidget", "cancel current operation", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancel_button.setText(QtGui.QApplication.translate("tagWidget", "Cancel", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancel_all_button.setToolTip(QtGui.QApplication.translate("tagWidget", "cancel all pending operations", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancel_all_button.setText(QtGui.QApplication.translate("tagWidget", "Cancel All", None, QtGui.QApplication.UnicodeUTF8))

    def show(self, center=False):
        """
        sets the widget visible
        """
        if center:
            self.center()
        super(TaggingDialog, self).show()
               
    def set_store_label_text(self, currentStoreName):
        """
        sets the store name on the user interface
        """
        self.__store_name_label.setText(currentStoreName)
        
    def set_file_label_text(self, currentFileName):
        """
        sets the file name on the user interface
        """
        self.__file_name_label.setText(currentFileName)
        
    def set_tag_preferences(self, itemList):
        """
        sets the preference list (drop-down) of the tag control
        """
        self.__tag_combobox.set_preferences(itemList)
        
    def set_tag_lookup_list(self, itemList):
        """
        sets the lookup list (completer) of the tag control
        """
        self.__tag_combobox.set_lookup_list(itemList)
    
    def set_tag_completion_prefix(self, prefix):
        """
        the prefix is the currently typed word used to generate the lookup list
        """
        self.__tag_combobox.set_completion_prefix(prefix)

    def set_tag_text(self, text):
        """
        sets the text of the controls lineEdit 
        """
        return self.__tag_combobox.set_text(text)
    
    def get_tag_text(self):
        """
        returns the text of the controls lineEdit
        """
        return self.__tag_combobox.get_text()
    
    def get_tag_cursor_position(self):
        """
        returns the controls cursor position
        """
        return self.__tag_combobox.get_cursor_position()
    
    def set_tag_cursor_position(self, position):
        """
        sets the controls cursor position
        """
        self.__tag_combobox.set_cursor_position(position)
        
    def set_category_lookup_list(self, itemList):
        """
        sets the controls lookup list items
        """
        self.__category_lineedit.set_lookup_list(itemList)
    
    def set_category_completion_prefix(self, prefix):
        """
        sets the prefix for lookup functionality of the control
        """
        self.__category_lineedit.set_completion_prefix(prefix)

    def set_category_text(self, text):
        """
        sets the text of the controls lineEdit 
        """
        return self.__category_lineedit.set_text(text)
    
    def get_category_text(self):
        """
        returns the controls lineEdit text
        """
        return self.__category_lineedit.get_text()
    
    def get_category_cursor_position(self):
        """
        returns the controls cursor position
        """
        return self.__category_lineedit.get_cursor_position()
    
    def set_category_cursor_position(self, position):
        """
        sets the controls cursor position
        """
        self.__category_lineedit.set_cursor_position(position)
        
    def set_cancel_all_button_enabled(self, enabled=True):
        """
        enables/disabled the "cancel all" button
        """
        self.__cancel_all_button.setEnabled(enabled)

    def center(self):
        """
        centers the widget on the screen
        """
        screen = QtGui.QDesktopWidget().screenGeometry()
        size =  self.geometry()
        self.move((screen.width()-size.width())/2, (screen.height()-size.height())/2)


## end
