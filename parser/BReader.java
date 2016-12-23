package edu.emu;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class BReader {
	String line = "";
	BufferedReader br = null;

	public BReader(String in) throws Exception {
		if (!in.endsWith(".gz")) {
			br = new BufferedReader(new FileReader(in));
		} else {
			FileInputStream fin = new FileInputStream(in);
			GZIPInputStream gzin = new GZIPInputStream(fin);
			br = new BufferedReader(new InputStreamReader(gzin));
		}
	}

	public BReader(String in, String encoding) throws Exception {
		if (!in.endsWith(".gz")) {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					in), encoding));

		} else {
			FileInputStream fin = new FileInputStream(in);
			GZIPInputStream gzin = new GZIPInputStream(fin);
			br = new BufferedReader(new InputStreamReader(gzin));
		}
	}

	public String getLine() {
		return line;
	}
	
	public String[] getFields(String delimiter) {
		return this.getLine().split(delimiter);
	}
	
	public String[] getFields() {
		return getFields("\t");
	}

	public String readLine() throws Exception {
		line = br.readLine();
		return line;
	}

	public void close() throws Exception {
		br.close();
	}

	public BufferedReader getReader() {
		return br;
	}

	public String readFile() throws Exception {
		StringBuffer sb = new StringBuffer();

		while (readLine() != null) {
			sb.append(getLine() + "\n");
		}

		return sb.toString();
	}
	

}
