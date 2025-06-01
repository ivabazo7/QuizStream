package hr.fer.ppks.quizstream.websocket;

import hr.fer.ppks.quizstream.service.QuizStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Praćenje broja spojenih sudionika.
 */
@Slf4j
@Component
public class QuizWebSocketEventListener {

    @Autowired
    private QuizStateService quizStateService;

    private final Map<String, Set<String>> participantSessions = new ConcurrentHashMap<>();
    private final Map<String, String> moderatorSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String destination = headers.getDestination();

        // /topic/quiz/{quizCode}/*
        if (destination != null) {
            String quizCode = extractQuizCode(destination);
            if (destination.contains("/moderatorState")) {
                // obrada moderatora
                moderatorSessions.put(sessionId, quizCode);
                log.info("Moderator {} subscribed to quiz {}", sessionId, quizCode);
                // slanje trenutnog broja sudionika
                sendParticipantCount(quizCode);
            } else if (destination.contains("/participantState")){
                // obrada sudionika
                participantSessions.computeIfAbsent(quizCode, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
                log.info("Participant {} subscribed to quiz {}", sessionId, quizCode);
                // slanje trenutnog broja sudionika
                sendParticipantCount(quizCode);
            }
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        // odspajanje moderatora
        if (moderatorSessions.containsKey(sessionId)) {
            String quizCode = moderatorSessions.remove(sessionId);
            log.info("Moderator {} disconnected from quiz {}", sessionId, quizCode);
        }

        // odspajanje sudionika
        participantSessions.forEach((quizCode, sessions) -> {
            if (sessions.remove(sessionId)) {
                log.info("Participant {} disconnected from quiz {}", sessionId, quizCode);
                sendParticipantCount(quizCode);
            }
        });
    }

    /**
     * Metoda za ažuriranje broja sudionka, odnosno stanja.
     * @param quizCode kod kviza
     */
    private void sendParticipantCount(String quizCode) {
        int count = participantSessions.getOrDefault(quizCode, Collections.emptySet()).size();
        quizStateService.updateParticipantCount(quizCode, count);
    }

    /**
     * Metoda za izdvajanje koda kviza
     * @param destination destination path
     * @return
     */
    private String extractQuizCode(String destination) {
        String[] parts = destination.split("/");
        return parts[3]; // /topic/quiz/{quizCode}/*
    }
}