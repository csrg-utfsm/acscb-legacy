package cern.gp.explorer;

import cern.gp.beans.BeanSupport;

/**
 * Class used to implement the {@link setName()
 * @author Vito Baggiolini
 * @version $Revision: 1.1 $, $Date: 2005/06/07 03:26:13 $, $Author: kzagar $
 */
class NamedBean extends BeanSupport {
	private String displayName = null;
	private String name = null;
	
	public NamedBean(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the name.
	 */
	public String getDisplayName() {
		if (displayName == null) {
			return getName();
		} else {
			return displayName;
		}
	}

	/**
	 * @param name The name to set.
	 */
	public void setDisplayName(String newDisplayName) {
		this.displayName = newDisplayName;
		super.fireDisplayNamePropertyChange(newDisplayName);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String newName) {
		this.name = newName;
		super.fireNamePropertyChange(newName);
	}

}
