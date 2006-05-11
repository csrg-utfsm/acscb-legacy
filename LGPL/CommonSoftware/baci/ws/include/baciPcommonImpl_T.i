/*
* ALMA - Atacama Large Millimiter Array
* (c) European Southern Observatory, 2003 
*
*This library is free software; you can redistribute it and/or
*modify it under the terms of the GNU Lesser General Public
*License as published by the Free Software Foundation; either
*version 2.1 of the License, or (at your option) any later version.
*
*This library is distributed in the hope that it will be useful,
*but WITHOUT ANY WARRANTY; without even the implied warranty of
*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*Lesser General Public License for more details.
*
*You should have received a copy of the GNU Lesser General Public
*License along with this library; if not, write to the Free Software
*Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*/

#include "baciPcommonImpl_T.h"
#include <sstream>

using namespace baciErrTypeProperty;

template<ACS_P_C> 
PcommonImpl<ACS_P_TL>::PcommonImpl(const ACE_CString& name, BACIComponent* component_p, DevIO<TM>* devIO, bool flagdeldevIO) : 
        CharacteristicModelImpl(name, component_p->getCharacteristicModel()), 
	property_mp(0), devIO_mp(0), initialization_m(1), destroyed_m(false), 
	reference_mp(CORBA::Object::_nil()),
	historyStart_m(-1), historyTurnaround_m(false)
{
  ACS_TRACE("baci::PcommonImpl&lt;&gt;::PcommonImpl");
  
  // read static data
  if (readCharacteristics()==false) 
    {
	std::string procName="PcommonImpl::PcommonImpl(";
	procName+=name.c_str();
	procName+=",...)";
	ACS_LOG(LM_RUNTIME_CONTEXT, "baci::PcommonImpl&lt;&gt;::PcommonImpl",
		(LM_ERROR, "Failed to read static data for '%s'", name.c_str()));

	baciErrTypeProperty::PropertyStaticDataExImpl ex(__FILE__,__LINE__,procName.c_str());
	ex.addData("Property",name.c_str());
	throw ex;
    }

  // create BACI property instance (calling default_value() and id() do not work properly!!!)
  property_mp = new BACIProperty(name.c_str(), this, this,
				BACIValue(defaultValue_m), component_p);
  if (property_mp==0)
  {
	std::string procName="PcommonImpl::PcommonImpl(";
	procName+=name.c_str();
	procName+=",...)";
	baciErrTypeProperty::PropertyCreationExImpl ex(__FILE__,__LINE__,procName.c_str());
	ex.addData("Property",name.c_str());
	throw ex;
  }

  reference_mp = BACI_CORBA::ActivateCORBAObject(this, name.c_str());
  if (CORBA::is_nil(reference_mp)==true)
  {
	std::string procName="PcommonImpl::PcommonImpl(";
	procName+=name.c_str();
	procName+=",...)";
	ACS_LOG(LM_RUNTIME_CONTEXT, "baci::PcommonImpl&lt;&gt;::PcommonImpl",
		(LM_ERROR, "Failed to activate CORBA object '%s'", name.c_str()));

	delete property_mp;
	baciErrTypeProperty::PropertyActivationExImpl ex(__FILE__,__LINE__,procName.c_str());
	ex.addData("Property",name.c_str());
	throw ex;
  }

  ACE_CString_Vector recoveryMonitorsNames = 
    BACIRecoveryManager::getInstance()->getObjectsStartingWith(name.c_str());
  if (recoveryMonitorsNames.size()>0)
    {
      for (ACE_CString_Vector::iterator i = recoveryMonitorsNames.begin();
	   i != recoveryMonitorsNames.end(); i++) {
        //ACS_DEBUG_PARAM("baci::template<ACS_P_C> PcommonImpl&lt;&gt;::template<ACS_P_C> PcommonImpl&lt;&gt;", "Recovering monitor: '%s'.", i->c_str());
	new TMonitorImpl (i->c_str(), min_timer_trigger(), 
			  BACIValue(0.0/*min_delta_trigger()*/), property_mp);
      }
    }

  if (devIO!=0)
    {
      devIO_mp = devIO;
      deldevIO_m = flagdeldevIO; 
   }
  else
    {
      deldevIO_m = true;
//      value_m = defaultValue_m;
      devIO_mp = new DevIOMem<TM>(value_m);
      ACS_DEBUG("baci::PcommonImpl&lt;&gt;::PcommonImpl", "No DevIO provided - created DevIOMem.");
    }
  
  ACS_DEBUG("baci::PcommonImpl&lt;&gt;::PcommonImpl", "Successfully created.");
}

template<ACS_P_C> PcommonImpl<ACS_P_TL>::~PcommonImpl()
{
  ACS_TRACE("baci::PcommonImpl&lt;&gt;::~PcommonImpl");
  
  if (deldevIO_m==true) {
      delete devIO_mp;
}

  // destroy BACI property
  if (property_mp!=0) {
    delete property_mp;
    property_mp = 0;
  }
}

template<ACS_P_C> 
void PcommonImpl<ACS_P_TL>::destroy()
{
  ACS_TRACE("baci::PcommonImpl&lt;&gt;::destroy");
  if (destroyed_m==true)
    return;
  destroyed_m = true;

  if (CORBA::is_nil(reference_mp)==false)
    {
	  // this calls delete on this object, so DO NOT use any of its variables anymore
	  if (BACI_CORBA::DestroyCORBAObject(reference_mp)==false)
	    {
		ACS_LOG(LM_RUNTIME_CONTEXT, "baci::PcommonImpl&lt;&gt;::~PcommonImpl",
			(LM_ERROR, "Failed to destroy CORBA object '%s'", property_mp->getName()));
	    }
	  else
	      _remove_ref();
    }
  
}

/* --------------- [ Action implementator interface ] -------------- */

template<ACS_P_C> 
ActionRequest PcommonImpl<ACS_P_TL>::invokeAction(int function,
						  BACIComponent* component_p, 
						  const int &callbackID, 
						  const CBDescIn& descIn, 
						  BACIValue* value, 
						  Completion& completion, 
						  CBDescOut& descOut)
{
  ACE_UNUSED_ARG(function);
  CompletionImpl c;
  // only one action
  ActionRequest req = getValueAction(component_p, callbackID, descIn, value, c, descOut);
  
  if (c.isErrorFree())
      {
      completion = c;
      }
  else
      {
      completion = InvokeActionErrorCompletion(c,
					       __FILE__,
					       __LINE__,
					       "baci::PcommonImpl&lt;&gt;::invokeAction");
      }//if-else

  return req;
}//invokeAction

/* -------------- [ Property implementator interface ] -------------- */

template<ACS_P_C> 
void PcommonImpl<ACS_P_TL>::getValue(BACIProperty* property,
		   BACIValue* value, 
		   Completion &completion,
		   CBDescOut& descOut)
{
  ACE_UNUSED_ARG(property);
  ACE_UNUSED_ARG(descOut);

  TM nval;
  
  try {
	nval = devIO_mp->read(completion.timeStamp);
  } catch (ACSErr::ACSbaseExImpl& ex) {
	completion = baciErrTypeDevIO::ReadErrorCompletion (ex, __FILE__, __LINE__,"PcommonImpl::getValue(...)");
	return;
  }

  value->setValue( nval );
      // if there is no error add value to history
      // !!! to be done in a loop
  addValueToHistory(completion.timeStamp, nval);

  completion = ACSErrTypeOK::ACSErrOKCompletion();
}//getValue
 
/* --------------- [ History support ] --------------- */

template<ACS_P_C> 
void PcommonImpl<ACS_P_TL>::addValueToHistory(ACS::Time time, TM &value)
{
  if ((historyTurnaround_m == false)&& (historyStart_m==(HISTORY_SIZE-1))) 
	  historyTurnaround_m = true;
  historyStart_m = ++historyStart_m % HISTORY_SIZE;
  historyTime_m[historyStart_m] = time;
  historyValue_m[historyStart_m] = value;   // check memory management
}

/* -------------- [ Other interfaces ] -------------- */

/// async. get value action implementation
template<ACS_P_C> 
ActionRequest PcommonImpl<ACS_P_TL>::getValueAction(BACIComponent* component_p, 
						    int callbackID,
						    const CBDescIn& descIn, 
						    BACIValue* value,
						    Completion& completion, 
						    CBDescOut& descOut)
{
  ACE_UNUSED_ARG(component_p);
  ACE_UNUSED_ARG(callbackID);
  ACE_UNUSED_ARG(descIn);

  CompletionImpl co;

  getValue(property_mp, value, co, descOut);
  
  if (co.isErrorFree())
      {
      completion = co;
      }
  else
      {
      completion = CanNotGetValueCompletion(co, 
					    __FILE__, 
					    __LINE__, 
					    "baci::PcommonImpl&lt;&gt;::getValueAction");
      }//if-else

  // complete action requesting done invokation, 
  // otherwise return reqInvokeWorking and set descOut.estimated_timeout
  return reqInvokeDone;
}//getValueAction


/* ---------------------- [ CORBA interface ] ---------------------- */

template<ACS_P_C> 
bool PcommonImpl<ACS_P_TL>::readCharacteristics()
{
    
  DAONode* dao = this->getDAONode();
  if (!dao)
      return false;
  
  try
      {
      CORBA::String_var str;
      
      str = dao->get_string("description");
      description_m = str.in();
      
      str = dao->get_string("format");
      format_m = str.in();
      
      str = dao->get_string("units");
      units_m = str.in();

      resolution_m = dao->get_long("resolution");

      CORBA::Double dbl;

      dbl = dao->get_double("default_timer_trig");
      dbl = dbl * static_cast<CORBA::Double>(10000000.0);
      defaultTimerTrig_m = static_cast<CORBA::ULong>(dbl);

      dbl = dao->get_double("min_timer_trig");
      dbl = dbl * static_cast<CORBA::Double>(10000000.0);
      minTimerTrig_m = static_cast<CORBA::ULong>(dbl);

      // NOTE: default_value is always a scalar value
      str = dao->get_string("default_value");
      CDBconverter<TSM>::convertValue(str.in(), defaultValue_m);

      return true;
      }
  catch (ACSErr::ACSbaseExImpl& ex)
      {
      ex.log();
      return false;
      }
  catch (...)
      {
      return false;
      }

}

/* ---------------------- [ CORBA interface ] ---------------------- */
template<ACS_P_C> 
char* PcommonImpl<ACS_P_TL>::characteristic_component_name ()
  throw (CORBA::SystemException)
{

  return CORBA::string_dup (property_mp->getComponent()->getName());
}

template<ACS_P_C> 
char* PcommonImpl<ACS_P_TL>::name ()
  throw (CORBA::SystemException)
{

  return CORBA::string_dup (property_mp->getName());
}

template<ACS_P_C> 
char* PcommonImpl<ACS_P_TL>::description ()
  throw (CORBA::SystemException)
{
  return CORBA::string_dup (description_m.c_str());
}

template<ACS_P_C> 
char* PcommonImpl<ACS_P_TL>::format ()
  throw (CORBA::SystemException)
{
  return CORBA::string_dup (format_m.c_str());
}

template<ACS_P_C> 
char* PcommonImpl<ACS_P_TL>::units ()
  throw (CORBA::SystemException)
{

  return CORBA::string_dup (units_m.c_str());
}
 
template<ACS_P_C> 
ACS::pattern PcommonImpl<ACS_P_TL>::resolution ()
  throw (CORBA::SystemException)
{
  return resolution_m;
}

template<ACS_P_C> 
T PcommonImpl<ACS_P_TL>::get_sync (ACSErr::Completion_out c
		    )
  throw (CORBA::SystemException)
{
 CompletionImpl co;
 ACS::CBDescOut descOut;
 BACIValue value(0.0);

 this->getValue(property_mp, &value, co, descOut);
  
 if (co.isErrorFree())
     {
     c = co.returnCompletion(false);
     }
 else
     {
     c = CanNotGetValueCompletion(co, 
				  __FILE__, 
				  __LINE__, 
				  "baci::PcommonImpl&lt;&gt;::get_sync").returnCompletion(false);
     }//if-else

 // .... or replace with call BACIValue::retn()
 TM t = value.getValue(static_cast<TM*>(0));

 return CORBAMem<T, TM>::retn(t); 
}//get_sync


template<ACS_P_C> 
void PcommonImpl<ACS_P_TL>::get_async (TCB *cb,
		     const ACS::CBDescIn & desc
		     )
  throw (CORBA::SystemException) 
{

  property_mp->getComponent()->registerAction(BACIValue::mapType(static_cast<TM*>(0)), cb, 
				       desc, this, 0);
}

template<ACS_P_C> 
CORBA::Long PcommonImpl<ACS_P_TL>::get_history (CORBA::Long n_last_values,
		       TSeq_out vs,
		       ACS::TimeSeq_out ts)
  throw (CORBA::SystemException)
{
  // thread lock needed
  
  if (n_last_values < 0)
	  ACE_THROW_RETURN(CORBA::BAD_PARAM(), 0);
 
  CORBA::Long length, first;
  if (historyTurnaround_m==true)
    {
      length = HISTORY_SIZE;
      first = historyStart_m+1;
    }
  else
    {
      length = historyStart_m+1;
      first = 0;
    }

  // last n values
  if (n_last_values > length)
      n_last_values = length;
  first = (first+length-n_last_values)%HISTORY_SIZE;

  if (n_last_values < length)
	  length = n_last_values;

  vs = new TSeq(length); vs->length(length);
  ts = new ACS::TimeSeq(length); ts->length(length);
  for (int i = 0; i < length; i++)
    {
      (*vs)[i] = CORBAMem<T, TM>::addToArry(historyValue_m[(first+i)%HISTORY_SIZE]);
      (*ts)[i] = historyTime_m[(first+i)%HISTORY_SIZE];
    } 
  return length;
}


template<ACS_P_C> 
TMonitor *PcommonImpl<ACS_P_TL>::create_monitor (TCB *cb,
			  const ACS::CBDescIn & desc)
  throw (CORBA::SystemException)
{
  TMonitorImpl* monitor_p = new TMonitorImpl(ACE_CString(property_mp->getName())+"_monitor",
					     cb, desc, 
					     default_timer_trigger(),
					     BACIValue::NullValue, 
					     min_timer_trigger(), 
					   BACIValue(0/*min_delta_trigger()*/),
					     property_mp);

  if (monitor_p==0)
	  ACE_THROW_RETURN(CORBA::NO_RESOURCES(), TMonitor::_nil());
  else if (monitor_p->initialization())
  {
	  monitor_p->destroy();
	  ACE_THROW_RETURN(CORBA::NO_RESOURCES(), TMonitor::_nil());
  }
  
  TMonitor* mon_p = 
	  TMonitor::_narrow(monitor_p->getCORBAReference());
  ACE_CHECK_RETURN(TMonitor::_nil());

  return mon_p;
}

template<ACS_P_C> 
TMonitor* PcommonImpl<ACS_P_TL>::create_postponed_monitor (ACS::Time start_time,
			  TCB *cb,
			  const ACS::CBDescIn & desc)
  throw (CORBA::SystemException)
{
  TMonitorImpl* monitor = new TMonitorImpl(ACE_CString(property_mp->getName())+"_monitor",
					     cb, desc, 
					     default_timer_trigger(),
					     BACIValue::NullValue, 
					     min_timer_trigger(), 
					   BACIValue(0/*min_delta_trigger()*/),  
					     property_mp,
					     start_time);
  
  if (!monitor)
	  ACE_THROW_RETURN(CORBA::NO_RESOURCES(), TMonitor::_nil());
  else if (monitor->initialization())
  {
	  monitor->destroy();
	  ACE_THROW_RETURN(CORBA::NO_RESOURCES(), TMonitor::_nil());
  }
  
  TMonitor *mon = 
	  TMonitor::_narrow(monitor->getCORBAReference());
  ACE_CHECK_RETURN(TMonitor::_nil());

  return mon;
}

template<ACS_P_C> 
ACS::TimeInterval PcommonImpl<ACS_P_TL>::default_timer_trigger ()
  throw (CORBA::SystemException)
{

  return defaultTimerTrig_m;
}

template<ACS_P_C> 
ACS::TimeInterval PcommonImpl<ACS_P_TL>::min_timer_trigger ()
  throw (CORBA::SystemException)
{

  return minTimerTrig_m;
}

template<ACS_P_C> 
TS PcommonImpl<ACS_P_TL>::default_value ()
  throw (CORBA::SystemException)
{
  return CORBAMem<TS, TSM>::retn(defaultValue_m);
}

/*___oOo___*/





























