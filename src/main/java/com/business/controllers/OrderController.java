package com.business.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.exceptions.BusinessException;
import com.business.services.OrderServices;

/** Admin-side order management (/admin/orders/**). Customers place orders through ShopController. */
@Controller
public class OrderController {

	@Autowired
	private OrderServices orderServices;

	@PostMapping("/admin/orders/{id}/status")
	public String updateStatus(@PathVariable("id") int id, @RequestParam("status") String status,
			RedirectAttributes ra) {
		try {
			orderServices.setStatus(id, status);
			Flash.success(ra, "Order #" + id + " marked as " + status.toLowerCase() + ".");
		} catch (BusinessException e) {
			Flash.error(ra, e.getMessage());
		}
		return "redirect:/admin/services#orders";
	}

	@PostMapping("/admin/orders/{id}/delete")
	public String deleteOrder(@PathVariable("id") int id, RedirectAttributes ra) {
		orderServices.deleteOrder(id);
		Flash.success(ra, "Order deleted.");
		return "redirect:/admin/services#orders";
	}
}
