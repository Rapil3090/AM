package apiMonitering.service;

import apiMonitering.DTO.ApiEndpointDTO.RequestApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.domain.ApiResponse;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

public interface ApiEndpointService {

    Mono<String> getApi(RequestApiEndpointDTO.Request request);

}
