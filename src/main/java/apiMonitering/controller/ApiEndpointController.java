package apiMonitering.controller;

import apiMonitering.domain.ApiResponse;
import apiMonitering.dto.create.CreateApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.service.ApiEndpointService;
import ch.qos.logback.core.model.Model;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

@RestController
@AllArgsConstructor
public class ApiEndpointController {

    private final ApiEndpointService apiEndpointService;

    @GetMapping("/api/{id}")
    public ResponseEntity<Mono<ApiResponse>> getTest(@Valid @PathVariable("id") Long id ) {



        return ResponseEntity.ok(apiEndpointService.getApi(id));
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
