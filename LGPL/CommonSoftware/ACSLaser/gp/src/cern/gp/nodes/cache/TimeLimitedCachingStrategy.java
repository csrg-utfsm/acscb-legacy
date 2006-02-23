/*
 * $Id: TimeLimitedCachingStrategy.java,v 1.1 2005/06/07 03:26:13 kzagar Exp $
 *
 * $Date: 2005/06/07 03:26:13 $
 * $Revision: 1.1 $
 * $Author: kzagar $
 *
 * Copyright CERN, All Rights Reserved.
 */
package cern.gp.nodes.cache;

/**
 * A caching strategy based on a given time. The objects are cached for a given fixed time
 * that is given at construction time
 *
 * @author  Vito Baggiolini
 * @version $Revision: 1.1 $ $Date: 2005/06/07 03:26:13 $
 */
public class TimeLimitedCachingStrategy implements CachingStrategy {
   
  // variables related to timeout
  private final int timeoutMillis;  // relative time
  private long lastAccessTimeMillis; // absolute time
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  /**
   * Constructs a new caching strategy based on the given time
   * @param timeoutMillis the timeout for the cache
   */
  public TimeLimitedCachingStrategy(int timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }
  
  

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  //
  // -- implements CachingStrategy -----------------------------------------------
  //
  
  public boolean isCacheValid() {
    if (lastAccessTimeMillis == 0) {
      // first access
      lastAccessTimeMillis = System.currentTimeMillis();
      return false;
    } else {
      if (timeoutMillis <= 0) {
        return false;
      } else {
        return (System.currentTimeMillis() - lastAccessTimeMillis < timeoutMillis);
      }
    }
  }
  
  public void validateCache() {
    lastAccessTimeMillis = System.currentTimeMillis();
  }
  
  public void invalidateCache() {
    lastAccessTimeMillis = 0;
  }

}