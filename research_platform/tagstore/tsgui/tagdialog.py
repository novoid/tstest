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
import logging
from PyQt4 import QtCore, QtGui
import tsresources.resources
from tagcompleter import TagCompleterWidget
from tscore.tsconstants import TsConstants

class TagDialog(QtGui.QDialog):

    """
    The main gui window for tagging files in several tagstores
    """
    __NO_OF_ITEMS_STRING = "untagged item(s)"
    
    def __init__(self, max_tags, tag_separator, parent=None):
        
        QtGui.QDialog.__init__(self, parent)
        
        self.APP_NAME = "tagstore"
        
        self.__log = logging.getLogger("TagStoreLogger")
        
        self.__max_tags = max_tags
        self.__tag_separator = tag_separator
        ## flag to recognize the current visibility state 
        self.__is_shown = False
        self.__show_datestamp = False
        self.__show_categories = False
        self.__selected_index = None
        
        self.setObjectName("TagDialog")
        self.setWindowModality(QtCore.Qt.WindowModal)
        
        
        # TODO: fix resizing ... 
        #sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        #sizePolicy.setHorizontalStretch(0)
        #sizePolicy.setVerticalStretch(0)
        #sizePolicy.setHeightForWidth(self.sizePolicy().hasHeightForWidth())
        #self.setSizePolicy(sizePolicy)
        self.setContextMenuPolicy(QtCore.Qt.NoContextMenu)
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(":/ts/images/icon.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.setWindowIcon(icon)

        self.__centralwidget = QtGui.QWidget(self)
        self.__centralwidget.setObjectName("__centralwidget")
        
        self.__item_list_view = QtGui.QListWidget(self.__centralwidget)
        self.__item_list_view.setGeometry(QtCore.QRect(20, 20, 451, 61))
        self.__item_list_view.setObjectName("__item_list_view")
        
        self.__tag_line_widget = TagCompleterWidget(self.__max_tags, separator=self.__tag_separator, 
            parent=self, show_datestamp=self.__show_datestamp)
        self.__tag_line = self.__tag_line_widget.get_tag_line()
        
        
        
        line_size = QtCore.QRect(20, 90, 381, 21)
        self.__tag_line.setGeometry(line_size)
        self.__tag_line.setObjectName("__tag_line_widget")
        #self.__tag_line_widget.set_enabled(False)
        
        self.__category_line = QtGui.QLineEdit(self.__centralwidget)
        self.__category_line.setGeometry(QtCore.QRect(20, 150, 381, 21))
        self.__category_line.setObjectName("__category_line")
        
        self.__help_button = QtGui.QToolButton(self.__centralwidget)
        self.__help_button.setGeometry(QtCore.QRect(20, 220, 25, 23))
        self.__help_button.setObjectName("__help_button")
        
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(":/ts/images/help.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.__help_button.setIcon(icon)
        
        self.__tag_label = QtGui.QLabel(self.__centralwidget)
        self.__tag_label.setGeometry(QtCore.QRect(23, 113, 371, 16))
        self.__tag_label.setObjectName("__tag_label")
        self.__category_label = QtGui.QLabel(self.__centralwidget)
        self.__category_label.setGeometry(QtCore.QRect(23, 174, 371, 16))
        self.__category_label.setObjectName("__category_label")
        self.__tag_button = QtGui.QPushButton(self.__centralwidget)
        self.__tag_button.setGeometry(QtCore.QRect(419, 104, 60, 60))
        self.__tag_button.setObjectName("__tag_button")
        self.__close_button = QtGui.QPushButton(self.__centralwidget)
        self.__close_button.setGeometry(QtCore.QRect(368, 216, 113, 32))
        self.__close_button.setObjectName("__close_button")
        self.__property_button = QtGui.QPushButton(self.__centralwidget)
        self.__property_button.setGeometry(QtCore.QRect(48, 217, 113, 32))
        self.__property_button.setObjectName("__property_button")

        self.enable_category_line(self.__show_categories)
        self.select_tag_line()
        
        self.retranslateUi()
        self.__set_taborder()
        self.connect(self, QtCore.SIGNAL("returnPressed()"), self.__handle_enter) 
        self.connect(self.__tag_line, QtCore.SIGNAL("returnPressed()"), self.__handle_tagline_enter) 
        self.connect(self.__category_line, QtCore.SIGNAL("returnPressed()"), self.__handle_categoryline_enter) 
        self.connect(self.__item_list_view, QtCore.SIGNAL("currentRowChanged(int)"), self.__handle_list_selection_changed)
        self.connect(self.__close_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("cancel_clicked()"))
        self.connect(self.__help_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("help_clicked()"))
        self.connect(self.__property_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("property_clicked()"))
        # TODO: check why this signal is emitted on every "enter"
        """
        self.connect(self.__tag_button, QtCore.SIGNAL("clicked()"), self.__tag_button_pressed)
        """
    def __handle_enter(self):
            self.__log.debug("enter hit")
        
    def __handle_tagline_enter(self):
        ## switch to the category_line if it is enabled
        if self.__show_categories:
            self.__category_line.selectAll()
            self.__category_line.setFocus(QtCore.Qt.OtherFocusReason)
        else:
            ## start the default tagging procedure
            self.__handle_tag_action()
            
    def __handle_categoryline_enter(self):
        """
        check if the tagline is not empty
        then emit the "tag" signal
        """
        if not self.__tag_line_widget.is_empty():
            self.__handle_tag_action()
        else:
            QtGui.QMessageBox.information(self.__tag_dialog, "No tag entered", "Please type at least one tag in the tag-line")
    
    def __tag_button_pressed(self):
        sender = self.sender()
        if sender is None or not isinstance(sender, QtGui.QPushButton):
            return
        self.__handle_tag_action()
        
    
    def __set_taborder(self):
        if self.__show_categories:
            self.setTabOrder(self.__tag_line, self.__category_line)
            self.setTabOrder(self.__category_line, self.__tag_button)
            self.setTabOrder(self.__tag_button, self.__close_button)
            self.setTabOrder(self.__close_button, self.__item_list_view)
            self.setTabOrder(self.__item_list_view, self.__help_button)
            self.setTabOrder(self.__help_button, self.__property_button)
        else:
            self.setTabOrder(self.__tag_line, self.__tag_button)
            self.setTabOrder(self.__tag_button, self.__close_button)
            self.setTabOrder(self.__close_button, self.__item_list_view)
            self.setTabOrder(self.__item_list_view, self.__help_button)
            self.setTabOrder(self.__help_button, self.__property_button)

    def retranslateUi(self):
        self.setWindowTitle(QtGui.QApplication.translate("tagstore", self.APP_NAME, None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_line.setText(QtGui.QApplication.translate("tagstore", "write your tags here", None, QtGui.QApplication.UnicodeUTF8))
        self.__category_line.setText(QtGui.QApplication.translate("tagstore", "categorize ...", None, QtGui.QApplication.UnicodeUTF8))
        self.__help_button.setToolTip(QtGui.QApplication.translate("tagstore", "Help", None, QtGui.QApplication.UnicodeUTF8))
        #self.__help_button.setText(QtGui.QApplication.translate("tagstore", "", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_label.setText(QtGui.QApplication.translate("tagstore", "", None, QtGui.QApplication.UnicodeUTF8))
        self.__category_label.setText(QtGui.QApplication.translate("tagstore", "dummy no values yet", None, QtGui.QApplication.UnicodeUTF8))
        self.__property_button.setToolTip(QtGui.QApplication.translate("tagstore", "Set application properties", None, QtGui.QApplication.UnicodeUTF8))
        self.__property_button.setText(QtGui.QApplication.translate("tagstore", "Properties ...", None, QtGui.QApplication.UnicodeUTF8))
        self.__close_button.setToolTip(QtGui.QApplication.translate("tagstore", "Close the dialog", None, QtGui.QApplication.UnicodeUTF8))
        self.__close_button.setText(QtGui.QApplication.translate("tagstore", "Close", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_button.setToolTip(QtGui.QApplication.translate("tagstore", "Tag the selected item", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_button.setText(QtGui.QApplication.translate("tagstore", "Tag!", None, QtGui.QApplication.UnicodeUTF8))
        
    def __handle_list_selection_changed(self, item):
        self.__selected_index = item
        
        #self.__tag_line_widget.set_enabled(True)
        if self.__selected_index is not None:
            self.__log.debug("selected %s" % self.__get_selected_item().text())

    def __get_selected_item(self):
        if self.__selected_index is None:
            self.__log.error("_TagDialog.__get_selected_item(): no item selected")
            return None
        return self.__item_list_view.item(self.__selected_index)
        #return self.__item_list_view.model().itemFromIndex(self.__selected_index)

    def remove_item_from_list(self, item):
        if item is None:
            return
        row_to_remove = self.__item_list_view.row(item)
        
        row_to_select = -1
        
        ## if there is an item above the one to remove - select it.
        if row_to_remove > 0:
            row_to_select = row_to_remove-1
        ## if there is an item below
        elif row_to_remove == 0 and self.__item_list_view.count() > 1:
            row_to_select = row_to_remove+1
        
        item_to_select = self.__item_list_view.item(row_to_select)    
        self.__selected_index = row_to_select
        self.__item_list_view.setItemSelected(item_to_select, True)
        self.__item_list_view.takeItem(row_to_remove)

    def __handle_tag_action(self):
        self.emit(QtCore.SIGNAL("tag_button_pressed"), self.__get_selected_item(), self.__tag_line_widget.get_tag_list())

    def select_tag_line(self):
        self.__tag_line_widget.select_line()

    def set_store_label_text(self, storename):
        ## TODO: set storename to app-title bar
        if storename != "":
            self.setWindowTitle("%s - %s" % (self.APP_NAME, storename))
        else:
            self.setWindowTitle(self.APP_NAME)
             
        
    def enable_category_line(self, enable):
        """
        if True - add a category_line and a popular_categories_label
        if False - remove them
        + resize the dialog 
        """
        self.__show_categories = enable
        
        # TODO: find a way to remove the category widgets at runtime
        # can be done with using "layout" objects. -> means NO absolute positioning
        # absolute positioning is not a good solution
        
        if enable:
            self.resize(489, 272)
            self.__tag_button.setGeometry(QtCore.QRect(419, 104, 60, 60))
            self.__help_button.setGeometry(QtCore.QRect(20, 220, 25, 23))
            self.__property_button.setGeometry(QtCore.QRect(48, 217, 113, 32))
            self.__close_button.setGeometry(QtCore.QRect(368, 217, 113, 32))
            
            self.__category_line.show()
            self.__category_label.show()
        else:
            self.resize(489, 195)
            self.__category_line.hide()
            self.__category_label.hide()
            
            self.__tag_button.setGeometry(QtCore.QRect(419, 85, 60, 40))
            self.__help_button.setGeometry(QtCore.QRect(20, 150, 25, 23))
            self.__property_button.setGeometry(QtCore.QRect(48, 146, 113, 32))
            self.__close_button.setGeometry(QtCore.QRect(368, 146, 113, 32))
            
        self.__set_taborder()
            
    def set_item_list(self, item_list):
        self.__item_list_view.addItems(item_list)

    def add_item(self, item_name):
        """
        add a new item to the list view
        """
        QtGui.QListWidgetItem(item_name, self.__item_list_view)
        self.__item_list_view.sortItems()
        #self.__item_list_view.addItem(item_name)
        self.__tag_line.setEnabled(True)
        self.__tag_line_widget.select_line()

    def set_tag_list(self, tag_list):
        self.__tag_line_widget.set_tag_completion_list(tag_list)
        
    def set_popular_tags(self, tag_list):
        self.__tag_label.setText("")
        for tag in tag_list:
            self.__tag_label.setText(self.__tag_label.text() +" "+ tag)

    def set_popular_categories(self, cat_list):
        self.__category_label.setText("")
        for tag in cat_list:
            self.__category_label.setText(self.__category_label.text() +" "+ tag)

    def clear_tag_line(self):
        self.__tag_line_widget.clear_line()
        
    def clear_item_view(self):
        self.__item_list_view.clear()
        
    def show_datestamp(self, show):
        self.__show_datestamp = show
        self.__tag_line_widget.show_datestamp(show)
        
    def set_datestamp_format(self, format):
        self.__tag_line_widget.set_datestamp_format(format)


class TagDialogController(QtCore.QObject):
    """
    __pyqtSignals__ = ("tag_item",
                       "handle_cancel") 
    """
    def __init__(self, store_name, max_tags, tag_separator):
        
        QtCore.QObject.__init__(self)

        self.__max_tags = max_tags
        self.__store_name = store_name
        
        self.__log = logging.getLogger("TagStoreLogger")
        self.__tag_separator = tag_separator
        self.__tag_dialog = TagDialog(self.__max_tags, self.__tag_separator)
        
        self.__is_shown = False
        
        self.connect(self.__tag_dialog, QtCore.SIGNAL("tag_button_pressed"), self.check_tag_item)
        self.connect(self.__tag_dialog, QtCore.SIGNAL("cancel_clicked()"), QtCore.SIGNAL("handle_cancel()"))
        self.connect(self.__tag_dialog, QtCore.SIGNAL("help_clicked()"), self.__help_clicked)
        self.connect(self.__tag_dialog, QtCore.SIGNAL("property_clicked()"), self.__property_clicked)
    
    def __help_clicked(self):
        QtGui.QMessageBox.information(self.__tag_dialog, "HELP", "HELP will be added soon")

    def __property_clicked(self):
        QtGui.QMessageBox.information(self.__tag_dialog, "Properties", "this button should open the property dialog for the current store")
        
    
    def check_tag_item(self, item, tag_list):
        """
        pre-check if all necessary data is available for storing tags to an item 
        """
        if tag_list is None or len(tag_list) == 0:
            QtGui.QMessageBox.information(self.__tag_dialog, "No tags entered", "Please enter at least one tag for the selected item")
            return
        if item is None:
            QtGui.QMessageBox.information(self.__tag_dialog, "No Item selected", "Please select an Item, to which the tags should be added")
            return
        
        self.__tag_dialog.remove_item_from_list(item)
        self.__tag_dialog.select_tag_line()
        
        self.emit(QtCore.SIGNAL("tag_item"), self.__store_name, item.text(), tag_list)
        
    def add_pending_item(self, file_name):
        #self.__tag_dialog.set_store_label_text(store_name)
        self.__tag_dialog.add_item(file_name)
        
    def set_tag_list(self, tag_list):
        self.__tag_dialog.set_tag_list(tag_list)    

    def show_datestamp(self, show):
        self.__tag_dialog.show_datestamp(show)

    def show_category_line(self, show):
        """
        if True - show the category line and popular categories
        """
        self.__tag_dialog.enable_category_line(show)

    def set_datestamp_format(self, format):
        self.__tag_dialog.set_datestamp_format(format)

    def clear_all_items(self):
        """
        remove all pending items from the tree_view
        """
        self.__tag_dialog.clear_item_view()    
        
    def clear_store_children(self, store_name):
        self.__log.debug("not implemented ... just for multi-store dialogs")
        #self.__tag_dialog.clear_store_children(store_name)

    def show_dialog(self):
        if self.__is_shown:
            return
        self.__is_shown = True
        self.__tag_dialog.show()
        self.__log.debug("show tag-dialog")
        
    def hide_dialog(self):
        if not self.__is_shown:
            return
        self.__is_shown = False
        self.__tag_dialog.clear_tag_line()
        self.__tag_dialog.hide()
        self.__log.debug("hide tag-dialog")
        
    def set_popular_tags(self, tag_list):
        self.__tag_dialog.set_popular_tags(tag_list)

    def set_popular_categories(self, cat_list):
        self.__tag_dialog.set_popular_categories(cat_list)
        
    def set_store_name(self, store_name):
        self.__store_name = store_name
        self.__tag_dialog.set_store_label_text(store_name)
## END