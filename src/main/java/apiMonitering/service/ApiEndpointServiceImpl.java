package apiMonitering.service;

import apiMonitering.dto.create.CreateApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.exception.ApiEndPointException;
import apiMonitering.repository.ApiEndpointRepository;
import apiMonitering.type.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiEndpointServiceImpl implements ApiEndpointService {

    private final ApiEndpointRepository apiEndpointRepository;


    public Flux<String> fetchAndProcessApiData() {

        List<ApiEndpoint> endPoints = apiEndpointRepository.findAll();

        return Flux.fromIterable(endPoints)
                .flatMap(endPoint ->
                        getApi((ApiEndpoint) endPoints)
                                .doOnNext(response -> System.out.println("API 응답:" + response))
                                .doOnError(error -> System.out.println("API 호출 오류:" + error.getMessage())));
    }

    public Mono<String> getApi(ApiEndpoint request) {

        ApiEndpoint apiEndpoint = apiEndpointRepository.findById(1L)
                .orElseThrow(() -> new ApiEndPointException(ErrorCode.INVALID_SERVICEKEY));

        //        String baseUrl = "http://apis.data.go.kr/B551210/supplyEstiValueList/getSupplyEstiValueList";
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiEndpoint.getUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(apiEndpoint.getUrl())
                .build();

//        String serviceKey = "R3fWxDee7P9ysC5ty%2B6Y7LbJyFTiH0ToWmOtlRCJVUdWYd1kAkDzzTS9RA6Mn8Ikq0GYE1eEu462kax9JgnaNw%3D%3D";
//        String openYr = "2022";

        URI encodedServiceKey;

        try {
            encodedServiceKey = new URI(apiEndpoint.getServiceKey());
        } catch (URISyntaxException e) {
            throw new ApiEndPointException(ErrorCode.INVALID_SERVICEKEY);
        }


        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", encodedServiceKey)
                        .queryParam("openYr", apiEndpoint.getQuery())
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }


    public Mono<String> getApi2() throws URISyntaxException {

        String baseUrl = "http://apis.data.go.kr/B551210/supplyValuePerformanceList/getSupplyValuePerformanceList";
//        String serviceKey = "R3fWxDee7P9ysC5ty+6Y7LbJyFTiH0ToWmOtlRCJVUdWYd1kAkDzzTS9RA6Mn8Ikq0GYE1eEu462kax9JgnaNw==";
        String serviceKey = "R3fWxDee7P9ysC5ty%2B6Y7LbJyFTiH0ToWmOtlRCJVUdWYd1kAkDzzTS9RA6Mn8Ikq0GYE1eEu462kax9JgnaNw%3D%3D";

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(baseUrl)
                .build();

        URI uri = new URI(serviceKey);

        Mono jxh = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", uri)
                        .queryParam("openYr", 2023)
                        .queryParam("openMm", "05")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
        System.out.println("mono" + jxh.map((ss) -> ss.toString().toString()));
        return jxh;

    }


    public ApiEndpoint createApi(CreateApiEndpointDTO.Request request) {

        Map<String, String> query = new HashMap<>();

        for (List<String> pair : request.getParam()) {
            if (pair.size() == 2) {
                String key = pair.get(0);
                String value = pair.get(1);
                query.put(key, value);
            } else {
                System.out.println("잘못된 형식의 param: " + pair);
            }
        }

        return apiEndpointRepository.save(ApiEndpoint.builder()
                .url(request.getUrl())
                        .serviceKey(request.getServiceKey())
                        .query(query)
                .build());
    }
}
