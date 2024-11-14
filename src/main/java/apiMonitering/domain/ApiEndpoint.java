package apiMonitering.domain;

import apiMonitering.utils.MapToJsonConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Convert(converter = MapToJsonConverter.class)
    private Map<String, String> queryParameters;

    @JsonIgnore
    @JoinColumn(name = "users_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Users users;

}
