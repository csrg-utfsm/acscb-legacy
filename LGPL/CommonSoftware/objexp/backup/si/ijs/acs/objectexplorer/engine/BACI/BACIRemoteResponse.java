package si.ijs.acs.objectexplorer.engine.BACI;

import si.ijs.acs.objectexplorer.engine.*;
/**
 * Insert the type's description here.
 * Creation date: (13.11.2000 20:35:50)
 * @author: 
 */
public class BACIRemoteResponse implements RemoteResponse {
	private Object[] data = null;
	private String[] names = null;
	private String opName = null;
	private int SN = 0;
	private static int serial = 0;
	private BACIInvocation invoc = null;
	
	// data for the dispather
	RemoteResponseCallback cb = null;
	boolean destroy = false;
/**
 * BACIRemoteResponse constructor comment.
 */
public BACIRemoteResponse(BACIInvocation invoc, String opName, String[] names, Object[] data) {
	super();
	if (opName == null) throw new NullPointerException("opName");
	if (names == null) throw new NullPointerException("names");
	if (data == null) throw new NullPointerException("data");
	if (invoc == null) throw new NullPointerException("invoc");

	this.invoc = invoc;
	this.opName = opName;
	this.names = names;
	this.data = data;
	SN = invoc.responseCount;
	invoc.responseCount++;
}
/**
 * Insert the method's description here.
 * Creation date: (13.11.2000 20:35:50)
 * @return java.lang.Object[]
 */
public java.lang.Object[] getData() {
	return data;
}
/**
 * Insert the method's description here.
 * Creation date: (13.11.2000 20:35:50)
 * @return java.lang.String[]
 */
public java.lang.String[] getDataNames() {
	return names;
}
/**
 * Insert the method's description here.
 * Creation date: (1.12.2000 13:44:56)
 * @return si.ijs.acs.objectexplorer.engine.Invocation
 */
public Invocation getInvocation() {
	return invoc;
}
/**
 * Insert the method's description here.
 * Creation date: (13.11.2000 20:35:50)
 * @return java.lang.String
 */
public String getName() {
	return opName;
}
/**
 * Insert the method's description here.
 * Creation date: (13.11.2000 20:35:50)
 * @return int
 */
public int getSequenceNumber() {
	return SN;
}
}
