package hr.fer.ppks.quizstream.service;

import hr.fer.ppks.quizstream.dto.CreateAnswerOptionDTO;
import hr.fer.ppks.quizstream.dto.CreateQuestionDTO;
import hr.fer.ppks.quizstream.dto.CreateQuizDTO;
import hr.fer.ppks.quizstream.dto.QuizDTO;
import hr.fer.ppks.quizstream.mapper.QuizMapper;
import hr.fer.ppks.quizstream.model.AnswerOption;
import hr.fer.ppks.quizstream.model.Moderator;
import hr.fer.ppks.quizstream.model.Question;
import hr.fer.ppks.quizstream.model.Quiz;
import hr.fer.ppks.quizstream.repository.AnswerOptionRepository;
import hr.fer.ppks.quizstream.repository.ModeratorRepository;
import hr.fer.ppks.quizstream.repository.QuestionRepository;
import hr.fer.ppks.quizstream.repository.QuizRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ModeratorRepository moderatorRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    /**
     * Metoda za kreiranje kviza.
     * @param quizDTO kviz koji se želi kreirati
     * @return kreirani kviz
     */
    @Transactional
    public QuizDTO createQuiz(CreateQuizDTO quizDTO) {
        if (quizDTO.getModeratorId() == null) {
            return null;
        }
        Moderator moderator = moderatorRepository.getReferenceById(quizDTO.getModeratorId());

        Quiz quiz = QuizMapper.toEntity(quizDTO);
        quiz.setModerator(moderator);
        Quiz savedQuiz = quizRepository.save(quiz);
        return QuizMapper.toDto(savedQuiz);
    }

    /**
     * Metoda za ažuriranje postojećeg kviza.
     * @param quizId identifikator kviza
     * @param quizDTO kviz koji se želi ažurirati
     * @return ažurirani kviz
     */
    @Transactional
    public QuizDTO updateQuiz(Long quizId, CreateQuizDTO quizDTO) {
        if (quizDTO.getModeratorId() == null) {
            return null;
        }
        if (quizId == null) {
            return null;
        }
        Optional<Quiz> opzQuiz = quizRepository.findById(quizId);
        if (opzQuiz.isEmpty()) {
            return null;
        }
        Quiz quiz = opzQuiz.get();
        if (!quiz.getModerator().getId().equals(quizDTO.getModeratorId())) {
            return null;
        }

        quiz.getQuestions().clear();
        quiz.setName(quizDTO.getName());
        quiz.setDescription(quizDTO.getDescription());
        quiz.setUpdatedAt(LocalDateTime.now());

        for (CreateQuestionDTO qDto : quizDTO.getQuestions()) {
            Question question = new Question();
            question.setText(qDto.getText());
            question.setQuiz(quiz);

            for (CreateAnswerOptionDTO aDto : qDto.getAnswerOptions()) {
                AnswerOption answerOption = new AnswerOption();
                answerOption.setText(aDto.getText());
                answerOption.setCorrect(aDto.isCorrect());
                answerOption.setQuestion(question);
                question.getAnswerOptions().add(answerOption);
            }

            quiz.getQuestions().add(question);
        }
        return QuizMapper.toDto(quiz);
    }

    /**
     * Metoga za dohvat kviza prema identifikatoru.
     * @param quizId identifikator kviza
     * @return pohranjeni kviz
     */
    public QuizDTO getQuiz(Long quizId) {
        if (quizId == null) {
            return null;
        }

        Quiz quiz = quizRepository.getReferenceById(quizId);
        return QuizMapper.toDto(quiz);
    }

    /**
     * Metoda za brisanje kviza prema identifikatoru.
     * @param quizId identifikator kviza
     */
    public void deleteQuiz(Long quizId) {
        if (quizId == null) {
            return;
        }

        quizRepository.deleteById(quizId);
    }
}
