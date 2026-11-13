package com.business.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Product {

	private static final String[] DEFAULT_IMAGES = { "/Images/final.jpg", "/Images/final2.jpg", "/Images/final3.jpg",
			"/Images/TEA.jpg", "/Images/TEA2.jpg" };

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int pid;
	private String pname;
	private double pprice;
	@Column(length = 1000)
	private String pdescription;
	/** Optional image URL (http(s) or a path such as /Images/x.jpg). */
	@Column(length = 500)
	private String pimage;

	public int getPid() { return pid; }
	public void setPid(int pid) { this.pid = pid; }
	public String getPname() { return pname; }
	public void setPname(String pname) { this.pname = pname; }
	public double getPprice() { return pprice; }
	public void setPprice(double pprice) { this.pprice = pprice; }
	public String getPdescription() { return pdescription; }
	public void setPdescription(String pdescription) { this.pdescription = pdescription; }
	public String getPimage() { return pimage; }
	public void setPimage(String pimage) { this.pimage = pimage; }

	/** The product image, or one of the bundled images when none was set. */
	@Transient
	public String getDisplayImage() {
		if (pimage != null && !pimage.isBlank()) {
			return pimage;
		}
		return DEFAULT_IMAGES[Math.abs(pid) % DEFAULT_IMAGES.length];
	}

	@Override
	public String toString() {
		return "Product [pid=" + pid + ", pname=" + pname + ", pprice=" + pprice + "]";
	}
}
