#!/usr/bin/env python
# -*- coding: iso-8859-15 -*-
## this file is part of tagstore, an alternative way of storing and retrieving information
## Copyright (C) 2010  Karl Voit, Michael Pirrer
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

from PyQt4 import QtGui, QtCore
from tscore.configwrapper import ConfigWrapper
from tscore.tsconstants import TsConstants

class HelpDialog(QtGui.QDialog):


    def __init__(self, tab, parent = None):
        '''
        Constructor
        '''
        QtGui.QDialog.__init__(self, parent)
        
        self.__tab = tab
              
        self.setWindowFlags(QtCore.Qt.WindowTitleHint)
        
        self.__config_wrapper = ConfigWrapper(TsConstants.CONFIG_PATH)
        
        self.setWindowTitle("tagstore Manager Help")
        
        self.setWindowIcon(QtGui.QIcon('./tsresources/images/help.png'))
           
        self.setWindowModality(QtCore.Qt.WindowModal)
        self.__base_layout = QtGui.QVBoxLayout()
        self.__bb_layout = QtGui.QHBoxLayout()
        self.setLayout(self.__base_layout)
        
        self.__description_label = QtGui.QLabel()
        self.__cancel_button = QtGui.QPushButton(self.trUtf8("Close"))
        self.__visible_checkbox = QtGui.QCheckBox(self.trUtf8("Show Help on "
                                                              "Startup"))      
        
        self.__visible_checkbox.setChecked(True)
        
        if self.__tab == "tagdialog":
            self.__description_label.setText(self.trUtf8("Hallo."))
            self.setWindowTitle(self.trUtf8("Welcome to tagstore"))
            if not self.__config_wrapper.get_show_tag_help():
                self.__visible_checkbox.setChecked(False)
        elif self.__tab == "My Tags":
            self.__description_label.setText(self.trUtf8("Hallo0."))
            if not self.__config_wrapper.get_show_my_tags_help():
                self.__visible_checkbox.setChecked(False)
        elif self.__tab == "Datestamps":
            self.__description_label.setText(self.trUtf8("Hallo1."))
            if not self.__config_wrapper.get_show_datestamps_help():
                self.__visible_checkbox.setChecked(False)
        elif self.__tab == "Expiry Date":
            self.__description_label.setText(self.trUtf8("Hallo2."))
            if not self.__config_wrapper.get_show_expiry_date_help():
                self.__visible_checkbox.setChecked(False)
        elif self.__tab == "Re-Tagging":
            self.__description_label.setText(self.trUtf8("Hallo3."))
            if not self.__config_wrapper.get_show_retagging_help():
                self.__visible_checkbox.setChecked(False)
        elif self.__tab == "Rename Tags":
            self.__description_label.setText(self.trUtf8("Hallo4."))
            if not self.__config_wrapper.get_show_rename_tags_help():
                self.__visible_checkbox.setChecked(False)
        elif self.__tab == "Store Management":
            self.__description_label.setText(self.trUtf8("Hallo5."))
            if not self.__config_wrapper.get_show_store_management_help():
                self.__visible_checkbox.setChecked(False)
        elif self.__tab == "Sync Settings":
            self.__description_label.setText(self.trUtf8("Hallo6."))
            if not self.__config_wrapper.get_show_sync_settings_help():
                self.__visible_checkbox.setChecked(False)
                
        self.__description_label.setWordWrap(True)
        
        self.__base_layout.addWidget(self.__description_label)
        self.__bb_layout.addWidget(self.__visible_checkbox)
        self.__bb_layout.addWidget(self.__cancel_button)
        self.__base_layout.addLayout(self.__bb_layout)
        
        self.__visible_checkbox.stateChanged.connect(self.__handle_checkbox)
 
        self.connect(self.__cancel_button, QtCore.SIGNAL('clicked()'), 
                     self.__handle_close)
        
    def __handle_close(self):
        self.close()
        
    def __handle_checkbox(self, state):
        if state:
            tmp_state = "true"
        else:
            tmp_state = "false"
        
        if self.__tab == "tagdialog":
            self.__config_wrapper.set_show_tag_help(tmp_state)
        elif self.__tab == "My Tags":
            self.__config_wrapper.set_show_my_tags_help(tmp_state)
        elif self.__tab == "Datestamps":
            self.__config_wrapper.set_show_datestamps_help(tmp_state)
        elif self.__tab == "Expiry Date":
            self.__config_wrapper.set_show_expiry_date_help(tmp_state)
        elif self.__tab == "Re-Tagging":
            self.__config_wrapper.set_show_retagging_help(tmp_state)
        elif self.__tab == "Rename Tags":
            self.__config_wrapper.set_show_rename_tags_help(tmp_state)
        elif self.__tab == "Store Management":
            self.__config_wrapper.set_show_store_management_help(tmp_state)
        elif self.__tab == "Sync Settings":
            self.__config_wrapper.set_show_sync_settings_help(tmp_state)
            
## end        