package cern.laser.guiplatform.actions;

import org.apache.log4j.Logger;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import cern.gp.actions.support.CallableSystemAction;
import cern.laser.guiplatform.util.LogFactory;

/** Action that can always be invoked and work procedurally.
 *
 * @author bartek
 */
public class ArchiveSearchAction extends CallableSystemAction {
    
    /** logger */
    private Logger logger = LogFactory.getLogger(ArchiveSearchAction.class.getName());
    
    public void performAction() {
        // do what you want
    }
    
    public String getName() {
        return NbBundle.getMessage(ArchiveSearchAction.class, "LBL_ArchiveSearchAction");
    }
    
    protected String iconResource() {
        return "org/openide/resources/actions/find.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(ArchiveSearchAction.class);
    }
    
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ArchiveSearchAction.class, "HINT_Action"));
     * }
     */

    public boolean isEnabled() {
        return false;        
    }
    
}
