/*
 *    ALMA - Atacama Large Millimiter Array
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
 */
package alma.acs.exceptions;

import java.lang.reflect.Constructor;

import alma.ACSErr.ErrorTrace;
import alma.acs.util.UTCUtility;

/**
 * @author hsommer
 * created Sep 26, 2003 10:53:40 AM
 */
public class CorbaExceptionConverter
{
	public static final String PROPERTY_JAVAEXCEPTION_CLASS = "javaex.class";
	public static final String PROPERTY_JAVAEXCEPTION_MESSAGE = "javaex.msg";

	/**
	 * Converts an <code>ErrorTrace</code> object to a Java <code>Throwable</code>.
	 * The chain of caused-by exceptions is translated, too.
	 * <p>
	 * See the comment on class substitution at
	 * {@link DefaultAcsJException}.
	 * 
	 * @param et
	 * @return Throwable  current implementation always returns a subclass of 
	 * 					<code>AcsJException</code>, but don't rely on it yet.
	 */
	static Throwable recursiveGetThrowable(ErrorTrace et)
	{
		// check if underlying Java exception class is stored as a property
		String classname = ErrorTraceManipulator.getProperty(
			et, PROPERTY_JAVAEXCEPTION_CLASS);
				
		String message = ErrorTraceManipulator.getProperty(
			et, PROPERTY_JAVAEXCEPTION_MESSAGE);
			
		Throwable thr = null;
		
		if (classname != null)
		{
			// try if that class is available to be reconstructed
			try
			{
				Class exClass = Class.forName(classname);
				if (Throwable.class.isAssignableFrom(exClass))
				{
					if (!AcsJException.class.isAssignableFrom(exClass))
					{
						// non-ACS exceptions we don't reconstruct directly
						// because we'd loose the additional information
						// like line number etc. that ErrorTrace has.
						message = "original type " + exClass.getName() + ": " + 
									( message == null ? "" : message );
						exClass = DefaultAcsJException.class;
						
					}
					Constructor msgCtor = exClass.getConstructor(new Class[] {String.class});
					thr = (Throwable) msgCtor.newInstance(new Object[]{message});
				}
			}
			catch (Exception e)
			{
				message = "failed to reconstruct Java exception. Original msg was " + message;
				// just leave thr == null
			}
		}
		else
		{
			message = "reconstructed from non-Java exception. ";
		}
		
		if (thr == null)
		{
			// default ex
			thr = new DefaultAcsJException(message);			
		}
		
		resurrectThrowable(thr, et);
		
		return thr;
	}




	private static void resurrectThrowable(Throwable thr, ErrorTrace et)
	{
		if (thr == null || et == null)
		{
			throw new NullPointerException("resurrectThrowable: parameters must not be null!"); 
		}

		if (thr instanceof AcsJException)
		{
			// data specific to AcsJException
			AcsJException acsJEx = (AcsJException) thr;
			
			acsJEx.m_host = et.host;
			acsJEx.m_process = et.process;

			acsJEx.m_file = et.file;
			acsJEx.m_method = et.routine;
			acsJEx.m_line = et.lineNum;
			
			// todo check if error type and code match
			//et.errorType == acsJEx.getErrorType();
			//et.errorCode == acsJEx.getErrorCode();
			
			acsJEx.m_severity = et.severity; 
			acsJEx.m_threadName = et.thread;
			acsJEx.m_timeMilli = UTCUtility.utcOmgToJava(et.timeStamp);
			acsJEx.m_properties = ErrorTraceManipulator.getProperties(et);
		}

		if (et.previousError != null && et.previousError.length > 0 && 
			et.previousError[0] != null)
		{
			ErrorTrace etCause = et.previousError[0];
			if (etCause != null)
			{
				// recursion
				Throwable thrCause = recursiveGetThrowable(etCause);
				thr.initCause(thrCause);
			}
		}
	}


	public static void convertErrorTraceToJavaException(ErrorTrace et, AcsJException jEx)
	{
		resurrectThrowable(jEx, et);
	}
		
}
