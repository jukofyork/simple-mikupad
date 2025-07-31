import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a single MikuPad session containing all state for a conversation.
 * Sessions can be saved, loaded, and switched between.
 */
public class Session {
    
    private String id;
    private String name;
    private String promptText;
    private LocalDateTime created;
    private LocalDateTime lastModified;
    
    // API Settings
    private String endpoint;
    private String apiKey;
    private String model;
    
    // Sampling Parameters
    private SamplingParameters samplingParams;
    
    /**
     * Creates a new session with default values
     */
    public Session() {
        this.id = UUID.randomUUID().toString();
        this.name = "New Session";
        this.promptText = "";
        this.created = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        
        // Default API settings
        this.endpoint = Constants.DEFAULT_ENDPOINT;
        this.apiKey = "";
        this.model = Constants.DEFAULT_MODEL;
        
        // Default sampling parameters
        this.samplingParams = new SamplingParameters();
    }
    
    /**
     * Creates a session with a specific name
     */
    public Session(String name) {
        this();
        this.name = name;
    }
    
    /**
     * Copy constructor for cloning sessions
     */
    public Session(Session other) {
        this.id = UUID.randomUUID().toString(); // New ID for clone
        this.name = other.name + " (Copy)";
        this.promptText = other.promptText;
        this.created = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        
        this.endpoint = other.endpoint;
        this.apiKey = other.apiKey;
        this.model = other.model;
        this.samplingParams = new SamplingParameters(other.samplingParams);
    }
    
    /**
     * Updates the last modified timestamp
     */
    public void touch() {
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Converts session to JSON for persistence
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("promptText", promptText);
        json.addProperty("created", created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        json.addProperty("lastModified", lastModified.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        json.addProperty("endpoint", endpoint);
        json.addProperty("apiKey", apiKey);
        json.addProperty("model", model);
        
        // Save sampling parameters
        json.add("samplingParams", samplingParams.toSessionJson());
        
        return json;
    }
    
    /**
     * Creates session from JSON
     */
    public static Session fromJson(JsonObject json) {
        Session session = new Session();
        
        session.id = json.get("id").getAsString();
        session.name = json.get("name").getAsString();
        session.promptText = json.get("promptText").getAsString();
        session.created = LocalDateTime.parse(json.get("created").getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        session.lastModified = LocalDateTime.parse(json.get("lastModified").getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        session.endpoint = json.get("endpoint").getAsString();
        session.apiKey = json.get("apiKey").getAsString();
        session.model = json.get("model").getAsString();
        
        // Load sampling parameters
        if (json.has("samplingParams")) {
            session.samplingParams = SamplingParameters.fromJson(json.getAsJsonObject("samplingParams"));
        } else {
            // Backward compatibility - convert old temperature/maxTokens
            session.samplingParams = new SamplingParameters();
            if (json.has("temperature")) {
                session.samplingParams.setTemperature(json.get("temperature").getAsDouble());
            }
            if (json.has("maxTokens")) {
                session.samplingParams.setMaxTokens(json.get("maxTokens").getAsInt());
            }
        }
        
        return session;
    }
    
    // Getters and setters
    public String getId() { return id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        touch();
    }
    
    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { 
        this.promptText = promptText; 
        touch();
    }
    
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getLastModified() { return lastModified; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { 
        this.endpoint = endpoint; 
        touch();
    }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { 
        this.apiKey = apiKey; 
        touch();
    }
    
    public String getModel() { return model; }
    public void setModel(String model) { 
        this.model = model; 
        touch();
    }
    
    public SamplingParameters getSamplingParams() { return samplingParams; }
    public void setSamplingParams(SamplingParameters samplingParams) { 
        this.samplingParams = samplingParams; 
        touch();
    }
    
    @Override
    public String toString() {
        return name + " (" + lastModified.format(DateTimeFormatter.ofPattern(Constants.SESSION_DISPLAY_DATE_FORMAT)) + ")";
    }
}