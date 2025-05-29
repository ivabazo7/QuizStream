package hr.fer.ppks.quizstream.mapper;

import hr.fer.ppks.quizstream.dto.AnswerOptionDTO;
import hr.fer.ppks.quizstream.dto.CreateAnswerOptionDTO;
import hr.fer.ppks.quizstream.model.AnswerOption;
import hr.fer.ppks.quizstream.model.Question;

import java.time.LocalDateTime;

public class AnswerOptionMapper {

    public static AnswerOptionDTO toDto(AnswerOption answerOption) {
        if (answerOption == null) {
            return null;
        }
        AnswerOptionDTO answerOptionDTO = new AnswerOptionDTO();
        answerOptionDTO.setId(answerOption.getId());
        answerOptionDTO.setText(answerOption.getText());
        answerOptionDTO.setCorrect(answerOption.isCorrect());
        answerOptionDTO.setCreatedAt(answerOption.getCreatedAt());
        answerOptionDTO.setUpdatedAt(answerOption.getUpdatedAt());
        return answerOptionDTO;
    }

    public static AnswerOption toEntity(CreateAnswerOptionDTO optDto, LocalDateTime currentTimestamp, Question q) {
        AnswerOption opt = new AnswerOption();
        opt.setText(optDto.getText());
        opt.setCorrect(optDto.isCorrect());
        opt.setCreatedAt(currentTimestamp);
        opt.setUpdatedAt(currentTimestamp);
        opt.setQuestion(q);
        return opt;
    }
}
