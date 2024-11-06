package apiMonitering.service;

import apiMonitering.domain.ApiEndpoint;
import apiMonitering.domain.ApiResponse;
import apiMonitering.repository.ApiEndpointRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiEndpointServiceImpl implements ApiEndpointService {

    private final ApiEndpointRepository apiEndpointRepository;


    public Mono<ApiResponse> getApi() {

        WebClient webClient = WebClient.create();

        String decodeServiceKey = URLDecoder.decode("\t\n" +
                "R3fWxDee7P9ysC5ty+6Y7LbJyFTiH0ToWmOtlRCJVUdWYd1kAkDzzTS9RA6Mn8Ikq0GYE1eEu462kax9JgnaNw==", StandardCharsets.UTF_8);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/1371033/mmcadensity/congestion")
                        .queryParam("serviceKey", decodeServiceKey)
                        .queryParam("spaceCode", "MMCA-SPACE-1001")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(xml -> {
                    try {
                        XmlMapper xmlMapper = new XmlMapper();
                        return xmlMapper.readValue(xml, ApiResponse.class);
                    } catch (Exception e) {
                        throw new RuntimeException("실패", e);
                    }
                });
    }

    public void callApiAndPrintResponse() {
        getApi().subscribe(
                apiResponse -> System.out.println("응답 데이터: " + apiResponse)

        );
    }

}
