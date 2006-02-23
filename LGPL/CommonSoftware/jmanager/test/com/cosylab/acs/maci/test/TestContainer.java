/*
 * @@COPYRIGHT@@
 */
package com.cosylab.acs.maci.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import abeans.core.defaults.MessageLogEntry;
import abeans.pluggable.RemoteException;
import abeans.pluggable.acs.logging.LoggingLevel;

import com.cosylab.acs.maci.Container;
import com.cosylab.acs.maci.Component;
import com.cosylab.acs.maci.ComponentInfo;

/**
 * Test implementation of an container.
 * 
 * @author Matej Sekoranja
 * @version @@VERSION@@
 */
public class TestContainer extends TestClient implements Container {

	protected Map supportedComponents = new HashMap();
	protected Map activatedComponents = new HashMap();
	protected long activationTime = 0;
	protected long deactivationTime = 0;
	protected int[] shutdownOrder = null;

	/**
	 * Constructor for TestContainer.
	 * @param name
	 * @param reply
	 */
	public TestContainer(String name, String reply) {
		super(name, reply);
	}

	/**
	 * @param name
	 */
	public TestContainer(String name) {
		this(name, "AR");
	}


	/**
	 * @see com.cosylab.acs.maci.Container#activate_COB(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public ComponentInfo activate_component(
		int handle,
		String name,
		String exe,
		String type)
		throws RemoteException {

		new MessageLogEntry(this, "Container '"+getName()+"': activating '"+name+"'.",LoggingLevel.DEBUG).dispatch();

		if (supportedComponents.containsKey(name))
		{
			// simulate activation
			try
			{
				Thread.sleep(activationTime);
			}
			catch (InterruptedException ie) {}
		
			Component cob = (Component)supportedComponents.get(name);
			
			if (cob instanceof TestComponent)
			{
				TestComponent tc = (TestComponent)cob; 
				tc.setHandle(handle);
				try
				{
					tc.activate();
				} catch (Exception ex) {
					throw new RemoteException(this, "Failed to construct(), error: " + ex.toString(), ex);
				}
			}
			
			ComponentInfo cobInfo = new ComponentInfo(handle, name, type, exe, cob);
			cobInfo.setContainer(this.handle);
			cobInfo.setContainerName(this.name);

			if (cob != null)			
				cobInfo.setInterfaces(new String[] {cob.getClass().getName()});

			synchronized (activatedComponents)
			{
				activatedComponents.put(new Integer(handle), cobInfo);
			}
			
			return cobInfo;
		}
		else
			return null;
	}

	/**
	 * @see com.cosylab.acs.maci.Container#deactivate_components(int[])
	 */
	public void deactivate_components(int[] handles) throws RemoteException {
		
		for (int i = 0; i < handles.length; i++)
		{
			synchronized (activatedComponents)
			{
				Integer key = new Integer(handles[i]);
				if (activatedComponents.containsKey(key))
				{

					ComponentInfo cobInfo = (ComponentInfo)activatedComponents.get(key);

					new MessageLogEntry(this, "Container '"+getName()+"': deactivating '"+cobInfo.getName()+"'.",LoggingLevel.DEBUG).dispatch();

					// simulate deactivation
					try
					{
						Thread.sleep(deactivationTime);
					}
					catch (InterruptedException ie) {}
					
					if (cobInfo.getComponent() instanceof TestComponent)
					{
						TestComponent tc = (TestComponent)cobInfo.getComponent(); 
						tc.deactivate();
						tc.setHandle(0);
					}

					activatedComponents.remove(key);
				}
			}
		}

	}

	/**
	 * @see com.cosylab.acs.maci.Container#get_COB_info(int[])
	 */
	public ComponentInfo[] get_component_info(int[] handles) throws RemoteException {
		
		ArrayList list = new ArrayList();
		
		synchronized (activatedComponents)
		{
			if (handles.length > 0)
			{
				// add only requested
				for (int i = 0; i < handles.length; i++)
				{
					Integer key = new Integer(handles[i]);
					if (activatedComponents.containsKey(key))
						list.add(activatedComponents.get(key));
				}
			}
			else
			{
				// add all
				list.addAll(activatedComponents.values());
			}
		}
				
		ComponentInfo[] infos = new ComponentInfo[list.size()];
		list.toArray(infos);
		return infos;
	}

	/**
	 * @see com.cosylab.acs.maci.Container#shutdown(int)
	 */
	public void shutdown(int action) throws RemoteException {
			// noop
	}

	/**
	 * Sets the supportedComponents.
	 * @param supportedComponents The supportedComponents to set
	 */
	public void setSupportedComponents(Map supportedComponents)
	{
		this.supportedComponents = supportedComponents;
	}

	/**
	 * Returns the supportedComponents.
	 * @return Map
	 */
	public Map getSupportedComponents()
	{
		return supportedComponents;
	}

	/**
	 * Returns the activationTime.
	 * @return long
	 */
	public long getActivationTime()
	{
		return activationTime;
	}

	/**
	 * Returns the deactivationTime.
	 * @return long
	 */
	public long getDeactivationTime()
	{
		return deactivationTime;
	}

	/**
	 * Sets the activationTime.
	 * @param activationTime The activationTime to set
	 */
	public void setActivationTime(long activationTime)
	{
		this.activationTime = activationTime;
	}

	/**
	 * Sets the deactivationTime.
	 * @param deactivationTime The deactivationTime to set
	 */
	public void setDeactivationTime(long deactivationTime)
	{
		this.deactivationTime = deactivationTime;
	}

	/**
	 * Returns the activatedComponents.
	 * @return Map
	 */
	public Map getActivatedComponents()
	{
		return activatedComponents;
	}

	/**
	 * Sets the activatedComponents.
	 * @param activatedComponents The activatedComponents to set
	 */
	public void setActivatedComponents(Map activatedComponents)
	{
		this.activatedComponents = activatedComponents;
	}

	/**
	 * @see com.cosylab.acs.maci.Container#restart_component(int)
	 */
	public Component restart_component(int handle) throws RemoteException {
		
		synchronized (activatedComponents)
		{
			Integer key = new Integer(handle);
			if (activatedComponents.containsKey(key))
			{

				ComponentInfo cobInfo = (ComponentInfo)activatedComponents.get(key);

				new MessageLogEntry(this, "Container '"+getName()+"': restarting '"+cobInfo.getName()+"'.", LoggingLevel.DEBUG).dispatch();

				// simulate restart
				try
				{
					Thread.sleep(deactivationTime+activationTime);
				}
				catch (InterruptedException ie) {}
				
				return cobInfo.getComponent();
					
			}
			else
				return null;
			
		}
		
	}

	/* (non-Javadoc)
	 * @see com.cosylab.acs.maci.Container#set_component_shutdown_order(int[])
	 */
	public void set_component_shutdown_order(int[] handles)
			throws RemoteException {
		this.shutdownOrder = handles;
	}

	/**
	 * Get (set) shutdown order (accessor for testing).
	 */
	public int[] get_component_shutdown_order() {
		return shutdownOrder;
	}
}
