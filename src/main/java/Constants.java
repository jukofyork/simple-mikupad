import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants for sampling parameters including defaults, ranges, and utility methods.
 */
public class Constants {
    
    // Basic parameter defaults
    public static final int DEFAULT_SEED = -1;
    public static final double DEFAULT_TEMPERATURE = 0.8;
    public static final int DEFAULT_MAX_TOKENS = -1;
    public static final double DEFAULT_TOP_P = 0.9;
    public static final int DEFAULT_TOP_K = 40;
    public static final double DEFAULT_MIN_P = 0.1;
    
    // Advanced sampling defaults
    public static final double DEFAULT_TYPICAL_P = 1.0;
    public static final double DEFAULT_TFS_Z = 1.0;
    
    // Repetition control defaults
    public static final double DEFAULT_REPEAT_PENALTY = 1.0;
    public static final double DEFAULT_PRESENCE_PENALTY = 1.0;
    public static final double DEFAULT_FREQUENCY_PENALTY = 1.0;
    public static final int DEFAULT_REPEAT_LAST_N = 64;
    public static final boolean DEFAULT_PENALIZE_NL = false;
    
    // Mirostat defaults
    public static final int DEFAULT_MIROSTAT = 0;
    public static final double DEFAULT_MIROSTAT_TAU = 5.0;
    public static final double DEFAULT_MIROSTAT_ETA = 0.1;
    
    // Dynamic Temperature defaults
    public static final double DEFAULT_DYNATEMP_RANGE = 0.0;
    public static final double DEFAULT_DYNATEMP_EXPONENT = 1.0;
    
    // XTC defaults
    public static final double DEFAULT_XTC_THRESHOLD = 0.1;
    public static final double DEFAULT_XTC_PROBABILITY = 0.0;
    
    // DRY defaults
    public static final double DEFAULT_DRY_MULTIPLIER = 0.0;
    public static final double DEFAULT_DRY_BASE = 1.75;
    public static final int DEFAULT_DRY_ALLOWED_LENGTH = 2;
    public static final String DEFAULT_DRY_SEQUENCE_BREAKERS = "\\n : \" *";
    public static final int DEFAULT_DRY_PENALTY_LAST_N = -1;
    
    // Sampler ordering default
    // NOTE: This is the default order used by llama.cpp when samplers array is not passed
    public static final String[] DEFAULT_SAMPLERS = {"dry", "top_k", "typ_p", "top_p", "min_p", "xtc", "temperature"};
    
    // Parameter ranges
    public static final double TEMPERATURE_MIN = 0.0;
    public static final double TEMPERATURE_MAX = 5.0;
    public static final int TOP_K_MIN = 0;
    public static final int TOP_K_MAX = 200;
    public static final double TOP_P_MIN = 0.0;
    public static final double TOP_P_MAX = 1.0;
    public static final int MAX_TOKENS_MIN = -1;
    public static final int MAX_TOKENS_MAX = Integer.MAX_VALUE;
    public static final int SEED_MIN = -1;
    public static final int SEED_MAX = Integer.MAX_VALUE;
    public static final double MIN_P_MIN = 0.0;
    public static final double MIN_P_MAX = 1.0;
    public static final double TYPICAL_P_MIN = 0.0;
    public static final double TYPICAL_P_MAX = 1.0;
    public static final double TFS_Z_MIN = 0.0;
    public static final double TFS_Z_MAX = 1.0;
    public static final double REPEAT_PENALTY_MIN = 0.0;
    public static final double REPEAT_PENALTY_MAX = 2.0;
    public static final int REPEAT_LAST_N_MIN = -1;
    public static final int REPEAT_LAST_N_MAX = 2048;
    public static final double PRESENCE_PENALTY_MIN = 0.0;
    public static final double PRESENCE_PENALTY_MAX = 2.0;
    public static final double FREQUENCY_PENALTY_MIN = 0.0;
    public static final double FREQUENCY_PENALTY_MAX = 2.0;
    public static final double MIROSTAT_TAU_MIN = 0.1;
    public static final double MIROSTAT_TAU_MAX = 10.0;
    public static final double MIROSTAT_ETA_MIN = 0.01;
    public static final double MIROSTAT_ETA_MAX = 1.0;
    public static final double DYNATEMP_RANGE_MIN = 0.0;
    public static final double DYNATEMP_RANGE_MAX = 5.0;
    public static final double DYNATEMP_EXPONENT_MIN = 0.1;
    public static final double DYNATEMP_EXPONENT_MAX = 5.0;
    public static final double XTC_THRESHOLD_MIN = 0.0;
    public static final double XTC_THRESHOLD_MAX = 1.0;
    public static final double XTC_PROBABILITY_MIN = 0.0;
    public static final double XTC_PROBABILITY_MAX = 1.0;
    public static final double DRY_MULTIPLIER_MIN = 0.0;
    public static final double DRY_MULTIPLIER_MAX = 5.0;
    public static final double DRY_BASE_MIN = 1.0;
    public static final double DRY_BASE_MAX = 4.0;
    public static final int DRY_ALLOWED_LENGTH_MIN = 1;
    public static final int DRY_ALLOWED_LENGTH_MAX = 20;
    public static final int DRY_PENALTY_LAST_N_MIN = -1;
    public static final int DRY_PENALTY_LAST_N_MAX = 4096;
    
    // Default enabled states
    public static final boolean DEFAULT_SEED_ENABLED = false;
    public static final boolean DEFAULT_SAMPLERS_ENABLED = false;
    public static final boolean DEFAULT_TEMPERATURE_ENABLED = false;
    public static final boolean DEFAULT_TOP_P_ENABLED = false;
    public static final boolean DEFAULT_TOP_K_ENABLED = false;
    public static final boolean DEFAULT_MIN_P_ENABLED = false;
    public static final boolean DEFAULT_TYPICAL_P_ENABLED = false;
    public static final boolean DEFAULT_TFS_Z_ENABLED = false;
    public static final boolean DEFAULT_REPEAT_PENALTY_ENABLED = false;
    public static final boolean DEFAULT_REPEAT_LAST_N_ENABLED = false;
    public static final boolean DEFAULT_PRESENCE_PENALTY_ENABLED = false;
    public static final boolean DEFAULT_FREQUENCY_PENALTY_ENABLED = false;
    public static final boolean DEFAULT_MIROSTAT_ENABLED = false;
    public static final boolean DEFAULT_DYNATEMP_ENABLED = false;
    public static final boolean DEFAULT_XTC_ENABLED = false;
    public static final boolean DEFAULT_DRY_ENABLED = false;
    public static final boolean DEFAULT_MAX_TOKENS_ENABLED = false;
    
    // Advanced settings defaults
    public static final String DEFAULT_GRAMMAR = "";
    public static final String DEFAULT_JSON_SCHEMA = "";
    public static final String DEFAULT_LOGIT_BIAS = "";
    public static final String DEFAULT_STOPPING_STRINGS = "";
    public static final String DEFAULT_BANNED_TOKENS = "";
    public static final boolean DEFAULT_IGNORE_EOS = false;
    
    // Dialog dimensions
    public static final int ADVANCED_DIALOG_WIDTH = 500;
    public static final int ADVANCED_DIALOG_HEIGHT = 400;
    
    // Main window dimensions
    public static final int MAIN_WINDOW_WIDTH = 1440;
    public static final int MAIN_WINDOW_HEIGHT = 1080;
    
    // Session defaults
    public static final String DEFAULT_ENDPOINT = "http://127.0.0.1:8080";
    public static final String DEFAULT_MODEL = "Qwen3-30B-A3B";
    public static final String SESSION_DISPLAY_DATE_FORMAT = "dd MMM yyyy HH:mm";
    public static final String MIKUPAD_DIR_NAME = ".mikupad";
    public static final String SESSIONS_FILE_NAME = "sessions.json";
    public static final String DEFAULT_SESSION_NAME = "Default Session";
    public static final String DEFAULT_SESSION_PROMPT = "<|im_start|>user\nWrite me story about pigs<|im_end|>\n<|im_start|>assistant\n\n<think>\n\n</think>\n\n";
    
    // Prompt coloring constants
    public static final int COLOR_BLUE_DARK = 120;
    public static final int COLOR_BLUE_LIGHT = 180;
    public static final int COLOR_BLUE_GREEN_LOW = 40;
    public static final int COLOR_BLUE_GREEN_HIGH = 60;

    // Token generation coloring constants
    public static final int COLOR_PROBABILITY_SCALE = 100;
    public static final float COLOR_RATIO_THRESHOLD = 0.5f;
    public static final float COLOR_RATIO_MULTIPLIER = 2.0f;
    public static final int COLOR_RED_BASE = 120;
    public static final int COLOR_RED_RANGE = 60;
    public static final int COLOR_RED_MAX = 180;
    public static final int COLOR_GREEN_LOW_RANGE = 40;
    public static final int COLOR_GREEN_HIGH_RANGE = 100;
    public static final int PERCENTAGE_MULTIPLIER = 100;
    
    // Tooltip positioning
    public static final int TOOLTIP_OFFSET_X = 10;
    public static final int TOOLTIP_OFFSET_Y = 10;
    
    // API constants
    public static final int DEFAULT_TOKEN_ALTERNATIVES_COUNT = 10;
        
    /**
     * Parses a space-delimited string into an array of trimmed, non-empty strings.
     * @param input The space-delimited string
     * @return Array of parsed strings
     */
    public static String[] parseSpaceDelimited(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new String[0];
        }
        
        return input.trim().split("\\s+");
    }
    
    /**
     * Converts an array of strings to a space-delimited string.
     * @param items Array of strings
     * @return Space-delimited string
     */
    public static String toSpaceDelimited(String[] items) {
        if (items == null || items.length == 0) {
            return "";
        }
        
        return String.join(" ", items);
    }
    
    /**
     * Processes escape sequences in strings (e.g., \\n to \n).
     * @param input String that may contain escape sequences
     * @return String with escape sequences processed
     */
    public static String processEscapeSequences(String input) {
        if (input == null) {
            return null;
        }
        
        return input.replace("\\n", "\n")
                   .replace("\\t", "\t")
                   .replace("\\r", "\r");
    }
    
    // Text area height hints for advanced settings dialog
    public static final int GRAMMAR_TEXT_HEIGHT = 100;
    public static final int JSON_SCHEMA_TEXT_HEIGHT = 80;
    public static final int LOGIT_BIAS_TEXT_HEIGHT = 60;
}