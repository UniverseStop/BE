package be.busstop.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StaticResponseDto {
    // 나이
    private Long tenCnt;
    private Long twentyCnt;
    private Long thirtyCnt;
    private Long fortyCnt;
    private Long fiftyCnt;
    private Long sixtyCnt;
    private Long ageEtcCnt;

    // 카테고리
    private Long eatsCnt;
    private Long cultureCnt;
    private Long exerciseCnt;
    private Long studyCnt;
    private Long categoryEtcCnt;

    // 성별
    private Long maleCnt;
    private Long femaleCnt;
    private Long genderEtcCnt;

    // 방문자수
    private Long todayCnt;
    private Long weekCnt;
}