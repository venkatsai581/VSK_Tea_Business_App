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

import com.business.entities.Product;
import com.business.exceptions.BusinessException;
import com.business.forms.ProductForm;
import com.business.services.ProductServices;

import jakarta.validation.Valid;

/** Admin-side product management (/admin/products/**). */
@Controller
public class ProductController {

	@Autowired
	private ProductServices productServices;

	@GetMapping("/admin/products/new")
	public String newProduct(Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new ProductForm());
		}
		model.addAttribute("editing", false);
		return "Product_Form";
	}

	@PostMapping("/admin/products")
	public String createProduct(@Valid @ModelAttribute("form") ProductForm form, BindingResult result, Model model,
			RedirectAttributes ra) {
		model.addAttribute("editing", false);
		if (result.hasErrors()) {
			return "Product_Form";
		}
		try {
			Product created = productServices.addProduct(form);
			Flash.success(ra, "Product \"" + created.getPname() + "\" added.");
			return "redirect:/admin/services#products";
		} catch (BusinessException e) {
			result.rejectValue("pname", "duplicate", e.getMessage());
			return "Product_Form";
		}
	}

	@GetMapping("/admin/products/{id}/edit")
	public String editProduct(@PathVariable("id") int id, Model model) {
		Product p = productServices.getProduct(id);
		ProductForm form = new ProductForm();
		form.setPname(p.getPname());
		form.setPprice(p.getPprice());
		form.setPdescription(p.getPdescription());
		form.setPimage(p.getPimage());
		model.addAttribute("form", form);
		model.addAttribute("editing", true);
		model.addAttribute("targetId", id);
		return "Product_Form";
	}

	@PostMapping("/admin/products/{id}")
	public String updateProduct(@PathVariable("id") int id, @Valid @ModelAttribute("form") ProductForm form,
			BindingResult result, Model model, RedirectAttributes ra) {
		model.addAttribute("editing", true);
		model.addAttribute("targetId", id);
		if (result.hasErrors()) {
			return "Product_Form";
		}
		try {
			productServices.updateProduct(id, form);
			Flash.success(ra, "Product updated.");
			return "redirect:/admin/services#products";
		} catch (BusinessException e) {
			result.rejectValue("pname", "duplicate", e.getMessage());
			return "Product_Form";
		}
	}

	@PostMapping("/admin/products/{id}/delete")
	public String deleteProduct(@PathVariable("id") int id, RedirectAttributes ra) {
		productServices.deleteProduct(id);
		Flash.success(ra, "Product deleted.");
		return "redirect:/admin/services#products";
	}
}
