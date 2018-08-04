package org.sonna.www.sonna.services;

import java.io.IOException;

public class DatabaseCopyException extends Throwable {
    public DatabaseCopyException(String message, Throwable exception) {
        super(message, exception);
    }
}
