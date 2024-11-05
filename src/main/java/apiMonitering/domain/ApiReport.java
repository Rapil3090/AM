package apiMonitering.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "apireport")
public class ApiReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apireport_id")
    private Long id;

    private int errorCount;

    private LocalDateTime reportStamp;

}
