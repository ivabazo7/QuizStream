package hr.fer.ppks.quizstream.dto;

import hr.fer.ppks.quizstream.model.Moderator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeratorDTO {
    private Long id;
    private String username;
    private String email;

    public ModeratorDTO(Moderator moderator) {
        this.id = moderator.getId();
        this.username = moderator.getUsername();
        this.email = moderator.getEmail();
    }
}
