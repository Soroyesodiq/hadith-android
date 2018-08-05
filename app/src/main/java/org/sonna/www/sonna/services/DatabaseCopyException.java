package org.sonna.www.sonna.services;

public class DatabaseCopyException extends Throwable {
    public DatabaseCopyException(String message, Throwable exception) {
        super(message, exception);
    }
}
