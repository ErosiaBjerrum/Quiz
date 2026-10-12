package bybjerrum.provider;

import bybjerrum.dto.ProviderQuestionDTO;
import java.util.List;

public interface QuestionProvider {

    List<ProviderQuestionDTO> fetchQuestions();

}