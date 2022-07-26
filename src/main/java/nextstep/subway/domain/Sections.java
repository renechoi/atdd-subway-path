package nextstep.subway.domain;

import nextstep.subway.exception.SectionException;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class Sections {
    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public void add(Line line, Station upStation, Station downStation, int distance) {
        checkStationsExist(upStation, downStation);
        checkSameSectionExist(upStation, downStation);

        for (Section section : sections) {
            if (section.isUpStation(upStation)) {
                checkAvailableDistance(distance, section);
                sections.add(new Section(line, upStation, downStation, distance));
                sections.add(new Section(line, downStation, section.getDownStation(), section.getDistance() - distance));
                sections.remove(section);
                return;
            }

            if (section.isDownStation(downStation)) {
                checkAvailableDistance(distance, section);
                sections.add(new Section(line, upStation, downStation, distance));
                sections.add(new Section(line, section.getUpStation(), upStation, section.getDistance() - distance));
                sections.remove(section);
                return;
            }
        }

        for (Section section : sections) {
            if (section.isDownStation(upStation) || section.isUpStation(downStation)) {
                sections.add(new Section(line, upStation, downStation, distance));
                return;
            }
        }

        sections.add(new Section(line, upStation, downStation, distance));
    }

    private void checkSameSectionExist(Station upStation, Station downStation) {
        for (Section section : sections) {
            section.hasSameStations(upStation, downStation);
        }
    }

    public void checkStationsExist(Station upStation, Station downStation) {
        if (sections.isEmpty()) {
            return;
        }

        for (Section section : sections) {
            if (section.contains(upStation, downStation)) {
                return;
            }
        }

        throw new SectionException();
    }

    private void checkAvailableDistance(int distance, Section section) {
        if (section.getDistance() <= distance) {
            throw new SectionException();
        }
    }

    public List<Station> getStations() {
        List<Station> allStations = allStations();
        if (allStations.isEmpty()) {
            return Collections.emptyList();
        }

        Station upStation = findFirstStation(allStations);
        List<Station> stations = new ArrayList<>(List.of(upStation));

        while (isNotLastStation(upStation)) {
            upStation = findNextStation(upStation, stations);
        }

        return stations;
    }

    private Station findNextStation(Station upStation, List<Station> stations) {
        for (Section section : sections) {
            if (section.isUpStation(upStation)) {
                stations.add(section.getDownStation());
                upStation = section.getDownStation();
                break;
            }
        }
        return upStation;
    }

    private boolean isNotLastStation(Station firstStation) {
        for (Section section : sections) {
            if (section.isUpStation(firstStation)) {
                return true;
            }
        }
        return false;
    }

    private List<Station> allStations() {
        if (sections.isEmpty()) {
            return Collections.emptyList();
        }

        List<Station> stations = new ArrayList<>();
        for (Section section : sections) {
            stations.add(section.getUpStation());
            stations.add(section.getDownStation());
        }

        return stations.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private Station findFirstStation(List<Station> stations) {
        for (Station station : stations) {
            if (isFirstStation(station)) {
                return station;
            }
        }
        throw new SectionException();
    }

    private boolean isFirstStation(Station station) {
        for (Section section : sections) {
            if (section.isDownStation(station)) {
                return false;
            }
        }
        return true;
    }

    public void remove(Station station) {
        if (isNotLastDownStation(station)) {
            throw new IllegalArgumentException();
        }

        sections.remove(sections.size() - 1);
    }

    private boolean isNotLastDownStation(Station station) {
        List<Station> stations = getStations();
        return !stations.get(stations.size() - 1).equals(station);
    }
}
