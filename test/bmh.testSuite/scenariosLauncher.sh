#!/bin/bash

path_to_script=`readlink -f $0`
dir=$(dirname $path_to_script)

pushd . > /dev/null 2>&1
cd ${dir}

for dir in `find src -type d`; do export PYTHONPATH=${PYTHONPATH}:${dir}; done

/awips2/python/bin/python src/bmh/test/suite/main/TestRunner.py

popd > /dev/null 2>&1
