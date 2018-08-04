package org.sonna.www.sonna;

public class BooksTreeNode {

	private String page_id;		//matches sqlite field name
	private String parent_id;	//matches sqlite field name
	private String book_code;	//matches sqlite field name
	private String title;
	private String page;

	public String getPage_id() {
		return page_id;
	}

	public void setPage_id(String page_id) {
		this.page_id = page_id;
	}

	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public String getBook_code() {
		return book_code;
	}

	public void setBook_code(String book_code) {
		this.book_code = book_code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}
	//Do not retrieve page_fts, as it it no-vowel text used for search only.
}
