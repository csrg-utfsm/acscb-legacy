/*
 * $Id: ViewCapability.java,v 1.2 2006/09/25 08:52:36 acaproni Exp $
 *
 * $Date: 2006/09/25 08:52:36 $
 * $Revision: 1.2 $
 * $Author: acaproni $
 *
 * Copyright CERN, All Rights Reserved.
 */
package cern.gp.capabilities;

import cern.gp.nodes.GPNode;

/**
 * Capability an object implements so that it can be viewed.
 * This capability is invoked by the corresponding Action.
 *
 * @see cern.gp.actions.ViewAction
 * @author Vito Baggiolini
 * @version $Revision: 1.2 $ $Date: 2006/09/25 08:52:36 $
 */
public interface ViewCapability extends Capability {
 
  /** 
   * Indicates to this object that it has to be viewed. The object 
   * has to interpret what "view" means in its own context, by
   * implementing this interface.
   * @param node the node representing the bean on which the capability has been activated
   */
  public void view(GPNode node);
  
}
