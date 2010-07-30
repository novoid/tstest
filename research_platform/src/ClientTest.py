'''
Created on Jul 29, 2010

@author: chris
'''
import sys
from PyQt4.QtCore import QObject, SIGNAL, Qt
from PyQt4.QtGui import QTextBrowser, QDialog, QLabel, QVBoxLayout
from PyQt4.Qt import QApplication
from tswidget.TagStoreWidget import TagStoreCompleter, TagStoreLineEdit

class TestForm(QDialog):
    '''
    class to build a simple dialog for testing purpose
    '''

    def __init__(self, parent = None):
        '''
        Constructor
        '''
        super(TestForm, self).__init__(parent)
        
        self.textarea = QTextBrowser()
        self.textarea.setText("This is a NON-EDITABLE textarea ... \n just for testing purpose")
        self.tag_line = TagStoreLineEdit("First test with PyQt")
        self.tag_line.selectAll()
        
        self.label = QLabel("check out multiple tag suggestions")

        # TODO: temporary taglist for testing
        # TODO: get the real tags from TagStoreLogic
        wList = ['Chris', 'Karl', 'Wolfgang', 'TUG', 'DA']
        
        completer = TagStoreCompleter(wList, self.tag_line);
        completer.setCaseSensitivity(Qt.CaseInsensitive)
        QObject.connect(self.tag_line, SIGNAL("text_changed(PyQt_PyObject, PyQt_PyObject)"), completer.update)
        QObject.connect(completer, SIGNAL("activated(QString)"), self.tag_line.complete_text)
        
        completer.setWidget(self.tag_line)

        #self.tag_line.setCompleter(completer);

        layout = QVBoxLayout()
        layout.addWidget(self.textarea)
        layout.addWidget(self.label)
        layout.addWidget(self.tag_line)
        
        self.setLayout(layout)

        self.tag_line.setFocus()
        #self.connect(self.tag_line, SIGNAL("returnPressed()"), self.updateUi)
        self.setWindowTitle("Hello PyQt")
        

app = QApplication(sys.argv)
form = TestForm()
form.show()

sys.exit(app.exec_()) 