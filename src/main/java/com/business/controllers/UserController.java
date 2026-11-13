package com.business.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.entities.User;
import com.business.exceptions.BusinessException;
import com.business.forms.UserForm;
import com.business.services.UserServices;

import jakarta.validation.Valid;

/** Admin-side user management (/admin/users/**). */
@Controller
public class UserController {

	@Autowired
	private UserServices userServices;

	@GetMapping("/admin/users/new")
	public String newUser(Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new UserForm());
		}
		model.addAttribute("editing", false);
		return "User_Form";
	}

	@PostMapping("/admin/users")
	public String createUser(@Valid @ModelAttribute("form") UserForm form, BindingResult result, Model model,
			RedirectAttributes ra) {
		model.addAttribute("editing", false);
		if (form.getUpassword() == null || form.getUpassword().isBlank()) {
			result.rejectValue("upassword", "required", "Password is required");
		}
		if (result.hasErrors()) {
			form.setUpassword(null);
			return "User_Form";
		}
		try {
			User created = userServices.addUser(form);
			Flash.success(ra, "User \"" + created.getUname() + "\" added.");
			return "redirect:/admin/services#users";
		} catch (BusinessException e) {
			form.setUpassword(null);
			result.rejectValue("uemail", "duplicate", e.getMessage());
			return "User_Form";
		}
	}

	@GetMapping("/admin/users/{id}/edit")
	public String editUser(@PathVariable("id") int id, Model model) {
		User user = userServices.getUser(id);
		UserForm form = new UserForm();
		form.setUname(user.getUname());
		form.setUemail(user.getUemail());
		form.setUnumber(user.getUnumber() == null ? "" : String.valueOf(user.getUnumber()));
		model.addAttribute("form", form);
		model.addAttribute("editing", true);
		model.addAttribute("targetId", id);
		return "User_Form";
	}

	@PostMapping("/admin/users/{id}")
	public String updateUser(@PathVariable("id") int id, @Valid @ModelAttribute("form") UserForm form,
			BindingResult result, Model model, RedirectAttributes ra) {
		model.addAttribute("editing", true);
		model.addAttribute("targetId", id);
		if (result.hasErrors()) {
			form.setUpassword(null);
			return "User_Form";
		}
		try {
			userServices.updateUser(id, form);
			Flash.success(ra, "User updated.");
			return "redirect:/admin/services#users";
		} catch (BusinessException e) {
			form.setUpassword(null);
			result.rejectValue("uemail", "duplicate", e.getMessage());
			return "User_Form";
		}
	}

	@PostMapping("/admin/users/{id}/delete")
	public String deleteUser(@PathVariable("id") int id, RedirectAttributes ra) {
		userServices.deleteUser(id);
		Flash.success(ra, "User and their orders deleted.");
		return "redirect:/admin/services#users";
	}
}
