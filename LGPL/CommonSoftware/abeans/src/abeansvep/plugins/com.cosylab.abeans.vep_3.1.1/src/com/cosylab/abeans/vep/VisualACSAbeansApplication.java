/*
 * Created on Feb 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.cosylab.abeans.vep;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import abeans.pluggable.acs.ACSAbeansEngine;

import com.cosylab.abeans.AbeansEngine;
import com.cosylab.abeans.AbeansLaunchable;
import com.cosylab.gui.framework.Desktop;
import com.cosylab.gui.framework.Launcher;
import com.cosylab.gui.framework.LauncherEnvironment;


/**
 * @author jkamenik
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VisualACSAbeansApplication extends AbeansLaunchable {

	/**
	 * 
	 */
	public VisualACSAbeansApplication() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public VisualACSAbeansApplication(Launcher arg0,
			LauncherEnvironment arg1, Desktop arg2, JInternalFrame arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public VisualACSAbeansApplication(Launcher arg0,
			LauncherEnvironment arg1, JApplet arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public VisualACSAbeansApplication(Launcher arg0,
			LauncherEnvironment arg1, JFrame arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}
		private ACSAbeansEngine engine;


			/* (non-Javadoc)
			 * @see com.cosylab.abeans.vep.AbeansLaunchable#getAbeansEngine()
			 */
			public ACSAbeansEngine getACSAbeansEngine() {
				if (engine == null) {
					engine = new ACSAbeansEngine(getClass().getName());
				}
				
				return engine;
				
			}
			/* (non-Javadoc)
			 * @see com.cosylab.abeans.vep.AbeansLaunchable#getAbeansEngine()
			 */
			public AbeansEngine getAbeansEngine() {
				// TODO Auto-generated method stub
				return getACSAbeansEngine();
			}
	}
