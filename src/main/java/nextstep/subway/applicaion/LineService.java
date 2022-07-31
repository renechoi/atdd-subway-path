package nextstep.subway.applicaion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nextstep.subway.applicaion.dto.LineRequest;
import nextstep.subway.applicaion.dto.LineResponse;
import nextstep.subway.applicaion.dto.SectionRequest;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Sections;
import nextstep.subway.domain.Station;

@Service
@Transactional(readOnly = true)
public class LineService {
	private final LineRepository lineRepository;
	private final StationService stationService;

	public LineService(LineRepository lineRepository, StationService stationService) {
		this.lineRepository = lineRepository;
		this.stationService = stationService;
	}

	@Transactional
	public LineResponse saveLine(LineRequest request) {
		Line line = lineRepository.save(new Line(request.getName(), request.getColor()));
		addLine(request, line);
		return LineResponse.from(line);
	}

	private void addLine(LineRequest request, Line line) {
		if (isAddable(request)) {
			Station upStation = stationService.findById(request.getUpStationId());
			Station downStation = stationService.findById(request.getDownStationId());
			line.addSection(new Section(line, upStation, downStation, request.getDistance()));
		}
	}

	private boolean isAddable(LineRequest request) {
		return request.getUpStationId() != null && request.getDownStationId() != null && request.getDistance() != 0;
	}

	public List<LineResponse> showLines() {
		return lineRepository.findAll().stream()
			.map(LineResponse::from)
			.collect(Collectors.toList());
	}

	public LineResponse findById(Long id) {
		return LineResponse.from(getLineById(id));
	}

	private Line getLineById(Long id) {
		return lineRepository.findById(id).orElseThrow(IllegalArgumentException::new);
	}

	@Transactional
	public void updateLine(Long id, LineRequest lineRequest) {
		Line line = getLineById(id);
		line.update(lineRequest.getName(), lineRequest.getColor());
	}

	@Transactional
	public void deleteLine(Long id) {
		lineRepository.deleteById(id);
	}

	@Transactional
	public void addSection(Long lineId, SectionRequest sectionRequest) {
		Station upStation = stationService.findById(sectionRequest.getUpStationId());
		Station downStation = stationService.findById(sectionRequest.getDownStationId());
		Line line = getLineById(lineId);

		line.addSection(new Section(line, upStation, downStation, sectionRequest.getDistance()));
	}

	@Transactional
	public void deleteSection(Long lineId, Long stationId) {
		Line line = getLineById(lineId);
		Station station = stationService.findById(stationId);
		Sections sections = Sections.from(line.getSections());
		sections.deleteSection(line, station);
	}
}
