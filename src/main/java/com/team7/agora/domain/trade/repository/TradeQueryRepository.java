package com.team7.agora.domain.trade.repository;

import com.team7.agora.domain.trade.dto.request.MyTradeRole;
import com.team7.agora.domain.trade.dto.response.MyTradeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradeQueryRepository {

    Page<MyTradeResponse> findMyTrades(Long userId, MyTradeRole role, Pageable pageable);
}