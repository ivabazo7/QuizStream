package hr.fer.ppks.quizstream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizDTO {
    private Long moderatorId;
    private String name;
    private String description;
    private List<CreateQuestionDTO> questions;
}
