/*
 * RemoveCategoryAction.java
 *
 * Created on October 28, 2004, 4:46 PM
 */

package cern.laser.guiplatform.actions.category;

import java.beans.IntrospectionException;

import org.apache.log4j.Logger;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import cern.gp.actions.support.NodeAction;
import cern.gp.nodes.GPNode;
import cern.laser.guiplatform.util.LogFactory;
import cern.laser.guiplatform.windows.configuration.ConsoleConfigurationWindow;
import cern.laser.guiplatform.windows.search.CategorySelectorWindow;

/**
 *
 * @author  woloszyn
 */
public class RemoveCategoryAction extends NodeAction {
    protected static final Logger logger = LogFactory.getLogger(RemoveCategoryAction.class.getName());    
    private static final String label = NbBundle.getMessage(RemoveCategoryAction.class,"LBL_RemoveCategoryAction_name");
    
    /** Creates a new instance of AddCategoryAction */
    public RemoveCategoryAction() {
    }
    
    protected void performAction(GPNode[] activatedNodes) {
        TopComponent.Registry registry = TopComponent.getRegistry();        
        
        if ( registry.getActivated() instanceof ConsoleConfigurationWindow ) {
            
            ConsoleConfigurationWindow confWindow =
            (ConsoleConfigurationWindow) registry.getActivated();
            
            logger.debug("confWindow=" + confWindow);
            
            if ( confWindow == null )
                throw new NullPointerException("Configuratrion window not found");
            
            
            for (int ix = 0; ix < activatedNodes.length; ix++) {
                try {
                    GPNode gpNode = activatedNodes[ix];
                    confWindow.removeCategoryWithoutChildren(gpNode);
                    confWindow.updateCategoryTreeExplorer();
                } catch (IntrospectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (CloneNotSupportedException e) {
                    // never happen
                    e.printStackTrace();
                }
            }
        }
        else {
            if ( registry.getActivated() instanceof CategorySelectorWindow ) {
                CategorySelectorWindow selectionWindow =
                (CategorySelectorWindow) registry.getActivated();
                
                logger.debug("selectionWindow=" + selectionWindow);
                
                if ( selectionWindow == null )
                    throw new NullPointerException("Configuratrion window not found");
                
                
                for (int ix = 0; ix < activatedNodes.length; ix++) {
                    try {
                        GPNode gpNode = activatedNodes[ix];
                        selectionWindow.removeCategoryWithoutChildren(gpNode);
                        selectionWindow.updateCategoryTreeExplorer();
                    } catch (IntrospectionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (CloneNotSupportedException e) {
                        // never happen
                        e.printStackTrace();
                    }
                }
                
            }
            else {
                throw new UnsupportedOperationException();
            }
        }        
    }
    /** the action will be enabled only if the super class enables it
     * and if the context is not null.
     * @see org.openide.util.actions.NodeAction#enable(Node[])
     *
     */
    protected boolean enable(Node[] nodes) {
        return super.enable(nodes);
    }
    
    
    public String getName() {
        return label;
    }
    
    protected String iconResource() {
        return "org/openide/resources/actions/delete.gif";
    }
    
}
