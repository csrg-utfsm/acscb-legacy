#!/usr/bin/env python
#------------------------------------------------------------------------------
# @(#) $Id: ACSStartContainerPy.py,v 1.4 2005/06/13 18:04:24 dfugate Exp $
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
#------------------------------------------------------------------------------
'''
Starts a Python Container.
'''
#------------------------------------------------------------------------------
__version__ = "$Id: ACSStartContainerPy.py,v 1.4 2005/06/13 18:04:24 dfugate Exp $"
#------------------------------------------------------------------------------
from Acspy.Container import Container
from sys import argv
#------------------------------------------------------------------------------
if __name__ == "__main__":
    g = Container(argv[1])
    if argv.count("-interactive")==0 and argv.count("-i")==0:
        g.run()
    else:
        import atexit
        atexit.register(g.destroyCORBA)
        print "This container is now running in interactive mode."
#------------------------------------------------------------------------------
