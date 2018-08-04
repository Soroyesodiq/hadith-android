package org.sonna.www.sonna;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Formatter;

public class BooksTreeService {

	static final String LOG_TAG = "BooksTreeService";

	private SQLiteDatabase mDb;
	private SQLiteInstaller mDbHelper;

	BooksTreeService(Context context) {
		mDbHelper = new SQLiteInstaller(context);
	}

	public BooksTreeService install() throws DatabaseCopyException {
        mDbHelper.install();
		return this;
	}

	public BooksTreeService open() throws SQLException {
		mDbHelper.openDataBase();
		mDbHelper.close();
		mDb = mDbHelper.getReadableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	private ArrayList<BooksTreeNode> selectData(String sql, String args[]) {
		Cursor cursor = mDb.rawQuery(sql, args);
		ArrayList<BooksTreeNode> out = new ArrayList<>();
		while (cursor != null && cursor.moveToNext()) {
			BooksTreeNode record = new BooksTreeNode();
			record.page_id = cursor.getString(0);
			record.parent_id = cursor.getString(1);
			record.book_code = cursor.getString(2);
			record.title = cursor.getString(3);
			record.page = cursor.getString(4);
			out.add(record);
		}
		if(cursor!= null) {
		    cursor.close();
        }
		return out;
	}


	public ArrayList<BooksTreeNode> findNode(String book_code, String page_id) {
		String sql = "SELECT * FROM pages where pages MATCH ?";
		String params = new Formatter().format("book_code:%s page_id:%s", book_code, page_id).toString();
		String args[] = new String[]{params};
		return selectData(sql, args);
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

		return selectData(sql, args);
	}


	public boolean IsLeafNode(String book_code, String page_id) {
		assert "".equals(book_code);
		String sql = "SELECT * FROM pages where pages MATCH ?";
		String param = new Formatter().format("book_code:%s parent_id:%s", book_code, page_id).toString();
		String[] args = new String[]{param};
		Cursor cursor = mDb.rawQuery(sql, args);
		boolean existKids = (cursor != null && cursor.moveToNext());
		if(cursor!= null) {
			cursor.close();
		}
		return ( ! existKids );
	}

	public ArrayList<BooksTreeNode> search(String terms, int pageLength, int pageNo) {
		String sql = "SELECT * FROM pages where pages MATCH ? order by book_code,page_id LIMIT ? OFFSET ? ";
		String args[] = {terms, String.valueOf(pageLength), String.valueOf((pageNo - 1) * pageLength)};
		Cursor cursor = mDb.rawQuery(sql, args);
		ArrayList<BooksTreeNode> out = new ArrayList<>();
		while (cursor != null && cursor.moveToNext()) {
			BooksTreeNode record = new BooksTreeNode();
			record.page_id = cursor.getString(0);
			record.parent_id = cursor.getString(1);
			record.book_code = cursor.getString(2);
			record.title = cursor.getString(3);
			record.page = cursor.getString(4);
			out.add(record);
		}
		if(cursor!= null) {
			cursor.close();
		}
		return out;
	}

	public int getSearchHitsTotalCount(String book_code, String queryString) {
		String ftsQuery;
		String sql;
		String params[];
		if ("".equals(book_code)) { //empty
			ftsQuery = queryString;
			sql = "SELECT count(*) AS total_count FROM pages WHERE page_fts MATCH ?";
		} else {
			ftsQuery = new Formatter().format("book_code:%s %s", book_code, queryString).toString();
			sql = "SELECT count(*) AS total_count FROM pages WHERE page_fts MATCH ?";
		}
		params = new String[]{ftsQuery};
		Cursor cursor = mDb.rawQuery(sql, params);
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

}
