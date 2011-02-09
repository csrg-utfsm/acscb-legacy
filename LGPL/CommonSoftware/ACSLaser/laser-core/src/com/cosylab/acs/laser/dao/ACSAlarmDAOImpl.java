/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *                 and Cosylab
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307  USA
 */
package com.cosylab.acs.laser.dao;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import alma.acs.alarmsystem.generated.Contact;
import alma.acs.alarmsystem.generated.FaultCode;
import alma.acs.alarmsystem.generated.FaultFamily;
import alma.acs.alarmsystem.generated.FaultMember;
import alma.acs.alarmsystem.generated.FaultMemberDefault;
import alma.acs.logging.AcsLogLevel;
import alma.alarmsystem.alarmmessage.generated.Child;
import alma.alarmsystem.alarmmessage.generated.Parent;
import alma.alarmsystem.alarmmessage.generated.ReductionDefinitions;
import alma.alarmsystem.alarmmessage.generated.ReductionLinkDefinitionListType;
import alma.alarmsystem.alarmmessage.generated.ReductionLinkType;
import alma.alarmsystem.alarmmessage.generated.Threshold;
import alma.alarmsystem.alarmmessage.generated.Thresholds;
import alma.alarmsystem.core.alarms.LaserCoreAlarms;
import alma.cdbErrType.CDBRecordDoesNotExistEx;
import cern.laser.business.LaserObjectNotFoundException;
import cern.laser.business.dao.AlarmDAO;
import cern.laser.business.dao.ResponsiblePersonDAO;
import cern.laser.business.data.Alarm;
import cern.laser.business.data.AlarmImpl;
import cern.laser.business.data.Building;
import cern.laser.business.data.Category;
import cern.laser.business.data.Location;
import cern.laser.business.data.ResponsiblePerson;
import cern.laser.business.data.Source;
import cern.laser.business.data.Status;
import cern.laser.business.data.Triplet;
import cern.laser.business.definition.data.SourceDefinition;

import com.cosylab.acs.laser.dao.utils.AlarmRefMatcher;
import com.cosylab.acs.laser.dao.utils.LinkSpec;

class HardcodedBuilding extends Building
{
	private static final long serialVersionUID = -2341173312854231369L;

	private HardcodedBuilding()
	{
		super("B/1", "Site", new Integer(1), "Map");
	}
	
	static final HardcodedBuilding instance = new HardcodedBuilding(); 
}

class HardcodedLocation extends Location
{
	private static final long serialVersionUID = -4988092784308952674L;

	private HardcodedLocation()
	{
		super("L/1", "1", "L/1/1", "1", "1");
		this.setBuilding(HardcodedBuilding.instance);
	}
	
	static final HardcodedLocation instance = new HardcodedLocation();
}


/**
 * Read alarms from the CDB.
 * 
 * CDB contains one file per each possible FF and one entry per each FC and FM.
 * It is possible to specify a default member to be used when the administrator
 * did not specify the member.
 * 
 * The alarms are stored in an HashMap having the triplet as key.
 * The default member has a triplet with a "*" replacing its name.
 * 
 *  The sources are defined together with an alarm definition so they are read here
 *  and requested by the ACSSourceDAOImpl at startup (instead of being read again from CDB).
 *  
 *  The initialization of the alarms is not completely done by loadAlarms because not all the info
 *  are available at this time. In fact the categories are assigned to alarms by ACSCategoryDAOImpl
 *  
 *  @see ACSSourceDAOImpl
 *  @see ACSCategoryDAOImpl
 * 
 * @author acaproni
 *
 */
public class ACSAlarmDAOImpl implements AlarmDAO
{
	static final HardcodedBuilding theBuilding=HardcodedBuilding.instance;
	static final HardcodedLocation theLocation=HardcodedLocation.instance;
	
	// The path where alarm definitions are
	private static final String ALARM_DEFINITION_PATH = "/Alarms/AlarmDefinitions";
	
	// The path in the CDB where reduction rules are defined
	private static final String REDUCTION_DEFINITION_PATH = "/Alarms/Administrative/ReductionDefinitions";
	
	// The type of the alarm definition in the XML
	private static final String XML_DOCUMENT_TYPE="AlarmDefinitions";
	
	// The FM for the default member
	private static final String DEFAULT_FM = "*";
	
	// The ACS logger
	Logger logger;
	
	ConfigurationAccessor conf;
	String surveillanceAlarmId;
	ResponsiblePersonDAO responsiblePersonDAO;
	
	/** The alarms read out the CDB
	 *  The CDB contains fault families.
	 *  The alarms are generated from the fault families.
	 */
	private ConcurrentHashMap<String,Alarm> alarmDefs=new ConcurrentHashMap<String,Alarm>();
	
	/**
	 * Source are defined together with alarms
	 * This HashMap contains all the sources read from CDB
	 */
	private ConcurrentHashMap<String, Source> srcDefs = new ConcurrentHashMap<String, Source>();
	
	/**
	 * Constructor 
	 * 
	 * @param log The log (not null)
	 * @param 
	 */
	public ACSAlarmDAOImpl(Logger log) {
		if (log==null) {
			throw new IllegalArgumentException("Invalid null logger");
		}
		logger =log;
		generateLaserCoreAlarms();
	}
	
	/**
	 * Load alarms from CDB.
	 * 
	 * Read the alarms by the alarm definitions file of the CDB.
	 * The initialization of the alarms is not complete at this stage.
	 * In fact the categories are assigned to alarms when reading the
	 * categories from the CDB by ACSCategoryDAOImpl
	 * 
	 *  
	 * @throws Exception In case of error while parsing XML definition files
	 * 
	 * @see ACSCategoryDAOImpl
	 */
	public List<FaultFamily> loadAlarms() throws Exception{
		if (conf==null) {
			throw new IllegalStateException("Missing dal");
		}
		String xml;
		try {
			xml=conf.getConfiguration(ALARM_DEFINITION_PATH);
		} catch (Throwable t) {
			throw new RuntimeException("Couldn't read alarm XML", t);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder= factory.newDocumentBuilder();
		} catch (Exception e) {
			throw new Exception("Error building the DocumentBuilder from the DocumentBuilderFactory",e);
		}
		StringReader stringReader = new StringReader(xml);
		InputSource inputSource = new InputSource(stringReader);
		Document doc;
		try {
			doc= builder.parse(inputSource);
			if (doc==null) {
				throw new Exception("The builder returned a null Document after parsing");
			}
		} catch (Exception e) {
			throw new Exception("Error parsing XML: "+xml,e);
		}
		NodeList docChilds = doc.getChildNodes();
		if (docChilds==null || docChilds.getLength()!=1) {
			throw new Exception("Malformed xml: only one node (AlarmDefinitions) expected");
		}
		Node alarmDefNode = docChilds.item(0);
		if (alarmDefNode==null || alarmDefNode.getNodeName()!=XML_DOCUMENT_TYPE) {
			throw new Exception("Malformed xml: "+XML_DOCUMENT_TYPE+" not found");
		}
		NodeList alarmDefsNodes = alarmDefNode.getChildNodes();
		Unmarshaller FF_unmarshaller = new Unmarshaller(FaultFamily.class);
		FF_unmarshaller.setValidation(false);
		FF_unmarshaller.setWhitespacePreserve(true);
		Vector<FaultFamily> cdbFamilies= new Vector<FaultFamily>();
		for (int t=0; t<alarmDefsNodes.getLength(); t++) {
			Node alarmDef = alarmDefsNodes.item(t);
			try {
				FaultFamily family = (FaultFamily) FF_unmarshaller.unmarshal(alarmDef);
				if (family==null) {
					throw new Exception("Error unmarshalling a family node");
				}
				cdbFamilies.add(family);
			} catch (Exception e) {
				throw new Exception("Error parsing "+alarmDef.getNodeName(),e);
			}
		}
		generateAlarmsMap(cdbFamilies);
		
		loadReductionRules();

                return cdbFamilies;
	}
	
	/**
	 * Add the alarms generated by the alarm component
	 */
	private void generateLaserCoreAlarms() {
		Vector<AlarmImpl> coreAlarms=LaserCoreAlarms.generateAlarms();
		for (AlarmImpl alarm: coreAlarms) {
			addAlarmToCache(alarm);
			logger.log(AcsLogLevel.DEBUG,"Alarm added "+alarm.getAlarmId());
		}
	}
	
	/**
	 * Generate the alarms from the definition of the fault families.
	 * The alarms will be added into the HashMap with their triplet as key.
	 * The default item has FM="*".
	 * 
	 * The sources read from the families are also added to the HashMap of the sources
	 *  
	 * @param families The FF read from the CDB
	 */
	private void generateAlarmsMap(Vector<FaultFamily> families) {
		if (families==null) {
			throw new IllegalArgumentException("Invalid null vector of FFs");
		}
		for (FaultFamily family: families) {
			String FF= family.getName();
			String helpUrl = family.getHelpUrl();
			String source = family.getAlarmSource();
			Contact contactPerson = family.getContact();
			FaultMember[] FMs = family.getFaultMember();
			FaultMemberDefault defaultFM = family.getFaultMemberDefault();
			FaultCode[] FCs = family.getFaultCode();
			// Check the FCs
			//
			// There should be at least one FC in the CDB
			if (FCs==null || FCs.length==0) {
				logger.log(AcsLogLevel.WARNING,"No FC defined for family "+family.getName());
				continue;
			}
			// Check the FM
			//
			// There should be at least one FM or a default FM defined in the CDB
			if (defaultFM==null && (FMs==null || FMs.length==0)) {
				logger.log(AcsLogLevel.WARNING,"No FM defined for family "+family.getName());
				continue;
			}
			
			// Iterate over the FCs
			for (FaultCode code: FCs) {
				int FC=code.getValue();
				int priority = code.getPriority();
				String action = code.getAction();
				String cause = code.getCause();
				String consequence = code.getConsequence();
				String problemDesc = code.getProblemDescription();
				boolean instant = code.getInstant();
				// Iterate over all the FMs
				for (FaultMember member: FMs) {
					alma.acs.alarmsystem.generated.Location loc = member.getLocation();
					if (loc==null) {
						loc = new alma.acs.alarmsystem.generated.Location();
					}
					if (loc.getBuilding()==null) {
						loc.setBuilding("");
					}
					if (loc.getFloor()==null) {
						loc.setFloor("");
					}
					if (loc.getMnemonic()==null) {
						loc.setMnemonic("");
					}
					if (loc.getPosition()==null) {
						loc.setPosition("");
					}
					if (loc.getRoom()==null) {
						loc.setRoom("");
					}
				
					String FM = member.getName();
					if (FM.equals(DEFAULT_FM)) {
						logger.log(AcsLogLevel.ERROR,"In the CDB, FM="+DEFAULT_FM+" in family "+FF+" is not allowed");
					}
					AlarmImpl alarm = new AlarmImpl(); 
					
					alarm.setMultiplicityChildrenIds(new HashSet());
					alarm.setMultiplicityParentIds(new HashSet());
					alarm.setNodeChildrenIds(new HashSet());
					alarm.setNodeParentIds(new HashSet());
					
					alarm.setAction(action);
					alarm.setTriplet(new Triplet(FF, FM, FC));
					alarm.setCategories(new HashSet<Category>());
					alarm.setCause(cause);
					alarm.setConsequence(consequence);
					alarm.setProblemDescription(problemDesc);
					try {
						alarm.setHelpURL(new URL(helpUrl));
					} catch (MalformedURLException e) {
						alarm.setHelpURL(null);
					}
					
					alarm.setInstant(instant);
					Location location = new Location("0",loc.getFloor(),loc.getMnemonic(),loc.getPosition(),loc.getRoom());
					alarm.setLocation(location);
					if (contactPerson.getEmail()!=null) {
						alarm.setPiquetEmail(contactPerson.getEmail());
					} else {
						alarm.setPiquetEmail("");
					}
					if (contactPerson.getGsm()!=null) {
						alarm.setPiquetGSM(contactPerson.getGsm());
					} else {
						alarm.setPiquetGSM("");
					}
					alarm.setPriority(priority);
					ResponsiblePerson responsible = new ResponsiblePerson(0,contactPerson.getName(),"",contactPerson.getEmail(),contactPerson.getGsm(),"");
					alarm.setResponsiblePerson(responsible);
					SourceDefinition srcDef = new SourceDefinition(source,"SOURCE","",15,1);
					Source src = new Source(srcDef,responsible);
					alarm.setSource(src);
					alarm.setIdentifier(alarm.getTriplet().toIdentifier());
					
					addAlarmToCache(alarm);
					if (!srcDefs.containsKey(source)) {
						srcDefs.put(src.getSourceId(),src);
						logger.log(AcsLogLevel.DEBUG,"Source "+src.getName()+" (id="+src.getSourceId()+") added");
					}
					
					logger.log(AcsLogLevel.DEBUG,"Alarm added "+alarm.getAlarmId());
				}
				// Add the default
				if (defaultFM!=null) {
					alma.acs.alarmsystem.generated.Location loc = defaultFM.getLocation();
					if (loc==null) {
						loc = new alma.acs.alarmsystem.generated.Location();
					}
					if (loc.getBuilding()==null) {
						loc.setBuilding("");
					}
					if (loc.getFloor()==null) {
						loc.setFloor("");
					}
					if (loc.getMnemonic()==null) {
						loc.setMnemonic("");
					}
					if (loc.getPosition()==null) {
						loc.setPosition("");
					}
					if (loc.getRoom()==null) {
						loc.setRoom("");
					}
					AlarmImpl defaultAlarm = new AlarmImpl(); 
					
					defaultAlarm.setMultiplicityChildrenIds(new HashSet());
					defaultAlarm.setMultiplicityParentIds(new HashSet());
					defaultAlarm.setNodeChildrenIds(new HashSet());
					defaultAlarm.setNodeParentIds(new HashSet());
					
					defaultAlarm.setAction(action);
					defaultAlarm.setCategories(new HashSet<Category>());
					defaultAlarm.setCause(cause);
					defaultAlarm.setConsequence(consequence);
					defaultAlarm.setProblemDescription(problemDesc);
					try {
						defaultAlarm.setHelpURL(new URL(helpUrl));
					} catch (MalformedURLException e) {
						defaultAlarm.setHelpURL(null);
					}
					
					defaultAlarm.setInstant(instant);
					Location location = new Location("0",loc.getFloor(),loc.getMnemonic(),loc.getPosition(),loc.getRoom());
					defaultAlarm.setLocation(location);
					defaultAlarm.setPiquetEmail(contactPerson.getEmail());
					defaultAlarm.setPiquetGSM(contactPerson.getGsm());
					defaultAlarm.setPriority(priority);
					ResponsiblePerson responsible = new ResponsiblePerson(0,contactPerson.getName(),"",contactPerson.getEmail(),contactPerson.getGsm(),"");
					defaultAlarm.setResponsiblePerson(responsible);
					SourceDefinition srcDef = new SourceDefinition(source,"SOURCE","",15,1);
					Source src = new Source(srcDef,responsible);
					defaultAlarm.setSource(src);
					defaultAlarm.setIdentifier(defaultAlarm.getTriplet().toIdentifier());
					Triplet triplet = new Triplet(FF,DEFAULT_FM,FC);
					defaultAlarm.setTriplet(triplet);
					defaultAlarm.setIdentifier(triplet.toIdentifier());
					addAlarmToCache(defaultAlarm);
					if (!srcDefs.containsKey(source)) {
						srcDefs.put(src.getSourceId(),src);
						logger.log(AcsLogLevel.DEBUG,"Source "+src.getName()+" (id="+src.getSourceId()+") added");
					}
					logger.log(AcsLogLevel.DEBUG,"Default alarm added "+defaultAlarm.getAlarmId());
				}
			}
		}
	}
	
	public ReductionDefinitions getReductionDefinitions()
	{
		if (conf==null) {
			throw new IllegalStateException("Missing dal");
		}
		
		String xml;
		try {
			xml=conf.getConfiguration(REDUCTION_DEFINITION_PATH);
		} catch (CDBRecordDoesNotExistEx cdbEx) {
			// No reduction rules defined in CDB
			logger.log(AcsLogLevel.WARNING,"No reduction rules defined in CDB");
			return null;
			
		} catch (Exception e) {
			throw new RuntimeException("Couldn't read "+REDUCTION_DEFINITION_PATH, e);
		}
		ReductionDefinitions rds = null;
		try {
			rds=(ReductionDefinitions) ReductionDefinitions.unmarshalReductionDefinitions(new StringReader(xml));
		} catch (Exception e) {
			throw new RuntimeException("Couldn't parse "+REDUCTION_DEFINITION_PATH, e);
		}
		
		return rds;
	}
	
	/**
	 * Load the reduction rules from the CDB.
	 * 
	 * Read the reduction rules from the CDB and set up the alarms accordingly
	 */
	private void loadReductionRules()
	{		
		ReductionDefinitions rds = getReductionDefinitions();
		if (rds == null)
			return;
		
		// The reduction rules (<parent, child>)
		ArrayList<LinkSpec> reductionRules=new ArrayList<LinkSpec>();

		// Read the links to create from the CDB
		ReductionLinkDefinitionListType ltc=rds.getLinksToCreate();
		//	Read the thresholds from the CDB
		Thresholds thresholds = rds.getThresholds();
		if (ltc!=null) { 
			ReductionLinkType[] rls=ltc.getReductionLink();
			for (ReductionLinkType link: rls) {

				LinkSpec newRule =new LinkSpec(link);
				reductionRules.add(newRule);

				if( logger.isLoggable(AcsLogLevel.DEBUG) ) {
					
					Parent p=link.getParent();
					Child c=link.getChild();

					StringBuffer buf = new StringBuffer();
					buf.replace(0, buf.length(), "");
					if (newRule._isMultiplicity) {
						buf.append("Found MULTIPLICITY RR: parent <");
					} else {
						buf.append("Found NODE RR: parent=<");
					}
					buf.append(p.getAlarmDefinition().getFaultFamily());
					buf.append(", ");
					buf.append(p.getAlarmDefinition().getFaultMember());
					buf.append(", ");
					buf.append(p.getAlarmDefinition().getFaultCode());
					buf.append("> child=<");
					buf.append(c.getAlarmDefinition().getFaultFamily());
					buf.append(", ");
					buf.append(c.getAlarmDefinition().getFaultMember());
					buf.append(", ");
					buf.append(c.getAlarmDefinition().getFaultCode());
					buf.append('>');
					logger.log(AcsLogLevel.DEBUG,buf.toString());
				}
			}
		}
		logger.log(AcsLogLevel.DEBUG,reductionRules.size()+" reduction rules read from CDB");
		
		Collection<Alarm> cc=alarmDefs.values();
		AlarmImpl[] allAlarms=new AlarmImpl[cc.size()];
		cc.toArray(allAlarms);
		
		LinkSpec[] ls=new LinkSpec[reductionRules.size()];
		reductionRules.toArray(ls);
		
		int num=allAlarms.length;
		
		for (int a=0; a<num; a++) {
			AlarmImpl parent=allAlarms[a];
			for (LinkSpec lsb: ls) {
				if (lsb.matchParent(parent)) {
					AlarmRefMatcher childMatcher=lsb._child;
					boolean isMulti=lsb.isMultiplicity();
					for (int c=0; c<num; c++) {
						if (a==c) {
							continue;
						}
						AlarmImpl aic=allAlarms[c];
						if (childMatcher.isMatch(aic)) {
							if (isMulti) {
								parent.addMultiplicityChild(aic);
								logger.log(AcsLogLevel.DEBUG,"Added MULTI RR node child "+aic.getAlarmId()+" to "+parent.getAlarmId());
							} else {
								parent.addNodeChild(aic);
								logger.log(AcsLogLevel.DEBUG,"Added NODE RR node child "+aic.getAlarmId()+" to "+parent.getAlarmId());
							}
						} 
					}
				}  
			}
		}
		
		if (thresholds!=null && thresholds.getThresholdCount()>0) {
			Threshold[] ta = thresholds.getThreshold();
			for (AlarmImpl alarm: allAlarms) {
				String alarmFF = alarm.getTriplet().getFaultFamily();
				String alarmFM = alarm.getTriplet().getFaultMember();
				Integer alarmFC= alarm.getTriplet().getFaultCode();
				for (Threshold threshold: ta) {
					String thresholdFF=threshold.getAlarmDefinition().getFaultFamily();
					String thresholdFM=threshold.getAlarmDefinition().getFaultMember();
					int thresholdFC=threshold.getAlarmDefinition().getFaultCode();
					int thresholdVal = threshold.getValue();
					if (alarmFF.equals(thresholdFF) && alarmFM.equals(thresholdFM) && alarmFC==thresholdFC) {
						alarm.setMultiplicityThreshold(thresholdVal);
						logger.log(AcsLogLevel.DEBUG,"Threshold = "+thresholdVal+" set in alarm "+alarm.getAlarmId());
					}
				}
			}
		}
	}

	private void saveAllIDs()
	{
		if (conf==null) {
			throw new IllegalStateException("null ConfigurationAccessor");
		}

		if (!conf.isWriteable()) {
			throw new RuntimeException("ConfigurationAccessor is not writeable");
		}
		
		StringBuffer result=new StringBuffer();
		result.append("<?xml version=\"1.0\"?>\n");
		result.append("<alarm-definition-list>\n");
		
		Iterator<Alarm> i=alarmDefs.values().iterator();
		while (i.hasNext()) {
			Alarm a = i.next();
			Triplet t = a.getTriplet();
			result.append("\t<alarm-definition fault-family=\"");
			DAOUtil.escapeXMLEntities(result, t.getFaultFamily());
			result.append("\" fault-member=\"");
			DAOUtil.escapeXMLEntities(result, t.getFaultMember());
			result.append("\" fault-code=\"");
			result.append(t.getFaultCode().intValue());
			result.append("\" />\n");
		}
		
		result.append("</alarm-definition-list>\n");
		
		try {
			conf.setConfiguration("/alarms", result.toString());
		} catch (Exception e) {
			throw new RuntimeException("Could not write alarm list", e);
		}
	}
	
	static String memberFromAlarmID(String alarmId)
	{
		int prev=alarmId.indexOf(':');
		int next=alarmId.lastIndexOf(':');
		
		if (!(prev>0 && next>0 && next>prev))
			throw new IllegalStateException();
		
		return alarmId.substring(prev+1, next);
	}

	public Alarm findAlarm(String alarmId)
	{
		Alarm res=getAlarm(alarmId);
		
		if (res==null) {
			throw new LaserObjectNotFoundException("Alarm ID "+alarmId+" expected, but not found");
		}

		return res;
	}

	/**
	 * Get an alarm from the cache.
	 * 
	 * Get an alarm from the cache. If the alarm with the given triplet is not in the cache then it
	 * looks for a default alarm before returning null.
	 * 
	 * If a default alarm is found, then a new alarm is created by cloning the default alarm.
	 * The triplet of this new alarm is set to be equal to the passed alarm ID.
	 * 
	 * @param alarmId The ID of the alarm
	 * @return An alarm with the given ID if it exists in the CDB
	 * 	       If an alarm with the give ID does not exist but a matching default alarm
	 *         is defined then it return a clone of the default alarm
	 *         null If the alarm is not defined in the CDB and a default alarm does not exist
	 *         
	 * 
	 */
	public Alarm getAlarm(String alarmId)
	{
		if (conf==null) {
			throw new IllegalStateException("Missing dal");
		}
		if (alarmId==null) {
			throw new IllegalArgumentException("Invalid null alarm ID");
		}
		
		AlarmImpl alarm =(AlarmImpl)alarmDefs.get(alarmId);
		if (alarm==null) {
			// The alarm is not in the HashMap
			// 
			// Does it exist a default alarm?
			String[] tripletItems = alarmId.split(":");
			if (tripletItems.length!=3) {
				logger.log(AcsLogLevel.ERROR,"Malformed alarm ID: "+alarmId);
				return null;
			}
			// Build the default alarm ID by replacing the FM with DEFAULT_FM
			String defaultTripletID=tripletItems[0]+":"+DEFAULT_FM+":"+tripletItems[2];
			logger.log(AcsLogLevel.DEBUG,alarmId+" not found: looking for default alarm "+defaultTripletID);
			AlarmImpl defaultalarm=(AlarmImpl)alarmDefs.get(defaultTripletID);
			if (defaultalarm==null) {
				// No available default alarm for this triplet
				logger.log(AcsLogLevel.WARNING,"No default alarm found for "+alarmId);
				return null;
			}
			logger.log(AcsLogLevel.DEBUG,"Default alarm found for "+alarmId);
			alarm=(AlarmImpl)defaultalarm.clone();
			Triplet alarmTriplet = new Triplet(tripletItems[0],tripletItems[1],Integer.parseInt(tripletItems[2]));
			alarm.setTriplet(alarmTriplet);
			alarm.setAlarmId(
					Triplet.toIdentifier(
							alarmTriplet.getFaultFamily(), 
							alarmTriplet.getFaultMember(), 
							alarmTriplet.getFaultCode()));
			// Add the alarm in the HashMap
		}
		return alarm;
		
	}
	
	Building loadBuilding(String buildingID)
	{
		if (buildingID.equals(theBuilding.getBuildingNumber()))
			return theBuilding;
		
		return null;
	}
	
	
	public String[] findAlarmIdsByPriority(Integer priority)
	{
		int p=priority.intValue();
		ArrayList<String> result=null;
		Iterator<Entry<String, Alarm>> i=alarmDefs.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, Alarm> e=i.next();
			String id=e.getKey().toString();
			AlarmImpl ai=(AlarmImpl) e.getValue();
			if (ai.getPriority().intValue()==p) {
				if (result==null)
					result=new ArrayList<String>();
				result.add(id);				
			}
		}
		
		if (result==null) {
			return new String[0];
		} else {
			int s=result.size();
			String[] res=new String[s];
			result.toArray(res);
			return res;
		}
	}

	public String findLaserSurveillanceAlarmId()
	{
		if (getAlarm(surveillanceAlarmId)==null)
			throw new LaserObjectNotFoundException("unable to find laser surveillance alarm");
		
		return surveillanceAlarmId;
	}

	public void deleteAlarm(Alarm alarm)
	{
		String member=alarm.getTriplet().getFaultMember();
		if (alarmDefs.remove(alarm.getTriplet().toIdentifier())!=null)
			saveMemberAlarms(member);
	}

	public void saveMemberAlarms(String member)
	{
		return;
	}
	
	public void saveAlarm(Alarm alarm)
	{
		addAlarmToCache(alarm);
		saveMemberAlarms(alarm.getTriplet().getFaultMember());
	}

	static String encodeToXML(Alarm alarm)
	{
		StringBuffer result=new StringBuffer();
		
		result.append("<?xml version=\"1.0\"?>\n");
		
		encodeToXML(result, alarm);
		
		return result.toString();
	}
	
	static String encodeToXML(StringBuffer result, Alarm alarm)
	{
		result.append("<alarm-definition");
		Triplet t=alarm.getTriplet();
		
		if (t==null || t.getFaultCode()==null || t.getFaultFamily()==null || t.getFaultMember()==null)
			throw new IllegalArgumentException("Incomplete alarm");
		
		DAOUtil.encodeAttr(result, "fault-family", t.getFaultFamily());
		DAOUtil.encodeAttr(result, "fault-member", t.getFaultMember());
		DAOUtil.encodeAttr(result, "fault-code", t.getFaultCode().toString());
		
		result.append(">\n");
		
		{
			String sn=alarm.getSystemName();
			String si=alarm.getIdentifier();
			String pd=alarm.getProblemDescription();
			
			if (sn!=null || si!=null || pd!=null) {
				result.append("\t<visual-fields>\n");
				
				// schema says all or nothing, but still, let's not be too picky
				
				if (sn!=null)
					DAOUtil.encodeElem(result, "system-name", sn, 2);
				if (si!=null)
					DAOUtil.encodeElem(result, "identifier", si, 2);
				if (pd!=null)
					DAOUtil.encodeElem(result, "problem-description", pd, 2);
				
				result.append("\t</visual-fields>\n");
			}
		}
		
		DAOUtil.encodeElemIf(result, "instant", alarm.getInstant(), 1);
		DAOUtil.encodeElemIf(result, "cause", alarm.getCause(), 1);
		DAOUtil.encodeElemIf(result, "action", alarm.getAction(), 1);
		DAOUtil.encodeElemIf(result, "consequence", alarm.getConsequence(), 1);
		DAOUtil.encodeElemIf(result, "priority", alarm.getPriority(), 1);
		
		ResponsiblePerson rp=alarm.getResponsiblePerson();
		if (rp!=null)
			DAOUtil.encodeElemIf(result, "responsible-id", rp.getResponsibleId(), 1);

		DAOUtil.encodeElemIf(result, "piquetGSM", alarm.getPiquetGSM(), 1);
		DAOUtil.encodeElemIf(result, "help-url", alarm.getHelpURL(), 1);
		
		Source s=alarm.getSource();
		if (s!=null)
			DAOUtil.encodeElemIf(result, "source-name", s.getName(), 1);
		
		Location l=alarm.getLocation();
		if (l!=null) {
			result.append("\t<location>\n");
			
			Building b=l.getBuilding();
			if (b!=null)
				DAOUtil.encodeElemIf(result, "building", b.getBuildingNumber(), 2);
			
			DAOUtil.encodeElemIf(result, "floor", l.getFloor(), 2);
			DAOUtil.encodeElemIf(result, "room", l.getRoom(), 2);
			DAOUtil.encodeElemIf(result, "mnemonic", l.getMnemonic(), 2);
			DAOUtil.encodeElemIf(result, "position", l.getPosition(), 2);
			
			result.append("\t</location>\n");
		}
		
		DAOUtil.encodeElemIf(result, "piquetEmail", alarm.getPiquetEmail(), 1);
		result.append("</alarm-definition>\n");
		
		return result.toString();
	}
	
	public void updateAlarm(Alarm alarm)
	{
		addAlarmToCache(alarm);
		saveMemberAlarms(alarm.getTriplet().getFaultMember());
	}

	public void updateStatus(Status status)
	{
		// TODO Auto-generated method stub
	}

	public Collection search(String select_sql)
	{
		throw new UnsupportedOperationException();
	}

	public Collection archiveSearch(String select_sql)
	{
		throw new UnsupportedOperationException();
	}

	public Building findBuilding(String building)
	{
		if (building.equals(theBuilding.getBuildingNumber()))
			return theBuilding;
		
		throw new LaserObjectNotFoundException("Couldn't find building "+building);
	}

	public void setConfAccessor(ConfigurationAccessor conf)
	{
		this.conf = conf;
	}

	public void setSurveillanceAlarmId(String surveillanceAlarmId)
	{
		this.surveillanceAlarmId = surveillanceAlarmId;
	}
	
	public void setResponsiblePersonDAO(ResponsiblePersonDAO responsiblePersonDAO)
	{
		this.responsiblePersonDAO=responsiblePersonDAO;
	}
	
	/**
	 * Add an alarm in cache (<code>alarmDefs</code>)
	 * <P>
	 * The {@link Alarm} to add in the cache must be not <code>null</code>
	 * and with a valid ID otherwise an exception is thrown.
	 * 
	 * @param alarm The Alarm to add to the cache
	 */
	private void addAlarmToCache(Alarm alarm) {
		if (alarm==null) {
			throw new IllegalArgumentException("It is forbidden to add null alarms to cache");
		}
		String alarmId=alarm.getAlarmId();
		if (alarmId==null || alarmId.isEmpty()) {
			throw new IllegalStateException("Trying to add an alarm with an invalid ID!");
		}
		alarmDefs.put(alarmId, alarm);
	}
	
	public String[] getAllAlarmIDs()
	{
		Set<String> keyset=alarmDefs.keySet();
		String[] result=new String[keyset.size()];
		keyset.toArray(result);
		return result;
	}
	
	/**
	 * Getter method
	 * 
	 * @return The sources
	 */
	public ConcurrentHashMap<String,Source> getSources() {
		return srcDefs;
		
	}
}
