package com.business.forms;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProductForm {

	@NotBlank(message = "Product name is required")
	@Size(max = 80, message = "Name can be at most 80 characters")
	private String pname;

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.01", message = "Price must be greater than 0")
	@DecimalMax(value = "1000000", message = "Price is too large")
	private Double pprice;

	@Size(max = 500, message = "Description can be at most 500 characters")
	private String pdescription;

	@Size(max = 500, message = "Image link is too long")
	@Pattern(regexp = "^$|^(https?://|/).*", message = "Use a full http(s) link or a path starting with /")
	private String pimage;

	public String getPname() { return pname; }
	public void setPname(String pname) { this.pname = pname; }
	public Double getPprice() { return pprice; }
	public void setPprice(Double pprice) { this.pprice = pprice; }
	public String getPdescription() { return pdescription; }
	public void setPdescription(String pdescription) { this.pdescription = pdescription; }
	public String getPimage() { return pimage; }
	public void setPimage(String pimage) { this.pimage = pimage; }
}
