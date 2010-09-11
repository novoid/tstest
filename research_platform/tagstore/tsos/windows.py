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

import win32com.client

class FileSystem():

    def __init__(self):
        self.__shell = win32com.client.Dispatch("WScript.Shell")
    
    def create_link(self, source, link_name):
        name = link_name.replace("/", "\\") + ".lnk"
        source = source.replace("/", "\\")
        
        shortcut = self.__shell.CreateShortCut(name)
        shortcut.Targetpath = source
        shortcut.save()

    #funktiont
        #shell = win32com.client.Dispatch("WScript.Shell")
        #shortcut = shell.CreateShortCut("C:\\tagstore\\testfolder\\test.xlsx.lnk")
        #shortcut.Targetpath = "C:\\tagstore\\excel.xlsx"
        #shortcut.save() 
                
## end