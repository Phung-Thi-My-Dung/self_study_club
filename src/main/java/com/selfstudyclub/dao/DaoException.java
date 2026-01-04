package com.selfstudyclub.dao;

/** Wrap SQL errors for upper layers. */
public class DaoException extends RuntimeException {
    public DaoException(String message, Throwable cause) { super(message, cause); }
}
