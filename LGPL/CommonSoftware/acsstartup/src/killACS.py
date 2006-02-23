#!/usr/bin/env python
################################################################################################
# @(#) $Id: killACS.py,v 1.15 2005/12/07 23:56:18 dfugate Exp $
#
#    ALMA - Atacama Large Millimiter Array
#    (c) Associated Universities, Inc. Washington DC, USA, 2001
#    (c) European Southern Observatory, 2002
#    Copyright by ESO (in the framework of the ALMA collaboration)
#    and Cosylab 2002, All rights reserved
#
#    This library is free software; you can redistribute it and/or
#    modify it under the terms of the GNU Lesser General Public
#    License as published by the Free Software Foundation; either
#    version 2.1 of the License, or (at your option) any later version.
#
#    This library is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#    Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with this library; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
################################################################################################
'''
This script is designed to kill every ACS process in a running system.  Really it should only be
run as root as it will shutdown other users CDBs/Managers/etc. too.
'''
################################################################################################
from os import environ, listdir, system
from sys import argv
from optparse import OptionParser
################################################################################################


parser = OptionParser(usage="This script is used as an alternative to rebooting your machine to regain TCP ports and you should report the exact circumstances that caused you to use it to the alma-sw-common@nrao.edu mailing list! Also, this will 'decapitate' other instances of ACS run by other users of this system!\n\nFor quick performance that kills all Java clients (similar behavior killACS had with older versions of ACS)\nRun:\n     killACS -q -a\nBe forewarned this kills all Java virtual machines though.")

parser.add_option("-v", "--verbose",
                  action="store_true",
                  dest="_DEBUG",
                  help="Used to print debugging information.")

parser.add_option("-Q", "-q", "--quick",
                  action="store_true",
                  dest="quickKill",
                  help="Used to kill ACS corba services, containers, etc. Does not kill clients!")

parser.add_option("-a", "--all",
                  action="store_true",
                  dest="killJava",
                  help="Used to kill all Java processes. Python processes are excluded (crashes VmWare)")

#parse everything
(options, args) = parser.parse_args()

#set this to one to get debugging information
_DEBUG=options._DEBUG
#tells whether we should try to kill everything quickly skipping a few things
quickKill=options.quickKill
#tells whether we should try to kill all Java processes
killJava=options.killJava

#PROCS is initially set to a list consisting of all known ACS binaries/scripts which exist
#outside of $ACSROOT/bin and $INTROOT/bin
PROCS = ["IFR_Service",
         "ird",
         "Notify_Service",
         "Naming_Service",
         ]
#environment variables referencing directories which include a "bin" directory full of executables.
ENVVARS = ['ACSROOT']
################################################################################################
if  __name__!="__main__":
    #pydoc -w is running this script!
    from sys import exit
    print "This script should not be imported as a module!"
    exit(0)
################################################################################################
def getExeNames(envVar):
    '''
    Returns a list of executable names located in $envVar/bin/.
    '''
    try:
        return listdir(str(environ[envVar]) + '/bin')
    except:
        #fails for any reason...must not really be a directory
        return []

#support a "quick" mode
if quickKill==1:
    PROCS= PROCS + ["acsLogSvc",
                    "loggingService",
                    "maciContainerShutdown",
                    "maciManagerJ",
                    "cdbjDAL",
                    "maciManagerShutdown",
                    "acsStartContainer"] 
else:
    #construct a list of all processes which needs to be destroyed
    for envVar in ENVVARS:
        PROCS = PROCS + getExeNames(envVar)

if _DEBUG==1:
    print "Execs are:", PROCS
    
print "Forcibly killing all known ALMA software executables..."
#We assume whats above must have failed.
#No reason to show the user any of this output...
for process in PROCS:

    #ignore this script!!!
    if process == 'killACS' or process == 'acsKillProc':
        continue

    #use the acsKillProc script to kill all commands of this type
    system('acsKillProc -C ' + process)
print "...done."
print ""

#Now everything should have been killed...let's try to cleanup $ACSDATA/tmp
print "==> Cleaning up $ACSDATA/tmp/ - removing ACS_INSTANCE temporary directories..."
try:
    system('rm -rf $ACSDATA/tmp/ACS_INSTANCE*')
    print "...done."
except:
    print "==> Failed to remove the ACS_INSTANCE directories from $ACSDATA/tmp/!"
    print "    Please clean up this directory on your own!"


if killJava==1:
    print "Killing all Java processes..."
    system('acsKillProc -C java')
    print "...done."


