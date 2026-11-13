package com.business.basiclogics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Logic {

	/** price x quantity, rounded to 2 decimal places. */
	public static double countTotal(double price, int quantity) {
		return BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP)
				.doubleValue();
	}
}
