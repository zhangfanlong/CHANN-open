package PreProcess;

import java.util.ArrayList;
import java.util.List;

import CloneRepresentation.CloneGenealogy;
import CloneRepresentation.GenealogyEvolution;
import CloneRepresentation.GroupMapping;
import Global.VariationInformation;

class CGMapInfo{
    public String id;
    public String pattern;
}

public class CreateGenealogyInfo {
	
	public List<GenealogyEvolution> evoGeneaList;
	public int[] evoPatternCount;
	private int id = 0;
	private int endIndex = -1;
	
	/*private boolean IsMappingVersionSuccessive() {
		for (GroupMapping mapping : VariationInformation.mappingInfo) {
			if (mapping.getDestVersionID() - mapping.getSrcVersionID() != 1)
				return false;
		}
		return true;
	}*/
	
	public void CreateGenealogyForAll(){
		/*if(!this.IsMappingVersionSuccessive()){
			System.out.println("...");
			return;
		}*/
		int prev = 0; 
		for (GroupMapping mapping : VariationInformation.mappingInfo) {
			if (mapping.getSrcVersionID() == 0) {
				
				if (mapping.getSrcCGID() != prev) {

					CloneGenealogy cloneGenealogy = new CloneGenealogy();
					cloneGenealogy.setStartVersion(0);
					cloneGenealogy.setRootCGid(mapping.getSrcCGID());

					endIndex = -1;
					id = 0;
					evoGeneaList = new ArrayList<GenealogyEvolution>();
					this.BuildGenealogyEvolution(mapping);

					cloneGenealogy.setEndVersion(cloneGenealogy.getStartVersion() + endIndex);
					cloneGenealogy.setAge(endIndex + 1);

					this.evoPatternCount = new int[7];
					for (GenealogyEvolution evolution : this.evoGeneaList) {
						if (evolution.getCgPattern().contains("STATIC")) {
							this.evoPatternCount[0]++;
						}
						if (evolution.getCgPattern().contains("SAME")) {
							this.evoPatternCount[1]++;
						}
						if (evolution.getCgPattern().contains("ADD")) {
							this.evoPatternCount[2]++;
						}
						if (evolution.getCgPattern().contains("DELETE")) {
							this.evoPatternCount[3]++;
						}
						if (!evolution.getCgPattern().contains("INCONSISTENTCHANGE") && evolution.getCgPattern().contains("CONSISTENTCHANGE")) {
							this.evoPatternCount[4]++;
						}
						if (evolution.getCgPattern().contains("INCONSISTENTCHANGE")) {
							this.evoPatternCount[5]++;
						}
						if (evolution.getCgPattern().contains("SPLIT")) {
							this.evoPatternCount[6]++;
						}
					}
					cloneGenealogy.setEvoPatternCount(evoPatternCount);
					cloneGenealogy.setEvolutionList(evoGeneaList);
					prev = mapping.getSrcCGID();
					VariationInformation.cloneGenealogy.add(cloneGenealogy);
				}
			}
		}
		
        if(VariationInformation.unMappedSrcInfo.size() != 0){
            for(GroupMapping singleMapping : VariationInformation.unMappedSrcInfo){
            	if(singleMapping.getSrcVersionID() == 0){
            		CloneGenealogy singleCG = new CloneGenealogy();
            		singleCG.setStartVersion(singleMapping.getSrcVersionID());
            		singleCG.setRootCGid(singleMapping.getSrcCGID());
            		System.out.println("" + singleMapping.getSrcVersionID());
            		VariationInformation.singleCgGenealogyList.add(singleCG);
            	}
            }
        }
      
        if(VariationInformation.unMappedDestInfo.size() != 0){
        	/*	for(GroupMapping destMapping : VariationInformation.unMappedDestInfo){
        		System.out.println("");
        	}
        	*/
        	for(GroupMapping destMapping : VariationInformation.unMappedDestInfo){
        		CloneGenealogy cloneGenealogy = new CloneGenealogy();
            	cloneGenealogy.setStartVersion(destMapping.getDestVersionID());
            	cloneGenealogy.setRootCGid(destMapping.getDestCGID());
            	
            	endIndex = -1;
            	id = 0;
            	evoGeneaList = new ArrayList<GenealogyEvolution>();
            	this.BuildGenealogyEvolution(destMapping);
            	
            	if(this.endIndex > 0){
            		cloneGenealogy.setEndVersion(cloneGenealogy.getStartVersion() + endIndex);
                	cloneGenealogy.setAge(endIndex+1);       	
                	
                    this.evoPatternCount = new int[7];
                    for (GenealogyEvolution evolution : this.evoGeneaList){
                        if (evolution.getCgPattern().contains("STATIC") )
                        { this.evoPatternCount[0]++; }
                        if (evolution.getCgPattern().contains("SAME"))
                        { this.evoPatternCount[1]++; }
                        if (evolution.getCgPattern().contains("ADD"))
                        { this.evoPatternCount[2]++; }
                        if (evolution.getCgPattern().contains("DELETE"))
                        { this.evoPatternCount[3]++; }
                        if (!evolution.getCgPattern().contains("INCONSISTENTCHANGE") && evolution.getCgPattern().contains("CONSISTENTCHANGE"))
                        { this.evoPatternCount[4]++; }
                        if (evolution.getCgPattern().contains("INCONSISTENTCHANGE"))
                        { this.evoPatternCount[5]++; }
                        if (evolution.getCgPattern().contains("SPLIT"))
                        { this.evoPatternCount[6]++; }
                    }
                    cloneGenealogy.setEvoPatternCount(evoPatternCount);
                	cloneGenealogy.setEvolutionList(evoGeneaList);	
                	VariationInformation.cloneGenealogy.add(cloneGenealogy);
            	} 	
            	else{
            		VariationInformation.singleCgGenealogyList.add(cloneGenealogy);
            	}
        	}
        }

	}
        	
    public void BuildGenealogyEvolution(GroupMapping mapping){
    	GenealogyEvolution gEvolu = new GenealogyEvolution ();
    	id = gEvolu.BuildFromCGMap(mapping, id, evoGeneaList);
    	endIndex++;
    	evoGeneaList.add(gEvolu);
    	List<GroupMapping> targetMapping = new ArrayList<GroupMapping>();
    	for(GroupMapping tar : VariationInformation.mappingInfo){
    		if((mapping.getDestVersionID() == tar.getSrcVersionID()) && 
    				(mapping.getDestCGID() == tar.getSrcCGID())){
    			targetMapping.add(tar);
    		}
    	}
    	if(targetMapping.size() > 0){
    		for(GroupMapping tar : targetMapping){
    			this.BuildGenealogyEvolution(tar);
    		}	
    	}
    	return;
    }
}
