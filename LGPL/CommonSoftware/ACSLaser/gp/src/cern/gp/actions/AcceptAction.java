/*
 * $Id: AcceptAction.java,v 1.1 2005/06/07 03:26:13 kzagar Exp $
 *
 * $Date: 2005/06/07 03:26:13 $
 * $Revision: 1.1 $
 * $Author: kzagar $
 *
 * Copyright CERN, All Rights Reserved.
 */

package cern.gp.actions;

import cern.gp.actions.support.*;
import cern.gp.capabilities.AcceptCapability;
import cern.gp.capabilities.Capability;
import cern.gp.nodes.GPNode;

/**
 * The action for "accepting" something. An Object that wants to support
 * this action (i.e. it wants to be "accepted") has to implement the AcceptCapability
 * and has to declare in its BeanInfo that it supports the AcceptAction.
 *
 * @see cern.gp.capabilities.AcceptCapability
 * @see cern.gp.beans.BeanInfoSupport
 * @author  Vito Baggiolini
 * @version $Revision: 1.1 $ $Date: 2005/06/07 03:26:13 $
 */
public class AcceptAction extends BeanAction {
  
  /** 
   * Creates a new instance 
   * do not call this directly, use SystemActions.get() instead
   */
  public AcceptAction() {
    super(AcceptCapability.class);
  }
  
  public String getName() {
    return "&Accept"; // & precedes the character to be used as shortcut
  }
  
  /** Performs the capability for the given node.
   * @param node the node for which the capability is performed
   * @param capability the capability to perform
   *
   */
  protected void performCapability(GPNode node, Capability capability) {
    AcceptCapability acceptCapability = (AcceptCapability) capability;
    acceptCapability.accept(node);
  }
    
}
