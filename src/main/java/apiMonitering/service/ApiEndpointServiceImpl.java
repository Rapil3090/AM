package apiMonitering.service;

import apiMonitering.DTO.ApiEndpointDTO.RequestApiEndpointDTO;
import apiMonitering.domain.ApiEndpoint;
import apiMonitering.domain.ApiResponse;
import apiMonitering.exception.ApiEndPointException;
import apiMonitering.repository.ApiEndpointRepository;
import apiMonitering.type.ErrorCode;
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


    public Mono<String> getApi() {



//        String baseUrl = "http://apis.data.go.kr/B551210/supplyEstiValueList/getSupplyEstiValueList";
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(request.getUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(request.getUrl())
                .build();

//        String serviceKey = "R3fWxDee7P9ysC5ty%2B6Y7LbJyFTiH0ToWmOtlRCJVUdWYd1kAkDzzTS9RA6Mn8Ikq0GYE1eEu462kax9JgnaNw%3D%3D";
//        String openYr = "2022";

        URI encodedServiceKey;

        try {
            encodedServiceKey = new URI(request.getServiceKey());
        } catch (URISyntaxException e) {
            throw new ApiEndPointException(ErrorCode.INVALID_SERVICEKEY);
        }


        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", encodedServiceKey)
                        .queryParam("openYr", request.getParam())
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }



}
