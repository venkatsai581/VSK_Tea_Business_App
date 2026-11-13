package com.business.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.business.basiclogics.Logic;
import com.business.entities.Orders;
import com.business.entities.Product;
import com.business.entities.User;
import com.business.exceptions.BusinessException;
import com.business.exceptions.NotFoundException;
import com.business.repositories.OrderRepository;
import com.business.repositories.ProductRepository;

@Service
public class OrderServices {

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private ProductRepository productRepository;

	public List<Orders> getOrders() {
		return orderRepository.findAllByOrderByOrderDateDesc();
	}

	public List<Orders> getOrdersForUser(User user) {
		return orderRepository.findByUserOrderByOrderDateDesc(user);
	}

	public Orders getOrder(int id) {
		return orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order #" + id + " was not found."));
	}

	/** The price always comes from the database, never from the browser. */
	@Transactional
	public Orders placeOrder(User user, int productId, int quantity) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new BusinessException("That product is no longer available."));
		Orders order = new Orders();
		order.setoName(product.getPname());
		order.setoPrice(product.getPprice());
		order.setoQuantity(quantity);
		order.setTotalAmmout(Logic.countTotal(product.getPprice(), quantity));
		order.setOrderDate(new Date());
		order.setStatus(Orders.PLACED);
		order.setUser(user);
		return orderRepository.save(order);
	}

	/** A user may only cancel their own order, and only while it is still PLACED. */
	@Transactional
	public Orders cancelOrder(User user, int orderId) {
		Orders order = getOrder(orderId);
		if (order.getUser() == null || order.getUser().getU_id() != user.getU_id()) {
			throw new NotFoundException("Order #" + orderId + " was not found.");
		}
		if (!Orders.PLACED.equals(order.getStatus())) {
			throw new BusinessException("Only orders that are still placed can be cancelled.");
		}
		order.setStatus(Orders.CANCELLED);
		return orderRepository.save(order);
	}

	@Transactional
	public Orders setStatus(int orderId, String status) {
		if (!Orders.PLACED.equals(status) && !Orders.DELIVERED.equals(status) && !Orders.CANCELLED.equals(status)) {
			throw new BusinessException("Unknown order status.");
		}
		Orders order = getOrder(orderId);
		order.setStatus(status);
		return orderRepository.save(order);
	}

	public void deleteOrder(int id) {
		getOrder(id);
		orderRepository.deleteById(id);
	}

	/** Money from orders that were not cancelled. */
	public double revenue(List<Orders> orders) {
		double sum = 0;
		for (Orders o : orders) {
			if (!Orders.CANCELLED.equals(o.getStatus())) {
				sum += o.getTotalAmmout();
			}
		}
		return Logic.countTotal(sum, 1);
	}
}
