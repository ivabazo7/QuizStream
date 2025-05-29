package hr.fer.ppks.quizstream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnswerOptionDTO {
    private String text;
    private boolean isCorrect;
}
