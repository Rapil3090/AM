package apiMonitering.domain;

import apiMonitering.type.MapToJsonConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "apiendpoint")
public class ApiEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apiendpoint_id")
    private Long id;

    private String url;

    private String serviceKey;

//    private List<List<String>> parameters;

    @Convert(converter = MapToJsonConverter.class)
    private Map<String, String> query;

    private int scheduledTime;

    private String status;

    private LocalDateTime startTime;

    @JsonIgnore
    @JoinColumn(name = "users_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Users users;

}
