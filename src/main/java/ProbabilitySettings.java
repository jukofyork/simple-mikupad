import com.google.gson.JsonObject;

public class ProbabilitySettings {
    private int nProbs = 0;
    private boolean postSamplingProbs = false;
    private boolean timingsPerToken = false;
    private int minKeep = 0;
    
    public ProbabilitySettings() {}
    
    public ProbabilitySettings(int nProbs, boolean postSamplingProbs, boolean timingsPerToken, int minKeep) {
        this.nProbs = nProbs;
        this.postSamplingProbs = postSamplingProbs;
        this.timingsPerToken = timingsPerToken;
        this.minKeep = minKeep;
    }
    
    // Getters
    public int getNProbs() { return nProbs; }
    public boolean isPostSamplingProbs() { return postSamplingProbs; }
    public boolean isTimingsPerToken() { return timingsPerToken; }
    public int getMinKeep() { return minKeep; }
    
    // Setters
    public void setNProbs(int nProbs) { this.nProbs = nProbs; }
    public void setPostSamplingProbs(boolean postSamplingProbs) { this.postSamplingProbs = postSamplingProbs; }
    public void setTimingsPerToken(boolean timingsPerToken) { this.timingsPerToken = timingsPerToken; }
    public void setMinKeep(int minKeep) { this.minKeep = minKeep; }
    
    public JsonObject addToRequest(JsonObject request) {
        if (nProbs > 0) {
            request.addProperty("n_probs", nProbs);
        }
        if (postSamplingProbs) {
            request.addProperty("post_sampling_probs", true);
        }
        if (timingsPerToken) {
            request.addProperty("timings_per_token", true);
        }
        if (minKeep > 0) {
            request.addProperty("min_keep", minKeep);
        }
        return request;
    }
}