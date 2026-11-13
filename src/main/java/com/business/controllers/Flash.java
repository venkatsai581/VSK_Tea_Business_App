package com.business.controllers;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** One-time messages shown as a pop-up (toast) on the next page. */
final class Flash {

	static void success(RedirectAttributes ra, String message) {
		ra.addFlashAttribute("toast", message);
		ra.addFlashAttribute("toastType", "success");
	}

	static void error(RedirectAttributes ra, String message) {
		ra.addFlashAttribute("toast", message);
		ra.addFlashAttribute("toastType", "error");
	}

	private Flash() {
	}
}
