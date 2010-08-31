# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'tagAfile.ui'
#
# Created: Tue Aug 24 17:10:13 2010
#      by: PyQt4 UI code generator 4.7.4
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui
import tsresources.resources
from tagcompleter import TagCompleterWidget

class TagDialog(QtGui.QWidget):
    """
    The main gui window for tagging files in several tagstores
    """
    __NO_OF_ITEMS_STRING = "untagged item(s)"
    
    def __init__(self, parent=None):
        
        QtGui.QWidget.__init__(self, parent)
        
        ## flag to recognize the current visibility state 
        self.__is_shown = False
        
        self.setObjectName("TagDialog")
        self.resize(594, 364)
        self.setAcceptDrops(False)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.sizePolicy().hasHeightForWidth())
        self.setSizePolicy(sizePolicy)
        self.setContextMenuPolicy(QtCore.Qt.NoContextMenu)
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(":/ts/images/icon.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.setWindowIcon(icon)
        #.setModal(True)
        
        #self.__item_list_view = QtGui.QListView(self)
        self.__item_list_view = QtGui.QTreeView(self)
        self.__item_list_view.setHeaderHidden(True)
        self.__item_list_view.setGeometry(QtCore.QRect(20, 60, 211, 241))
        self.__item_list_view.setObjectName("__item_list_view")
        
        self.__selected_index = None

        model = QtGui.QStandardItemModel()
        self.__item_list_view.setModel(model)
        
        self.__cancel_button = QtGui.QPushButton(self)
        self.__cancel_button.setGeometry(QtCore.QRect(470, 320, 113, 32))
        self.__cancel_button.setObjectName("__cancel_button")
        self.__right_frame = QtGui.QFrame(self)
        self.__right_frame.setGeometry(QtCore.QRect(240, 60, 341, 241))
        self.__right_frame.setFrameShape(QtGui.QFrame.StyledPanel)
        self.__right_frame.setFrameShadow(QtGui.QFrame.Raised)
        self.__right_frame.setObjectName("__right_frame")
        self.__recent_label = QtGui.QLabel(self.__right_frame)
        self.__recent_label.setGeometry(QtCore.QRect(20, 90, 91, 21))
        self.__recent_label.setObjectName("__recent_label")
        self.__tag_button = QtGui.QPushButton(self.__right_frame)
        
        self.__tag_line_widget = TagCompleterWidget(parent=self.__right_frame)
        self.__tag_line_widget.setGeometry(QtCore.QRect(100, 160, 231, 22))        
        self.__tag_line_widget.set_enabled(False)
        
        self.__tag_button.setGeometry(QtCore.QRect(220, 190, 113, 32))
        icon1 = QtGui.QIcon()
        icon1.addPixmap(QtGui.QPixmap(":/ts/images/accept.png"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.__tag_button.setIcon(icon1)
        self.__tag_button.setObjectName("__tag_button")

        
        self.__popular_label = QtGui.QLabel(self.__right_frame)
        self.__popular_label.setGeometry(QtCore.QRect(20, 40, 91, 21))
        self.__popular_label.setObjectName("__popular_label")
        self.__tag_label = QtGui.QLabel(self.__right_frame)
        self.__tag_label.setGeometry(QtCore.QRect(20, 160, 61, 21))
        self.__tag_label.setObjectName("__tag_label")
        self.__dummy_label_widget = QtGui.QWidget(self.__right_frame)
        self.__dummy_label_widget.setGeometry(QtCore.QRect(115, 40, 211, 20))
        self.__dummy_label_widget.setObjectName("__dummy_label_widget")
        self.popular_horizontal_layout = QtGui.QHBoxLayout(self.__dummy_label_widget)
        self.popular_horizontal_layout.setObjectName("popular_horizontal_layout")
        self.label_8 = QtGui.QLabel(self.__dummy_label_widget)
        font = QtGui.QFont()
        font.setWeight(75)
        font.setItalic(True)
        font.setUnderline(False)
        font.setBold(True)
        self.label_8.setFont(font)
        self.label_8.setObjectName("label_8")
        self.popular_horizontal_layout.addWidget(self.label_8)
        self.label_9 = QtGui.QLabel(self.__dummy_label_widget)
        font = QtGui.QFont()
        font.setWeight(75)
        font.setItalic(True)
        font.setUnderline(False)
        font.setBold(True)
        self.label_9.setFont(font)
        self.label_9.setObjectName("label_9")
        self.popular_horizontal_layout.addWidget(self.label_9)
        self.label_10 = QtGui.QLabel(self.__dummy_label_widget)
        font = QtGui.QFont()
        font.setWeight(75)
        font.setItalic(True)
        font.setUnderline(False)
        font.setBold(True)
        self.label_10.setFont(font)
        self.label_10.setObjectName("label_10")
        self.popular_horizontal_layout.addWidget(self.label_10)
        self.__dummy_latest_widget = QtGui.QWidget(self.__right_frame)
        self.__dummy_latest_widget.setGeometry(QtCore.QRect(115, 90, 211, 20))
        self.__dummy_latest_widget.setObjectName("__dummy_latest_widget")
        self.latest_horizontal_layout = QtGui.QHBoxLayout(self.__dummy_latest_widget)
        self.latest_horizontal_layout.setObjectName("latest_horizontal_layout")
        self.recent_label_1 = QtGui.QLabel(self.__dummy_latest_widget)
        font = QtGui.QFont()
        font.setWeight(75)
        font.setItalic(True)
        font.setUnderline(False)
        font.setBold(True)
        self.recent_label_1.setFont(font)
        self.recent_label_1.setObjectName("recent_label_1")
        self.latest_horizontal_layout.addWidget(self.recent_label_1)
        self.label_11 = QtGui.QLabel(self.__dummy_latest_widget)
        font = QtGui.QFont()
        font.setWeight(75)
        font.setItalic(True)
        font.setUnderline(False)
        font.setBold(True)
        self.label_11.setFont(font)
        self.label_11.setObjectName("label_11")
        self.latest_horizontal_layout.addWidget(self.label_11)
        self.label_13 = QtGui.QLabel(self.__dummy_latest_widget)
        font = QtGui.QFont()
        font.setWeight(75)
        font.setItalic(True)
        font.setUnderline(False)
        font.setBold(True)
        self.label_13.setFont(font)
        self.label_13.setObjectName("label_13")
        self.latest_horizontal_layout.addWidget(self.label_13)
        self.__remove_button = QtGui.QPushButton(self)
        self.__remove_button.setGeometry(QtCore.QRect(20, 310, 31, 32))
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
        self.no_of_items_label = QtGui.QLabel(self)
        self.no_of_items_label.setGeometry(QtCore.QRect(110, 315, 121, 21))
        self.no_of_items_label.setObjectName("no_of_items_label")
        self.label_2 = QtGui.QLabel(self)
        self.label_2.setGeometry(QtCore.QRect(20, 20, 201, 31))
        self.label_2.setScaledContents(False)
        self.label_2.setWordWrap(True)
        self.label_2.setObjectName("label_2")
        self.store_label = QtGui.QLabel(self)
        self.store_label.setGeometry(QtCore.QRect(241, 11, 331, 29));
        font = QtGui.QFont()
        font.setPointSize(25)
        font.setWeight(75)
        font.setBold(True)
        self.store_label.setFont(font)
        self.store_label.setObjectName("store_label")

        self.retranslateUi()

        self.connect(self.__tag_button, QtCore.SIGNAL("clicked()"), self.__tag_button_pressed)
        self.connect(self.__remove_button, QtCore.SIGNAL("clicked()"), self.__remove_button_pressed)
        self.connect(self.__item_list_view, QtCore.SIGNAL("clicked(QModelIndex)"), self.__handle_tree_clicked)
        self.connect(self.__cancel_button, QtCore.SIGNAL("clicked()"), QtCore.SIGNAL("cancel_clicked()"))
        #self.connect(self.__cancel_button, QtCore.SIGNAL("clicked()"), self.cancel)

    def __handle_tree_clicked(self, index):
        self.__selected_index = index
        ## check if the selected item is a "store-item"
        if self.__get_selected_item().parent() is None:
            self.__tag_line_widget.set_enabled(False)
            return
        item = self.__item_list_view.model().itemFromIndex(index)
        self.__tag_line_widget.set_enabled(True)
        print "click %s" % item.text()

    def __get_selected_item(self):
        return self.__item_list_view.model().itemFromIndex(self.__selected_index)

    def __get_selected_store(self):
        return self.__get_selected_item().parent()

    def __remove_item_from_list(self, item):
        if item is None:
            return
        item.parent().removeRow(item.row())
        self.__item_list_view.doItemsLayout()
        # TODO is this good usability?
        self.__item_list_view.clearSelection()

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
        self.setWindowTitle(QtGui.QApplication.translate("TagDialog", "Tag-A-File - tagstore", None, QtGui.QApplication.UnicodeUTF8))
        self.__cancel_button.setText(QtGui.QApplication.translate("TagDialog", "Cancel", None, QtGui.QApplication.UnicodeUTF8))
        self.__recent_label.setText(QtGui.QApplication.translate("TagDialog", "Recent:", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_button.setText(QtGui.QApplication.translate("TagDialog", "Tag It", None, QtGui.QApplication.UnicodeUTF8))
        self.__popular_label.setText(QtGui.QApplication.translate("TagDialog", "Most popular:", None, QtGui.QApplication.UnicodeUTF8))
        self.__tag_label.setText(QtGui.QApplication.translate("TagDialog", "Tags:", None, QtGui.QApplication.UnicodeUTF8))
        self.label_8.setText(QtGui.QApplication.translate("TagDialog", "label1", None, QtGui.QApplication.UnicodeUTF8))
        self.label_9.setText(QtGui.QApplication.translate("TagDialog", "label2", None, QtGui.QApplication.UnicodeUTF8))
        self.label_10.setText(QtGui.QApplication.translate("TagDialog", "label3", None, QtGui.QApplication.UnicodeUTF8))
        self.recent_label_1.setText(QtGui.QApplication.translate("TagDialog", "label1", None, QtGui.QApplication.UnicodeUTF8))
        self.label_11.setText(QtGui.QApplication.translate("TagDialog", "label2", None, QtGui.QApplication.UnicodeUTF8))
        self.label_13.setText(QtGui.QApplication.translate("TagDialog", "label3", None, QtGui.QApplication.UnicodeUTF8))
        self.__remove_button.setToolTip(QtGui.QApplication.translate("TagDialog", "remove file from list", None, QtGui.QApplication.UnicodeUTF8))
        self.no_of_items_label.setText(QtGui.QApplication.translate("TagDialog", self.__NO_OF_ITEMS_STRING, None, QtGui.QApplication.UnicodeUTF8))
        self.label_2.setText(QtGui.QApplication.translate("TagDialog", "All these items have not been tagged yet", None, QtGui.QApplication.UnicodeUTF8))
        self.store_label.setText(QtGui.QApplication.translate("TagDialog", "@ store1", None, QtGui.QApplication.UnicodeUTF8))

    def set_store_label_text(self, storename):
        self.store_label.setText("@ " + storename)
        
    def set_item_list(self, item_list):
        model = QtGui.QStringListModel()
        model.setStringList(item_list)
        self.__item_list_view.setModel(model)
        self.no_of_items_label.setText("%s %s" %(len(item_list), self.__NO_OF_ITEMS_STRING))

    def add_item(self, store_name, item_name):
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
        
        ## now check if there is already such an item name in the treeview
        #file_item = model.findItems(item_name)
         
        store_item = store_items[0]
        new_item = QtGui.QStandardItem(item_name)
        new_item.setEditable(False)
        store_item.appendRow(new_item)
        
        self.__item_list_view.expandAll()
        
        #self.no_of_items_label.setText("%s %s" %(len(item_list), self.__NO_OF_ITEMS_STRING))
        
    def set_tag_list(self, tag_list):
        self.__tag_line_widget.set_tag_completion_list(tag_list)
        
    def set_popular_tags(self, tag_list):
        pass

    def set_recent_tags(self, tag_list):
        for tag in tag_list:
            label = QtGui.QLabel(self.__dummy_latest_widget)
            label.setText("tag")
            self.latest_horizontal_layout.addWidget(label)
    
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
    def __init__(self):
        
        QtCore.QObject.__init__(self)
        
        self.__tag_dialog = TagDialog()
        
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
        print "SHOW"
        
    def hide_dialog(self):
        if not self.__is_shown:
            return
        self.__is_shown = False
        self.__tag_dialog.hide()
        
    def set_popular_tags(self, tag_list):
        self.__tag_dialog.set_popular_tags(tag_list)

    def set_recent_tags(self, tag_list):
        self.__tag_dialog.set_recent_tags(tag_list)
    def get_dialog(self):
        return self.__tag_dialog
## END