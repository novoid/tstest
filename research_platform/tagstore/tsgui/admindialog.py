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
import os
import logging
from PyQt4 import QtGui, QtCore
from tscore.configwrapper import ConfigWrapper
from tscore.tsconstants import TsConstants
from tscore.enums import ECategorySetting, EDateStampFormat
from tscore.vocabularywrapper import VocabularyWrapper
from tscore.specialcharhelper import SpecialCharHelper

class StorePreferencesView(QtGui.QDialog):
    """
    view class for handling tagstore - preferences
    """
    
    def __init__(self, parent=None):
        
        QtGui.QDialog.__init__(self, parent)
        
        self.resize(640, 480)
        self.setModal(True)
            
        self.__layout = QtGui.QVBoxLayout(self)
        
        self.__tab_widget = QtGui.QTabWidget()
        
        self.__apply_button = QtGui.QPushButton(self.trUtf8("Save"))
        self.__cancel_button = QtGui.QPushButton(self.trUtf8("Cancel"))
        
        self.__button_widget = QtGui.QWidget()
        self.__button_layout = QtGui.QHBoxLayout()
        self.__button_widget.setLayout(self.__button_layout)

        self.__button_layout.addWidget(self.__cancel_button)
        self.__button_layout.addWidget(self.__apply_button)
        
        self.__layout.addWidget(self.__tab_widget)
        self.__layout.addWidget(self.__button_widget)
        
        self.connect(self.__apply_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("apply_clicked()"))
        self.connect(self.__cancel_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("cancel_clicked()"))
        
    def add_preference_tab(self, preference_widget, title):
        """
        put a new preference view to the tab list 
        """
        self.__tab_widget.addTab(preference_widget, title)
    
    def select_preference_tab(self, preference_widget):
        """
        select the tab of the widget provided as parameter
        """
        index = self.__tab_widget.indexOf(preference_widget)
        self.__tab_widget.setCurrentWidget(preference_widget)
        
    def remove_preference_tab(self, preference_widget):
        """
        remove an already added tab
        """
        index = self.__tab_widget.indexOf(preference_widget)
        self.__tab_widget.removeTab(index)
        
    def show_tooltip(self, message, parent=None):
        """
        show a tooltip
        """
        
        if parent is None:
            parent = self
        
        tip_position = parent.pos()
        
        height = parent.height()/2
        width = parent.width()/2

        tip_position.setX(tip_position.x()+width)
        tip_position.setY(tip_position.y()+height)
        
        QtGui.QWhatsThis.showText(tip_position, message, parent)
        
        
class BasePreferenceView(QtGui.QWidget):
    
    def __init__(self, parent=None):
        QtGui.QWidget.__init__(self)
        
        self.__description_label = QtGui.QLabel()
        self.__main_layout = QtGui.QVBoxLayout()
        self.__main_panel = QtGui.QWidget()
        self.__main_panel.setLayout(self.__main_layout)
        
        self.__base_layout = QtGui.QVBoxLayout()
        
        self.__base_layout.addWidget(self.__description_label)
        self.__base_layout.addWidget(self.__main_panel)
        
        self.setLayout(self.__base_layout)
            
    def set_description(self, description):
        """
        set the description to be shown at the top of the view
        """
        self.__description_label.setText(description)
        
    def add_widget(self, widget):
        self.__main_layout.addWidget(widget)
        
    def _promote_setting_changed(self, store_name, setting_name, setting_value):
        self.emit(QtCore.SIGNAL("setting_changed"), store_name, setting_name, setting_value) 
        
    def get_main_layout(self):
        return self.__main_layout
    
    def show_tooltip(self, message, parent=None):
        """
        show a tooltip
        """
        
        if parent is None:
            parent = self
        
        tip_position = parent.mapToGlobal(parent.pos())
        
        height = parent.height()/2
        width = parent.width()/2

        tip_position.setX(tip_position.x()+width)
        tip_position.setY(tip_position.y()+height)
        
        QtGui.QWhatsThis.showText(tip_position, message, parent)
        
class BasePreferenceController(QtCore.QObject):

    def __init__(self):
        QtCore.QObject.__init__(self)
        
        self._view = self._create_view()
        ## a list with dictionary elements containing store name, setting and value
        self._settings_dict_list = []
    
        ## a view can emit a "setting_changed" signal. 
        ## the controller will to the necessary steps to handle this change
        self.connect(self._view, QtCore.SIGNAL("setting_changed"), self._handle_gui_setting_change)
    
    def _create_view(self):
        """
        must be reimplemented by the subclasses
        """
        raise Exception("this method has not been reimplemented")

    def _handle_gui_setting_change(self, store_name, setting_name, setting_value):
        """
        change the new setting value to the settings list 
        """
        for setting in self._settings_dict_list:
            if setting["STORE_NAME"] == store_name and setting["SETTING_NAME"] == setting_name:
                setting["SETTING_VALUE"] = setting_value

    def get_settings(self):
        """
        return a dictinary with preferences, the values and the proper store_name 
        """
        self._add_additional_settings()
        return self._settings_dict_list
    
    def _add_additional_settings(self):
        """
        refresh not automatically stored settings in the dict_list
        """
        raise Exception("must be implemented by subclass")
        
    def add_setting(self, setting_name, value, store_id=None):
        self._settings_dict_list.append(dict(STORE_NAME=store_id, SETTING_NAME=setting_name, SETTING_VALUE=value))
        self._handle_setting(store_id, setting_name, value)
        
    def _handle_setting(self, store_id, setting_name, setting_value):
        raise Exception("method must be re-implemented by sublass")
    
    def get_view(self):
        return self._view
        
class MultipleStorePreferenceView(BasePreferenceView):
    
    def __init__(self, store_name_list, parent=None):
        """
        super class for views which are used for setting values to multiple stores
        provides a combobox for selecting a store
        """
        BasePreferenceView.__init__(self)
        
        self._store_setting_list = []
        
        self._store_combo = QtGui.QComboBox()
        
        if store_name_list is not None:
            self.set_store_names(store_name_list)
        
        self.__content_panel = QtGui.QWidget()
        self.__content_layout = QtGui.QVBoxLayout()
        self.__content_panel.setLayout(self.__content_layout)
        
        ## call the add_widget method of super class
        ## this is because this class provides a add_widget method as well
        BasePreferenceView.add_widget(self, self._store_combo)
        BasePreferenceView.add_widget(self, self.__content_panel)
    
        self.connect(self._store_combo, QtCore.SIGNAL("activated(QString)"), QtCore.SIGNAL("store_selected(QString)"))
        
    def add_widget(self, widget):
        """
        the provided widget will be added to the reserved content area
        """
        self.__content_layout.addWidget(widget)
        
    def set_store_names(self, store_name_list):
        """
        set a list of store_names to be displayed at the combo box
        """
        self._store_name_list = store_name_list
        self._store_combo.clear()
        self._store_combo.addItems(store_name_list)

    def add_store_name(self, store_name):
        """
        add a store_name to be displayed at the combo box
        """
        self._store_name_list.append(store_name)
        self._store_combo.addItem(store_name)

class MultipleStorePreferenceController(BasePreferenceController):
    
    def __init__(self, store_list):
        BasePreferenceController.__init__(self)
        
        ## this is the name of the currently selected store
        self._current_store = None
        self._store_list = store_list
        
        self.connect(self._view, QtCore.SIGNAL("store_selected(QString)"), self._set_selected_store)
    
    def _set_selected_store(self, store_name):
        self._current_store = unicode(store_name)
        self._map_store_settings_to_gui()
        
    def _map_store_settings_to_gui(self):
        """
        another store has been selected in the combobox 
        so map the settings of the selected store to the gui
        """
        for setting in self._settings_dict_list:
            if setting["STORE_NAME"] == self._current_store:
                self._handle_setting(setting["STORE_NAME"], setting["SETTING_NAME"], setting["SETTING_VALUE"])
    
    def set_store_names(self, store_name_list):
        self.get_view().set_store_names(store_name_list)
        if store_name_list is not None and len(store_name_list) > 0:
            ## take the first store in the list to be selected in the gui
            self._set_selected_store(store_name_list[0]) 
    
class StoreAdminView(BasePreferenceView):

    def __init__(self, store_list):
        
        BasePreferenceView.__init__(self)
        
        self.set_description(self.trUtf8("Manage your tagstore directories. Build new ones, rename them or delete ..."))
        
        self.__selected_store = None
        
        self.__store_list_view = QtGui.QListWidget()
        
        self.__central_widget = QtGui.QWidget()
        self.__central_layout = QtGui.QGridLayout()
        self.__central_widget.setLayout(self.__central_layout)
        
        self.__btn_new_store = QtGui.QPushButton(self.trUtf8("New Tagstore"))
        self.__btn_build_new = QtGui.QPushButton(self.trUtf8("Rebuild ..."))
        self.__btn_rename = QtGui.QPushButton(self.trUtf8("Rename ..."))
        self.__btn_delete = QtGui.QPushButton(self.trUtf8("Delete ..."))

        self.__central_layout.addWidget(self.__btn_new_store, 0, 0, 1, 1)
        self.__central_layout.addWidget(self.__store_list_view, 1, 0, 1, 3)
        self.__central_layout.addWidget(self.__btn_build_new, 2, 0, 1, 1)
        self.__central_layout.addWidget(self.__btn_rename, 2, 1, 1, 1)
        self.__central_layout.addWidget(self.__btn_delete, 2, 2, 1, 1)
        
        self.add_widget(self.__central_widget)
        
        if store_list is not None:
            self.set_store_names(store_list)
        
        self.__enable_buttons(False)
        
        self.connect(self.__btn_new_store, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("create_new_store()"))
        self.connect(self.__btn_build_new, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("rebuild_store()"))
        self.connect(self.__btn_delete, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("delete_store()"))
        self.connect(self.__btn_rename, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("rename_store()"))
        self.connect(self.__store_list_view, QtCore.SIGNAL("itemSelectionChanged()"), self.__store_selection_changed)
    
    def __store_selection_changed(self):
        selection_list = self.__store_list_view.selectedItems()
        if selection_list is None or len(selection_list) == 0:
            self.__selected_store = None
            self.__enable_buttons(False)
        else:
            self.__selected_store = selection_list[0]
            self.__enable_buttons(True)
    
    def __enable_buttons(self, enable):
        self.__btn_build_new.setEnabled(enable)
        self.__btn_delete.setEnabled(enable)
        self.__btn_rename.setEnabled(enable)
    
    def set_store_names(self, store_names):
        """
        set the store paths to be shown in the list view
        """
        
        for store_name in store_names:
            self.__store_list_view.addItem(QtGui.QListWidgetItem(store_name))
    
    def get_selected_store(self):
        return self.__selected_store
        
    def add_store_name(self, store_name):
        """
        add a new store path to be shown in the list view
        """
        self.__store_list_view.addItem(QtGui.QListWidgetItem(store_name))
        
class StoreAdminController(BasePreferenceController):
    
    #TODO: signals for immediate store creating/deleting/renaming
    
    def __init__(self, store_list):
        BasePreferenceController.__init__(self)
        
        self.__store_list = store_list
        self.get_view().set_store_names(store_list.keys())
        
        self.connect(self.get_view(), QtCore.SIGNAL("create_new_store()"), self.__handle_new_store)
        self.connect(self.get_view(), QtCore.SIGNAL("rebuild_store()"), self.__rebuild_store)
        self.connect(self.get_view(), QtCore.SIGNAL("rename_store()"), self.__rename_store)
        self.connect(self.get_view(), QtCore.SIGNAL("delete_store()"), self.__delete_store)
        
    def __handle_new_store(self):
        
        home_path = os.path.expanduser("~")
        
        dir = QtGui.QFileDialog.getExistingDirectory(self.get_view(), 
                    self.trUtf8("Select a directory for the new tagstore"), 
                    home_path)

        ##TODO create new store framework (folders, files)
        ##TODO write new store to config

        if dir is None or dir == "":
            return
        ## prepare path for a split("/")        
        if dir.endsWith("/"):
            dir = dir.remove(dir.length()-1, 1)
        ## check if new store name is a duplicate
        store_name = dir.split("/").takeLast()
        if store_name in self.__store_list.keys():
            self.get_view().show_tooltip(self.trUtf8("A store with this name already exists. Please choose another store"))
            return 
        
        self.emit(QtCore.SIGNAL("new"), dir)
        self.get_view().add_store_name(store_name)
    
    def __rebuild_store(self):
        
        selection = QtGui.QMessageBox.information(self.get_view(), self.trUtf8("Rebuild Store"), 
                    self.trUtf8("Do you really want to rebuild the selected store? Please be aware, that this may take some minutes"),
                    QtGui.QMessageBox.Yes, QtGui.QMessageBox.Cancel)
        if selection == QtGui.QMessageBox.Yes:
            self.emit(QtCore.SIGNAL("rebuild"), self.get_view().get_selected_store().text())

    def __delete_store(self):
        selection = QtGui.QMessageBox.information(self.get_view(), self.trUtf8("Delete Store"), 
                        self.trUtf8("Do you really want to delete the selected store? Be aware, " \
                        "that the following directory and all of its contents will be deleted: \n %s" % self.__store_list[str(self.get_view().get_selected_store().text())]),
                        QtGui.QMessageBox.Yes, QtGui.QMessageBox.Cancel)
        if selection == QtGui.QMessageBox.Yes:
            self.emit(QtCore.SIGNAL("delete"), self.get_view().get_selected_store().text())
    
    def __rename_store(self):
        store_name = self.get_view().get_selected_store().text()
        new_store_name = QtGui.QInputDialog.getText(self.get_view(), self.trUtf8("Rename a tagstore"), self.trUtf8("new name:"), text=store_name)
        if new_store_name is not None and new_store_name != "":
            self.emit(QtCore.SIGNAL("rename"), store_name, new_store_name)
    
    def _create_view(self):
        return StoreAdminView(None)
        
    def _add_additional_settings(self):
        pass
    
class VocabularyAdminView(MultipleStorePreferenceView):

    def __init__(self, store_list=None, parent=None):
        MultipleStorePreferenceView.__init__(self, store_list)
        
        self.set_description(self.trUtf8("Define your own vocabulary to be used as categorizing tags"))

        self.__selected_vocabulary = None

        self.__radio_layout = QtGui.QVBoxLayout()

        self.__radio_deactivated = QtGui.QRadioButton(self.trUtf8("deactivated"))
        self.__radio_deactivated.setProperty("value", QtCore.QVariant(ECategorySetting.DISABLED))
        
        self.__radio_activated = QtGui.QRadioButton(self.trUtf8("activated, without restrictions"))
        self.__radio_activated.setProperty("value", QtCore.QVariant(ECategorySetting.ENABLED))
        
        self.__radio_activated_restricted = QtGui.QRadioButton(self.trUtf8("activated, restricted to personal categories"))
        self.__radio_activated_restricted.setProperty("value", QtCore.QVariant(ECategorySetting.ENABLED_ONLY_PERSONAL))
        
        self.__radio_single_restricted = QtGui.QRadioButton(self.trUtf8("activated, restricted to personal categories, only one tagline"))
        self.__radio_single_restricted.setProperty("value", QtCore.QVariant(ECategorySetting.ENABLED_SINGLE_CONTROLLED_TAGLINE))
        
        self.__checkbox_mandatory = QtGui.QCheckBox(self.trUtf8("At least one category is mandatory"))
        
        self.__radio_layout.addWidget(self.__radio_deactivated)
        self.__radio_layout.addWidget(self.__radio_activated)
        self.__radio_layout.addWidget(self.__radio_activated_restricted)
        self.__radio_layout.addWidget(self.__radio_single_restricted)
        #self.__radio_layout.addWidget(self.__checkbox_mandatory)
        
        self.__radio_panel = QtGui.QWidget()
        self.__radio_panel.setLayout(self.__radio_layout)
        
        self.__vocabulary_list_view = QtGui.QListWidget()
        
        self.__btn_add_vocabulary = QtGui.QPushButton(self.trUtf8("Add"))
        self.__btn_del_vocabulary = QtGui.QPushButton(self.trUtf8("Delete"))
        
        self.__btn_panel_layout = QtGui.QHBoxLayout()
        self.__btn_panel_layout.addWidget(self.__btn_add_vocabulary)
        self.__btn_panel_layout.addWidget(self.__btn_del_vocabulary)
        
        self.__btn_panel = QtGui.QWidget()
        self.__btn_panel.setLayout(self.__btn_panel_layout)
        
        self.add_widget(self.__radio_panel)
        self.add_widget(self.__vocabulary_list_view)
        self.add_widget(self.__btn_panel)
        self.add_widget(self.__checkbox_mandatory)
        
        self.connect(self.__btn_add_vocabulary, QtCore.SIGNAL("clicked()"), self.__add_vocabulary)
        self.connect(self.__btn_del_vocabulary, QtCore.SIGNAL("clicked()"), self.__delete_vocabulary)
        self.connect(self.__radio_deactivated, QtCore.SIGNAL("toggled(bool)"), self.__voc_deactivated)
        self.connect(self.__radio_activated, QtCore.SIGNAL("toggled(bool)"), self.__voc_activated)
        self.connect(self.__radio_activated_restricted, QtCore.SIGNAL("toggled(bool)"), self.__voc_activated_restricted)
        self.connect(self.__radio_single_restricted, QtCore.SIGNAL("toggled(bool)"), self.__voc_single_restricted)
        self.connect(self.__vocabulary_list_view, QtCore.SIGNAL("itemSelectionChanged()"), self.__voc_selection_changed)
        self.connect(self.__checkbox_mandatory, QtCore.SIGNAL("toggled(bool)"), self.__category_mandatory_checked)
        
    def enable_radio_buttons(self, enable):
        self.__radio_activated.setEnabled(enable)
        self.__radio_activated_restricted.setEnabled(enable)
        self.__radio_deactivated.setEnabled(enable)
        self.__radio_single_restricted.setEnabled(enable)
        self.__checkbox_mandatory.setEnabled(enable)
        
    def __category_mandatory_checked(self, checked):
        if checked:
            self._promote_setting_changed(self._store_combo.currentText(), TsConstants.SETTING_CATEGORY_MANDATORY, True)
        else:
            self._promote_setting_changed(self._store_combo.currentText(), TsConstants.SETTING_CATEGORY_MANDATORY, False)
        
        self.connect(self.__vocabulary_list_view, QtCore.SIGNAL("itemSelectionChanged()"), self.__voc_selection_changed)
    def __voc_selection_changed(self):
        selection_list = self.__vocabulary_list_view.selectedItems()
        if selection_list is None or len(selection_list) == 0:
            self.__selected_vocabulary = None
            self.__voc_not_selected_state()
        else:
            self.__selected_vocabulary = selection_list[0]
            self.__voc_selected_state()
            
    def __voc_not_selected_state(self):
        self.__btn_del_vocabulary.setEnabled(False)

    def __voc_selected_state(self):
        self.__btn_del_vocabulary.setEnabled(True)
    
    def __enable_voc_widgets(self, enable):
        self.__vocabulary_list_view.clearSelection()
        self.__btn_del_vocabulary.setEnabled(False)
        self.__vocabulary_list_view.setEnabled(enable)
        self.__btn_add_vocabulary.setEnabled(enable)
    
    def __add_vocabulary(self):
        result = QtGui.QInputDialog.getText(self, self.trUtf8("Add new vocabulary"), self.trUtf8("vocabulary:"))
        if result[1]:
            self.__vocabulary_list_view.addItem(QtGui.QListWidgetItem(result[0]))
        
        self._promote_setting_changed(self._store_combo.currentText(),
                  TsConstants.SETTING_CATEGORY_VOCABULARY, self.__get_vocabulary_list())
        
    def __delete_vocabulary(self):
        row_to_remove = self.__vocabulary_list_view.row(self.__selected_vocabulary)
        self.__vocabulary_list_view.takeItem(row_to_remove)
        
        self._promote_setting_changed(self._store_combo.currentText(),
                  TsConstants.SETTING_CATEGORY_VOCABULARY, self.__get_vocabulary_list())
        
    def __get_vocabulary_list(self):
        vocabulary_list = []
        for index in range(self.__vocabulary_list_view.count()):
            vocabulary_list.append(unicode(self.__vocabulary_list_view.item(index).text()))
        return set(vocabulary_list)
    
    def __voc_deactivated(self, checked):
        if checked:
            self._promote_setting_changed(self._store_combo.currentText(), 
                      TsConstants.SETTING_SHOW_CATEGORY_LINE, ECategorySetting.DISABLED)
            self.__checkbox_mandatory.setEnabled(False)
            
    def __voc_activated(self, checked):
        if checked:
            self._promote_setting_changed(self._store_combo.currentText(), 
                      TsConstants.SETTING_SHOW_CATEGORY_LINE, ECategorySetting.ENABLED)
            self.__checkbox_mandatory.setEnabled(True)
        
    def __voc_activated_restricted(self, checked):
        if checked:
            self.__enable_voc_widgets(True)
            self._promote_setting_changed(self._store_combo.currentText(), 
                      TsConstants.SETTING_SHOW_CATEGORY_LINE, ECategorySetting.ENABLED_ONLY_PERSONAL)
            self.__checkbox_mandatory.setEnabled(True)
        else:
            self.__enable_voc_widgets(False)

    def __voc_single_restricted(self, checked):
        if checked:
            self.__enable_voc_widgets(True)
            self._promote_setting_changed(self._store_combo.currentText(), 
                      TsConstants.SETTING_SHOW_CATEGORY_LINE, ECategorySetting.ENABLED_SINGLE_CONTROLLED_TAGLINE)
            self.__checkbox_mandatory.setEnabled(True)
        else:
            self.__enable_voc_widgets(False)
        
    def set_vocabulary_disabled(self):
        self.__radio_deactivated.setChecked(True)
        self.__enable_voc_widgets(False)
        
    def set_vocabulary_enabled(self):
        self.__radio_activated.setChecked(True)
        self.__enable_voc_widgets(False)
        
    def set_vocabulary_enabled_personal(self):
        self.__radio_activated_restricted.setChecked(True)
        self.__enable_voc_widgets(True)
        
    def set_vocabulary_list(self, vocabulary_list):
        self.__vocabulary_list_view.clear()
        self.__vocabulary_list_view.addItems(vocabulary_list)
        
    def set_category_mandatory(self, is_mandatory):
        """
        there must be a boolean value provided as param
        """
        self.__checkbox_mandatory.setChecked(is_mandatory)
        
class VocabularyAdminController(MultipleStorePreferenceController):
    
    def __init__(self, store_list):
        MultipleStorePreferenceController.__init__(self, store_list)
        
        self.set_store_names(self._store_list)
        
        self.__vocabulary_list = []
        
    def _create_view(self):
        return VocabularyAdminView()
    
    def _handle_setting(self, store_name, setting_name, setting_value):
        if store_name == self._current_store:
            if setting_name == TsConstants.SETTING_SHOW_CATEGORY_LINE:
                if setting_value == ECategorySetting.DISABLED:
                    self.get_view().set_vocabulary_disabled()
                elif setting_value == ECategorySetting.ENABLED:
                    self.get_view().set_vocabulary_enabled()
                elif setting_value == ECategorySetting.ENABLED_ONLY_PERSONAL:
                    self.get_view().set_vocabulary_enabled_personal()
            if setting_name == TsConstants.SETTING_CATEGORY_VOCABULARY:
                self.get_view().set_vocabulary_list(setting_value)
            if setting_name == TsConstants.SETTING_CATEGORY_MANDATORY:
                self.get_view().set_category_mandatory(setting_value)
                
    def _add_additional_settings(self):
        pass
    
    def set_settings_editable(self, enabled):
        self.get_view().enable_radio_buttons(enabled)
 
class TagAdminView(MultipleStorePreferenceView):
    
    def __init__(self, store_list=None, parent=None):
        MultipleStorePreferenceView.__init__(self, store_list)
    
        self.set_description(self.trUtf8("Define your own vocabulary to be used as categorizing tags"))

        self.__selected_desc_tag = None
        self.__selected_cat_tag = None

        self.__desc_tag_list_view = QtGui.QListWidget()
        self.__cat_tag_list_view = QtGui.QListWidget()
        
        self.__btn_desc_tag_rename = QtGui.QPushButton(self.trUtf8("Rename"))
        self.__btn_desc_tag_rename.setEnabled(False)
        self.__btn_cat_tag_rename = QtGui.QPushButton(self.trUtf8("Rename"))
        self.__btn_cat_tag_rename.setEnabled(False)
        
        desc_layout = QtGui.QVBoxLayout()
        desc_layout.addWidget(QtGui.QLabel(self.trUtf8("Describing Tags")))
        desc_layout.addWidget(self.__desc_tag_list_view)
        desc_layout.addWidget(self.__btn_desc_tag_rename)

        self.__desc_panel = QtGui.QWidget()
        self.__desc_panel.setLayout(desc_layout)

        cat_layout = QtGui.QVBoxLayout()
        cat_layout.addWidget(QtGui.QLabel(self.trUtf8("Categorizing Tags")))
        cat_layout.addWidget(self.__cat_tag_list_view)
        cat_layout.addWidget(self.__btn_cat_tag_rename)
        
        self.__cat_panel = QtGui.QWidget()
        self.__cat_panel.setLayout(cat_layout)
        
        self.__list_view_layout = QtGui.QHBoxLayout()
        self.__list_view_layout.addWidget(self.__desc_panel)
        self.__list_view_layout.addWidget(self.__cat_panel)
        
        self.__list_view_panel = QtGui.QWidget()
        self.__list_view_panel.setLayout(self.__list_view_layout)
        
        self.add_widget(self.__list_view_panel)
    
        self.connect(self.__desc_tag_list_view, QtCore.SIGNAL("itemSelectionChanged()"), self.__desc_selection_changed)
        self.connect(self.__cat_tag_list_view, QtCore.SIGNAL("itemSelectionChanged()"), self.__cat_selection_changed)
        self.connect(self.__btn_desc_tag_rename, QtCore.SIGNAL("clicked()"), self.__rename_desc_tag_btn_clicked)
        self.connect(self.__btn_cat_tag_rename, QtCore.SIGNAL("clicked()"), self.__rename_cat_tag_btn_clicked)
    
    def __rename_desc_tag_btn_clicked(self):
        """
        show an input dialog to rename a selected tag
        """
        new_tag = self.__show_renaming_dialog(self.__selected_desc_tag.text())
        if new_tag != "":
            self.emit(QtCore.SIGNAL("rename_desc_tag"), self.__selected_desc_tag.text(), new_tag)
            self.__desc_tag_list_view.takeItem(self.__desc_tag_list_view.row(self.__selected_desc_tag))
            self.__desc_tag_list_view.addItem(QtGui.QListWidgetItem(new_tag))
            self.__desc_tag_list_view.sortItems()
            
    def __rename_cat_tag_btn_clicked(self):
        """
        show an input dialog to rename a selected tag
        """
        new_tag = self.__show_renaming_dialog(self.__selected_cat_tag.text())
        if new_tag != "":
            self.emit(QtCore.SIGNAL("rename_cat_tag"), self.__selected_cat_tag.text(), new_tag)
            self.__cat_tag_list_view.takeItem(self.__cat_tag_list_view.row(self.__selected_cat_tag))
            self.__cat_tag_list_view.addItem(QtGui.QListWidgetItem(new_tag))
            self.__cat_tag_list_view.sortItems()
    
    def __show_renaming_dialog(self, old_value):
        """
        show the dialog for renaming a tag. 
        returns an empty string if the renaming is not allowed.
        OR
        returns the new entered tag 
        """
        
        result = QtGui.QInputDialog.getText(self, self.trUtf8("Rename a tag"), 
                                            self.trUtf8("Please consider that renaming a tag can be a time consuming process for your system"), 
                                            text=old_value)
        
        ## the second element of the result list is True if the OK button has been clicked 
        if result[1]:
            new_tag = unicode(result[0])
            if self.__check_special_chars(new_tag):
                return ""
            return new_tag
        return ""
        
    
    def __check_special_chars(self, string_to_check):
        """
        check whether the string contains a not allowed special char
        OR
        if it equals a not allowed keyword
        this method automatically shows a message
        """
        if SpecialCharHelper.contains_special_chars([string_to_check]):
            self.show_tooltip(self.trUtf8("The new tag contains a special character which is not allowed."))
            return True
        special_string = SpecialCharHelper.is_special_string([string_to_check])
        if special_string != "":
            self.show_tooltip(self.trUtf8("The new tag '%s' is a not allowed keyword." % special_string))
            return True
        return False

    def __desc_selection_changed(self):
        selection_list = self.__desc_tag_list_view.selectedItems()
        if selection_list is None or len(selection_list) == 0:
            self.__selected_desc_tag = None
            self.__btn_desc_tag_rename.setEnabled(False)
        else:
            self.__selected_desc_tag = selection_list[0]
            self.__btn_desc_tag_rename.setEnabled(True)
            
    def __cat_selection_changed(self):
        selection_list = self.__cat_tag_list_view.selectedItems()
        if selection_list is None or len(selection_list) == 0:
            self.__selected_cat_tag = None
            self.__btn_cat_tag_rename.setEnabled(False)
        else:
            self.__selected_cat_tag = selection_list[0]
            self.__btn_cat_tag_rename.setEnabled(True)
    
    def set_describing_tags(self, tag_list):
        self.clear_describing_tags()
        if tag_list is not None:
            self.__desc_tag_list_view.addItems(tag_list)
            
    def set_categorizing_tags(self, tag_list):
        self.__cat_tag_list_view.clear()
        if tag_list is not None:
            self.__cat_tag_list_view.addItems(tag_list)

    def clear_describing_tags(self):
        self.__desc_tag_list_view.clear()

    def clear_categorizing_tags(self):
        self.__cat_tag_list_view.clear()
            
    def enable_cat_widgets(self, enable):
        self.__cat_tag_list_view.setEnabled(enable)
    
    def enable_desc_widgets(self, enable):
        self.__desc_tag_list_view.setEnabled(enable)
        
    def __get_cat_list(self):
        vocabulary_list = []
        for index in range(self.__desc_tag_list_view.count()):
            vocabulary_list.append(unicode(self.__desc_tag_list_view.item(index).text()))
        return set(vocabulary_list)
    
    def __get_desc_list(self):
        vocabulary_list = []
        for index in range(self.__cat_tag_list_view.count()):
            vocabulary_list.append(unicode(self.__cat_tag_list_view.item(index).text()))
        return set(vocabulary_list)
    

class TagAdminController(MultipleStorePreferenceController):
    
    def __init__(self, store_list):
        MultipleStorePreferenceController.__init__(self, store_list)
        
        self.set_store_names(self._store_list)
        
        self.connect(self.get_view(), QtCore.SIGNAL("rename_desc_tag"), self.__handle_desc_tag_renaming)
        self.connect(self.get_view(), QtCore.SIGNAL("rename_cat_tag"), self.__handle_cat_tag_renaming)
    
    def __handle_desc_tag_renaming(self, old_tag, new_tag):
        self.emit(QtCore.SIGNAL("rename_desc_tag"), old_tag, new_tag, self._current_store)
    
    def __handle_cat_tag_renaming(self, old_tag, new_tag):
        self.emit(QtCore.SIGNAL("rename_cat_tag"), old_tag, new_tag, self._current_store)
    
    def _create_view(self):
        return TagAdminView()
    
    def _add_additional_settings(self):
        """
        not needed here ... yet
        """
        pass
    
    def _handle_setting(self, store_name, setting_name, setting_value):
        if store_name == self._current_store:
            if setting_name == TsConstants.SETTING_DESC_TAGS:
                self.get_view().set_describing_tags(setting_value)
            elif setting_name == TsConstants.SETTING_CAT_TAGS:
                self.get_view().set_categorizing_tags(setting_value)
            elif setting_name == TsConstants.SETTING_SHOW_CATEGORY_LINE:
                    if setting_value == ECategorySetting.DISABLED:
                        self.get_view().enable_desc_widgets(True)
                        self.get_view().enable_cat_widgets(False)
                        ## clear the list because it is not needed
                        self.get_view().clear_categorizing_tags()
                    if setting_value == ECategorySetting.ENABLED_SINGLE_CONTROLLED_TAGLINE:
                        self.get_view().enable_desc_widgets(False)
                        ## clear the list because it is not needed
                        self.get_view().clear_describing_tags()
                        self.get_view().enable_cat_widgets(True)
                        self.get_view().set_describing_tags(None)
    
class DatestampAdminView(MultipleStorePreferenceView):

    def __init__(self, store_list=None):
        MultipleStorePreferenceView.__init__(self, store_list)
        self.set_description(self.trUtf8("You can enable datestamps to be provided automatically at the tagging dialog"))
        
        self.__radio_layout = QtGui.QVBoxLayout()

        self.__radio_deactivated = QtGui.QRadioButton(self.trUtf8("deactivated"))
        self.__radio_activated_m = QtGui.QRadioButton(self.trUtf8("automatic datestamp: 2010-12"))
        self.__radio_activated_d = QtGui.QRadioButton(self.trUtf8("automatic datestamp: 2010-12-31"))
        
        self.__radio_layout.addWidget(self.__radio_deactivated)
        self.__radio_layout.addWidget(self.__radio_activated_m)
        self.__radio_layout.addWidget(self.__radio_activated_d)
        
        self.__radio_panel = QtGui.QWidget()
        self.__radio_panel.setLayout(self.__radio_layout)
        
        self.add_widget(self.__radio_panel)
    
        self.connect(self.__radio_deactivated, QtCore.SIGNAL("toggled(bool)"), self.__datestamp_deactivated)
        self.connect(self.__radio_activated_m, QtCore.SIGNAL("toggled(bool)"), self.__datestamp_activated_m)
        self.connect(self.__radio_activated_d, QtCore.SIGNAL("toggled(bool)"), self.__datestamp_activated_d)
    
    def __datestamp_deactivated(self, checked):
        if checked:
            self._promote_setting_changed(self._store_combo.currentText(), 
                      TsConstants.SETTING_DATESTAMP_FORMAT, EDateStampFormat.DISABLED)
            
    def __datestamp_activated_m(self, checked):
        if checked:
            self._promote_setting_changed(self._store_combo.currentText(), 
                      TsConstants.SETTING_DATESTAMP_FORMAT, EDateStampFormat.MONTH)
            
    def __datestamp_activated_d(self, checked):
        if checked:
            self._promote_setting_changed(self._store_combo.currentText(), 
                      TsConstants.SETTING_DATESTAMP_FORMAT, EDateStampFormat.DAY)
    
    def set_datestamp_disabled(self):
        self.__radio_deactivated.setChecked(True)
        
    def set_datestamp_month(self):
        self.__radio_activated_m.setChecked(True)
        
    def set_datestamp_day(self):
        self.__radio_activated_d.setChecked(True)
        
class DatestampAdminController(MultipleStorePreferenceController):
    
    def __init__(self, store_list):
        MultipleStorePreferenceController.__init__(self, store_list)
        
        self.set_store_names(self._store_list)
        
    def _create_view(self):
        return DatestampAdminView()
    
    def _handle_setting(self, store_name, setting_name, setting_value):
        if store_name == self._current_store:
            if setting_name == TsConstants.SETTING_DATESTAMP_FORMAT:
                if setting_value == EDateStampFormat.DISABLED:
                    self.get_view().set_datestamp_disabled()
                elif setting_value == EDateStampFormat.MONTH:
                    self.get_view().set_datestamp_month()
                elif setting_value == EDateStampFormat.DAY:
                        self.get_view().set_datestamp_day()
    
    def _add_additional_settings(self):
        pass
    
class ExpiryAdminView(BasePreferenceView):

    def __init__(self, storparent=None):
        BasePreferenceView.__init__(self)
        self.set_description(self.trUtf8("Define a prefix for giving files an expiry date."))
        
        self.__prefix = ""
        
        self.__radio_layout = QtGui.QVBoxLayout()

        self.__radio_deactivated = QtGui.QRadioButton(self.trUtf8("deactivated"))
        self.__radio_activated = QtGui.QRadioButton(self.trUtf8("activated, with prefix"))
        
        self.__radio_layout.addWidget(self.__radio_deactivated)
        self.__radio_layout.addWidget(self.__radio_activated)
        
        self.__radio_panel = QtGui.QWidget()
        self.__radio_panel.setLayout(self.__radio_layout)
        
        self.__prefix_line = QtGui.QLineEdit()

        self.__detailed_description_label = QtGui.QLabel()
        self.__update_prefix(self.__prefix)
        self.__detailed_description_label.setWordWrap(True)
        
        self.add_widget(self.__radio_panel)
        self.add_widget(self.__prefix_line)
        self.add_widget(self.__detailed_description_label)
        
        
        self.connect(self.__radio_deactivated, QtCore.SIGNAL("toggled(bool)"), self.__expiry_detoggled)
        self.connect(self.__radio_activated, QtCore.SIGNAL("toggled(bool)"), self.__expiry_toggled)
        self.connect(self.__prefix_line, QtCore.SIGNAL("textChanged(QString)"), self.__update_prefix)
                     
    def __expiry_detoggled(self, checked):
        if checked:
            self.__prefix_line.setEnabled(False)
            self.__detailed_description_label.setEnabled(False)
            self._promote_setting_changed(None, TsConstants.SETTING_EXPIRY_PREFIX, "")

    def __expiry_toggled(self, checked):
        if checked:
            self.__prefix_line.setEnabled(True)
            self.__detailed_description_label.setEnabled(True)
            self._promote_setting_changed(None, TsConstants.SETTING_EXPIRY_PREFIX, self.__prefix_line.text())
                     
    def __update_prefix(self, prefix):
        self.__prefix = prefix
        descr_text = self.trUtf8("Directories or files tagged with '%s2010-12' " \
            "will be moved to the tagstore directory %s on the January 1st 2011 associated tags will be removed. "\
            "The correct writing of '%s2010-12' is really important thereby." % (self.__prefix, self.trUtf8("expired_items"), self.__prefix))
        self.__detailed_description_label.setText(descr_text)
    
    def get_prefix(self):
        return self.trUtf8(self.__prefix_line.text())

    def set_prefix(self, prefix):
        self.__prefix = prefix
        self.__prefix_line.setText(prefix)
        
    def set_expiry_disabled(self):
        self.__radio_deactivated.setChecked(True)
        self.__prefix_line.setEnabled(False)
    
    def set_expiry_enabled(self, prefix):
        self.__radio_activated.setChecked(True)
        self.__prefix_line.setEnabled(True)
        self.set_prefix(prefix)
        
    def is_expiry_enabled(self):
        return self.__radio_activated.isChecked()
        

class ExpiryAdminController(BasePreferenceController):
    
    def __init__(self):
        BasePreferenceController.__init__(self)
    
    def _create_view(self):
        return ExpiryAdminView(None)
        
    def _handle_setting(self, store_id, setting_name, setting_value):
        if setting_name == TsConstants.SETTING_EXPIRY_PREFIX:
            if setting_value is not None and setting_value != "":
                self.get_view().set_expiry_enabled(setting_value)
                self.get_view().set_prefix(setting_value)
            else:
                self.get_view().set_expiry_disabled()
                    
    def _add_additional_settings(self):
        for setting in self._settings_dict_list:
            if setting["SETTING_NAME"] == TsConstants.SETTING_EXPIRY_PREFIX:
                ## just write the prefix if it is enabled
                if self.get_view().is_expiry_enabled():
                    prefix = self.get_view().get_prefix()
                    if prefix is None or prefix == "":
                        self.get_view().show_tooltip(self.trUtf8("Please provide a prefix, when this setting is enabled"))
                        return
                    setting["SETTING_VALUE"] = prefix        
        
class StorePreferencesController(QtCore.QObject):
    
    
    def __init__(self, parent=None):
        """
        controller for the store-preferences dialog
        """
        #QtCore.QObject.__init__(self)
        super(StorePreferencesController, self).__init__(parent)
        #TODO: @CF: config wrapper should be passed from administration.py later on, same with store configs
        self.__log = logging.getLogger("TagStoreLogger")
      
        self.__store_config_dict = {}
        self.__store_vocabulary_wrapper_dict = {}
        self.__store_names = []
        self.__store_dict = {}
        self._store_list = None
        self.__first_time_init = True
        self.__first_start = False
        
        self.TAB_NAME_STORE = self.trUtf8("Store Management")
        self.TAB_NAME_DATESTAMP = self.trUtf8("Datestamps")
        self.TAB_NAME_EXPIRY = self.trUtf8("Expiry Date")
        self.TAB_NAME_VOCABULARY = self.trUtf8("Vocabulary")
        self.TAB_NAME_TAGS = self.trUtf8("Rename Tags")
        ## a list with all controllers used at the preference view
        self.__preference_controller_list = {}
        
        ## the main preferences window 
        self.__dialog = StorePreferencesView(parent=parent)
            
        self.connect(self.__dialog, QtCore.SIGNAL("apply_clicked()"), self.__handle_save)
        self.connect(self.__dialog, QtCore.SIGNAL("cancel_clicked()"), self.__handle_cancel)
    def set_main_config(self, main_config):
        self.__main_config = main_config

    def set_store_list(self, store_list):
        if store_list is not None:
            self._store_list = store_list
            self.__store_names = []
            self.__store_dict = {}
            for store in self._store_list:
                store_name = store["path"].split("/").pop()
                self.__store_names.append(store_name)
                self.__store_dict[store_name] = store["path"]
        
        ## initialize the controllers for each preference tab
        if self.__first_time_init:
            self.__controller_vocabulary = VocabularyAdminController(self.__store_names)
            self.__register_controller(self.__controller_vocabulary, self.TAB_NAME_VOCABULARY)
            
            self.__controller_datestamp = DatestampAdminController(self.__store_names)
            self.__register_controller(self.__controller_datestamp, self.TAB_NAME_DATESTAMP)
    
            self.__controller_expiry_admin = ExpiryAdminController()
            self.__register_controller(self.__controller_expiry_admin, self.TAB_NAME_EXPIRY)

            self.__controller_tag_admin = TagAdminController(self.__store_names)
            self.__register_controller(self.__controller_tag_admin, self.TAB_NAME_TAGS)

            self.__controller_store_admin = StoreAdminController(self.__store_dict)
            self.__register_controller(self.__controller_store_admin, self.TAB_NAME_STORE)

            self.__first_time_init = False
        else:
            self.__controller_vocabulary.set_store_names(self.__store_names)
            self.__controller_datestamp.set_store_names(self.__store_names)
            self.__controller_tag_admin.set_store_names(self.__store_names)
            
        

        ## create a list with one config wrapper for each store
        for store in store_list:
            store_path = store["path"]
            store_name = store_path.split("/").pop()
            
            config_path = "%s/%s/%s" % (store_path, TsConstants.DEFAULT_STORE_CONFIG_DIR, TsConstants.DEFAULT_STORE_CONFIG_FILENAME)
            config = ConfigWrapper(config_path)
            self.__store_config_dict[store_name] = config
            
            voc_path = "%s/%s/%s" % (store_path, TsConstants.DEFAULT_STORE_CONFIG_DIR, TsConstants.DEFAULT_STORE_VOCABULARY_FILENAME)
            voc_wrapper = VocabularyWrapper(voc_path)
            self.__store_vocabulary_wrapper_dict[store_name] = voc_wrapper
            #self.connect(voc_wrapper, QtCore.SIGNAL("changed"), self.__refresh_vocabulary)

            self.__controller_vocabulary.add_setting(TsConstants.SETTING_CATEGORY_MANDATORY, config.get_category_mandatory(), store_name)
            self.__controller_vocabulary.add_setting(TsConstants.SETTING_SHOW_CATEGORY_LINE, config.get_show_category_line(), store_name)
            self.__controller_vocabulary.add_setting(TsConstants.SETTING_CATEGORY_VOCABULARY, voc_wrapper.get_vocabulary(), store_name)
            ## TODO: create a method to switch this from outside
            self.__controller_vocabulary.set_settings_editable(False)
            
            self.__controller_tag_admin.add_setting(TsConstants.SETTING_DESC_TAGS, store["desc_tags"], store_name)
            self.__controller_tag_admin.add_setting(TsConstants.SETTING_CAT_TAGS, store["cat_tags"], store_name)
            self.__controller_tag_admin.add_setting(TsConstants.SETTING_SHOW_CATEGORY_LINE, config.get_show_category_line(), store_name)
            
            self.__controller_datestamp.add_setting(TsConstants.SETTING_DATESTAMP_FORMAT, config.get_datestamp_format(), store_name)

        self.connect(self.__controller_store_admin, QtCore.SIGNAL("new"), self.__handle_new_store)
#        self.connect(self.__controller_store_admin, QtCore.SIGNAL("new"), QtCore.SIGNAL("create_new_store"))
        self.connect(self.__controller_store_admin, QtCore.SIGNAL("rebuild"), self.__handle_rebuild)
        self.connect(self.__controller_store_admin, QtCore.SIGNAL("rename"), self.__handle_rename)
        self.connect(self.__controller_store_admin, QtCore.SIGNAL("delete"), self.__handle_delete)
#        self.connect(controller_tag_admin, QtCore.SIGNAL("rename_desc_tag"), self.__handle_desc_tag_rename)
#        self.connect(controller_tag_admin, QtCore.SIGNAL("rename_cat_tag"), self.__handle_cat_tag_rename)
        self.connect(self.__controller_tag_admin, QtCore.SIGNAL("rename_desc_tag"), QtCore.SIGNAL("rename_desc_tag"))
        self.connect(self.__controller_tag_admin, QtCore.SIGNAL("rename_cat_tag"), QtCore.SIGNAL("rename_cat_tag"))

        ## this setting comes from the main config
        self.__controller_expiry_admin.add_setting(TsConstants.SETTING_EXPIRY_PREFIX, self.__main_config.get_expiry_prefix())
    
    def __handle_desc_tag_rename(self, old, new, store_name):
        self.emit(QtCore.SIGNAL("rename_desc_tag"), old, new, self.__store_dict[store_name])

    def __handle_cat_tag_rename(self, old, new, store_name):
        self.emit(QtCore.SIGNAL("rename_cat_tag"), old, new, self.__store_dict[store_name])
    
    def __refresh_vocabulary(self):
        """
        re-write the vocabulary in all stores 
        """
        for store_name in self.__store_names:
            voc_wrapper = self.__store_vocabulary_wrapper_dict[store_name]
            self.__controller_vocabulary.add_setting(TsConstants.SETTING_CATEGORY_VOCABULARY, voc_wrapper.get_vocabulary(), store_name)
    
    def __handle_new_store(self, path):
        self.emit(QtCore.SIGNAL("create_new_store"), path)
    
    def __handle_rebuild(self, store_name):
        # TODO: 
        pass
    
    def __handle_rename(self, store_name, new_store_name):
        pass
    
    def __handle_delete(self, store_name):
        pass

    def __get_config_for_store(self, store_path):
        """
        returns the config file 
        """

    def __register_controller(self, controller, title):
        """
        use this method to register a new preference controller
        it will be added to the tablist and to the internal controller_list too
        """
        self.__preference_controller_list[str(title)] = controller
        ## add the preference tabs to the preferences window        
        self.__dialog.add_preference_tab(controller.get_view(), title)

    def __handle_save(self):
        ##iterate the controllers 
        self.__log.info("**** CONFIG CHANGES ****")
        self.__log.info("writing to the config files:")
        self.__log.info("**** **** **** **** ****")
        for controller in self.__preference_controller_list.values():
            ## iterate the properties of the controller
            for property in controller.get_settings():
                ## write the properties into the config file
                if property["STORE_NAME"] is not None:
                    ## this is a store specific setting
                    store_config = self.__get_store_config_by_name(property["STORE_NAME"])
                    if property["SETTING_NAME"] == TsConstants.SETTING_DATESTAMP_FORMAT:
                        store_config.set_datestamp_format(property["SETTING_VALUE"])
                    elif property["SETTING_NAME"] == TsConstants.SETTING_SHOW_CATEGORY_LINE:
                        store_config.set_show_category_line(property["SETTING_VALUE"])
                    elif property["SETTING_NAME"] == TsConstants.SETTING_CATEGORY_VOCABULARY:
                        vocabulary_wrapper = self.__store_vocabulary_wrapper_dict[property["STORE_NAME"]]
                        vocabulary_wrapper.set_vocabulary(property["SETTING_VALUE"])
                    elif property["SETTING_NAME"] == TsConstants.SETTING_CATEGORY_MANDATORY:
                        store_config.set_category_mandatory(property["SETTING_VALUE"])
                else:
                    ## this is a general setting  
                    if property["SETTING_NAME"] == TsConstants.SETTING_EXPIRY_PREFIX:
                        self.__main_config.set_expiry_prefix(property["SETTING_VALUE"])
                self.__log.info("%s, setting: %s, value: %s" % (property["STORE_NAME"], 
                                property["SETTING_NAME"], property["SETTING_VALUE"]))
        
        self.__dialog.close()
    
    def __get_store_config_by_name(self, store_name):
        if store_name is not None and store_name != "":
            return self.__store_config_dict[store_name]
        return None
     
    def __handle_cancel(self):
        self.__dialog.close()
    
    def remove_tab(self, tab_name):
        """
        set to true if the controlled vocabulary tab should be shown
        """
        if tab_name is not None and tab_name != "":
            self.__dialog.remove_preference_tab(self.__preference_controller_list[tab_name].get_view())
            
    def select_tab(self, tab_name):
        """
        method to programmatically set the selected tab
        """
        if tab_name is not None and tab_name != "":
            self.__dialog.select_preference_tab(self.__preference_controller_list[str(tab_name)].get_view())
            
    def select_store_admin_tab(self):
        self.select_tab(self.TAB_NAME_STORE)
    
    def set_first_start(self, first_start):
        self.__first_start = first_start
        if first_start:
            self.__main_config.set_first_start(False)
            self.select_store_admin_tab()
            ## TODO: show wizard ...
    
    def set_parent(self, parent):
#        self.__dialog.setParent(parent)
        self.setParent(parent)
    
    def show_tooltip(self, message, parent=None):
        self.__dialog.show_tooltip(message, parent)
    
    def show_dialog(self):
        self.__dialog.show()
        
    def hide_dialog(self):
        self.__dialog.hide()

## end