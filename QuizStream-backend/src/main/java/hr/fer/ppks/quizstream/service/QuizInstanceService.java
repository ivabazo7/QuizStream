package hr.fer.ppks.quizstream.service;

import hr.fer.ppks.quizstream.dto.ParticipantAnswersDTO;
import hr.fer.ppks.quizstream.model.Quiz;
import hr.fer.ppks.quizstream.model.QuizInstance;
import hr.fer.ppks.quizstream.repository.QuizInstanceRepository;
import hr.fer.ppks.quizstream.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuizInstanceService {

    @Autowired
    private QuizInstanceRepository quizInstanceRepository;

    @Autowired
    private QuizRepository quizRepository;

    private final Map<String, List<ParticipantAnswersDTO>> answerStore = new ConcurrentHashMap<>();

    public QuizInstance createInstance(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        String code = generateUniqueCode();

        QuizInstance instance = new QuizInstance();
        instance.setQuiz(quiz);
        instance.setCode(code);
        instance.setStartTimestamp(LocalDateTime.now());

        return quizInstanceRepository.save(instance);
    }

    private String generateUniqueCode() {
        Random random = new Random();
        String code;

        do {
            code = String.format("%06d", random.nextInt(1_000_000));
        } while (quizInstanceRepository.existsByCodeAndEndTimestampIsNull(code));

        return code;
    }

    public void endInstanceByCode(String quizCode) {
        QuizInstance instance = quizInstanceRepository.findByCodeAndEndTimestampIsNull(quizCode)
                .orElseThrow(() -> new NoSuchElementException("Active quiz instance not found"));

        instance.setEndTimestamp(LocalDateTime.now());
        quizInstanceRepository.save(instance);
    }

    public boolean existsByCode(String quizCode) {
        Optional<QuizInstance> instance = quizInstanceRepository.findByCodeAndEndTimestampIsNull(quizCode);
        return instance.isPresent();
    }

    public void saveAnswer(String quizCode, ParticipantAnswersDTO answer) {
        answerStore.computeIfAbsent(quizCode + ":" + answer.getQuestionId(), k -> new ArrayList<>())
                .add(answer);
    }

    public Map<String, Integer> getAnswerStats(String quizCode, String questionId) {
        List<ParticipantAnswersDTO> answers = answerStore.getOrDefault(quizCode + ":" + questionId, new ArrayList<>());

        Map<String, Integer> stats = new HashMap<>();
        for (ParticipantAnswersDTO a : answers) {
            for (String id : a.getAnswerIds()) {
                stats.put(id, stats.getOrDefault(id, 0) + 1);
            }
        }
        return stats;
    }
}
