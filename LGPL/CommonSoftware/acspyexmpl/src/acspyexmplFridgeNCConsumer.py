#!/usr/bin/env python
# @(#) $Id: acspyexmplFridgeNCConsumer.py,v 1.12 2005/02/25 23:42:32 dfugate Exp $
#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) Associated Universities Inc., 2002 
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

'''
DESCRIPTION
This trivial example shows how to consume events from a notification channel in 
Python using the standard FRIDGE::temperatureDataBlockEvent example. This particular
example does not return control until five events have been received.

WHAT CAN I GAIN FROM THIS EXAMPLE?
- Consumer class usage.
- Consuming events.
- Implementation of so-called handler functions to process event data.

LINKS
- 
'''
#--REGULAR IMPORTS-------------------------------------------------------------
from time import sleep
#--CORBA STUBS-----------------------------------------------------------------
import FRIDGE
#--ACS Imports-----------------------------------------------------------------
from Acspy.Nc.Consumer          import Consumer
#--GLOBALS---------------------------------------------------------------------
#count is a global integer used to make sure we don't print more than five
#events to standard out.  really this is done just to make this module's modular
# test happy.
count = 0
#------------------------------------------------------------------------------
def fridgeDataHandler(someParam):
    '''
    This function serves only one purpose...it must do something with the extracted
    data from the structured event.  That is, it must be capable of processing
    filterable_data[0].any in the structured event.  We can be certain of the real
    type of someParam because handlers are registered only for specific
    types (i.e., the type_name field of a structured event).

    Parameters: someParam is the real CORBA type extracted from the CORBA Any at
    filterable_data[0].  In this case, it will always be a FRIDGE.temperatureDataBlockEvent.

    Returns: event handler functions return nothing.

    Raises: If any exception is thrown by this function, the Consumer class will
    catch it and call processEvent(...) which will hopefully have been overriden.
    '''
    global count
    
    if count < 5:
        tempDiff   = someParam.absoluteDiff
        tempStatus = someParam.status
        print 'The temperature difference is', tempDiff
        count = count + 1
        
    return
#------------------------------------------------------------------------------
if __name__ == "__main__":

    #Create a FridgeConsumer
    print 'Creating FridgeConsumer'
    g = Consumer(FRIDGE.CHANNELNAME_FRIDGE)

    #Subscribe to temperatureDataBlockEvent events (see acsexmplFridge.idl) and register
    #this handler to process those events
    g.addSubscription(FRIDGE.temperatureDataBlockEvent, fridgeDataHandler)

    #Let the Notification Service know we are ready to start processing events.
    g.consumerReady()

    #After five events have been received, disconnect from the channel
    print "Waiting for events . . ."
    while(count<5):
        sleep(1)

    print "Events all done . . . exiting"
    g.disconnect()
#------------------------------------------------------------------------------











