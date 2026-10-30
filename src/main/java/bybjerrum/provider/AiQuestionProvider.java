package bybjerrum.provider;

import bybjerrum.dto.ProviderQuestionDTO;

import java.util.List;

// Used later when AI is implemented to fetch/generate questions
public class AiQuestionProvider implements QuestionProvider {

    @Override
    public List<ProviderQuestionDTO> fetchQuestions() {
        return null;
    }
}