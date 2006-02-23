package alma.demo.dyncomp;

import java.util.logging.Logger;
import alma.acs.component.client.ComponentClient;
import alma.ACS.ACSComponent;
import alma.ACS.ACSComponentHelper;
import si.ijs.maci.ComponentSpec;
import si.ijs.maci.ComponentInfo;
import alma.acs.container.ContainerException;

/** 
* Client demostrates how to activate and release dynamic components
*
* @author Alessandro Caproni, 2003, Nov 7
*/
public class Client extends ComponentClient {

	private final int MAXCOMPONENTS=32;
	// The two lists with the acivated components and their URLs
	// When a component is freed the items are not reordered (so a hole will exist in the
	// position of the freed element)
	// Free slots contain null
	ACSComponent m_components[];
	String m_cURLs[];

	/** The constructor
	*/
	public Client(Logger logger, String managerLoc, String clientName) 
		throws Exception
	{
		super(logger,managerLoc,clientName);
		m_components =  new ACSComponent[MAXCOMPONENTS];
		m_cURLs= new String[MAXCOMPONENTS];
		for (int t=0; t<MAXCOMPONENTS; t++) {
			m_components[t]=null;
			m_cURLs[t]=null;
		}
	}

	/** Check if a free slot exists into the array
	* A free slot has a null value and, as the array is not ordered, may be
	* in every position of the array
	*/
	public boolean hasFreeSlot() {
		boolean ret=false;
		for (int t=0; t<MAXCOMPONENTS; t++) 
			if (m_components[t]==null) ret=true;
		return ret;
	}

	/** Start a dynamic component
	* @param cs The component specification record
	* @param markAsDefault marck the specified dynamic component ad default
	* @return The cUrl of the activated component (nul if the component is not activated)
	*/
	public String getDynamicComponent(ComponentSpec cs, boolean markAsDefault) 
		throws ContainerException
	{
		// Search for the first free slot in the array
		int freeSlot=0;
		while (freeSlot<MAXCOMPONENTS && m_components[freeSlot]!=null) freeSlot++;
		if (freeSlot<MAXCOMPONENTS) {
			m_components[freeSlot]=ACSComponentHelper.narrow(getContainerServices().getDynamicComponent(cs,markAsDefault));
			if (m_components[freeSlot]!=null) {
				m_cURLs[freeSlot]=m_components[freeSlot].name();
				return  m_cURLs[freeSlot];
			} else {
				m_cURLs[freeSlot]=null;
				return null;
			}
		}
		return null;
	}

	/** Release a component
	* The component is specified with its cURL 
	* @param url The name of the component
	* @return false in case of error releasing the component or if the component with the specified url is not found
	*/
	public boolean releaseComponent(String url) {
		// Search if the given url exists
		int t;
		for (t=0; t<MAXCOMPONENTS; t++) {
			if (m_cURLs[t]!=null)
				if (url.compareTo(m_cURLs[t])==0) break;
		}
		if (t<MAXCOMPONENTS && url.compareTo(m_cURLs[t])==0) {
			getContainerServices().releaseComponent(url);
			m_components[t]=null;
			m_cURLs[t]=null;
			return true;
		} else return false;
	}

	/** Release all the components  before exiting
	*/
	public void cleanExit() {
		String cURLs[];
		for (int t=0; t<MAXCOMPONENTS; t++) {
			if (m_cURLs[t]!=null && m_components[t]!=null) 
				getContainerServices().releaseComponent(m_cURLs[t]);
				m_components[t]=null;
				m_cURLs[t]=null;
		}
	}
}

