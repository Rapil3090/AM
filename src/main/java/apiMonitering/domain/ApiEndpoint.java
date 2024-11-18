package apiMonitering.domain;

import apiMonitering.utils.MapToJsonConverter;
import apiMonitering.utils.ParamListToJsonConverter;
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

    @Convert(converter = ParamListToJsonConverter.class)
    @Lob
    private List<Parameter> parameters;

    @JsonIgnore
    @JoinColumn(name = "users_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Users users;

    @Embeddable
    @Getter
    public static class Parameter { // public 추가
        private String type;
        private String key;
        private String value;
    }
}
