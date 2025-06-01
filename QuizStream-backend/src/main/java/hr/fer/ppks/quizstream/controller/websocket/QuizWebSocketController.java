package hr.fer.ppks.quizstream.controller.websocket;

import hr.fer.ppks.quizstream.dto.*;
import hr.fer.ppks.quizstream.service.QuizStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class QuizWebSocketController {

    @Autowired
    private QuizStateService quizStateService;

    private final SimpMessagingTemplate messagingTemplate;

    public QuizWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Metoda za objavu novog pitanja.
     * Moderator radi publish kada ide na sljedeće pitanje.
     * Sudionik dobije pitanje u svom state-u.
     * Novo stanje se šalje i moderatoru i sudionicima.
     * @param quizCode kod kviza
     * @param question pitanje koje se emitira
     */
    @MessageMapping("/quiz/{quizCode}/question")
    public void sendQuestion(@DestinationVariable String quizCode, @Payload QuestionDTO question) {

        // Ažuriraj trenutno stanje
        int currentIndex = quizStateService.getCurrentQuestionIndex(quizCode);
        quizStateService.setCurrentQuestionIndex(quizCode, currentIndex + 1);
        quizStateService.setCurrentQuestion(quizCode, question);
        List<AnswerCorrectResultDTO> results = quizStateService.getVotingStats(quizCode, quizStateService.getCurrentQuestionIndex(quizCode).toString());
        quizStateService.setResultsStat(quizCode, quizStateService.getCurrentQuestionIndex(quizCode).toString(), results);
        quizStateService.setShowResults(quizCode, false);

        // Pošalji stanje sudionicima i moderatoru
        quizStateService.sendParticipantState(quizCode);
        quizStateService.sendModeratorState(quizCode);
    }

    /**
     * Metoda za objavu da se statistika glasanja prikazuju sudionicima.
     * @param quizCode kod kviza
     * @param empty prazni podaci
     */
    @MessageMapping("/quiz/{quizCode}/showResults")
    public void sendShowResults(@DestinationVariable String quizCode, @Payload String empty) {

        // Ažuriraj trenutno stanje
        quizStateService.setShowResults(quizCode, true);

        // Pošalji stanje sudionicima
        quizStateService.sendParticipantState(quizCode);
    }

    /**
     * Metoda koja označava kraj emitiranja kviza i inicira gašenje sudionika.
     * Moderator radi publish kada je instanca kviza završila.
     * Sudionik se gasi.
     * @param quizCode kod kviza
     * @param empty prazni podaci
     */
    @MessageMapping("/quiz/{quizCode}/end")
    public void endQuiz(@DestinationVariable String quizCode, @Payload String empty) {
        // Pošalji kraj svim sudionicima koji slušaju
        // Stanje se čisti REST API pozivom
        quizStateService.clearState(quizCode);
        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/end", "Quiz ended");
    }

    /**
     * Metoda koja na dolazak odgovora od sudionika moderatoru ažurira statistiku glasanja za neko pitanje.
     * @param quizCode kod kviza
     * @param answer odgovor sudionika
     */
    @MessageMapping("/quiz/{quizCode}/answer")
    public void receiveAnswer(@DestinationVariable String quizCode, @Payload ParticipantAnswersDTO answer) {
        // Pohrani odgovor lokalno
        quizStateService.saveAnswer(quizCode, answer);

        // Ažuriraj stanje
        String currentQuestionId = quizStateService.getCurrentQuestion(quizCode).getId().toString();
        List<AnswerCorrectResultDTO> results = quizStateService.getVotingStats(quizCode, currentQuestionId);
        quizStateService.setResultsStat(quizCode, currentQuestionId, results);

        // Pošalji stanje moderatoru, a sudionicima samo ako treba prikazati rezultate
        quizStateService.sendModeratorState(quizCode);
        if (quizStateService.getShowResults(quizCode)) {
            quizStateService.sendParticipantState(quizCode);
        }
    }

    /**
     * Metoda za slanje stanja moderatoru.
     * @param quizCode kod kviza
     * @param empty prazni podaci
     */
    @MessageMapping("/quiz/{quizCode}/getModeratorState")
    public void getModeratorState(@DestinationVariable String quizCode, @Payload String empty) {
        quizStateService.sendModeratorState(quizCode);
    }

    /**
     * Metoda za slanje stanja sudionicima.
     * @param quizCode kod kviza
     * @param empty prazni podaci
     */
    @MessageMapping("/quiz/{quizCode}/getParticipantState")
    public void getParticipantState(@DestinationVariable String quizCode, @Payload String empty) {
        quizStateService.sendParticipantState(quizCode);
    }

}
