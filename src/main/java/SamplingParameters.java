import com.google.gson.JsonObject;

/**
 * Comprehensive sampling parameters for text generation.
 * Includes all major sampling methods and their configurations.
 */
public class SamplingParameters {
    
    // Basic parameters
    private int seed = Constants.DEFAULT_SEED;
    private double temperature = Constants.DEFAULT_TEMPERATURE;
    private int maxTokens = Constants.DEFAULT_MAX_TOKENS;
    
    // Basic sampling
    private double topP = Constants.DEFAULT_TOP_P;
    private double minP = Constants.DEFAULT_MIN_P;
    private int topK = Constants.DEFAULT_TOP_K;
    
    // Advanced sampling
    private double typicalP = Constants.DEFAULT_TYPICAL_P;
    private double tfsZ = Constants.DEFAULT_TFS_Z;
    
    // Repetition control
    private double repeatPenalty = Constants.DEFAULT_REPEAT_PENALTY;
    private double presencePenalty = Constants.DEFAULT_PRESENCE_PENALTY;
    private double frequencyPenalty = Constants.DEFAULT_FREQUENCY_PENALTY;
    private int repeatLastN = Constants.DEFAULT_REPEAT_LAST_N;
    private boolean penalizeNl = Constants.DEFAULT_PENALIZE_NL;
    
    // Mirostat
    private int mirostat = Constants.DEFAULT_MIROSTAT; // 0=disabled, 1=v1, 2=v2
    private double mirostatTau = Constants.DEFAULT_MIROSTAT_TAU;
    private double mirostatEta = Constants.DEFAULT_MIROSTAT_ETA;
    
    // Dynamic Temperature
    private double dynatempRange = Constants.DEFAULT_DYNATEMP_RANGE;
    private double dynatempExponent = Constants.DEFAULT_DYNATEMP_EXPONENT;
    
    // XTC Sampling
    private double xtcThreshold = Constants.DEFAULT_XTC_THRESHOLD;
    private double xtcProbability = Constants.DEFAULT_XTC_PROBABILITY;
    
    // DRY Sampling
    private double dryMultiplier = Constants.DEFAULT_DRY_MULTIPLIER;
    private double dryBase = Constants.DEFAULT_DRY_BASE;
    private int dryAllowedLength = Constants.DEFAULT_DRY_ALLOWED_LENGTH;
    private String drySequenceBreakers = Constants.DEFAULT_DRY_SEQUENCE_BREAKERS;
    
    // Additional DRY and control parameters
    private int dryPenaltyLastN = Constants.DEFAULT_DRY_PENALTY_LAST_N;
    
    // Sampler ordering
    private String samplers = Constants.DEFAULT_SAMPLERS;
    
    // Sampler toggles - which samplers are enabled
    private boolean temperatureEnabled = Constants.DEFAULT_TEMPERATURE_ENABLED;
    private boolean topPEnabled = Constants.DEFAULT_TOP_P_ENABLED;
    private boolean topKEnabled = Constants.DEFAULT_TOP_K_ENABLED;
    private boolean minPEnabled = Constants.DEFAULT_MIN_P_ENABLED;
    private boolean typicalPEnabled = Constants.DEFAULT_TYPICAL_P_ENABLED;
    private boolean tfsZEnabled = Constants.DEFAULT_TFS_Z_ENABLED;
    private boolean repeatPenaltyEnabled = Constants.DEFAULT_REPEAT_PENALTY_ENABLED;
    private boolean repeatLastNEnabled = Constants.DEFAULT_REPEAT_LAST_N_ENABLED;
    private boolean presencePenaltyEnabled = Constants.DEFAULT_PRESENCE_PENALTY_ENABLED;
    private boolean frequencyPenaltyEnabled = Constants.DEFAULT_FREQUENCY_PENALTY_ENABLED;
    private boolean mirostatEnabled = Constants.DEFAULT_MIROSTAT_ENABLED;
    private boolean dynatempEnabled = Constants.DEFAULT_DYNATEMP_ENABLED;
    private boolean xtcEnabled = Constants.DEFAULT_XTC_ENABLED;
    private boolean dryEnabled = Constants.DEFAULT_DRY_ENABLED;
    private boolean maxTokensEnabled = Constants.DEFAULT_MAX_TOKENS_ENABLED;
    
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
        this.samplers = other.samplers;
        
        // Copy enabled flags
        this.temperatureEnabled = other.temperatureEnabled;
        this.topPEnabled = other.topPEnabled;
        this.topKEnabled = other.topKEnabled;
        this.minPEnabled = other.minPEnabled;
        this.typicalPEnabled = other.typicalPEnabled;
        this.tfsZEnabled = other.tfsZEnabled;
        this.repeatPenaltyEnabled = other.repeatPenaltyEnabled;
        this.repeatLastNEnabled = other.repeatLastNEnabled;
        this.presencePenaltyEnabled = other.presencePenaltyEnabled;
        this.frequencyPenaltyEnabled = other.frequencyPenaltyEnabled;
        this.mirostatEnabled = other.mirostatEnabled;
        this.dynatempEnabled = other.dynatempEnabled;
        this.xtcEnabled = other.xtcEnabled;
        this.dryEnabled = other.dryEnabled;
        this.maxTokensEnabled = other.maxTokensEnabled;
    }
    
    /**
     * Converts to JSON for API requests (sampling parameters only)
     */
    public JsonObject toJson(AdvancedSettings advancedSettings) {
        JsonObject json = new JsonObject();
        
        // Add max_tokens only if enabled
        if (maxTokensEnabled) {
            json.addProperty("max_tokens", maxTokens);
        }
        
        // Add seed (always include, -1 means random)
        json.addProperty("seed", seed);
        
        // Add ignore_eos
        if (advancedSettings != null && advancedSettings.isIgnoreEos()) {
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
            if (repeatLastNEnabled) {
                json.addProperty("repeat_last_n", repeatLastN);
            }
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
            
            // Parse space-delimited dry sequence breakers
            String[] breakers = Constants.parseSpaceDelimited(drySequenceBreakers);
            
            com.google.gson.JsonArray breakersArray = new com.google.gson.JsonArray();
            for (String breaker : breakers) {
                // Handle escape sequences
                String processed = Constants.processEscapeSequences(breaker);
                breakersArray.add(processed);
            }
            if (breakersArray.size() > 0) {
                json.add("dry_sequence_breaker", breakersArray);
            }
        }
        
        // Add samplers array (space-delimited)
        if (!samplers.trim().isEmpty()) {
            String[] samplerArray = Constants.parseSpaceDelimited(samplers);
            
            com.google.gson.JsonArray samplersJsonArray = new com.google.gson.JsonArray();
            for (String sampler : samplerArray) {
                samplersJsonArray.add(sampler);
            }
            
            if (samplersJsonArray.size() > 0) {
                json.add("samplers", samplersJsonArray);
            }
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
        if (json.has("samplers")) params.samplers = json.get("samplers").getAsString();
        
        // Load enabled flags
        if (json.has("temperatureEnabled")) params.temperatureEnabled = json.get("temperatureEnabled").getAsBoolean();
        if (json.has("topPEnabled")) params.topPEnabled = json.get("topPEnabled").getAsBoolean();
        if (json.has("topKEnabled")) params.topKEnabled = json.get("topKEnabled").getAsBoolean();
        if (json.has("minPEnabled")) params.minPEnabled = json.get("minPEnabled").getAsBoolean();
        if (json.has("typicalPEnabled")) params.typicalPEnabled = json.get("typicalPEnabled").getAsBoolean();
        if (json.has("tfsZEnabled")) params.tfsZEnabled = json.get("tfsZEnabled").getAsBoolean();
        if (json.has("repeatPenaltyEnabled")) params.repeatPenaltyEnabled = json.get("repeatPenaltyEnabled").getAsBoolean();
        if (json.has("repeatLastNEnabled")) params.repeatLastNEnabled = json.get("repeatLastNEnabled").getAsBoolean();
        if (json.has("presencePenaltyEnabled")) params.presencePenaltyEnabled = json.get("presencePenaltyEnabled").getAsBoolean();
        if (json.has("frequencyPenaltyEnabled")) params.frequencyPenaltyEnabled = json.get("frequencyPenaltyEnabled").getAsBoolean();
        if (json.has("mirostatEnabled")) params.mirostatEnabled = json.get("mirostatEnabled").getAsBoolean();
        if (json.has("dynatempEnabled")) params.dynatempEnabled = json.get("dynatempEnabled").getAsBoolean();
        if (json.has("xtcEnabled")) params.xtcEnabled = json.get("xtcEnabled").getAsBoolean();
        if (json.has("dryEnabled")) params.dryEnabled = json.get("dryEnabled").getAsBoolean();
        if (json.has("maxTokensEnabled")) params.maxTokensEnabled = json.get("maxTokensEnabled").getAsBoolean();
        
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
        json.addProperty("samplers", samplers);
        
        // Save enabled flags
        json.addProperty("temperatureEnabled", temperatureEnabled);
        json.addProperty("topPEnabled", topPEnabled);
        json.addProperty("topKEnabled", topKEnabled);
        json.addProperty("minPEnabled", minPEnabled);
        json.addProperty("typicalPEnabled", typicalPEnabled);
        json.addProperty("tfsZEnabled", tfsZEnabled);
        json.addProperty("repeatPenaltyEnabled", repeatPenaltyEnabled);
        json.addProperty("repeatLastNEnabled", repeatLastNEnabled);
        json.addProperty("presencePenaltyEnabled", presencePenaltyEnabled);
        json.addProperty("frequencyPenaltyEnabled", frequencyPenaltyEnabled);
        json.addProperty("mirostatEnabled", mirostatEnabled);
        json.addProperty("dynatempEnabled", dynatempEnabled);
        json.addProperty("xtcEnabled", xtcEnabled);
        json.addProperty("dryEnabled", dryEnabled);
        json.addProperty("maxTokensEnabled", maxTokensEnabled);
        
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
    
    public boolean isRepeatLastNEnabled() { return repeatLastNEnabled; }
    public void setRepeatLastNEnabled(boolean enabled) { this.repeatLastNEnabled = enabled; }
    
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
    
    public boolean isMaxTokensEnabled() { return maxTokensEnabled; }
    public void setMaxTokensEnabled(boolean enabled) { this.maxTokensEnabled = enabled; }
}