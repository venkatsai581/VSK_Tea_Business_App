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

import com.business.config.Auth;
import com.business.entities.Orders;
import com.business.entities.User;
import com.business.exceptions.BusinessException;
import com.business.exceptions.NotFoundException;
import com.business.forms.OrderForm;
import com.business.forms.ProfileForm;
import com.business.services.OrderServices;
import com.business.services.ProductServices;
import com.business.services.UserServices;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/** Customer area (/shop/**): browse and order products, order history, profile. */
@Controller
public class ShopController {

	@Autowired
	private UserServices userServices;
	@Autowired
	private ProductServices productServices;
	@Autowired
	private OrderServices orderServices;

	private User current(HttpSession session) {
		return userServices.getUser((Integer) session.getAttribute(Auth.USER_ID));
	}

	@GetMapping("/shop")
	public String shop(Model model) {
		model.addAttribute("products", productServices.getAllProducts());
		return "Shop";
	}

	@PostMapping("/shop/order")
	public String order(@Valid @ModelAttribute("orderForm") OrderForm form, BindingResult result, HttpSession session,
			RedirectAttributes ra) {
		if (result.hasErrors()) {
			Flash.error(ra, result.getAllErrors().get(0).getDefaultMessage());
			return "redirect:/shop";
		}
		try {
			Orders order = orderServices.placeOrder(current(session), form.getProductId(), form.getQuantity());
			return "redirect:/shop/orders/" + order.getoId() + "/confirmation";
		} catch (BusinessException e) {
			Flash.error(ra, e.getMessage());
			return "redirect:/shop";
		}
	}

	@GetMapping("/shop/orders/{id}/confirmation")
	public String confirmation(@PathVariable("id") int id, HttpSession session, Model model) {
		model.addAttribute("order", ownOrder(id, session));
		return "Order_success";
	}

	@GetMapping("/shop/orders")
	public String orders(HttpSession session, Model model) {
		model.addAttribute("orders", orderServices.getOrdersForUser(current(session)));
		return "My_Orders";
	}

	@PostMapping("/shop/orders/{id}/cancel")
	public String cancel(@PathVariable("id") int id, HttpSession session, RedirectAttributes ra) {
		try {
			orderServices.cancelOrder(current(session), id);
			Flash.success(ra, "Order #" + id + " cancelled.");
		} catch (BusinessException e) {
			Flash.error(ra, e.getMessage());
		}
		return "redirect:/shop/orders";
	}

	// ---------- profile ----------

	@GetMapping("/shop/profile")
	public String profile(HttpSession session, Model model) {
		User user = current(session);
		ProfileForm form = new ProfileForm();
		form.setUname(user.getUname());
		form.setUnumber(user.getUnumber() == null ? "" : String.valueOf(user.getUnumber()));
		model.addAttribute("form", form);
		model.addAttribute("email", user.getUemail());
		return "Profile";
	}

	@PostMapping("/shop/profile")
	public String saveProfile(@Valid @ModelAttribute("form") ProfileForm form, BindingResult result,
			HttpSession session, Model model, RedirectAttributes ra) {
		User user = current(session);
		model.addAttribute("email", user.getUemail());
		boolean wantsNewPassword = form.getNewPassword() != null && !form.getNewPassword().isBlank();
		if (wantsNewPassword && (form.getCurrentPassword() == null || form.getCurrentPassword().isBlank())) {
			result.rejectValue("currentPassword", "required", "Enter your current password to set a new one");
		}
		if (result.hasErrors()) {
			clearPasswords(form);
			return "Profile";
		}
		try {
			User saved = userServices.updateProfile(user.getU_id(), form);
			session.setAttribute(Auth.USER_NAME, saved.getUname());
			Flash.success(ra, "Profile updated.");
			return "redirect:/shop/profile";
		} catch (BusinessException e) {
			clearPasswords(form);
			result.rejectValue("currentPassword", "wrong", e.getMessage());
			return "Profile";
		}
	}

	private void clearPasswords(ProfileForm form) {
		form.setCurrentPassword(null);
		form.setNewPassword(null);
	}

	private Orders ownOrder(int id, HttpSession session) {
		Orders order = orderServices.getOrder(id);
		User user = current(session);
		if (order.getUser() == null || order.getUser().getU_id() != user.getU_id()) {
			throw new NotFoundException("Order #" + id + " was not found.");
		}
		return order;
	}
}
