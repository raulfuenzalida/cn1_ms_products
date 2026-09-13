package duoc.cn1.ms_products.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {

	@Test
	void calculateMaterialCost_100gAt25000PerKg() {
		PriceCalculator.PriceBreakdown result = PriceCalculator.calculate(
			new BigDecimal("100"),
			new BigDecimal("25000"),
			new BigDecimal("1"),
			new BigDecimal("0.5"),
			new BigDecimal("150"),
			new BigDecimal("20")
		);

		assertEquals(0, new BigDecimal("2500").compareTo(result.materialCost()));
	}

	@Test
	void calculateElectricityCost_2Hours() {
		PriceCalculator.PriceBreakdown result = PriceCalculator.calculate(
			new BigDecimal("100"),
			new BigDecimal("25000"),
			new BigDecimal("2"),
			new BigDecimal("0.5"),
			new BigDecimal("150"),
			new BigDecimal("20")
		);

		assertEquals(0, new BigDecimal("150").compareTo(result.electricityCost()));
	}

	@Test
	void calculateBaseCost() {
		PriceCalculator.PriceBreakdown result = PriceCalculator.calculate(
			new BigDecimal("100"),
			new BigDecimal("25000"),
			new BigDecimal("2"),
			new BigDecimal("0.5"),
			new BigDecimal("150"),
			new BigDecimal("20")
		);

		assertEquals(0, new BigDecimal("2650").compareTo(result.baseCost()));
	}

	@Test
	void calculateProfitAmount_20Percent() {
		PriceCalculator.PriceBreakdown result = PriceCalculator.calculate(
			new BigDecimal("100"),
			new BigDecimal("25000"),
			new BigDecimal("2"),
			new BigDecimal("0.5"),
			new BigDecimal("150"),
			new BigDecimal("20")
		);

		assertEquals(0, new BigDecimal("530").compareTo(result.profitAmount()));
	}

	@Test
	void calculateFinalPrice_HalfUpRounding() {
		PriceCalculator.PriceBreakdown result = PriceCalculator.calculate(
			new BigDecimal("100"),
			new BigDecimal("25000"),
			new BigDecimal("2"),
			new BigDecimal("0.5"),
			new BigDecimal("150"),
			new BigDecimal("20")
		);

		assertEquals(new BigDecimal("3180"), result.finalPrice());
		assertEquals(0, result.finalPrice().scale());
	}

	@Test
	void calculateFinalPrice_WithHalfUp_2650() {
		PriceCalculator.PriceBreakdown result = PriceCalculator.calculate(
			new BigDecimal("100"),
			new BigDecimal("25000"),
			new BigDecimal("2"),
			new BigDecimal("0.5"),
			new BigDecimal("150"),
			new BigDecimal("0")
		);

		assertEquals(new BigDecimal("2650"), result.finalPrice());
	}

	@Test
	void calculateFinalPrice_WithHalfUp_26505() {
		PriceCalculator.PriceBreakdown result = PriceCalculator.calculate(
			new BigDecimal("100"),
			new BigDecimal("25000"),
			new BigDecimal("2"),
			new BigDecimal("0.5"),
			new BigDecimal("150"),
			new BigDecimal("0.19")
		);

		assertEquals(new BigDecimal("2655"), result.finalPrice());
	}
}
