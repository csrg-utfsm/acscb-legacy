package com.cosylab.cdb.jdal;

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
 * @author dragan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
import org.omg.CORBA.*;
import com.cosylab.CDB.*;
import java.net.InetAddress;
import alma.acs.util.ACSPorts;

public class ClearCache {
	public static void main(String args[]) {
		try {
			String curl = null;
			String strIOR = null;

			if (args.length >= 1 && !args[0].equals("-k")) {
				curl = args[0];
			}

			// test for IOR in cmd line
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-k")) {
					if (i < args.length - 1) {
						strIOR = args[++i];
					}
				}
			}
			if (strIOR == null) {
				// use default
				strIOR = "corbaloc::" + InetAddress.getLocalHost().getHostName() + ":" + ACSPorts.getCDBPort() + "/CDB";
			}

			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			JDAL dal = JDALHelper.narrow(orb.string_to_object(strIOR));
			
			if( curl != null ) {
				dal.clear_cache( curl );
				System.out.println( "clear_cache is invoked for " + curl );
			} 
			else {
				dal.clear_cache_all();
				System.out.println( "clear_cache is invoked for all curls" );
			}
		}
		catch (Exception e) {
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}
}
