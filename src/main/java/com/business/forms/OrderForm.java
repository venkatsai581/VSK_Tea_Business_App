package com.business.forms;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderForm {

	@NotNull(message = "Choose a product")
	private Integer productId;

	@NotNull(message = "Enter a quantity")
	@Min(value = 1, message = "Minimum quantity is 1 kg")
	@Max(value = 1000, message = "Maximum quantity is 1000 kg per order")
	private Integer quantity;

	public Integer getProductId() { return productId; }
	public void setProductId(Integer productId) { this.productId = productId; }
	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
