package org.sonna.www.sonna.services;

public class BooksTreeNode {

	private String page_id;		//matches sqlite field name
	private String book_code;	//matches sqlite field name
	private String title;
	private String page;

    BooksTreeNode(String page_id, String book_code, String title, String page) {
        this.page_id = page_id;
        this.book_code = book_code;
        this.title = title;
        this.page = page;
    }

    public String getPage_id() {
		return page_id;
	}

	public String getBook_code() {
		return book_code;
	}

	public String getTitle() {
		return title;
	}

	public String getPage() {
		return page;
	}

	//Do not retrieve page_fts, as it it no-vowel text used for search only.
}
