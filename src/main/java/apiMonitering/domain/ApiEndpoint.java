package apiMonitering.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "apiendpoint")
public class ApiEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apiendpoint_id")
    private Long id;

    private String url;

    private String serviceKey;

    private String param;

    private int scheduledTime;

    private String status;

    private LocalDateTime startTime;

    @JsonIgnore
    @JoinColumn(name = "users_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Users users;

}
