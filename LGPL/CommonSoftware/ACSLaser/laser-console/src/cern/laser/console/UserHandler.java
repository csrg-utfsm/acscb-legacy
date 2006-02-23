/*
 * $Id: UserHandler.java,v 1.1 2005/06/07 03:17:25 kzagar Exp $
 *
 * $Date: 2005/06/07 03:17:25 $ 
 * $Revision: 1.1 $ 
 * $Author: kzagar $
 *
 * Copyright CERN, All Rights Reserved.
 */
package cern.laser.console;
import java.util.Collection;

import cern.laser.console.impl.UserHandlerImpl;

/** Provides the services to handle alarm console users.
 * @author F.Calderini
 * @see cern.laser.console.User
 */
public abstract class UserHandler 
{
  private static UserHandler instance = null;
    /** Factory method.
     * @return an instance of the implementation class
     * @throws LaserConsoleException if the request can not be served
     */    
  public static UserHandler get() throws LaserConsoleException 
  {
    if (instance == null) {
      instance = new UserHandlerImpl();
    }

    return instance;
  }
  
  /** Get all the defined alarm console users.
   * @return defined alarm console users.
   * @throws LaserConsoleException if the request can not be served.
   */    
  public abstract Collection getUsers() throws LaserConsoleException;
  /** Get the user by its name.
   * @param name the user name.
   * @return the alarm console user .
   * @throws LaserConsoleException if the request can not be served.
   * @throws LaserUserNotFoundException if the user does not exist.
   */    
  public abstract User getUser(String name) throws LaserConsoleException;
  /** Factory method. Define a new alarm console user.
   * @param name the user name.
   * @param password the user password.
   * @return a new alarm console user instance.
   * @throws LaserConsoleException if the request can not be served.
   * @throws LaserUserDuplicationException if the user already exists.
   */    
  public abstract User createUser(String name, String password) throws LaserConsoleException;
  /** Remove an alarm console user.
   * @param name the name of the user to remove.
   * @throws LaserConsoleException if the request can not be served.
   * @throws LaserUserNotFoundException if the user does not exist.
   */    
  public abstract void removeUser(String name) throws LaserConsoleException;
}