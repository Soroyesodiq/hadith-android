package org.sonna.www.sonna;

import java.io.IOException;

class DatabaseCopyException extends Throwable {
    public DatabaseCopyException(String message, Throwable exception) {
        super(message, exception);
    }
}
