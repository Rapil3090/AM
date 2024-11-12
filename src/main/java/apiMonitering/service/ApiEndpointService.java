package apiMonitering.service;

import apiMonitering.dto.create.CreateApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

public interface ApiEndpointService {

    Mono<String> getApi(Long id);

//    Mono<String> getApi2() throws URISyntaxException;

//    Flux<String> fetchAndProcessApiData();

    ApiEndpoint createApi(CreateApiEndpointDTO.Request request);

    void scheduledApiCall();
}
