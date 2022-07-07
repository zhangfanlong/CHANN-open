package Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import CloneRepresentation.CloneGenealogy;
import CloneRepresentation.CloneGroup;
import CloneRepresentation.ClonesInVersion;
import CloneRepresentation.GroupMapping;
import ExtractFeatures.FeatureVector;


public class VariationInformation {
	
	public static Map<String,List<String>> allVersionJavaFiles;
	public static List<ClonesInVersion> clonesInVersion;
	public static List<CloneGroup> cloneGroup;
	
	public static List<GroupMapping> mappingInfo; 
	public static List<GroupMapping> unMappedSrcInfo;
	public static List<GroupMapping> unMappedDestInfo;
	
	public static List<CloneGenealogy> cloneGenealogy;
	public static List<CloneGenealogy> singleCgGenealogyList;
	
	public static List<FeatureVector> featureVectorList;
	
	public static void init (){	
		VariationInformation.allVersionJavaFiles = new HashMap<String,List<String>>();
		VariationInformation.clonesInVersion = new ArrayList<ClonesInVersion>();
		VariationInformation.cloneGroup = new ArrayList<CloneGroup>();
		VariationInformation.mappingInfo = new ArrayList<GroupMapping>();
		VariationInformation.unMappedDestInfo = new ArrayList<GroupMapping>();
		VariationInformation.unMappedSrcInfo = new ArrayList<GroupMapping>();
		VariationInformation.cloneGenealogy = new ArrayList<CloneGenealogy>();
		VariationInformation.singleCgGenealogyList = new ArrayList<CloneGenealogy>(); 
		//VariationInformation.featureVectorList = new ArrayList<FeatureVector>();
		
	}
	
}
