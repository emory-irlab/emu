package edu.emu;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import edu.emu.DataTypes.*;

public class LibRawLogParser extends LogParser {
	
    public LibRawLogParser(String prefix) {
        this.prefix = prefix;

    }

    public static void main(String[] args) throws Exception {
		if (args.length < 2) {
            System.out.println("usage LibRawLogParser <access_log> <table_prefix>");
            System.exit(-1);
        }
        DBUtil.prefix = args[1];
		LibRawLogParser lrlp = new LibRawLogParser(args[1]);
		lrlp.parse(args[0], Version.v_07);
	}

    
    public void initTables(String table_name, String create_sql) {
        DBUtil db = new DBUtil();

        // pages table
        try {
            db.executeQuerySQL("DROP TABLE " + table_name);
        }
        catch (Exception e) {
        }
        db.commit();
        db.execute(create_sql);
        db.commit();
    }
	
	public void parse(String path, Version v) throws Exception {

        String sql_events  = "CREATE TABLE "+prefix+"_emu_events	(event_id serial PRIMARY KEY NOT NULL , event_sha1 character(40), " +
                "\"time\" timestamp without time zone,	ev character varying(20), page_id integer," +
                "url character varying, ref character varying, ip character varying(20), user_id integer," +
                "content_id integer, content_sha1 character varying(40), cx integer, cy integer, x integer," +
                "y integer, scrlx integer, scrly integer, iw integer, ih integer, ow integer, oh integer," +
                "scrlw integer, scrlh integer, targ_id text, dom_path character varying, is_doc_area boolean," +
                "duration integer, select_text character varying, button character(1), is_ref_serp text); ";
        String sql_pages = " CREATE TABLE "+prefix+"_emu_pages ( page_id serial PRIMARY KEY NOT NULL, page_sha1 character(40), \"time\" timestamp without time zone," +
                " log_time character varying(40),wid character varying(20) NOT NULL,tab character varying(20) NOT NULL," +
                "\"type\" character(1), url text,ref text,ip character varying(20),user_id integer,content_id integer, " +
                "content_fn character varying(100), is_search boolean, engine character varying(20)," +
                "vertical character varying(20), is_ref_from_serp boolean, serp_ref text, has_lp boolean," +
                " mouse_ev_cnt integer, scroll_ev_cnt integer, click_verified boolean, mouse_over_cnt integer," +
                "  kp_ev_cnt integer, click_ev_cnt integer, query text);";

        initTables(prefix + "_emu_pages", sql_pages);
        initTables(prefix + "_emu_events", sql_events);

        parseRawLog(path, 0, v);

        String sql = "SELECT distinct ip FROM  "+prefix+"_emu_pages";
		DBUtil db = new DBUtil();
		ResultSet rs = db.executeQuerySQL(sql);

        sql = "SELECT distinct ip FROM "+prefix+"_emu_pages";
        DBUtil db2 = new DBUtil();
        ResultSet rs1 = db.executeQuerySQL(sql);
        while (rs1.next()) {
        String ip = rs1.getString("ip");
            assignPageIds(db2, ip);
            db2.commit();
        }

		// insertPageContent(pageContentDir, null);
		// updatePageWithContentID(null);
		db.rundown();
	}
    /*
	public static void updatePageWithContentID(Integer userID)
			throws SQLException {
		DBUtil db1 = new DBUtil();
		String sql = "select content_sha1, page_id from  "+prefix+"_emu_events where (ev='PageShow' OR ev ='TabSelect') AND user_id='"
				+ userID + "';";
		if (userID == null)
			sql = "select content_sha1, page_id from emu_events_"+table_suffix+" where ev='PageShow' OR ev ='TabSelect' ;";
		ResultSet rs1 = db1.executeQuerySQL(sql);
		while (rs1.next()) {
			String content_sha1 = rs1.getString("content_sha1");
			if (content_sha1 != null) {
				Integer page_id = rs1.getInt("page_id");
				ResultSet rs2 = db1
						.executeQuerySQL("select content_id from  emu_contents_"+table_suffix+" where content_sha1 = '"
								+ content_sha1 + "'");
				rs2.next();
				Integer content_id = rs2.getInt("content_id");
				String updateSql = "UPDATE  emu_pages_"+table_suffix+" SET content_id = '"
						+ content_id + "' WHERE page_id = " + page_id;
				db1.executeUpdateSQL(updateSql);
			}
		}
		rs1.close();
		db1.rundown();
	}
    */
    /*
	public static void insertPageContent(String pageContentDir, Integer userID)
			throws Exception {
		DBUtil db1 = new DBUtil();
		String sql = "SELECT content_sha1 FROM  emu_events_"+table_suffix+" WHERE (ev='PageShow' OR ev='TabSelect') AND user_id='"
				+ userID + "';";
		if (userID == null) {
			sql = "SELECT content_sha1 FROM  emu_events_"+table_suffix+" WHERE (ev='PageShow' OR ev='TabSelect') AND user_id='"
					+ userID + "';";
		}
		ResultSet rs1 = db1.executeQuerySQL(sql);
		while (rs1.next()) {
			String content_sha1 = rs1.getString("content_sha1");
			if (content_sha1 != null) {
				// only content for SERP or SERPClick is stored
				Long time = null;
				String url = null;
				String contentFile = pageContentDir + content_sha1;
				File f = new File(contentFile);
				if (!f.exists())
					contentFile = contentFile + ".html";
				String data = TextReader.readFile(contentFile);
				String type = null;
				int length = data.length();
				db1.insertContent(content_sha1, time, url, data, type, length);
			}
		}
		rs1.close();
		db1.rundown();
	}
    */

	// next page time can be null, but not the current page time
	boolean eventBeforePage(Timestamp eventTime, Timestamp currPageTime) {
		return eventTime.before(currPageTime);
	}

	boolean eventOnPage(Timestamp eventTime, Timestamp currPageTime,
			Timestamp nextPageTime) {
		return !eventBeforePage(eventTime, currPageTime)
				&& !eventAfterPage(eventTime, nextPageTime);
	}

	boolean eventAfterPage(Timestamp eventTime, Timestamp nextPageTime) {
		if (nextPageTime != null)
			return eventTime.before(nextPageTime);
		else
			return false;
	}
}
