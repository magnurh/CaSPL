package no.uio.ifi.cfmDatasetGenerator;

import java.util.regex.Pattern;

import org.json.simple.JSONObject;

public interface VarModelExtender {
	
	final int RELATION = 1;
	final int ATTRIBUTE = 2;
	final int CTC = 3;
	final int EXTENDEDCONST = 4;
	
	static String[] eqOperators = {"=",">","<","!=",">=","<="};
	static Pattern relationSplit = Pattern.compile(" : ");
	static Pattern featPattern = Pattern.compile("\\'F\\d+\\'");
	static Pattern mandPattern = Pattern.compile("^\\'F\\d+\\';$");
	static Pattern optPattern = Pattern.compile("^\\[\\'F\\d+\\'\\];$");
	static Pattern altPattern = Pattern.compile("^\\[1,1\\]( \\'F\\d+\\')+;$");
	static Pattern orPattern = Pattern.compile("^\\[1,([2-9]|[1-9]\\d+)\\]( \\'F\\d+\\')+;$");
	
	public int getNumberOfAttributes();
	
	public int getHighestID();
	
	public int getContextSize();
	
	public String getOriginPath();
	
	public JSONObject generateAFMwithContext(String FilePath);

}
