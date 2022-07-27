package nextstep.subway.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nextstep.subway.enums.SubwayErrorMessage;
import nextstep.subway.exception.SubwayException;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Sections {

    @OrderColumn
    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> values = new ArrayList<>();
    private static final int MIN_SIZE = 1;

    public void add(Section section) {
        if (values.isEmpty()) {
            values.add(section);
            return;
        }

        ensureCanAddition(section);

        if(canChangeMiddleSection(section)) {
            Section originSection = findSectionWithSameStation(section);
            originSection.changeSectionConditionBy(section);
        }

        values.add(section);
    }

    public void remove(Station station) {
        ensureCanRemove(station);

        if(!isEndPointStation(station)) {
            removeMiddleSection(station);
            return;
        }

        removeEndPointSection(station);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public List<Station> findConnectedStations() {
        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<Station> stations = new ArrayList<>();
        Station upStation = findUpTerminusStation();
        stations.add(upStation);

        while(!isDownTerminus(upStation)) {
            upStation = findNextStation(upStation);
            stations.add(upStation);
        }

        return stations;
    }

    public Station findNextStation(Station upStation) {
        return values.stream()
                     .filter(s -> s.getUpStation().equals(upStation))
                     .map(Section::getDownStation)
                     .findAny()
                     .orElseThrow(IllegalArgumentException::new);
    }

    public Section findSectionWithSameStation(Section section) {
        return  values.stream()
                      .filter(hasSameStationToOriginSection(section))
                      .findAny()
                      .orElseThrow(IllegalArgumentException::new);
    }

    public Station findUpTerminusStation() {
        return values.stream()
                     .map(Section::getUpStation)
                     .filter(this::isUpTerminus)
                     .findAny()
                     .orElseThrow(SubwayException::new);
    }

    private boolean canChangeMiddleSection(Section newSection) {
        return values.stream().anyMatch(hasSameStationToOriginSection(newSection));
    }

    private Predicate<Section> hasSameStationToOriginSection(Section newSection) {
        return s -> s.isEqualToUpStation(newSection.getUpStation())
                || s.isEqualToDownStation(newSection.getDownStation());
    }


    private boolean isUpTerminus(Station upStation) {
        return values.stream().noneMatch(s -> s.getDownStation().equals(upStation));
    }

    private boolean isDownTerminus(Station station) {
        return values.stream().noneMatch( s -> s.getUpStation().equals(station));
    }

    private boolean existAllStation(Section section) {
        return new HashSet<>(findConnectedStations()).containsAll(section.getStations());
   }


    private void ensureCanRemove(Station station) {
        if (values.size() <= MIN_SIZE) {
            throw new IllegalArgumentException(SubwayErrorMessage.MUST_BE_REGISTERED_TWO_SECTION.getMessage());
        }
        if(notExistStationOfSection(s -> s.equals(station))) {
            throw new IllegalArgumentException(SubwayErrorMessage.NOT_EXIST_STATION_OF_SECTION.getMessage());
        }
    }

    private void ensureCanAddition(Section section) {
        if (existAllStation(section)) {
            throw new IllegalArgumentException(SubwayErrorMessage.EXIST_SECTION.getMessage());
        }
        if (notExistStationOfSection(containsStationOfSection(section))) {
            throw new IllegalArgumentException(SubwayErrorMessage.NOT_EXIST_STATION_OF_SECTION.getMessage());
        }
    }

    private Predicate<Station> containsStationOfSection(Section section) {
        return s -> s.equals(section.getUpStation()) || s.equals(section.getDownStation());
    }

    private boolean notExistStationOfSection(Predicate<Station> isMach) {
        return findAllStations().stream().noneMatch(isMach);
    }

    private List<Station> findAllStations() {
        List<Station> stations = values.stream()
                                       .map(Section::getUpStation)
                                       .collect(Collectors.toList());

        Section downTerminus = getDownTerminus();
        stations.add(downTerminus.getDownStation());
        return stations;
    }

    private Section getDownTerminus() {
        return values.stream()
                     .filter(s -> isDownTerminus(s.getDownStation()))
                     .findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }

    private boolean isEndPointStation(Station station) {
        return values.stream().anyMatch(s -> isUpTerminus(station) || isDownTerminus(station));
    }

    private Section findConnectedSectionBy(Predicate<Section> isMachStation) {
        return values.stream().filter(isMachStation)
                     .findAny()
                     .orElseThrow(IllegalArgumentException::new);
    }

    private void removeMiddleSection(Station station) {
        Section findSameDownStationSection = findConnectedSectionBy(s -> s.isEqualToDownStation(station));
        Section findSameUpStationSection = findConnectedSectionBy(s -> s.isEqualToUpStation(station));
        findSameDownStationSection.changeDownStation(findSameUpStationSection.getDownStation());
        findSameDownStationSection.addDistance(findSameUpStationSection.getDistance());
        values.remove(findSameUpStationSection);
    }

    private void removeEndPointSection(Station station) {
        values.remove(
                findConnectedSectionBy(
                        s -> s.isEqualToDownStation(station) ||  s.isEqualToUpStation(station))
        );
    }


}
