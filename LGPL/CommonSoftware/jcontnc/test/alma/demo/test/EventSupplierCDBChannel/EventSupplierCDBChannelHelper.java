/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) Associated Universities Inc., 2002 
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 *
 * EventSupplierHelper.java
 *
 * Created on April 11, 2003, 3:09 PM
 */

package alma.demo.test.EventSupplierCDBChannel;

import java.util.logging.Logger;

import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ComponentHelper;

import alma.demo.SupplierCompOperations;
import alma.demo.SupplierCompPOATie;

/**
 *
 * @author  dfugate
 */
public class EventSupplierCDBChannelHelper extends ComponentHelper
{
	public EventSupplierCDBChannelHelper(Logger containerLogger)
	{
		super(containerLogger);
	}
	
    /** Method _createComponentImpl to be provided by subclasses.
     * Must return the same object that also implements the functional interface.
     *
     * @return ComponentLifecycle
     */
    protected ComponentLifecycle _createComponentImpl()
    {
        return new EventSupplierCDBChannel();
    }
    
    /** This method must be provided by the component helper class.
     *
     * @return Class
     */
    protected Class _getPOATieClass()
    {
        return SupplierCompPOATie.class;
    }
    
    /** Gets the xxOperations interface as generated by the IDL compiler.
     *
     * This method must be provided by the component helper class.
     *
     * @return  the <code>Class</code> object associated with the operations interface.
     */
    protected Class _getOperationsInterface()
    {
        return SupplierCompOperations.class;
    }
    
}

