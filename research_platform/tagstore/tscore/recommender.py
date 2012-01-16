#!/usr/bin/env python
# -*- coding: iso-8859-15 -*-
## this file is part of tagstore, an alternative way of storing and retrieving information
## Copyright (C) 2010  Karl Voit, Georg Schober
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
from PyQt4 import QtCore
#from pyPdf import PdfFileReader
#from mutagen.mp3 import MP3

class Recommender(QtCore.QObject):
    '''
    classdocs
    '''

    def __init__(self, store_path):
        '''
        Constructor
        '''
        QtCore.QObject.__init__(self)
        self.store_path = store_path
        
        
    #def get_tag_recommendation(self, tag_wrapper, new_file, number):
    def get_tag_recommendation(self, tag_wrapper, file_name, number, storage_dir_name):    
        '''
        function_explanation
        '''
        extension = self.get_file_extension(file_name)
        dictionary = tag_wrapper.get_tag_dict().copy()
        
        print len(dictionary)
        number_of_tags = 0
        for tag_name, rating in dictionary.iteritems():
            number_of_tags += rating
           
        for tag_name, rating in dictionary.iteritems():
            dictionary[tag_name] = ((rating * 2.0) / number_of_tags)
        
        #meta_dict = self.get_metadata(file_name, extension, storage_dir_name)
        #if meta_dict != None:
        #    for meta_data, rating in meta_dict.iteritems():
        #        if meta_data != None:
        #            self.add_tag_to_dict(dictionary, meta_data, rating)
        dictionary = self.string_matching(file_name, dictionary, extension)
        
        file = self.store_path + "/" + storage_dir_name + "/" + file_name
        if os.path.isdir(file):
        #if file.isdir():
            same_tags_dict = self.rate_tags_from_folder(tag_wrapper, extension, storage_dir_name, tag_wrapper.KEY_TAGS)
            for same_tags, rating in same_tags_dict.iteritems():
                dictionary[same_tags] += rating
        else:   
            same_tags_dict = self.rate_tags_from_same_data_typ(tag_wrapper, extension, tag_wrapper.KEY_TAGS)
            for same_tags, rating in same_tags_dict.iteritems():
                dictionary[same_tags] += rating
                
        if len(dictionary) <= number:
            self.recommend_new_tags(dictionary, extension)
        return dictionary

    def get_cat_recommendation(self, tag_wrapper, file_name, number, storage_dir_name):
        
        '''
        function_explanation
        '''
        extension = self.get_file_extension(file_name)
        dictionary = tag_wrapper.get_tag_dict(tag_wrapper.KEY_CATEGORIES).copy()
        
        number_of_tags = 0
        for tag_name, rating in dictionary.iteritems():
            number_of_tags += rating
           
        for tag_name, rating in dictionary.iteritems():
            dictionary[tag_name] = ((rating * 2.0) / number_of_tags)
        
        #meta_dict = self.get_metadata(file_name, extension, storage_dir_name)
        #if meta_dict != None:
        #    for meta_data, rating in meta_dict.iteritems():
        #        if meta_data != None:
        #            self.add_tag_to_dict(dictionary, meta_data, rating)
        dictionary = self.string_matching(file_name, dictionary, extension)
        
        file = self.store_path + "/" + storage_dir_name + "/" + file_name
        if os.path.isdir(file):
        #if file.isdir():
            same_tags_dict = self.rate_tags_from_folder(tag_wrapper, extension, storage_dir_name, tag_wrapper.KEY_CATEGORIES)
            for same_tags, rating in same_tags_dict.iteritems():
                dictionary[same_tags] += rating
        else:
            same_tags_dict = self.rate_tags_from_same_data_typ(tag_wrapper, extension, tag_wrapper.KEY_CATEGORIES)
            for same_tags, rating in same_tags_dict.iteritems():
                dictionary[same_tags] += rating
        if len(dictionary) <= number:
            self.recommend_new_tags(dictionary, extension)

        return dictionary
        

        
        self.rate_tags_from_same_data_typ(tag_wrapper, extension, tag_wrapper.KEY_CATEGORIES)
        '''
        if len(dictionary) <= number:
            self.recommend_new_tags(dictionary, extension)
        '''
        return dictionary
        
        #self.get_metadata(file_name, extension)

        
    def get_file_extension(self, new_file):
        '''
        returns the file extension
        '''

        point_position = new_file.rfind("."[-4:])#TODO: change 4 into variable
        new_file_extension = ""
        if point_position > 0:
            new_file_extension = new_file
            point_position -= len(new_file)
            new_file_extension = new_file[point_position:]    
        return new_file_extension

    '''
    def get_file_extension(self, new_file):
        
        comma_position = new_file.find(",")
        point_position = new_file.rfind("."[-4:])#TODO: change 4 into variable
        new_file_extension = ""
        if point_position > 0:
            new_file_extension = new_file
            point_position -= len(new_file)
            
            if comma_position > 0:
                comma_position -= len(new_file)
                new_file_extension = new_file[point_position : comma_position]
            else:
                new_file_extension = new_file[point_position:]        
        return new_file_extension
    '''

    def get_file_name(self, new_file):
        new_file_name = new_file
        comma_position = new_file_name.find(",")        
        while comma_position > 0:
            if comma_position > 0:
                comma_position -= (len(new_file_name) - 2)
                new_file_name = new_file_name[comma_position:]
            else:
                new_file_name = new_file_name
            comma_position = new_file_name.find(",")
        
        return new_file_name
    
    
    #TODO: change function-name:
    #def file_extension(self, new_file_name, old_file_dict, tag_dict):     
    
    def rate_tags_from_same_data_typ(self, tag_wrapper, file_extension, attribute):
        '''
        function_explanation
        '''
        #TODO:Check if
        if attribute == tag_wrapper.KEY_CATEGORIES:
            tag_dict = tag_wrapper.get_tag_dict(tag_wrapper.KEY_CATEGORIES).copy()
        else:
            tag_dict = tag_wrapper.get_tag_dict().copy()
            
        for tag_name, rating in tag_dict.iteritems():
            tag_dict[tag_name] = 0
        
        if file_extension not in "":
            file_list = tag_wrapper.get_files()
            
            for file_name in file_list:
            #get_file_tags = returns the describing tag list of a given file
                if file_extension in file_name:
                    if attribute == tag_wrapper.KEY_TAGS:
                        tag_list = tag_wrapper.get_file_tags(file_name)
                    else:
                        tag_list = tag_wrapper.get_file_categories(file_name)
                    for tag in tag_list:
                        tag_dict[tag] += 1
            
            number_of_tags = 0
            #necessary???
            for tag_name, rating in tag_dict.iteritems():
                number_of_tags += rating
            if number_of_tags:
                for tag_name, rating in tag_dict.iteritems():
                    tag_dict[tag_name] = ((rating * 2.5) / number_of_tags)#TODO:check calculation 

        return tag_dict        
  
    def rate_tags_from_folder(self, tag_wrapper, file_extension, storage_dir_name, attribute):
        '''
        function_explanation
        '''
        #TODO:Check if
        if attribute == tag_wrapper.KEY_CATEGORIES:
            tag_dict = tag_wrapper.get_tag_dict(tag_wrapper.KEY_CATEGORIES).copy()
        else:
            tag_dict = tag_wrapper.get_tag_dict().copy()
            
        for tag_name, rating in tag_dict.iteritems():
            tag_dict[tag_name] = 0
        
        file_list = tag_wrapper.get_files()
        
        for file_name in file_list:
        #get_file_tags = returns the describing tag list of a given file
            file = self.store_path + "/" + storage_dir_name + "/" + file_name
            if os.path.isdir(file):
                print file_name
                if attribute == tag_wrapper.KEY_TAGS:
                    tag_list = tag_wrapper.get_file_tags(file_name)
                else:
                    tag_list = tag_wrapper.get_file_categories(file_name)
                for tag in tag_list:
                    tag_dict[tag] += 1
        
        number_of_tags = 0
        #necessary???
        for tag_name, rating in tag_dict.iteritems():
            number_of_tags += rating
        if number_of_tags:
            for tag_name, rating in tag_dict.iteritems():
                tag_dict[tag_name] = ((rating * 2.5) / number_of_tags)#TODO:check calculation 
        return tag_dict   
  
  
    def get_metadata(self, file_name, extension, storage_dir_name):
        '''
        function_explanation
        '''
        #print file_name
        file = self.store_path + "/" + storage_dir_name + "/" + file_name
        #print file
        if ".pdf" in extension:
            return self.get_pdf_metadata(file)
        
        if ".mp3" in extension:
            return self.get_mp3_metadata(file)
        
        #print "anderes Format"
        return
        
        
    '''   
    def get_pdf_metadata(self, pdf):

        #wait while file is being copied
        i = 0
        while i < 100:
            i += 1
        while os.access(pdf, os.R_OK) == False:
            i = 1
        
        #TODO: Translate \\storage\\ and control the path      
        input1 = PdfFileReader(file(pdf, "rb"))
        author = input1.getDocumentInfo().author
        title = input1.getDocumentInfo().title
        subject = input1.getDocumentInfo().subject
        creator = input1.getDocumentInfo().creator
        producer = input1.getDocumentInfo().producer
        dict = {author: 1, title: 0.9, subject: 0.8, creator: 0.3, producer: 0.2}#TODO: werte kontrollieren
        return dict

    def get_mp3_metadata(self, mp3):
      
        #wait while file is being copied
        i = 0
        while i < 100:
            i += 1
        while os.access(mp3, os.R_OK) == False:
            i = 1 
            

        audio = MP3(mp3)
        audio.pprint()
        audio.items()
        songtitle = audio["TIT2"]
        artist = audio["TPE1"]
        album = audio["TALB"]

        dict = {artist[0]: 1, songtitle[0]: 0.8, album[0]: 0.9}#TODO: werte kontrollieren
        return dict
    '''
    
    def string_matching(self, file_name, dictionary, file_extension):
        '''
        function_explanation
        '''
        bool = 0
        
        for tag_name, rating in dictionary.iteritems():
            tmp_rating = self.string_matching2(file_name.upper(), tag_name.upper(), file_extension)
            dictionary[tag_name] = rating + tmp_rating
            if tmp_rating == 1:
                bool = 1
        if bool < 1:
            ext_len = len(file_extension) * -1
            file_name_without_extension = file_name[:ext_len]
            self.add_tag_to_dict(dictionary, file_name_without_extension, 1)
            self.add_tag_to_dict(dictionary, file_extension[1:].upper(), 0.9)
        return dictionary
        
    def string_matching2(self, file_name, tag_name, file_extension):
        '''
        function_explanation
        '''
        if tag_name in file_extension:
            return 1

        if file_name in tag_name:
            return 1
        
        if tag_name in file_name:
            return 1

        rating = self.damerau_levenshtein_distance(file_name, tag_name)
        if len(file_name) != 0 or len(tag_name) != 0:
            rating = 1 - ((rating * 1.0) / max(len(file_name), len(tag_name)))
        else:
            rating = 0
        return rating
              
    def damerau_levenshtein_distance(self, file_name, tag_name):

        
        length1 = len(file_name)
        length2 = len(tag_name)
        
        if length1 < length2:
            string1 = file_name.upper()
            string2 = tag_name.upper()
        else:
            string2 = file_name.upper()
            string1 = tag_name.upper()

        length1 = len(string1)
        length2 = len(string2)

        '''
        controls if DamerauLevenshteinDistance is necessary
        '''
        if length1 <= 2 or length2 <= 2:
            return max(length1, length2)
        if string1 in string2:
            return 0
        d = {}
        
        for i in range(0, length1 + 1):
            d[(i, 0)] = i
        for j in range(-1, length2 + 1):
            d[(0, j)] = j
        '''
        Damerau-Levenshtein Distance:
        '''
        for i in range(1, length1 + 1):
            for j in range(1, length2 + 1):
                if string1[i - 1] == string2[j - 1]:
                    cost = 0
                else:
                    cost = 1
                d[i, j] = min(d[i - 1, j  ] + 1,
                            d[i  , j - 1] + 1,
                            d[i - 1, j - 1] + cost)
                if i > 1 and j > 1 and (string1[i - 1] == string2[j - 2]) and string1[i - 2] == string2[j - 1]:
                    d[i, j] = min(d[i, j], d[i - 1, j - 1] + cost)
                
        return d[length1 - 1, length2 - 1]

        
    def recommend_new_tags(self, dictionary, file_extension):
        '''
        TODO: Change words (translation)
        '''
        
        if file_extension in ".pdf":
            self.add_tag_to_dict(dictionary, self.trUtf8("documents"), 0.06)
            self.add_tag_to_dict(dictionary, self.trUtf8("PDF"), 0.04)
        elif file_extension in (".gif.png.jpg.jpeg"): 
            self.add_tag_to_dict(dictionary, "image", 0.06)
        elif file_extension in (".mkv.avi.flv.mov.mp4.wmv"): 
            self.add_tag_to_dict(dictionary, self.trUtf8("video"), 0.06)
            self.add_tag_to_dict(dictionary, self.trUtf8("movie"), 0.05)
        elif file_extension in ".doc.docx":
            self.add_tag_to_dict(dictionary, self.trUtf8("documents"), 0.06)
            self.add_tag_to_dict(dictionary, self.trUtf8("DOC"), 0.04)
            
        self.add_tag_to_dict(dictionary, self.trUtf8("family"), 0.05)
        self.add_tag_to_dict(dictionary, self.trUtf8("work"), 0.05)
        self.add_tag_to_dict(dictionary, self.trUtf8("holiday"), 0.04)
        self.add_tag_to_dict(dictionary, self.trUtf8("image"), 0.03)
        self.add_tag_to_dict(dictionary, self.trUtf8("documents"), 0.03)
        self.add_tag_to_dict(dictionary, self.trUtf8("music"), 0.035)
        self.add_tag_to_dict(dictionary, self.trUtf8("video"), 0.035)
    
    def add_tag_to_dict(self, dictionary, tag, rating):
        if str(tag) not in dictionary:
            dictionary.setdefault(tag, rating)
## end
