package com.team7.agora.domain.chat.repository;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.enums.ChatRoomStatus;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductAndSellerAndBuyer(Product product, User seller, User buyer);

    Optional<ChatRoom> findByIdAndStatus(Long id, ChatRoomStatus status);

    List<ChatRoom> findAllBySellerOrBuyer(User seller, User buyer);
}
