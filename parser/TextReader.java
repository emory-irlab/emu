package edu.emu;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class TextReader {
	public static String readFile(String _url) throws Exception {
		StringBuffer buf = new StringBuffer();
		String s = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(_url));
			while ((s = reader.readLine()) != null) {
				buf.append(s + "\n");
			}
			reader.close();
			// BReader br = new BReader(_url);
			// while(br.readLine()!=null) {
			// s += br.getLine()+"\n";
			// }
			// br.close();

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}

		return buf.toString();
		// return s;
	}

	public static String[] readFile2lines(String _url) throws Exception {
		return readFile(_url).split("\n");
	}

	public static Vector<String> readFile2lineVec(String _url) throws Exception {
		Vector<String> lines = new Vector<String>();
		for (String line : readFile2lines(_url)) {
			lines.add(line);
		}
		return lines;
	}

}
