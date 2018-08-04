package org.sonna.www.sonna;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Stack;

public class MainActivity extends AppCompatActivity
//		implements NavigationView.OnNavigationItemSelectedListener
{

	protected static final String LOG_TAG = "MainActivity";
	BooksTreeService booksService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

//        Right button of dots
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

/////////////////////////////////////////////////////////////
		//install DB

		//FIXME: Use this instead of getting Context because activity is already extends Context
		Context context = getApplicationContext();
		SQLiteInstaller db = new SQLiteInstaller(context);

//		ProgressDialog hourGlassDlg = new ProgressDialog(this);
//		hourGlassDlg.setMessage("برجاء الإنتظار");
//		hourGlassDlg.setIndeterminate(true);
//		hourGlassDlg.setCancelable(false);
//		hourGlassDlg.show();

		try {
			db.install();
		} catch (DatabaseCopyException exception) {
			Log.e(LOG_TAG, "open >>" + exception.toString());
            showErrorDialogue("خطأ في العمل", "يوجد خطأ في تهيئة العمل علي ملف البيانات.", exception);
		}

//		hourGlassDlg.hide();

		//Open DB and display initial view
		booksService = new BooksTreeService(context);
		booksService.open();
		displayKids("", "");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		booksService.close();
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			if (historyStack.size() > 0) {
				displayPreviousContents();
			} else {
				super.onBackPressed();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.nav_home_screen) {
			historyStack.push(curPageId);
			displayKids("", "");
			findViewById(R.id.textViewDisplay).setVisibility(View.GONE);
			findViewById(R.id.listViewTabweeb).setVisibility(View.VISIBLE);
			return true;
		} else if (id == R.id.nav_about_us) {
			showAboutDialogue();
			return true;
		} else if (id == R.id.action_exit) {

			finish();
			//Go phone home
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	void displayPreviousContents() {
		String page_id = historyStack.pop();

		WebView display = (WebView) findViewById(R.id.textViewDisplay);
		ListView tabweeb = (ListView) findViewById(R.id.listViewTabweeb);

		if (booksService.isLeafNode(curBookCode, page_id)) {
			display.setVisibility(View.VISIBLE);
			tabweeb.setVisibility(View.GONE);
			displayContent(curBookCode, page_id, "");
		} else {
			display.setVisibility(View.GONE);
			emptyDisplay(display);
			tabweeb.setVisibility(View.VISIBLE);
			displayKids(curBookCode, page_id);
		}
	}

	private void emptyDisplay(WebView display) {
		display.loadData("", "text/html; charset=UTF-8", null);
	}

	String curBookCode = "", curPageId = "";
	ArrayList<BooksTreeNode> curRecords = new ArrayList<>();
	Stack<String> historyStack = new Stack<>();

	protected void displayContent(String book_code, String page_id, String searchWords) {
		try {
			WebView displayTextView = (WebView) findViewById(R.id.textViewDisplay);
			displayTextView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return handleSwipeLeftAndRight(event);
				}
			});
			ArrayList<BooksTreeNode> records = booksService.findNode(book_code, page_id);
			if (records.size() != 1) {
				emptyDisplay(displayTextView);

			} else {
				BooksTreeNode record = records.get(0);
				String content = record.getPage();
				String htmlContent = TextUtils.decorate(searchWords, record.getTitle(), content);
				displayTextView.loadData(htmlContent, "text/html; charset=UTF-8", null);
				curBookCode = record.getBook_code();
				curPageId = record.getPage_id();
			}
		} catch (Exception exception) {
			Log.e(LOG_TAG, "exception", exception);
		}
	}

	protected void displayKids(String book_code, String page_id) {
		try {
			curBookCode = book_code;
			curPageId = page_id;
			ArrayList<BooksTreeNode> records = booksService.findKidNodes(book_code, page_id);
			final ArrayList<String> list = new ArrayList<>();
			curRecords.clear();
			for (BooksTreeNode record : records) {
				list.add(TextUtils.removeTrailingDot(record.getTitle()));
				curRecords.add(record);
			}
			//populate the list of items into the ListView
			ListView listView = (ListView) findViewById(R.id.listViewTabweeb);
			listView.clearChoices();

			ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1, list);
			listView.setAdapter(adapter);

			// ListView Item Click Listener
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					BooksTreeNode record = curRecords.get(position);
					historyStack.push(curPageId); //is going to change per user click
					WebView display = (WebView) findViewById(R.id.textViewDisplay);
					ListView tabweeb = (ListView) findViewById(R.id.listViewTabweeb);

					if (booksService.isLeafNode(record.getBook_code(), record.getPage_id())) {
						display.setVisibility(View.VISIBLE);
						tabweeb.setVisibility(View.GONE);
						displayContent(record.getBook_code(), record.getPage_id(), "");
					} else {
						display.setVisibility(View.GONE);
						emptyDisplay(display);
						tabweeb.setVisibility(View.VISIBLE);
						displayKids(record.getBook_code(), record.getPage_id());

					}
				}
			});
		} catch (Exception exception) {
			Log.e(LOG_TAG, "exception", exception);
			showErrorDialogue("خطأ", "خطأ في عرض البيانات.", exception);

		}
	}

	void showErrorDialogue(String title, String body, Throwable exp) {
        Log.e(LOG_TAG, "fatal error", exp);
	    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(body);
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		alertDialog.show();
	}

	//Seems search can has its own class
	ArrayList<BooksTreeNode> curSearchHits = new ArrayList<>();

	public void onSearch(View view) {
		searchDatabase(1);
	}

	public void searchDatabase(int pageNumber) {
		currentSearchPageNumber = pageNumber;
		EditText searchEditor = (EditText) findViewById(R.id.search_edit_text);
		final String searchWords = searchEditor.getText().toString();
		if (searchWords.trim().length() == 0) {
			return; //just do nothing
		}
		int totalHitsCount = booksService.getSearchHitsTotalCount("", searchWords);
		String pagingString = getPagingString(totalHitsCount);

        //set text in between next and prev
		TextView paging = (TextView) findViewById(R.id.text_view_paging);
		paging.setText(Html.fromHtml(pagingString));

		ArrayList<BooksTreeNode> hits = booksService.search(searchWords, pageLength, pageNumber);
		curSearchHits.clear();
		final ArrayList<String> list = new ArrayList<>();
		for (BooksTreeNode record : hits) {
			list.add(record.getTitle());
			curSearchHits.add(record);
		}
		ListView listView = (ListView) findViewById(R.id.listView_search_hits);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//				android.R.layout.simple_list_item_1, android.R.id.text1, list);
				R.layout.search_hits_list_view, android.R.id.text1, list);
		listView.setAdapter(adapter);

		// ListView Item Click Listener
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				BooksTreeNode record = curSearchHits.get(position);
				historyStack.push(curPageId); //is going to change per user click
				findViewById(R.id.textViewDisplay).setVisibility(View.VISIBLE);
				findViewById(R.id.listViewTabweeb).setVisibility(View.GONE);

				//searchWords
				displayContent(record.getBook_code(), record.getPage_id(), searchWords);
				DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
				drawer.closeDrawer(GravityCompat.START);
				InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0); //hide keyboard

			}
		});

	}

	// Swipe left and right
	private float x1, x2;
	static final int MIN_DISTANCE = 150;

	public boolean handleSwipeLeftAndRight(MotionEvent event) {
		if (findViewById(R.id.listViewTabweeb).getVisibility() == View.VISIBLE) {
			return super.onTouchEvent(event);
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				x1 = event.getX();
				break;
			case MotionEvent.ACTION_UP:
				x2 = event.getX();
				float deltaX = x2 - x1;
				if (Math.abs(deltaX) > MIN_DISTANCE) {
					if (x2 > x1) { // Left to Right swipe action : NEXT
						displayContent(curBookCode, String.valueOf(Integer.parseInt(curPageId) + 1), "");
					} else {  // Right to left swipe action: PREVIOUS
						if (Integer.parseInt(curPageId) > 1) {
							displayContent(curBookCode, String.valueOf(Integer.parseInt(curPageId) - 1), "");
						}
					}
				}
				break;
		}
		return super.onTouchEvent(event);
	}

	void showAboutDialogue() {
		AlertDialog.Builder aboutAlert = new AlertDialog.Builder(
				MainActivity.this);
		LayoutInflater factory = LayoutInflater.from(MainActivity.this);
		final ImageView view = (ImageView) factory.inflate(R.layout.about_image_view, null);
		aboutAlert.setView(view);
		aboutAlert.setTitle("عن البرنامج");
		aboutAlert.setNeutralButton("إغلاق", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int which) {
				dlg.dismiss();
			}
		});
		aboutAlert.show();
	}


    public void onSearchNextPage(View view) {
        int newPageNumber = getNextSearchPageNumber();
        if(newPageNumber != currentSearchPageNumber) {
            searchDatabase(newPageNumber);
        }
    }

    public void onSearchPreviousPage(View view) {
        int newPageNumber = getPreviousPageNumber();
        if(newPageNumber != currentSearchPageNumber) {
            searchDatabase(newPageNumber);
        }
    }

    ///////////////////////////////////////////////////

    int currentSearchPagesCount;
    int currentSearchPageNumber;
    final int pageLength = 50;

    private int getNextSearchPageNumber() {
        int newSearchPageNumber = currentSearchPageNumber + 1;
        if (newSearchPageNumber > currentSearchPagesCount) {
            newSearchPageNumber--;
        }
        return newSearchPageNumber;
    }

    private int getPreviousPageNumber() {
        int newPageNumber = currentSearchPageNumber - 1;
        if (newPageNumber < 1) {
            newPageNumber = 1;
        }
        return newPageNumber;
    }

    public String getPagingString(int totalHitsCount) {
        //Adjust paging
        currentSearchPagesCount = (int) Math.ceil((double) totalHitsCount / (double) pageLength);
        return new Formatter().format(" ( %d / %d ) ", currentSearchPageNumber, currentSearchPagesCount).toString();
    }

}
