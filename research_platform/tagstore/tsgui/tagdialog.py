# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'tagAfile.ui'
#
# Created: Tue Aug 24 17:10:13 2010
#      by: PyQt4 UI code generator 4.7.4
#
# WARNING! All changes made in this file will be lost!
import logging.handlers
from PyQt4 import QtCore, QtGui
import tsresources.resources
from tagcompleter import TagCompleterWidget
from tscore.tsconstants import TsConstants

class TagDialog(QtGui.QWidget):
    """
    The main gui window for tagging files in several tagstores
    """
    __NO_OF_ITEMS_STRING = "untagged item(s)"
    
    def __init__(self, max_tags, tag_separator, parent=None):
        
        QtGui.QWidget.__init__(self, parent)
        
        self.__log = logging.getLogger("TagStoreLogger")
        
        self.__max_tags = max_tags
        self.__tag_separator = tag_separator
        ## flag to recognize the current visibility state 
        self.__is_shown = False
        self.__selected_index = None
        
        self.setObjectName("TagDialog")
        self.setWindowModality(QtCore.Qt.WindowModal)
        self.resize(567, 256)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.sizePolicy().hasHeightForWidth())
        self.setSizePolicy(sizePolicy)
        self.setContextMenuPolicy(QtCore.Qt.NoContextMenu)
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(":/ts/images/icon.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.setWindowIcon(icon)
        #self.setModal(True)
        self.__store_label = QtGui.QLabel(self)
        self.__store_label.setGeometry(QtCore.QRect(241, 11, 331, 29))
        font = QtGui.QFont()
        font.setPointSize(25)
        font.setWeight(75)
        font.setBold(True)
        self.__store_label.setFont(font)
        self.__store_label.setObjectName("__store_label")
        self.__close_button = QtGui.QPushButton(self)
        self.__close_button.setGeometry(QtCore.QRect(450, 210, 113, 32))
        self.__close_button.setObjectName("__close_button")
        self.__right_frame = QtGui.QFrame(self)
        self.__right_frame.setGeometry(QtCore.QRect(220, 60, 331, 141))
        self.__right_frame.setFrameShape(QtGui.QFrame.StyledPanel)
        self.__right_frame.setFrameShadow(QtGui.QFrame.Raised)
        self.__right_frame.setObjectName("__right_frame")
        self.__recent_label = QtGui.QLabel(self.__right_frame)
        self.__recent_label.setGeometry(QtCore.QRect(10, 40, 91, 21))
        self.__recent_label.setObjectName("__recent_label")
        self.__tag_button = QtGui.QPushButton(self.__right_frame)
        self.__tag_button.setGeometry(QtCore.QRect(250, 100, 61, 21))
        icon1 = QtGui.QIcon()
        icon1.addPixmap(QtGui.QPixmap(":/ts/images/accept.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.__tag_button.setIcon(icon1)
        self.__tag_button.setObjectName("__tag_button")
        
        self.__tag_line_widget = TagCompleterWidget(self.__max_tags, separator=self.__tag_separator, parent=self.__right_frame)
        self.__tag_line = self.__tag_line_widget.get_tag_line()
        #self.__tag_line_widget.set_size(QtCore.QRect(10, 100, 231, 22))
        self.__tag_line.setGeometry(QtCore.QRect(10, 100, 231, 22))
        self.__tag_line.setObjectName("__tag_line_widget")
        self.__tag_line_widget.set_enabled(False)
        
        self.__popular_label = QtGui.QLabel(self.__right_frame)
        self.__popular_label.setGeometry(QtCore.QRect(10, 10, 91, 21))
        self.__popular_label.setObjectName("__popular_label")
        self.__tag_label = QtGui.QLabel(self.__right_frame)
        self.__tag_label.setGeometry(QtCore.QRect(10, 70, 200, 21))
        self.__tag_label.setObjectName("__tag_label")
        self.__popular_value_label = QtGui.QLabel(self.__right_frame)
        self.__popular_value_label.setGeometry(QtCore.QRect(110, 13, 211, 16))
        self.__popular_value_label.setObjectName("__popular_value_label")
        self.__recent_value_label = QtGui.QLabel(self.__right_frame)
        self.__recent_value_label.setGeometry(QtCore.QRect(110, 42, 211, 16))
        self.__recent_value_label.setObjectName("__recent_value_label")
        self.__remove_button = QtGui.QPushButton(self)
        self.__remove_button.setGeometry(QtCore.QRect(10, 210, 31, 32))
        font = QtGui.QFont()
        font.setPointSize(13)
        font.setWeight(50)
        font.setBold(False)
        self.__remove_button.setFont(font)
        self.__remove_button.setText("")
        icon2 = QtGui.QIcon()
        icon2.addPixmap(QtGui.QPixmap(":/ts/images/delete.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.__remove_button.setIcon(icon2)
        self.__remove_button.setObjectName("__remove_button")
        self.__info_label = QtGui.QLabel(self)
        self.__info_label.setGeometry(QtCore.QRect(20, 20, 201, 31))
        self.__info_label.setScaledContents(False)
        self.__info_label.setWordWrap(True)
        self.__info_label.setObjectName("__info_label")
        self.__item_list_view = QtGui.QTreeView(self)
        self.__item_list_view.setGeometry(QtCore.QRect(10, 60, 201, 141))
        self.__item_list_view.setObjectName("__item_list_view")
        self.__item_list_view.setHeaderHidden(True)
        self.__item_list_view.setModel(QtGui.QStandardItemModel())
        self.retranslateUi()

        self.connect(self.__tag_button, QtCore.SIGNAL("clicked()"), self.__tag_button_pressed)
        self.connect(self.__remove_button, QtCore.SIGNAL("clicked()"), self.__remove_button_pressed)
        #self.connect(self.__item_list_view.selectionModel(), QtCore.SIGNAL("selectionChanged(QModelIndex)"), self.__handle_tree_clicked)
        self.connect(self.__item_list_view.selectionModel(), QtCore.SIGNAL("selectionChanged(QItemSelection, QItemSelection)"), self.__handle_tree_clicked)
        self.connect(self.__close_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("cancel_clicked()"))
        #self.connect(self.__close_button, QtCore.SIGNAL("clicked()"), self.cancel)

    def __handle_tree_clicked(self, index):
        self.__selected_index = index
        ## check if the selected item is a "store-item"
        if self.__get_selected_item().parent() is None:
            self.__tag_line_widget.set_enabled(False)
            return
        item = self.__item_list_view.model().itemFromIndex(index)
        self.__tag_line_widget.set_enabled(True)
        print "selected %s" % item.text()
        
    def __handle_tree_clicked(self, new_index, old_index):
        index_list = new_index.indexes()
        if index_list is not None and len(index_list) == 1:
            self.__selected_index = index_list[0]
        else:
            pass
        ## check if the selected item is a "store-item"
        if self.__get_selected_item().parent() is None:
            self.__tag_line_widget.set_enabled(False)
            return
        self.__tag_line_widget.set_enabled(True)
        self.__log.debug("selected %s" % self.__get_selected_item().text())

    def __get_selected_item(self):
        return self.__item_list_view.model().itemFromIndex(self.__selected_index)

    def __get_selected_store(self):
        return self.__get_selected_item().parent()

    def __remove_item_from_list(self, item):
        if item is None:
            return
        item.parent().removeRow(item.row())
        self.__item_list_view.doItemsLayout()

    def __remove_button_pressed(self):
        """
        remove the selected item from the list - and do internal handling ....
        """
        # TODO: define what to do, if an item is to be ignored
        self.__remove_item_from_list(self.__get_selected_item())
        
    def __tag_button_pressed(self):
        """
        write the typed tags to the tagstore
        """
        if self.__selected_index is None:
            QtGui.QMessageBox.information(self, "No Item selected", "Please select an Item, to which the tags should be added")
            return
        self.emit(QtCore.SIGNAL("tag_button_pressed"), self.__get_selected_store(), self.__get_selected_item(), self.__tag_line_widget.get_tag_list())
        self.__remove_item_from_list(self.__get_selected_item())
        self.__tag_line_widget.set_text("")
        
    def retranslateUi(self):
        self.setWindowTitle(QtGui.QApplication.translate("Dialog", "Tag-A-File - tagstore", None, QtGui.QApplication.UnicodeUTF8))
        self.__store_label.setText(QtGui.QApplication.translate("Dialog", "@ store1", None, QtGui.QApplication.UnicodeUTF8))
        self.__close_button.setText(QtGui.QApplication.translate("Dialog", "Close", None, QtGui.QApplication.UnicodeUTF8))
        self.__recent_label.setText(QtGui.QApplication.translate("Dialog", "Recent:", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_button.setText(QtGui.QApplication.translate("Dialog", "Tag It", None, QtGui.QApplication.UnicodeUTF8))
        self.__popular_label.setText(QtGui.QApplication.translate("Dialog", "Most popular:", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_label.setText(QtGui.QApplication.translate("Dialog", "Tags (use '%s' between tags)" % self.__tag_separator, 
            None, QtGui.QApplication.UnicodeUTF8))
        self.__popular_value_label.setText(QtGui.QApplication.translate("Dialog", "TextLabel", None, QtGui.QApplication.UnicodeUTF8))
        self.__recent_value_label.setText(QtGui.QApplication.translate("Dialog", "TextLabel", None, QtGui.QApplication.UnicodeUTF8))
        self.__remove_button.setToolTip(QtGui.QApplication.translate("Dialog", "remove file from list", None, QtGui.QApplication.UnicodeUTF8))
        self.__info_label.setText(QtGui.QApplication.translate("Dialog", "All these items have not been tagged yet", None, QtGui.QApplication.UnicodeUTF8))

    def set_store_label_text(self, storename):
        self.__store_label.setText("@ " + storename)
        
    def set_item_list(self, item_list):
        model = QtGui.QStringListModel()
        model.setStringList(item_list)
        self.__item_list_view.setModel(model)

    def add_item(self, store_name, item_name):
        """
        add a new item to the tree view
        """
        model = self.__item_list_view.model()
        
        ## there should just be one result, but the method returns a list
        store_items = model.findItems(store_name) 
        
        ## create a new store_item if it is not already in the list
        if store_items is None or len(store_items) == 0:
            root_item = model.invisibleRootItem()
            new_store_item = QtGui.QStandardItem(store_name)
            new_store_item.setSelectable(False)
            new_store_item.setEditable(False)
            root_item.appendRow(new_store_item)
            ## put the new one on forst place - because it will be accessed on this position later on
            store_items.insert(0, new_store_item)

        store_item = store_items[0]
        new_item = QtGui.QStandardItem(item_name)
        new_item.setEditable(False)
        
        store_item.appendRow(new_item)
        
        ## select the new item automatically in the treeview
        self.__item_list_view.selectionModel().select(new_item.index(), QtGui.QItemSelectionModel.ClearAndSelect)
        self.__tag_line_widget.set_enabled(True)
        self.__tag_line_widget.set_line_focus()
        self.__item_list_view.expandAll()
        
    def set_tag_list(self, tag_list):
        self.__tag_line_widget.set_tag_completion_list(tag_list)
        
    def set_popular_tags(self, tag_list):
        self.__popular_value_label.setText("")
        for tag in tag_list:
            self.__popular_value_label.setText(self.__popular_value_label.text() +" "+ tag)

    def set_recent_tags(self, tag_list):
        self.__recent_value_label.setText("")
        for tag in tag_list:
            self.__recent_value_label.setText(self.__recent_value_label.text() +" "+ tag)
    
    def clear_store_children(self, store_name):
        model = self.__item_list_view.model()
        store_items = model.findItems(store_name)
        
        if store_items is not None and len(store_items) > 0:
            store = store_items[0]
            rowcount = store.rowCount()
            store.removeRows(0, rowcount)
        
    def clear_tree_view(self):
        self.__item_list_view.model().clear()

class TagDialogController(QtCore.QObject):
    """
    __pyqtSignals__ = ("tag_item",
                       "handle_cancel") 
    """
    def __init__(self, max_tags, tag_separator):
        
        self.__max_tags = max_tags
        
        QtCore.QObject.__init__(self)
        
        self.__log = logging.getLogger("TagStoreLogger")
        self.__tag_separator = tag_separator
        self.__tag_dialog = TagDialog(self.__max_tags, self.__tag_separator)
        
        self.__is_shown = False
        
        self.connect(self.__tag_dialog, QtCore.SIGNAL("tag_button_pressed"), QtCore.SIGNAL("tag_item"))
        self.connect(self.__tag_dialog, QtCore.SIGNAL("cancel_clicked()"), QtCore.SIGNAL("handle_cancel()"))
        
    def add_pending_item(self, store_name, file_name):
        #self.__tag_dialog.set_store_label_text(store_name)
        self.__tag_dialog.add_item(store_name, file_name)
        
    def set_tag_list(self, tag_list):
        self.__tag_dialog.set_tag_list(tag_list)    

    def clear_all_items(self):
        """
        remove all pending items from the tree_view
        """
        self.__tag_dialog.clear_tree_view()    
        
    def clear_store_children(self, store_name):
        self.__tag_dialog.clear_store_children(store_name)

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
        self.__tag_dialog.hide()
        self.__log.debug("hide tag-dialog")
        
    def set_popular_tags(self, tag_list):
        self.__tag_dialog.set_popular_tags(tag_list)

    def set_recent_tags(self, tag_list):
        self.__tag_dialog.set_recent_tags(tag_list)
        
    def set_store_name(self, store_name):
        self.__tag_dialog.set_store_label_text(store_name)
## END