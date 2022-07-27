package nextstep.subway.unit;

import java.util.List;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Sections;
import nextstep.subway.domain.Station;
import nextstep.subway.exception.SectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class SectionsTest {

    Line 이호선;

    Station 강남역;
    Station 역삼역;
    Station 선릉역;
    Station 삼성역;

    @BeforeEach
    void setup() {
        이호선 = new Line("2호선", "bg-green-600");

        강남역 = new Station(1L, "강남역");
        역삼역 = new Station(2L, "역삼역");
        선릉역 = new Station(3L, "선릉역");
        삼성역 = new Station(4L, "삼성역");
    }

    @Test
    @DisplayName("지하철 구간 등록합니다.")
    void addSection() {
        Section 강남_역삼_구간 = new Section(이호선, 강남역, 역삼역, 6);
        Sections 구간 = new Sections();

        구간.addSection(강남_역삼_구간);

        assertThat(구간.getSections()).isEqualTo(List.of(강남_역삼_구간));
    }

    @Test
    @DisplayName("지하철역 조회하기")
    void getStations() {
        Section 강남_역삼_구간 = new Section(이호선, 강남역, 역삼역, 6);

        Sections 구간 = new Sections();
        구간.addSection(강남_역삼_구간);

        List<Station> 지하철역들 = 구간.getStations();

        assertThat(지하철역들).isEqualTo(List.of(강남역, 역삼역));
    }

    @Test
    @DisplayName("지하철역 삭제")
    void deleteStation() {
        Section 강남_역삼_구간 = new Section(이호선, 강남역, 역삼역, 6);
        Section 역삼_선릉_구간 = new Section(이호선, 역삼역, 선릉역, 10);

        Sections 구간 = new Sections();
        구간.addSection(강남_역삼_구간);
        구간.addSection(역삼_선릉_구간);
        구간.removeStation(역삼역);

        assertThat(구간.getStations()).containsExactly(강남역, 선릉역);
    }

    @Test
    @DisplayName("존재하지 않는 지하철역을 삭제 시도시 에러를 반환합니다.")
    void isNotExistsStation() {
        Section 강남_역삼_구간 = new Section(이호선, 강남역, 역삼역, 6);
        Section 역삼_선릉_구간 = new Section(이호선, 역삼역, 선릉역, 10);

        Sections 구간 = new Sections();
        구간.addSection(강남_역삼_구간);
        구간.addSection(역삼_선릉_구간);

        assertThatExceptionOfType(SectionException.class).isThrownBy(() -> {
            구간.removeStation(삼성역);
        })
            .withMessage("존재하지 않는 지하철역이라 삭제할 수가 없습니다.");
    }

    @Test
    @DisplayName("구간이 하나일때는 삭제할 수 없습니다.")
    void removeStationException() {
        Section 강남_역삼_구간 = new Section(이호선, 강남역, 역삼역, 6);

        Sections 구간 = new Sections();
        구간.addSection(강남_역삼_구간);

        assertThatExceptionOfType(SectionException.class).isThrownBy(() -> {
                구간.removeStation(강남역);
            })
            .withMessage("구간이 하나일때는 삭제할 수 없습니다.");
    }

}
