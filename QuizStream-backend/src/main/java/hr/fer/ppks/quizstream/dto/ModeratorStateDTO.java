package hr.fer.ppks.quizstream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeratorStateDTO {
    private int currentQuestionIndex;
    private int participantCount;
    private boolean showResults;
    private List<AnswerCorrectResultDTO> resultsStat;
}
