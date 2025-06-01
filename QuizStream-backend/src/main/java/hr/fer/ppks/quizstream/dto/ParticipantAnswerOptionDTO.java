package hr.fer.ppks.quizstream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantAnswerOptionDTO {
    private Long id;
    private String text;

    public ParticipantAnswerOptionDTO(AnswerOptionDTO ao) {
        if (ao == null) {
            return;
        }
        this.id = ao.getId();
        this.text = ao.getText();
    }
}
