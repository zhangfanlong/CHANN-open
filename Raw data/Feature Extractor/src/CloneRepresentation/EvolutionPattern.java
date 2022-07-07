package CloneRepresentation;

public class EvolutionPattern {
	private boolean STATIC; 
	private boolean SAME;
	private boolean ADD;
	private boolean SUBSTRACT; 
	private boolean CONSISTENTCHANGE;
	private boolean INCONSISTENTCHANGE;
	private boolean SPLIT;
	private String MapGroupIDs;

	public boolean isSTATIC() {
		return STATIC;
	}
	public void setSTATIC(boolean sTATIC) {
		STATIC = sTATIC;
	}
	public boolean isSAME() {
		return SAME;
	}
	public void setSAME(boolean sAME) {
		SAME = sAME;
	}
	public boolean isADD() {
		return ADD;
	}
	public void setADD(boolean aDD) {
		ADD = aDD;
	}
	public boolean isSUBSTRACT() {
		return SUBSTRACT;
	}
	public void setSUBSTRACT(boolean sUBSTRACT) {
		SUBSTRACT = sUBSTRACT;
	}
	public boolean isCONSISTENTCHANGE() {
		return CONSISTENTCHANGE;
	}
	public void setCONSISTENTCHANGE(boolean cONSISTENTCHANGE) {
		CONSISTENTCHANGE = cONSISTENTCHANGE;
	}
	public boolean isINCONSISTENTCHANGE() {
		return INCONSISTENTCHANGE;
	}
	public void setINCONSISTENTCHANGE(boolean iNCONSISTENTCHANGE) {
		INCONSISTENTCHANGE = iNCONSISTENTCHANGE;
	}
	public boolean isSPLIT() {
		return SPLIT;
	}
	public void setSPLIT(boolean sPLIT) {
		SPLIT = sPLIT;
	}
	public String getMapGroupIDs() {
		return MapGroupIDs;
	}
	public void setMapGroupIDs(String mapGroupIDs) {
		MapGroupIDs = mapGroupIDs;
	}
	
	
}
