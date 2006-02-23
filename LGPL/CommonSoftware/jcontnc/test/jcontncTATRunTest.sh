#!/bin/bash

#run the command
$* >& $ACS_TMP/$$.log

#sleep so things stabilize
sleep 15

#stop the container
acsStopContainer frodoContainer >&  /dev/null

#give the container a chance to flush logs
sleep 15
