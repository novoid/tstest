#!/bin/sh
## generates pyc files from the main components and
## pyw files for windows (they do not show any DOS-windows)
python /usr/lib/python2.7/compileall.py .
cp administration.pyc administration.pyw
cp tagstore_manager.pyc tagstore_manager.pyw
cp tagstore.pyc tagstore.pyw
cp tagstore_retag.pyc tagstore_retag.pyw

