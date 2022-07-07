package CloneRepresentation;

import java.util.List;


public class CloneGenealogy {
	private int startVersion;
	private int rootCGid;
	
	private int endVersion;
	
	private int age;
	  
	private int[] evoPatternCount;

    private List<GenealogyEvolution> evolutionList;
    
    

	public int getStartVersion() {
		return startVersion;
	}

	public void setStartVersion(int startVersion) {
		this.startVersion = startVersion;
	}

	public int getEndVersion() {
		return endVersion;
	}

	public void setEndVersion(int endVersion) {
		this.endVersion = endVersion;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int[] getEvoPatternCount() {
		return evoPatternCount;
	}

	public void setEvoPatternCount(int[] evoPatternCount) {
		this.evoPatternCount = new int[7];
		this.evoPatternCount = evoPatternCount;
	}

	public int getRootCGid() {
		return rootCGid;
	}

	public void setRootCGid(int rootCGid) {
		this.rootCGid = rootCGid;
	}

	public List<GenealogyEvolution> getEvolutionList() {
		return evolutionList;
	}

	public void setEvolutionList(List<GenealogyEvolution> evolutionList) {
		this.evolutionList = evolutionList;
	}
	


}
