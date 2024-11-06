package apiMonitering.service;

import apiMonitering.domain.ApiEndpoint;
import apiMonitering.domain.ApiResponse;
import reactor.core.publisher.Mono;

public interface ApiEndpointService {

    Mono<ApiResponse> getApi();

}
