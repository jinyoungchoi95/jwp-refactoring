package kitchenpos.product.dao;

import java.util.List;
import java.util.Optional;
import kitchenpos.product.domain.Product;

public interface ProductDao {
    Product save(Product entity);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    default Product getById(Long id) {
        return findById(id).orElseThrow(IllegalArgumentException::new);
    }
}
