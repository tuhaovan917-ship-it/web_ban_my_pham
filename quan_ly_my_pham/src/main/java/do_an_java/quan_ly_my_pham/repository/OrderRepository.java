package do_an_java.quan_ly_my_pham.repository;

import do_an_java.quan_ly_my_pham.model.Order;
import do_an_java.quan_ly_my_pham.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserIdOrderByOrderDateDesc(Integer userId);

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findByStatus(OrderStatus status);
}
