package nextstep.subway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import nextstep.subway.domain.exception.NotValidDeleteTargetStationException;
import nextstep.subway.domain.exception.NotValidSectionDistanceException;
import nextstep.subway.domain.exception.NotValidSectionStationsException;
import nextstep.subway.domain.exception.StationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SectionsTest {

    private Sections sections;

    private Line 분당선;
    private Station 청량리역;
    private Station 왕십리역;

    @BeforeEach
    void setUp() {
        분당선 = new Line("분당선", "yellow");
        청량리역 = new Station("청량리역");
        왕십리역 = new Station("왕십리역");
        sections = new Sections();
        sections.add(분당선, 청량리역, 왕십리역, 10);
    }

    @DisplayName("마지막 구간 추가")
    @Test
    void addLastSection() {
        var 서울숲역 = new Station("서울숲역");
        var distance = 10;
        sections.add(분당선, 왕십리역, 서울숲역, distance);

        var sectionList = sections.getOrderedSections();
        var lastSection = sectionList.get(sectionList.size() - 1);
        assertAll(
                () -> 구간_검증(lastSection, 왕십리역, 서울숲역, distance),
                () -> 역_순서_검증(sections, List.of(청량리역, 왕십리역, 서울숲역))
        );
    }

    @DisplayName("첫 구간 추가")
    @Test
    void addFirstSection() {
        var 새로운역 = new Station("새로운역");
        var distance = 10;
        sections.add(분당선, 새로운역, 청량리역, distance);

        var firstSection = sections.getOrderedSections().get(0);
        assertAll(
                () -> 구간_검증(firstSection, 새로운역, 청량리역, distance),
                () -> 역_순서_검증(sections, List.of(새로운역, 청량리역, 왕십리역))
        );
    }

    @DisplayName("구간 사이에 새 구간 추가 (하행역이 신규역)")
    @Test
    void addSectionWithNewDownStationInMiddle() {
        var 중간역 = new Station("중간역");
        sections.add(분당선, 청량리역, 중간역, 5);

        var sectionList = sections.getOrderedSections();
        var newSection = sectionList.get(0);
        var updatedSection = sectionList.get(1);
        assertAll(
                () -> 구간_검증(newSection, 청량리역, 중간역, 5),
                () -> 구간_검증(updatedSection, 중간역, 왕십리역, 5),
                () -> 역_순서_검증(sections, List.of(청량리역, 중간역, 왕십리역))
        );
    }

    @DisplayName("구간 사이에 새 구간 추가 (상행역이 신규역)")
    @Test
    void addSectionWithNewUpStationInMiddle() {
        var 중간역 = new Station("중간역");
        sections.add(분당선, 중간역, 왕십리역, 5);

        var sectionList = sections.getOrderedSections();
        var newSection = sectionList.get(1);
        var updatedSection = sectionList.get(0);
        assertAll(
                () -> 구간_검증(newSection, 중간역, 왕십리역, 5),
                () -> 구간_검증(updatedSection, 청량리역, 중간역, 5),
                () -> 역_순서_검증(sections, List.of(청량리역, 중간역, 왕십리역))
        );
    }

    @ParameterizedTest(name = "구간 사이의 새 구간의 거리가 기존 구간보다 크거나 같으면 추가 실패 / distance = {0}")
    @ValueSource(ints = {10, 15})
    void sectionAdditionFailsWhenDistanceOfNewSectionInMiddleIsGreater(int distance) {
        var 중간역 = new Station("중간역");

        assertThrows(NotValidSectionDistanceException.class, () -> sections.add(분당선, 청량리역, 중간역, distance));
    }

    @DisplayName("구간의 상하행역이 모두 노선에 존재하지 않으면 추가 실패")
    @Test
    void sectionAdditionFailsWhenNeitherUpAndDownStationNotExist() {
        var 새로운역 = new Station("새로운역");
        var 다른새로운역 = new Station("다른새로운역");

        assertThrows(NotValidSectionStationsException.class, () -> sections.add(분당선, 새로운역, 다른새로운역, 10));
    }

    @DisplayName("마지막 구간 제거")
    @Test
    void removeSectionByLastStation() {
        var 서울숲역 = new Station("서울숲역");
        sections.add(분당선, 왕십리역, 서울숲역, 10);

        sections.removeByStation(서울숲역);

        var sectionList = sections.getOrderedSections();
        assertAll(
                () -> 구간_검증(sectionList.get(0), 청량리역, 왕십리역, 10),
                () -> 역_순서_검증(sections, List.of(청량리역, 왕십리역))
        );
    }

    @DisplayName("첫 번째 역으로 구간 제거")
    @Test
    void removeSectionByFirstStation() {
        var 서울숲역 = new Station("서울숲역");
        sections.add(분당선, 왕십리역, 서울숲역, 10);

        sections.removeByStation(청량리역);

        var sectionList = sections.getOrderedSections();
        assertAll(
                () -> 구간_검증(sectionList.get(0), 왕십리역, 서울숲역, 10),
                () -> 역_순서_검증(sections, List.of(왕십리역, 서울숲역))
        );
    }

    @DisplayName("중간역으로 구간 제거")
    @Test
    void removeSectionByMiddleStation() {
        var 서울숲역 = new Station("서울숲역");
        sections.add(분당선, 왕십리역, 서울숲역, 10);

        sections.removeByStation(왕십리역);

        var sectionList = sections.getOrderedSections();
        assertAll(
                () -> 구간_검증(sectionList.get(0), 청량리역, 서울숲역, 20),
                () -> 역_순서_검증(sections, List.of(청량리역, 서울숲역))
        );
    }

    @DisplayName("마지막 남은 구간의 역 제거시 실패 (상행역)")
    @Test
    void removeLastRemainSectionByUpStationFails() {
        assertThrows(NotValidDeleteTargetStationException.class, () -> sections.removeByStation(청량리역));
    }

    @DisplayName("마지막 남은 구간의 역 제거시 실패 (하행역)")
    @Test
    void removeLastRemainSectionByDownStationFails() {
        assertThrows(NotValidDeleteTargetStationException.class, () -> sections.removeByStation(왕십리역));
    }

    @DisplayName("존재하지 않는 역으로 구간 제거 실패")
    @Test
    void removeWithStationNotInLineFails() {
        var 서울숲역 = new Station("서울숲역");
        sections.add(분당선, 왕십리역, 서울숲역, 10);

        var 뉴욕역 = new Station("뉴욕역");
        assertThrows(StationNotFoundException.class, () -> sections.removeByStation(뉴욕역));
    }

    private void 역_순서_검증(Sections sections, List<Station> stations) {
        assertThat(sections.getStations()).containsExactlyElementsOf(stations);
    }

    private void 구간_검증(Section section, Station upStation, Station downStation, Integer distance) {
        assertAll(
                () -> assertThat(section.getUpStation()).isEqualTo(upStation),
                () -> assertThat(section.getDownStation()).isEqualTo(downStation),
                () -> assertThat(section.getDistance()).isEqualTo(distance)
        );
    }
}