package apiMonitering.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    ENDPOINT_ID_NOT_FOUND("저장된 API_ID가 없습니다."),
    INVALID_SERVICEKEY("잘못된 서비스키입니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    CLIENT_ERROR("클라이언트 요청 오류"),
    SERVER_ERROR("서버 오류 발생"),
    UNKNOWN_ERROR("알 수 없는 오류 발생"),
    RETRY_EXHAUSTED("재시도 횟수를 모두 소모했습니다."),
    TIMEOUT_ERROR("요청시간이 초과되었습니다"),
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다.");

    private final String Description;
}
