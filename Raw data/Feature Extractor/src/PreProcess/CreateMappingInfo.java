package PreProcess;

import java.util.ArrayList;
import java.util.List;





import PreProcess.CreateCRDInfo.CRDMatchLevel;
import CRDInformation.CloneSourceInfo;
import CloneRepresentation.CRD;
import CloneRepresentation.CloneFragment;
import CloneRepresentation.CloneGroup;
import CloneRepresentation.EvolutionPattern;
import CloneRepresentation.FragmentMapping;
import CloneRepresentation.GroupMapping;
import Global.VariationInformation;

abstract class Mapping{
    public String ID;
}

class  CGInfo {
    public String id;
    public int size;
}

class MappingList extends ArrayList { }

class CloneFragmentMapping extends Mapping{
    public String SrcCFID;
    public String DestCFID;


    public CRDMatchLevel CrdMatchLevel;

    public float textSim;
    public CloneSourceInfo[] sourceInfos = new CloneSourceInfo[2];
}


class CloneGroupMapping extends Mapping{

    public CGInfo srcCGInfo;
    public CGInfo destCGInfo;
    public EvolutionPattern EvoPattern;
    public MappingList CFMapList;
    
    public int CreateCGMapping(CloneGroup srcGroupEle, CloneGroup destGroupEle,int mapCount){
    	this.srcCGInfo = new CGInfo();
    	this.srcCGInfo.id = String.valueOf(srcGroupEle.getCGID());
    	this.srcCGInfo.size = srcGroupEle.getNumberofCF();
    	
    	this.destCGInfo = new CGInfo();
    	this.destCGInfo.id = String.valueOf(destGroupEle.getCGID());
    	this.destCGInfo.size = destGroupEle.getNumberofCF();

        this.CFMapList = MapCF(srcGroupEle, destGroupEle);
        this.ID = String.valueOf(++mapCount);
        return mapCount;
    }
    
    public MappingList MapCF(CloneGroup srcCG, CloneGroup destCG){
    	
        MappingList cfMappingList = new MappingList();
        List<CRD> srcCGCrdList = new ArrayList<CRD>();
        List<CRD> destCGCrdList = new ArrayList<CRD>();
        for(CloneFragment frag : srcCG.getClonefragment()){
        	srcCGCrdList.add(frag.getCRD());
        }
        for(CloneFragment frag : destCG.getClonefragment()){
        	destCGCrdList.add(frag.getCRD());
        }
 
        int i, j;
        boolean[] srcCFMapped = new boolean[srcCGCrdList.size()];
        for (i = 0; i < srcCGCrdList.size(); i++)
        	srcCFMapped[i] = false; 
        boolean[] destCFMapped = new boolean[destCGCrdList.size()];
        for (j = 0; j < destCGCrdList.size(); j++)
        	destCFMapped[j] = false; 

        if (srcCGCrdList != null && destCGCrdList != null && srcCGCrdList.size() != 0 && destCGCrdList.size() != 0){
            CRDMatchLevel[][] crdMatchMatrix = new CRDMatchLevel[srcCGCrdList.size()][destCGCrdList.size()];
            float[][] textSimMatrix = new float[srcCGCrdList.size()][destCGCrdList.size()];

            i = -1;
            int mapCount = 0;
            for (CRD srcCRD : srcCGCrdList){
                i++;
                j = -1;
                for (CRD destCRD : destCGCrdList){
                    j++;
                    if (!destCFMapped[j]){
                        CRDMatchLevel matchLevel = CreateCRDInfo.GetCRDMatchLevel(srcCRD, destCRD);
                        crdMatchMatrix[i][j] = matchLevel;
                        textSimMatrix[i][j] = CreateCRDInfo.GetTextSimilarity(srcCRD, destCRD, true);
                    }
                }
            }
 
            for (i = 0; i < srcCGCrdList.size(); i++){
                if (!srcCFMapped[i]){ 
                    int maxTextSimIndex = -1;
                    int maxMatchLevelIndex = -1;
                    float maxTextSim = CreateCRDInfo.defaultTextSimTh;
                    CRDMatchLevel maxMatchLevel = CRDMatchLevel.DIFFERENT;
                    for (j = 0; j < destCGCrdList.size(); j++){                 
                        if (!destCFMapped[j]){             
                            if (textSimMatrix[i][j] >= maxTextSim)
                            { maxTextSim = textSimMatrix[i][j]; maxTextSimIndex = j; }
                            if (crdMatchMatrix[i][j].ordinal() > maxMatchLevel.ordinal())
                            { maxMatchLevel = crdMatchMatrix[i][j]; maxMatchLevelIndex = j; }
                        }
                        else continue; 
                    }
                    if (maxTextSimIndex > -1 || maxMatchLevelIndex > -1){
                        int finalIndex;
                        if(maxTextSimIndex == -1){
                        	finalIndex = maxMatchLevelIndex;
                        }else if(maxMatchLevelIndex == -1){
                        	finalIndex = maxTextSimIndex;
                        }else if (maxTextSimIndex == maxMatchLevelIndex){   
                            finalIndex = maxTextSimIndex;
                        }else if (crdMatchMatrix[i][maxTextSimIndex].ordinal() < maxMatchLevel.ordinal()){    
                            finalIndex = maxMatchLevelIndex;
                        }else if (textSimMatrix[i][maxMatchLevelIndex] < maxTextSim){  
                            finalIndex = maxTextSimIndex;
                        }else{
                            if (Math.abs(maxTextSimIndex - i) < Math.abs(maxMatchLevelIndex - i))
                            { finalIndex = maxTextSimIndex; }
                            else
                            { finalIndex = maxMatchLevelIndex; }
                        }
                        CloneFragmentMapping cfMapping = new CloneFragmentMapping();
                        cfMapping.ID = String.valueOf(++mapCount);
                        cfMapping.SrcCFID = String.valueOf(i + 1);
                        cfMapping.DestCFID = String.valueOf(finalIndex + 1);
                        cfMapping.textSim = textSimMatrix[i][finalIndex];
                        cfMapping.CrdMatchLevel = crdMatchMatrix[i][finalIndex];
                        cfMappingList.add(cfMapping);
                        srcCFMapped[i] = true;
                        destCFMapped[finalIndex] = true;
                    }
                }
            }
        }
        return cfMappingList;
    }
}

public class CreateMappingInfo extends Mapping{
	private String srcFileName;
	private String destFileName;

	private List<CloneGroupMapping> CGMapList;
	private List UnMappedSrcCGList;
	private List UnMappedDestCGList;

	public void MapBetweenVersions(List<CloneGroup> srcGroupList, List<CloneGroup> destGroupList){

		CGMapList = new ArrayList<CloneGroupMapping>();
		UnMappedSrcCGList = null;
        UnMappedDestCGList = null;
        int mapCount = 0;
      
        int[][] cgMatchLevelMatrix = new int[srcGroupList.size()][destGroupList.size()];
        float[][] cgLocationOverlapMatrix = new float[srcGroupList.size()][destGroupList.size()];
        for(int i=0;i<srcGroupList.size();i++){
        	for(int j=0;j<destGroupList.size();j++){
        		cgMatchLevelMatrix[i][j] = -1;
        		cgLocationOverlapMatrix[i][j] = -1;
        	}
        }
        
        boolean[] isSrcCGMapped = new boolean[srcGroupList.size()];
        boolean[] isDestCGMapped = new boolean[destGroupList.size()];
        for (int srcIndex = 0; srcIndex < srcGroupList.size(); srcIndex++)
        	isSrcCGMapped[srcIndex] = false; 
        for (int destIndex = 0; destIndex < destGroupList.size(); destIndex++)
        	isDestCGMapped[destIndex] = false; 
        
        for(int i=0;i<srcGroupList.size();i++){
        	for(int j=0;j<destGroupList.size();j++){
        		if(!isDestCGMapped[j] && IsCGMatch(srcGroupList.get(i),destGroupList.get(j),CRDMatchLevel.METHODINFOMATCH)){
                    CloneGroupMapping cgMapping = new CloneGroupMapping();
                    mapCount = cgMapping.CreateCGMapping(srcGroupList.get(i), destGroupList.get(j),mapCount);
                    this.CGMapList.add(cgMapping);
                    isSrcCGMapped[i] = true;
                    isDestCGMapped[j] = true;
                    break; 
                }
                else continue;
        	}
        }
		
        for(int i=0;i<srcGroupList.size();i++){
        	if(!isSrcCGMapped[i]){
        		for(int j=0;j<destGroupList.size();j++){
        			 if (!isDestCGMapped[j] && IsCGMatch(srcGroupList.get(i),destGroupList.get(j), CRDMatchLevel.METHODNAMEMATCH)){
        				 CloneGroupMapping cgMapping = new CloneGroupMapping();
        				 mapCount = cgMapping.CreateCGMapping(srcGroupList.get(i), destGroupList.get(j),mapCount);
                         this.CGMapList.add(cgMapping);
                         isSrcCGMapped[i] = true;
                         isDestCGMapped[j] = true;
                         break;
                     }
        			 else continue;
        		}
        	}
        	else continue;
        }
        
        for(int i=0;i<srcGroupList.size();i++){
        	if(!isSrcCGMapped[i]){
        		for(int j=0;j<destGroupList.size();j++){
        			 if (!isDestCGMapped[j] && IsCGMatch(srcGroupList.get(i),destGroupList.get(j), CRDMatchLevel.FILECLASSMATCH)){
        				 CloneGroupMapping cgMapping = new CloneGroupMapping();
        				 mapCount = cgMapping.CreateCGMapping(srcGroupList.get(i), destGroupList.get(j),mapCount);
                         this.CGMapList.add(cgMapping);
                         isSrcCGMapped[i] = true;
                         isDestCGMapped[j] = true;
                         break;
                     }
        			 else continue;
        		}
        	}
        	else continue;
        }
        
        for(int i=0;i<srcGroupList.size();i++){
        	if(!isSrcCGMapped[i]){
        		if (this.UnMappedSrcCGList == null)
        			this.UnMappedSrcCGList = new MappingList(); 
                CGInfo info = new CGInfo();
                info.id = String.valueOf(srcGroupList.get(i).getCGID());
                info.size = srcGroupList.get(i).getNumberofCF();
                
                this.UnMappedSrcCGList.add(info);
        	}
        }
        
        for(int j=0;j<destGroupList.size();j++){
        	if(!isDestCGMapped[j]){
        		for(int i=0;i<srcGroupList.size();i++){ 
        			 if (isSrcCGMapped[i]  && IsCGMatch(srcGroupList.get(i),destGroupList.get(j), CRDMatchLevel.METHODNAMEMATCH)){
        			
        				 int mapIndex = -1;
                         for (CloneGroupMapping cgMap : this.CGMapList){
                             mapIndex++;
                             String classId = String.valueOf(srcGroupList.get(i).getCGID());
                             if (classId != null && cgMap.srcCGInfo.id.equals(classId)){
                                 CloneGroupMapping cgMapping = new CloneGroupMapping();
                                 mapCount = cgMapping.CreateCGMapping(srcGroupList.get(i), destGroupList.get(j),mapCount);
                                 this.CGMapList.add(mapIndex + 1, cgMapping);
                                 isDestCGMapped[j] = true;                                 
                                 break;
                             }
                         }
                         break;
                     }
        			 else continue;
        		}
        	}
        }
        
        for(int j=0;j<destGroupList.size();j++){
        	if (!isDestCGMapped[j]){
                if (this.UnMappedDestCGList == null)
                	this.UnMappedDestCGList = new MappingList();
                CGInfo info = new CGInfo();
                info.id = String.valueOf(destGroupList.get(j).getCGID());
                info.size = destGroupList.get(j).getNumberofCF();

            	this.UnMappedDestCGList.add(info);
            }
        }
	}
	

	private boolean IsCGMatch(CloneGroup srcCG, CloneGroup destCG, CRDMatchLevel matchLevelThres){
		
        float overlapTh = -1;   
        for(CloneFragment srcCF :  srcCG.getClonefragment()){
        	CRD srcCRD = srcCF.getCRD();
        	for(CloneFragment destCF : destCG.getClonefragment()){
        		CRD destCRD = destCF.getCRD();
        		CRDMatchLevel matchLevel = CreateCRDInfo.GetCRDMatchLevel(srcCRD, destCRD);
        		if ( matchLevel.ordinal() >= matchLevelThres.ordinal()){
                    if (matchLevelThres.ordinal() >= CRDMatchLevel.METHODNAMEMATCH.ordinal()){
                   	 
                        if (matchLevelThres.ordinal() == CRDMatchLevel.METHODINFOMATCH.ordinal()){ 
                        	overlapTh = CreateCRDInfo.locationOverlap1; 
                        }
                        else if (matchLevelThres.ordinal() == CRDMatchLevel.METHODNAMEMATCH.ordinal()){ 
                        	overlapTh = CreateCRDInfo.locationOverlap2; 
                        }

                        float overlap = CreateCRDInfo.GetLocationOverlap(srcCRD, destCRD);
                        if (overlap >= overlapTh)return true; 
                        else{            	 
                            float textSim = CreateCRDInfo.GetTextSimilarity(srcCRD, destCRD, true);
                            if (textSim >= CreateCRDInfo.defaultTextSimTh)	return true; 
                        }
                    }
                    if (matchLevelThres.ordinal() == CRDMatchLevel.FILECLASSMATCH.ordinal()){
                    
                        float textSim = CreateCRDInfo.GetTextSimilarity(srcCRD, destCRD, true);
                        if (textSim >= CreateCRDInfo.defaultTextSimTh)	return true;
                    }
                }
        	}
        }
        
        return false;
	}
	public void RecognizeEvolutionPattern(){
    	boolean[] atGroupFlag;
        if (this.CGMapList.size() > 0){
        	atGroupFlag = new boolean[this.CGMapList.size()];		
        	for (CloneGroupMapping cgMap : this.CGMapList) {
    	
				if (cgMap.EvoPattern == null)
					cgMap.EvoPattern = new EvolutionPattern();


				if ((cgMap.srcCGInfo.size == cgMap.destCGInfo.size) && (cgMap.srcCGInfo.size == cgMap.CFMapList.size())){	
					
					cgMap.EvoPattern.setSAME(true);
					
					/*
					boolean equal = true;
					float textSim = ((CloneFragmentMapping)cgMap.CFMapList.get(0)).textSim;
					for (int k = 1; k < cgMap.CFMapList.size(); k++) {
						if (Math.abs(((CloneFragmentMapping) cgMap.CFMapList.get(k)).textSim - textSim) > 0.007) {
							equal = false;
							break;
						}
					}
					if(equal){
						if(textSim < 1) cgMap.EvoPattern.setCONSISTENTCHANGE(true);
						else cgMap.EvoPattern.setSTATIC(true);
					}else { 
						cgMap.EvoPattern.setINCONSISTENTCHANGE(true); 
					}
					*/
										
					boolean Static = true;
					for(int m=0;m<cgMap.CFMapList.size()-1; m++){
						float textSim_m = ((CloneFragmentMapping) cgMap.CFMapList.get(m)).textSim;
						for(int n=m+1;n<cgMap.CFMapList.size(); n++){
							float textSim_n = ((CloneFragmentMapping) cgMap.CFMapList.get(n)).textSim;
							if (textSim_m < 1 && textSim_n < 1) {
								if(Math.abs(textSim_m - textSim_n) <= 0.007 ) {
									cgMap.EvoPattern.setCONSISTENTCHANGE(true);
								}
								Static = false;
							} else if(Math.abs(textSim_m - 1) <= 0.0000001 && Math.abs(textSim_n - 1) <= 0.0000001){
								if (Static)	Static = true; 
							} else {
								if (Static)	Static = false; 
								}
						}
					}
					if(Static) {
						cgMap.EvoPattern.setSTATIC(true);
					} else {
						if(!cgMap.EvoPattern.isCONSISTENTCHANGE()) {
							cgMap.EvoPattern.setINCONSISTENTCHANGE(true);
						} 
					}
					
					/*
					int chanFraCount = 0;
					int unChagFraCount = 0;
					for (int k = 0; k < cgMap.CFMapList.size(); k++) {
						if (((CloneFragmentMapping) cgMap.CFMapList.get(k)).textSim < 1) {
							chanFraCount ++;
						}else if (((CloneFragmentMapping) cgMap.CFMapList.get(k)).textSim == 1) {
							unChagFraCount ++;
						}
					}
					if(unChagFraCount == cgMap.CFMapList.size()) {
						cgMap.EvoPattern.setSTATIC(true);
					} else if(chanFraCount >= 2){
						cgMap.EvoPattern.setCONSISTENTCHANGE(true);
					} else {
						cgMap.EvoPattern.setINCONSISTENTCHANGE(true); 
					}
						 */
				}
				

				if ((cgMap.CFMapList.size() < cgMap.destCGInfo.size)){
									
					cgMap.EvoPattern.setADD(true);
					
					/*
					boolean equal = true;
					float textSim = ((CloneFragmentMapping)cgMap.CFMapList.get(0)).textSim;
					for (int k = 1; k < cgMap.CFMapList.size(); k++) {
						if (Math.abs(((CloneFragmentMapping) cgMap.CFMapList.get(k)).textSim - textSim) > 0.0000001) {
							equal = false;
							break;
						}
					}
					if(equal){
						if(textSim < 1) cgMap.EvoPattern.setCONSISTENTCHANGE(true);
					}else { 
						cgMap.EvoPattern.setINCONSISTENTCHANGE(true); 
					}
					*/
					
					boolean Static = true;
					for(int m=0;m<cgMap.CFMapList.size()-1; m++){
						float textSim_m = ((CloneFragmentMapping) cgMap.CFMapList.get(m)).textSim;
						for(int n=m+1;n<cgMap.CFMapList.size(); n++){
							float textSim_n = ((CloneFragmentMapping) cgMap.CFMapList.get(n)).textSim;
							if (textSim_m < 1 && textSim_n < 1) {
								if(Math.abs(textSim_m - textSim_n) <= 0.007 ) {
									cgMap.EvoPattern.setCONSISTENTCHANGE(true);
								}
								Static = false;
							} else if(Math.abs(textSim_m - 1) <= 0.007 && Math.abs(textSim_n - 1) <= 0.007){
								if (Static)	Static = true; 
							} else {if (Static)	Static = false; }
						}
					}
					if(Static) {
						cgMap.EvoPattern.setSTATIC(true);
					} else {
						if(!cgMap.EvoPattern.isCONSISTENTCHANGE()) {
							cgMap.EvoPattern.setINCONSISTENTCHANGE(true);
						} 
					}
					
					/*
					int chanFraCount = 0;
					for (int k = 0; k < cgMap.CFMapList.size(); k++) {
						if (((CloneFragmentMapping) cgMap.CFMapList.get(k)).textSim < 1) {
							chanFraCount ++;
						}
					}
					if(chanFraCount >= 2){
						cgMap.EvoPattern.setCONSISTENTCHANGE(true);
					} else {
						cgMap.EvoPattern.setINCONSISTENTCHANGE(true); 
					}*/
				}
	            
				if ((cgMap.CFMapList.size() < cgMap.srcCGInfo.size)) {
					
					cgMap.EvoPattern.setSUBSTRACT(true);
					
					/*
					boolean equal = true;
					float textSim = ((CloneFragmentMapping)cgMap.CFMapList.get(0)).textSim;
					for (int k = 1; k < cgMap.CFMapList.size(); k++) {
						if (Math.abs(((CloneFragmentMapping) cgMap.CFMapList.get(k)).textSim - textSim) > 0.0000001) {
							equal = false;
							break;
						}
					}
					if(equal){
						if(textSim < 1) cgMap.EvoPattern.setCONSISTENTCHANGE(true);
					}else { 
						cgMap.EvoPattern.setINCONSISTENTCHANGE(true); 
					}
					*/
						
					boolean Static = true;
					for(int m=0;m<cgMap.CFMapList.size()-1; m++){
						float textSim_m = ((CloneFragmentMapping) cgMap.CFMapList.get(m)).textSim;
						for(int n=m+1;n<cgMap.CFMapList.size(); n++){
							float textSim_n = ((CloneFragmentMapping) cgMap.CFMapList.get(n)).textSim;
							if (textSim_m < 1 && textSim_n < 1) {
								if(Math.abs(textSim_m - textSim_n) <= 0.007 ) {
									cgMap.EvoPattern.setCONSISTENTCHANGE(true);
								}
								Static = false;
							} else if(Math.abs(textSim_m - 1) <= 0.007 && Math.abs(textSim_n - 1) <= 0.007){
								if (Static)	Static = true; 
							} else {if (Static)	Static = false; }
						}
					}
					if(Static) {
						cgMap.EvoPattern.setSTATIC(true);
					} else {
						if(!cgMap.EvoPattern.isCONSISTENTCHANGE()) {cgMap.EvoPattern.setINCONSISTENTCHANGE(true);} 
					}
					
					/*
					int chanFraCount = 0;
					for (int k = 0; k < cgMap.CFMapList.size(); k++) {
						if (((CloneFragmentMapping) cgMap.CFMapList.get(k)).textSim < 1) {
							chanFraCount ++;
						}
					}
					if(chanFraCount >= 2){
						cgMap.EvoPattern.setCONSISTENTCHANGE(true);
					} else {
						cgMap.EvoPattern.setINCONSISTENTCHANGE(true); 
					}*/
				}

				
			/*	
				boolean textSameFlag = true;
				for (int k = 0; k < cgMap.CFMapList.size(); k++) {
					if (((CloneFragmentMapping) cgMap.CFMapList.get(k)).textSim < 1) {
						textSameFlag = false;
						break;
					}
				}
				if (textSameFlag)
					cgMap.EvoPattern.setSTATIC(true);
			*/

				int i = this.CGMapList.indexOf(cgMap);
				if (!atGroupFlag[i] && i < this.CGMapList.size() - 1) {
					while (i < this.CGMapList.size() - 1
							&& ((CloneGroupMapping) this.CGMapList.get(i + 1)).srcCGInfo.id == cgMap.srcCGInfo.id) {
						if (cgMap.EvoPattern.getMapGroupIDs() == null) {
							cgMap.EvoPattern.setMapGroupIDs(cgMap.ID);
							cgMap.EvoPattern.setSPLIT(true);
							cgMap.EvoPattern.setINCONSISTENTCHANGE(true);
							atGroupFlag[i] = true;
						}
						cgMap.EvoPattern.setMapGroupIDs(cgMap.EvoPattern.getMapGroupIDs() + ",");
						cgMap.EvoPattern.setMapGroupIDs(cgMap.EvoPattern.getMapGroupIDs() + ((CloneGroupMapping) this.CGMapList.get(i + 1)).ID);
						i++;
					}
					for (int j = this.CGMapList.indexOf(cgMap) + 1; j <= i; j++) {
						((CloneGroupMapping) this.CGMapList.get(j)).EvoPattern.setSPLIT(true);
						cgMap.EvoPattern.setINCONSISTENTCHANGE(true);
						((CloneGroupMapping) this.CGMapList.get(j)).EvoPattern.setMapGroupIDs(cgMap.EvoPattern.getMapGroupIDs());
						atGroupFlag[j] = true;
					}
				}
			}//for
			
        }
       
    }
	
	/*public void SaveMappingForGroup(int srcVersion,int destVersion){
		GroupMapping gSrcMap;
		GroupMapping gDestMap;

		for (CloneGroupMapping cgMap : this.CGMapList){
			gSrcMap = new GroupMapping();
			gDestMap = new GroupMapping();
			gSrcMap.setDestVersionID(destVersion);
			gSrcMap.setDestCGID(Integer.parseInt(cgMap.destCGInfo.id));
			gSrcMap.setDestCGSize(cgMap.destCGInfo.size);
			
			gDestMap.setSrcVersionID(srcVersion);
			gDestMap.setSrcCGID(Integer.parseInt(cgMap.srcCGInfo.id));
			gDestMap.setSrcCGSize(cgMap.srcCGInfo.size);
			
			List<FragmentMapping> fMapList = new ArrayList<FragmentMapping>();
			FragmentMapping fMap = new FragmentMapping();
			for(int k=0;k<cgMap.CFMapList.size();k++){
				fMap.setSrcCFid(Integer.parseInt(((CloneFragmentMapping)cgMap.CFMapList.get(k)).SrcCFID));
				fMap.setDestCFid(Integer.parseInt(((CloneFragmentMapping)cgMap.CFMapList.get(k)).DestCFID));
				fMap.setCRDMatchLevel((((CloneFragmentMapping)cgMap.CFMapList.get(k)).CrdMatchLevel).toString());
				fMap.setTextSim(((CloneFragmentMapping)cgMap.CFMapList.get(k)).textSim);
				
				fMapList.add(fMap);
			}
			
			EvolutionPattern evoPattern = new EvolutionPattern();
			evoPattern.setSTATIC(cgMap.EvoPattern.isSTATIC());
			evoPattern.setSAME(cgMap.EvoPattern.isSAME());
			evoPattern.setADD(cgMap.EvoPattern.isADD());
			evoPattern.setSUBSTRACT(cgMap.EvoPattern.isSUBSTRACT());
			evoPattern.setCONSISTENTCHANGE(cgMap.EvoPattern.isCONSISTENTCHANGE());
			evoPattern.setINCONSISTENTCHANGE(cgMap.EvoPattern.isINCONSISTENTCHANGE());
			evoPattern.setSPLIT(cgMap.EvoPattern.isSPLIT());
			if(cgMap.EvoPattern.getMapGroupIDs() != null){
				evoPattern.setMapGroupIDs(String.valueOf(cgMap.EvoPattern.getMapGroupIDs()));
			}

			for(CloneGroup group : VariationInformation.cloneGroup){
				if(group.getVersionID() == srcVersion && group.getCGID() == Integer.parseInt(cgMap.srcCGInfo.id)){
					group.setDestGroupMapping(gSrcMap);
					for(CloneFragment fragment : group.getClonefragment()){
						for(FragmentMapping fraMap : fMapList){
							if(fragment.getCFID() == fraMap.getSrcCFid()){
								fragment.setDestFragmentMapping(fMap);
							}
						}
					}
				}
				else if(group.getVersionID() == destVersion && group.getCGID() == Integer.parseInt(cgMap.destCGInfo.id)){
					group.setSrcGroupMapping(gDestMap);
					for(CloneFragment fragment : group.getClonefragment()){
						for(FragmentMapping fraMap : fMapList){
							if(fragment.getCFID() == fraMap.getDestCFid()){
								fragment.setSrcFragmentMapping(fMap);
							}
						}
					}
				}
			}			
		}
	}*/
	
	public void SaveMappingForSys(int srcVersion,int destVersion){
		GroupMapping gMap=null;
		for (CloneGroupMapping cgMap : this.CGMapList){
			gMap = new GroupMapping();
			gMap.setSrcVersionID(srcVersion);
			gMap.setDestVersionID(destVersion);
			gMap.setSrcCGID(Integer.parseInt(cgMap.srcCGInfo.id));
			gMap.setDestCGID(Integer.parseInt(cgMap.destCGInfo.id));
			gMap.setSrcCGSize(cgMap.srcCGInfo.size);
			gMap.setDestCGSize(cgMap.destCGInfo.size);
			
			List<FragmentMapping> fMapList = new ArrayList<FragmentMapping>();
			FragmentMapping fMap=null;
			for(int k=0;k<cgMap.CFMapList.size();k++){
				fMap = new FragmentMapping();
				fMap.setSrcCFid(Integer.parseInt(((CloneFragmentMapping)cgMap.CFMapList.get(k)).SrcCFID));
				fMap.setDestCFid(Integer.parseInt(((CloneFragmentMapping)cgMap.CFMapList.get(k)).DestCFID));
				fMap.setCRDMatchLevel((((CloneFragmentMapping)cgMap.CFMapList.get(k)).CrdMatchLevel).toString());
				fMap.setTextSim(((CloneFragmentMapping)cgMap.CFMapList.get(k)).textSim);
				
				fMapList.add(fMap);
			}
			gMap.setFragMapList(fMapList);
			
			EvolutionPattern evoPattern = new EvolutionPattern();
			evoPattern.setSTATIC(cgMap.EvoPattern.isSTATIC());
			evoPattern.setSAME(cgMap.EvoPattern.isSAME());
			evoPattern.setADD(cgMap.EvoPattern.isADD());
			evoPattern.setSUBSTRACT(cgMap.EvoPattern.isSUBSTRACT());
			evoPattern.setCONSISTENTCHANGE(cgMap.EvoPattern.isCONSISTENTCHANGE());
			evoPattern.setINCONSISTENTCHANGE(cgMap.EvoPattern.isINCONSISTENTCHANGE());
			evoPattern.setSPLIT(cgMap.EvoPattern.isSPLIT());
			if(cgMap.EvoPattern.getMapGroupIDs() != null){
				evoPattern.setMapGroupIDs(String.valueOf(cgMap.EvoPattern.getMapGroupIDs()));
			}
			gMap.setEvolutionPattern(evoPattern);
			
			VariationInformation.mappingInfo.add(gMap);
			
			for(CloneGroup group : VariationInformation.cloneGroup){
				if(group.getVersionID() == srcVersion && group.getCGID() == Integer.parseInt(cgMap.srcCGInfo.id)){
					group.setDestGroupMapping(gMap);
					for(CloneFragment fragment : group.getClonefragment()){
						for(FragmentMapping fraMap : gMap.getFragMapList()){
							if(fragment.getCFID() == fraMap.getSrcCFid()){
								fragment.setDestFragmentMapping(fraMap);
							}
						}
					}
				}
				else if(group.getVersionID() == destVersion && group.getCGID() == Integer.parseInt(cgMap.destCGInfo.id)){
					group.setSrcGroupMapping(gMap);
					for(CloneFragment fragment : group.getClonefragment()){
						for(FragmentMapping fraMap : gMap.getFragMapList()){
							if(fragment.getCFID() == fraMap.getDestCFid()){
								fragment.setSrcFragmentMapping(fraMap);
							}
						}
					}
				}
			}
		}
		
		if(this.UnMappedSrcCGList != null){
			for(int k=0;k<this.UnMappedSrcCGList.size();k++){
				gMap = new GroupMapping();
				gMap.setSrcVersionID(srcVersion);
				gMap.setSrcCGID(Integer.parseInt(((CGInfo)this.UnMappedSrcCGList.get(k)).id));
				gMap.setSrcCGSize(((CGInfo)this.UnMappedSrcCGList.get(k)).size);
				
				VariationInformation.unMappedSrcInfo.add(gMap); 
				//System.out.println("unMappedSrcCGVersion " + srcVersion + " CGID " + gMap.getSrcCGID());
			}
		}

		
		if(this.UnMappedDestCGList != null){
			for(int k=0;k<this.UnMappedDestCGList.size();k++){
				gMap = new GroupMapping();
				gMap.setDestVersionID(destVersion);
				gMap.setDestCGID(Integer.parseInt(((CGInfo)this.UnMappedDestCGList.get(k)).id));
				gMap.setDestCGSize(((CGInfo)this.UnMappedDestCGList.get(k)).size);
				
				VariationInformation.unMappedDestInfo.add(gMap);
				//System.out.println("unMappeddestCGVersion" + gMap.getDestVersionID() + " CGID " + gMap.getDestCGID());
			}
		}
				
	}
	
}
