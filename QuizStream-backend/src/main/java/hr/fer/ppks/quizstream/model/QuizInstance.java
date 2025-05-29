package hr.fer.ppks.quizstream.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "quiz_instance")
public class QuizInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "start_timestamp", nullable = false)
    private LocalDateTime startTimestamp;

    @Column(name = "end_timestamp", nullable = true)
    private LocalDateTime endTimestamp;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "quizInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizResponse> quizResponses;
}