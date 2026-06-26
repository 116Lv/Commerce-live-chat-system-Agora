package com.team7.agora.domain.chat.repository;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.chat.enums.ChatRoomStatus;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 채팅방 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductAndSellerAndBuyer(Product product, User seller, User buyer);

    Optional<ChatRoom> findByIdAndStatus(Long id, ChatRoomStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cr from ChatRoom cr where cr.id = :id and cr.status = :status")
    Optional<ChatRoom> findByIdAndStatusForUpdate(@Param("id") Long id, @Param("status") ChatRoomStatus status);

    @EntityGraph(attributePaths = {"seller", "buyer", "product"})
    List<ChatRoom> findAllBySellerOrBuyer(User seller, User buyer);
}
