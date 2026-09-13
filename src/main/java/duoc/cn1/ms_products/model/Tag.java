package duoc.cn1.ms_products.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_tag")
	private Long id;

	@Column(name = "name", nullable = false, unique = true, length = 100)
	private String name;

	@ManyToMany(mappedBy = "tags")
	private Set<Product> products = new HashSet<>();

	public Tag() {
	}

	public Tag(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Product> getProducts() {
		return products;
	}

	public void setProducts(Set<Product> products) {
		this.products = products;
	}
}
