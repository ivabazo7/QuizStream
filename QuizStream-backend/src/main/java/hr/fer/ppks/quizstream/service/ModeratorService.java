package hr.fer.ppks.quizstream.service;

import hr.fer.ppks.quizstream.dto.QuizDTO;
import hr.fer.ppks.quizstream.mapper.QuizMapper;
import hr.fer.ppks.quizstream.model.Moderator;
import hr.fer.ppks.quizstream.model.Quiz;
import hr.fer.ppks.quizstream.repository.ModeratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModeratorService {

    @Autowired
    private ModeratorRepository moderatorRepository;

    public List<QuizDTO> getAll(Long moderatorId) {
        if (moderatorId == null) {
            return null;
        }
        Moderator moderator = moderatorRepository.getReferenceById(moderatorId);
        List<Quiz> quizzes = moderator.getQuizzes();
        return quizzes.stream().map(QuizMapper::toDto).toList();
    }
}
