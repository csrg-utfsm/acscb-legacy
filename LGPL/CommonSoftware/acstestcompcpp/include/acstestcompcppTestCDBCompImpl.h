#ifndef cdbTestCDBCompImpl_h
#define cdbTestCDBCompImpl_h
/*******************************************************************************
*    ALMA - Atacama Large Millimiter Array
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
*/

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

///Contains the defintion of the standard superclass for C++ components
#include <baciCharacteristicComponentImpl.h>
#include <acsexmplExport.h>

///CORBA generated servant stub
#include <acstestcompcppTestCDBCompS.h>

///Includes for each BACI property used in this example
#include <baciROdouble.h>

///Include the smart pointer for properties
#include <baciSmartPropertyPointer.h>

using namespace baci;

/** @file cdbTestCDBCompImpl.h
 */

class cdb_EXPORT TestCDBComp: public CharacteristicComponentImpl     //Standard component superclass
{
  public:
    TestCDBComp(
	  ACE_CString name,
	  maci::ContainerServices * containerServices);
    
    /**
     * Destructor
     */
    virtual ~TestCDBComp();
    virtual ACS::ROdouble_ptr 
    testatt ()
	throw (CORBA::SystemException);
    SmartPropertyPointer<ROdouble> m_testatt_sp;
};

#endif /*!cdbTestCDBCompImpl_H*/



