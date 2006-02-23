/*
 * InstantFaultExplorer.java
 *
 * Created on May 14, 2003, 3:41 PM
 */

package cern.laser.guiplatform.windows;

import org.apache.log4j.Logger;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import cern.laser.guiplatform.alarms.AlarmNodeManager;
import cern.laser.guiplatform.explorer.ACExplorer;
import cern.laser.guiplatform.util.LogFactory;

/**
 * This class stores and display information about instant faults
 *
 * @author  pawlowsk
 */
public class InstantFaultExplorer extends ACExplorer /*cern.gp.explorer.ListTableExplorer*/ {

    private final static Logger logger =
    LogFactory.getLogger(InstantFaultExplorer.class.getName());
    
    /** instant explorer name */
    private final static String thisExplorerName =
    NbBundle.getMessage(InstantFaultExplorer.class,
    "LBL_Instant_list_component_name");
    
    /** Creates new form InstantFaultExplorer */
    public InstantFaultExplorer(AlarmNodeManager nodeManager,
    String [] columnsToDisplay) {
        super(nodeManager, columnsToDisplay );
        
        initComponents();
        
        setName(thisExplorerName);
        // not serialize
        putClientProperty("PersistenceType", "Never");
        putClientProperty("TabPolicy", "HideWhenAlone");
        
        /*
        try {
            cern.gp.nodes.GPNode root = nodeManager.getRootNode();            
            this.setRootNode(root);
            this.setTableColumns(new AlarmBean(), columnToDisplay);
        } catch (java.beans.IntrospectionException ie) {
            logger.error(ie, ie.fillInStackTrace());
        }
        */
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

    }//GEN-END:initComponents
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /** This method returns name */
    public String getName() {
        return thisExplorerName;
    }
    //public java.awt.Image getIcon() {
    //}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
