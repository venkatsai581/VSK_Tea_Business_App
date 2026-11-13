package com.business.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/** Blocks an email for a few minutes after repeated wrong passwords (simple brute-force protection). */
@Component
public class LoginAttempts {

	private static final int MAX_FAILURES = 5;
	private static final long LOCK_MILLIS = 5 * 60 * 1000L;

	private static class Entry {
		int failures;
		long lockedUntil;
	}

	private final Map<String, Entry> entries = new ConcurrentHashMap<>();

	public boolean isLocked(String scope, String email) {
		Entry e = entries.get(key(scope, email));
		if (e == null) {
			return false;
		}
		if (e.lockedUntil > System.currentTimeMillis()) {
			return true;
		}
		if (e.lockedUntil != 0) { // lock expired
			entries.remove(key(scope, email));
		}
		return false;
	}

	public void failed(String scope, String email) {
		Entry e = entries.computeIfAbsent(key(scope, email), k -> new Entry());
		synchronized (e) {
			e.failures++;
			if (e.failures >= MAX_FAILURES) {
				e.lockedUntil = System.currentTimeMillis() + LOCK_MILLIS;
				e.failures = 0;
			}
		}
	}

	public void succeeded(String scope, String email) {
		entries.remove(key(scope, email));
	}

	private String key(String scope, String email) {
		return scope + ":" + (email == null ? "" : email.trim().toLowerCase());
	}
}
