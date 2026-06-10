package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.Cart;
import do_an_java.quan_ly_my_pham.model.CartItem;
import do_an_java.quan_ly_my_pham.model.Product;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.repository.CartItemRepository;
import do_an_java.quan_ly_my_pham.repository.CartRepository;
import do_an_java.quan_ly_my_pham.repository.ProductRepository;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Cart getOrCreateCart(Integer userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    public List<CartItem> getItems(Integer userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Transactional
    public CartItem addItem(Integer userId, Integer productId, Integer quantity) {
        validateQuantity(quantity);

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Sản phẩm đang không được bán");
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(0);
                return newItem;
            });

        int newQuantity = item.getQuantity() + quantity;
        ensureStockAvailable(product, newQuantity);

        item.setQuantity(newQuantity);
        item.setAddedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        return cartItemRepository.save(item);
    }

    @Transactional
    public CartItem updateQuantity(Integer userId, Integer productId, Integer quantity) {
        validateQuantity(quantity);

        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() -> new NotFoundException("Sản phẩm không có trong giỏ hàng"));

        ensureStockAvailable(item.getProduct(), quantity);
        item.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Integer userId, Integer productId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() -> new NotFoundException("Sản phẩm không có trong giỏ hàng"));

        cartItemRepository.delete(item);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.findByCartId(cart.getId()).forEach(cartItemRepository::delete);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    public void ensureStockAvailable(Product product, int requestedQuantity) {
        if (requestedQuantity > product.getStockQuantity()) {
            throw new BusinessException("So luong san pham trong kho khong du");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("So luong phai lon hon 0");
        }
    }
}
