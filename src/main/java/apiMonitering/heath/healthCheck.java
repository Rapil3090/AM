package apiMonitering.heath;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class healthCheck {



    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {

        return ResponseEntity.ok("ok");
    }
}
