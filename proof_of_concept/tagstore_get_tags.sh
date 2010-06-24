#!/bin/sh

FILENAMETOADD=${1}
SECONDARGUMENT=${2}
DRYRUN="false"
CONFIGUREFILE=~/.config/tagstore.cfg
SEPARATOR=""
BULLETITEM=" -> "

## usage:
## if a second command line argument OR an entered tagstring including "tsdryrun" is found, execute nothing, just print out to stdout

## exit, if no argument is found
if [ "x${FILENAMETOADD}" = "x" ]; then
        echo "usage: ${0} fileordirectoryname"
        echo "ERROR: you did not provide any file- or directory name."
        exit 1
fi

[ "x${SECONDARGUMENT}" = "xtsdryrun" ] && DRYRUN="true"
[ ${DRYRUN} = "true" ] && echo "$0: command line argument for DRYRUN (\"tsdryrun\") is found"

get_preference()
{
    KEYWORD=${1}
    VALUE="really_nil_for_temp"
    VALUE=`egrep "^${KEYWORD}=" ${CONFIGUREFILE} | sed 's/.*=//'`
    if [ "${VALUE}" = "really_nil_for_temp" ]; then
            echo "$0: ERROR: value for KEYWORD \"${KEYWORD}\" could not be obtained from ${CONFIGUREFILE}!"
            exit 6
    elif [ "x${VALUE}" = "x" ]; then
            echo "$0: WARNING: value for KEYWORD \"${KEYWORD}\" obtained from ${CONFIGUREFILE} is empty"
    fi
}

[ -f ${CONFIGUREFILE} ] || "$0 ERROR: configure file \"${CONFIGUREFILE}\" not found!"
[ -f ${CONFIGUREFILE} ] || exit 2

get_preference TAGLIMIT
TAG_LIMIT=${VALUE}
get_preference METADATADIR
METADATADIR=${VALUE}
get_preference MANAGE_TAGSTORE_SCRIPT
MANAGE_TAGSTORE_SCRIPT=${VALUE}
get_preference NUM_OF_MOST_USED_TAGS
NUM_OF_MOST_USED_TAGS=${VALUE}
get_preference NUM_OF_RECENTLY_USED_FILES_TO_OBTAIN_RECENTLY_TAGS
NUM_OF_RECENTLY_USED_FILES_TO_OBTAIN_RECENTLY_TAGS=${VALUE}
get_preference ADDDATESTAMP
ADDDATESTAMP=${VALUE}
get_preference DATESTAMPFORMAT
DATESTAMPFORMAT=${VALUE}
get_preference DO_NOT_SHOW_DATESTAMP_IN_PROPOSED_TAGS
DO_NOT_SHOW_DATESTAMP_IN_PROPOSED_TAGS=${VALUE}

## if there is an additional tag for the datestamp, reduce the number of allowed tags by one:
[ ADDDATESTAMP=true ] && TAG_LIMIT=`expr ${TAG_LIMIT} - 1`

TEMPFILE=`mktemp`

## prepare filterstrings for datestamp ignoring things:
[ ${DATESTAMPFORMAT} = "YYYYMMDD" ] && IGNORE_DATESTAMP_ENTRIES_SCRIPT='| egrep -v "^[1-3][0-9][0-9][0-9][0-1][0-9][0-3][0-9]$"'
[ ${DATESTAMPFORMAT} = "YYYYMM" ]   && IGNORE_DATESTAMP_ENTRIES_SCRIPT='| egrep -v "^[1-3][0-9][0-9][0-9][0-1][0-9]$"'
[ ${DO_NOT_SHOW_DATESTAMP_IN_PROPOSED_TAGS} = "false" ] && IGNORE_DATESTAMP_ENTRIES_SCRIPT=""

write_most_used_tags_into_tempfile()
{
    echo "${BULLETITEM}Top ${NUM_OF_MOST_USED_TAGS} most used tags" >> ${TEMPFILE}
    eval "cat ${METADATADIR}/.* | sort | uniq -c | sort -n -r | sed 's/\W*[0-9]*\W//' ${IGNORE_DATESTAMP_ENTRIES_SCRIPT} | head -n ${NUM_OF_MOST_USED_TAGS} >> ${TEMPFILE}"
    echo ${SEPARATOR} >> ${TEMPFILE}
}

write_most_recently_tags_into_tempfile()
{
    echo "${BULLETITEM}Tags of last ${NUM_OF_RECENTLY_USED_FILES_TO_OBTAIN_RECENTLY_TAGS} items added" >> ${TEMPFILE}
    for curfile in `ls -1t ${METADATADIR}/.* | head -n ${NUM_OF_RECENTLY_USED_FILES_TO_OBTAIN_RECENTLY_TAGS}`; do 
            eval "cat $curfile ${IGNORE_DATESTAMP_ENTRIES_SCRIPT}" ; 
    done | sort | uniq >> ${TEMPFILE}
    echo ${SEPARATOR} >> ${TEMPFILE}
}

## import wrapper-script for screen dialogs:
. ssft.sh

## choose frontend for screen dialogs:
[ -n "$SSFT_FRONTEND" ] || SSFT_FRONTEND="$(ssft_choose_frontend)"

FILENAMETOADD_BASENAME=`basename ${FILENAMETOADD}` 

## building the dialog message content:
echo "${BULLETITEM}Item" > ${TEMPFILE}
echo "${FILENAMETOADD_BASENAME}" >> ${TEMPFILE}
echo " " >> ${TEMPFILE}
write_most_used_tags_into_tempfile
write_most_recently_tags_into_tempfile

ssft_read_string "Please enter up to ${TAG_LIMIT} tags" "`cat ${TEMPFILE}`"

rm ${TEMPFILE}

#echo "Ergebnis: [$SSFT_RESULT]"

if [ "${SSFT_RESULT}x" = "x" ]; then
        echo "it's OK, you desided to cancel."
        exit 0
fi

if echo ${SSFT_RESULT} | grep -q "tsdryrun"; then
        DRYRUN="true"
        echo "${0} tsdryrun: \"tsdryrun\" found within tags -> do *not* execute following scripts"
fi

if [ "${DRYRUN}" = "true" ]; then
        echo "${0} tsdryrun: I would do \"${MANAGE_TAGSTORE_SCRIPT} ${FILENAMETOADD} ${SSFT_RESULT}\""
else
        # echo "no dryrun found"
        ${MANAGE_TAGSTORE_SCRIPT} ${FILENAMETOADD} ${SSFT_RESULT}
fi
exit


#end
