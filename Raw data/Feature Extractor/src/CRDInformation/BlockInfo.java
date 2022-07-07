package CRDInformation;


public class BlockInfo {
	public  String bType;
	public  String anchor;

	public BlockInfo(){
		bType = null;
		anchor = null;
	}
	public  boolean equals(BlockInfo  bInfo){
		if(bInfo == null) return false;
		if(!this.bType.equals(bInfo.bType))	return false;
		if(this.anchor == null || bInfo.anchor == null) return false;
		if(!this.anchor.equals(bInfo.anchor))	return false;
		return true;
	}
}
