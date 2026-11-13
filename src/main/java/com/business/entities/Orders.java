package com.business.entities;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Orders {

	public static final String PLACED = "PLACED";
	public static final String DELIVERED = "DELIVERED";
	public static final String CANCELLED = "CANCELLED";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int oId;
	private String oName;
	private double oPrice;
	private int oQuantity;
	private Date orderDate;
	private double totalAmmout;
	/** PLACED, DELIVERED or CANCELLED. Rows created before this column existed read as PLACED. */
	private String status = PLACED;

	@ManyToOne
	@JoinColumn(name = "user_u_id")
	private User user;

	public int getoId() { return oId; }
	public void setoId(int oId) { this.oId = oId; }
	public String getoName() { return oName; }
	public void setoName(String oName) { this.oName = oName; }
	public double getoPrice() { return oPrice; }
	public void setoPrice(double oPrice) { this.oPrice = oPrice; }
	public int getoQuantity() { return oQuantity; }
	public void setoQuantity(int oQuantity) { this.oQuantity = oQuantity; }
	public Date getOrderDate() { return orderDate; }
	public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
	public double getTotalAmmout() { return totalAmmout; }
	public void setTotalAmmout(double totalAmmout) { this.totalAmmout = totalAmmout; }
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }

	public String getStatus() { return status == null || status.isBlank() ? PLACED : status; }
	public void setStatus(String status) { this.status = status; }

	@Override
	public String toString() {
		return "Orders [oId=" + oId + ", oName=" + oName + ", oQuantity=" + oQuantity + ", status=" + status + "]";
	}
}
