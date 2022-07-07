package CloneRepresentation;

import java.util.List;

class InfoStruct{

    public String version;
    public String cgid;
    public int size;
}

public class GenealogyEvolution {
	private String ID;
    private String parentID;

    private String childID;
   
    private int srcVersion;
    private int srcCGID;
    private int srcSize;

    private int destVersion;
    private int destCGID;
    private int destSize;
    
    private String cgPattern;

    public int BuildFromCGMap(GroupMapping cgMap,int id, List<GenealogyEvolution> evolutionList){
    	this.ID = String.valueOf(++id);
    	
        this.srcVersion = cgMap.getSrcVersionID();
        this.srcCGID = cgMap.getSrcCGID();
        this.srcSize = cgMap.getSrcCGSize();
        this.destVersion = cgMap.getDestVersionID();
        this.destCGID = cgMap.getDestCGID();
        this.destSize = cgMap.getDestCGSize();

        this.cgPattern = "";
        if (cgMap.getEvolutionPattern().isSTATIC())
        { this.cgPattern += "STATIC"; }
        if (cgMap.getEvolutionPattern().isSAME())
        { this.cgPattern += "+SAME"; }
        if (cgMap.getEvolutionPattern().isADD())
        { this.cgPattern += "+ADD"; }
        if (cgMap.getEvolutionPattern().isSUBSTRACT())
        { this.cgPattern += "+DELETE"; }
        if (cgMap.getEvolutionPattern().isCONSISTENTCHANGE())
        { this.cgPattern += "+CONSISTENTCHANGE"; }
        if (cgMap.getEvolutionPattern().isINCONSISTENTCHANGE())
        { this.cgPattern += "+INCONSISTENTCHANGE"; }
        if (cgMap.getEvolutionPattern().isSPLIT())
        { this.cgPattern += "+SPLIT"; }

        this.parentID = null;
        this.childID = null;
        
        for (int i = 0; i < evolutionList.size(); i++){
            if ((evolutionList.get(i).destVersion == this.srcVersion) && (evolutionList.get(i).destCGID == this.srcCGID)){
                if (evolutionList.get(i).childID == null){
                    evolutionList.get(i).childID = this.ID;
                }
                else
                { evolutionList.get(i).childID += "+" + this.ID; }
                this.parentID = evolutionList.get(i).ID;
                //break;
            }
        }
        
        return Integer.parseInt(this.ID);
   }

	public String getCgPattern() {
		return cgPattern;
	}

	public void setCgPattern(String cgPattern) {
		this.cgPattern = cgPattern;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getParentID() {
		return parentID;
	}

	public void setParentID(String parentID) {
		this.parentID = parentID;
	}

	public String getChildID() {
		return childID;
	}

	public void setChildID(String childID) {
		this.childID = childID;
	}

	public int getSrcVersion() {
		return srcVersion;
	}

	public void setSrcVersion(int srcVersion) {
		this.srcVersion = srcVersion;
	}

	public int getSrcCGID() {
		return srcCGID;
	}

	public void setSrcCGID(int srcCGID) {
		this.srcCGID = srcCGID;
	}

	public int getSrcSize() {
		return srcSize;
	}

	public void setSrcSize(int srcSize) {
		this.srcSize = srcSize;
	}

	public int getDestVersion() {
		return destVersion;
	}

	public void setDestVersion(int destVersion) {
		this.destVersion = destVersion;
	}

	public int getDestCGID() {
		return destCGID;
	}

	public void setDestCGID(int destCGID) {
		this.destCGID = destCGID;
	}

	public int getDestSize() {
		return destSize;
	}

	public void setDestSize(int destSize) {
		this.destSize = destSize;
	}
    
    
    
}
