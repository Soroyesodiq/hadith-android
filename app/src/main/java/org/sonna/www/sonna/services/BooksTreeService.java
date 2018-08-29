package org.sonna.www.sonna.services;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

public class BooksTreeService {

	private SQLiteDatabase db;
	private SQLiteInstaller sqLiteInstaller;
    private HashMap<String, Integer> bookSize = new HashMap<>(); 

	public BooksTreeService(Context context) {
		sqLiteInstaller = new SQLiteInstaller(context);
	}

	public void open() throws SQLException {
		sqLiteInstaller.openDataBase();
		sqLiteInstaller.close();
		db = sqLiteInstaller.getReadableDatabase();
        
        //select count(*) from pages where book_code="g2b01";
        //select count(*) from pages where parent_id="NO_PARENT";
        //12 books 
        //Hardcoded here to enhance androips app performance
        bookSize.put("g2b1", 11862);   //
        bookSize.put("g2b2", 9162);    //
        bookSize.put("g2b3", 6712);    //6693
        bookSize.put("g2b4", 8366);    //8345
        bookSize.put("g2b5", 7203);    //7189
        bookSize.put("g2b6", 6059);    //6046
        bookSize.put("g2b7", 2625);    //2619
        bookSize.put("g2b8", 29759);   //29695
        bookSize.put("g2b9", 4962);    //4937
        bookSize.put("g2b10", 5202);    //5192
        bookSize.put("g2b11", 1544);    //1541
        bookSize.put("g2b12", 26668);   //26607
	}

	public void close() {
		sqLiteInstaller.close();
	}

	private ArrayList<BooksTreeNode> executeQuery(String sql, String args[]) {
		Cursor cursor = db.rawQuery(sql, args);
		ArrayList<BooksTreeNode> out = new ArrayList<>();
		while (cursor != null && cursor.moveToNext()) {
			out.add(getBooksTreeNodeObject(cursor));
		}
		if(cursor != null) {
		    cursor.close();
        }
		return out;
	}

    @NonNull
    private BooksTreeNode getBooksTreeNodeObject(@NonNull Cursor cursor) {
        return new BooksTreeNode(cursor.getString(0), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4));
    }

    public ArrayList<BooksTreeNode> findNode(String book_code, String page_id) {
		String sql = "SELECT * FROM pages where pages MATCH ?";
		String params = new Formatter().format("book_code:%s page_id:%s", book_code, page_id).toString();
		String args[] = new String[]{params};
		return executeQuery(sql, args);
	}

	public ArrayList<BooksTreeNode> findKidNodes(String book_code, String page_id) {
		String args[];
		String sql;
		if ("".equals(page_id)) {
			sql = "SELECT * FROM pages where parent_id MATCH 'NO_PARENT'";
			args = null;
		} else {
			sql = "SELECT * FROM pages where pages MATCH ?";
			String param = new Formatter().format("book_code:%s parent_id:%s", book_code, page_id).toString();
			args = new String[]{param};
		}

		return executeQuery(sql, args);
	}

	public boolean isLeafNode(String book_code, String page_id) {

	    if(book_code.isEmpty()) return false;

	    String sql = "SELECT * FROM pages where pages MATCH ?";
		String param = new Formatter().format("book_code:%s parent_id:%s", book_code, page_id).toString();
		String[] args = new String[]{param};
		Cursor cursor = db.rawQuery(sql, args);
		boolean existKids = (cursor != null && cursor.moveToNext());
		if(cursor!= null) {
			cursor.close();
		}
		return ( ! existKids );
	}

	public ArrayList<BooksTreeNode> search(String terms, int pageLength, int pageNo) {
		String sql = "SELECT * FROM pages where pages MATCH ? order by book_code,page_id LIMIT ? OFFSET ? ";
		String args[] = {terms, String.valueOf(pageLength), String.valueOf((pageNo - 1) * pageLength)};
		Cursor cursor = db.rawQuery(sql, args);
		ArrayList<BooksTreeNode> out = new ArrayList<>();
		while (cursor != null && cursor.moveToNext()) {
			out.add(getBooksTreeNodeObject(cursor));
		}
		if(cursor != null) {
			cursor.close();
		}
		return out;
	}

	public int getSearchHitsTotalCount(String book_code, String queryString) {
		String ftsQuery;
		String sql;
		String params[];
		if (book_code.isEmpty()) { //empty
			ftsQuery = queryString;
			sql = "SELECT count(*) AS total_count FROM pages WHERE page_fts MATCH ?";
		} else {
			ftsQuery = new Formatter().format("book_code:%s %s", book_code, queryString).toString();
			sql = "SELECT count(*) AS total_count FROM pages WHERE page_fts MATCH ?";
		}
		params = new String[]{ftsQuery};
		Cursor cursor = db.rawQuery(sql, params);
		int count;
		if (cursor != null && cursor.moveToNext()) {
			String countString = cursor.getString(0);
			count = Integer.parseInt(countString);
		} else {
			count = 0;
		}
		if(cursor!= null) {
			cursor.close();
		}
		return count;
	}

    public String getNextHadithId(String curBookCode, String curPageId) {
        int nextId = Integer.parseInt(curPageId);
        if(nextId + 1 < bookSize.get(curBookCode)) nextId++;
        ArrayList<BooksTreeNode> nodes = new ArrayList<>();
        do {
            nodes = findNode(curBookCode, String.valueOf(nextId));
            if(nodes.size() > 0) return String.valueOf(nextId);
            nextId++;
        } while (nodes.size() == 0 && nextId < bookSize.get(curBookCode));
        return curPageId;
    }

   public String getPreviousHadithId(String curBookCode, String curPageId) {
        int previousId = Integer.parseInt(curPageId);
        if(previousId - 1 > -1) previousId--;
        ArrayList<BooksTreeNode> nodes = new ArrayList<>();
        do {
            nodes = findNode(curBookCode, String.valueOf(previousId));
            if(nodes.size() > 0) return String.valueOf(previousId);
            previousId--;
        } while (nodes.size() == 0 && previousId > 0);
        return curBookCode;
    }

}
