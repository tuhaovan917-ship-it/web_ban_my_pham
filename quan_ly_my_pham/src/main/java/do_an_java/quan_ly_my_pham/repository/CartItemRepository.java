package do_an_java.quan_ly_my_pham.repository;

import do_an_java.quan_ly_my_pham.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartId(Integer cartId);

    Optional<CartItem> findByCartIdAndProductId(Integer cartId, Integer productId);

    void deleteByProductId(Integer productId);
}
