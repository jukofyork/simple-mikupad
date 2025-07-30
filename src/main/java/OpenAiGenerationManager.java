import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

public class OpenAiGenerationManager extends BaseGenerationManager {

	public OpenAiGenerationManager(SimpleMikuPad app) {
		super(app);
	}

	@Override
	protected String getCompletionEndpoint() {
		return "/v1/completions";
	}

	@Override
	protected void tokenizePrompt(String endpoint, String apiKey, String prompt) throws Exception {
		// No-op - OpenAI doesn't support tokenization
	}

	@Override
	protected JsonObject extractTokenData(JsonObject tokenResponse) {
		JsonArray completionChoices = tokenResponse.getAsJsonArray("choices");
		JsonObject primaryCompletion = completionChoices.get(0).getAsJsonObject();
		if (primaryCompletion.get("logprobs").isJsonNull()) {
			return null;
		}
		JsonObject logProbabilityData = primaryCompletion.getAsJsonObject("logprobs");
		JsonArray logProbabilityEntries = logProbabilityData.getAsJsonArray("content");
		JsonObject currentTokenLogprobs = logProbabilityEntries.get(logProbabilityEntries.size() - 1).getAsJsonObject();
		return currentTokenLogprobs;
	}

	@Override
	protected String getTokenText(JsonObject tokenLogprobs) {
		return tokenLogprobs.get("token").getAsString();
	}

	@Override
	protected double getTokenProbability(JsonObject tokenLogprobs) {
		double logProbability = tokenLogprobs.get("logprob").getAsDouble();
		return Math.exp(logProbability);
	}

	@Override
	protected List<TokenManager.TokenAlternative> getTokenAlternatives(JsonObject tokenLogprobs) {
		if (tokenLogprobs.has("top_logprobs")) {
			JsonArray alternativeLogprobs = tokenLogprobs.getAsJsonArray("top_logprobs");
			return buildAlternativesFromLogprobs(alternativeLogprobs);
		}
		return new java.util.ArrayList<>();
	}

	@Override
	protected String extractCompletionReason(JsonObject tokenResponse) {
		if (tokenResponse.has("choices")) {
			JsonArray choices = tokenResponse.getAsJsonArray("choices");
			JsonObject choice = choices.get(0).getAsJsonObject();
			if (choice.has("finish_reason") && !choice.get("finish_reason").isJsonNull()) {
				return choice.get("finish_reason").getAsString();
			}
		}
		return null;
	}
}