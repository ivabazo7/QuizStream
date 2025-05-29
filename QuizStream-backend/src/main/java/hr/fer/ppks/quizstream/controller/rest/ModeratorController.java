package hr.fer.ppks.quizstream.controller.rest;

import hr.fer.ppks.quizstream.dto.QuizDTO;
import hr.fer.ppks.quizstream.service.ModeratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/moderator")
public class ModeratorController {

    @Autowired
    private ModeratorService moderatorService;

    @GetMapping("/{moderatorId}/quiz")
    public ResponseEntity<List<QuizDTO>> getAllQuizzes(@PathVariable("moderatorId") Long moderatorId) {
        System.out.println("GET /moderator/{moderatorId}/quiz");
        List<QuizDTO> quizList = moderatorService.getAll(moderatorId);
        if (quizList == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(quizList);
    }
}
