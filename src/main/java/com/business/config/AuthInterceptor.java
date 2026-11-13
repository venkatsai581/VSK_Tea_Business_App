package com.business.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.business.repositories.AdminRepository;
import com.business.repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Protects /admin/** (admins only) and /shop/** (logged-in users only). If the account was deleted while the
 * session was open, the session entry is dropped and the visitor is sent back to the login page.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private UserRepository userRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		boolean adminArea = path.equals("/admin") || path.startsWith("/admin/");
		String key = adminArea ? Auth.ADMIN_ID : Auth.USER_ID;

		HttpSession session = request.getSession(false);
		Object id = session == null ? null : session.getAttribute(key);

		boolean valid = id instanceof Integer
				&& (adminArea ? adminRepository.existsById((Integer) id) : userRepository.existsById((Integer) id));
		if (!valid) {
			if (session != null) {
				session.removeAttribute(key);
			}
			response.sendRedirect(request.getContextPath() + "/login?tab=" + (adminArea ? "admin" : "user")
					+ "&required=1");
			return false;
		}
		return true;
	}
}
