package hr.fer.ppks.quizstream.service;

import hr.fer.ppks.quizstream.dto.AnswerOptionDTO;
import hr.fer.ppks.quizstream.mapper.AnswerOptionMapper;
import hr.fer.ppks.quizstream.model.AnswerOption;
import hr.fer.ppks.quizstream.repository.AnswerOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnswerOptionService {

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    public Map<Long, AnswerOptionDTO> getAllByQuestionId(Long questionId) {
        List<AnswerOption> answerOptions = answerOptionRepository.findAllByQuestionId(questionId);
        return answerOptions.stream()
                .collect(Collectors.toMap(AnswerOption::getId, AnswerOptionMapper::toDto));
    }
}
