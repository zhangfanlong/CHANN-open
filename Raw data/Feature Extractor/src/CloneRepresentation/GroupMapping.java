package CloneRepresentation;

import java.util.ArrayList;
import java.util.List;

public class GroupMapping {
	
	private int srcVersionID;
	private int destVersionID;

	private int srcCGID;
	private int srcCGSize;
	
	private int destCGID;
	private int destCGSize;

	private EvolutionPattern evolutionPattern;
	
	private List<FragmentMapping> fragMapList;
	
	public GroupMapping(){
		srcVersionID = -1;
		destVersionID = -1;
		srcCGID = -1;
		destCGID = -1;
		srcCGSize = -1;
		destCGSize = -1;
		evolutionPattern = new EvolutionPattern();
		fragMapList = new ArrayList<FragmentMapping>();
	}
	
	public int getSrcCGID() {
		return srcCGID;
	}

	public void setSrcCGID(int srcCGID) {
		this.srcCGID = srcCGID;
	}

	public int getSrcCGSize() {
		return srcCGSize;
	}

	public void setSrcCGSize(int srcCGSize) {
		this.srcCGSize = srcCGSize;
	}

	public int getDestCGID() {
		return destCGID;
	}

	public void setDestCGID(int destCGID) {
		this.destCGID = destCGID;
	}

	public int getDestCGSize() {
		return destCGSize;
	}

	public void setDestCGSize(int destCGSize) {
		this.destCGSize = destCGSize;
	}

	public EvolutionPattern getEvolutionPattern() {
		return evolutionPattern;
	}

	public void setEvolutionPattern(EvolutionPattern evolutionPattern) {
		this.evolutionPattern = evolutionPattern;
	}

	public int getSrcVersionID() {
		return srcVersionID;
	}

	public void setSrcVersionID(int srcVersionID) {
		this.srcVersionID = srcVersionID;
	}

	public int getDestVersionID() {
		return destVersionID;
	}

	public void setDestVersionID(int destVersionID) {
		this.destVersionID = destVersionID;
	}

	public List<FragmentMapping> getFragMapList() {
		return fragMapList;
	}

	public void setFragMapList(List<FragmentMapping> fragMapList) {
		this.fragMapList = new ArrayList<FragmentMapping>();
		this.fragMapList = fragMapList;
	}
	
	
}
