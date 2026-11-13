package com.business.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.business.entities.User;
import com.business.exceptions.BusinessException;
import com.business.exceptions.NotFoundException;
import com.business.forms.ProfileForm;
import com.business.forms.UserForm;
import com.business.repositories.UserRepository;

@Service
public class UserServices {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordService passwords;

	public List<User> getAllUser() {
		return userRepository.findAll();
	}

	public long count() {
		return userRepository.count();
	}

	public User getUser(int id) {
		return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User #" + id + " was not found."));
	}

	public Optional<User> authenticate(String email, String password) {
		User user = userRepository.findFirstByUemailIgnoreCase(email.trim());
		if (user == null || !passwords.matches(password, user.getUpassword())) {
			return Optional.empty();
		}
		if (!passwords.isHashed(user.getUpassword())) {
			user.setUpassword(passwords.hash(password));
			userRepository.save(user);
		}
		return Optional.of(user);
	}

	/** Used by both self-registration and the admin "Add user" form. */
	public User addUser(UserForm form) {
		if (form.getUpassword() == null || form.getUpassword().isBlank()) {
			throw new BusinessException("Password is required.");
		}
		String email = form.getUemail().trim();
		if (userRepository.findFirstByUemailIgnoreCase(email) != null) {
			throw new BusinessException("This email is already registered.");
		}
		User user = new User();
		user.setUname(form.getUname().trim());
		user.setUemail(email);
		user.setUnumber(Long.parseLong(form.getUnumber()));
		user.setUpassword(passwords.hash(form.getUpassword()));
		return userRepository.save(user);
	}

	public User updateUser(int id, UserForm form) {
		User user = getUser(id);
		String email = form.getUemail().trim();
		User other = userRepository.findFirstByUemailIgnoreCase(email);
		if (other != null && other.getU_id() != id) {
			throw new BusinessException("Another user already uses this email.");
		}
		user.setUname(form.getUname().trim());
		user.setUemail(email);
		user.setUnumber(Long.parseLong(form.getUnumber()));
		if (form.getUpassword() != null && !form.getUpassword().isBlank()) {
			user.setUpassword(passwords.hash(form.getUpassword()));
		}
		return userRepository.save(user);
	}

	/** "My profile": name, number and (optionally) password, which needs the current password. */
	public User updateProfile(int id, ProfileForm form) {
		User user = getUser(id);
		user.setUname(form.getUname().trim());
		user.setUnumber(Long.parseLong(form.getUnumber()));
		String newPassword = form.getNewPassword();
		if (newPassword != null && !newPassword.isBlank()) {
			if (!passwords.matches(form.getCurrentPassword(), user.getUpassword())) {
				throw new BusinessException("Current password is incorrect.");
			}
			user.setUpassword(passwords.hash(newPassword));
		}
		return userRepository.save(user);
	}

	/** Deleting a user also deletes that user's orders. */
	public void deleteUser(int id) {
		getUser(id);
		userRepository.deleteById(id);
	}
}
