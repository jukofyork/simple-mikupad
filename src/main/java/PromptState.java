/**
 * Represents a snapshot of the prompt state for undo/redo functionality.
 * Captures both the text content and cursor position.
 */
public class PromptState {
    
    private final String text;
    private final int caretOffset;
    private final long timestamp;
    
    public PromptState(String text, int caretOffset) {
        this.text = text;
        this.caretOffset = caretOffset;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getText() {
        return text;
    }
    
    public int getCaretOffset() {
        return caretOffset;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PromptState)) return false;
        
        PromptState other = (PromptState) obj;
        return text.equals(other.text) && caretOffset == other.caretOffset;
    }
    
    @Override
    public int hashCode() {
        return text.hashCode() ^ Integer.hashCode(caretOffset);
    }
}