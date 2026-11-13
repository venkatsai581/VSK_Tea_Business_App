package com.business.config;

import java.net.URI;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lightweight CSRF protection: a state-changing request (POST) is rejected when the browser says it came from a
 * different site (Origin or Referer host does not match the Host header).
 */
public class SameOriginInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			return true;
		}
		String source = request.getHeader("Origin");
		if (source == null || source.isBlank() || "null".equals(source)) {
			source = request.getHeader("Referer");
		}
		if (source == null || source.isBlank()) {
			return true; // non-browser client or privacy setting; cookies are SameSite=Lax anyway
		}
		String host = request.getHeader("Host");
		try {
			URI uri = URI.create(source);
			String sourceHost = uri.getPort() == -1 ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
			if (host != null && host.equalsIgnoreCase(sourceHost)) {
				return true;
			}
			// allow default ports written either way (example.com vs example.com:443)
			if (host != null && uri.getHost() != null && host.equalsIgnoreCase(uri.getHost())) {
				return true;
			}
		} catch (IllegalArgumentException ignored) {
			// fall through to rejection
		}
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-site request blocked");
		return false;
	}
}
