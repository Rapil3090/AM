package apiMonitering.service;

import apiMonitering.domain.ApiEndpoint;
import apiMonitering.domain.ApiResponse;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

public interface ApiEndpointService {

    Mono<String> getApi() throws URISyntaxException;

}
