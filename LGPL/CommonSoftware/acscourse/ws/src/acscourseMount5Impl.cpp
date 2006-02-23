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
* "@(#) $Id: acscourseMount5Impl.cpp,v 1.6 2005/07/04 17:18:25 gchiozzi Exp $"
*
*/
 
#include <acscourseMount5Impl.h>
#include <iostream>

using namespace std;

/* ----------------------------------------------------------------*/
/** Function designed to do something useful with MountEventData's
 *  received from the ACSCOURSE_MOUNT::MOUNT_CHANNEL channel. This will be 
 *  passed to the macro responsible for initializing SimpleConsumer objects 
 *  used in the constructor of Mount5Impl.
 *  
 *  @param joe A MountEventData describing some command sent to a mount device
 *  @return void
 *  @htmlonly
 *  <br><br>
 *  @endhtmlonly
 */
void myHandlerFunction(ACSCOURSE_MOUNT::MountEventData joe, void *handlerParam)
{
    ACS_SHORT_LOG((LM_INFO,"Received objfix command. Az: %f El: %f", joe.Azimuth, joe.Elevation));
}
/* ----------------------------------------------------------------*/
Mount5Impl::Mount5Impl(const ACE_CString &_name, maci::ContainerServices *containerServices) :
    ACSComponentImpl(_name, containerServices),
    m_MountSupplier_p(0),
    m_simpConsumer_p(0)
{
    // ACS_TRACE is used for debugging purposes
    ACS_TRACE("::Mount5Impl::Mount5Impl");

    // Handle supplier creation here.
    // - ACSCOURSE_MOUNT::MOUNT_CHANNEL is the name of the channel these events will be published too
    m_MountSupplier_p =  new nc::SimpleSupplier(ACSCOURSE_MOUNT::MOUNT_CHANNEL, this);

    // Handle consumer creation here.
    // - m_simpConsumer_p is a SimpleConsumer pointer
    // - ACSCOURSE_MOUNT::MountEventData is the event that will be consumed
    // - ACSCOURSE_MOUNT::MOUNT_CHANNEL is the name of the channel these events will come from
    // - myHandlerFunction is the function that will be automatically invoked each time an event is received
    ACS_NEW_SIMPLE_CONSUMER(m_simpConsumer_p, ACSCOURSE_MOUNT::MountEventData, ACSCOURSE_MOUNT::MOUNT_CHANNEL, myHandlerFunction, 0);

    //Let the channel know we are ready to begin processing events.
    //After consumerReady has been invoked on the Consumer object, we 
    //have very little control over when myHandlerFunction is invoked.
    m_simpConsumer_p->consumerReady();
}
/* ----------------------------------------------------------------*/
Mount5Impl::~Mount5Impl()
{
    // ACS_TRACE is used for debugging purposes
    ACS_TRACE("::Mount5Impl::~Mount5Impl");
    ACS_DEBUG_PARAM("::Mount5Impl::~Mount5Impl", "Destroying %s...", name());

    // clean-up associated with the supplier object
    if (m_MountSupplier_p != 0)
	{
	m_MountSupplier_p->disconnect();
	m_MountSupplier_p=0;
	}

    // clean-up associated with the consumer object
    if (m_simpConsumer_p != 0)
	{
	m_simpConsumer_p->disconnect();
	m_simpConsumer_p=0;
	}
}
/* --------------------- [ CORBA interface ] ----------------------*/
void 
Mount5Impl::objfix (CORBA::Double az,
		    CORBA::Double elev)
    throw (CORBA::SystemException)
{
    ACS_TRACE("::Mount5Impl::objfix");
    ACS_SHORT_LOG((LM_INFO,"Received objfix command. Az: %f El: %f", az, elev));
    
    //Create the data to be published on the event channel
    ACSCOURSE_MOUNT::MountEventData data;
    data.Azimuth = az;
    data.Elevation = elev;
    
    //Publish the data to the event channel
    m_MountSupplier_p->publishData<ACSCOURSE_MOUNT::MountEventData>(data);
}

/* --------------- [ MACI DLL support functions ] -----------------*/
#include <maciACSComponentDefines.h>
MACI_DLL_SUPPORT_FUNCTIONS(Mount5Impl)
/* ----------------------------------------------------------------*/


/*___oOo___*/
