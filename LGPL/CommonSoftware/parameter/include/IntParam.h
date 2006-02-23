#ifndef _INT_PARAM_H
#define _INT_PARAM_H
/*******************************************************************************
*    ALMA - Atacama Large Millimiter Array
*    (c) Associated Universities Inc., 2002 
*    (c) European Southern Observatory, 2002
*    Copyright by ESO (in the framework of the ALMA collaboration)
*    and Cosylab 2002, All rights reserved
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
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*
* "@(#) $Id: IntParam.h,v 1.3 2005/08/15 23:26:53 sharring Exp $"
*
* who       when      what
* --------  --------  ----------------------------------------------
* sharring  10/27/04  created
*/

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include <QuantityParam.h>
#include <memory>
#include <string>

using std::string;
using std::auto_ptr;

/** @file IntParam.h */

namespace parameterSet {

	/**
	 * IntParam class used to support OFFLINE tasks
	 */
	class IntParam : public parameterSet::QuantityParam
	{    
	  public:
	    /**
	     * Constructor
	     */
	    IntParam();

	    /**
	     * Constructor
	     */
	    IntParam(int intVal, string nameVal, auto_ptr<string> unitsVal);
	    
	    /**
	     * Destructor
	     */
	    virtual ~IntParam();
	    
	    /*
	     * Accessor for the value.
	     * @return the value as an integer.
	     */
	    int getValue();

	    /*
	     * Accessor for the type, e.g. "bool" or "int" etc.
	     * @return the type as a string.
	     */
	    virtual string getType();

		protected:
		/**
		 * Used to create the value portion of the toString (XML) string.
		 * Different concrete implementations may do this differently, but
		 * they will all have such a method.
		 */
		virtual string valueToString();

	  private:
	    int value;
	};

}
#endif /*!_INT_PARAM_H*/
