#!/usr/bin/env python
# -*- coding: iso-8859-15 -*-

## author: Karl Voit
##
## version: v0.1, 20090310   initial version
##
## usage:
## $0 AFILE TAG1 ... TAGn
##
## notes:
##  * AFILE does not need absolute path; the path from config-file will be added to basename of AFILE
##  * at least ONE tag is expected (else error)
##  * config-file ~/.config/tagstore.cfg is expected (else error)
##  * if the tag "tsdryrun" is found within tags from command line arguments, no action is executed; just print out what *would* be done to stdout
##

__version__ = "$Revision: 68852 $"

import os   ## os.getcwd()
import sys  ## sys.argv
import ConfigParser  ## http://docs.python.org/library/configparser.html
from datetime import datetime, date, time
import re   ## regular expressions

class TS_NotEnoughParametersException(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)

class TS_TooMuchTagsException(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)

class TS_DirectoryNotFoundException(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)

class TS_FileNotFoundException(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)

## print out message for dryrun-mode:
def dryrunprint(message):
    print "DRYRUN: " + message

PAR_FILENAME = sys.argv[1]
taglist = sys.argv[2:]
CONFIGFILENAME = os.path.expanduser('~')+'/.config/tagstore.cfg'
DRYRUN=False

## error, if config-file is not found
if not os.path.isfile( CONFIGFILENAME ):
    raise TS_FileNotFoundException( CONFIGFILENAME )

## generate all permutations
def generate_permutation(str):
    if len(str) <=1:
        yield str
    else:
        for perm in generate_permutation(str[1:]):
            for i in range(len(perm)+1):
                yield perm[:i] + str[0:1] + perm[i:]

## basic output of infos
print "file to link: [" + PAR_FILENAME + "]"
print "found", len(taglist), "tags:", taglist

## check argument number
if len(taglist) == 0:
    raise TS_NotEnoughParametersException

## FIXXME: remove multiple tags

## read out configuration file:
if not os.path.isfile( CONFIGFILENAME ):
    raise TS_FileNotFoundException( CONFIGFILENAME )
config = ConfigParser.ConfigParser()
config.readfp(open(CONFIGFILENAME))
FILEDIR=config.get('General', 'FILEDIR')
TAGDIR=config.get('General', 'TAGDIR')
METADATADIR=config.get('General', 'METADATADIR')
ADDDATESTAMP=config.getboolean('Tagdialog', 'ADDDATESTAMP')
DATESTAMPFORMAT=config.get('Tagdialog', 'DATESTAMPFORMAT')
OMIT_DATESTAMP_IF_DATESTAMP_IS_ENTERED_BY_USER=config.getboolean('Tagdialog', 'OMIT_DATESTAMP_IF_DATESTAMP_IS_ENTERED_BY_USER')
TAGLIMIT=config.getint('Tagdialog', 'TAGLIMIT')

## FIXXME: Error, if DATESTAMPFORMAT is not recognised

## error, if FILEDIR is not found
if not os.path.isdir( FILEDIR ):
    raise TS_FileNotFoundException( FILEDIR )

## error, if TAGDIR is not found
if not os.path.isdir( TAGDIR ):
    raise TS_FileNotFoundException( TAGDIR )

## error, if METADATADIR is not found
if not os.path.isdir( METADATADIR ):
    raise TS_FileNotFoundException( METADATADIR )

## error, if in FILEDIR a PAR_FILENAME is not found
absolutelinkname = os.path.join(FILEDIR, os.path.basename(PAR_FILENAME) )
baselinkname = os.path.basename(PAR_FILENAME)

## FIXXME: when dir provided and not found, raise a DirNotFount-Exception
if not ( os.path.isfile( absolutelinkname ) or os.path.isdir( absolutelinkname ) ):
    raise TS_FileNotFoundException( absolutelinkname )

## check, if tsdryrun is found within taglist
if "tsdryrun" in taglist:
    DRYRUN=True
    taglist.remove("tsdryrun")
    dryrunprint( "found \"tsdryrun\" in provided tags, entering dryrun mode" )

## metadata-file is dot plus filename
metadatafilename = os.path.join(METADATADIR, "." + os.path.basename(PAR_FILENAME) )

## FIXXME: check if metadatafilename conflicts with existing one
if os.path.isfile( metadatafilename ):
        print "WARNING: metadatafile \""+metadatafilename+"\" already exists. Repeated tagging not yet supportet!"

## open metadatafile
if not DRYRUN:
        metadatafile = open( metadatafilename, 'a+')
else:
        dryrunprint("opening \"" + metadatafilename + "\" for writing")

## search, if a tag corresponds to eight numbers (a date) 
datestring_found=0
if DATESTAMPFORMAT=="YYYYMMDD":
        matchingstring='^[1-3][0-9]{3}[0-1][0-9][0-3][0-9]$'
        formatstring="%Y%m%d"
elif DATESTAMPFORMAT=="YYYYMM":
        matchingstring='^[1-3][0-9]{3}[0-1][0-9]$'
        formatstring="%Y%m"

for currenttag in taglist:
        if re.search( matchingstring, currenttag ):
                datestring_found=1

## injecting default tags:
if ADDDATESTAMP and not ( OMIT_DATESTAMP_IF_DATESTAMP_IS_ENTERED_BY_USER and datestring_found==1 ):
    print "adding default tag for datestring:", datetime.today().strftime(formatstring)
    taglist.append( datetime.today().strftime(formatstring) )

## limit number of tags because of exponential runtime and number of symlinks/directories
## reasonable suggestion: 7  (with typically 37s and 13699 links/dirs)
## reasonable suggestion: 8  (with typically 5m25s and 109600 links/dirs)
if len(taglist)>TAGLIMIT:
    raise TS_TooMuchTagsException( len(taglist) )

## append tags to metadata-file
for currenttag in taglist:
    if not DRYRUN:
        print >> metadatafile, currenttag
    else:
        dryrunprint("writing to metadatafile \"" + currenttag + "\"")

## I am finished writing to Metadatafile
if not DRYRUN:
    metadatafile.close()
else:
    dryrunprint("closing metadatafile")

## changing to TAGDIR for creating the tags
if not DRYRUN:
    os.chdir(TAGDIR)
else:
    dryrunprint("chdir to " + TAGDIR)

num_symlinks_created = 0
num_symlinks_skipped = 0
num_dirs_created = 0
num_dirs_skipped = 0

if not DRYRUN:
    for currentpermutation in generate_permutation(taglist):
        for currenttag in currentpermutation:
            nextdir = os.path.join(os.getcwd(), currenttag)
            if not os.path.isdir(nextdir):
                os.mkdir(nextdir)
                num_dirs_created += 1
            else:
                num_dirs_skipped += 1
            os.chdir(nextdir)
            if os.path.islink(baselinkname):
                num_symlinks_skipped+=1
            else:
                os.symlink(absolutelinkname, baselinkname)
                num_symlinks_created+=1
        ## going back to original path
        os.chdir(TAGDIR)
else:
    currentpath=TAGDIR
    for currentpermutation in generate_permutation(taglist):
        for currenttag in currentpermutation:
            nextdir = os.path.join(currentpath, currenttag)
            if not os.path.isdir(nextdir):
                dryrunprint(currentpath + " % mkdir \"" + nextdir + "\"")
                num_dirs_created += 1
            else:
                dryrunprint(currentpath + " % -> \""+nextdir+"\" ... directory already found ")
                num_dirs_skipped += 1
            dryrunprint(currentpath + " % cd \""+nextdir+"\"")
            currentpath = nextdir
            if os.path.islink(baselinkname):
                dryrunprint(currentpath + " % -> \""+baselinkname+"\" ... symlink already found ")
                num_symlinks_skipped+=1
            else:
                dryrunprint(currentpath + " % creating symlink \""+baselinkname+"\"")
                num_symlinks_created+=1
        ## going back to original path
        dryrunprint("going back to TAGDIR \""+TAGDIR+"\"")
        currentpath=TAGDIR

print "new symlinks:",num_symlinks_created,"  (skipped",num_symlinks_skipped,")"
print "new dirs:    ",num_dirs_created,"  (skipped",num_dirs_skipped,")"

#end
