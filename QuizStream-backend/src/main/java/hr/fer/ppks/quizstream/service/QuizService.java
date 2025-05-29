package hr.fer.ppks.quizstream.service;

import hr.fer.ppks.quizstream.dto.CreateQuestionDTO;
import hr.fer.ppks.quizstream.dto.CreateQuizDTO;
import hr.fer.ppks.quizstream.dto.QuizDTO;
import hr.fer.ppks.quizstream.mapper.QuestionMapper;
import hr.fer.ppks.quizstream.mapper.QuizMapper;
import hr.fer.ppks.quizstream.model.Moderator;
import hr.fer.ppks.quizstream.model.Question;
import hr.fer.ppks.quizstream.model.Quiz;
import hr.fer.ppks.quizstream.repository.ModeratorRepository;
import hr.fer.ppks.quizstream.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ModeratorRepository moderatorRepository;

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

        if (quizDTO.getName() != null && !quiz.getName().equals(quizDTO.getName())) {
            quiz.setName(quizDTO.getName());
        }

        if (quizDTO.getDescription() != null && !quiz.getDescription().equals(quizDTO.getDescription())) {
            quiz.setDescription(quizDTO.getDescription());
        }

        /* TODO
        List<Question> questions = quizDTO.getQuestions().stream()
                .map(qDto -> QuestionMapper.toEntity(qDto, LocalDateTime.now(), quiz)).toList();
        quiz.setQuestions(questions);
        */

        Quiz savedQuiz = quizRepository.save(quiz);
        return QuizMapper.toDto(savedQuiz);
    }

    public QuizDTO getQuiz(Long quizId) {
        if (quizId == null) {
            return null;
        }

        Quiz quiz = quizRepository.getReferenceById(quizId);
        return QuizMapper.toDto(quiz);
    }

    public void deleteQuiz(Long quizId) {
        if (quizId == null) {
            return;
        }

        quizRepository.deleteById(quizId);
    }
}
