package com.business.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.business.entities.Admin;
import com.business.exceptions.BusinessException;
import com.business.exceptions.NotFoundException;
import com.business.forms.AdminForm;
import com.business.repositories.AdminRepository;

@Service
public class AdminServices {

	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private PasswordService passwords;

	public List<Admin> getAll() {
		return adminRepository.findAll();
	}

	public long count() {
		return adminRepository.count();
	}

	public Admin getAdmin(int id) {
		return adminRepository.findById(id).orElseThrow(() -> new NotFoundException("Admin #" + id + " was not found."));
	}

	/** Returns the admin when the email/password are right (upgrading an old plain-text password to BCrypt). */
	public Optional<Admin> authenticate(String email, String password) {
		Admin admin = adminRepository.findFirstByAdminEmailIgnoreCase(email.trim());
		if (admin == null || !passwords.matches(password, admin.getAdminPassword())) {
			return Optional.empty();
		}
		if (!passwords.isHashed(admin.getAdminPassword())) {
			admin.setAdminPassword(passwords.hash(password));
			adminRepository.save(admin);
		}
		return Optional.of(admin);
	}

	public Admin addAdmin(AdminForm form) {
		if (form.getAdminPassword() == null || form.getAdminPassword().isBlank()) {
			throw new BusinessException("Password is required.");
		}
		String email = form.getAdminEmail().trim();
		if (adminRepository.findFirstByAdminEmailIgnoreCase(email) != null) {
			throw new BusinessException("An admin with this email already exists.");
		}
		Admin admin = new Admin();
		admin.setAdminName(form.getAdminName().trim());
		admin.setAdminEmail(email);
		admin.setAdminNumber(form.getAdminNumber());
		admin.setAdminPassword(passwords.hash(form.getAdminPassword()));
		return adminRepository.save(admin);
	}

	public Admin update(int id, AdminForm form) {
		Admin admin = getAdmin(id);
		String email = form.getAdminEmail().trim();
		Admin other = adminRepository.findFirstByAdminEmailIgnoreCase(email);
		if (other != null && other.getAdminId() != id) {
			throw new BusinessException("Another admin already uses this email.");
		}
		admin.setAdminName(form.getAdminName().trim());
		admin.setAdminEmail(email);
		admin.setAdminNumber(form.getAdminNumber());
		if (form.getAdminPassword() != null && !form.getAdminPassword().isBlank()) {
			admin.setAdminPassword(passwords.hash(form.getAdminPassword()));
		}
		return adminRepository.save(admin);
	}

	public void delete(int id, int currentAdminId) {
		if (id == currentAdminId) {
			throw new BusinessException("You cannot delete the account you are logged in with.");
		}
		if (adminRepository.count() <= 1) {
			throw new BusinessException("The last remaining admin cannot be deleted.");
		}
		getAdmin(id);
		adminRepository.deleteById(id);
	}
}
