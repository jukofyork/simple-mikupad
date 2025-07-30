import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BaseGenerationManager {

	protected SimpleMikuPad app;
	protected volatile boolean isCancelled = false;

	public BaseGenerationManager(SimpleMikuPad app) {
		this.app = app;
	}

	public void setupEventListeners() {
		if (app.getGenerateButton() == null || app.getCancelButton() == null) {
			return; // UI not ready yet
		}

		app.getGenerateButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				generateCompletion();
			}
		});

		app.getCancelButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelGeneration();
			}
		});
	}

	public void generateCompletion() {
		isCancelled = false;
		app.getGenerateButton().setEnabled(false);
		app.getCancelButton().setEnabled(true);
		app.getTokenManager().clearTokenColoring();

		String endpoint = app.getEndpointText().getText().trim();
		String apiKey = app.getApiKeyText().getText().trim();
		String model = app.getModelText().getText().trim();
		String prompt = app.getPromptText().getText();

		Session currentSession = app.getSessionManager().getCurrentSession();
		SamplingParameters samplingParams = currentSession.getSamplingParams();
		AdvancedSettings advancedSettings = currentSession.getAdvancedSettings();

		ProbabilitySettings probabilitySettings = new ProbabilitySettings(Constants.DEFAULT_TOKEN_ALTERNATIVES_COUNT,
				false, false, 0);

		CompletableFuture.runAsync(() -> {
			try {
				app.getDisplay().asyncExec(() -> {
					app.updateStatus("Tokenizing prompt...");
				});

				tokenizePrompt(endpoint, apiKey, prompt);

				if (isCancelled)
					return;

				app.getDisplay().asyncExec(() -> {
					app.updateStatus("Generating completion...");
				});

				JsonObject request = buildRequest(prompt, samplingParams, advancedSettings, probabilitySettings);

				if (!model.isEmpty()) {
					request.addProperty("model", model);
				}

				String requestBody = new Gson().toJson(request);

				URI uri = URI.create(endpoint + getCompletionEndpoint());
				HttpResponse<InputStream> response = app.getHttpClient().sendRequest(uri,
						apiKey.isEmpty() ? null : apiKey, requestBody, true);

				if (isCancelled)
					return;

				processStreamingResponse(response);

				app.getDisplay().asyncExec(() -> {
					if (!isCancelled) {
						app.updateStatus("Generation completed");
						SessionUIManager sessionUI = new SessionUIManager(app);
						sessionUI.saveCurrentSessionState();
					}
					resetButtons();
				});

			} catch (Exception ex) {
				app.getDisplay().asyncExec(() -> {
					app.updateStatus("Error: " + ex.getMessage());
					resetButtons();
				});
			}
		});
	}

	public void cancelGeneration() {
		isCancelled = true;
		app.updateStatus("Cancelled");
		resetButtons();
	}

	protected void resetButtons() {
		app.getGenerateButton().setEnabled(true);
		app.getCancelButton().setEnabled(false);
	}

	protected void processStreamingResponse(HttpResponse<InputStream> response) throws Exception {
		try (java.io.BufferedReader reader = new java.io.BufferedReader(
				new java.io.InputStreamReader(response.body()))) {
			String responseLine;
			while ((responseLine = reader.readLine()) != null && !isCancelled) {
				processStreamingChunk(responseLine);
			}
		}
	}

	protected void processStreamingChunk(String responseLine) {
		if (responseLine.startsWith("data: ")) {
			String eventData = responseLine.substring(6);
			if ("[DONE]".equals(eventData)) {
				return;
			}

			try {
				JsonObject tokenResponse = JsonParser.parseString(eventData).getAsJsonObject();
				JsonObject tokenLogprobs = extractTokenData(tokenResponse);
				
				if (tokenLogprobs != null) {
					String tokenText = getTokenText(tokenLogprobs);
					
					if (tokenText != null && !tokenText.isEmpty()) {
						double probability = getTokenProbability(tokenLogprobs);
						List<TokenManager.TokenAlternative> alternatives = getTokenAlternatives(tokenLogprobs);

						app.getDisplay().asyncExec(() -> {
							if (!isCancelled && !app.getPromptText().isDisposed()) {
								app.getTokenManager().appendSingleToken(tokenText, probability, alternatives);
								app.scrollToBottom();
							}
						});
					}
				}
			} catch (Exception e) {
				System.err.println("Error parsing token response: " + e.getMessage());
			}
		}
	}

	protected JsonObject buildRequest(String prompt, SamplingParameters params, AdvancedSettings advancedSettings,
			ProbabilitySettings probSettings) {
		JsonObject request = params.toJson(advancedSettings);
		request.addProperty("prompt", prompt);
		request.addProperty("stream", true);
		probSettings.addToRequest(request);
		return request;
	}

	protected List<TokenManager.TokenAlternative> buildAlternativesFromLogprobs(JsonArray topLogprobs) {
		List<TokenManager.TokenAlternative> alternatives = new ArrayList<>();

		for (int i = 0; i < topLogprobs.size(); i++) {
			JsonObject alternativeToken = topLogprobs.get(i).getAsJsonObject();
			String tokenText = alternativeToken.get("token").getAsString();
			double logProbability = alternativeToken.get("logprob").getAsDouble();
			double probability = Math.exp(logProbability);
			alternatives.add(new TokenManager.TokenAlternative(tokenText, probability));
		}

		alternatives.sort((a, b) -> Double.compare(b.probability, a.probability));
		return alternatives;
	}

	protected abstract String getCompletionEndpoint();
	protected abstract void tokenizePrompt(String endpoint, String apiKey, String prompt) throws Exception;
	protected abstract JsonObject extractTokenData(JsonObject tokenResponse);
	protected abstract String getTokenText(JsonObject tokenLogprobs);
	protected abstract double getTokenProbability(JsonObject tokenLogprobs);
	protected abstract List<TokenManager.TokenAlternative> getTokenAlternatives(JsonObject tokenLogprobs);
}