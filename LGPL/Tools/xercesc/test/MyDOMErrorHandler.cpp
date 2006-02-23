/*******************************************************************************
*    ALMA - Atacama Large Millimiter Array
*    (c) Associated Universities Inc., 2002 *
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
*
*
* "@(#) $Id: MyDOMErrorHandler.cpp,v 1.1 2004/12/16 13:36:44 sharring Exp $"
*
* who       when      what
* --------  --------  ----------------------------------------------
* sharring  27/09/04  created 
*/

#include <string>
#include <xercesc/util/XMLString.hpp>
#include <MyDOMErrorHandler.h>
#include <xercesc/dom/DOMError.hpp>
#include <xercesc/dom/DOMLocator.hpp>

//  MyDOMHandlers: Overrides of the DOM ErrorHandler interface

//XERCES_CPP_NAMESPACE_BEGIN

/**
 * Constructor.
 */
MyDOMErrorHandler::MyDOMErrorHandler()
{
}

/**
 * Destructor.
 */
MyDOMErrorHandler::~MyDOMErrorHandler()
{
}

bool MyDOMErrorHandler::handleError(const DOMError& domError)
{
        cerr << "--------------------------------------------------" << endl;

	if (domError.getSeverity() == DOMError::DOM_SEVERITY_WARNING)
		cerr << "\nWarning at file ";
	else if (domError.getSeverity() == DOMError::DOM_SEVERITY_ERROR)
		cerr << "\nError at file ";
	else
		cerr << "\nFatal Error at file ";

        DOMLocator * myLocator = domError.getLocation();
        string location = XMLString::transcode( myLocator->getURI() );
        string errorMsg = XMLString::transcode(domError.getMessage());

	cerr << location << ", line " << domError.getLocation()->getLineNumber()
		<< ", char " << domError.getLocation()->getColumnNumber()
		<< "\n  Message: " << errorMsg << endl;

        cerr << "--------------------------------------------------" << endl;
	return true;
}

//XERCES_CPP_NAMESPACE_END
