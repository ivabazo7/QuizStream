package hr.fer.ppks.quizstream.mapper;

import hr.fer.ppks.quizstream.dto.AnswerOptionDTO;
import hr.fer.ppks.quizstream.dto.CreateAnswerOptionDTO;
import hr.fer.ppks.quizstream.model.AnswerOption;
import hr.fer.ppks.quizstream.model.Question;

public class AnswerOptionMapper {

    public static AnswerOptionDTO toDto(AnswerOption answerOption) {
        if (answerOption == null) {
            return null;
        }
        AnswerOptionDTO answerOptionDTO = new AnswerOptionDTO();
        answerOptionDTO.setId(answerOption.getId());
        answerOptionDTO.setText(answerOption.getText());
        answerOptionDTO.setCorrect(answerOption.isCorrect());
        return answerOptionDTO;
    }

    public static AnswerOption toEntity(CreateAnswerOptionDTO optDto, Question q) {
        AnswerOption opt = new AnswerOption();
        opt.setText(optDto.getText());
        opt.setCorrect(optDto.isCorrect());
        opt.setQuestion(q);
        return opt;
    }
}
