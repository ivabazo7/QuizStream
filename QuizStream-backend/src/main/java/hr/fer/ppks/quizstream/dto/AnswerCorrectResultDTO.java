package hr.fer.ppks.quizstream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerCorrectResultDTO {
    private Long answerId;
    private int count;
    private double percentage;
    private boolean isCorrect;
}