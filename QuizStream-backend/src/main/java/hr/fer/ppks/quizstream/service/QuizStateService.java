package hr.fer.ppks.quizstream.service;

import hr.fer.ppks.quizstream.dto.AnswerCorrectResultDTO;
import hr.fer.ppks.quizstream.dto.QuestionDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuizStateService {

    // Map<quizCode, currentQuestion>
    private final Map<String, QuestionDTO> currentQuestionMap = new ConcurrentHashMap<>();
    // Map<quizCode, showResults>
    private final Map<String, Boolean> showResultsMap = new ConcurrentHashMap<>();
    // Map<quizCode, Map<questionId, results>>
    private final Map<String, Map<String, List<AnswerCorrectResultDTO>>> resultsStatMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, List<AnswerCorrectResultDTO>>> tempResultsStatMap = new ConcurrentHashMap<>();
    // Map<quizCode, currentQuestionIndex>
    private final Map<String, Integer> currentQuestionIndexMap = new ConcurrentHashMap<>();

    public void setCurrentQuestionIndex(String quizCode, int index) {
        currentQuestionIndexMap.put(quizCode, index);
    }

    public int getCurrentQuestionIndex(String quizCode) {
        return currentQuestionIndexMap.getOrDefault(quizCode, 0);
    }

    public void setShowResults(String quizCode, boolean show) {
        showResultsMap.put(quizCode, show);
    }

    public boolean getShowResults(String quizCode) {
        //return showResultsMap.getOrDefault(quizCode, false);
        return Boolean.TRUE.equals(showResultsMap.get(quizCode));
    }

    public void setResultsStat(String quizCode, String questionId, List<AnswerCorrectResultDTO> results) {
        resultsStatMap
                .computeIfAbsent(quizCode, k -> new ConcurrentHashMap<>())
                .put(questionId, results);
    }

    public List<AnswerCorrectResultDTO> getResultsStat(String quizCode, String questionId) {
        /*
        return resultsStatMap
                .getOrDefault(quizCode, new HashMap<>())
                .get(questionId);
        */
        return Optional.ofNullable(resultsStatMap.get(quizCode))
                .map(map -> map.get(questionId))
                .orElse(Collections.emptyList());
    }

    public void setTempResultsStat(String quizCode, String questionId, List<AnswerCorrectResultDTO> results) {
        tempResultsStatMap
                .computeIfAbsent(quizCode, k -> new ConcurrentHashMap<>())
                .put(questionId, results);
    }

    public List<AnswerCorrectResultDTO> getTempResultsStat(String quizCode, String questionId) {
        return Optional.ofNullable(tempResultsStatMap.get(quizCode))
                .map(map -> map.get(questionId))
                .orElse(Collections.emptyList());
    }

    public void setCurrentQuestion(String quizCode, QuestionDTO question) {
        currentQuestionMap.put(quizCode, question);
        showResultsMap.put(quizCode, false);
    }

    public QuestionDTO getCurrentQuestion(String quizCode) {
        return currentQuestionMap.get(quizCode);
    }

    public void clearState(String quizCode) {
        currentQuestionIndexMap.remove(quizCode);
        showResultsMap.remove(quizCode);
        resultsStatMap.remove(quizCode);
        tempResultsStatMap.remove(quizCode);
        currentQuestionMap.remove(quizCode);
    }
}
