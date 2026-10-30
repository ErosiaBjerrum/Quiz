package bybjerrum.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TdbResponseDTO {

    private int response_code;
    private List<QuestionTdbDTO> results;
}