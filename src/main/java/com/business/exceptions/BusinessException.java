package com.business.exceptions;

/** A rule was broken (duplicate email, wrong password...). The message is safe to show to the user. */
public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BusinessException(String message) {
		super(message);
	}
}
