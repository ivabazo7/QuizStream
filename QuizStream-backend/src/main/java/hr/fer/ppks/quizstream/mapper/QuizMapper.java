package hr.fer.ppks.quizstream.mapper;

import hr.fer.ppks.quizstream.dto.CreateQuizDTO;
import hr.fer.ppks.quizstream.dto.QuestionDTO;
import hr.fer.ppks.quizstream.dto.QuizDTO;
import hr.fer.ppks.quizstream.model.Question;
import hr.fer.ppks.quizstream.model.Quiz;

import java.time.LocalDateTime;
import java.util.List;

public class QuizMapper {

    public static QuizDTO toDto(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setName(quiz.getName());
        if (quiz.getDescription() != null && !quiz.getDescription().isEmpty()) {
            quizDTO.setDescription(quiz.getDescription());
        }
        quizDTO.setCreatedAt(quiz.getCreatedAt());
        quizDTO.setUpdatedAt(quiz.getUpdatedAt());

        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            List<QuestionDTO> questionDTOS = quiz.getQuestions().stream()
                    .map(QuestionMapper::toDto).toList();
            quizDTO.setQuestions(questionDTOS);
        }

        return quizDTO;
    }

    public static Quiz toEntity(CreateQuizDTO quizDTO) {
        Quiz quiz = new Quiz();
        quiz.setName(quizDTO.getName());
        if (quizDTO.getDescription() != null && !quizDTO.getDescription().isEmpty()) {
            quiz.setDescription(quizDTO.getDescription());
        }
        LocalDateTime currentTimestamp = LocalDateTime.now();
        quiz.setCreatedAt(currentTimestamp);
        quiz.setUpdatedAt(currentTimestamp);
        List<Question> questions = quizDTO.getQuestions().stream()
                .map(qDto -> QuestionMapper.toEntity(qDto, quiz)).toList();
        quiz.setQuestions(questions);
        return quiz;
    }
}
