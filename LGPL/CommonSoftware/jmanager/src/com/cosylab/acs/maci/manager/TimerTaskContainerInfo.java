/*
 * @@COPYRIGHT@@
 */

package com.cosylab.acs.maci.manager;

import java.util.TimerTask;

import com.cosylab.acs.maci.Container;
import com.cosylab.acs.maci.ContainerInfo;
import com.cosylab.acs.maci.ClientInfo;

/**
 * <code>ContainerInfo</code> class containing <code>TimerTask<code> object.
 * 
 * @author		Matej Sekoranja (matej.sekoranja@cosylab.com)
 * @version	@@VERSION@@
 */
public class TimerTaskContainerInfo extends ContainerInfo
{

	transient private TimerTask task = null;

	/**
	 * Constructor for TimerTaskContainerInfo.
	 * @param handle
	 * @param name
	 * @param container
	 */
	public TimerTaskContainerInfo(int handle, String name, Container container)
	{
		super(handle, name, container);
	}

	/**
	 * Returns the task.
	 * @return TimerTask
	 */
	public TimerTask getTask()
	{
		return task;
	}

	/**
	 * Sets the task.
	 * @param task The task to set
	 */
	public void setTask(TimerTask task)
	{
		this.task = task;
	}
	
	/**
	 * Creates <ocde>ClientInfo</code> from this object.
	 * 
	 * @return	ClientInfo
	 */
	public ClientInfo createClientInfo()
	{
		ClientInfo clientInfo = new ClientInfo(getHandle(), getName(), getContainer());
		clientInfo.setAccessRights(0);
		clientInfo.setComponents(getComponents());
		
		return clientInfo;
	}	

}
