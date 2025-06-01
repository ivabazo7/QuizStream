package hr.fer.ppks.quizstream.service;

import hr.fer.ppks.quizstream.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servis koji pohranjuje stanje WebSocket konekcije za instancu kviza
 */
@Service
public class QuizStateService {

    @Autowired
    private QuizInstanceService quizInstanceService;

    @Autowired
    private AnswerOptionService answerOptionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, Integer> currentQuestionIndexMap = new ConcurrentHashMap<>(); // Map<quizCode, currentQuestionIndex>
    private final Map<String, QuestionDTO> currentQuestionMap = new ConcurrentHashMap<>(); // Map<quizCode, currentQuestion>
    private final Map<String, Boolean> showResultsMap = new ConcurrentHashMap<>(); // Map<quizCode, showResults>
    private final Map<String, Map<String, List<AnswerCorrectResultDTO>>> resultsStatMap = new ConcurrentHashMap<>(); // Map<quizCode, Map<questionId, results>>
    private final Map<String, Integer> participantCountMap = new ConcurrentHashMap<>(); // Map<quizCode, count>

    /**
     * Metoda za postavljanje indeksa trenutnog pitanja
     * @param quizCode kod kviza
     * @param index indeks novog pitanja
     */
    public void setCurrentQuestionIndex(String quizCode, int index) {
        currentQuestionIndexMap.put(quizCode, index);
    }

    /**
     * Metoda za dohvat trenutnog indeksa pitanja
     * @param quizCode kod kviza
     * @return indeks pitanja
     */
    public Integer getCurrentQuestionIndex(String quizCode) {
        return currentQuestionIndexMap.getOrDefault(quizCode, 0);
    }

    /**
     * Metoda za postavljanje prikaza rezultata
     * @param quizCode kod kviza
     * @param show nova vrijednost prikaza
     */
    public void setShowResults(String quizCode, boolean show) {
        showResultsMap.put(quizCode, show);
    }

    /**
     * Metoda za dohvat zastavice prikaza rezultata
     * @param quizCode kod kviza
     * @return zastavica prikaza rezultata
     */
    public boolean getShowResults(String quizCode) {
        return Boolean.TRUE.equals(showResultsMap.get(quizCode));
    }

    /**
     * Metoda za postavljanje statistike glasanja za neko pitanje
     * @param quizCode kod kviza
     * @param questionId identifikator pitanja
     * @param results statistika glasanja
     */
    public void setResultsStat(String quizCode, String questionId, List<AnswerCorrectResultDTO> results) {
        resultsStatMap
                .computeIfAbsent(quizCode, k -> new ConcurrentHashMap<>())
                .put(questionId, results);
    }

    /**
     * Metoda za dohvat statistike glasanja
     * @param quizCode kod kviza
     * @param questionId identifikator pitanja
     * @return statistika glasanja za pitanje
     */
    public List<AnswerCorrectResultDTO> getResultsStat(String quizCode, String questionId) {
        return Optional.ofNullable(resultsStatMap.get(quizCode))
                .map(map -> map.get(questionId))
                .orElse(Collections.emptyList());
    }

    /**
     * Metoda za postavljanje trenutnog pitanja
     * @param quizCode kod kviza
     * @param question trenutno pitanje
     */
    public void setCurrentQuestion(String quizCode, QuestionDTO question) {
        currentQuestionMap.put(quizCode, question);
        showResultsMap.put(quizCode, false);
    }

    /**
     * Metoda za dohvat trenutnog pitanja
     * @param quizCode kod kviza
     * @return pitanje
     */
    public QuestionDTO getCurrentQuestion(String quizCode) {
        return currentQuestionMap.get(quizCode);
    }

    /**
     * Metoda za ažuriranje broja trenutno spojenih sudionika
     * @param quizCode kod kviza
     * @param count broj sudionika
     */
    public void updateParticipantCount(String quizCode, int count) {
        participantCountMap.put(quizCode, count);
        sendModeratorState(quizCode);
    }

    /**
     * Metoda za dohvat broja trenutno spojenih sudionika
     * @param quizCode kod kviza
     * @return broj sudionika
     */
    public int getParticipantCount(String quizCode) {
        return participantCountMap.getOrDefault(quizCode, 0);
    }

    /**
     * Metoda za slanje trenutnog stanja moderatora.
     * @param quizCode kod kviza
     */
    public void sendModeratorState(String quizCode) {
        ModeratorStateDTO moderatorStateDTO = new ModeratorStateDTO();
        moderatorStateDTO.setCurrentQuestionIndex(getCurrentQuestionIndex(quizCode));
        moderatorStateDTO.setParticipantCount(getParticipantCount(quizCode));
        moderatorStateDTO.setShowResults(getShowResults(quizCode));

        QuestionDTO currentQuestion = getCurrentQuestion(quizCode);
        if (currentQuestion != null) {
            // Postavlja inicijalnu statistiku glasanja
            List<AnswerCorrectResultDTO> results = getVotingStats(quizCode, currentQuestion.getId().toString());
            setResultsStat(quizCode, currentQuestion.getId().toString(), results);
            moderatorStateDTO.setResultsStat(results);
        } else {
            moderatorStateDTO.setResultsStat(null);
        }
        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/moderatorState", moderatorStateDTO);
    }

    /**
     * Metoda za slanje trenutnog stanja sudionika.
     * @param quizCode kod kviza
     */
    public void sendParticipantState(String quizCode) {
        ParticipantStateDTO participantStateDTO = new ParticipantStateDTO();
        participantStateDTO.setCurrentQuestion(getCurrentQuestion(quizCode));
        participantStateDTO.setShowResults(getShowResults(quizCode));

        QuestionDTO currentQuestion = getCurrentQuestion(quizCode);
        if (currentQuestion != null && getShowResults(quizCode)) { // postavlja se samo kada treba rezultate prikazati
            participantStateDTO.setResultsStat(getResultsStat(quizCode, currentQuestion.getId().toString()));
        } else {
            participantStateDTO.setResultsStat(null);
        }
        messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/participantState", participantStateDTO);
    }

    /**
     * Metoda za pripremu statistike glasanja za neko pitanje i trenutno aktivni kviz.
     * @param quizCode kod kviza
     * @param questionId id pitanja
     * @return statistika glasanja
     */
    public List<AnswerCorrectResultDTO> getVotingStats(String quizCode, String questionId) {
        Map<String, Integer> answerStats = quizInstanceService.getAnswerStats(quizCode, questionId);
        int total = answerStats.values().stream().mapToInt(Integer::intValue).sum();

        Map<Long, AnswerOptionDTO> map = answerOptionService.getAllByQuestionId(Long.parseLong(questionId));

        return map.entrySet().stream()
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
    }

    /**
     * Metoda za čišćenje stanja instance kviza kada postane neaktivna.
     * @param quizCode kod kviza
     */
    public void clearState(String quizCode) {
        currentQuestionIndexMap.remove(quizCode);
        showResultsMap.remove(quizCode);
        resultsStatMap.remove(quizCode);
        currentQuestionMap.remove(quizCode);
        participantCountMap.remove(quizCode);
    }

    /**
     * Metoda za lokalnu pohranu rezultata glansanja.
     * @param quizCode kod kviza
     * @param answer odgovor
     */
    public void saveAnswer(String quizCode, ParticipantAnswersDTO answer) {
        quizInstanceService.saveAnswer(quizCode, answer);
    }
}
