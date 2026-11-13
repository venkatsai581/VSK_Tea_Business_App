package com.business.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.config.Auth;
import com.business.config.LoginAttempts;
import com.business.entities.Admin;
import com.business.entities.User;
import com.business.exceptions.BusinessException;
import com.business.forms.LoginForm;
import com.business.forms.UserForm;
import com.business.services.AdminServices;
import com.business.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

	private static final String LOCKED = "Too many wrong attempts. Please wait 5 minutes and try again.";
	private static final String WRONG = "Invalid email or password.";

	@Autowired
	private AdminServices adminServices;
	@Autowired
	private UserServices userServices;
	@Autowired
	private LoginAttempts attempts;

	// ---------- login page ----------

	@GetMapping("/login")
	public String login(@RequestParam(value = "tab", required = false) String tab,
			@RequestParam(value = "required", required = false) String required,
			@RequestParam(value = "loggedout", required = false) String loggedOut, Model model) {
		model.addAttribute("tab", "admin".equals(tab) ? "admin" : "user");
		if (!model.containsAttribute("adminForm")) {
			model.addAttribute("adminForm", new LoginForm());
		}
		if (!model.containsAttribute("userForm")) {
			model.addAttribute("userForm", new LoginForm());
		}
		if (required != null) {
			model.addAttribute("toast", "Please log in to continue.");
			model.addAttribute("toastType", "info");
		} else if (loggedOut != null) {
			model.addAttribute("toast", "You have been logged out.");
			model.addAttribute("toastType", "success");
		}
		return "Login";
	}

	@PostMapping("/login/admin")
	public String adminLogin(@Valid @ModelAttribute("adminForm") LoginForm form, BindingResult result,
			HttpServletRequest request, Model model) {
		model.addAttribute("tab", "admin");
		model.addAttribute("userForm", new LoginForm());
		if (result.hasErrors()) {
			form.setPassword(null);
			return "Login";
		}
		if (attempts.isLocked("admin", form.getEmail())) {
			form.setPassword(null);
			model.addAttribute("adminError", LOCKED);
			return "Login";
		}
		return finishLogin(request, model, form, true);
	}

	@PostMapping("/login/user")
	public String userLogin(@Valid @ModelAttribute("userForm") LoginForm form, BindingResult result,
			HttpServletRequest request, Model model) {
		model.addAttribute("tab", "user");
		model.addAttribute("adminForm", new LoginForm());
		if (result.hasErrors()) {
			form.setPassword(null);
			return "Login";
		}
		if (attempts.isLocked("user", form.getEmail())) {
			form.setPassword(null);
			model.addAttribute("userError", LOCKED);
			return "Login";
		}
		return finishLogin(request, model, form, false);
	}

	/** The form object still has the typed password here; it is cleared before the page is shown again. */
	private String finishLogin(HttpServletRequest request, Model model, LoginForm form, boolean admin) {
		String scope = admin ? "admin" : "user";
		String password = form.getPassword();
		form.setPassword(null);
		if (admin) {
			Optional<Admin> found = adminServices.authenticate(form.getEmail(), password);
			if (found.isEmpty()) {
				attempts.failed(scope, form.getEmail());
				model.addAttribute("adminError", WRONG);
				return "Login";
			}
			attempts.succeeded(scope, form.getEmail());
			HttpSession session = newSession(request);
			session.setAttribute(Auth.ADMIN_ID, found.get().getAdminId());
			session.setAttribute(Auth.ADMIN_NAME, found.get().getAdminName());
			return "redirect:/admin/services";
		}
		Optional<User> found = userServices.authenticate(form.getEmail(), password);
		if (found.isEmpty()) {
			attempts.failed(scope, form.getEmail());
			model.addAttribute("userError", WRONG);
			return "Login";
		}
		attempts.succeeded(scope, form.getEmail());
		HttpSession session = newSession(request);
		session.setAttribute(Auth.USER_ID, found.get().getU_id());
		session.setAttribute(Auth.USER_NAME, found.get().getUname());
		return "redirect:/shop";
	}

	/** Fresh session id on every login (prevents session fixation). */
	private HttpSession newSession(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		request.changeSessionId();
		return session;
	}

	// ---------- registration ----------

	@GetMapping("/register")
	public String registerPage(Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new UserForm());
		}
		return "Register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("form") UserForm form, BindingResult result,
			@RequestParam(value = "confirmPassword", required = false) String confirm, HttpServletRequest request,
			Model model, RedirectAttributes ra) {
		String password = form.getUpassword();
		form.setUpassword(null);
		if (password == null || password.isBlank()) {
			result.rejectValue("upassword", "required", "Password is required");
		} else if (!password.equals(confirm)) {
			model.addAttribute("confirmError", "Passwords do not match");
		}
		if (result.hasErrors() || model.containsAttribute("confirmError")) {
			return "Register";
		}
		form.setUpassword(password);
		try {
			User user = userServices.addUser(form);
			HttpSession session = newSession(request);
			session.setAttribute(Auth.USER_ID, user.getU_id());
			session.setAttribute(Auth.USER_NAME, user.getUname());
			Flash.success(ra, "Welcome, " + user.getUname() + "! Your account is ready.");
			return "redirect:/shop";
		} catch (BusinessException e) {
			form.setUpassword(null);
			result.rejectValue("uemail", "duplicate", e.getMessage());
			return "Register";
		}
	}

	// ---------- logout ----------

	@PostMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login?loggedout=1";
	}
}
