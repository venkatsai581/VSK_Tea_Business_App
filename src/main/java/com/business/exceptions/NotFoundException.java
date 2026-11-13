package com.business.exceptions;

/** Thrown when a record does not exist. Rendered as a 404 page. */
public class NotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}
}
