# @(#) $Id: ComponentLifecycle.py,v 1.5 2005/02/25 23:42:32 dfugate Exp $
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
# "@(#) $Id: ComponentLifecycle.py,v 1.5 2005/02/25 23:42:32 dfugate Exp $"
#
# who       when        what
# --------  ----------  ----------------------------------------------
# dfugate   2003/08/05  Created.
#------------------------------------------------------------------------------

'''
This module provides the ComponentLifecycle class along with
ComponentLifecycleException.

Components can be derived from ComponentLifecycle (but do not have to
necessarily) and have startup/shutdown methods that can only be invoked by the
container.

ComponentLifecycleException is an exception thrown by ComponentLifecycle, but
will be caught by the container.

TODO:
- ComponentLifecycleException is not defined in an IDL file, but is not
automatically generated by the ACS Error System. This makes it difficult
to use within components. Submit an SPR which will hopefully result in
ComponentLifeCycleException class in this module being removed.
'''

__revision__ = "$Id: ComponentLifecycle.py,v 1.5 2005/02/25 23:42:32 dfugate Exp $"

#--REGULAR IMPORTS-------------------------------------------------------------
import exceptions
#--CORBA STUBS-----------------------------------------------------------------

#--ACS Imports-----------------------------------------------------------------

#--GLOBALS---------------------------------------------------------------------

#------------------------------------------------------------------------------
class ComponentLifecycle:
    '''
    Class ComponentLifecycle is provided so that the developer does not have to
    expose startup/shutdown methods in an IDL interface for their component.

    If the component does inherit from this class, it should override at least
    one method.  Also, note that these methods should only raise
    ComponentLifecycleException excepts. Any other exception will result in the
    servant not being activated!
    '''
    #--------------------------------------------------------------------------
    def __init__(self):
        '''
        '''
        pass
    #--------------------------------------------------------------------------
    def initialize (self):
        '''
        Called to give the component time to initialize itself.    
        For instance, the component could retrieve connections, read in 
        configuration files/parameters, build up in-memory tables, ...
    
        Called before execute.  In fact, this method might be called quite some
        time before functional requests can be sent to the component.
        
        Must be implemented as a synchronous (blocking) call.
        
        Container will catch and log ComponentLifecycleExceptions raised by
        this method.

        Params: None.

        Returns: Nothing.
        '''
        pass
    #--------------------------------------------------------------------------
    def execute (self):
        '''
        Called after initialize to tell the component that it has to be ready
        to accept incoming functional calls at any time. 
    
        Examples:
        - last-minute initializations for which initialize seemed too early
        - component could start actions which arent triggered by any functional
        call, e.g. the Scheduler could start to rank SBs in a separate thread.
    
        Must be implemented as a synchronous (blocking) call (can spawn threads
        though).

        Container will catch and log ComponentLifecycleExceptions raised by
        this method.
        
        Params: None.

        Returns: Nothing.
        '''
        pass
    #--------------------------------------------------------------------------
    def cleanUp (self):
        '''
        Called after the last functional call to the component has finished.
        The component should then orderly release resources etc.

        Must be implemented as a synchronous (blocking) call.

        Container will catch all exceptions.
        
        Params: None.

        Returns: Nothing.
        '''
        pass
    #--------------------------------------------------------------------------
    def aboutToAbort (self):
        '''
        Called when due to some error condition the component is about to be
        forcefully removed some unknown amount of time later (usually not very
        much...).
        
        The component should make an effort to die as neatly as possible.
        
        Because of its urgency, this method will be called asynchronously to the execution of 
        any other method of the component.

        Container will catch all exceptions.
        
        Params: None.

        Returns: Nothing.
        '''
        self.cleanUp()
    #--------------------------------------------------------------------------

#------------------------------------------------------------------------------
class ComponentLifecycleException(exceptions.Exception):
    '''
    Class ComponentLifecycleException is provided so that the container can
    catch and log non-critical exceptions, but destroy the Component upon
    anything else.
    '''
    #--------------------------------------------------------------------------
    def __init__(self, args=None):
        '''
        Default constructor.
        
        Params: args SHOULD be in string format.

        Returns: Nothing.
        '''
        exceptions.Exception.__init__(self)
        self.args = args
    #--------------------------------------------------------------------------

