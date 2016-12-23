package edu.emu;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.csvreader.*;

public class Utils {

	// public static String word_separators =
	// "[\\s`~!@#\\$%\\^&\\*\\(\\)\\-=_\\+,\\./;\\[\\]\\<>\\?:\"{}]+";// removed
	public static String word_separators = "\\W";// removed

	public static Vector<String> readVector(String path) throws Exception {
		Vector<String> vec = new Vector<String>();
		BReader br = new BReader(path);
		while (br.readLine() != null) {
			String line = br.getLine().trim();
			if (!line.startsWith("#")) {
				vec.add(line);
			}
		}
		br.close();
		return vec;
	}

	public static Vector<String> readFieldIntoVector(String fn, int k,
			String delimiter) throws Exception {
		String s = edu.emu.TextReader.readFile(fn);
		String[] ss = s.split("\n");
		Vector<String> rc = new Vector<String>();
		for (int i = 0; i < ss.length; i++) {
			String[] sss = ss[i].split(delimiter);
			rc.add(sss[k]);
		}
		return rc;
	}

	public static Vector<String> reverseVector(Vector<String> rc) {
		Collections.reverse(rc);
		return rc;
	}

	public static void pause(long time) {
		pause(time, time);
	}

	public static void pause(long low, long high) {// mill-second
		try {
			// 6 ~ 10 seconds
			long t = (long) (Math.random() * low) + (high - low);
			Thread.sleep(t);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public static void copy(String fn_src, String fn_dst) {
		try {
			FileInputStream fis = new FileInputStream(fn_src);
			FileOutputStream fos = new FileOutputStream(fn_dst);
			int c = -1;
			while ((c = fis.read()) != -1) {
				fos.write(c);
			}
			fis.close();
			fos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static int str_contains_i(String big, String small) {
		int rc = 0;
		if (big.length() < small.length())
			return 0;
		String[] ss = big.trim().toLowerCase().split(Utils.word_separators);
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].equals(small.toLowerCase().trim()))
				rc++;
		}
		return rc;
	}

	public static int str_contains_c(String big, String small) {
		int rc = 0;
		if (big.length() < small.length())
			return 0;
		String[] ss = big.trim().split(Utils.word_separators);
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].equals(small.trim()))
				rc++;
		}
		return rc;
	}

	public static Vector<String> tokenize_lower(String s) {
		Vector<String> rc = new Vector<String>();
		// System.out.println(s);
		String text = s.replaceAll("\n", " ").replaceAll("<.*?>", "").trim()
				.toLowerCase();
		String[] ss = text.split(Utils.word_separators);
		for (int i = 0; i < ss.length; i++) {
			rc.add(ss[i]);
		}
		return rc;
	}

	public static HashSet<String> stop_words = new HashSet<String>();

	public static boolean inStopWords(String token) throws IOException {
		if (stop_words.isEmpty()) {
			// String s = TextReader
			// .readFile(
			// "J:/research/user_behavior_modeling/mission_commercial_intent/data/stop_words.dat"
			// );
			String ss[] = stop_words_str.split("\\W|\n");
			for (String e : ss) {
				if (!e.trim().isEmpty())
					stop_words.add(e.toLowerCase());
			}
			// TextWriter
			// .appendFile(
			// "J:/research/user_behavior_modeling/mission_commercial_intent/data/"
			// ,
			// "stop_words2.dat", s.replace("\n", "\\n"));
		}
		return stop_words.contains(token.toLowerCase());
	}

	public static HashSet<String> toHashSetS(Vector<String> vec) {
		HashSet<String> hashSet = new HashSet<String>();
		for (String s : vec) {
			hashSet.add(s);
		}
		return hashSet;
	}

	public static HashSet<Double> toHashSetD(Vector<Double> vec) {
		HashSet<Double> hashSet = new HashSet<Double>();
		for (Double s : vec) {
			hashSet.add(s);
		}
		return hashSet;
	}

	public static int _nFields = 7;

	public static void mtFormat(String dir, String ifn, String ofn, int n,
			int hitSize) throws Exception {
		edu.emu.TReader tr = new edu.emu.TReader(dir + ifn);
		tr.readHeader();
		List<CsvWriter> cws = new ArrayList<CsvWriter>();

		for (int i = 0; i < n; i++) {
			int from = (i * hitSize);
			String to = (i + 1) * hitSize + "";
			if (i == n - 1) {
				to = "inf";
			}
			CsvWriter cw = new CsvWriter(dir + ofn + "_" + from + "-" + to
					+ ".csv");
			for (int j = 0; j < (i + 1) * hitSize; j++) {
				for (String name : tr._names) {
					cw.write(name + "_" + j);
				}
			}
			cw.endRecord();
			cws.add(cw);
		}

		List<String> session = new ArrayList<String>();
		while (tr.readLine() != null) {
			String line = tr.getLine();
			if (!line.isEmpty()) {
				session.add(line);
			} else {
				if (session.size() > 0) {
					int index = n - 1;
					for (int i = 0; i < n - 1; i++) {
						if (session.size() == 5) {
							System.out.println(session.size() + "\t" + i);
						}
						int tmp = (session.size() - 1) / ((i + 1) * hitSize);

						if (tmp == 0) {
							index = i;
							break;
						}
					}

					CsvWriter cw = cws.get(index);
					for (int i = 0; i < session.size(); i++) {
						for (String val : session.get(i).split("\t")) {
							cw.write(val);
						}
					}
					int upper = (index + 1) * hitSize;
					for (int i = session.size(); i < upper; i++) {
						for (int j = 0; j < _nFields; j++) {
							cw.write("#");
						}
					}
					cw.endRecord();
					session.clear();
				}
			}
		}

		tr.close();
		for (int i = 0; i < n; i++) {
			cws.get(i).close();
		}
	}

	// public static void countFreq(String dir, String ifn, String ofn) throws
	// Exception {
	// BReader br = new BReader(dir+ifn);
	// BWriter bw = new BWriter(dir+ofn);
	// FrequencyCounter fc = new FrequencyCounter();
	// String prevLine = null;
	// int n = 0;
	// while (br.readLine()!=null) {
	// String line = br.getLine();
	// if (prevLine!=null && !prevLine.equals(line)) {
	// bw.append(fc.get(prevLine)+"\t"+prevLine+"\n");
	// if (n++%100000==0)
	// System.out.println((n-1) +": "+fc.get(prevLine)+"\t"+prevLine);
	// fc.clear();
	// }
	// fc.add(line);
	// prevLine = line;
	// }
	// br.close();
	// bw.close();
	// }

	public static String stop_words_str = "a\nabout\nabove\nacross\nafter\nafterwards\nagain\nagainst\nall\nalmost\nalone\nalong\nalready\nalso\nalthough\nalways\nam\namong\namongst\namoungst\namount\nan\nand\nanother\nany\nanyhow\nanyone\nanything\nanyway\nanywhere\nare\naround\nas\nat\nback\nbe\nbecame\nbecause\nbecome\nbecomes\nbecoming\nbeen\nbefore\nbeforehand\nbehind\nbeing\nbelow\nbeside\nbesides\nbetween\nbeyond\nbill\nboth\nbottom\nbut\nby\ncall\ncan\ncannot\ncant\nco\ncomputer\ncon\ncould\ncouldnt\ncry\nde\ndescribe\ndetail\ndo\ndone\ndown\ndue\nduring\neach\neg\neight\neither\neleven\nelse\nelsewhere\nempty\nenough\netc\neven\never\nevery\neveryone\neverything\neverywhere\nexcept\nfew\nfifteen\nfify\nfill\nfind\nfire\nfirst\nfive\nfor\nformer\nformerly\nforty\nfound\nfour\nfrom\nfront\nfull\nfurther\nget\ngive\ngo\nhad\nhas\nhasnt\nhave\nhe\nhence\nher\nhere\nhereafter\nhereby\nherein\nhereupon\nhers\nherself\nhim\nhimself\nhis\nhow\nhowever\nhundred\ni\nie\nif\nin\ninc\nindeed\ninterest\ninto\nis\nit\nits\nitself\nkeep\nlast\nlatter\nlatterly\nleast\nless\nltd\nmade\nmany\nmay\nme\nmeanwhile\nmight\nmill\nmine\nmore\nmoreover\nmost\nmostly\nmove\nmuch\nmust\nmy\nmyself\nname\nnamely\nneither\nnever\nnevertheless\nnext\nnine\nno\nnobody\nnone\nnoone\nnor\nnot\nnothing\nnow\nnowhere\nof\noff\noften\non\nonce\none\nonly\nonto\nor\nother\nothers\notherwise\nour\nours\nourselves\nout\nover\nown\npart\nper\nperhaps\nplease\nput\nrather\nre\nsame\nsee\nseem\nseemed\nseeming\nseems\nserious\nseveral\nshe\nshould\nshow\nside\nsince\nsincere\nsix\nsixty\nso\nsome\nsomehow\nsomeone\nsomething\nsometime\nsometimes\nsomewhere\nstill\nsuch\nsystem\ntake\nten\nthan\nthat\nthe\ntheir\nthem\nthemselves\nthen\nthence\nthere\nthereafter\nthereby\ntherefore\ntherein\nthereupon\nthese\nthey\nthick\nthin\nthird\nthis\nthose\nthough\nthree\nthrough\nthroughout\nthru\nthus\nto\ntogether\ntoo\ntop\ntoward\ntowards\ntwelve\ntwenty\ntwo\nun\nunder\nuntil\nup\nupon\nus\nvery\nvia\nwas\nwe\nwell\nwere\nwhat\nwhatever\nwhen\nwhence\nwhenever\nwhere\nwhereafter\nwhereas\nwhereby\nwherein\nwhereupon\nwherever\nwhether\nwhich\nwhile\nwhither\nwho\nwhoever\nwhole\nwhom\nwhose\nwhy\nwill\nwith\nwithin\nwithout\nwould\nyet\nyou\nyour\nyours\nyourself\nyourselves\n";

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String SHA1(String text) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash = new byte[40];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	public static Long getLong(String val) {
		if (val != null) {
			return Long.valueOf(val);
		} else {
			return null;
		}
	}

	public static Integer getInt(String val) {
		if (val != null) {
			return Integer.valueOf(val);
		} else {
			return null;
		}
	}

	public static Boolean getBoolean(String val) {
		if (val != null) {
			return Integer.valueOf(val) == 1 ? true : false;
		} else {
			return null;
		}
	}

	public static Boolean isSERP(String url) {
		if (url.startsWith("http"))
			return url.contains("google.com") && url.contains("search")
					&& url.contains("q=");
		else if (url.startsWith("file") && url.contains("task_data_library")) {
			return !url.endsWith("index.html");
		} else {
			return false;
		}
	}

	// given the string, returns the time of date in terms of milliseconds
	public static long getTOD(String strTOD) {
		long TOD = 0;
		String ss[] = strTOD.split(":");
		int hour = Integer.valueOf(ss[0]);
		int min = Integer.valueOf(ss[1]);
		int sec = Integer.valueOf(ss[2]);
		int ms = Integer.valueOf(ss[3]);
		TOD += ((hour * 60 + min) * 60 + sec) * 1000 + ms;
		return TOD;
	}

	public static int countSubStr(final String string, final String substring) {
		int count = 0;
		int idx = 0;

		while ((idx = string.indexOf(substring, idx)) != -1) {
			idx++;
			count++;
		}

		return count;
	}

	public static void main(String[] args) throws Exception {
		// copy(args[0], args[1]);
		// System.out.println(inStopWords("about"));
		String s = "Amazon.com� Official Site &raquo;";
		// System.out.println(s);

		// String dir =
		// "J:/Research/user_behavior_modeling/mission_commercial_intent/data/11242009/localData/"
		// ;
		// String fn = "test.html";
		// TextWriter.writeFile_utf8(dir, fn, s);
		String dir = "J:/courses/CS7461/data/";
		String ifn = "urls.sorted.1000.dat";
		String ofn = "urls.sorted.1000.freq.dat";
		// countFreq(args[0], args[1], args[2], Integer.valueOf(args[3]));
		// filterByFreq(args[0], args[1], args[2], Integer.valueOf(args[3]),
		// Integer.valueOf(args[4]));
		// filterByFreq(dir, "queries.sorted.sample.100.txt",
		// "queries.sorted.sample.100.ge2.txt", 0, 2);
		// System.out.println("".compareTo("abc"));
		// merge(args[0], args[1], args[2], args[3]);
		// genSampleURL(args[0], args[1], args[2], args[3]);
		// merge(dir, "queries.sorted.ge2.qid.10.txt",
		// "clicksLog.sample.10.txt", "out.txt");
		// merge(args[0], args[1], args[2], args[3]);
		// sample(args[0], args[1], args[2], Integer.valueOf(args[3]));
		// if (args.length == 5) {
		// sample(args[0], args[1], args[2], Integer.valueOf(args[3]),
		// Integer.valueOf(args[4]));
		// } else {
		// //sample(dir, "sessions_1of1000.txt", "sessions_100.txt", 0, 100);
		// System.err.println("Usage: java -jar sample.jar dir ifn ofn start end");
		// }

		// mtFormat(dir, "sessions_100.txt", "sessions_100_mt.txt", 3, 5);
		// shrink(args[0], args[1], args[2]);
		// shrink(dir, "in.txt", "out.txt");
	}

}
