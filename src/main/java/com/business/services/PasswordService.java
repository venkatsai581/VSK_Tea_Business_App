package com.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {

	@Autowired
	private PasswordEncoder encoder;

	public String hash(String raw) {
		return encoder.encode(raw);
	}

	/** Works with BCrypt hashes and with older plain-text passwords. */
	public boolean matches(String raw, String stored) {
		if (raw == null || stored == null) {
			return false;
		}
		if (isHashed(stored)) {
			return encoder.matches(raw, stored);
		}
		return stored.equals(raw);
	}

	public boolean isHashed(String stored) {
		return stored != null && stored.startsWith("$2");
	}
}
