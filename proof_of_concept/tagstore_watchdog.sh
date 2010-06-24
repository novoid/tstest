#!/bin/sh

## usage:
##
## tagstore_watchdog.sh
##       ... starts active watchdog
##
## tagstore_watchdog.sh tsdryrun
##       ... does not invoke ASK_USER_SCRIPT, just displays what's happening to stdout
##

LOGFILE=${HOME}/log/tagstore_${HOSTNAME}.log
CONFIGUREFILE=~/.config/tagstore.cfg
DRYRUN="false"
[ "x${1}" = "xtsdryrun" ] && DRYRUN="true"

get_preference()
{
    KEYWORD=${1}
    VALUE="really_nil_for_temp"
    VALUE=`egrep "^${KEYWORD}=" ${CONFIGUREFILE} | sed 's/.*=//'`
    if [ "${VALUE}" = "really_nil_for_temp" ]; then
            echo "$0: ERROR: value for KEYWORD \"${KEYWORD}\" could not be obtained from ${CONFIGUREFILE}!"
            exit 6
    fi
}

get_broken_symlinks_and_empty_dirs()
{
    NUM_BROKEN_SYMLINKS=`find "${TAGDIR}" -type l ! -exec test -r {} \; -print|wc -l`
    NUM_EMPTY_DIRS=`find "${TAGDIR}" -depth -type d -empty|wc -l`
}

delete_broken_symlinks()
{
    if [ "${DRYRUN}" = "true" ]; then
        echo "$0: ${TIMESTAMP} dryrun: I would remove following dead links:"
        find "${TAGDIR}" -type l ! -exec test -r {} \; -print
        echo "$0: ${TIMESTAMP} dryrun: --------------------- end of list"
    else
        find "${TAGDIR}" -type l ! -exec test -r {} \; -print0 | xargs -0 rm
    fi
}

delete_empty_directories()
{
    if [ "${DRYRUN}" = "true" ]; then
        echo "$0: ${TIMESTAMP} dryrun: I would remove following empty directories:"
        find "${TAGDIR}" -depth -type d -empty -print
        echo "$0: ${TIMESTAMP} dryrun: --------------------- end of list"
    else
        find "${TAGDIR}" -depth -type d -empty -print0 | xargs -0 rmdir
    fi
}

remove_metadatafile()
{
    ITEMBASENAME="${1}"
    METAFILE="${METADATADIR}/.${ITEMBASENAME}"
    if [ -f "${METAFILE}" ]; then
            if [ "${DRYRUN}" = "true" ]; then
                    echo "$0: ${TIMESTAMP} dryrun: I would do \"rm ${METAFILE}\""
            else
                    rm "${METAFILE}"
            fi
    else
            echo "$0: metadata file "${METAFILE}" not found, ignoring." >> ${LOGFILE}
    fi
}

remove_dead_links_and_empty_directories()
{
    get_broken_symlinks_and_empty_dirs

    while [ ${NUM_BROKEN_SYMLINKS} -gt 0 -o ${NUM_EMPTY_DIRS} -gt 0 ]; do
        if [ ${NUM_BROKEN_SYMLINKS} -gt 0 ]; then
                echo "$0: ${TIMESTAMP} removing ${NUM_BROKEN_SYMLINKS} broken links" >> ${LOGFILE}
                delete_broken_symlinks
        fi
        if [ ${NUM_EMPTY_DIRS} -gt 0 ]; then
                echo "$0: ${TIMESTAMP} removing ${NUM_EMPTY_DIRS} empty directories" >> ${LOGFILE}
                delete_empty_directories
        fi
        get_broken_symlinks_and_empty_dirs
        if [ "${DRYRUN}" = "true" ]; then
                echo "$0: ${TIMESTAMP} dryrun: setting NUM_BROKEN_SYMLINKS and NUM_EMPTY_DIRS to 0 to break the endless loop"
                NUM_BROKEN_SYMLINKS=0
                NUM_EMPTY_DIRS=0
        fi
    done
}

delete_item()
{
    ITEM="${1}"
    ISDIR="${2}"
    ITEMBASENAME=`echo "${ITEM}" | sed 's/.*\///'` 

    remove_metadatafile "${ITEMBASENAME}"

    remove_dead_links_and_empty_directories
}

[ -f ${CONFIGUREFILE} ] || echo "$0 ERROR: configure file \"${CONFIGUREFILE}\" not found!"
[ -f ${CONFIGUREFILE} ] || exit 2

get_preference FILEDIR
FILEDIR=${VALUE}
get_preference ASK_USER_SCRIPT
ASK_USER_SCRIPT=${VALUE}
get_preference METADATADIR
METADATADIR=${VALUE}
get_preference TAGDIR
TAGDIR=${VALUE}

TIMESTAMP=`/bin/date +%Y-%m-%d_%Hh%Mm%Ss`
echo "${0} started at ${TIMESTAMP} with logfile ${LOGFILE}"
## FIXXME: echo only in DRYRUN mode!
echo "${0} is started in DRYRUN-mode: display only events to stdout, do not execute anything"
echo "waiting (endlessly) for changes in directory ${FILEDIR} ..."
echo "----------------------------------------------------" >> ${LOGFILE}
echo "${0} started at ${TIMESTAMP}" >> ${LOGFILE}

while { OUTPUT=`inotifywait --quiet -e create -e delete -e moved_to -e moved_from --format "%e#%w%f" ${FILEDIR}`; }; do
        TIMESTAMP=`/bin/date +%Y-%m-%d_%Hh%Mm%Ss`
        #echo "${0} ${TIMESTAMP} dryrun: wc -l OUTPUT ["`echo ${OUTPUT}|wc -l`"]"
        INOTIFYMESSAGE=`echo ${OUTPUT} | sed 's/#.*//'`  ## e.g. "CREATE,ISDIR"
        EVENT=`echo ${INOTIFYMESSAGE} | sed 's/,.*//'`   ## e.g. "CREATE"
        ITEM=`echo ${OUTPUT} | sed 's/.*#//'`            ## e.g. "inotify tests/a file with spaces"
        if echo ${INOTIFYMESSAGE} | grep -q ISDIR; then
                ISDIR="true"
        else
                ISDIR="false"
        fi
        echo "${TIMESTAMP} notification: ${OUTPUT}" >> ${LOGFILE}
        case ${EVENT} in
                CREATE|MOVED_TO)
                    if [ "${DRYRUN}" = "true" ]; then
                        echo "${0} ${TIMESTAMP} dryrun: I got OUTPUT    [${OUTPUT}]"
                        echo "${0} ${TIMESTAMP} dryrun: I would execute \"${ASK_USER_SCRIPT} \"${ITEM}\" >> ${LOGFILE} &\""
                    else
                        ${ASK_USER_SCRIPT} "${ITEM}" >> ${LOGFILE} &
                    fi;
                    echo "------" >> ${LOGFILE};;
                DELETE|MOVED_FROM)
                    if [ "${DRYRUN}" = "true" ]; then
                        echo "${0} ${TIMESTAMP} dryrun: I got OUTPUT    [${OUTPUT}]"
                        echo "${0} ${TIMESTAMP} dryrun: I would DELETE \"${ITEM}\" and all resulting dead symlinks and empty tagdirectories"
                        delete_item "${ITEM}" "${ISDIR}" "${DRYRUN}"
                    else
                        delete_item "${ITEM}" "${ISDIR}"
                    fi;
                    echo "------" >> ${LOGFILE};;
                *) echo "${TIMESTAMP} ERROR: EVENT \"${EVENT}\" not recognised or implemented; doing nothing" >> ${LOGFILE};;
        esac
done


#end
