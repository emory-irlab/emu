package edu.emu;



import java.util.*;

public class TReader extends BReader {

	public TReader(String in) throws Exception {
		super(in);
		// TODO Auto-generated constructor stub
	}
	
	public TReader(String in, String encoding) throws Exception {
		super(in, encoding);
	}
	
	HashMap<String, Integer> _fieldIndex = new HashMap<String, Integer>();
	List<String> _names = new ArrayList<String>();
	List<String> _selNames = new ArrayList<String>();
	
	
	public String getSelVals() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i <_selNames.size(); i++) {
			String name = this._selNames.get(i);			
			sb.append(this.getVal(name));
			if (i < this._selNames.size() - 1) {
				sb.append("\t");
			}
		}
		return sb.toString();
	}
	
	public void getSelectedField(String name) {
		_selNames.add(name);
	}	
	
	public void getSelectedFields(List<String> names) {
		for (String name: names)
			getSelectedField(name);
	}	
	
	public String _selHeader = "";
	public void readSelHeader(String fn) throws Exception {
		BReader br = new BReader(fn);
		System.out.println("Selected HEADER:");		
		_selNames.clear();
		_selHeader = br.readLine();
		String[] ss = _selHeader.split("\t");
		for (int i = 0; i < ss.length; i++) {
			String name = normalizeName(ss[i]);
			_selNames.add(name);
			System.out.println(i+":\t"+name);
		}
		br.close();
	}
	
	public void readSelHeader() throws Exception {
		readSelHeader("sel_fields.txt");
	}
	
	public String normalizeName(String name) {
		return name.trim().toLowerCase();
	}
	
	public void readHeader(String header) {		
		System.out.println("HEADER:");
		_names.clear();
		_fieldIndex.clear();
		String[] ss = header.split("\t");
		for (int i = 0; i < ss.length; i++) {
			String name = normalizeName(ss[i]);
			_names.add(name);
			// will use the first if there's duplicates
			if (!_fieldIndex.containsKey(name))
				_fieldIndex.put(name, i);
			System.out.println(i+":\t"+name);
		}
		
	}
	
	public List<String> getVals() {
		return _vals;
	}
	
	public void readHeader() throws Exception {
		readHeader(readLine());
	}
	
	public String getFieldName(int i) {
		return _names.get(i);
	}
	
	public Integer getFieldIndex(String name) {
		name = normalizeName(name);
		return _fieldIndex.get(name);
	}
	
	public String getVal(String name) {
		name = normalizeName(name);
		if (_vals != null) {
			Integer index = getFieldIndex(name);
			if (index == null) {
				return null;
			}
			return _vals.get(index);
		} else {
			return null;
		}
	}
	
	public Integer getInt(String name) {
		return Integer.valueOf(getVal(name));
	}
	
	public Long getLong(String name) {
		return Long.valueOf(getVal(name));
	}
	
	public Double getDouble(String name) {
		return Double.valueOf(getVal(name));
	}
	
	public int valSize() {
		return _vals.size();
	}
	
	public int size() {
		return _names.size();
	}
	
	public String getVal(int i) {
		if (_vals != null) {
			return _vals.get(i);
		} else {
			return null;
		}
	}
	
	List<String> _vals = new ArrayList<String>();
	public String readLine() throws Exception {
		_vals.clear();
		super.readLine();
		if (line != null) {
			//System.out.println(line);
			String[] vals = line.split("\t");
			for (String val: vals) {
				_vals.add(val);
			}
		}		
		return line;
	}
	
	public boolean next() throws Exception {
		return readLine()!=null;
	}
}
