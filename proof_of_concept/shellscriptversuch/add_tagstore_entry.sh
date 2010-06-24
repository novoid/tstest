#!/bin/sh

. /home/vk/private/directory_structure_home/20090309_tagstore_demonstrator/.tagstorerc

NEWFILE=${1}
shift
TAGLIST=( $* )

echo "NEWFILE=[${NEWFILE}]"
## echo "Dollar-Stern: [$*] (single string)"
## echo "Dollar-At:    [$@] (sequence of strings)"

## echo "taglist0=${TAGLIST[0]}"  
## echo "taglist1=${TAGLIST[1]}" 
## echo "taglist2=${TAGLIST[2]}"  
## echo "taglist3=${TAGLIST[3]}"  
echo "num_of_tags=${#TAGLIST[@]}"
echo "----------------------------------"

remove_word_from_array_in_RESULTLIST()
{
    ELEMENT=${1}
    shift
    ARRAY=( ${@} )
    RESULTLIST=( `echo ${ARRAY[@]} | sed "s/^${ELEMENT}\W//g" | sed "s/^${ELEMENT}$//g" | sed "s/\W${ELEMENT}\W/ /g" | sed "s/\W${ELEMENT}\W/ /g" | sed "s/\W${ELEMENT}$//g"` )
}

## echo "========================="
## echo "removing element \"zwei\" using shellreplace"
## echo "Taglist=[${TAGLIST[@]}]"
## echo "RESULTLIST=[${RESULTLIST[@]}]"
## echo "..."
## remove_word_from_array_in_RESULTLIST "zwei" ${TAGLIST[@]}
## echo "..."
## echo "Taglist=[${TAGLIST[@]}]"
## echo "RESULTLIST=[${RESULTLIST[@]}]"
## exit

create_links_in_dir()
{
    CURRENTLIST=( $* )
    echo "`pwd`: called with: taglist[${CURRENTLIST[@]}]"
    for tag in ${CURRENTLIST[@]} ; do
        echo "`pwd`: creating directory [${tag}] (with CURRENTLIST[${CURRENTLIST[@]}] in mind)"
#        [ `pwd | grep demotagstore` ] || exit 42
        mkdir -p ${tag}
        echo "`pwd`: entering [${tag}]"
        cd ${tag}
#        echo "`pwd`: creating symlink to [${NEWFILE}]"
#        ln -s a_file .
        remove_word_from_array_in_RESULTLIST ${tag} ${CURRENTLIST[@]}
        if [ ${#RESULTLIST[@]} -gt 0 ]; then
            echo "`pwd`: creating subdirs:"
            echo "`pwd`: RESULTLIST=[${RESULTLIST[@]}]"
            create_links_in_dir ${RESULTLIST[@]}
        fi
        echo "`pwd`: cd .."
        cd ..
    done
}

cd ${TS_FILEDIR}
create_links_in_dir ${TAGLIST[@]}

#end
