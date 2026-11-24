package org.example.service.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.BankAccountState;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveBalanceWebSocketHandler extends TextWebSocketHandler {
    private final BankAccountService bankAccountService;
    private final ObjectMapper objectMapper;

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Web socket connected. Total active sessions {}", sessions.size());

        sendFullData(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);

       // session.close();

    }

    private void sendFullData(WebSocketSession session) {

        var accounts = bankAccountService.findAll()
                .stream()
                .map(acc -> new BankAccountState(
                        acc.getAccountNumber(),
                        acc.getBalance()))
                .toList();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(accounts);
            session.sendMessage(new TextMessage(json));
        } catch (IOException | JacksonException e) {
            log.error("Error send message to websocket session {} {} {} ", json, session.getId(), e.getMessage());
        }

    }

    public void sendUpdate(BankAccountState update) {
        if (sessions.isEmpty()) {
            return;
        }
        String json;

        try {
            json = objectMapper.writeValueAsString(update);
        } catch (JacksonException e) {
            log.error("Error serializeble {}", e.getMessage());
            return;
        }


        for (var session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException | JacksonException e) {
                log.error("Error update message to websocket  {} {} ", json, e.getMessage());
            }
        }
    }
}
