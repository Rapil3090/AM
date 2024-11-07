package apiMonitering.service;

import apiMonitering.domain.ApiEndpoint;
import apiMonitering.domain.ApiResponse;
import apiMonitering.repository.ApiEndpointRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiEndpointServiceImpl implements ApiEndpointService {

    private final ApiEndpointRepository apiEndpointRepository;


    public Mono<String> getApi() throws URISyntaxException {

        String baseUrl = "http://apis.data.go.kr/B551210/supplyEstiValueList/getSupplyEstiValueList";
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(baseUrl)
                .build();

//        String serviceKey = "R3fWxDee7P9ysC5ty+6Y7LbJyFTiH0ToWmOtlRCJVUdWYd1kAkDzzTS9RA6Mn8Ikq0GYE1eEu462kax9JgnaNw==";
        String serviceKey = "R3fWxDee7P9ysC5ty%2B6Y7LbJyFTiH0ToWmOtlRCJVUdWYd1kAkDzzTS9RA6Mn8Ikq0GYE1eEu462kax9JgnaNw%3D%3D";
//        String baseUrl = "apis.data.go.kr/B551210/supplyEstiValueList";
        String endPoint = "/getSupplyEstiValueList";
        String openYr = "2022";
        URI uri = new URI(serviceKey);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", uri)
                        .queryParam("openYr", openYr)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }



}
