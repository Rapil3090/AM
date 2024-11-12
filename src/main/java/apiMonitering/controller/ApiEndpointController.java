package apiMonitering.controller;

import apiMonitering.dto.create.CreateApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.service.ApiEndpointService;
import ch.qos.logback.core.model.Model;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

@RestController
@AllArgsConstructor
public class ApiEndpointController {

    private final ApiEndpointService apiEndpointService;

    @GetMapping("/api")
    public ResponseEntity<Mono<String>> getTest(Model model) throws URISyntaxException {



        return ResponseEntity.ok(apiEndpointService.getApi(7L));
    }

    @GetMapping("/api3")
    public void getApiCall() {

        apiEndpointService.scheduledApiCall();
    }

    @PostMapping("/save")
    public ResponseEntity<ApiEndpoint> saveApiEndpoint(
            @Valid @RequestBody CreateApiEndpointDTO.Request request) {

        return ResponseEntity.ok(apiEndpointService.createApi(request));
    }
}
