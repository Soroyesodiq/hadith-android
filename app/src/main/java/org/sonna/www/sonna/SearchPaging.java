package org.sonna.www.sonna;

import java.util.Formatter;

class SearchPaging {
    int currentSearchPagesCount;
    final int pageLength = 50;

    void init(int totalHitsCount) {
        currentSearchPagesCount = (int) Math.ceil((double) totalHitsCount / (double) pageLength);
    }

    int getPageLength() {
        return pageLength;
    }

    int getNextSearchPageNumber(int currentSearchPageNumber) {
        int newSearchPageNumber = currentSearchPageNumber + 1;
        if (newSearchPageNumber > currentSearchPagesCount) {
            newSearchPageNumber--;
        }
        return newSearchPageNumber;
    }

    int getPreviousPageNumber(int currentSearchPageNumber) {
        int newPageNumber = currentSearchPageNumber - 1;
        if (newPageNumber < 1) {
            newPageNumber = 1;
        }
        return newPageNumber;
    }

    String getPagingString(int currentSearchPageNumber) {
        return new Formatter().format(" ( %d / %d ) ", currentSearchPageNumber, currentSearchPagesCount).toString();
    }
}
