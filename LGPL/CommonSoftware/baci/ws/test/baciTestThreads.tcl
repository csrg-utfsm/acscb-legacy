#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) European Southern Observatory, 2002
# Copyright by ESO (in the framework of the ALMA collaboration)
# and Cosylab 2002, All rights reserved
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
# MA 02111-1307  USA
#
# "@(#) $Id: baciTestThreads.tcl,v 1.92 2005/02/14 14:36:39 bjeram Exp $"
#
# who       when      what
# --------  --------  ----------------------------------------------
# bgustafs 2001-06-27  created
#


global argc argv

if { $argc != 2 } {
    puts "usage: baciTest <lcu> <process name>"
    exit 1
}
set LCU  [lindex $argv 0]
set Proc [lindex $argv 1]

if {[info exists env(LCU_WAIT)]} {
   set LCU_WAIT $env(LCU_WAIT)
} else {
   set LCU_WAIT 200
}

if {[info exists env(DAL_REFERENCE)]} {
    set DREF $env(DAL_REFERENCE)
} else {
    set DREF "corbaloc::$env(HOST):3012/CDB"
}

set ReturnCode 0
set timeout $LCU_WAIT

#LCU connect

spawn rlogin $LCU
expect -gl "*->"

#send "< reloadScript \r"
#expect -gl "*->"

#after 5000

#send "unld \"baciTestServer\" \r"
#expect -gl "*->"

send "lcubootAutoLoad 1,\"baciTestThreads\" \r"
expect -gl "*->"

send "putenv \"DAL_REFERENCE=$DREF\"\r"
expect -gl "*->"

if { $argc == 2 } {
send "sp $Proc \r"
}

expect -gl "*->"

expect {
         "*Done." { puts ""}
         timeout { puts "timeout ($LCU_WAIT) for $Proc on $LCU";
                   set ReturnCode 1}
     }


close
exit $ReturnCode

#
# ___oOo___
