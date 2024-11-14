package apiMonitering.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    ENDPOINT_ID_NOT_FOUND("저장된 API_ID가 없습니다."),
    INVALID_SERVICEKEY("잘못된 서비스키입니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다.");

    private final String Description;
}
