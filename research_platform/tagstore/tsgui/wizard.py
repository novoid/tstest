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
        self.__wizard.resize(700, 480)
        self.__wizard.setButtonText(self.__wizard.NextButton, 
                                    self.trUtf8("Next"))
        self.__wizard.setButtonText(self.__wizard.BackButton, 
                                    self.trUtf8("Back"))
        self.__wizard.setButtonText(self.__wizard.CancelButton, 
                                    self.trUtf8("Cancle"))
        self.__wizard.setButtonText(self.__wizard.FinishButton, 
                                    self.trUtf8("Finish"))
        
        self.__wizard.addPage(self.__create_welcome_page())
        self.__wizard.addPage(self.__create_intro_page())
        self.__wizard.addPage(self.__create_first_steps_page())
        self.__wizard.addPage(self.__create_setting_page())
        self.__wizard.addPage(self.__create_tagging_page())
        self.__wizard.addPage(self.__create_ending_page())
    
        self.__wizard.setWindowTitle(self.trUtf8("Help Wizard"))
        
    def __create_welcome_page(self):
        page = QtGui.QWizardPage()
        page.setTitle(self.trUtf8("Welcome to Tagstore!"))
    
        label = QtGui.QLabel(self.trUtf8("Danke, dass sie sich f�r tagstore entschieden haben.<br>"
                                         "Sie haben tagstore erfolgreich installiert ..."))
        
        label.setWordWrap(True)
    
        layout = QtGui.QVBoxLayout()
        layout.addWidget(label)
        page.setLayout(layout)
    
        return page
    
    
    def __create_intro_page(self):
        page = QtGui.QWizardPage()
        page.setTitle(self.trUtf8("What is tagstore?"))
    
        text_label = QtGui.QLabel(self.trUtf8("Tagstore ist ein Program, welches dabei helfen soll, Dateien auf dem Computer schneller wieder zu finden.<br>"
                                         "Dies geschiet durch sogenanntes tagging.<br>"
                                         "Tagging ist ein Verfahren, bei dem ein Benutzer einem St�ck Information (z.B: Digitale Bilder, MP3, Videos,..) sogenannte Tags zuordnet.<br>"
                                         "Ein Tag ist ein Schl�sselwort oder Term welcher dabei helfen soll ein St�ck Information zu beschreiben und es dadurch schneller wieder gefunden werden kann.<br>"
                                         "Bei tagstore k�nnen, je nach Einstellung, kategorisierende und/oder beschreibende Tags g/benutzt werden. "))
        text_label.setWordWrap(True)
    
        layout = QtGui.QVBoxLayout()
        layout.addWidget(text_label)
        page.setLayout(layout)
    
        return page
    
    def __create_first_steps_page(self):
        page = QtGui.QWizardPage()
        page.setTitle(self.trUtf8("First steps"))
        
        text_label = QtGui.QLabel(self.trUtf8("Bei dem ersten Start des Programmes, muss ein sogenannter Store angelegt werden. <br>"
                                                "Jeder Benutzer muss zumindest einen Store anlegen. Weitere Stores f�r verschiedene Zwecke (beruflich, privat, Videos, Downloads, ...) k�nnen jederzeit nachtr�glich erstellt werden.<br>"
                                                "Um einen Store anzulegen wird zuerst im tagstore Manager der Tab \"Store-Verwaltung\" ausgew�hlt und danach auf \"Neuer Tagstore\" geklickt. Daraufhin erscheint ein neues Fenster, in welchem ein Ordner ausgew�hlt wird, der den Store beinhalten soll. <br>"
                                                "Wenn ein der Store erfolgreich angelegt wurde, sollte die Stuktur in dem Ordenr, welcher f�r den Store ausgewehlt wurde, so aussehen:"))
        
        image_lable = QtGui.QLabel()
        image_lable.setPixmap(QtGui.QPixmap("./tsresources/images/structure_de.png"))
        
        text_label.setWordWrap(True)
    
        layout = QtGui.QVBoxLayout()
        layout.addWidget(text_label)
        layout.addWidget(image_lable)
        page.setLayout(layout)
    
        return page
    
    
    def __create_ending_page(self):
        page = QtGui.QWizardPage()
        page.setTitle(self.trUtf8("End"))
    
        return page
    
    def __create_setting_page(self):
        page = QtGui.QWizardPage()
        page.setTitle(self.trUtf8("Store settings"))
        
        tab_image_lable = QtGui.QLabel()
        dropdown_image_lable = QtGui.QLabel()
        settings_image_lable = QtGui.QLabel()
        tab_image_lable.setPixmap(QtGui.QPixmap("./tsresources/images/tabs_de.png"))
        dropdown_image_lable.setPixmap(QtGui.QPixmap("./tsresources/images/store_dropdown.png"))
        settings_image_lable.setPixmap(QtGui.QPixmap("./tsresources/images/my_tags_tag_lines_de.png"))
        
        text_label1 = QtGui.QLabel(self.trUtf8("Jeder Store der angelegt wurde, kann �ber den Manager jederzeit konfiguriert werden. Dieser ist in verschiedene Tabs/Reiter unterteilt."))
        text_label2 = QtGui.QLabel(self.trUtf8("Wenn eine Einstellung f�r jeden Store gemacht werden kann, wird der gew�nschte Store �ber ein \"Dropdown Menu\" ausgew�hlt. Dieses befindet sich unter der Beschreibung des Tabs."))
        text_label3 = QtGui.QLabel(self.trUtf8("Unter dem Tab/Reiter \"Meine Tags\" kann f�r jeden Store nachgeschaut werden, ob eine oder zwei Tag-Zeilen verwendet werden. In der erste Tag-Zeile werden beschreibende und in der zweiten kategorisierende Tags verwendet."))
        text_label4 = QtGui.QLabel(self.trUtf8("Wird nur eine Zeile verwendet, kann eingestellt werden, ob in dieser nur vom Benutzter vordefinierte Tags (�Meine Tags�) erlaubt werden oder nicht. Dies soll die Verwendung von �hnlichen Tags verhindern(z.B: Uni, Universit�t). <br>"
                                                      "Solche Tags k�nnen mit einem klick auf \"Hinzuf�gen\" der Liste von Tags hinzugef�gt werden oder mit \"L�schen\" von ihr gel�scht werden. <br>"
                                                      "Werden zwei Zeilen verwendet, kann nur noch f�r die zweite Tag-Zeile eingestellt werden, ob diese nur �meine Tags� verwenden soll."))
        text_label1.setWordWrap(True)
        text_label2.setWordWrap(True)
        text_label3.setWordWrap(True)
        text_label4.setWordWrap(True)
        
        layout = QtGui.QVBoxLayout()
        layout.addWidget(text_label1)
        layout.addWidget(tab_image_lable)
        layout.addWidget(text_label2)
        layout.addWidget(dropdown_image_lable)
        layout.addWidget(text_label3)
        layout.addWidget(settings_image_lable)
        layout.addWidget(text_label4)
        page.setLayout(layout)
        
        return page
    
    def __create_tagging_page(self):
        page = QtGui.QWizardPage()
        page.setTitle(self.trUtf8("Tagging"))
        
        tagging_label = QtGui.QLabel()
        tagging_label.setPixmap(QtGui.QPixmap("./tsresources/image/tagging_de.png"))
        
        text_label1 = QtGui.QLabel(self.trUtf8("Wenn ein neues �Item� (Datei, Ordner) in den Ordner \"Ablage\" hinzugef�gt wird, erscheint der sogenannte \"Tag-Dialog\"."))
        text_label2 = QtGui.QLabel(self.trUtf8("Hier befindet sich eine sogenannte \"Tag-Cloud\"(1), eine Liste der noch nicht getaggten Objekte(2) und je nach Einstellung eine oder zwei Tag-Zeilen mit Vorschl�gen f�r Tags(3). Die erste Tag-Zeile(4) ist f�r beschreibenden und die zweite(5) f�r kategorisierende Tags.<br>"
                                               "Mit einem klick auf \"Tag\"(6) wird das ausgew�hlte Objekt getaggt und ein klick auf \"Manager...\"(7) �ffnet den tagstore Manager.<br>"
                                               "Wenn man ein Objekt nicht sofort taggen will, kann man auf \"Sp�ter bearbeiten\" klicken.<br>"
                                               "Items, die noch nicht getaggt sind, erscheinen so lange in der Liste vom \"Tag-Dialog\", bis entsprechende Items wieder gel�scht werden oder mit Tags versehen wurden."))
                                   
        text_label1.setWordWrap(True)
        text_label2.setWordWrap(True)
        
        layout = QtGui.QVBoxLayout()
        layout.addWidget(text_label1)
        layout.addWidget(tagging_label)
        layout.addWidget(text_label2)
        page.setLayout(layout)
        
        return page
    
    def get_view(self):
        return self.__wizard

## end 