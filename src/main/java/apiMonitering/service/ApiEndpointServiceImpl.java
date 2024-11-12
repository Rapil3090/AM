package apiMonitering.service;

import apiMonitering.dto.create.CreateApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.exception.ApiEndPointException;
import apiMonitering.repository.ApiEndpointRepository;
import apiMonitering.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
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

    @Scheduled(fixedRate = 10000)
    public void scheduledApiCall() {

        Long id = 7L;
        System.out.println("출력 완료");
        getApi(id)
                .subscribe(response -> System.out.println("응답" + response),
                        error -> System.out.println("에러" + error.getMessage()));
    }


    public Mono<String> getApi(Long id) {

        ApiEndpoint apiEndpoint =apiEndpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("에러발생"));

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiEndpoint.getUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        Map<String, String> queryParams = apiEndpoint.getQuery();

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(apiEndpoint.getUrl())
                .build();

        URI encodedServiceKey;

        try {
            encodedServiceKey = new URI(apiEndpoint.getServiceKey());
        } catch (URISyntaxException e) {
            throw new ApiEndPointException(ErrorCode.INVALID_SERVICEKEY);
        }

        return webClient.get()
                .uri(uriBuilder -> {
                    queryParams.forEach(uriBuilder::queryParam);
                    uriBuilder.queryParam("serviceKey", encodedServiceKey);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("응답" + response))
                .doOnError(error -> System.out.println("에러" + error.getMessage()));
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
