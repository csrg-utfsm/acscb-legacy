# @(#) $Id: String.py,v 1.3 2005/06/13 18:04:24 dfugate Exp $
#
# Copyright (C) 2001
# Associated Universities, Inc. Washington DC, USA.
#
# Produced for the ALMA project
#
# This library is free software; you can redistribute it and/or modify it under
# the terms of the GNU Library General Public License as published by the Free
# Software Foundation; either version 2 of the License, or (at your option) any
# later version.
#
# This library is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
# details.
#
# You should have received a copy of the GNU Library General Public License
# along with this library; if not, write to the Free Software Foundation, Inc.,
# 675 Massachusetts Ave, Cambridge, MA 02139, USA.  Correspondence concerning
# ALMA should be addressed as follows:
#
# Internet email: alma-sw-admin@nrao.edu
# "@(#) $Id: String.py,v 1.3 2005/06/13 18:04:24 dfugate Exp $"
#
# who       when        what
# --------  ----------  ----------------------------------------------
# dfugate   2004/07/21  Created.
#------------------------------------------------------------------------------

'''
This module provides an implementation of the Pstring IDL interface
'''

__version__ = "$Id: String.py,v 1.3 2005/06/13 18:04:24 dfugate Exp $"

#--REGULAR IMPORTS-------------------------------------------------------------
from traceback import print_exc
#--CORBA STUBS-----------------------------------------------------------------
import ACS__POA
#--ACS Imports-----------------------------------------------------------------
from ACSImpl.GenericProperty     import GenericProperty
from ACSImpl.Monitors            import Monitorstring
#--GLOBALS---------------------------------------------------------------------

#------------------------------------------------------------------------------
#--P property------------------------------------------------------------------
#------------------------------------------------------------------------------
class Pstring(GenericProperty):
    '''
    Properties can be derived from Pstring only if their IDL derives from
    ACS::Pstring.
    '''
    #--------------------------------------------------------------------------
    def __init__(self, name, charCompRef, devIORef):
        '''
        Constructor

        Params:
        - name is the quite literally the name of the property
        - charCompRef is the characteristic component object which contains this
        property
        - devIORef is a reference to a DevIO to be used with this property

        Returns: Nothing

        Raises: Nothing.
        '''
        GenericProperty.__init__(self, name, charCompRef, devIORef)
        return
    #--------------------------------------------------------------------------
    def coerceToPropertyType(self, value=None):
        '''
        Overriden.
        '''
        #something went wrong. Return default value
        if value==None:
            return ""
        
        try:
            #coerce into an int type
            return eval("str(" + value + ")")
        except:
            #warn them about CDB access
            self.getLogger().logAlert("Unble to coerce '" + str(value) + "' into the correct type!")
            print_exc()
            #return an acceptable default value instead...an empty sequence
            return ""
    #--------------------------------------------------------------------------
    def getMonitorObject(self, scheduler, timeoutID):
        '''
        Helper method returns a monitor object of the correct type.
        '''
        return Monitorstring(scheduler, timeoutID)
#------------------------------------------------------------------------------
#--RO property-----------------------------------------------------------------
#------------------------------------------------------------------------------
class ROstring(ACS__POA.ROstring, Pstring):
    '''
    Properties can be derived from ROstring only if their IDL derives from
    ACS::ROstring.
    '''
    #--------------------------------------------------------------------------
    def __init__(self, name, charCompRef, devIORef=None):
        '''
        Constructor

        Params:
        - name is the quite literally the name of the property
        - charCompRef is the characteristic component object which contains this
        property
        - devIORef is a reference to a DevIO to be used with this property

        Returns: Nothing

        Raises: Nothing.
        '''
        Pstring.__init__(self, name, charCompRef, devIORef)
        return
#-----------------------------------------------------------------------------
#--RW property----------------------------------------------------------------
#-----------------------------------------------------------------------------
class RWstring(ACS__POA.RWstring, ROstring):
    '''
    Properties can be derived from ROstring only if their IDL derives from
    ACS::ROstring.
    '''
    #-------------------------------------------------------------------------
    def __init__(self, name, charCompRef, devIORef=None):
        '''
        Constructor

        Params:
        - name is the quite literally the name of the property
        - charCompRef is the characteristic component object which contains this
        property
        - devIORef is a reference to a DevIO to be used with this property

        Returns: Nothing

        Raises: Nothing.
        '''
        ROstring.__init__(self, name, charCompRef, devIORef)
        return
#---------------------------------------------------------------------------
