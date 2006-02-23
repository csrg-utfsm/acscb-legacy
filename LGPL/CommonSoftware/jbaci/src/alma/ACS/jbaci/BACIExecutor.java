/*******************************************************************************
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * Copyright by ESO (in the framework of the ALMA collaboration)
 * and Cosylab 2002, All rights reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.ACS.jbaci;

import EDU.oswego.cs.dl.util.concurrent.BoundedPriorityQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * BACI action executor (thread pool)
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $id$
 */
// TODO make it a daemon
public class BACIExecutor {

	/**
	 * Singleton instance.
	 */
	private static BACIExecutor instance = null;

	/**
	 * Thread pool.
	 */
	private PooledExecutor threadPool;

	/**
	 * Number of threads in thread pool.
	 */
	private static final int MAX_REQUESTS = 100;

	/**
	 * Number of requests ( in thread pool (guarantees order of execution).
	 */
	private static final int POOL_THREADS = 10;

	/**
	 * Protected constructor (singleton pattern). 
	 */
	protected BACIExecutor()
	{
		// TODO should I use PooledExecutorWithWaitInNewThreadWhenBlocked...
		threadPool = new PooledExecutor(
			new BoundedPriorityQueue(MAX_REQUESTS, new PrioritizedRunnableComparator()));

		// create threads
		threadPool.setMinimumPoolSize(POOL_THREADS);
		threadPool.createThreads(POOL_THREADS);
	}
	
	/**
	 * Singleton pattern.
	 */
	public static synchronized BACIExecutor getInstance()
	{
		if (instance == null)
			instance = new BACIExecutor();
		return instance;
	}

	/**
	 * Execute action. 
	 * If the maximum pool size or queue size is bounded,
	 * then it is possible for incoming execute requests to block. 
	 * <code>BACIExecutor</code> uses default 'Run' blocking policy: 
	 * The thread making the execute request runs the task itself. This policy helps guard against lockup. 
	 * @param action	action to execute.
	 * @return <code>true</code> on success.
	 */
	public boolean execute(PrioritizedRunnable action)
	{
		try
		{
			threadPool.execute(action);
			return true;	
		}
		catch (InterruptedException ie)
		{
			return false;
		}
	}

}
