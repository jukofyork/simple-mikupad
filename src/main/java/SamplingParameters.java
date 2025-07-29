import com.google.gson.JsonObject;

/**
 * Comprehensive sampling parameters for text generation.
 * Includes all major sampling methods and their configurations.
 */
public class SamplingParameters {
    
    // Basic parameters
    private int seed = -1;
    private double temperature = 0.7;
    private int maxTokens = 512;
    
    // Top sampling
    private double topP = 0.95;
    private int topK = 40;
    private double minP = 0.0;
    private double typicalP = 1.0;
    private double tfsZ = 1.0;
    
    // Repetition control
    private double repeatPenalty = 1.1;
    private int repeatLastN = 256;
    private boolean penalizeNl = false;
    private double presencePenalty = 0.0;
    private double frequencyPenalty = 0.0;
    
    // Mirostat
    private int mirostat = 0; // 0=disabled, 1=v1, 2=v2
    private double mirostatTau = 5.0;
    private double mirostatEta = 0.1;
    
    // Dynamic Temperature
    private double dynatempRange = 0.0;
    private double dynatempExponent = 1.0;
    
    // XTC Sampling
    private double xtcThreshold = 0.1;
    private double xtcProbability = 0.0;
    
    // DRY Sampling
    private double dryMultiplier = 0.0;
    private double dryBase = 1.75;
    private int dryAllowedLength = 2;
    private String drySequenceBreakers = "\\n,.,!,?,;,:";
    
    // Additional DRY and control parameters
    private int dryPenaltyLastN = -1;
    private boolean ignoreEos = false;
    
    // Sampler ordering
    private String samplers = "dry;top_k;typ_p;top_p;min_p;xtc;temperature";
    
    // Sampler toggles - which samplers are enabled
    private boolean temperatureEnabled = true;
    private boolean topPEnabled = true;
    private boolean topKEnabled = true;
    private boolean minPEnabled = false;
    private boolean typicalPEnabled = false;
    private boolean tfsZEnabled = false;
    private boolean repeatPenaltyEnabled = true;
    private boolean presencePenaltyEnabled = false;
    private boolean frequencyPenaltyEnabled = false;
    private boolean mirostatEnabled = false;
    private boolean dynatempEnabled = false;
    private boolean xtcEnabled = false;
    private boolean dryEnabled = false;
    
    public SamplingParameters() {
        // Default constructor with sensible defaults
    }
    
    /**
     * Copy constructor
     */
    public SamplingParameters(SamplingParameters other) {
        this.seed = other.seed;
        this.temperature = other.temperature;
        this.maxTokens = other.maxTokens;
        this.topP = other.topP;
        this.topK = other.topK;
        this.minP = other.minP;
        this.typicalP = other.typicalP;
        this.tfsZ = other.tfsZ;
        this.repeatPenalty = other.repeatPenalty;
        this.repeatLastN = other.repeatLastN;
        this.penalizeNl = other.penalizeNl;
        this.presencePenalty = other.presencePenalty;
        this.frequencyPenalty = other.frequencyPenalty;
        this.mirostat = other.mirostat;
        this.mirostatTau = other.mirostatTau;
        this.mirostatEta = other.mirostatEta;
        this.dynatempRange = other.dynatempRange;
        this.dynatempExponent = other.dynatempExponent;
        this.xtcThreshold = other.xtcThreshold;
        this.xtcProbability = other.xtcProbability;
        this.dryMultiplier = other.dryMultiplier;
        this.dryBase = other.dryBase;
        this.dryAllowedLength = other.dryAllowedLength;
        this.drySequenceBreakers = other.drySequenceBreakers;
        this.dryPenaltyLastN = other.dryPenaltyLastN;
        this.ignoreEos = other.ignoreEos;
        this.samplers = other.samplers;
        
        // Copy enabled flags
        this.temperatureEnabled = other.temperatureEnabled;
        this.topPEnabled = other.topPEnabled;
        this.topKEnabled = other.topKEnabled;
        this.minPEnabled = other.minPEnabled;
        this.typicalPEnabled = other.typicalPEnabled;
        this.tfsZEnabled = other.tfsZEnabled;
        this.repeatPenaltyEnabled = other.repeatPenaltyEnabled;
        this.presencePenaltyEnabled = other.presencePenaltyEnabled;
        this.frequencyPenaltyEnabled = other.frequencyPenaltyEnabled;
        this.mirostatEnabled = other.mirostatEnabled;
        this.dynatempEnabled = other.dynatempEnabled;
        this.xtcEnabled = other.xtcEnabled;
        this.dryEnabled = other.dryEnabled;
    }
    
    /**
     * Converts to JSON for API requests (sampling parameters only)
     */
    public JsonObject toJson(AdvancedSettings advancedSettings) {
        JsonObject json = new JsonObject();
        
        // Always include basic parameters
        json.addProperty("max_tokens", maxTokens);
        
        // Add seed (always include, -1 means random)
        json.addProperty("seed", seed);
        
        // Add ignore_eos
        if (ignoreEos) {
            json.addProperty("ignore_eos", true);
        }
        
        // Add enabled samplers only
        if (temperatureEnabled) {
            json.addProperty("temperature", temperature);
        }
        
        if (topPEnabled) {
            json.addProperty("top_p", topP);
        }
        
        if (topKEnabled) {
            json.addProperty("top_k", topK);
        }
        
        if (minPEnabled && minP > 0) {
            json.addProperty("min_p", minP);
        }
        
        if (typicalPEnabled && typicalP < 1.0) {
            json.addProperty("typical_p", typicalP);
        }
        
        if (tfsZEnabled && tfsZ < 1.0) {
            json.addProperty("tfs_z", tfsZ);
        }
        
        if (repeatPenaltyEnabled) {
            json.addProperty("repeat_penalty", repeatPenalty);
            json.addProperty("repeat_last_n", repeatLastN);
            json.addProperty("penalize_nl", penalizeNl);
        }
        
        if (presencePenaltyEnabled && presencePenalty != 0) {
            json.addProperty("presence_penalty", presencePenalty);
        }
        
        if (frequencyPenaltyEnabled && frequencyPenalty != 0) {
            json.addProperty("frequency_penalty", frequencyPenalty);
        }
        
        if (mirostatEnabled && mirostat > 0) {
            json.addProperty("mirostat", mirostat);
            json.addProperty("mirostat_tau", mirostatTau);
            json.addProperty("mirostat_eta", mirostatEta);
        }
        
        if (dynatempEnabled && dynatempRange > 0) {
            json.addProperty("dynatemp_range", dynatempRange);
            json.addProperty("dynatemp_exponent", dynatempExponent);
        }
        
        if (xtcEnabled && xtcProbability > 0) {
            json.addProperty("xtc_threshold", xtcThreshold);
            json.addProperty("xtc_probability", xtcProbability);
        }
        
        if (dryEnabled && dryMultiplier > 0) {
            json.addProperty("dry_multiplier", dryMultiplier);
            json.addProperty("dry_base", dryBase);
            json.addProperty("dry_allowed_length", dryAllowedLength);
            json.addProperty("dry_penalty_last_n", dryPenaltyLastN);
            
            // Convert dry sequence breakers to array
            String[] breakers = drySequenceBreakers.split(",");
            com.google.gson.JsonArray breakersArray = new com.google.gson.JsonArray();
            for (String breaker : breakers) {
                String trimmed = breaker.trim();
                if (!trimmed.isEmpty()) {
                    // Handle escape sequences
                    if (trimmed.equals("\\n")) {
                        breakersArray.add("\n");
                    } else if (trimmed.equals("\\t")) {
                        breakersArray.add("\t");
                    } else if (trimmed.equals("\\r")) {
                        breakersArray.add("\r");
                    } else {
                        breakersArray.add(trimmed);
                    }
                }
            }
            json.add("dry_sequence_breaker", breakersArray);
        }
        
        // Add samplers array (convert comma or semicolon separated to array)
        if (!samplers.trim().isEmpty()) {
            String[] samplerArray;
            if (samplers.contains(";")) {
                samplerArray = samplers.split(";");
            } else {
                samplerArray = samplers.split(",");
            }
            com.google.gson.JsonArray samplersJsonArray = new com.google.gson.JsonArray();
            for (String sampler : samplerArray) {
                samplersJsonArray.add(sampler.trim());
            }
            json.add("samplers", samplersJsonArray);
        }
        
        // Advanced features
        if (advancedSettings != null && !advancedSettings.getGrammar().trim().isEmpty()) {
            json.addProperty("grammar", advancedSettings.getGrammar().trim());
        }
        
        if (advancedSettings != null && !advancedSettings.getJsonSchema().trim().isEmpty()) {
            json.addProperty("json_schema", advancedSettings.getJsonSchema().trim());
        }
        
        if (advancedSettings != null && !advancedSettings.getLogitBias().trim().isEmpty()) {
            try {
                // Parse logit bias as JSON array
                com.google.gson.JsonElement biasElement = com.google.gson.JsonParser.parseString(advancedSettings.getLogitBias());
                if (biasElement.isJsonArray()) {
                    json.add("logit_bias", biasElement.getAsJsonArray());
                }
            } catch (Exception e) {
                // If parsing fails, ignore logit bias
                System.err.println("Warning: Invalid logit bias format, ignoring: " + e.getMessage());
            }
        }
        
        // Handle banned tokens by converting to logit bias
        if (advancedSettings != null && !advancedSettings.getBannedTokens().trim().isEmpty()) {
            try {
                String[] tokens = advancedSettings.getBannedTokens().split(",");
                com.google.gson.JsonArray biasArray = new com.google.gson.JsonArray();
                for (String token : tokens) {
                    String trimmed = token.trim();
                    if (!trimmed.isEmpty()) {
                        com.google.gson.JsonArray tokenBias = new com.google.gson.JsonArray();
                        tokenBias.add(trimmed);
                        tokenBias.add(-100.0);
                        biasArray.add(tokenBias);
                    }
                }
                if (biasArray.size() > 0) {
                    json.add("logit_bias", biasArray);
                }
            } catch (Exception e) {
                System.err.println("Warning: Error processing banned tokens: " + e.getMessage());
            }
        }
        
        if (advancedSettings != null && !advancedSettings.getStoppingStrings().trim().isEmpty()) {
            // Parse stopping strings (comma-separated)
            String[] stops = advancedSettings.getStoppingStrings().split(",");
            if (stops.length > 0) {
                com.google.gson.JsonArray stopArray = new com.google.gson.JsonArray();
                for (String stop : stops) {
                    String trimmed = stop.trim();
                    if (!trimmed.isEmpty()) {
                        stopArray.add(trimmed);
                    }
                }
                if (stopArray.size() > 0) {
                    json.add("stop", stopArray);
                }
            }
        }
        
        return json;
    }
    
    /**
     * Loads from JSON (for session persistence)
     */
    public static SamplingParameters fromJson(JsonObject json) {
        SamplingParameters params = new SamplingParameters();
        
        if (json.has("seed")) params.seed = json.get("seed").getAsInt();
        if (json.has("temperature")) params.temperature = json.get("temperature").getAsDouble();
        if (json.has("maxTokens")) params.maxTokens = json.get("maxTokens").getAsInt();
        if (json.has("topP")) params.topP = json.get("topP").getAsDouble();
        if (json.has("topK")) params.topK = json.get("topK").getAsInt();
        if (json.has("minP")) params.minP = json.get("minP").getAsDouble();
        if (json.has("typicalP")) params.typicalP = json.get("typicalP").getAsDouble();
        if (json.has("tfsZ")) params.tfsZ = json.get("tfsZ").getAsDouble();
        if (json.has("repeatPenalty")) params.repeatPenalty = json.get("repeatPenalty").getAsDouble();
        if (json.has("repeatLastN")) params.repeatLastN = json.get("repeatLastN").getAsInt();
        if (json.has("penalizeNl")) params.penalizeNl = json.get("penalizeNl").getAsBoolean();
        if (json.has("presencePenalty")) params.presencePenalty = json.get("presencePenalty").getAsDouble();
        if (json.has("frequencyPenalty")) params.frequencyPenalty = json.get("frequencyPenalty").getAsDouble();
        if (json.has("mirostat")) params.mirostat = json.get("mirostat").getAsInt();
        if (json.has("mirostatTau")) params.mirostatTau = json.get("mirostatTau").getAsDouble();
        if (json.has("mirostatEta")) params.mirostatEta = json.get("mirostatEta").getAsDouble();
        if (json.has("dynatempRange")) params.dynatempRange = json.get("dynatempRange").getAsDouble();
        if (json.has("dynatempExponent")) params.dynatempExponent = json.get("dynatempExponent").getAsDouble();
        if (json.has("xtcThreshold")) params.xtcThreshold = json.get("xtcThreshold").getAsDouble();
        if (json.has("xtcProbability")) params.xtcProbability = json.get("xtcProbability").getAsDouble();
        if (json.has("dryMultiplier")) params.dryMultiplier = json.get("dryMultiplier").getAsDouble();
        if (json.has("dryBase")) params.dryBase = json.get("dryBase").getAsDouble();
        if (json.has("dryAllowedLength")) params.dryAllowedLength = json.get("dryAllowedLength").getAsInt();
        if (json.has("drySequenceBreakers")) params.drySequenceBreakers = json.get("drySequenceBreakers").getAsString();
        if (json.has("dryPenaltyLastN")) params.dryPenaltyLastN = json.get("dryPenaltyLastN").getAsInt();
        if (json.has("ignoreEos")) params.ignoreEos = json.get("ignoreEos").getAsBoolean();
        if (json.has("samplers")) params.samplers = json.get("samplers").getAsString();
        
        // Load enabled flags
        if (json.has("temperatureEnabled")) params.temperatureEnabled = json.get("temperatureEnabled").getAsBoolean();
        if (json.has("topPEnabled")) params.topPEnabled = json.get("topPEnabled").getAsBoolean();
        if (json.has("topKEnabled")) params.topKEnabled = json.get("topKEnabled").getAsBoolean();
        if (json.has("minPEnabled")) params.minPEnabled = json.get("minPEnabled").getAsBoolean();
        if (json.has("typicalPEnabled")) params.typicalPEnabled = json.get("typicalPEnabled").getAsBoolean();
        if (json.has("tfsZEnabled")) params.tfsZEnabled = json.get("tfsZEnabled").getAsBoolean();
        if (json.has("repeatPenaltyEnabled")) params.repeatPenaltyEnabled = json.get("repeatPenaltyEnabled").getAsBoolean();
        if (json.has("presencePenaltyEnabled")) params.presencePenaltyEnabled = json.get("presencePenaltyEnabled").getAsBoolean();
        if (json.has("frequencyPenaltyEnabled")) params.frequencyPenaltyEnabled = json.get("frequencyPenaltyEnabled").getAsBoolean();
        if (json.has("mirostatEnabled")) params.mirostatEnabled = json.get("mirostatEnabled").getAsBoolean();
        if (json.has("dynatempEnabled")) params.dynatempEnabled = json.get("dynatempEnabled").getAsBoolean();
        if (json.has("xtcEnabled")) params.xtcEnabled = json.get("xtcEnabled").getAsBoolean();
        if (json.has("dryEnabled")) params.dryEnabled = json.get("dryEnabled").getAsBoolean();
        
        return params;
    }
    
    /**
     * Converts to JSON for session persistence (includes enabled flags)
     */
    public JsonObject toSessionJson() {
        JsonObject json = new JsonObject();
        
        // Save all parameters
        json.addProperty("seed", seed);
        json.addProperty("temperature", temperature);
        json.addProperty("maxTokens", maxTokens);
        json.addProperty("topP", topP);
        json.addProperty("topK", topK);
        json.addProperty("minP", minP);
        json.addProperty("typicalP", typicalP);
        json.addProperty("tfsZ", tfsZ);
        json.addProperty("repeatPenalty", repeatPenalty);
        json.addProperty("repeatLastN", repeatLastN);
        json.addProperty("penalizeNl", penalizeNl);
        json.addProperty("presencePenalty", presencePenalty);
        json.addProperty("frequencyPenalty", frequencyPenalty);
        json.addProperty("mirostat", mirostat);
        json.addProperty("mirostatTau", mirostatTau);
        json.addProperty("mirostatEta", mirostatEta);
        json.addProperty("dynatempRange", dynatempRange);
        json.addProperty("dynatempExponent", dynatempExponent);
        json.addProperty("xtcThreshold", xtcThreshold);
        json.addProperty("xtcProbability", xtcProbability);
        json.addProperty("dryMultiplier", dryMultiplier);
        json.addProperty("dryBase", dryBase);
        json.addProperty("dryAllowedLength", dryAllowedLength);
        json.addProperty("drySequenceBreakers", drySequenceBreakers);
        json.addProperty("dryPenaltyLastN", dryPenaltyLastN);
        json.addProperty("ignoreEos", ignoreEos);
        json.addProperty("samplers", samplers);
        
        // Save enabled flags
        json.addProperty("temperatureEnabled", temperatureEnabled);
        json.addProperty("topPEnabled", topPEnabled);
        json.addProperty("topKEnabled", topKEnabled);
        json.addProperty("minPEnabled", minPEnabled);
        json.addProperty("typicalPEnabled", typicalPEnabled);
        json.addProperty("tfsZEnabled", tfsZEnabled);
        json.addProperty("repeatPenaltyEnabled", repeatPenaltyEnabled);
        json.addProperty("presencePenaltyEnabled", presencePenaltyEnabled);
        json.addProperty("frequencyPenaltyEnabled", frequencyPenaltyEnabled);
        json.addProperty("mirostatEnabled", mirostatEnabled);
        json.addProperty("dynatempEnabled", dynatempEnabled);
        json.addProperty("xtcEnabled", xtcEnabled);
        json.addProperty("dryEnabled", dryEnabled);
        
        return json;
    }
    
    // Getters and setters for all parameters
    public int getSeed() { return seed; }
    public void setSeed(int seed) { this.seed = seed; }
    
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    
    public double getTopP() { return topP; }
    public void setTopP(double topP) { this.topP = topP; }
    
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
    
    public double getMinP() { return minP; }
    public void setMinP(double minP) { this.minP = minP; }
    
    public double getTypicalP() { return typicalP; }
    public void setTypicalP(double typicalP) { this.typicalP = typicalP; }
    
    public double getTfsZ() { return tfsZ; }
    public void setTfsZ(double tfsZ) { this.tfsZ = tfsZ; }
    
    public double getRepeatPenalty() { return repeatPenalty; }
    public void setRepeatPenalty(double repeatPenalty) { this.repeatPenalty = repeatPenalty; }
    
    public int getRepeatLastN() { return repeatLastN; }
    public void setRepeatLastN(int repeatLastN) { this.repeatLastN = repeatLastN; }
    
    public boolean isPenalizeNl() { return penalizeNl; }
    public void setPenalizeNl(boolean penalizeNl) { this.penalizeNl = penalizeNl; }
    
    public double getPresencePenalty() { return presencePenalty; }
    public void setPresencePenalty(double presencePenalty) { this.presencePenalty = presencePenalty; }
    
    public double getFrequencyPenalty() { return frequencyPenalty; }
    public void setFrequencyPenalty(double frequencyPenalty) { this.frequencyPenalty = frequencyPenalty; }
    
    public int getMirostat() { return mirostat; }
    public void setMirostat(int mirostat) { this.mirostat = mirostat; }
    
    public double getMirostatTau() { return mirostatTau; }
    public void setMirostatTau(double mirostatTau) { this.mirostatTau = mirostatTau; }
    
    public double getMirostatEta() { return mirostatEta; }
    public void setMirostatEta(double mirostatEta) { this.mirostatEta = mirostatEta; }
    
    public double getDynatempRange() { return dynatempRange; }
    public void setDynatempRange(double dynatempRange) { this.dynatempRange = dynatempRange; }
    
    public double getDynatempExponent() { return dynatempExponent; }
    public void setDynatempExponent(double dynatempExponent) { this.dynatempExponent = dynatempExponent; }
    
    public double getXtcThreshold() { return xtcThreshold; }
    public void setXtcThreshold(double xtcThreshold) { this.xtcThreshold = xtcThreshold; }
    
    public double getXtcProbability() { return xtcProbability; }
    public void setXtcProbability(double xtcProbability) { this.xtcProbability = xtcProbability; }
    
    public double getDryMultiplier() { return dryMultiplier; }
    public void setDryMultiplier(double dryMultiplier) { this.dryMultiplier = dryMultiplier; }
    
    public double getDryBase() { return dryBase; }
    public void setDryBase(double dryBase) { this.dryBase = dryBase; }
    
    public int getDryAllowedLength() { return dryAllowedLength; }
    public void setDryAllowedLength(int dryAllowedLength) { this.dryAllowedLength = dryAllowedLength; }
    
    public String getDrySequenceBreakers() { return drySequenceBreakers; }
    public void setDrySequenceBreakers(String drySequenceBreakers) { this.drySequenceBreakers = drySequenceBreakers; }
    
    public int getDryPenaltyLastN() { return dryPenaltyLastN; }
    public void setDryPenaltyLastN(int dryPenaltyLastN) { this.dryPenaltyLastN = dryPenaltyLastN; }
    
    public boolean isIgnoreEos() { return ignoreEos; }
    public void setIgnoreEos(boolean ignoreEos) { this.ignoreEos = ignoreEos; }
    
    public String getSamplers() { return samplers; }
    public void setSamplers(String samplers) { this.samplers = samplers; }
    
    // Enabled flag getters and setters
    public boolean isTemperatureEnabled() { return temperatureEnabled; }
    public void setTemperatureEnabled(boolean enabled) { this.temperatureEnabled = enabled; }
    
    public boolean isTopPEnabled() { return topPEnabled; }
    public void setTopPEnabled(boolean enabled) { this.topPEnabled = enabled; }
    
    public boolean isTopKEnabled() { return topKEnabled; }
    public void setTopKEnabled(boolean enabled) { this.topKEnabled = enabled; }
    
    public boolean isMinPEnabled() { return minPEnabled; }
    public void setMinPEnabled(boolean enabled) { this.minPEnabled = enabled; }
    
    public boolean isTypicalPEnabled() { return typicalPEnabled; }
    public void setTypicalPEnabled(boolean enabled) { this.typicalPEnabled = enabled; }
    
    public boolean isTfsZEnabled() { return tfsZEnabled; }
    public void setTfsZEnabled(boolean enabled) { this.tfsZEnabled = enabled; }
    
    public boolean isRepeatPenaltyEnabled() { return repeatPenaltyEnabled; }
    public void setRepeatPenaltyEnabled(boolean enabled) { this.repeatPenaltyEnabled = enabled; }
    
    public boolean isPresencePenaltyEnabled() { return presencePenaltyEnabled; }
    public void setPresencePenaltyEnabled(boolean enabled) { this.presencePenaltyEnabled = enabled; }
    
    public boolean isFrequencyPenaltyEnabled() { return frequencyPenaltyEnabled; }
    public void setFrequencyPenaltyEnabled(boolean enabled) { this.frequencyPenaltyEnabled = enabled; }
    
    public boolean isMirostatEnabled() { return mirostatEnabled; }
    public void setMirostatEnabled(boolean enabled) { this.mirostatEnabled = enabled; }
    
    public boolean isDynatempEnabled() { return dynatempEnabled; }
    public void setDynatempEnabled(boolean enabled) { this.dynatempEnabled = enabled; }
    
    public boolean isXtcEnabled() { return xtcEnabled; }
    public void setXtcEnabled(boolean enabled) { this.xtcEnabled = enabled; }
    
    public boolean isDryEnabled() { return dryEnabled; }
    public void setDryEnabled(boolean enabled) { this.dryEnabled = enabled; }
}