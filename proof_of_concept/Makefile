DEMODIR = /home/vk/private/directory_structure_home/20090309_tagstore_demonstrator/demotagstore
FILEDIR = /home/vk/private/directory_structure_home/20090309_tagstore_demonstrator/demotagstore/FILES
SCRIPTDIR = /home/vk/bin
ADDENTRYSCRIPT= ${SCRIPTDIR}/tagstore_add_entry.py
TESTFILENAME = Datei.txt
TESTTAGS = "eins zwei drei"


#help
#helpThe main targets of this Makefile are:
#help	help	this help
.PHONY: help
help:
	@sed -n 's/^#help//p' < Makefile


#help	dvi	generates a DVI-file
.PHONY: run
run: demodir
	${ADDENTRYSCRIPT} ${TESTFILENAME} ${TESTTAGS}

#help	demodir	generate testdata in 
.PHONY: demodir
demodir: 
	mkdir ${DEMODIR}
	mkdir ${FILEDIR}
	echo "Test" >${FILEDIR}/${TESTFILENAME}
	
#help	clean	clean up temporary files
.PHONY: clean
clean:
	rm -rf ${DEMODIR}


