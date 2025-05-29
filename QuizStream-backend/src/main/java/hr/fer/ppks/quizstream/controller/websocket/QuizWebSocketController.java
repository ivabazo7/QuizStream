package hr.fer.ppks.quizstream.controller.websocket;

import hr.fer.ppks.quizstream.dto.*;
import hr.fer.ppks.quizstream.service.AnswerOptionService;
import hr.fer.ppks.quizstream.service.QuizInstanceService;
import hr.fer.ppks.quizstream.service.QuizStateService;
import hr.fer.ppks.quizstream.websocket.QuizWebSocketEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class QuizWebSocketController {

    @Autowired
    private QuizInstanceService quizInstanceService;

    @Autowired
    private AnswerOptionService answerOptionService;

    @Autowired
    private QuizWebSocketEventListener quizWebSocketEventListener;

    @Autowired
    private QuizStateService quizStateService;

    private final SimpMessagingTemplate messagingTemplate;

    public QuizWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/quiz/{quizCode}/question")
    public void sendQuestion(@DestinationVariable String quizCode, @Payload QuestionDTO question) {

        // Spremi trenutno stanje
        int currentIndex = quizStateService.getCurrentQuestionIndex(quizCode);
        quizStateService.setCurrentQuestionIndex(quizCode, currentIndex + 1);
        quizStateService.setCurrentQuestion(quizCode, question);
        quizStateService.setShowResults(quizCode, false);

        // Pošalji stanje svim klijentima
        getCurrentState(quizCode);

        // Pošalji pitanje svim sudionicima koji slušaju
        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/question", question);
    }

    @MessageMapping("/quiz/{quizCode}/end")
    public void endQuiz(@DestinationVariable String quizCode, @Payload String empty) {
        // Pošalji kraj svim sudionicima koji slušaju
        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/end", "Quiz ended");
    }

    /**
     * Metoda koja na dolazak odgovora od sudionika moderatoru ažurira statistiku glasanja za neko pitanje.
     * @param quizCode
     * @param answer
     */
    @MessageMapping("/quiz/{quizCode}/answer")
    public void receiveAnswer(@DestinationVariable String quizCode, @Payload ParticipantAnswersDTO answer) {
        quizInstanceService.saveAnswer(quizCode, answer);
        List<AnswerCorrectResultDTO> results = getVotingStats(quizCode, answer.getQuestionId());
        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/showResults", results);
    }

    /**
     * Metoda koja prikazuje konačnu statistiku glasanja za neko pitanje.
     * @param quizCode
     * @param questionId
     */
    @MessageMapping("/quiz/{quizCode}/finalResults")
    public void showQuestionResults(@DestinationVariable String quizCode, @Payload String questionId) {
        List<AnswerCorrectResultDTO> results = getVotingStats(quizCode, questionId);
        quizStateService.setResultsStat(quizCode, questionId, results);
        quizStateService.setShowResults(quizCode, true);

        // Pošalji stanje svim klijentima
        getCurrentState(quizCode);

        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/finalResults", results);
    }

    /**
     * Metoda za pripremu statistike glasanja za neko pitanje i trenutno aktivni kviz.
     * @param quizCode
     * @param questionId
     * @return
     */
    private List<AnswerCorrectResultDTO> getVotingStats(String quizCode, String questionId) {
        Map<String, Integer> answerStats = quizInstanceService.getAnswerStats(quizCode, questionId);
        int total = answerStats.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println(answerStats);

        Map<Long, AnswerOptionDTO> map = answerOptionService.getAllByQuestionId(Long.parseLong(questionId));

        List<AnswerCorrectResultDTO> results = map.entrySet().stream()
                .map(entry -> {
                    Integer count = answerStats.get(entry.getKey().toString());
                    if (count == null) count = 0;
                    double percentage = 0.;
                    if (total !=  0) {
                        percentage = count * 100. / total;
                    }
                    return new AnswerCorrectResultDTO(entry.getKey(), count, percentage, entry.getValue().isCorrect());
                })
                .toList();

        for (AnswerCorrectResultDTO r : results) {
            System.out.println(r);
        }
        return results;
    }

    /**
     * Metoda za dohvat trenutnog stanja.
     * @param quizCode
     */
    @MessageMapping("/quiz/{quizCode}/getState")
    public void getCurrentState(@DestinationVariable String quizCode) {
        // npr broj sudionika, aktivno pitanje i slično
        Map<String, Object> state = new HashMap<>();
        state.put("participantCount", quizWebSocketEventListener.getParticipantCount(quizCode));
        state.put("currentQuestion", quizStateService.getCurrentQuestion(quizCode));
        state.put("showResults", quizStateService.getShowResults(quizCode));
        state.put("currentQuestionIndex", quizStateService.getCurrentQuestionIndex(quizCode));

        // Add results to state
        QuestionDTO currentQuestion = quizStateService.getCurrentQuestion(quizCode);
        if (currentQuestion != null) {
            List<AnswerCorrectResultDTO> results = quizStateService.getResultsStat(
                    quizCode,
                    currentQuestion.getId().toString()
            );
            state.put("resultsStat", results);
        }

        if (currentQuestion != null && !quizStateService.getShowResults(quizCode)) {
            List<AnswerCorrectResultDTO> tempResults = quizStateService.getTempResultsStat(
                    quizCode,
                    currentQuestion.getId().toString()
            );
            if (!tempResults.isEmpty()) {
                state.put("resultsStat", tempResults);
            }
        }

        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/state", state);
    }

    @MessageMapping("/quiz/{quizCode}/getResults")
    public void getResults(@DestinationVariable String quizCode, @Payload String questionId) {
        List<AnswerCorrectResultDTO> results = quizStateService.getResultsStat(quizCode, questionId);
        if (results != null) {
            messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/finalResults", results);
        }
    }

    @MessageMapping("/quiz/{quizCode}/getTempResults")
    public void getTempResults(@DestinationVariable String quizCode, @Payload String questionId) {
        List<AnswerCorrectResultDTO> results = getVotingStats(quizCode, questionId);
        // pohrani privremene rezultate
        quizStateService.setTempResultsStat(quizCode, questionId, results);
        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/showResults", results);
    }

}
