# -*- coding: utf-8 -*-

## this file is part of tagstore, an alternative way of storing and retrieving information
## Copyright (C) 2010  Karl Voit, Christoph Friedl, Wolfgang Wintersteller, Johannes Anderwald
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

class SyncDialog(QtGui.QDialog):
    
    
    def __init__(self, store_name, store_list, parent=None):
        """
        initialize the dialog
        """
        
        QtGui.QDialog.__init__(self, parent)

        self.APP_NAME = "Sync Tagstore"
        self.__log = logging.getLogger("TagStoreLogger")
        self.__target_tagstore = None
        self.__source_tagstore = None
        self.__store_list = store_list
    
        self.setObjectName("SyncDialog")
        self.setWindowModality(QtCore.Qt.WindowModal)
        self.__baseLayout = QtGui.QHBoxLayout()
        self.__baseLayout.setContentsMargins(3, 3, 3, 3)
        self.setLayout(self.__baseLayout)

        self.__mainlayout = QtGui.QGridLayout()
        self.__mainwidget = QtGui.QWidget(self)
        self.__mainwidget.setLayout(self.__mainlayout)
        self.__baseLayout.addWidget(self.__mainwidget)
               

        # label for source tagstore
        self.__source_tagstore_label = QtGui.QLabel()
        self.__source_tagstore_label.setWordWrap(True)

        # drop down list
        self.__tagstore_list_view = QtGui.QComboBox()
        self.__tagstore_list_view.clear()
        self.__selected_index = 0
        for current_store_item in store_list:
            name = current_store_item["name"]
            if store_name != None and store_name != "":
                if name == store_name:
                    self.__tagstore_list_view.addItem(name)
                    self.__selected_index = self.__tagstore_list_view.count() - 1                            
                    self.__tagstore_list_view.setItemData(self.__selected_index, QtCore.QVariant(store_list.index(current_store_item)))            

                    self.__tagstore_list_view.setCurrentIndex(self.__selected_index)
            else:
                self.__tagstore_list_view.addItem(name)
                self.__tagstore_list_view.setItemData(self.__tagstore_list_view.count()-1, QtCore.QVariant(store_list.index(current_store_item)))

        # target path label
        self.__target_tagstore_label = QtGui.QLabel()
        self.__target_tagstore_label.setWordWrap(True)

        # drop down list
        self.__tagstore_target_list_view = QtGui.QComboBox()
        self.__tagstore_target_list_view.clear()
        self.__selected_target_index = 0
        for current_store_item in store_list:
            name = current_store_item["name"]
            if store_name != None and store_name != "":
                if name != store_name:
                    self.__tagstore_target_list_view.addItem(name)                    
                    self.__selected_target_index = self.__tagstore_target_list_view.count() - 1
                    self.__tagstore_target_list_view.setItemData(self.__selected_target_index, QtCore.QVariant(store_list.index(current_store_item)))                    
                    self.__tagstore_target_list_view.setCurrentIndex(self.__selected_target_index)
            else:
                self.__tagstore_target_list_view.addItem(name)        
                self.__tagstore_target_list_view.setItemData(self.__tagstore_target_list_view.count()-1, QtCore.QVariant(store_list.index(current_store_item)))        
        
        # target path line
        #self.__target_store_path_line = QtGui.QLineEdit()
        #self.__target_store_path_line.setReadOnly(True)
        #if self.__target_tagstore != None and self.__target_tagstore != "":
        #    self.__target_store_path_line.setText(self.__target_tagstore)
            
        # debug code 
        #self.__target_tagstore = "F:/"
        #self.__target_store_path_line.setText(self.__target_tagstore)
        
        # target directory button
        #self.__config_button = QtGui.QPushButton()
        #icon = QtGui.QIcon()
        #icon.addPixmap(QtGui.QPixmap("./tsresources/images/config.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        #self.__config_button.setIcon(icon)        
        
        # help button
        self.__help_button = QtGui.QPushButton()
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap("./tsresources/images/help.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.__help_button.setIcon(icon)

        # sync button
        self.__sync_button = QtGui.QPushButton()
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap("./tsresources/images/sync.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.__sync_button.setIcon(icon)
        
        #close button
        self.__close_button = QtGui.QPushButton()
        
        # status label
        self.__sync_status_description_label = QtGui.QLabel()
        self.__sync_status_description_label.setWordWrap(True)
        
        # target path label
        self.__sync_status_label = QtGui.QLabel()
        self.__sync_status_label.setWordWrap(True)
                
        # add items to the main layout     
        self.__mainlayout.addWidget(self.__source_tagstore_label, 0, 0, 1, 4)
        self.__mainlayout.addWidget(self.__tagstore_list_view, 0, 5, 1, 4)
        
        self.__mainlayout.addWidget(self.__target_tagstore_label, 1, 0, 1, 4)
        self.__mainlayout.addWidget(self.__tagstore_target_list_view, 1, 5, 1, 4)
        
        #self.__mainlayout.addWidget(self.__target_store_path_line, 1, 5, 1, 10)
        #self.__mainlayout.addWidget(self.__config_button, 1, 15, 1, 4)
        
        self.__mainlayout.addWidget(self.__sync_button, 2, 0, 1, 4)
        self.__mainlayout.addWidget(self.__close_button, 2, 7, 1, 4)
        
        self.__mainlayout.addWidget(self.__sync_status_description_label, 3, 0, 1, 4)
        self.__mainlayout.addWidget(self.__sync_status_label, 3, 5, 2, 10)
        
        
        #self.__mainlayout.addWidget(self.__help_button, 3, 0, 1, 4)
        
        # translate the gui
        self.retranslateUi()
        
        # connect the signals
        self.connect(self.__sync_button, QtCore.SIGNAL("clicked()"), self.__handle_sync_button_action)
        self.connect(self.__close_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("cancel_clicked()"))        
        self.connect(self.__help_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("help_clicked()"))
        #self.connect(self.__config_button, QtCore.SIGNAL("clicked()"), self.__handle_config_button_action)
        self.connect(self.__tagstore_list_view, QtCore.SIGNAL("currentIndexChanged(int)"), self.__handle_source_tagstore_changed)
        self.connect(self.__tagstore_target_list_view, QtCore.SIGNAL("currentIndexChanged(int)"), self.__handle_target_tagstore_changed)
        
        
    def retranslateUi(self):
        """
        translates the gui
        """
        
        self.setWindowTitle(QtGui.QApplication.translate("tagstore", self.APP_NAME, None, QtGui.QApplication.UnicodeUTF8))
        self.__sync_button.setText(QtGui.QApplication.translate("tagstore", "Sync!", None, QtGui.QApplication.UnicodeUTF8))
        self.__sync_button.setToolTip(QtGui.QApplication.translate("tagstore", "Sync the selected target tagstore with the source tagstore", None, QtGui.QApplication.UnicodeUTF8))
       
       
        self.__close_button.setText(QtGui.QApplication.translate("tagstore", "Cancel", None, QtGui.QApplication.UnicodeUTF8))
        self.__help_button.setText(QtGui.QApplication.translate("tagstore", "Help", None, QtGui.QApplication.UnicodeUTF8))
        #self.__config_button.setText(QtGui.QApplication.translate("tagstore", "Choose", None, QtGui.QApplication.UnicodeUTF8))
        
        self.__sync_status_description_label.setText(QtGui.QApplication.translate("tagstore", "Sync Status:", None, QtGui.QApplication.UnicodeUTF8))
        self.__source_tagstore_label.setText(QtGui.QApplication.translate("tagstore", "Source Tagstore:", None, QtGui.QApplication.UnicodeUTF8))
        self.__target_tagstore_label.setText(QtGui.QApplication.translate("tagstore", "Target Tagstore:", None, QtGui.QApplication.UnicodeUTF8))
        
        
    def __handle_source_tagstore_changed(self, index):
        # update current index
        self.__selected_index = index

    def __handle_target_tagstore_changed(self, index):
        # update current index
        self.__selected_target_index = index

            
    #def __handle_config_button_action(self):
    #    file_path = str(QtGui.QFileDialog.getExistingDirectory(self, "Select Root Directory"))
    #    
    #    if file_path != None or file_path != "":
    #        self.__target_tagstore = file_path 
    #        if self.__target_store_path_line != None:
    #            # update path
    #            self.__target_store_path_line.setText(self.__target_tagstore)
        
    def __handle_sync_button_action(self):
        """
        performs arguments checks and then signals the dialog controller to do the work
        """
        
        # get current source tagstore
        current_store_item_index = self.__tagstore_list_view.itemData(self.__selected_index).toPyObject()
        current_store_item = self.__store_list[current_store_item_index]
        self.__source_tagstore = current_store_item["path"]

        # get target tagstore
        current_store_item_index = self.__tagstore_target_list_view.itemData(self.__selected_target_index).toPyObject()
        current_store_item = self.__store_list[current_store_item_index]        
        self.__target_tagstore = current_store_item["path"]
        
        if self.__target_tagstore.find(":/") == -1:
            self.__target_tagstore = self.__target_tagstore.replace(":", ":/")
        
        if self.__source_tagstore.find(":/") == -1:
            self.__source_tagstore = self.__source_tagstore.replace(":", ":/")
        
        
        if self.__source_tagstore == self.__target_tagstore:
            self.show_tooltip("Source and target tagstore are equal")
            return
        
        # disable sync button while the action is performed
        self.__sync_button.setEnabled(False)
        
        # emit signal to the dialog controller
        self.emit(QtCore.SIGNAL("sync_button_pressed"), self.__source_tagstore, self.__target_tagstore)
    
    def show_conflict_dialog(self, title, message, file_name):
        """
        displays a message dialog and signals the result to the caller
        """

        reply = QtGui.QMessageBox.question(self, title,
            message, QtGui.QMessageBox.Yes | 
            QtGui.QMessageBox.No)        

        result = None
        if reply == QtGui.QMessageBox.Yes:
            result = "replace"
        else:
            result = "keep"
    
        # emit signal that the conflict is resolved
        self.emit(QtCore.SIGNAL("sync_conflict_action"), file_name, result)
    
    def set_status_msg(self, message):
        """
        sets a new message in the status label
        """
        if message != None:
            self.__sync_status_label.setText(message)
    
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

    def toggle_sync_button(self, enabled):
        """
        enables / disables the sync button
        """
        self.__sync_button.setEnabled(enabled)        

class SyncDialogController(QtCore.QObject):
    
    def __init__(self, store_name, store_list):
        
        """
        initalize sync dialog controller
        """
        
        QtCore.QObject.__init__(self)
        
        self.__store_name = store_name
        self.__log = logging.getLogger("TagStoreLogger")        
        self.__is_shown = False     
        self.__sync_dialog = SyncDialog(store_name, store_list)
        
        self.connect(self.__sync_dialog, QtCore.SIGNAL("sync_button_pressed"), self.__start_sync)
        self.connect(self.__sync_dialog, QtCore.SIGNAL("sync_conflict_action"), self.__conflict_action)
        self.connect(self.__sync_dialog, QtCore.SIGNAL("cancel_clicked()"), QtCore.SIGNAL("handle_cancel()"))
        self.connect(self.__sync_dialog, QtCore.SIGNAL("help_clicked()"), self.__help_clicked)
        self.connect(self.__sync_dialog, QtCore.SIGNAL("property_clicked()"), QtCore.SIGNAL("open_store_admin_dialog()"))
    
    def __help_clicked(self):
        self.__sync_dialog.show_tooltip("HELP HELP HEl....")

    def __conflict_action(self, file_name, action):
        
        # emit signal to the sync controller
        self.__log.info("__conflict_action: action %s" %(action))
        self.emit(QtCore.SIGNAL("sync_conflict"), file_name, action)

    def __start_sync(self, source_store, target_store):
        """
        initiates the sync process
        """
        
        # signal sync controller that the sync should be started
        self.emit(QtCore.SIGNAL("sync_store"), source_store, target_store)

    def get_view(self):
        return self.__sync_dialog
    
    def set_status_msg(self, message):
        """
        updates the status message
        """
        
        self.__sync_dialog.set_status_msg(message)
    
    def show_conflict_dialog(self, title, message, file_name):
        """
        displays the conflict dialog
        """
        self.__sync_dialog.show_conflict_dialog(title, message, file_name)
    
    def toggle_sync_button(self, enabled):
        """
        enables / disables the sync button
        """
        self.__sync_dialog.toggle_sync_button(enabled)
    
    def show_dialog(self):
        """
        shows the dialog
        """
        
        if self.__is_shown:
            return
        self.__is_shown = True
        self.__sync_dialog.show()
        self.__log.debug("show sync-dialog")
