package com.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.business.entities.Product;
import com.business.exceptions.BusinessException;
import com.business.exceptions.NotFoundException;
import com.business.forms.ProductForm;
import com.business.repositories.ProductRepository;

@Service
public class ProductServices {

	@Autowired
	private ProductRepository productRepository;

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	public long count() {
		return productRepository.count();
	}

	public Product getProduct(int id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product #" + id + " was not found."));
	}

	public Product addProduct(ProductForm form) {
		String name = form.getPname().trim();
		if (productRepository.findFirstByPnameIgnoreCase(name) != null) {
			throw new BusinessException("A product named \"" + name + "\" already exists.");
		}
		return productRepository.save(apply(new Product(), form));
	}

	public Product updateProduct(int id, ProductForm form) {
		Product product = getProduct(id);
		String name = form.getPname().trim();
		Product other = productRepository.findFirstByPnameIgnoreCase(name);
		if (other != null && other.getPid() != id) {
			throw new BusinessException("Another product is already named \"" + name + "\".");
		}
		return productRepository.save(apply(product, form));
	}

	public void deleteProduct(int id) {
		getProduct(id);
		productRepository.deleteById(id);
	}

	private Product apply(Product product, ProductForm form) {
		product.setPname(form.getPname().trim());
		product.setPprice(form.getPprice());
		product.setPdescription(form.getPdescription() == null ? null : form.getPdescription().trim());
		product.setPimage(form.getPimage() == null || form.getPimage().isBlank() ? null : form.getPimage().trim());
		return product;
	}
}
