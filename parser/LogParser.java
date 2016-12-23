package edu.emu;

import java.io.*;
import java.security.*;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import edu.emu.DataTypes.*;

public abstract class LogParser {

	public static Version _version = Version.v_07;
	public String prefix = "";
	
	public static enum TaskType {
		nav, info, any
	}

	/**
	 * get index of the content
	 * 
	 * @return
	 */
	public static String getLogContent(String line) {
		String[] ss = line.split("\\s+");
		for (String s : ss) {
			if (s.contains("v=EMU.")) {
				return s;
			}
		}
		return null;
	}

	public void setTargetVersion(Version v) {
		_version = v;
	}

	public Version getVersion(HashMap<String, String> keyVals) {
		String v = keyVals.get("v");
		if (v.equals("EMU.0.7")) {
			return Version.v_07;
		} else if (v.equals("EMU.0.7.c")) {
			return Version.v_07c;
		} else
			return null;
	}

	BufferedReader br = null;

	public static int getQueryWordLength(String query) {
		return query.split("\\s+|[+]").length;
	}

	public static int getQueryCharLength(String query) {
		String[] ss = query.split("\\s+|[+]");
		int length = 0;
		for (String s : ss) {
			length += s.length();
		}
		return length;
	}

	public static enum UrlType {
		Serp, Homepage, NonSerp
	}

	/**
	 * only deal with Google related Urls for now
	 * 
	 * @param url
	 *            decoded url
	 * @return
	 */
	public static UrlType getUrlType(String url) {
		if (isSerp(url)) {
			return UrlType.Serp;
		} else if (isHomepage(url)) {
			return UrlType.Homepage;
		} else {
			return UrlType.NonSerp;
		}
	}

	public static boolean isHomepage(String url) {
		return url.equals("http://www.google.com/");
	}

	public static boolean isSerp(String url) {
		return url.startsWith("http://www.google.com/search");
	}

	public abstract void parse(String path, Version v)
			throws Exception;

	public static HashMap<String, String> getKeyValPairs(String line)
			throws Exception {
		HashMap<String, String> keyVals = new HashMap<String, String>();
		String ip = line.split("\\s")[0];
		keyVals.put("ip", ip);
		String content = getLogContent(line);
		// System.out.println(line);
		content = content.substring(1, content.length());

		String ss[] = content.split("&");
		for (String s : ss) {
			String[] keyVal = s.split("=");
			if (keyVal.length == 2)
				keyVals.put(keyVal[0].toLowerCase(), keyVal[1]);
			else if (!keyVal[0].equals("targ_id"))
				System.err.println("[keyVal.length=" + keyVal.length + "]:" + s
						+ ",ev=" + keyVals.get("ev") + ",url="
						+ keyVals.get("url"));
		}
		return keyVals;
	}

	public boolean pageAction(ActionType actionType) {
		return ActionType.Load.equals(actionType)
				|| ActionType.LocationChange1.equals(actionType);
	}

	public boolean filterLine(String str)
	{
		if (str.contains("www.google.com/search"))
		{
			/*if (str.contains("ev=LocationChange")||str.contains("ev=Click") || str.contains("ev=Load")
					|| str.contains("ev=UnLoad"))
				return true;*/
            return  true;
		}		
		return false;
	}
	
	public void parseRawLog(String rawLogFile, Integer userID, Version v)
			throws Exception {
		this.setTargetVersion(v);
		BufferedReader br = null;

		if (!rawLogFile.endsWith(".gz")) {
			br = new BufferedReader(new FileReader(rawLogFile));
		} else {
			FileInputStream fin = new FileInputStream(rawLogFile);
			GZIPInputStream gzin = new GZIPInputStream(fin);
			br = new BufferedReader(new InputStreamReader(gzin));
		}

		edu.emu.DBUtil db1 = new edu.emu.DBUtil();
		String pageSHA1 = null;
		// db1.executeUpdateSQL("delete from emu_pages;");
		// db1.executeUpdateSQL("delete from emu_events;");
		String line = null;
        int line_cnt = 0;
        long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			if (line.contains("GET") && line.contains("v=EMU")) {
				// System.out.println(line); // null???
				if (line.contains("76.14.23.241	[21/Dec/2010:23:00:54 -0500]")) {
					System.out.println(line);
				}

				if (!filterLine(line))
					continue;
				
				HashMap<String, String> keyVals = getKeyValPairs(line);
				Version version = getVersion(keyVals);
				if (version == v) {
					String ev = keyVals.get("ev");
					String time = keyVals.get("time");
					String wid = keyVals.get("wid");
					String tab = keyVals.get("tab");
					String type = keyVals.get("type");
					String url = keyVals.get("url");
					String ref = keyVals.get("ref");
					String ip = keyVals.get("ip");
					String content_sha1 = keyVals.get("content_id");
					String cx = keyVals.get("cx");
					String cy = keyVals.get("cy");
					String x = keyVals.get("x");
					String y = keyVals.get("y");
					String scrlX = keyVals.get("scrlx");
					String scrlY = keyVals.get("scrly");
					String iw = keyVals.get("iw");
					String ih = keyVals.get("ih");
					String ow = keyVals.get("ow");
					String oh = keyVals.get("oh");
					String scrlW = keyVals.get("scrlw");
					String scrlH = keyVals.get("scrlh");
					String targ_id = keyVals.get("targ_id");
					String dom_path = keyVals.get("dom_path");
					String is_doc_area = keyVals.get("is_doc_area");
					String duration = keyVals.get("duration");
					String select_text = keyVals.get("select_text");
					String button = keyVals.get("btn");
					// System.out.println("time:\t" + time);
					// System.out.println("wid:\t" + wid);
					// System.out.println("tab:\t" + tab);
					// System.out.println("type:\t" + type);
					// System.out.println("url:\t" + url);
					// System.out.println("ref:\t" + ref);

					ActionType actionType = ActionType.valueOf(ev);
					// if (url.contains("http%3A//www.google.com/search"))
					{
						// only insert events and pages for SERPs
						if (pageAction(actionType)) {
							pageSHA1 = Utils.SHA1(tab + time + ip);
							// System.out.println("ev:\t" + ev);
							// System.out.println("url is:" + url);
							// System.out.println(line);
							String ud = URLDecoder.decode(url, "UTF-8");
							byte[] bytesOfMessage = ud.getBytes("UTF-8");
							MessageDigest md = MessageDigest.getInstance("MD5");
							byte[] thedigest = md.digest(bytesOfMessage);
							StringBuffer hexString = new StringBuffer();
							for (int i = 0; i < thedigest.length; i++) {
								String hex = Integer
										.toHexString(0xFF & thedigest[i]);
								if (hex.length() == 1)
									hexString.append('0');
								hexString.append(hex);
							}

							Calendar c = Calendar.getInstance();
							c.setTimeInMillis(Long.valueOf(time));

							String content_fn = String.format(
									"%4d_%02d_%02d_%02d_",
									c.get(Calendar.YEAR),
									c.get(Calendar.MONTH) + 1,
									c.get(Calendar.DAY_OF_MONTH),
									c.get(Calendar.HOUR_OF_DAY));

							content_fn += hexString.toString() + ".html";
							// System.out.println(content_fn);
							db1.insertPage(pageSHA1, Long.valueOf(time), wid,
									tab, type, url, ref, ip, userID,
									Utils.isSERP(url), null, null, content_fn);

						} else if (ActionType.MouseMove.equals(actionType)
								|| ActionType.Scroll.equals(actionType)
								|| ActionType.KeyPress.equals(actionType)) {
							String xCxDiff = keyVals.get("xcxdiff");
							String yCyDiff = keyVals.get("ycydiff");
							String buff = keyVals.get("buff");
							if (buff == null)
								System.err.println("[buff == null]:" + line);
							else {
								String[] buffEvents = buff.split("[|]");

								for (String e : buffEvents) {
									String[] ss = e.split(",");
									time = ss[0];
									String eventSHA1 = Utils.SHA1(tab + time
                                            + ip + ev);
									if (ActionType.MouseMove.equals(actionType)) {
										cx = ss[1];
										cy = ss[2];
										x = ""
												+ (Integer.valueOf(cx) + Integer
														.valueOf(xCxDiff));
										y = ""
												+ (Integer.valueOf(cy) + Integer
														.valueOf(yCyDiff));
										// System.out.println("xCxDiff, yCyDiff: "
										// + xCxDiff + "," + yCyDiff);
										// System.out.println("cx, cy: " + cx +
										// ","
										// +
										// cy);
										// System.out.println("x, y: " + x + ","
										// +
										// y);
									} else if (ActionType.Scroll
											.equals(actionType)) {
										scrlX = ss[1];
										scrlY = ss[2];
									} else if (ActionType.KeyPress
											.equals(actionType)) {
										// add char, might need to update the
										// table
										// design
									}
									if (time.isEmpty()) {
										// maybe some bug in the logging
										System.err
												.println("empty buff: no time");
									} else {
										db1.insertEvent(eventSHA1,
												Utils.getLong(time), ev, url,
												ref, ip, userID, content_sha1,
												Utils.getInt(cx),
												Utils.getInt(cy),
												Utils.getInt(x),
												Utils.getInt(y),
												Utils.getInt(scrlX),
												Utils.getInt(scrlY),
												Utils.getInt(iw),
												Utils.getInt(ih),
												Utils.getInt(ow),
												Utils.getInt(oh),
												Utils.getInt(scrlW),
												Utils.getInt(scrlH), targ_id,
												dom_path,
												Utils.getBoolean(is_doc_area),
												Utils.getInt(duration),
												select_text, button);
									}

								}
							}
							// insert the content of buffs, no need to insert
							// the
							// logging info
							continue;
						}
						String eventSHA1 = Utils.SHA1(tab + time + ip + ev);
						db1.insertEvent(eventSHA1, Utils.getLong(time), ev,
								url, ref, ip, userID, content_sha1,
								Utils.getInt(cx), Utils.getInt(cy),
								Utils.getInt(x), Utils.getInt(y),
								Utils.getInt(scrlX), Utils.getInt(scrlY),
								Utils.getInt(iw), Utils.getInt(ih),
								Utils.getInt(ow), Utils.getInt(oh),
								Utils.getInt(scrlW), Utils.getInt(scrlH),
								targ_id, dom_path,
								Utils.getBoolean(is_doc_area),
								Utils.getInt(duration), select_text, button);
					}

				}
			}
            if (++line_cnt % 1000 == 0) {
                System.out.println(line_cnt + "\t" + (System.currentTimeMillis() - start)*1e-3);
                db1.commit();
            }
		}
		br.close();
        db1.commit();
		db1.rundown();
	}

	public void stats(String dir, String fn) throws Exception {

	}

	/**
	 * assign page ids to events
	 */
	public void assignPageIds(edu.emu.DBUtil db1, String selIp) throws Exception {
		System.out.println("assigning page ids for: " + selIp);
		String sql = "SELECT * FROM  "+prefix+"_emu_events ORDER BY ip, time;";
		System.out.println(prefix);
        if (selIp != null) {
			sql = "SELECT * FROM  "+prefix+"_emu_events  WHERE ip = '" + selIp
					+ "' AND time > '2011-03-19 19:56:50.008'"
					+ " ORDER BY ip, time;";
		}
		ResultSet events = db1.executeQuerySQL(sql);
		sql = "SELECT * FROM  "+prefix+"_emu_pages ORDER BY ip, time;";
		if (selIp != null) {
			sql = "SELECT * FROM "+prefix+"_emu_pages WHERE ip = '" + selIp
					+ "' AND time > '2011-03-19 19:56:50.008'"
					+ " ORDER BY ip, time;";
		}
		ResultSet pages = db1.executeQuerySQL(sql);
		String prevIp = null;
		Integer pageId = null;

		while (events.next()) {
			Long time = events.getTimestamp("time").getTime();
			String ip = events.getString("ip");
			String ev = events.getString("ev");
			ActionType actionType = ActionType.valueOf(ev);

			if (!ip.equals(prevIp)) {
				pageId = null;
			}
			if (pageAction(actionType)) {
				// this condition is correct when
				pages.next();
				pageId = pages.getInt("page_id");
				Long pageTime = pages.getTimestamp("time").getTime();
				if (!time.equals(pageTime)) {
					System.err.println("(!time.equals(pageTime)");
					break;
				}
				// System.out.println(ev + "\t" + pageId);

			} else {
				// System.out.println(ev + "\t" + pageId);
			}
			// udpate page id for the current event record
			if (pageId != null) {
				events.updateInt("page_id", pageId);
				events.updateRow();
			}
			prevIp = ip;
		}
		events.close();
	}

	public void assignPageId(Integer userID) throws Exception {
		edu.emu.DBUtil db1 = new edu.emu.DBUtil();
		String sql1 = "SELECT * FROM  "+prefix+"_emu_pages WHERE user_id='" + userID
				+ "' ORDER BY time ;";
		String sql2 = "SELECT * FROM  "+prefix+"_emu_events WHERE user_id='" + userID
				+ "' ORDER BY time ;";
		if (userID == null) {
			sql1 = "SELECT * FROM  "+prefix+"_emu_pages ORDER BY user_id, time;";
			sql2 = "SELECT * FROM  "+prefix+"_emu_events ORDER BY user_id, time;";
		}
		ResultSet rs1 = db1.executeQuerySQL(sql1);
		ResultSet rs2 = db1.executeQuerySQL(sql2);
		// dwell time could be computed here too
		Integer currUserID = null;
		Integer currPageID = null;
		Timestamp currPageTime = null;
		Integer nextUserID = null;
		Integer nextPageID = null;
		Timestamp nextPageTime = null;
		while (rs2.next()) {
			int eventID = rs2.getInt("event_id");
			userID = rs2.getInt("user_id");
			Timestamp eventTime = rs2.getTimestamp("time");
			if (currUserID == null) {
				if (rs1.next()) {
					currUserID = rs1.getInt("user_id");
					currPageID = rs1.getInt("page_id");
					currPageTime = rs1.getTimestamp("time");
					if (rs1.next()) {
						nextUserID = rs1.getInt("user_id");
						nextPageID = rs1.getInt("page_id");
						nextPageTime = rs1.getTimestamp("time");
					} else {
						nextUserID = null;
						nextPageID = null;
						nextPageTime = null;
					}
				} else {
					currUserID = null;
					currPageID = null;
					currPageTime = null;
				}
			} else if (!currUserID.equals(userID)
					|| (nextPageTime != null && !nextPageTime.after(eventTime))) {
				currUserID = nextUserID;
				currPageID = nextPageID;
				currPageTime = nextPageTime;
				if (rs1.next()) {
					nextUserID = rs1.getInt("user_id");
					nextPageID = rs1.getInt("page_id");
					nextPageTime = rs1.getTimestamp("time");
				} else {
					nextUserID = null;
					nextPageID = null;
					nextPageTime = null;
				}
			}
			if (currUserID != null) {
				if (currUserID.equals(userID)
						&& !currPageTime.after(eventTime)
						&& (nextPageTime == null || nextPageTime
								.after(eventTime))) {
					// udpate while reading/or save in an temporary file before
					String updateSql = "UPDATE "+prefix+"_emu_events SET page_id = "
							+ currPageID + "WHERE event_id = " + eventID;
					db1.executeUpdateSQL(updateSql);
				} else {
				}
			}
		}
		rs1.close();
		rs2.close();
		db1.rundown();
	}

}
