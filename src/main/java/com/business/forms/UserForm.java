package com.business.forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Used for registration, "add user" and "edit user". On edit an empty password keeps the old one. */
public class UserForm {

	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 50, message = "Name must be 2 to 50 characters")
	private String uname;

	@NotBlank(message = "Email is required")
	@Email(message = "Enter a valid email address")
	@Size(max = 100, message = "Email is too long")
	private String uemail;

	@NotBlank(message = "Mobile number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Enter a 10 digit mobile number")
	private String unumber;

	@Pattern(regexp = "^$|^.{6,64}$", message = "Password must be 6 to 64 characters")
	private String upassword;

	public String getUname() { return uname; }
	public void setUname(String uname) { this.uname = uname; }
	public String getUemail() { return uemail; }
	public void setUemail(String uemail) { this.uemail = uemail; }
	public String getUnumber() { return unumber; }
	public void setUnumber(String unumber) { this.unumber = unumber; }
	public String getUpassword() { return upassword; }
	public void setUpassword(String upassword) { this.upassword = upassword; }
}
