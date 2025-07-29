/**
 * Advanced generation settings that are not sampling parameters.
 * Includes grammar constraints, stopping conditions, and token restrictions.
 */
public class AdvancedSettings {
    
    private String grammar = "";
    private String jsonSchema = "";
    private String logitBias = "";
    private String stoppingStrings = "";
    private String bannedTokens = "";
    
    public AdvancedSettings() {
        // Default constructor
    }
    
    /**
     * Copy constructor
     */
    public AdvancedSettings(AdvancedSettings other) {
        this.grammar = other.grammar;
        this.jsonSchema = other.jsonSchema;
        this.logitBias = other.logitBias;
        this.stoppingStrings = other.stoppingStrings;
        this.bannedTokens = other.bannedTokens;
    }
    
    /**
     * Loads from JSON (for session persistence)
     */
    public static AdvancedSettings fromJson(com.google.gson.JsonObject json) {
        AdvancedSettings settings = new AdvancedSettings();
        
        if (json.has("grammar")) settings.grammar = json.get("grammar").getAsString();
        if (json.has("jsonSchema")) settings.jsonSchema = json.get("jsonSchema").getAsString();
        if (json.has("logitBias")) settings.logitBias = json.get("logitBias").getAsString();
        if (json.has("stoppingStrings")) settings.stoppingStrings = json.get("stoppingStrings").getAsString();
        if (json.has("bannedTokens")) settings.bannedTokens = json.get("bannedTokens").getAsString();
        
        return settings;
    }
    
    /**
     * Converts to JSON for session persistence
     */
    public com.google.gson.JsonObject toJson() {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        
        json.addProperty("grammar", grammar);
        json.addProperty("jsonSchema", jsonSchema);
        json.addProperty("logitBias", logitBias);
        json.addProperty("stoppingStrings", stoppingStrings);
        json.addProperty("bannedTokens", bannedTokens);
        
        return json;
    }
    
    /**
     * Returns a summary string for display in UI
     */
    public String getSummary() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        
        if (!grammar.trim().isEmpty()) {
            parts.add("Grammar");
        }
        
        if (!jsonSchema.trim().isEmpty()) {
            parts.add("JSON Schema");
        }
        
        if (!logitBias.trim().isEmpty()) {
            parts.add("Logit Bias");
        }
        
        if (!stoppingStrings.trim().isEmpty()) {
            String[] stops = stoppingStrings.split(",");
            int count = 0;
            for (String stop : stops) {
                if (!stop.trim().isEmpty()) count++;
            }
            if (count > 0) {
                parts.add(count + " stop string" + (count == 1 ? "" : "s"));
            }
        }
        
        if (!bannedTokens.trim().isEmpty()) {
            String[] tokens = bannedTokens.split(",");
            int count = 0;
            for (String token : tokens) {
                if (!token.trim().isEmpty()) count++;
            }
            if (count > 0) {
                parts.add(count + " banned token" + (count == 1 ? "" : "s"));
            }
        }
        
        if (parts.isEmpty()) {
            return "No constraints";
        }
        
        return String.join(", ", parts);
    }
    
    // Getters and setters
    public String getGrammar() { return grammar; }
    public void setGrammar(String grammar) { this.grammar = grammar; }
    
    public String getJsonSchema() { return jsonSchema; }
    public void setJsonSchema(String jsonSchema) { this.jsonSchema = jsonSchema; }
    
    public String getLogitBias() { return logitBias; }
    public void setLogitBias(String logitBias) { this.logitBias = logitBias; }
    
    public String getStoppingStrings() { return stoppingStrings; }
    public void setStoppingStrings(String stoppingStrings) { this.stoppingStrings = stoppingStrings; }
    
    public String getBannedTokens() { return bannedTokens; }
    public void setBannedTokens(String bannedTokens) { this.bannedTokens = bannedTokens; }
}