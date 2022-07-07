package CloneRepresentation;

import java.util.HashMap;
//import java.util.TreeMap;


/**
 * @author ZFL
 */

public class VersionMappingSystem {
	
	HashMap<Integer, String> NummapVersion;
	//Integer StartVersion;
	//Integer EndVersion;
	Integer VersionNumer;
	
	public VersionMappingSystem() {
		// TODO Auto-generated constructor stub
		 NummapVersion = new HashMap<>();
		 NummapVersion.put(0, "V1");	 
		 
	}
	
		String LookupSystemVersion(Integer Version){
			
			return NummapVersion.get(Version);
		}	
	
	//Integer lookupVersion(String SystemVersion){
		
	//	return NummapVersion.get(SystemVersion);
	//}
	
		public void setVersionNumer() {
			VersionNumer = NummapVersion.size();
		}
	
		public Integer getVersionNumer() {
			this.setVersionNumer();
			return VersionNumer;
		}
}
