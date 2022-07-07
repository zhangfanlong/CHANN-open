package CloneRepresentation;


/**
 * @author ZFL
 */

public class CloneFragment {
	
	private int CFID;
	private int CGID;
	private int VersionID;
	private String Path;
	private String FileName;
	private int StartLine;
	private int EndLine;
	
	private CRD crd;
	private FragmentMapping srcFragmentMapping;
	private FragmentMapping destFragmentMapping;
	
	public void setCFID(int cFID) {
		CFID = cFID;
	}
	public int getCFID() {
		return CFID;
	}
	public void setCGID(int cGID) {
		CGID = cGID;
	}
	public int getCGID() {
		return CGID;
	}
	public int getVersionID() {
		return VersionID;
	}
	public void setVersionID(int VersionID) {
		this.VersionID = VersionID;
	}
	public String getPath() {
		return Path;
	}
	public void setPath(String Path) {
		this.Path = Path;
	}
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}
	public int getStartLine() {
		return StartLine;
	}
	public void setStartLine(int start_line) {
		this.StartLine = start_line;
	}
	public int getEndLine() {
		return EndLine;
	}
	public void setEndLine(int end_line) {
		this.EndLine = end_line;
	}
	public CRD getCRD() {
		return crd;
	}
	public void setCRD(CRD crd) {
		this.crd = new CRD();
		this.crd = crd;
	}
	public FragmentMapping getSrcFragmentMapping() {
		return srcFragmentMapping;
	}
	public void setSrcFragmentMapping(FragmentMapping srcFragmentMapping) {
		this.srcFragmentMapping = new FragmentMapping();
		this.srcFragmentMapping = srcFragmentMapping;
	}
	public FragmentMapping getDestFragmentMapping() {
		return destFragmentMapping;
	}
	public void setDestFragmentMapping(FragmentMapping destFragmentMapping) {
		this.destFragmentMapping = new FragmentMapping();
		this.destFragmentMapping = destFragmentMapping;
	}

	

}
