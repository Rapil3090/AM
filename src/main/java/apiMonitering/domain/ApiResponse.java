package apiMonitering.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "apiresponse")
public class ApiResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apiresponse_id")
    private Long id;

    private String statusCode;

    private int responseTime;

    private String body;

    private String errorMessage;

    @JsonProperty("cmmMsgHeader")
    private String cmmMsgHeader;

    @JsonIgnore
    @JoinColumn(name = "apiendpoint_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ApiEndpoint apiEndpoint;
}
