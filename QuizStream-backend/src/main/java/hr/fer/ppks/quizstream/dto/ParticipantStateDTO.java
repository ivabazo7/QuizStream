package hr.fer.ppks.quizstream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantStateDTO {
    private ParticipantQuestionDTO currentQuestion;
    private boolean showResults;
    private List<AnswerCorrectResultDTO> resultsStat;

    public void setCurrentQuestion(QuestionDTO question) {
        if (question == null) {
            return;
        }
        ParticipantQuestionDTO participantQuestionDTO = new ParticipantQuestionDTO();
        participantQuestionDTO.setId(question.getId());
        participantQuestionDTO.setText(question.getText());
        participantQuestionDTO.setAnswerOptions(question.getAnswerOptions().stream().map(ParticipantAnswerOptionDTO::new).toList());
        currentQuestion = participantQuestionDTO;
    }
}
