package apiMonitering.controller;

import apiMonitering.domain.ApiResponse;
import apiMonitering.service.ApiEndpointService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

@RestController
@AllArgsConstructor
public class ApiEndpointController {

    private final ApiEndpointService apiEndpointService;

    @GetMapping("/api")
    public ResponseEntity<Mono<String>> getTest(Model model) throws URISyntaxException {



        return ResponseEntity.ok(apiEndpointService.getApi());
    }
}
