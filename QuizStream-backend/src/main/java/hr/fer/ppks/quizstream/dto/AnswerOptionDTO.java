package hr.fer.ppks.quizstream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOptionDTO {
    private Long id;
    private String text;
    private boolean isCorrect;
}
