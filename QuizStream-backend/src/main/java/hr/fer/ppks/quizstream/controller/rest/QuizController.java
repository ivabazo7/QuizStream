package hr.fer.ppks.quizstream.controller.rest;

import hr.fer.ppks.quizstream.dto.CreateQuizDTO;
import hr.fer.ppks.quizstream.dto.QuizDTO;
import hr.fer.ppks.quizstream.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PostMapping
    public ResponseEntity<QuizDTO> saveQuiz(@RequestBody CreateQuizDTO createQuizDTO) {
        QuizDTO createdQuiz = quizService.createQuiz(createQuizDTO);
        if (createdQuiz == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable("quizId") Long quizId, @RequestBody CreateQuizDTO createQuizDTO) {
        QuizDTO createdQuiz = quizService.updateQuiz(quizId, createQuizDTO);
        if (createdQuiz == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDTO> getQuiz(@PathVariable("quizId") Long quizId) {
        QuizDTO quiz = quizService.getQuiz(quizId);
        if (quiz == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }

    @DeleteMapping("/{quizId}")
    public void deleteQuiz(@PathVariable("quizId") Long quizId) {
       quizService.deleteQuiz(quizId);
    }
}
