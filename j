#!/bin/bash
#
#
# Copyright 2008-2017 Wells Burke
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# JJ - A Generic Java Launcher
#

# ===============================================
# script customizations

# set jvm heap initial and maximum sizes (in megabytes).
JAVA_HEAP_INIT_SIZE=64
JAVA_HEAP_MAX_SIZE=1024

# add additional class reporitories here
CLASSPATH=${CLASSPATH}

# ===============================================
# establish the 'JJ_HOME' which defaults to the script dir

if [ -z "$JJ_HOME" ]; then
JJ_HOME="$(dirname "$(readlink -f ${BASH_SOURCE[0]})")"
fi

if [ -z "$JJ_HOME" ]; then
	echo "Failed to locate JJ_HOME"
	exit 1
fi	



# ===============================================


# a space seperated list of all root directories that should be searched
# for jars, zips and classes dirs to automatically add to the classpath 
JJ_DIRS="./ ${JJ_HOME} ${JJ_DIRS} "



# automatically add classes dirs and jar/zips to classpath
for dir in ${JJ_DIRS}
do
    # ===============================================
    # inner looop finds all classes dirs, zip and jar files
	for path in `find ${dir} -name 'classes' -or -wholename '*.jar' -or -wholename '*.zip' -maxdepth 2 2>/dev/null`
	do
	  CLASSPATH=${CLASSPATH}:${path}
	done
    # ===============================================
done


# find a java installation.
if [ -z "$JAVA_HOME" ]; then
	j=`which java 2>/dev/null`
	if [ -z "$j" ]; then
		echo "Failed to locate the java virtual machine! Bailing..."
		exit 1
	else
		JAVA="$j"
	fi
else
	JAVA="$JAVA_HOME/bin/java"
fi

#echo $CLASSPATH
# launch application.
$JAVA -Xms${JAVA_HEAP_INIT_SIZE}M -Xmx${JAVA_HEAP_MAX_SIZE}M -cp "${CLASSPATH}" $*