/*******************************************************************************
* E.S.O. - ACS project
*
* "@(#) $Id: maciServantManager.cpp,v 1.85 2005/09/27 08:35:14 vwang Exp $"
*
* who       when        what
* --------  ----------  ----------------------------------------------
* msekoran  2001/02/21  created 
*/

#include <vltPort.h>

#include <orbconf.h>

#include <maciContainerImpl.h>
#include <maciServantManager.h>

NAMESPACE_USE(maci);

PortableServer::Servant
MACIServantManager::incarnate (const PortableServer::ObjectId &oid,
			       PortableServer::POA_ptr poa)
#if (TAO_HAS_MINIMUM_CORBA == 0)
      throw (
        CORBA::SystemException
        , PortableServer::ForwardRequest
      )
#else
      throw (
        CORBA::SystemException
      )
#endif /* TAO_HAS_MINIMUM_CORBA == 0 */
{
  ACE_UNUSED_ARG(oid);
  ACE_UNUSED_ARG(poa);

  // should not be called because all objects should be
  // already activated by activator

  throw CORBA::OBJECT_NOT_EXIST ();
  return 0;
}


void
MACIServantManager::etherealize (const PortableServer::ObjectId &oid,
				 PortableServer::POA_ptr adapter,
				 PortableServer::Servant servant,
				 CORBA::Boolean cleanup_in_progress,
				 CORBA::Boolean remaining_activations)
throw (
	CORBA::SystemException
			  )
{ 
  
  ACE_UNUSED_ARG(adapter);
  ACE_UNUSED_ARG(cleanup_in_progress);

#ifndef ACE_HAS_EXCEPTIONS
  ACE_UNUSED_ARG(_ACE_CORBA_Environment_variable);
#endif

  if (remaining_activations==0)
    {
      CORBA::String_var id = PortableServer::ObjectId_to_string (oid);
      ContainerImpl::getContainer()->etherealizeComponent(id.in(), servant);
    }

}


