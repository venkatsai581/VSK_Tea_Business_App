package com.business.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** "My profile" form. Password fields are optional; fill both to change the password. */
public class ProfileForm {

	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 50, message = "Name must be 2 to 50 characters")
	private String uname;

	@NotBlank(message = "Mobile number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Enter a 10 digit mobile number")
	private String unumber;

	private String currentPassword;

	@Pattern(regexp = "^$|^.{6,64}$", message = "New password must be 6 to 64 characters")
	private String newPassword;

	public String getUname() { return uname; }
	public void setUname(String uname) { this.uname = uname; }
	public String getUnumber() { return unumber; }
	public void setUnumber(String unumber) { this.unumber = unumber; }
	public String getCurrentPassword() { return currentPassword; }
	public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
	public String getNewPassword() { return newPassword; }
	public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
