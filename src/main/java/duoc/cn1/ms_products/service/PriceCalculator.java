package duoc.cn1.ms_products.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Motor de cálculo de costos y precio final de un producto.
 * Utiliza {@link BigDecimal} y redondea el precio final CLP a 0 decimales con {@link RoundingMode#HALF_UP}.
 */
public final class PriceCalculator {

	private static final BigDecimal GRAMS_PER_KG = new BigDecimal("1000");
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private static final int INTERMEDIATE_SCALE = 10;

	private PriceCalculator() {
	}

	public static PriceBreakdown calculate(
			BigDecimal filamentGrams,
			BigDecimal filamentPricePerKg,
			BigDecimal printingHours,
			BigDecimal printerConsumptionKwh,
			BigDecimal electricityPriceKwh,
			BigDecimal profitPercentage) {

		BigDecimal materialCost = filamentGrams
			.divide(GRAMS_PER_KG, INTERMEDIATE_SCALE, RoundingMode.HALF_UP)
			.multiply(filamentPricePerKg);

		BigDecimal electricityCost = printingHours
			.multiply(printerConsumptionKwh)
			.multiply(electricityPriceKwh);

		BigDecimal baseCost = materialCost.add(electricityCost);

		BigDecimal profitAmount = baseCost
			.multiply(profitPercentage)
			.divide(ONE_HUNDRED, INTERMEDIATE_SCALE, RoundingMode.HALF_UP);

		BigDecimal finalPrice = baseCost.add(profitAmount).setScale(0, RoundingMode.HALF_UP);

		return new PriceBreakdown(materialCost, electricityCost, baseCost, profitAmount, finalPrice);
	}

	public record PriceBreakdown(
			BigDecimal materialCost,
			BigDecimal electricityCost,
			BigDecimal baseCost,
			BigDecimal profitAmount,
			BigDecimal finalPrice) {
	}
}
