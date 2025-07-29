import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GenerationManager {
    
    private SimpleMikuPad app;
    private volatile boolean isCancelled = false;
    
    public GenerationManager(SimpleMikuPad app) {
        this.app = app;
        // Don't setup listeners in constructor
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
        
        app.updateStatus("Generating completion...");
        
        CompletableFuture.runAsync(() -> {
            try {
                JsonObject request = samplingParams.toJson();
                request.addProperty("prompt", prompt);
                request.addProperty("stream", true);
                request.addProperty("logprobs", 10);
                
                if (!model.isEmpty()) {
                    request.addProperty("model", model);
                }
                
                String requestBody = new Gson().toJson(request);
                
                URI uri = URI.create(endpoint + "/v1/completions");
                HttpResponse<InputStream> response = app.getHttpClient().sendRequest(
                    uri, 
                    apiKey.isEmpty() ? null : apiKey, 
                    requestBody, 
                    true
                );
                
                if (isCancelled) return;
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                    String line;
                    while ((line = reader.readLine()) != null && !isCancelled) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            
                            try {
                                JsonObject chunk = JsonParser.parseString(data).getAsJsonObject();
                                if (chunk.has("choices") && chunk.getAsJsonArray("choices").size() > 0) {
                                    JsonObject choice = chunk.getAsJsonArray("choices").get(0).getAsJsonObject();
                                    
                                    if (choice.has("text")) {
                                        String token = choice.get("text").getAsString();
                                        double probability = extractProbability(choice);
                                        List<TokenManager.TokenAlternative> alternatives = extractAlternatives(choice);
                                        
                                        final String finalToken = token;
                                        final double finalProbability = probability;
                                        final List<TokenManager.TokenAlternative> finalAlternatives = alternatives;
                                        app.getDisplay().asyncExec(() -> {
                                            if (!isCancelled && !app.getPromptText().isDisposed()) {
                                                app.getTokenManager().appendSingleToken(finalToken, finalProbability, finalAlternatives);
                                                app.scrollToBottom();
                                            }
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing chunk: " + e.getMessage());
                            }
                        }
                    }
                }
                
                app.getDisplay().asyncExec(() -> {
                    if (!isCancelled) {
                        app.updateStatus("Generation completed");
                        // Auto-save session after generation
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
        app.updateStatus("Cancelling...");
        resetButtons();
    }
    
    private void resetButtons() {
        app.getGenerateButton().setEnabled(true);
        app.getCancelButton().setEnabled(false);
    }
    
    private double extractProbability(JsonObject choice) {
        if (choice.has("logprobs") && !choice.get("logprobs").isJsonNull()) {
            JsonObject logprobs = choice.getAsJsonObject("logprobs");
            
            if (logprobs.has("content")) {
                JsonArray content = logprobs.getAsJsonArray("content");
                if (content.size() > 0) {
                    JsonObject currentToken = content.get(content.size() - 1).getAsJsonObject();
                    if (currentToken.has("logprob")) {
                        double logprob = currentToken.get("logprob").getAsDouble();
                        return Math.exp(logprob);
                    }
                }
            }
            
            if (logprobs.has("token_logprobs")) {
                JsonArray tokenLogprobs = logprobs.getAsJsonArray("token_logprobs");
                if (tokenLogprobs.size() > 0) {
                    JsonElement logprobElement = tokenLogprobs.get(tokenLogprobs.size() - 1);
                    if (!logprobElement.isJsonNull()) {
                        double logprob = logprobElement.getAsDouble();
                        return Math.exp(logprob);
                    }
                }
            }
        }
        
        return Math.random();
    }
    
    private List<TokenManager.TokenAlternative> extractAlternatives(JsonObject choice) {
        List<TokenManager.TokenAlternative> alternatives = new ArrayList<>();
        
        if (choice.has("logprobs") && !choice.get("logprobs").isJsonNull()) {
            JsonObject logprobs = choice.getAsJsonObject("logprobs");
            
            if (logprobs.has("content")) {
                JsonArray content = logprobs.getAsJsonArray("content");
                if (content.size() > 0) {
                    JsonObject currentToken = content.get(content.size() - 1).getAsJsonObject();
                    
                    if (currentToken.has("top_logprobs")) {
                        JsonArray topLogprobs = currentToken.getAsJsonArray("top_logprobs");
                        
                        for (int i = 0; i < topLogprobs.size(); i++) {
                            JsonObject altToken = topLogprobs.get(i).getAsJsonObject();
                            if (altToken.has("token") && altToken.has("logprob")) {
                                String tokenText = altToken.get("token").getAsString();
                                double logprob = altToken.get("logprob").getAsDouble();
                                double probability = Math.exp(logprob);
                                alternatives.add(new TokenManager.TokenAlternative(tokenText, probability));
                            }
                        }
                        
                        alternatives.sort((a, b) -> Double.compare(b.probability, a.probability));
                    }
                }
            }
        }
        
        return alternatives;
    }
}