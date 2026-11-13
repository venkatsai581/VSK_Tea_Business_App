package com.business.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.business.entities.Product;
import com.business.services.ProductServices;

@Controller
public class HomeController {

	@Autowired
	private ProductServices productServices;

	@GetMapping("/")
	public String root() {
		return "redirect:/home";
	}

	@GetMapping("/home")
	public String home(Model model) {
		List<Product> all = productServices.getAllProducts();
		model.addAttribute("featured", all.subList(0, Math.min(3, all.size())));
		return "Home";
	}

	@GetMapping("/products")
	public String products(Model model) {
		model.addAttribute("products", productServices.getAllProducts());
		return "Products";
	}

	@GetMapping("/location")
	public String location() {
		return "Locate_us";
	}

	@GetMapping("/about")
	public String about() {
		return "About";
	}
}
