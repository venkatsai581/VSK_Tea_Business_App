package com.business.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.config.Auth;
import com.business.entities.Admin;
import com.business.entities.Orders;
import com.business.exceptions.BusinessException;
import com.business.forms.AdminForm;
import com.business.services.AdminServices;
import com.business.services.OrderServices;
import com.business.services.ProductServices;
import com.business.services.UserServices;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/** Admin dashboard and admin-account management. Everything under /admin is protected by AuthInterceptor. */
@Controller
public class AdminController {

	@Autowired
	private UserServices userServices;
	@Autowired
	private AdminServices adminServices;
	@Autowired
	private ProductServices productServices;
	@Autowired
	private OrderServices orderServices;

	@GetMapping("/admin")
	public String adminHome() {
		return "redirect:/admin/services";
	}

	@GetMapping("/admin/services")
	public String dashboard(Model model) {
		List<Orders> orders = orderServices.getOrders();
		long pending = orders.stream().filter(o -> Orders.PLACED.equals(o.getStatus())).count();
		model.addAttribute("users", userServices.getAllUser());
		model.addAttribute("admins", adminServices.getAll());
		model.addAttribute("products", productServices.getAllProducts());
		model.addAttribute("orders", orders);
		model.addAttribute("revenue", orderServices.revenue(orders));
		model.addAttribute("pending", pending);
		return "Admin_Page";
	}

	// ---------- admins ----------

	@GetMapping("/admin/admins/new")
	public String newAdmin(Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new AdminForm());
		}
		model.addAttribute("editing", false);
		return "Admin_Form";
	}

	@PostMapping("/admin/admins")
	public String createAdmin(@Valid @ModelAttribute("form") AdminForm form, BindingResult result, Model model,
			RedirectAttributes ra) {
		model.addAttribute("editing", false);
		if (form.getAdminPassword() == null || form.getAdminPassword().isBlank()) {
			result.rejectValue("adminPassword", "required", "Password is required");
		}
		if (result.hasErrors()) {
			form.setAdminPassword(null);
			return "Admin_Form";
		}
		try {
			Admin created = adminServices.addAdmin(form);
			Flash.success(ra, "Admin \"" + created.getAdminName() + "\" added.");
			return "redirect:/admin/services#admins";
		} catch (BusinessException e) {
			form.setAdminPassword(null);
			result.rejectValue("adminEmail", "duplicate", e.getMessage());
			return "Admin_Form";
		}
	}

	@GetMapping("/admin/admins/{id}/edit")
	public String editAdmin(@PathVariable("id") int id, Model model) {
		Admin admin = adminServices.getAdmin(id);
		AdminForm form = new AdminForm();
		form.setAdminName(admin.getAdminName());
		form.setAdminEmail(admin.getAdminEmail());
		form.setAdminNumber(admin.getAdminNumber());
		model.addAttribute("form", form);
		model.addAttribute("editing", true);
		model.addAttribute("targetId", id);
		return "Admin_Form";
	}

	@PostMapping("/admin/admins/{id}")
	public String updateAdmin(@PathVariable("id") int id, @Valid @ModelAttribute("form") AdminForm form,
			BindingResult result, HttpSession session, Model model, RedirectAttributes ra) {
		model.addAttribute("editing", true);
		model.addAttribute("targetId", id);
		if (result.hasErrors()) {
			form.setAdminPassword(null);
			return "Admin_Form";
		}
		try {
			Admin saved = adminServices.update(id, form);
			if (saved.getAdminId() == (Integer) session.getAttribute(Auth.ADMIN_ID)) {
				session.setAttribute(Auth.ADMIN_NAME, saved.getAdminName());
			}
			Flash.success(ra, "Admin updated.");
			return "redirect:/admin/services#admins";
		} catch (BusinessException e) {
			form.setAdminPassword(null);
			result.rejectValue("adminEmail", "duplicate", e.getMessage());
			return "Admin_Form";
		}
	}

	@PostMapping("/admin/admins/{id}/delete")
	public String deleteAdmin(@PathVariable("id") int id, HttpSession session, RedirectAttributes ra) {
		try {
			adminServices.delete(id, (Integer) session.getAttribute(Auth.ADMIN_ID));
			Flash.success(ra, "Admin deleted.");
		} catch (BusinessException e) {
			Flash.error(ra, e.getMessage());
		}
		return "redirect:/admin/services#admins";
	}
}
