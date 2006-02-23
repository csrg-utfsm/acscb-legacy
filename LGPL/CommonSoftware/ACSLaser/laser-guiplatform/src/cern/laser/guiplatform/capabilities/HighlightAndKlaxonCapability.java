/*
 * InhibitCapability.java
 *
 * Created on May 23, 2003, 11:19 AM
 */

package cern.laser.guiplatform.capabilities;

import cern.gp.capabilities.Capability;

/**
 * Capability an object implements so that it can be highlighted with klaxon. 
 * This capability is invoked by the corresponding Action 
 *
 * @author  pawlowsk
 */
public interface HighlightAndKlaxonCapability extends Capability {
    
    /** Indicates to this object that it has to be highlighted */
    void highlightAndKlaxon(/*GPNode node*/);
    
}
