import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages multiple sessions with persistence to disk.
 * Handles session creation, switching, deletion, and import/export.
 */
public class SessionManager {
    
    private Map<String, Session> sessions;
    private String currentSessionId;
    private File sessionsFile;
    private Gson gson;
    
    public SessionManager() {
        this.sessions = new HashMap<>();
        this.gson = new Gson();
        
        // Sessions stored in user home directory
        String userHome = System.getProperty("user.home");
        File mikupadDir = new File(userHome, Constants.MIKUPAD_DIR_NAME);
        if (!mikupadDir.exists()) {
            mikupadDir.mkdirs();
        }
        this.sessionsFile = new File(mikupadDir, Constants.SESSIONS_FILE_NAME);
        
        loadSessions();
        
        // Create default session if none exist
        if (sessions.isEmpty()) {
            Session defaultSession = new Session(Constants.DEFAULT_SESSION_NAME);
            defaultSession.setPromptText(Constants.DEFAULT_SESSION_PROMPT);
            addSession(defaultSession);
            setCurrentSession(defaultSession.getId());
        }
    }
    
    /**
     * Creates a new session and adds it to the manager
     */
    public Session createSession(String name) {
        Session session = new Session(name);
        addSession(session);
        return session;
    }
    
    /**
     * Adds an existing session to the manager
     */
    public void addSession(Session session) {
        sessions.put(session.getId(), session);
        saveSessions();
    }
    
    /**
     * Removes a session by ID
     */
    public boolean deleteSession(String sessionId) {
        if (sessions.size() <= 1) {
            return false; // Don't delete the last session
        }
        
        Session removed = sessions.remove(sessionId);
        if (removed != null) {
            // If we deleted the current session, switch to another one
            if (sessionId.equals(currentSessionId)) {
                String newCurrentId = sessions.keySet().iterator().next();
                setCurrentSession(newCurrentId);
            }
            saveSessions();
            return true;
        }
        return false;
    }
    
    /**
     * Clones an existing session
     */
    public Session cloneSession(String sessionId) {
        Session original = sessions.get(sessionId);
        if (original != null) {
            Session clone = new Session(original);
            addSession(clone);
            return clone;
        }
        return null;
    }
    
    /**
     * Renames a session
     */
    public boolean renameSession(String sessionId, String newName) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            session.setName(newName);
            saveSessions();
            return true;
        }
        return false;
    }
    
    /**
     * Gets the current active session
     */
    public Session getCurrentSession() {
        return sessions.get(currentSessionId);
    }
    
    /**
     * Sets the current active session
     */
    public void setCurrentSession(String sessionId) {
        if (sessions.containsKey(sessionId)) {
            this.currentSessionId = sessionId;
            saveSessions();
        }
    }
    
    /**
     * Gets all sessions as a list
     */
    public List<Session> getAllSessions() {
        return new ArrayList<>(sessions.values());
    }
    
    /**
     * Gets a session by ID
     */
    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * Exports a session to a file
     */
    public void exportSession(String sessionId, File file) throws IOException {
        Session session = sessions.get(sessionId);
        if (session != null) {
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(session.toJson(), writer);
            }
        }
    }
    
    /**
     * Imports a session from a file
     */
    public Session importSession(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            Session session = Session.fromJson(json);
            addSession(session);
            return session;
        }
    }
    
    /**
     * Saves all sessions to disk
     */
    private void saveSessions() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("currentSessionId", currentSessionId);
            
            JsonArray sessionsArray = new JsonArray();
            for (Session session : sessions.values()) {
                sessionsArray.add(session.toJson());
            }
            root.add("sessions", sessionsArray);
            
            try (FileWriter writer = new FileWriter(sessionsFile)) {
                gson.toJson(root, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save sessions: " + e.getMessage());
        }
    }
    
    /**
     * Loads all sessions from disk
     */
    private void loadSessions() {
        if (!sessionsFile.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(sessionsFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            if (root.has("currentSessionId")) {
                this.currentSessionId = root.get("currentSessionId").getAsString();
            }
            
            if (root.has("sessions")) {
                JsonArray sessionsArray = root.getAsJsonArray("sessions");
                for (JsonElement element : sessionsArray) {
                    Session session = Session.fromJson(element.getAsJsonObject());
                    sessions.put(session.getId(), session);
                }
            }
            
            // Validate current session exists
            if (currentSessionId != null && !sessions.containsKey(currentSessionId)) {
                currentSessionId = null;
            }
            
        } catch (Exception e) {
            System.err.println("Failed to load sessions: " + e.getMessage());
            // Continue with empty sessions - will create default
        }
    }
    
    /**
     * Forces a save of sessions (useful when session content changes)
     */
    public void saveCurrentState() {
        saveSessions();
    }
}