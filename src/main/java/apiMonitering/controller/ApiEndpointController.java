package apiMonitering.controller;

import apiMonitering.dto.apiEndpointDTO.RequestApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.service.ApiEndpointService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

@RestController
@AllArgsConstructor
public class ApiEndpointController {

    private final ApiEndpointService apiEndpointService;

    @GetMapping("/api")
    public ResponseEntity<Flux<String>> getTest(Model model) throws URISyntaxException {

        return ResponseEntity.ok(apiEndpointService.fetchAndProcessApiData());
    }

    @GetMapping("/api2")
    public ResponseEntity<Mono<String>> getTest2(Model model) throws URISyntaxException {

        return ResponseEntity.ok(apiEndpointService.getApi2());
    }

    @PostMapping("/save")
    public ResponseEntity<ApiEndpoint> saveApiEndpoint(
            @Valid @RequestBody RequestApiEndpointDTO.Request request) {

        return ResponseEntity.ok(apiEndpointService.createApi(request));
    }
}
