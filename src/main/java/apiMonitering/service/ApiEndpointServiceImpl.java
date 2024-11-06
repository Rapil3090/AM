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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiEndpointServiceImpl implements ApiEndpointService {

    private final ApiEndpointRepository apiEndpointRepository;


    public Mono<String> getApi(Model model) {

        String baseUrl = "http://apis.data.go.kr/B551210/supplyEstiValueList/getSupplyEstiValueList";
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(baseUrl)
                .build();

        String serviceKey = "R3fWxDee7P9ysC5ty+6Y7LbJyFTiH0ToWmOtlRCJVUdWYd1kAkDzzTS9RA6Mn8Ikq0GYE1eEu462kax9JgnaNw==";
//        String baseUrl = "apis.data.go.kr/B551210/supplyEstiValueList";
        String endPoint = "/getSupplyEstiValueList";
        String openYr = "2022";

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("openYr", openYr)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                        .map(response -> {
                            model.addAttribute("response", response);
                            return "test/test";
                        });
    }



}
