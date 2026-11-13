package com.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ModelAndView notFound(NotFoundException ex) {
		return page("404", "Not found", ex.getMessage());
	}

	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ModelAndView business(BusinessException ex) {
		return page("400", "Request not allowed", ex.getMessage());
	}

	private ModelAndView page(String code, String title, String message) {
		ModelAndView mv = new ModelAndView("error");
		mv.addObject("errorCode", code);
		mv.addObject("errorTitle", title);
		mv.addObject("errorMessage", message);
		return mv;
	}
}
