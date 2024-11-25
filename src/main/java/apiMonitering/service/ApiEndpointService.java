package apiMonitering.service;

import apiMonitering.domain.ApiResponse;
import apiMonitering.dto.create.CreateApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

public interface ApiEndpointService {

    Mono<ApiResponse> getApi(ApiEndpoint apiEndpoint);

    ApiEndpoint createApi(CreateApiEndpointDTO.Request request);

    void scheduledApiCall();

    String setServiceKey(CreateApiEndpointDTO.Request request);
}
