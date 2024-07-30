package beyond.orderSystem.product.repository;

import beyond.orderSystem.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
