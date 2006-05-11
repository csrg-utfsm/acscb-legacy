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

#include "baciRWSeqContImpl_T.h"
#include "baciRWcontImpl_T.i"

template <ACS_RW_C> 
RWSeqContImpl<ACS_RW_TL>::RWSeqContImpl(const ACE_CString& name, BACIComponent *component_p, DevIO<TM> *devIO, bool flagdeldevIO) :
    RWcontImpl<ACS_RW_TL>(false/* =not set intialize value*/, name, component_p, devIO, flagdeldevIO) 
{
    ACS_TRACE("baci::RWSeqContImpl&lt;&gt;::RWSeqContImpl"); 

    if (this->devIO_mp->initializeValue()==true) 
	{
	//ACS::Time timeStamp;
	//devIO_mp->write(defaultValue_m, timeStamp);
        //TODO: setting of default value for sequences not implemented.
	ACS_DEBUG("baci::RWSeqContImpl&lt;&gt;::RWSeqContImpl", "DevIO initial value set not implemented yet.");
	}

  this->initialization_m = 0; // property successfuly initialized
  ACS_DEBUG("baci::RWSeqContImpl&lt;&gt;::RWSeqContImpl", "Successfully created.");  
}

template <ACS_RW_C> 
void RWSeqContImpl<ACS_RW_TL>::setValue(BACIProperty* property,
				   BACIValue* value, 
				   Completion &completion,
				   CBDescOut& descOut)
{
  ACE_UNUSED_ARG(property);
  ACE_UNUSED_ARG(descOut);

  ACS::Time ts;
  TS minVal = this->min_value();
  TS maxVal = this->max_value();
  TM val = value->getValue(static_cast<TM*>(0));
  for (CORBA::ULong n = 0UL; n < val.length(); n++)
      if (minVal>val[n] || maxVal<val[n])
	{ 
	  completion.timeStamp=getTimeStamp();
	  completion = ACSErrTypeCommon::OutOfBoundsCompletion( __FILE__, 
								__LINE__,
								"baci::RWSeqCont&lt;&gt;::setValue");
	  return;
	} 

  try
      {
      this->devIO_mp->write( val, ts);
      }
  catch (ACSErr::ACSbaseExImpl& ex)
      {
      completion = baciErrTypeDevIO::WriteErrorCompletion (ex, __FILE__, __LINE__,"RWSeqContImpl::setValue(...)");
      return;
      }

  completion = ACSErrTypeOK::ACSErrOKCompletion();
  completion.timeStamp = ts; // update timestamp with value set in DevIO
}//setValue

template <ACS_RW_C> 
ActionRequest RWSeqContImpl<ACS_RW_TL>::incrementAction(BACIComponent* component_p, int callbackID,
			  const CBDescIn& descIn, BACIValue* val,
			  Completion& completion, CBDescOut& descOut)
{
  ACE_UNUSED_ARG(val);
  CompletionImpl c;
  BACIValue value;

  this->getValueAction(component_p, callbackID, descIn, &value, c, descOut);
  
  if (c.isErrorFree()) // if there is no error
      {
      // let's try to set the values now
      TS dv = this->min_step();
      TM val = value.getValue(static_cast<TM*>(0));
      for (CORBA::ULong n = 0UL; n < val.length(); n++)
	  val[n] = val[n] + dv;
      value.setValue(val);
      this->setValueAction(component_p, callbackID, descIn, &value, c, descOut);

      if (c.isErrorFree())
	  {
	  completion = c;
	  // complete action requesting done invokation, 
	  // otherwise return reqInvokeWorking and set descOut.estimated_timeout
	  return reqInvokeDone;
	  }//if
      }//if

  // otherwise something went wrong
  completion = IncrementErrorCompletion(c,
					__FILE__,
					__LINE__,
					"baci::RWSeqContImpl&lt;&gt;::incrementAction");

  // complete action requesting done invokation, 
  // otherwise return reqInvokeWorking and set descOut.estimated_timeout
  return reqInvokeDone;
}//incrementAction



/// async. decrement value action implementation
template <ACS_RW_C> 
ActionRequest RWSeqContImpl<ACS_RW_TL>::decrementAction(BACIComponent* component_p, int callbackID,
			  const CBDescIn& descIn, BACIValue* val,
			  Completion& completion, CBDescOut& descOut)
{
  ACE_UNUSED_ARG(val);
  CompletionImpl c;
  BACIValue value;

  this->getValueAction(component_p, callbackID, descIn, &value, c, descOut);

   if (c.isErrorFree()) // if there is no error
      {
      // let's try to set the values now
      TS dv = this->min_step();
      TM val = value.getValue(static_cast<TM*>(0));
      for (CORBA::ULong n = 0UL; n < val.length(); n++)
	  val[n] = val[n] - dv;
      value.setValue(val);
      this->setValueAction(component_p, callbackID, descIn, &value, c, descOut);
      
      if (c.isErrorFree())
	  {
	  completion = c;
	  // complete action requesting done invokation, 
	  // otherwise return reqInvokeWorking and set descOut.estimated_timeout
	  return reqInvokeDone;
	  }//if
      }//if
   // otherwise something went wrong
   completion = DecrementErrorCompletion(c,
					 __FILE__,
					 __LINE__,
					 "baci::RWSeqContImpl&lt;&gt;::decrementAction");
  
  // complete action requesting done invokation, 
  // otherwise return reqInvokeWorking and set descOut.estimated_timeout
  return reqInvokeDone;
}//decrementAction







