#!/bin/bash

SCRIPT_DIR=$(dirname $0)

for x in ${SCRIPT_DIR}/../lib/*.jar ; do
  CP="$CP:$x"
done
java -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y -cp ${SCRIPT_DIR}/../build/classes${CP} gov.pnnl.emsl.SWADLcli.Main -b myemsl -u dmlb2000 -s a4.my.emsl.pnl.gov -d /tmp/ -g proposal=45791 -g Tag=NMR_EXP_jun_19
