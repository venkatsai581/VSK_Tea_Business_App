package com.business.forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Used for "add admin" and "edit admin". On edit an empty password keeps the old one. */
public class AdminForm {

	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 50, message = "Name must be 2 to 50 characters")
	private String adminName;

	@NotBlank(message = "Email is required")
	@Email(message = "Enter a valid email address")
	@Size(max = 100, message = "Email is too long")
	private String adminEmail;

	@NotBlank(message = "Mobile number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Enter a 10 digit mobile number")
	private String adminNumber;

	@Pattern(regexp = "^$|^.{6,64}$", message = "Password must be 6 to 64 characters")
	private String adminPassword;

	public String getAdminName() { return adminName; }
	public void setAdminName(String adminName) { this.adminName = adminName; }
	public String getAdminEmail() { return adminEmail; }
	public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
	public String getAdminNumber() { return adminNumber; }
	public void setAdminNumber(String adminNumber) { this.adminNumber = adminNumber; }
	public String getAdminPassword() { return adminPassword; }
	public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
}
