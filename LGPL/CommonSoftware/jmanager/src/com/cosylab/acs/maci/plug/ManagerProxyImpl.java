/*
 * @@COPYRIGHT@@
 */

package com.cosylab.acs.maci.plug;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import abeans.core.CoreException;
import abeans.core.Identifiable;
import abeans.core.Identifier;
import abeans.core.IdentifierSupport;
import abeans.core.defaults.MessageLogEntry;
import abeans.framework.ReportEvent;
import abeans.framework.ApplicationContext;

import com.cosylab.acs.maci.AccessRights;
import com.cosylab.acs.maci.BadParametersException;
import com.cosylab.acs.maci.Component;
import com.cosylab.acs.maci.ComponentSpecIncompatibleWithActiveComponentException;
import com.cosylab.acs.maci.ComponentStatus;
import com.cosylab.acs.maci.ComponentSpec;
import com.cosylab.acs.maci.IncompleteComponentSpecException;
import com.cosylab.acs.maci.InvalidComponentSpecException;
import com.cosylab.acs.maci.Manager;
import com.cosylab.acs.maci.NoDefaultComponentException;
import com.cosylab.acs.maci.NoPermissionException;
import com.cosylab.acs.maci.NoResourcesException;
import com.cosylab.acs.maci.StatusHolder;
import com.cosylab.acs.maci.StatusSeqHolder;
import com.cosylab.acs.maci.manager.CURLHelper;
import com.cosylab.acs.maci.manager.ManagerImpl;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.Object;
import org.omg.CORBA.UNKNOWN;

import si.ijs.maci.ComponentSpecIncompatibleWithActiveComponent;
import si.ijs.maci.Container;
import si.ijs.maci.ContainerInfo;
import si.ijs.maci.ContainerHelper;
import si.ijs.maci.AdministratorHelper;
import si.ijs.maci.ComponentInfo;
import si.ijs.maci.Client;
import si.ijs.maci.ClientHelper;
import si.ijs.maci.ClientInfo;
import si.ijs.maci.IncompleteComponentSpec;
import si.ijs.maci.InvalidComponentSpec;
import si.ijs.maci.ManagerPOA;
import si.ijs.maci.NoDefaultComponent;
import si.ijs.maci.ulongSeqHolder;

/**
 * Manager is the central point of interaction between the components
 * and the clients that request MACI services. A Manager is
 * responsible for managing a domain.
 * Manager has the following functionality:
 *	<UL>
 *	<LI>It is the communication entry point.</LI>
 *	<LI>It performs as a name service, resolving CURLs into Component references.</LI>
 *	<LI>It delegates the Component life cycle management to the Container.</LI>
 *	<LI>It provides information about the whole domain.</LI>
 *	</UL>
 *
 * This class acts like a proxy and delegates all requests to the given implementation.
 *
 * @author		Matej Sekoranja (matej.sekoranja@cosylab.com)
 * @version	@@VERSION@@
 */
public class ManagerProxyImpl extends ManagerPOA implements Identifiable
{
	/**
	 * Identifier.
	 */
	private Identifier id = null;

	/**
	 * Implementation of the manager to which all requests are delegated.
	 */
	private Manager manager;

	/**
	 * Identifier.
	 */
	private SynchronizedInt pendingRequests = new SynchronizedInt(0);

	/**
	 * Construct a new Manager which will <code>manager</code> implementation.
	 * @param	manager		implementation of the manager, non-<code>null</code>.
	 */
	public ManagerProxyImpl(Manager manager)
	{
		assert (manager != null);
		this.manager = manager;
	}

	/**
	 * Return the fully qualified name of the domain, e.g., "antenna1.alma.nrao".
	 *
	 * @return the fully qualified name of the domain
	 */
	public String domain_name()
	{
		pendingRequests.increment();
		try
		{
			return manager.getDomain();
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "domain_name");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Get all the information that the Manager has about its known Containers.
	 * To invoke this method, the caller must have INTROSPECT_MANAGER access rights, or it must be the object whose info it is requesting.
	 * Calling this function does not affect the internal state of the Manager.
	 *
	 * @param id Identification of the caller.
	 * @param h Handles of the containers whose information is requested. If this is an empty sequence, the name_wc parameter is used.
	 * @param name_wc Wildcard that the container's name must match in order for its information to be returned.
	 * @return A sequence of ContainerInfo structures containing the entire Manager's knowledge about the containers.
	 *		  If access is denied to a subset of objects, the handles to those objects are set to 0.
	 */
	public ContainerInfo[] get_container_info(int id, int[] h, String name_wc)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_container_info", new java.lang.Object[] { new Integer(id), h, name_wc } ).dispatch();

			// invalid info (replacement for null)
			final ContainerInfo invalidInfo = new ContainerInfo("<invalid>", 0, null, new int[0]);

			// returned value
			ContainerInfo[] retVal = null;

			// transform to CORBA specific
			com.cosylab.acs.maci.ContainerInfo[] infos = manager.getContainerInfo(id, h, name_wc);
			if (infos != null)
			{
				retVal = new ContainerInfo[infos.length];
				for (int i = 0; i < infos.length; i++)
					if (infos[i] == null)
						retVal[i] = invalidInfo;
					else
						retVal[i] = new ContainerInfo(infos[i].getName(),
													   infos[i].getHandle(),
													   (Container)((ContainerProxy)infos[i].getContainer()).getClient(),
													   infos[i].getComponents().toArray());
			}
			else
				retVal = new ContainerInfo[0];

			if (isDebug())
				new MessageLogEntry(this, "get_container_info", "Exiting.", Level.FINEST).dispatch();

			return retVal;
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_container_info");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_container_info");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_container_info");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_container_info");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Get all the information that the Manager has about its known clients.
	 * To invoke this method, the caller must have INTROSPECT_MANAGER access rights, or it must be the object whose info it is requesting.
	 * Calling this function does not affect the internal state of the Manager.
	 *
	 * @param id Identification of the caller.
	 * @param h Handles of the clients whose information is requested. If this is an empty sequence, the name_wc parameter is used.
	 * @param name_wc Wildcard that the clients's name must match in order for its information to be returned.
	 * @return A sequence of ClientInfo structures containing the entire Manager's knowledge about the containers.
	 *		  If access is denied to a subset of objects, the handles to those objects are set to 0.
	 */
	public ClientInfo[] get_client_info(int id, int[] h, String name_wc)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_client_info", new java.lang.Object[] { new Integer(id), h, name_wc } ).dispatch();

			// invalid info (replacement for null)
			final ClientInfo invalidInfo = new ClientInfo(0, null, new int[0], "<invalid>", 0);

			// returned value
			ClientInfo[] retVal = null;

			// transform to CORBA specific
			com.cosylab.acs.maci.ClientInfo[] infos = manager.getClientInfo(id, h, name_wc);
			if (infos != null)
			{
				retVal = new ClientInfo[infos.length];
				for (int i = 0; i < infos.length; i++)
					if (infos[i] == null)
						retVal[i] = invalidInfo;
					else
						retVal[i] = new ClientInfo(infos[i].getHandle(),
												    ((ClientProxy)infos[i].getClient()).getClient(),
													infos[i].getComponents().toArray(),
													infos[i].getName(),
													mapAccessRights(infos[i].getAccessRights()));
			}
			else
				retVal = new ClientInfo[0];

			if (isDebug())
				new MessageLogEntry(this, "get_client_info", "Exiting.", Level.FINEST).dispatch();

			return retVal;
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_client_info");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_client_info");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_client_info");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_client_info");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Get all the information that the Manager has about components.
	 * To invoke this method, the caller must have INTROSPECT_MANAGER access rights, or it must have adequate privileges to access the Component (the same as with the get_component method).
	 * Information about all components is returned, unless the active_only parameter is set to true,
	 * in which case only information about those components that are currently registered with the Manager
	 * and activated is returned.
	 * Calling this function does not affect the internal state of the Manager.
	 *
	 * @param id Identification of the caller.
	 * @param h Handles of the components whose information is requested. If this is an empty sequence, the name_wc and type_wc parameters are used.
	 * @param name_wc Wildcard that the Component's name must match in order for its information to be returned.
	 * @param type_wc Wildcard that the Component's type must match in order for its information to be returned.
	 * @param active_only
	 * @return A sequence of ComponentInfo structures containing the entire Manager's knowledge about the components.
	 *		  If access is denied to a subset of objects, the handles to those objects are set to 0.
	 */
	public ComponentInfo[] get_component_info(int id, int[] h, String name_wc, String type_wc, boolean active_only)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_component_info", new java.lang.Object[] { new Integer(id), h, name_wc, type_wc, new Boolean(active_only) } ).dispatch();

			// invalid info (replacement for null)
			final ComponentInfo invalidInfo = new ComponentInfo("<invalid>", "<invalid>", null, "<invalid>", new int[0], 0, "<invalid>", 0, 0, new String[0]);

			// returned value
			ComponentInfo[] retVal = null;

			// transform to CORBA specific
			com.cosylab.acs.maci.ComponentInfo[] infos = manager.getComponentInfo(id, h, name_wc, type_wc, active_only);
			if (infos != null)
			{
				retVal = new ComponentInfo[infos.length];
				for (int i = 0; i < infos.length; i++)
					if (infos[i] == null)
						retVal[i] = invalidInfo;
					else
					{
						Object obj = null;
						if (infos[i].getComponent() != null)
							obj = (Object)infos[i].getComponent().getObject();
						String[] interfaces;
						if (infos[i].getInterfaces() != null)
							interfaces = infos[i].getInterfaces();
						else
							interfaces = new String[0];
						retVal[i] = new ComponentInfo(infos[i].getType(),
												 infos[i].getCode(),
											     obj,
												 infos[i].getName(),
												 infos[i].getClients().toArray(),
												 infos[i].getContainer(),
												 infos[i].getContainerName(),
												 infos[i].getHandle(),
												 mapAccessRights(infos[i].getAccessRights()),
												 interfaces);
					}
			}
			else
				retVal = new ComponentInfo[0];

			if (isDebug())
				new MessageLogEntry(this, "get_component_info", "Exiting.", Level.FINEST).dispatch();

			return retVal;
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_component_info");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_component_info");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_component_info");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_component_info");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Get a Component, activating it if necessary.
	 * The client represented by id (the handle)
	 * must have adequate access rights to access the Component. This is untrue of components:
	 * components always have unlimited access rights to other components.
	 *
	 * @param id Identification of the caller. If this is an invalid handle, or if the caller does not have enough access rights, a CORBA::NO_PERMISSION exception is raised.
	 * @param component_url CURL of the Component whose reference is to be retrieved.
	 * @param activate True if the Component is to be activated in case it does not exist. If set to False, and the Component does not exist, a nil reference is returned and status is set to COMPONENT_NOT_ACTIVATED.
	 * @param status Status of the request. One of COMPONENT_ACTIVATED, COMPONENT_NONEXISTANT and COMPONENT_NOT_ACTIVATED.
	 * @return Reference to the Component. If the Component could not be activated, a nil reference is returned,
	 *		  and the status contains an error code detailing the cause of failure (one of the COMPONENT_* constants).
	 */
	public Object get_component(int id, String component_url, boolean activate, IntHolder status)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_component", new java.lang.Object[] { new Integer(id), component_url, new Boolean(activate), status } ).dispatch();

			// returned value
			Object retVal = null;

			// returned status
			StatusHolder statusHolder = new StatusHolder();

			// transform to CORBA specific
			URI uri = null;
			if (component_url != null)
				uri = CURLHelper.createURI(component_url);
			Component component = manager.getComponent(id, uri, activate, statusHolder);

			// map status
			status.value = mapStatus(statusHolder.getStatus());

			// extract Component CORBA reference
			if (component != null)
				retVal = (Object)component.getObject();

			if (isDebug())
				new MessageLogEntry(this, "get_component", "Exiting.", Level.FINEST).dispatch();

			return retVal;
		}
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "get_component");
			hbpe.putValue("component_url", component_url);
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Used for retrieving several components with one call.
	 *
	 * @param id Identification of the caller. If this is an invalid handle, or if the caller does not have enough access rights, a CORBA::NO_PERMISSION exception is raised.
	 * @param component_urls CURL of the components whose reference is to be retrieved.
	 * @param activate True if the Component is to be activated in case it does not exist. If set to False, and the Component does not exist, a nil reference is returned and status is set to COMPONENT_NOT_ACTIVATED.
	 * @param status Status of the request. One of COMPONENT_ACTIVATED, COMPONENT_NONEXISTANT and COMPONENT_NOT_ACTIVATED.
	 * @see get_component
	 * @return A sequence of requested components.
	 */
	public Object[] get_components(int id, String[] component_urls, boolean activate,	ulongSeqHolder status)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_components", new java.lang.Object[] { new Integer(id), component_urls, new Boolean(activate), status } ).dispatch();

			// returned value
			Object[] retVal = null;

			// returned status
			StatusSeqHolder statusHolder = new StatusSeqHolder();

			// map strings to CURLs
			URI[] uris = null;
			if (component_urls != null)
			{
				uris = new URI[component_urls.length];
				for (int i = 0; i < component_urls.length; i++)
				{
					try
					{
						if (component_urls[i] != null)
							uris[i] = CURLHelper.createURI(component_urls[i]);
					}
					catch (URISyntaxException usi)
					{
						BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
						hbpe.caughtIn(this, "get_components");
						hbpe.putValue("component_urls[i]", component_urls[i]);
						// exception service will handle this
					}
				}
			}

			// transform to CORBA specific
			Component[] components = manager.getComponents(id, uris, activate, statusHolder);

			// map statuses
			status.value = new int[statusHolder.getStatus().length];
			for (int i = 0; i < statusHolder.getStatus().length; i++)
				status.value[i] = mapStatus(statusHolder.getStatus()[i]);

			// extract Component CORBA references
			if (components != null)
			{
				retVal = new Object[components.length];

				for (int i = 0; i < components.length; i++)
					if (components[i] != null)
						retVal[i] = (Object)(components[i].getObject());
			}
			else
				retVal = new Object[0];

			if (isDebug())
				new MessageLogEntry(this, "get_components", "Exiting.", Level.FINEST).dispatch();

			return retVal;
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_components");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_components");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_components");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_components");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Login to MACI.
	 * Containers, Clients and Administrative clients call this function
	 * first to identify themselves with the Manager. The Manager authenticates them
	 * (through the authenticate function), and assigns them access rights and a handle,
	 * through which they will identify themselves at subsequent calls to the Manager.
	 *
	 * @param reference A reference to the Client.
	 * @return A ClientInfo structure with handle (h) and access fields filled-in.
	 * 			If the client with this name did not logout prior to calling login,
	 *			the components sequence in ClientInfo contains the handles of all components that
	 *			the client was using. (For Containers, the components sequence contains
	 *			handles of all components previously hosted by the Container.)
	 */
	public ClientInfo login(Client reference)
	{
		pendingRequests.increment();
		try
		{
			// NOTE: do not log "reference" - prevents GC to finalize and terminate connection thread (JacORB)
			if (isDebug())
				new MessageLogEntry(this, "login", new java.lang.Object[] { reference == null ? "null" : reference.toString() } ).dispatch();

			// client proxy
			com.cosylab.acs.maci.Client clientProxy = null;

			// create approperiate proxies
			if (reference != null)
			{
				if (reference._is_a(ContainerHelper.id()))
				{
					clientProxy = new ContainerProxy(ContainerHelper.narrow(reference));
				}
				else if (reference._is_a(AdministratorHelper.id()))
				{
					clientProxy = new AdministratorProxy(AdministratorHelper.narrow(reference));
				}
				else if (reference._is_a(ClientHelper.id()))
				{
					clientProxy = new ClientProxy(reference);
				}
				else
				{
					// this should never happen, but we are carefuly anyway
					BadParametersException af = new BadParametersException(this, "Given reference does not implement 'maci::Client' interface.");
					af.caughtIn(this, "login");
					// exception service will handle this
					reportException(af);

					throw new BAD_PARAM(af.getMessage());
				}
			}


			ClientInfo retVal = null;

			com.cosylab.acs.maci.ClientInfo info = manager.login(clientProxy);
			if (info != null)
				retVal = new ClientInfo(info.getHandle(),
										((ClientProxy)info.getClient()).getClient(),
										info.getComponents().toArray(),
										info.getName(),
										mapAccessRights(info.getAccessRights()));


			return retVal;
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "login");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "login");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "login");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "login");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Logout from MACI.
	 *
	 * @param id Handle of the Client that is logging out
	 */
	public void logout(int id)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "logout", new java.lang.Object[] { new Integer(id) } ).dispatch();

			// simply logout
			manager.logout(id);
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "logout");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "logout");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "logout");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "logout");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Register a CORBA object as a Component, assigning it a CURL and making it accessible through the Manager.
	 * The Component is treated as an immortal Component.
	 *
	 * @param id Identification of the caller. The caller must have the REGISTER_component access right to perform this operation.
	 * @param component_url CURL that will be assigned to the object. The CURL must be in the Manager's domain, otherwise a fundamental property of domains that one computer belongs to only one domain would be too easy to violate.
	 * @param type Type of the Component
	 * @param Component Reference to the CORBA object (Component).
	 * @return Returns the handle of the newly created Component.
	 */
	public int register_component(int id, String component_url, String type, Object component)
	{
		pendingRequests.increment();
		try
		{
			// NOTE: do not log "component" - prevents GC to finalize and terminate connection thread (JacORB)
			if (isDebug())
				new MessageLogEntry(this, "register_component", new java.lang.Object[] { new Integer(id), component_url, type, component == null ? "null" : component.toString() } ).dispatch();

			// simply register
			URI uri = null;
			if (component_url != null)
				uri = CURLHelper.createURI(component_url);
			return manager.registerComponent(id, uri, type, new ComponentProxy(component_url, component));
		}
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "register_component");
			hbpe.putValue("component_url", component_url);
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "register_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "register_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "register_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "register_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Change mortality state of an component.
	 * Compnent must be already active, otherwise CORBA::NO_RESOURCE exception will be thrown.
	 * The caller must be an owner of an component or have administator rights,
	 * otherwise CORBA::NO_PERMISSION exception will be thrown.
	 * 
	 * @param id Identification of the caller. The caller must be an owner of an component or have administator rights.
	 * @param component_url The CURL of the component whose mortality to change.
	 * @param immortal_state New mortality state.
	 **/
	public void make_component_immortal(int id, String component_url, boolean immortal_state)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "make_component_immortal", new java.lang.Object[] { new Integer(id), component_url, new Boolean(immortal_state) } ).dispatch();

			// simply release Component
			URI uri = null;
			if (component_url != null)
				uri = CURLHelper.createURI(component_url);
			manager.makeComponentImmortal(id, uri, immortal_state);
		}
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "make_component_immortal");
			hbpe.putValue("component_url", component_url);
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "make_component_immortal");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "make_component_immortal");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "make_component_immortal");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "make_component_immortal");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Release a Component.
	 * In order for this operation to be possible, the caller represented by the id
	 * must have previously successfuly requested the Component via a call to get_component.
	 * Releasing a Component more times than requesting it should be avoided, but it produces no errors.
	 *
	 * @param id Identification of the caller. The caller must have previously gotten the Component through get_component.
	 * @param component_url The CURL of the Component to be released.
	 * @return Number of clients that are still using the Component after the operation completed.
	 *		  This is a useful debugging tool.
	 */
	public int release_component(int id, String component_url)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "release_component", new java.lang.Object[] { new Integer(id), component_url } ).dispatch();

			// simply release Component
			URI uri = null;
			if (component_url != null)
				uri = CURLHelper.createURI(component_url);
			return manager.releaseComponent(id, uri);
		}
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "release_component");
			hbpe.putValue("component_url", component_url);
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "release_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "release_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "release_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "release_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Release components.
	 *
	 * @param id Identification of the caller. The caller must have previously gotten the Component through get_component.
	 * @param component_url The CURL of the Component to be released.
	 * @see release_component
	 */
	public void release_components(int id, String[] component_urls)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "release_components", new java.lang.Object[] { new Integer(id), component_urls } ).dispatch();

			// map strings to CURLs
			URI[] uris = null;
			if (component_urls != null)
			{
				uris = new URI[component_urls.length];
				for (int i = 0; i < component_urls.length; i++)
				{
					try
					{
						if (component_urls[i] != null)
							uris[i] = CURLHelper.createURI(component_urls[i]);
					}
					catch (URISyntaxException usi)
					{
						BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
						hbpe.caughtIn(this, "release_components");
						hbpe.putValue("component_urls[i]", component_urls[i]);
						// exception service will handle this
					}
				}
			}

			// simply release components
			manager.releaseComponents(id, uris);
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "release_components");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "release_components");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "release_components");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "release_components");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Forcefully release a Component.
	 *
	 * @param id Identification of the caller. 
	 * @param component_url The CURL of the Component to be released.
	 * @return Number of clients that are still using the Component after the operation completed.
	 *		  This is a useful debugging tool.
	 */
	public int force_release_component(int id, String component_url)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "force_release_component", new java.lang.Object[] { new Integer(id), component_url } ).dispatch();

			// simply release Component
			URI uri = null;
			if (component_url != null)
				uri = CURLHelper.createURI(component_url);
			return manager.forceReleaseComponent(id, uri);
		}
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "force_release_component");
			hbpe.putValue("component_url", component_url);
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "force_release_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "force_release_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "force_release_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "force_release_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Shutdown the Manager.
	 * <B>Warning:</B> This call will also deactivate all components active in the system, including startup and immortal components.
	 *
	 * @param id Identification of the caller. The caller must have the SHUTDOWN_SYSTEM access right.
	 * @param containers The code to send to shutdown methods of all Containers. If 0, the Container's shutdown methods are not called.
	 */
	public void shutdown(int id, int containers)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "shutdown", new java.lang.Object[] { new Integer(id), new Integer(containers) } ).dispatch();

			// simply shutdown
			manager.shutdown(id, containers);
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "shutdown");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "shutdown");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "shutdown");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "shutdown");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}

	}

	/**
	 * Unregister a Component from the Manager.
	 *
	 * @param id Identification of the caller. The caller must have the REGISTER_COMPONENT access right to perform this operation.
	 * @param h Component's handle. The Component must have been previously registered through the call to register_component. If there are clients still using this Component,
	 * a components_unavailable notification is issued to all of them, and the Component is unregistered.
	 */
	public void unregister_component(int id, int h)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "unregister_component", new java.lang.Object[] { new Integer(id), new Integer(h) } ).dispatch();

			// simply unregistercomponent
			manager.unregisterComponent(id, h);
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "unregister_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "unregister_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "unregister_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "unregister_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Returns the default component of specific type.
	 * @param	id		identification of the caller.
	 * @param	type	component type, IDL ID.
	 * @return	<code>ComponentInfo</code> of requested component.
	 */
	public ComponentInfo get_default_component(int id, String type) throws NoDefaultComponent
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_default_component", new java.lang.Object[] { new Integer(id), type } ).dispatch();

			// invalid info (replacement for null)
			final ComponentInfo invalidInfo = new ComponentInfo("<invalid>", "<invalid>", null, "<invalid>", new int[0], 0, "<invalid>", 0, 0, new String[0]);

			// returned value
			ComponentInfo retVal = null;

			// transform to CORBA specific
			com.cosylab.acs.maci.ComponentInfo info = manager.getDefaultComponent(id, type);
			if (info != null)
			{
				Object obj = null;
				if (info.getComponent() != null)
					obj = (Object)info.getComponent().getObject();
				String[] interfaces;
				if (info.getInterfaces() != null)
					interfaces = info.getInterfaces();
				else
					interfaces = new String[0];
					
				retVal = new ComponentInfo(info.getType(),
										    info.getCode(),
											obj,
											info.getName(),
											info.getClients().toArray(),
											info.getContainer(),
											info.getContainerName(),
										 	info.getHandle(),
										 	mapAccessRights(info.getAccessRights()),
										 	interfaces);
			}
			else
				retVal = invalidInfo;

			if (isDebug())
				new MessageLogEntry(this, "get_default_component", "Exiting.", Level.FINEST).dispatch();

			return retVal;

		}
		catch (NoDefaultComponentException ndce)
		{
			NoDefaultComponentException hndce = new NoDefaultComponentException(this, ndce.getMessage(), ndce);
			hndce.caughtIn(this, "get_default_component");
			// exception service will handle this
			reportException(hndce);

			// rethrow CORBA specific
			throw new NoDefaultComponent(ndce.getMessage());
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_default_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_default_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_default_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_default_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Activation of an dynamic component.
	 * @param	id 	identification of the caller.
	 * @param	c	component to be obtained.
	 * @param	mark_as_default	mark component as default component of its type.
	 * @return	<code>ComponentInfo</code> of requested component.
	 */
	public ComponentInfo get_dynamic_component(int id, si.ijs.maci.ComponentSpec c, boolean mark_as_default)
		throws IncompleteComponentSpec, InvalidComponentSpec, ComponentSpecIncompatibleWithActiveComponent
	{
		pendingRequests.increment();

		// invalid info (replacement for null)
		final ComponentInfo invalidInfo = new ComponentInfo("<invalid>", "<invalid>", null, "<invalid>", new int[0], 0, "<invalid>", 0, 0, new String[0]);

		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_dynamic_component", new java.lang.Object[] { new Integer(id), c, new Boolean(mark_as_default) } ).dispatch();

			// returned value
			ComponentInfo retVal = null;

			/*
			URI uri = null;
			if (c.component_name != null)
				uri = CURLHelper.createURI(c.component_name);
			ComponentSpec componentSpec = new ComponentSpec(uri, c.component_type, c.component_code, c.container_name);
			*/
			// TODO si.ijs.maci.COMPONENT_SPEC_ANY -> ComponentSpec.COMPSPEC_ANY
			ComponentSpec componentSpec = new ComponentSpec(c.component_name, c.component_type, c.component_code, c.container_name);
			com.cosylab.acs.maci.ComponentInfo info = manager.getDynamicComponent(id, componentSpec, mark_as_default);

			// transform to CORBA specific
			if (info != null)
			{
				Object obj = null;
				if (info.getComponent() != null)
					obj = (Object)info.getComponent().getObject();
				String[] interfaces;
				if (info.getInterfaces() != null)
					interfaces = info.getInterfaces();
				else
					interfaces = new String[0];
					
				retVal = new ComponentInfo(info.getType(),
										   info.getCode(),
											obj,
											info.getName(),
											info.getClients().toArray(),
											info.getContainer(),
											info.getContainerName(),
											info.getHandle(),
											mapAccessRights(info.getAccessRights()),
											interfaces);
			}
			else
				retVal = invalidInfo;

			if (isDebug())
				new MessageLogEntry(this, "get_dynamic_component", "Exiting.", Level.FINEST).dispatch();

			return retVal;

		}
		/*
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "get_dynamic_component");
			hbpe.putValue("c.component_name", c.component_name);
			// exception service will handle this
			reportException(hbpe);
	
			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}*/
		catch (IncompleteComponentSpecException icse)
		{
			IncompleteComponentSpecException hicse = new IncompleteComponentSpecException(this, icse.getMessage(), icse, icse.getIncompleteSpec());
			hicse.caughtIn(this, "get_dynamic_component");
			// exception service will handle this
			reportException(hicse);

			throw new IncompleteComponentSpec(icse.getMessage(),
							new si.ijs.maci.ComponentSpec(
								//icse.getIncompleteSpec().getCURL().getPath(),
								icse.getIncompleteSpec().getName(),
								icse.getIncompleteSpec().getType(),
								icse.getIncompleteSpec().getCode(),
								icse.getIncompleteSpec().getContainer())
						);
		}
		catch (InvalidComponentSpecException incse)
		{
			InvalidComponentSpecException hincse = new InvalidComponentSpecException(this, incse.getMessage(), incse, incse.getInvalidSpec());
			hincse.caughtIn(this, "get_dynamic_component");
			// exception service will handle this
			reportException(hincse);
			
			throw new InvalidComponentSpec(incse.getMessage());
		}
		catch (ComponentSpecIncompatibleWithActiveComponentException ciace)
		{
			ComponentSpecIncompatibleWithActiveComponentException hciace = new ComponentSpecIncompatibleWithActiveComponentException(this, ciace.getMessage(), ciace, ciace.getActiveComponentSpec());
			hciace.caughtIn(this, "get_dynamic_component");
			// exception service will handle this
			reportException(hciace);

			// rethrow CORBA specific
			throw new ComponentSpecIncompatibleWithActiveComponent(ciace.getMessage(),
							new si.ijs.maci.ComponentSpec(
								//ciace.getActiveComponentSpec().getCURL().getPath(),
								ciace.getActiveComponentSpec().getName(),
								ciace.getActiveComponentSpec().getType(),
								ciace.getActiveComponentSpec().getCode(),
								ciace.getActiveComponentSpec().getContainer())
						);
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_dynamic_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_dynamic_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_dynamic_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_dynamic_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Group request of dynamic components.
	 * @param	id 	identification of the caller.
	 * @param	c	components to be obtained.
	 * @return	<code>ComponentInfo[]</code> of requested components.
	 * @see #get_dynamic_component
	 */
	public ComponentInfo[] get_dynamic_components(int id, si.ijs.maci.ComponentSpec[] components)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_dynamic_components", new java.lang.Object[] { new Integer(id), components } ).dispatch();

			// invalid info (replacement for null)
			final ComponentInfo invalidInfo = new ComponentInfo("<invalid>", "<invalid>", null, "<invalid>", new int[0], 0, "<invalid>", 0, 0, new String[0]);

			// returned value
			ComponentInfo[] retVal = null;
			
			// map strings to CURLs
			ComponentSpec[] componentSpecs = null;
			if (components != null)
			{
				componentSpecs = new ComponentSpec[components.length];
				for (int i = 0; i < components.length; i++)
				{
					//try
					//{
						if (components[i] != null)
							// TODO si.ijs.maci.COMPONENT_SPEC_ANY -> ComponentSpec.COMPSPEC_ANY
							componentSpecs[i] = new ComponentSpec(
											//CURLHelper.createURI(components[i].component_name),
											components[i].component_name,
											components[i].component_type,
											components[i].component_code,
											components[i].container_name);
					/*}
					catch (URISyntaxException usi)
					{
						BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
						hbpe.caughtIn(this, "get_dynamic_components");
						hbpe.putValue("components[i].component_name", components[i].component_name);
						// exception service will handle this
					}*/
				}
			}
			com.cosylab.acs.maci.ComponentInfo[] infos = manager.getDynamicComponents(id, componentSpecs);

			// transform to CORBA specific
			if (infos != null)
			{
				retVal = new ComponentInfo[infos.length];
				for (int i = 0; i < infos.length; i++)
					if (infos[i] == null)
						retVal[i] = invalidInfo;
					else
					{
						Object obj = null;
						if (infos[i].getComponent() != null)
							obj = (Object)infos[i].getComponent().getObject();
						String[] interfaces;
						if (infos[i].getInterfaces() != null)
							interfaces = infos[i].getInterfaces();
						else
							interfaces = new String[0];
						retVal[i] = new ComponentInfo(infos[i].getType(),
												 infos[i].getCode(),
												 obj,
												 infos[i].getName(),
												 infos[i].getClients().toArray(),
												 infos[i].getContainer(),
												 infos[i].getContainerName(),
												 infos[i].getHandle(),
												 mapAccessRights(infos[i].getAccessRights()),
												 interfaces);
					}
			}
			else
				retVal = new ComponentInfo[0];

			if (isDebug())
				new MessageLogEntry(this, "get_dynamic_components", "Exiting.", Level.FINEST).dispatch();

			return retVal;
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_dynamic_components");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_dynamic_components");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_dynamic_components");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_dynamic_components");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	
	/**
	 * Activation of an co-deployed component.
	 * @param	id 	identification of the caller.
	 * @param	c	component to be obtained.
	 * @param	mark_as_default	mark component as default component of its type.
	 * @param	target_component	target co-deployed component.
	 * @return	<code>ComponentInfo</code> of requested co-deployed component.
	 */
	public ComponentInfo get_collocated_component(int id, si.ijs.maci.ComponentSpec c,
			boolean mark_as_default, String target_component)
		throws IncompleteComponentSpec,	InvalidComponentSpec, ComponentSpecIncompatibleWithActiveComponent
	{
		pendingRequests.increment();

		// invalid info (replacement for null)
		final ComponentInfo invalidInfo = new ComponentInfo("<invalid>", "<invalid>", null, "<invalid>", new int[0], 0, "<invalid>", 0, 0, new String[0]);

		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_collocated_component", new java.lang.Object[] { new Integer(id), c, new Boolean(mark_as_default), target_component } ).dispatch();

			// returned value
			ComponentInfo retVal = null;

			/*
			URI uri = null;
			if (c.component_name != null)
				uri = CURLHelper.createURI(c.component_name);
			ComponentSpec componentSpec = new ComponentSpec(uri, c.component_type, c.component_code, c.container_name);
			*/
			URI targetComponentURI = null;
			if (target_component != null)
				targetComponentURI = CURLHelper.createURI(target_component);

			// TODO si.ijs.maci.COMPONENT_SPEC_ANY -> ComponentSpec.COMPSPEC_ANY
			ComponentSpec componentSpec = new ComponentSpec(c.component_name, c.component_type, c.component_code, c.container_name);
			com.cosylab.acs.maci.ComponentInfo info = manager.getCollocatedComponent(id, componentSpec, mark_as_default, targetComponentURI);

			// transform to CORBA specific
			if (info != null)
			{
				Object obj = null;
				if (info.getComponent() != null)
					obj = (Object)info.getComponent().getObject();
				String[] interfaces;
				if (info.getInterfaces() != null)
					interfaces = info.getInterfaces();
				else
					interfaces = new String[0];
					
				retVal = new ComponentInfo(info.getType(),
										   info.getCode(),
											obj,
											info.getName(),
											info.getClients().toArray(),
											info.getContainer(),
											info.getContainerName(),
											info.getHandle(),
											mapAccessRights(info.getAccessRights()),
											interfaces);
			}
			else
				retVal = invalidInfo;

			if (isDebug())
				new MessageLogEntry(this, "get_collocated_component", "Exiting.", Level.FINEST).dispatch();

			return retVal;

		}
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "get_collocated_component");
			hbpe.putValue("target_component", target_component);
			// exception service will handle this
			reportException(hbpe);
	
			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}
		catch (IncompleteComponentSpecException icse)
		{
			IncompleteComponentSpecException hicse = new IncompleteComponentSpecException(this, icse.getMessage(), icse, icse.getIncompleteSpec());
			hicse.caughtIn(this, "get_collocated_component");
			// exception service will handle this
			reportException(hicse);

			throw new IncompleteComponentSpec(icse.getMessage(),
							new si.ijs.maci.ComponentSpec(
								//icse.getIncompleteSpec().getCURL().getPath(),
								icse.getIncompleteSpec().getName(),
								icse.getIncompleteSpec().getType(),
								icse.getIncompleteSpec().getCode(),
								icse.getIncompleteSpec().getContainer())
						);
		}
		catch (InvalidComponentSpecException incse)
		{
			InvalidComponentSpecException hincse = new InvalidComponentSpecException(this, incse.getMessage(), incse, incse.getInvalidSpec());
			hincse.caughtIn(this, "get_collocated_component");
			// exception service will handle this
			reportException(hincse);
			
			throw new InvalidComponentSpec(incse.getMessage());
		}
		catch (ComponentSpecIncompatibleWithActiveComponentException ciace)
		{
			ComponentSpecIncompatibleWithActiveComponentException hciace = new ComponentSpecIncompatibleWithActiveComponentException(this, ciace.getMessage(), ciace, ciace.getActiveComponentSpec());
			hciace.caughtIn(this, "get_collocated_component");
			// exception service will handle this
			reportException(hciace);

			// rethrow CORBA specific
			throw new ComponentSpecIncompatibleWithActiveComponent(ciace.getMessage(),
							new si.ijs.maci.ComponentSpec(
								//ciace.getActiveComponentSpec().getCURL().getPath(),
								ciace.getActiveComponentSpec().getName(),
								ciace.getActiveComponentSpec().getType(),
								ciace.getActiveComponentSpec().getCode(),
								ciace.getActiveComponentSpec().getContainer())
						);
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_collocated_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_collocated_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_collocated_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_collocated_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Get a service, activating it if necessary (components).
	 * The client represented by id (the handle) must have adequate access rights to access the service.
	 * NOTE: a component is also a service, i.e. a service activated by a container.
	 * 
	 * @param id Identification of the caller. If this is an invalid handle, or if the caller does not have enough access rights, a CORBA::NO_PERMISSION exception is raised.
	 * @param service_url CURL of the service whose reference is to be retrieved.
	 * @param activate True if the component is to be activated in case it does not exist. If set to False, and the Component does not exist, a nil reference is returned and status is set to COMPONENT_NOT_ACTIVATED.
	 * @param status Status of the request. One of COMPONENT_ACTIVATED, COMPONENT_NONEXISTANT and COMPONENT_NOT_ACTIVATED.
	 * @return Reference to the service. If the service could not be obtained, a nil reference is returned,
	 *		  and the status contains an error code detailing the cause of failure (one of the COMPONENT_* constants).
	 * @see #get_component
	 */
	public Object get_service(int id, String service_url, boolean activate, IntHolder status)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_service", new java.lang.Object[] { new Integer(id), service_url, new Boolean(activate), status } ).dispatch();

			// returned value
			Object retVal = null;

			// returned status
			StatusHolder statusHolder = new StatusHolder();

			// transform to CORBA specific
			URI uri = null;
			if (service_url != null)
				uri = CURLHelper.createURI(service_url);
			Component component = manager.getService(id, uri, activate, statusHolder);

			// map status
			status.value = mapStatus(statusHolder.getStatus());

			// extract Component CORBA reference
			if (component != null)
				retVal = (Object)component.getObject();

			if (isDebug())
				new MessageLogEntry(this, "get_service", "Exiting.", Level.FINEST).dispatch();

			return retVal;
		}
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "get_service");
			hbpe.putValue("service_url", service_url);
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_service");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_service");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_service");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_service");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Used for retrieving several services with one call.
	 *
	 * @param id Identification of the caller. If this is an invalid handle, or if the caller does not have enough access rights, a CORBA::NO_PERMISSION exception is raised.
	 * @param service_urls CURL of the services whose reference is to be retrieved.
	 * @param activate True if the Component is to be activated in case it does not exist. If set to False, and the Component does not exist, a nil reference is returned and status is set to COMPONENT_NOT_ACTIVATED.
	 * @param status Status of the request. One of COMPONENT_ACTIVATED, COMPONENT_NONEXISTANT and COMPONENT_NOT_ACTIVATED.
	 * @see #get_service
	 * @return A sequence of requested services.
	 */
	public Object[] get_services(int id, String[] service_urls, boolean activate, ulongSeqHolder status)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "get_services", new java.lang.Object[] { new Integer(id), service_urls, new Boolean(activate), status } ).dispatch();

			// returned value
			Object[] retVal = null;

			// returned status
			StatusSeqHolder statusHolder = new StatusSeqHolder();

			// map strings to CURLs
			URI[] uris = null;
			if (service_urls != null)
			{
				uris = new URI[service_urls.length];
				for (int i = 0; i < service_urls.length; i++)
				{
					try
					{
						if (service_urls[i] != null)
							uris[i] = CURLHelper.createURI(service_urls[i]);
					}
					catch (URISyntaxException usi)
					{
						BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
						hbpe.caughtIn(this, "get_components");
						hbpe.putValue("service_urls[i]", service_urls[i]);
						// exception service will handle this
					}
				}
			}

			// transform to CORBA specific
			Component[] services = manager.getServices(id, uris, activate, statusHolder);

			// map statuses
			status.value = new int[statusHolder.getStatus().length];
			for (int i = 0; i < statusHolder.getStatus().length; i++)
				status.value[i] = mapStatus(statusHolder.getStatus()[i]);

			// extract services CORBA references
			if (services != null)
			{
				retVal = new Object[services.length];

				for (int i = 0; i < services.length; i++)
					if (services[i] != null)
						retVal[i] = (Object)(services[i].getObject());
			}
			else
				retVal = new Object[0];

			if (isDebug())
				new MessageLogEntry(this, "get_services", "Exiting.", Level.FINEST).dispatch();

			return retVal;
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "get_services");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "get_services");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "get_services");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "get_services");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	/**
	 * Restarts an component.
	 * @param	id 	identification of the caller. Called has to be an owner of the component.
	 * @param	component_url	CURL of the component to be restarted.
	 * @return	CORBA reference of the restarted component, <code>null</code> if it fails.
	 */
	public Object restart_component(int id, String component_url)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "restart_component", new java.lang.Object[] { new Integer(id), component_url } ).dispatch();

			// returned value
			Object retVal = null;

			// transform to CORBA specific
			URI uri = null;
			if (component_url != null)
				uri = CURLHelper.createURI(component_url);
			Component component = manager.restartComponent(id, uri);

			// extract component CORBA reference
			if (component != null)
				retVal = (Object)component.getObject();

			if (isDebug())
				new MessageLogEntry(this, "restart_component", "Exiting.", Level.FINEST).dispatch();
				
			return retVal;
		}
		catch (URISyntaxException usi)
		{
			BadParametersException hbpe = new BadParametersException(this, usi.getMessage(), usi);
			hbpe.caughtIn(this, "restart_component");
			hbpe.putValue("component_url", component_url);
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(usi.getMessage());
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "restart_component");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "restart_component");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "restart_component");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "restart_component");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}
	}

	
	/**
	 * Shutdown a container.
	 * 
	 * @param	id 	identification of the caller. Called has to be an owner of the component.
	 * @param container_name	name of the container to shutdown.
	 * @param	action	The code to send to shutdown method of the container. If <code>0</code>, the Container's disconnect methods is called instead.
	 */
	public void shutdown_container(int id, String container_name, int action)
	{
		pendingRequests.increment();
		try
		{
			if (isDebug())
				new MessageLogEntry(this, "shutdown_container", new java.lang.Object[] { new Integer(id), container_name, new Integer(action) } ).dispatch();

			// simply shutdown the container
			manager.shutdownContainer(id, container_name, action);
		}
		catch (BadParametersException bpe)
		{
			BadParametersException hbpe = new BadParametersException(this, bpe.getMessage(), bpe);
			hbpe.caughtIn(this, "shutdown_container");
			// exception service will handle this
			reportException(hbpe);

			// rethrow CORBA specific
			throw new BAD_PARAM(bpe.getMessage());
		}
		catch (NoPermissionException npe)
		{
			NoPermissionException hnpe = new NoPermissionException(this, npe.getMessage(), npe);
			hnpe.caughtIn(this, "shutdown_container");
			// exception service will handle this
			reportException(hnpe);

			// rethrow CORBA specific
			throw new NO_PERMISSION(npe.getMessage());
		}
		catch (NoResourcesException nre)
		{
			NoResourcesException hnre = new NoResourcesException(this, nre.getMessage(), nre);
			hnre.caughtIn(this, "shutdown_container");
			// exception service will handle this
			reportException(hnre);

			// rethrow CORBA specific
			throw new NO_RESOURCES(nre.getMessage());
		}
		catch (Throwable ex)
		{
			CoreException hce = new CoreException(this, ex.getMessage(), ex);
			hce.caughtIn(this, "shutdown_container");
			// exception service will handle this
			reportException(hce);

			// rethrow CORBA specific
			throw new UNKNOWN(ex.getMessage());
		}
		finally
		{
			pendingRequests.decrement();
		}

	}
	
 	/*****************************************************************************/
	/************************** [ Mapping methods ] ******************************/
	/*****************************************************************************/

	/**
	 * Map <code>componentStatus</code> status codes to CORBA specific.
	 *
	 * @param	status	status of type <code>componentStatus</code>
	 * @return	CORBA specific Component status, <code>-1</code> if status is invalid.
	 */
	public static int mapStatus(ComponentStatus status)
	{
		if (status == ComponentStatus.COMPONENT_NONEXISTANT)
			return si.ijs.maci.Manager.COMPONENT_NONEXISTENT;
		else if (status == ComponentStatus.COMPONENT_NOT_ACTIVATED)
			return si.ijs.maci.Manager.COMPONENT_NOT_ACTIVATED;
		else if (status == ComponentStatus.COMPONENT_ACTIVATED)
			return si.ijs.maci.Manager.COMPONENT_ACTIVATED;
		else
			return -1;
	}

	/**
	 * Map <code>AccessRights</code> status codes to CORBA specific.
	 *
	 * @param	accessRights	access rights as defined in <code>AccessRights</code>
	 * @return	CORBA specific access rights code.
	 */
	public static int mapAccessRights(int accessRights)
	{
		int retVal = 0;
		
		if ((accessRights & AccessRights.INTROSPECT_MANAGER) == AccessRights.INTROSPECT_MANAGER)
			retVal |= si.ijs.maci.AccessRights.INTROSPECT_MANAGER.value;
			
		if ((accessRights & AccessRights.REGISTER_COMPONENT) == AccessRights.REGISTER_COMPONENT)
			retVal |= si.ijs.maci.AccessRights.REGISTER_COMPONENT.value;
			
		if ((accessRights & AccessRights.SHUTDOWN_SYSTEM) == AccessRights.SHUTDOWN_SYSTEM)
			retVal |= si.ijs.maci.AccessRights.SHUTDOWN_SYSTEM.value;

		return retVal;
	}

	/**
	 * Returns number of pending requests.
	 * @return	number of pending requests.
	 */
	public int getNumberOfPendingRequests()
	{
		return pendingRequests.get();
	}

 	/*****************************************************************************/
	/*************************** [ Abeans methods ] ******************************/
	/*****************************************************************************/

	/**
	 * @see abeans.core.Identifiable#getIdentifier()
	 */
	private void reportException(Exception ex)
	{
		ApplicationContext ctx = ((ManagerImpl)manager).getApplicationContext();
		// if manager is not in destruction mode, report exception
		if (ctx != null)
		{
			ReportEvent re = new ReportEvent(ctx, ex.getMessage());
			re.setException(ex);
			re.dispatch();
		}
	}

	/**
	 * @see abeans.core.Identifiable#getIdentifier()
	 */
	public Identifier getIdentifier()
	{
		if (id == null)
			id = new IdentifierSupport("Manager CORBA Proxy", "Manager", Identifier.PLUG);

		return id;
	}

	/**
	 * @see abeans.core.Identifiable#isDebug()
	 */
	public boolean isDebug()
	{
		return true;
	}

	/**
	 * Returns a single-line rendition of this instance into text.
	 *
	 * @return internal state of this instance
	 */
	public String toString()
	{
		StringBuffer sbuff = new StringBuffer();
		sbuff.append("ManagerProxyImpl = { ");
		sbuff.append("manager = '");
		sbuff.append(manager);
		sbuff.append("' }");
		return new String(sbuff);
	}

}
