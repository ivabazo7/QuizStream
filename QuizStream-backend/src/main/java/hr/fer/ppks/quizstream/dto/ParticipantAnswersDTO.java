package hr.fer.ppks.quizstream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantAnswersDTO {
    private String questionId;
    private List<String> answerIds;
}
