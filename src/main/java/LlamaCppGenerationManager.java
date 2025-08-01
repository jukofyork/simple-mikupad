import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpResponse;
import java.io.InputStream;
import java.util.List;

public class LlamaCppGenerationManager extends BaseGenerationManager {

	public LlamaCppGenerationManager(SimpleMikuPad app) {
		super(app);
	}

	@Override
	protected String getCompletionEndpoint() {
		return "/completion";
	}

	@Override
	protected void tokenizePrompt(String endpoint, String apiKey, String prompt) throws Exception {
		JsonObject tokenizeRequest = new JsonObject();
		tokenizeRequest.addProperty("content", prompt);
		tokenizeRequest.addProperty("with_pieces", true);
	
		String requestBody = new Gson().toJson(tokenizeRequest);
	
		URI uri = URI.create(endpoint + "/tokenize");
		HttpResponse<InputStream> response = app.getHttpClient().sendRequest(uri, apiKey.isEmpty() ? null : apiKey,
				requestBody, false);
	
		if (isCancelled)
			return;
	
		String responseBody;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
			StringBuilder responseBuilder = new StringBuilder();
			String responseLine;
			while ((responseLine = reader.readLine()) != null) {
				responseBuilder.append(responseLine);
			}
			responseBody = responseBuilder.toString();
		}
	
	    JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
	    JsonArray tokenArray = responseJson.getAsJsonArray("tokens");
	
	    String remainingText = prompt;
	    int currentOffset = 0;
	
	    for (int i = 0; i < tokenArray.size(); i++) {
	        JsonElement tokenEntry = tokenArray.get(i);
	
	        if (tokenEntry.isJsonObject()) {
	            JsonObject tokenMetadata = tokenEntry.getAsJsonObject();
	
	            if (tokenMetadata.has("piece")) {
	                JsonElement tokenPart = tokenMetadata.get("piece");
	                
	                // Skip byte-array tokens (split Unicode characters)
	                if (tokenPart.isJsonArray()) {
	                    continue; // Skip this token entirely
	                }
	                
	                // Only process string tokens
	                String tokenText = tokenPart.getAsString();
	                if (tokenText != null && !tokenText.isEmpty()) {
	                    // Find this token in the remaining text
	                    int tokenPosition = remainingText.indexOf(tokenText);
	                    if (tokenPosition >= 0) {
	                        final int startOffset = currentOffset + tokenPosition;
	                        final int tokenLength = tokenText.length();
	                        final int tokenIndex = i;
	                        final String finalTokenText = tokenText;
	                        
	                        app.getDisplay().asyncExec(() -> {
	                            if (!isCancelled) {
	                                app.getTokenManager().showPromptToken(startOffset, tokenLength, tokenIndex);
	                                app.getTokenManager().storeTokenInfo(startOffset, finalTokenText);
	                            }
	                        });
	                        
	                        // Update position for next search
	                        int consumedLength = tokenPosition + tokenText.length();
	                        currentOffset += consumedLength;
	                        remainingText = remainingText.substring(consumedLength);
	                    }
	                }
	            }
	        }
	    }
	}

	@Override
	protected JsonObject extractTokenData(JsonObject tokenResponse) {
		// For LlamaCpp, the streaming response itself contains the token data
		return tokenResponse;
	}

	@Override
	protected String getTokenText(JsonObject tokenLogprobs) {
		return tokenLogprobs.get("content").getAsString();
	}

	@Override
	protected double getTokenProbability(JsonObject tokenLogprobs) {
		JsonArray completionProbabilities = tokenLogprobs.getAsJsonArray("completion_probabilities");
		JsonObject currentTokenProbabilities = completionProbabilities.get(0).getAsJsonObject();
		
		if (currentTokenProbabilities.has("prob")) {
			return currentTokenProbabilities.get("prob").getAsDouble();
		} else {
			double logProbability = currentTokenProbabilities.get("logprob").getAsDouble();
			return Math.exp(logProbability);
		}
	}

	@Override
	protected List<TokenManager.TokenAlternative> getTokenAlternatives(JsonObject tokenLogprobs) {
		if (tokenLogprobs.has("completion_probabilities")) {
			JsonArray completionProbabilities = tokenLogprobs.getAsJsonArray("completion_probabilities");
			if (completionProbabilities.size() > 0) {
				JsonObject currentTokenProbabilities = completionProbabilities.get(0).getAsJsonObject();

				if (currentTokenProbabilities.has("top_logprobs")) {
					JsonArray alternativeLogprobs = currentTokenProbabilities.getAsJsonArray("top_logprobs");
					return buildAlternativesFromLogprobs(alternativeLogprobs);
				}
			}
		}

		return new java.util.ArrayList<>();
	}

	@Override
	protected String extractCompletionReason(JsonObject tokenResponse) {
		if (tokenResponse.has("stop_type")) {
			return tokenResponse.get("stop_type").getAsString();
		}
		return null;
	}
}