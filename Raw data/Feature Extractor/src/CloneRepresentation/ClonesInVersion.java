package CloneRepresentation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZFL
 */
public class ClonesInVersion {

	private int NumberofCG;
	private int NumberofCF;
	private int VersionID;
	private String SystemVersion;
	private List<CloneGroup> cloneGroup;
	
	
	public void setNumberofCG(int NumberofCG){
		this.NumberofCG=NumberofCG;
	}
	
	public int getNumberofCG(){
		return NumberofCG;
	}
	
	public void setNumberofCF(int NumberofCF){
		this.NumberofCF=NumberofCF;
	}
	
	public int getNumberofCF(){
		return NumberofCF;
	}
	
	public void setVersionID(int VersionID){
		this.VersionID=VersionID;
	}
	
	public int getVersionID(){
		return VersionID;
	}
	
	public void setSystemVersion(String SystemVersion){
		this.SystemVersion=SystemVersion;
	}
	
	public String getSystemVersion(){
		return SystemVersion;
	}
	
	public List<CloneGroup> getCloneGroup() {
		return cloneGroup;
	}

	public void setCloneGroup(List<CloneGroup> cloneGroup) {
		this.cloneGroup = new ArrayList<CloneGroup>();
		this.cloneGroup = cloneGroup;
	}

	
	
	
}
