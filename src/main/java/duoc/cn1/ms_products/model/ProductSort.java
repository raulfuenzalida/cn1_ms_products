package duoc.cn1.ms_products.model;

/**
 * Criterios de ordenamiento del catálogo público.
 */
public enum ProductSort {
	NAME_ASC("name"),
	NAME_DESC("name"),
	PRICE_ASC("finalPrice"),
	PRICE_DESC("finalPrice");

	private final String fieldName;

	ProductSort(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}
}
