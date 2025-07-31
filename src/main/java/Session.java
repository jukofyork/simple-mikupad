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
    
    // Generation Settings
    private Settings settings;
    
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
        
        // Default generation settings
        this.settings = new Settings();
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
        this.settings = new Settings(other.settings);
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
        
        // Save generation settings
        json.add("settings", settings.toSessionJson());
        
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
        
        // Load generation settings
        if (json.has("settings") || json.has("samplingParams")) {
            JsonObject settingsJson = json.has("settings") ? json.getAsJsonObject("settings") : json.getAsJsonObject("samplingParams");
            session.settings = Settings.fromJson(settingsJson);
        } else {
            // Backward compatibility - convert old temperature/maxTokens
            session.settings = new Settings();
            if (json.has("temperature")) {
                session.settings.setTemperature(json.get("temperature").getAsDouble());
            }
            if (json.has("maxTokens")) {
                session.settings.setMaxTokens(json.get("maxTokens").getAsInt());
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
    
    public Settings getSettings() { return settings; }
    public void setSettings(Settings settings) { 
        this.settings = settings; 
        touch();
    }
    
    @Override
    public String toString() {
        return name + " (" + lastModified.format(DateTimeFormatter.ofPattern(Constants.SESSION_DISPLAY_DATE_FORMAT)) + ")";
    }
}