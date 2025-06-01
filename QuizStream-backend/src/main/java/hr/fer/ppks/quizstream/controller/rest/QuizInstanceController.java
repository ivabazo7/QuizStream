package hr.fer.ppks.quizstream.controller.rest;

import hr.fer.ppks.quizstream.dto.QuizCodeResponse;
import hr.fer.ppks.quizstream.model.QuizInstance;
import hr.fer.ppks.quizstream.service.QuizInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz-instance")
public class QuizInstanceController {

    @Autowired
    private QuizInstanceService quizInstanceService;

    @PostMapping("/{quizId}/start")
    public ResponseEntity<QuizCodeResponse> startQuiz(@PathVariable Long quizId) {
        QuizInstance instance = quizInstanceService.createInstance(quizId);
        return ResponseEntity.ok(new QuizCodeResponse(instance.getCode()));
    }

    @PutMapping("/{quizCode}/end")
    public ResponseEntity<Void> endQuizInstanceByCode(@PathVariable String quizCode) {
        quizInstanceService.endInstanceByCode(quizCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate-code/{quizCode}")
    public ResponseEntity<Boolean> validateQuizCode(@PathVariable String quizCode) {
        boolean exists = quizInstanceService.existsByCode(quizCode);
        return ResponseEntity.ok(exists);
    }
}
