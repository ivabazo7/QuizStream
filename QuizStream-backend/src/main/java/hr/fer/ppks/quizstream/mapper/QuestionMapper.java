package hr.fer.ppks.quizstream.mapper;

import hr.fer.ppks.quizstream.dto.AnswerOptionDTO;
import hr.fer.ppks.quizstream.dto.CreateQuestionDTO;
import hr.fer.ppks.quizstream.dto.QuestionDTO;
import hr.fer.ppks.quizstream.model.AnswerOption;
import hr.fer.ppks.quizstream.model.Question;
import hr.fer.ppks.quizstream.model.Quiz;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionMapper {

    public static QuestionDTO toDto(Question question) {
        if (question == null) {
            return null;
        }
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(question.getId());
        questionDTO.setText(question.getText());
        questionDTO.setCreatedAt(question.getCreatedAt());
        questionDTO.setUpdatedAt(question.getUpdatedAt());
        if (question.getAnswerOptions() != null && !question.getAnswerOptions().isEmpty()) {
            List<AnswerOptionDTO> answerOptionDTOS = question.getAnswerOptions().stream()
                    .map(AnswerOptionMapper::toDto).toList();
            questionDTO.setAnswerOptions(answerOptionDTOS);
        }
        return questionDTO;
    }

    public static Question toEntity(CreateQuestionDTO qDto, LocalDateTime currentTimestamp, Quiz quiz) {
        Question q = new Question();
        q.setText(qDto.getText());
        q.setCreatedAt(currentTimestamp);
        q.setUpdatedAt(currentTimestamp);
        q.setQuiz(quiz);
        List<AnswerOption> answerOptions = qDto.getAnswerOptions().stream()
                .map(optDto -> AnswerOptionMapper.toEntity(optDto, currentTimestamp, q)).toList();
        q.setAnswerOptions(answerOptions);
        return q;
    }
}
