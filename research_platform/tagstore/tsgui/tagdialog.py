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
from tscore.enums import ECategorySetting
from admindialog import StorePreferencesController
from administration import Administration

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
        self.__category_mandatory = False
        
        ## state-flags for indicating, if an error is currently being displayed
        self.__info_no_item_selected_shown = False
        self.__info_no_tag_shown = False
        self.__info_no_category_shown = False
        self.__info_tag_limit_shown = False
        
        self.__selected_item = None
        
        self.__info_palette = None
        ## a list to hold all tag labels - that they can be accessed and removed later on
        self.__tag_label_list = []
        self.__category_label_list = []

        self.setObjectName("TagDialog")
        self.setWindowModality(QtCore.Qt.WindowModal)
        self.__baseLayout = QtGui.QHBoxLayout()
        self.__baseLayout.setContentsMargins(3, 3, 3, 3)
        self.setLayout(self.__baseLayout)
        
        self.__mainlayout = QtGui.QGridLayout()
        self.__mainwidget = QtGui.QWidget()
        self.__mainwidget.setLayout(self.__mainlayout)
        self.__baseLayout.addWidget(self.__mainwidget)
        
        self.__help_button = QtGui.QToolButton()
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(":/ts/images/help.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.__help_button.setIcon(icon)
        
        self.__property_button = QtGui.QPushButton()
        self.__tag_button = QtGui.QPushButton()
        self.__close_button = QtGui.QPushButton()
        self.__item_list_view = QtGui.QListWidget()
        
        self.__cat_line_widget = TagCompleterWidget(self.__max_tags, separator=self.__tag_separator, 
            parent=self)
        
        self.__category_line = self.__cat_line_widget.get_tag_line()
        
        self.__tag_line_widget = TagCompleterWidget(self.__max_tags, separator=self.__tag_separator, 
            parent=self, show_datestamp=self.__show_datestamp)
        
        self.__tag_line = self.__tag_line_widget.get_tag_line()
        
        #self.__tag_label = QtGui.QLabel()
        self.__pop_tag_layout = QtGui.QHBoxLayout()
        self.__pop_tag_layout.setContentsMargins(0, 0, 0, 0)
        self.__pop_tag_widget = QtGui.QWidget()
        self.__pop_tag_widget.setLayout(self.__pop_tag_layout)
        
        
        self.__tag_error_label = QtGui.QLabel()
        self.__tag_error_label.setPalette(self.get_red_palette())
        
#        self.__category_label = QtGui.QLabel()
        self.__pop_category_layout = QtGui.QHBoxLayout()
        self.__pop_category_layout.setContentsMargins(0, 0, 0, 0)
        self.__pop_category_widget = QtGui.QWidget()
        self.__pop_category_widget.setLayout(self.__pop_category_layout)
        
        self.__category_error_label = QtGui.QLabel()
        self.__category_error_label.setPalette(self.get_red_palette())
        
        self.__item_error_label = QtGui.QLabel()
        self.__item_error_label.setPalette(self.get_red_palette())
        
        #self.__tag_error_label.setVisible(False)
        #self.__category_error_label.setVisible(False)
        #self.__item_error_label.setVisible(False)

        self.__mainlayout.addWidget(self.__item_list_view, 0, 0, 1, 4)
        self.__mainlayout.addWidget(self.__item_error_label, 1, 0, 1, 4)
        
        self.__mainlayout.addWidget(self.__tag_line, 2, 0, 1, 3)
        self.__mainlayout.addWidget(self.__tag_button, 2, 3, 4, 1)
        self.__mainlayout.addWidget(self.__tag_error_label, 3, 0, 1, 3)
#        self.__mainlayout.addWidget(self.__tag_label, 4, 0, 1, 3)
        self.__mainlayout.addWidget(self.__pop_tag_widget, 4, 0, 1, 3)
        
        self.__mainlayout.addWidget(self.__category_line, 5, 0, 1, 3)
        self.__mainlayout.addWidget(self.__category_error_label, 6, 0, 1, 3)
        self.__mainlayout.addWidget(self.__pop_category_widget, 7, 0, 1, 3)

        self.__mainlayout.addWidget(self.__help_button, 8, 0, 1, 1)
        self.__mainlayout.addWidget(self.__property_button, 8, 1, 1, 1)
        self.__mainlayout.addItem(QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding), 8, 2, 1, 1)
        self.__mainlayout.addWidget(self.__close_button, 8, 3, 1, 1)
        
        self.setGeometry(self.__mainwidget.geometry())
        # TODO: fix resizing ... 
        #sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        #sizePolicy.setHorizontalStretch(0)
        #sizePolicy.setVerticalStretch(0)
        #sizePolicy.setHeightForWidth(self.sizePolicy().hasHeightForWidth())
        #self.setSizePolicy(sizePolicy)
        """
        self.setContextMenuPolicy(QtCore.Qt.NoContextMenu)
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(":/ts/images/icon.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.setWindowIcon(icon)

        self.__centralwidget = QtGui.QWidget(self)
        self.__centralwidget.setObjectName("__centralwidget")
        
        self.__close_button = QtGui.QPushButton(self.__centralwidget)
        self.__close_button.setGeometry(QtCore.QRect(368, 216, 113, 32))
        self.__close_button.setObjectName("__close_button")
        self.__tag_button = QtGui.QPushButton(self.__centralwidget)
        self.__tag_button.setGeometry(QtCore.QRect(419, 104, 60, 60))
        self.__tag_button.setObjectName("__tag_button")
        self.__property_button = QtGui.QPushButton(self.__centralwidget)
        self.__property_button.setGeometry(QtCore.QRect(48, 217, 113, 32))
        self.__property_button.setObjectName("__property_button")
        self.__help_button = QtGui.QToolButton(self.__centralwidget)
        self.__help_button.setGeometry(QtCore.QRect(20, 220, 25, 23))
        self.__help_button.setObjectName("__help_button")
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(":/ts/images/help.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.__help_button.setIcon(icon)

        self.__item_list_view = QtGui.QListWidget(self.__centralwidget)
        self.__item_list_view.setGeometry(QtCore.QRect(20, 20, 451, 61))
        self.__item_list_view.setObjectName("__item_list_view")
        self.__item_list_view.setSelectionMode(QtGui.QAbstractItemView.SingleSelection)
        
        self.__cat_line_widget = TagCompleterWidget(self.__max_tags, separator=self.__tag_separator, 
            parent=self)
        self.__category_line = self.__cat_line_widget.get_tag_line()
        
        line_size = QtCore.QRect(20, 150, 381, 21)
        self.__category_line.setGeometry(line_size)
        self.__category_line.setObjectName("__category_line")

        self.__tag_line_widget = TagCompleterWidget(self.__max_tags, separator=self.__tag_separator, 
            parent=self, show_datestamp=self.__show_datestamp)
        self.__tag_line = self.__tag_line_widget.get_tag_line()
        
        line_size = QtCore.QRect(20, 90, 381, 21)
        self.__tag_line.setGeometry(line_size)
        self.__tag_line.setObjectName("__tag_line_widget")

        
        self.__category_label = QtGui.QLabel(self.__centralwidget)
        self.__category_label.setGeometry(QtCore.QRect(23, 174, 401, 16))
        self.__category_label.setObjectName("__category_label")
        
        #self.__tag_label = QtGui.QLabel(self.__centralwidget)
        self.__tag_layout = QtGui.QHBoxLayout()
        self.__tag_layout.setGeometry(QtCore.QRect(23, 113, 371, 16))
        
        self.__tag_label = QtGui.QLabel(self.__centralwidget)
#        self.__tag_label = QtGui.QWidget()
#        self.__tag_label.setLayout(self.__tag_layout)
        self.__tag_label.setGeometry(QtCore.QRect(23, 113, 371, 16))
        self.__tag_label.setObjectName("__tag_label")
        """

        self.show_category_line(self.__show_categories)
        self.select_tag_line()
        
        self.retranslateUi()
        self.__set_taborder()
        #self.connect(self, QtCore.SIGNAL("returnPressed()"), self.__handle_enter) 
        self.connect(self.__category_line, QtCore.SIGNAL("returnPressed()"), self.__handle_categoryline_enter) 
        self.connect(self.__tag_line, QtCore.SIGNAL("returnPressed()"), self.__handle_tagline_enter) 
        self.connect(self.__item_list_view, QtCore.SIGNAL("currentRowChanged(int)"), self.__handle_selection_changed)
        self.connect(self.__close_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("cancel_clicked()"))
        self.connect(self.__help_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("help_clicked()"))
        self.connect(self.__property_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("property_clicked()"))
        self.connect(self.__tag_button, QtCore.SIGNAL("clicked()"), self.__tag_button_pressed)
        self.connect(self.__tag_line_widget, QtCore.SIGNAL("tag_limit_reached"), self.__handle_tag_limit_reached)
        self.connect(self.__tag_line_widget, QtCore.SIGNAL("line_empty"), self.__handle_tag_line_empty)
        self.connect(self.__cat_line_widget, QtCore.SIGNAL("line_empty"), self.__handle_category_line_empty)
        """
        """
    def __handle_tag_line_empty(self, empty):
        if not empty:
            self.remove_no_tag_entered_info()
    
    def __handle_category_line_empty(self, empty):
        if not empty:
            self.remove_category_info()
    
    def get_red_palette(self):
        """
        create a palette with red font, to be used on the info-labels 
        """
        if self.__info_palette is None:
            brush = QtGui.QBrush(QtGui.QColor(255, 0, 0, 255));
            #brush.setStyle(QtGui.QBrush.SolidPattern);
            self.__info_palette = QtGui.QPalette()
            self.__info_palette.setBrush(QtGui.QPalette.Active, QtGui.QPalette.WindowText, brush);
            self.__info_palette.setBrush(QtGui.QPalette.Inactive, QtGui.QPalette.WindowText, brush);
        
        return self.__info_palette
    
    def __reset_all_info_states(self, state=False):
        """
        reset all info states
        """
        self.__info_no_category_shown = state
        self.__info_no_tag_shown = state
        self.__info_no_item_selected_shown = state
        self.__info_tag_limit_shown = state
    
    def set_item_info(self, info_text):
        self.__item_error_label.setText(info_text)
        self.__item_error_label.setVisible(True)
        self.__info_no_item_selected_shown = True
        
    def remove_item_info(self):
        self.__item_error_label.setText("")
        self.__item_error_label.setVisible(False)
        self.__info_no_item_selected_shown = False
        
    def set_max_tags_reached_info(self, info_text):
        self.__tag_error_label.setText(info_text)
        #self.__tag_error_label.setVisible(True)
        self.__info_tag_limit_shown = True
        
    def remove_max_tags_reached_info(self):
        self.__tag_error_label.setText("")
        #self.__tag_error_label.setVisible(False)
        self.__info_tag_limit_shown = False
        
    def set_no_tag_entered_info(self, info_text):
        self.__tag_error_label.setText(info_text)
        #self.__tag_error_label.setVisible(True)
        self.__tag_line_widget.set_check_not_empty(True)
        self.__info_no_tag_shown = True
        
    def remove_no_tag_entered_info(self):
        self.__tag_error_label.setText("")
        #self.__tag_error_label.setVisible(False)
        self.__tag_line_widget.set_check_not_empty(False)
        self.__info_no_tag_shown = False
    
    def set_no_category_entered_info(self, info_text):
        self.__category_error_label.setText(info_text)
        #self.__category_error_label.setVisible(True)
        self.__cat_line_widget.set_check_not_empty(True)
        self.__info_no_category_shown = True
    def remove_no_category_entered_info(self):
        self.__category_error_label.setText("")
        #self.__category_error_label.setVisible(False)
        self.__cat_line_widget.set_check_not_empty(False)
        self.__info_no_category_shown = False
        
    def set_not_suitable_categories_entered(self, info_text):
        self.__category_error_label.setText(info_text)
        #self.__categor7y_error_label.setVisible(True)
    
    def remove_not_suitable_categories_entered(self):
        self.__category_error_label.setText("")
        #self.__category_error_label.setVisible(False)
        
        
    def remove_category_info(self):
        self.__category_error_label.setText("")
        #self.__info_no_tag_shown = False
    
    def keyPressEvent(self, event):
        """
        dummy re-implementation to avoid random signal sending
        """
        key = event.key()
        if key == QtCore.Qt.Key_Return or key == QtCore.Qt.Key_Enter:
            self.__log.debug("tag_dialog: RETURN PRESSED")
        ## pass the signal to the normal parent chain
    
    def closeEvent(self, event):
        pass
        
    def __handle_tag_limit_reached(self):
        self.set_max_tags_reached_info(self.trUtf8("Tag limit reached. No more tags can be provided for this item."))
        
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
        
        ##TODO - re-implement the enter-handling
        #sender = self.sender()
        
        #if sender is None or not isinstance(self.__category_line, QtGui.QLineEdit):
        #    return
        
        #if not self.__tag_line_widget.is_empty():
        #    self.__handle_tag_action()
        #else:
        #    self.set_no_tag_entered_info(self.trUtf8("Please type at least one tag in the tag-line"))
    
    def __tag_button_pressed(self):
        self.__handle_tag_action()
    
    def __set_taborder(self):
        if self.__show_categories:
            self.setTabOrder(self.__tag_line, self.__category_line)
            self.setTabOrder(self.__category_line, self.__tag_button)
            self.setTabOrder(self.__tag_button, self.__close_button)
            self.setTabOrder(self.__close_button, self.__help_button)
            self.setTabOrder(self.__help_button, self.__property_button)
            self.setTabOrder(self.__property_button, self.__item_list_view)
        else:
            self.setTabOrder(self.__tag_line, self.__tag_button)
            self.setTabOrder(self.__tag_button, self.__close_button)
            self.setTabOrder(self.__close_button, self.__help_button)
            self.setTabOrder(self.__help_button, self.__property_button)
            self.setTabOrder(self.__property_button, self.__item_list_view)

    def retranslateUi(self):
        self.setWindowTitle(QtGui.QApplication.translate("tagstore", self.APP_NAME, None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_line.setText(QtGui.QApplication.translate("tagstore", "write your tags here", None, QtGui.QApplication.UnicodeUTF8))
        #self.__category_line.setText(QtGui.QApplication.translate("tagstore", "categorize ...", None, QtGui.QApplication.UnicodeUTF8))
        self.__help_button.setToolTip(QtGui.QApplication.translate("tagstore", "Help", None, QtGui.QApplication.UnicodeUTF8))
        self.__property_button.setToolTip(QtGui.QApplication.translate("tagstore", "Set application properties", None, QtGui.QApplication.UnicodeUTF8))
        self.__property_button.setText(QtGui.QApplication.translate("tagstore", "Preferences ...", None, QtGui.QApplication.UnicodeUTF8))
        self.__close_button.setToolTip(QtGui.QApplication.translate("tagstore", "Close the dialog", None, QtGui.QApplication.UnicodeUTF8))
        self.__close_button.setText(QtGui.QApplication.translate("tagstore", "Close", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_button.setToolTip(QtGui.QApplication.translate("tagstore", "Tag the selected item", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_button.setText(QtGui.QApplication.translate("tagstore", "Tag!", None, QtGui.QApplication.UnicodeUTF8))
        
    def __handle_selection_changed(self, row):
        self.__set_selected_item(self.__item_list_view.item(row))

    def __get_selected_item(self):
        if self.__selected_item is None:
            self.__log.error("_TagDialog.__get_selected_item(): no item selected")
            return None
        return self.__selected_item

    def __set_selected_item(self, item):
        """
        save the selected item as field and handle the selection in the list_widget as well
        """
        self.__selected_item = item
        if  item is not None:
            item.setSelected(True)
            self.__log.debug("set_selected_item: %s" % self.__selected_item.text())
        
    def __handle_tag_label_clicked(self, clicked_text):
        current_text = self.__tag_line.text()
        if current_text == "":
            self.__tag_line.setText(clicked_text)
        else:
            self.__tag_line.setText("%s%s %s" % (current_text, self.__tag_separator, clicked_text))
        print clicked_text

    def __handle_category_label_clicked(self, clicked_text):
        current_text = self.__tag_line.text()
        if current_text == "":
            self.__category_line.setText(clicked_text)
        else:
            self.__category_line.setText("%s%s %s" % (current_text, self.__tag_separator, clicked_text))
        print clicked_text

    def remove_item_from_list(self, item):
        if item is None:
            return
        row_to_remove = self.__item_list_view.row(item)
        self.__item_list_view.takeItem(row_to_remove)
        
        current_item = self.__item_list_view.currentItem()
        count = self.__item_list_view.count()
        if  current_item is not None:
            ## selection has automatically been made by widget 
            self.__set_selected_item(current_item)
        elif count == 0:
            ## no items left
            self.__set_selected_item(None)
            self.emit(QtCore.SIGNAL("no_items_left"))
        elif row_to_remove >= count:
            ## the last item has been removed - select the "new" last item
            self.__set_selected_item(self.__item_list_view.item(count-1))
        else:
            ## set the new selected item manually
            self.__set_selected_item(self.__item_list_view.item(row_to_remove))
            
    def remove_all_infos(self):
        """
        remove all info-labels and their according states
        """
        self.remove_category_info()
        self.remove_item_info()
        self.remove_no_tag_entered_info()
        self.remove_max_tags_reached_info()

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
           
        
    def show_category_line(self, setting):
        """
        if ENABLED | ENABLED_ONLY_PERSONAL - add a category_line and a popular_categories_label
        if DISABLED - remove them
        + resize the dialog 
        """
        self.__show_categories = setting
        
        
        if setting != ECategorySetting.DISABLED:
            self.resize(481, 334)
            self.__category_line.setVisible(True)
            self.__pop_category_widget.setVisible(True)
            self.__category_error_label.setVisible(True)
            self.__mainlayout.removeWidget(self.__tag_button)
            self.__mainlayout.addWidget(self.__tag_button, 2, 3, 4, 1)
        else:
            self.resize(481, 268)
            self.__category_line.setVisible(False)
            self.__pop_category_widget.setVisible(False)
            self.__category_error_label.setVisible(False)
            self.__mainlayout.removeWidget(self.__tag_button)
            self.__mainlayout.addWidget(self.__tag_button, 2, 3, 1, 1)
        self.__set_taborder()
    
    def set_category_mandatory(self, mandatory):
        self.__category_mandatory = mandatory
     
    def set_item_list(self, item_list):
        self.__item_list_view.addItems(item_list)

    def add_item(self, item_name):
        """
        add a new item to the list view
        """
        item = QtGui.QListWidgetItem(item_name, self.__item_list_view)
        self.__item_list_view.sortItems()
        self.__set_selected_item(item)
        #self.__item_list_view.addItem(item_name)
        self.__tag_line.setEnabled(True)
        self.__tag_line_widget.select_line()

    def set_tag_list(self, tag_list):
        self.__tag_line_widget.set_tag_completion_list(tag_list)
        
    def set_popular_tags(self, tag_list):
        
        ## remove all existing labels
        for label in self.__tag_label_list:
            self.__pop_tag_layout.removeWidget(label)
            label.destroy()
        ## clear the label list
        self.__tag_label_list = []
        
        for tag in tag_list:
            tmp_label = QtGui.QLabel("<a href='%s'>%s</a>" % (tag, tag))
            tmp_label.setTextInteractionFlags(QtCore.Qt.LinksAccessibleByMouse|
                                              QtCore.Qt.LinksAccessibleByKeyboard|
                                              QtCore.Qt.TextSelectableByMouse)

            self.connect(tmp_label, QtCore.SIGNAL("linkActivated(QString)"), self.__handle_tag_label_clicked)
            self.__pop_tag_layout.addWidget(tmp_label)
            ## fill the new label list
            self.__tag_label_list.append(tmp_label)

    def set_popular_categories(self, cat_list):
        ## remove all existing labels
        for label in self.__category_label_list:
            self.__pop_category_layout.removeWidget(label)
            label.destroy()
        ## clear the label list
        self.__category_label_list = []
        
        for cat in cat_list:
            tmp_label = QtGui.QLabel("<a href='%s'>%s</a>" % (cat, cat))
            tmp_label.setTextInteractionFlags(QtCore.Qt.LinksAccessibleByMouse|
                                              QtCore.Qt.LinksAccessibleByKeyboard|
                                              QtCore.Qt.TextSelectableByMouse)

            self.connect(tmp_label, QtCore.SIGNAL("linkActivated(QString)"), self.__handle_category_label_clicked)
            self.__pop_category_layout.addWidget(tmp_label)
            self.__category_label_list.append(tmp_label)

    def clear_tag_line(self):
        self.__tag_line_widget.clear_line()
        if not self.__show_datestamp:
            self.__tag_line_widget.set_text("write your tags here")
        
    def clear_item_view(self):
        self.__item_list_view.clear()
        
    def show_datestamp(self, show):
        self.__show_datestamp = show
        self.__tag_line_widget.show_datestamp(show)
        
    def set_datestamp_format(self, format):
        self.__tag_line_widget.set_datestamp_format(format)
        
    def show_tooltip(self, message, parent=None):
        """
        show a tooltip which automatically disappears after a few seconds
        an unannoying way to present messages to the user
        default is to show it at the center of the parent-widget
        """
        
        if parent is None:
            parent = self
        
        tip_position = parent.pos()
        
        height = parent.height()/2
        width = parent.width()/2

        tip_position.setX(tip_position.x()+width)
        tip_position.setY(tip_position.y()+height)
        
        QtGui.QWhatsThis.showText(tip_position, message, parent)
        
        ## use a timer to automatically remove the tooltip
        #timer = QtCore.QTimer()
        #timer.connect(timer, QtCore.SIGNAL("timeout()"), self.hide_tooltip)
        #timer.start(4000)

    def hide_tooltip(self):
        """
        if a tooltip is shown - use this method to remove the message
        """
        QtGui.QWhatsThis.hideText()
        
    def set_available_categories(self, category_list):
        """
        the list to be used as item categories
        """
#        self.__category_list = category_list
        self.__cat_line_widget.clear_line()
        self.__cat_line_widget.set_tag_completion_list(category_list)
        #for category in category_list:
        #    QtGui.QListWidgetItem(category, self.__category_line)
        
    def get_category_list(self):
        """
        returns a string list with the user-selected categories
        """
        return self.__cat_line_widget.get_tag_list()
    
    def get_category_completion_list(self):
        return self.__cat_line_widget.get_tag_completion_list()
    
    def is_category_mandatory(self):
        return self.__category_mandatory
    
    def is_category_enabled(self):
        return self.__show_categories

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
        
        self.__item_to_remove = None
        
        self.__is_shown = False
        
        self.connect(self.__tag_dialog, QtCore.SIGNAL("tag_button_pressed"), self.tag_item)
        self.connect(self.__tag_dialog, QtCore.SIGNAL("no_items_left"), self.__handle_no_items)
        self.connect(self.__tag_dialog, QtCore.SIGNAL("cancel_clicked()"), QtCore.SIGNAL("handle_cancel()"))
        self.connect(self.__tag_dialog, QtCore.SIGNAL("help_clicked()"), self.__help_clicked)
        self.connect(self.__tag_dialog, QtCore.SIGNAL("property_clicked()"), QtCore.SIGNAL("open_store_admin_dialog()"))
    
    def __help_clicked(self):
        self.__tag_dialog.show_tooltip("HELP HELP HEl....")

    def __handle_no_items(self):
        """
        use this method to handle the case, there are not items left to tag
        """
        self.hide_dialog()
    
    def tag_item(self, item, tag_list):
        """
        pre-check if all necessary data is available for storing tags to an item 
        """
        if tag_list is None or len(tag_list) == 0:
            self.__tag_dialog.set_no_tag_entered_info(self.trUtf8("Please enter at least one tag for the selected item"))
            return
        if item is None:
            self.__tag_dialog.set_item_info(self.trUtf8("Please select an Item, to which the tags should be added"))
            return
        
        category_list = self.__tag_dialog.get_category_list()
        category_mandatory = self.__tag_dialog.is_category_mandatory()
        category_setting = self.__tag_dialog.is_category_enabled()
        
        ## just check this, if the category line is enabled
        if (category_setting == ECategorySetting.ENABLED or category_setting == ECategorySetting.ENABLED) and category_mandatory and (category_list is None or len(category_list) == 0):
            self.__tag_dialog.set_no_category_entered_info(self.trUtf8("Please select at least one category"))
            return
        
        ## just predefined categories are allowed - check this
        if category_setting == ECategorySetting.ENABLED_ONLY_PERSONAL:
            completion_set = set(self.__tag_dialog.get_category_completion_list())
            #TODO: maybe just take the intersection
            if not category_list.issubset(completion_set):
                self.__tag_dialog.set_not_suitable_categories_entered(
                        self.trUtf8("Please use just the suggested categories"))
            return
            
        self.__item_to_remove = item
        self.emit(QtCore.SIGNAL("tag_item"), self.__store_name, item.text(), tag_list, category_list)
        
    def remove_item(self, item_name):
        """
        should be called after a successful tag-operation at the store
        """
        if item_name == self.__item_to_remove.text():
            self.__tag_dialog.remove_item_from_list(self.__item_to_remove)
            self.__tag_dialog.remove_all_infos()
            self.__item_to_remove = None
            self.__tag_dialog.select_tag_line()
            
    def get_view(self):
        return self.__tag_dialog
    
    def show_message(self, message):
        self.__tag_dialog.show_tooltip(message)    
    
    def add_pending_item(self, file_name):
        #self.__tag_dialog.set_store_label_text(store_name)
        self.__tag_dialog.add_item(file_name)
        
    def set_tag_list(self, tag_list):
        self.__tag_dialog.set_tag_list(tag_list)    

    def set_category_list(self, category_list):
        self.__tag_dialog.set_available_categories(category_list)    

    def show_datestamp(self, show):
        self.__tag_dialog.show_datestamp(show)

    def show_category_line(self, show):
        """
        if True - show the category line and popular categories
        """
        self.__tag_dialog.show_category_line(show)
    
    def set_category_mandatory(self, mandatory):
        self.__tag_dialog.set_category_mandatory(mandatory)

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