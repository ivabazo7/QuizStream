package hr.fer.ppks.quizstream.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "quiz_response")
public class QuizResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "votes_count", nullable = false)
    private int votesCount;

    @Column(name = "participant_ids", columnDefinition = "jsonb")
    private List<String> participantIds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_instance_id", nullable = false)
    private QuizInstance quizInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_option_id", nullable = false)
    private AnswerOption answerOption;
}