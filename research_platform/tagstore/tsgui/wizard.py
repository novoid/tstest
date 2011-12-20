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

from PyQt4 import QtGui, QtCore, Qt

class Wizard(QtGui.QWizard):


    def __init__(self, parent = None):
        '''
        Constructor
        '''
        QtGui.QWizard.__init__(self, parent)

        self.__wizard = QtGui.QWizard()
        self.__wizard.addPage(self.__create_intor_page())
        self.__wizard.addPage(self.__createRegistrationPage())
        self.__wizard.addPage(self.__create_ending_page())
    
        self.__wizard.setWindowTitle(self.trUtf8("Help Wizard"))
        
    def __create_intor_page(self):
        self.__page = QtGui.QWizardPage()
        self.__page.setTitle(self.trUtf8("Introduction"))
    
        self.__label = QtGui.QLabel(self.trUtf8("This wizard will show you, "
                                                "how to handle this program"))
        
        self.__label.setWordWrap(True)
    
        self.__layout = QtGui.QVBoxLayout()
        self.__layout.addWidget(self.__label)
        self.__page.setLayout(self.__layout)
    
        return self.__page
    
    
    def __createRegistrationPage(self):
        self.__page = QtGui.QWizardPage()
        self.__page.setTitle(self.trUtf8("Step"))
        self.__page.setSubTitle("Sub Title")
    
        self.__text_label = QtGui.QLabel(self.trUtf8("Between Start an End!"))
        self.__text_label.setWordWrap(True)
        self.__image_lable = QtGui.QLabel()
        self.__image = QtGui.QPixmap("./tsresources/images/help.png")
        self.__image_lable.setPixmap(self.__image)
    
        self.__layout = QtGui.QVBoxLayout()
        self.__layout.addWidget(self.__text_label)
        self.__layout.addWidget(self.__image_lable)
        self.__layout
        self.__page.setLayout(self.__layout)
    
        return self.__page
    
    
    def __create_ending_page(self):
        self.__page = QtGui.QWizardPage()
        self.__page.setTitle(self.trUtf8("End"))
    
        self.__label = QtGui.QLabel(self.trUtf8("Thank you for using this "
                                                "program. Have a nice day!"))
        self.__label.setWordWrap(True)
    
        self.__layout = QtGui.QVBoxLayout()
        self.__layout.addWidget(self.__label)
        self.__page.setLayout(self.__layout)
    
        return self.__page
    
    def get_view(self):
        return self.__wizard

## end 