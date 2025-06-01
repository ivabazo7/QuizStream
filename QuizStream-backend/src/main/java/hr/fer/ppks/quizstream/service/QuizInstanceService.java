package hr.fer.ppks.quizstream.service;

import hr.fer.ppks.quizstream.dto.ParticipantAnswersDTO;
import hr.fer.ppks.quizstream.model.AnswerOption;
import hr.fer.ppks.quizstream.model.Quiz;
import hr.fer.ppks.quizstream.model.QuizInstance;
import hr.fer.ppks.quizstream.model.QuizResponse;
import hr.fer.ppks.quizstream.repository.AnswerOptionRepository;
import hr.fer.ppks.quizstream.repository.QuizInstanceRepository;
import hr.fer.ppks.quizstream.repository.QuizRepository;
import hr.fer.ppks.quizstream.repository.QuizResponseRepository;
import jakarta.transaction.Transactional;
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
    private AnswerOptionRepository answerOptionRepository;

    @Autowired
    private QuizResponseRepository quizResponseRepository;

    @Autowired
    private QuizRepository quizRepository;

    // Privremena lokalna pohrana glasanja
    // Map<quizCode:questionId, List<ParticipantAnswersDTO>>
    private final Map<String, List<ParticipantAnswersDTO>> answerStore = new ConcurrentHashMap<>();

    /**
     * Metoda za kreiranje instance kviza.
     * @param quizId identifikator kviza
     * @return instanca kviza
     */
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

    /**
     * Metoda za generiranje jedinstvenog koda kviza.
     * @return
     */
    private String generateUniqueCode() {
        Random random = new Random();
        String code;

        do {
            code = String.format("%06d", random.nextInt(1_000_000));
        } while (quizInstanceRepository.existsByCodeAndEndTimestampIsNull(code));

        return code;
    }

    /**
     * Metoda koja označava da je kviz neaktivan i čisti lokalne podatke glasanja, te ih sumira i pohranjuje u bazu.
     * @param quizCode kod kviza
     */
    @Transactional
    public void endInstanceByCode(String quizCode) {
        QuizInstance instance = quizInstanceRepository.findByCodeAndEndTimestampIsNull(quizCode)
                .orElseThrow(() -> new NoSuchElementException("Active quiz instance not found"));

        // Počisti answerStore na kraju i pohrani u bazu što je potrebno
        // answerId, count
        Map<String, Integer> counts = new HashMap<>();
        String prefix = quizCode + ":";
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, List<ParticipantAnswersDTO>> entry : answerStore.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                for (ParticipantAnswersDTO answerDTO : entry.getValue()) {
                    for (String answerId : answerDTO.getAnswerIds()) {
                        counts.put(answerId, counts.getOrDefault(answerId, 0) + 1);
                    }
                }
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            answerStore.remove(key);
        }

        // Sumirana statistika
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Long answerId = Long.parseLong(entry.getKey());
            Integer count = entry.getValue();

            Optional<AnswerOption> optAnswerOption = answerOptionRepository.findById(answerId);
            if (optAnswerOption.isEmpty()) {
                // TODO
                return;
            }
            AnswerOption answerOption = optAnswerOption.get();

            QuizResponse quizResponse = new QuizResponse();
            quizResponse.setQuizInstance(instance);
            quizResponse.setAnswerOption(answerOption);
            quizResponse.setVotesCount(count);
            quizResponseRepository.save(quizResponse);
        }

        instance.setEndTimestamp(LocalDateTime.now());
        quizInstanceRepository.save(instance);
    }

    /**
     * Metoda za provjeru postojanja instance kviza prema kodu.
     * @param quizCode kod kviza
     * @return postoji li instanca prema kodu
     */
    public boolean existsByCode(String quizCode) {
        Optional<QuizInstance> instance = quizInstanceRepository.findByCodeAndEndTimestampIsNull(quizCode);
        return instance.isPresent();
    }

    /**
     * Metoda za lokalnu pohranu odgovora sudionika.
     * @param quizCode kod kviza
     * @param answer odgovor
     */
    public void saveAnswer(String quizCode, ParticipantAnswersDTO answer) {
        if (!existsByCode(quizCode)) {
            return;
        }
        answerStore.computeIfAbsent(quizCode + ":" + answer.getQuestionId(), k -> new ArrayList<>())
                .add(answer);
    }

    /**
     * Metoda za dohvat statistike glasanja za neko pitanje iz lokalne pohrane.
     * @param quizCode kod kviza
     * @param questionId identifikator pitanja
     * @return statistika glasnja za pitanje
     */
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
