import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class StyledTextUndoManager {

    /**
     * Represents an undo-able text change.
     */
    private static class UndoableTextChange {
        /** The start index of the replaced text. */
        protected int fStart = -1;
        /** The end index of the replaced text. */
        protected int fEnd = -1;
        /** The newly inserted text. */
        protected String fText;
        /** The replaced text. */
        protected String fPreservedText;
        /** The undo manager that generated the change. */
        protected StyledTextUndoManager fUndoManager;

        /**
         * Creates a new text change.
         */
        UndoableTextChange(StyledTextUndoManager manager) {
            this.fUndoManager = manager;
        }

        /**
         * Re-initializes this text change.
         */
        protected void reinitialize() {
            fStart = fEnd = -1;
            fText = fPreservedText = null;
        }

        /**
         * Sets the start and the end index of this change.
         */
        protected void set(int start, int end) {
            fStart = start;
            fEnd = end;
            fText = null;
            fPreservedText = null;
        }

        /**
         * Undo the change described by this change.
         */
        protected void undoTextChange() {
            try {
                if (fStart < 0 || fText == null || fPreservedText == null) {
                    return; // Invalid state, skip silently
                }
                fUndoManager.fStyledText.replaceTextRange(fStart, fText.length(), fPreservedText);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Undo operation failed due to document structure change: " + e.getMessage());
            }
        }

        public boolean canUndo() {
            return isValid();
        }

        public boolean canRedo() {
            return isValid();
        }

        /**
         * Undo the change described by this command.
         */
        public void undo() {
            if (isValid()) {
                undoTextChange();
                selectAndReveal(fStart, fPreservedText == null ? 0 : fPreservedText.length());
                fUndoManager.resetProcessChangeState();
            }
        }

        /**
         * Re-applies the change described by this change.
         */
        protected void redoTextChange() {
            try {
                if (fStart < 0 || fText == null) {
                    return; // Invalid state, skip silently
                }
                fUndoManager.fStyledText.replaceTextRange(fStart, fEnd - fStart, fText);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Redo operation failed due to document structure change: " + e.getMessage());
            }
        }

        /**
         * Re-applies the change described by this command.
         */
        public void redo() {
            if (isValid()) {
                redoTextChange();
                fUndoManager.resetProcessChangeState();
                selectAndReveal(fStart, fText == null ? 0 : fText.length());
            }
        }

        /**
         * Update the command in response to a commit.
         */
        protected void updateCommand() {
            fText = fUndoManager.fTextBuffer.toString();
            fUndoManager.fTextBuffer.setLength(0);
            fPreservedText = fUndoManager.fPreservedTextBuffer.toString();
            fUndoManager.fPreservedTextBuffer.setLength(0);
        }

        /**
         * Creates a new uncommitted text command.
         */
        protected UndoableTextChange createCurrent() {
            return fUndoManager.fFoldingIntoCompoundChange ? 
                new UndoableCompoundTextChange(fUndoManager) : 
                new UndoableTextChange(fUndoManager);
        }

        /**
         * Commits the current change into this command.
         */
        protected void commit() {
            if (fStart < 0) {
                if (fUndoManager.fFoldingIntoCompoundChange) {
                    fUndoManager.fCurrent = createCurrent();
                } else {
                    reinitialize();
                }
            } else {
                updateCommand();
                fUndoManager.fCurrent = createCurrent();
            }
            fUndoManager.resetProcessChangeState();
        }

        /**
         * Updates the text from the buffers without resetting buffers.
         */
        protected void pretendCommit() {
            if (fStart > -1) {
                fText = fUndoManager.fTextBuffer.toString();
                fPreservedText = fUndoManager.fPreservedTextBuffer.toString();
            }
        }

        /**
         * Attempt a commit of this command.
         */
        protected boolean attemptCommit() {
            pretendCommit();
            if (isValid()) {
                fUndoManager.commit();
                return true;
            }
            return false;
        }

        /**
         * Checks whether this text command is valid for undo or redo.
         */
        protected boolean isValid() {
            return fStart > -1 && fEnd > -1 && fText != null;
        }

        /**
         * Selects and reveals the specified range.
         */
        protected void selectAndReveal(int offset, int length) {
            fUndoManager.fStyledText.setSelection(offset, offset + length);
        }
    }

    /**
     * Represents an undo-able text change consisting of several individual changes.
     */
    private static class UndoableCompoundTextChange extends UndoableTextChange {
        /** The list of individual changes */
        private List<UndoableTextChange> fChanges = new ArrayList<>();

        /**
         * Creates a new compound text change.
         */
        UndoableCompoundTextChange(StyledTextUndoManager manager) {
            super(manager);
        }

        /**
         * Adds a new individual change to this compound change.
         */
        protected void add(UndoableTextChange change) {
            fChanges.add(change);
        }

        @Override
        public void undo() {
            fUndoManager.resetProcessChangeState();

            int size = fChanges.size();
            if (size > 0) {
                UndoableTextChange c;
                for (int i = size - 1; i > 0; --i) {
                    c = fChanges.get(i);
                    c.undoTextChange();
                }
                c = fChanges.get(0);
                c.undo();
            }
        }

        @Override
        public void redo() {
            fUndoManager.resetProcessChangeState();

            int size = fChanges.size();
            if (size > 0) {
                // Calculate total range for selection
                int startOffset = Integer.MAX_VALUE;
                int totalLength = 0;
                
                UndoableTextChange c;
                for (int i = 0; i < size - 1; ++i) {
                    c = fChanges.get(i);
                    if (c.fStart < startOffset) {
                        startOffset = c.fStart;
                    }
                    if (c.fText != null) {
                        totalLength += c.fText.length();
                    }
                    c.redoTextChange();
                }
                c = fChanges.get(size - 1);
                c.redoTextChange();
                
                // Calculate final change contribution
                if (c.fStart < startOffset) {
                    startOffset = c.fStart;
                }
                if (c.fText != null) {
                    totalLength += c.fText.length();
                }
                
                // Select the entire compound change range
                selectAndReveal(startOffset, totalLength);
            }
        }

        @Override
        protected void updateCommand() {
            // first gather the data from the buffers
            super.updateCommand();

            // the result of the command update is stored as a child command
            UndoableTextChange c = new UndoableTextChange(fUndoManager);
            c.fStart = fStart;
            c.fEnd = fEnd;
            c.fText = fText;
            c.fPreservedText = fPreservedText;
            add(c);

            // clear out all indexes now that the child is added
            reinitialize();
        }

        @Override
        protected UndoableTextChange createCurrent() {
            if (!fUndoManager.fFoldingIntoCompoundChange)
                return new UndoableTextChange(fUndoManager);

            reinitialize();
            return this;
        }

        @Override
        protected void commit() {
            // if there is pending data, update the command
            if (fStart > -1)
                updateCommand();
            fUndoManager.fCurrent = createCurrent();
            fUndoManager.resetProcessChangeState();
        }

        @Override
        protected boolean isValid() {
            return fStart > -1 || !fChanges.isEmpty();
        }
    }

    /**
     * Internal listener to mouse and key events.
     */
    private class KeyAndMouseListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.keyCode) {
                case SWT.ARROW_UP:
                case SWT.ARROW_DOWN:
                case SWT.ARROW_LEFT:
                case SWT.ARROW_RIGHT:
                    commit();
                    break;
            }
        }
    }

    private class MouseListener extends MouseAdapter {
        @Override
        public void mouseDown(MouseEvent e) {
            if (e.button == 1)
                commit();
        }
    }

    /** Text buffer to collect text which is inserted into the viewer */
    private StringBuilder fTextBuffer;
    /** Text buffer to collect viewer content which has been replaced */
    private StringBuilder fPreservedTextBuffer;

    /** The styled text widget */
    private StyledText fStyledText;
    /** Supported undo level */
    private int fUndoLevel;
    /** The currently constructed edit command */
    private UndoableTextChange fCurrent;
    /** The last delete edit command */
    private UndoableTextChange fPreviousDelete;

    /** Indicates inserting state */
    private boolean fInserting = false;
    /** Indicates overwriting state */
    private boolean fOverwriting = false;
    /** Indicates whether the current change belongs to a compound change */
    private boolean fFoldingIntoCompoundChange = false;

    /** The undo stack */
    private LinkedList<UndoableTextChange> fUndoStack = new LinkedList<>();
    /** The redo stack */
    private LinkedList<UndoableTextChange> fRedoStack = new LinkedList<>();

    /** Change tracking */
    private String fLastText = "";
    private boolean fIgnoreTextChanges = false;
    
    /** Listener references for proper disposal */
    private KeyAndMouseListener fKeyListener;
    private MouseListener fMouseListener;
    private ModifyListener fModifyListener;

    /**
     * Creates a new undo manager.
     */
    public StyledTextUndoManager(StyledText styledText, int undoLevel) {
        fStyledText = styledText;
        fUndoLevel = Math.max(0, undoLevel);
        fTextBuffer = new StringBuilder();
        fPreservedTextBuffer = new StringBuilder();

        // Initialize
        fCurrent = new UndoableTextChange(this);
        fPreviousDelete = new UndoableTextChange(this);
        fLastText = fStyledText.getText();

        addListeners();
    }

    /**
     * Registers all necessary listeners.
     */
    private void addListeners() {
        fKeyListener = new KeyAndMouseListener();
        fStyledText.addKeyListener(fKeyListener);
        
        fMouseListener = new MouseListener();
        fStyledText.addMouseListener(fMouseListener);
        
        fModifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!fIgnoreTextChanges) {
                    handleTextChange();
                }
            }
        };
        fStyledText.addModifyListener(fModifyListener);
    }

    /**
     * Removes all listeners.
     */
    private void removeListeners() {
        if (fStyledText != null && !fStyledText.isDisposed()) {
            if (fKeyListener != null) {
                fStyledText.removeKeyListener(fKeyListener);
                fKeyListener = null;
            }
            if (fMouseListener != null) {
                fStyledText.removeMouseListener(fMouseListener);
                fMouseListener = null;
            }
            if (fModifyListener != null) {
                fStyledText.removeModifyListener(fModifyListener);
                fModifyListener = null;
            }
        }
    }

    /**
     * Handle text change by computing what changed and processing it.
     */
    private void handleTextChange() {
        String currentText = fStyledText.getText();
        if (currentText.equals(fLastText)) {
            return;
        }

        // Compute the change
        TextChangeInfo change = computeChange(fLastText, currentText);
        if (change != null) {
            processChange(change.offset, change.offset + change.length, 
                         change.insertedText, change.replacedText);
            fCurrent.pretendCommit();
            
            if (!fFoldingIntoCompoundChange) {
                if (fCurrent.isValid() && fCurrent != getLastAddedCommand()) {
                    addToUndoStack(fCurrent);
                }
            }
        }

        fLastText = currentText;
        fRedoStack.clear(); // Clear redo on new change
    }

    private static class TextChangeInfo {
        int offset;
        int length;
        String insertedText;
        String replacedText;
    }

    /**
     * Compute what changed between old and new text.
     */
    private TextChangeInfo computeChange(String oldText, String newText) {
        // Find common prefix
        int prefixLength = 0;
        int minLength = Math.min(oldText.length(), newText.length());
        while (prefixLength < minLength && 
               oldText.charAt(prefixLength) == newText.charAt(prefixLength)) {
            prefixLength++;
        }

        // Find common suffix
        int suffixLength = 0;
        int oldPos = oldText.length() - 1;
        int newPos = newText.length() - 1;
        while (suffixLength < minLength - prefixLength &&
               oldPos >= prefixLength && newPos >= prefixLength &&
               oldText.charAt(oldPos) == newText.charAt(newPos)) {
            suffixLength++;
            oldPos--;
            newPos--;
        }

        // Extract the changed parts
        int offset = prefixLength;
        String deleted = oldText.substring(prefixLength, oldText.length() - suffixLength);
        String inserted = newText.substring(prefixLength, newText.length() - suffixLength);

        if (deleted.isEmpty() && inserted.isEmpty()) {
            return null;
        }

        TextChangeInfo info = new TextChangeInfo();
        info.offset = offset;
        info.length = deleted.length();
        info.insertedText = inserted;
        info.replacedText = deleted;
        return info;
    }

    public void beginCompoundChange() {
        fFoldingIntoCompoundChange = true;
        commit();
    }

    public void endCompoundChange() {
        fFoldingIntoCompoundChange = false;
        commit();
    }

    /**
     * Closes the current editing command and opens a new one.
     */
    private void commit() {
        if (getLastAddedCommand() != fCurrent) {
            fCurrent.pretendCommit();
            if (fCurrent.isValid())
                addToUndoStack(fCurrent);
        }
        fCurrent.commit();
    }

    /**
     * Reset process change state.
     */
    private void resetProcessChangeState() {
        fInserting = false;
        fOverwriting = false;
        fPreviousDelete.reinitialize();
    }

    /**
     * Checks whether the given text starts with a line delimiter and
     * subsequently contains a white space only.
     */
    private boolean isWhitespaceText(String text) {
        if (text == null || text.isEmpty())
            return false;

        String[] delimiters = getLineDelimiters();
        int index = startsWith(delimiters, text);
        if (index > -1) {
            char c;
            int length = text.length();
            for (int i = delimiters[index].length(); i < length; i++) {
                c = text.charAt(i);
                if (c != ' ' && c != '\t')
                    return false;
            }
            return true;
        }
        return false;
    }

    private static int startsWith(String[] delimiters, String text) {
        for (int i = 0; i < delimiters.length; i++) {
            if (text.startsWith(delimiters[i])) {
                return i;
            }
        }
        return -1;
    }

    private static boolean equals(String[] delimiters, String text) {
        for (String delimiter : delimiters) {
            if (delimiter.equals(text)) {
                return true;
            }
        }
        return false;
    }

    private void processChange(int modelStart, int modelEnd, String insertedText, String replacedText) {
        if (insertedText == null)
            insertedText = "";

        if (replacedText == null)
            replacedText = "";

        int length = insertedText.length();
        int diff = modelEnd - modelStart;

        // normalize
        if (diff < 0) {
            int tmp = modelEnd;
            modelEnd = modelStart;
            modelStart = tmp;
        }

        if (modelStart == modelEnd) {
            // text will be inserted
            if ((length == 1) || isWhitespaceText(insertedText)) {
                // by typing or whitespace
                if (!fInserting || (modelStart != fCurrent.fStart + fTextBuffer.length())) {
                    if (fCurrent.attemptCommit()) {
                        // started new command
                    }
                    fInserting = true;
                }
                if (fCurrent.fStart < 0)
                    fCurrent.fStart = fCurrent.fEnd = modelStart;
                if (length > 0)
                    fTextBuffer.append(insertedText);
            } else if (length >= 0) {
                // by pasting or model manipulation
                if (fCurrent.attemptCommit()) {
                    // started new command
                }
                fCurrent.fStart = fCurrent.fEnd = modelStart;
                fTextBuffer.append(insertedText);
                if (fCurrent.attemptCommit()) {
                    // committed
                }
            }
        } else {
            if (length == 0) {
                // text will be deleted by backspace or DEL key or empty clipboard
                length = replacedText.length();
                String[] delimiters = getLineDelimiters();

                if ((length == 1) || equals(delimiters, replacedText)) {
                    // whereby selection is empty

                    if (fPreviousDelete.fStart == modelStart && fPreviousDelete.fEnd == modelEnd) {
                        // repeated DEL
                        // correct wrong settings of fCurrent
                        if (fCurrent.fStart == modelEnd && fCurrent.fEnd == modelStart) {
                            fCurrent.fStart = modelStart;
                            fCurrent.fEnd = modelEnd;
                        }
                        // append to buffer && extend command range
                        fPreservedTextBuffer.append(replacedText);
                        ++fCurrent.fEnd;

                    } else if (fPreviousDelete.fStart == modelEnd) {
                        // repeated backspace
                        // insert in buffer and extend command range
                        fPreservedTextBuffer.insert(0, replacedText);
                        fCurrent.fStart = modelStart;

                    } else {
                        // either DEL or backspace for the first time
                        if (fCurrent.attemptCommit()) {
                            // started new command
                        }
                        // as we can not decide whether it was DEL or backspace we initialize for backspace
                        fPreservedTextBuffer.append(replacedText);
                        fCurrent.fStart = modelStart;
                        fCurrent.fEnd = modelEnd;
                    }

                    fPreviousDelete.set(modelStart, modelEnd);

                } else if (length > 0) {
                    // whereby selection is not empty
                    if (fCurrent.attemptCommit()) {
                        // started new command
                    }
                    fCurrent.fStart = modelStart;
                    fCurrent.fEnd = modelEnd;
                    fPreservedTextBuffer.append(replacedText);
                }
            } else {
                // text will be replaced
                if (length == 1) {
                    length = replacedText.length();
                    String[] delimiters = getLineDelimiters();

                    if ((length == 1) || equals(delimiters, replacedText)) {
                        // because of overwrite mode or model manipulation
                        if (!fOverwriting || (modelStart != fCurrent.fStart + fTextBuffer.length())) {
                            if (fCurrent.attemptCommit()) {
                                // started new command
                            }
                            fOverwriting = true;
                        }

                        if (fCurrent.fStart < 0)
                            fCurrent.fStart = modelStart;

                        fCurrent.fEnd = modelEnd;
                        fTextBuffer.append(insertedText);
                        fPreservedTextBuffer.append(replacedText);
                        return;
                    }
                }
                // because of typing or pasting whereby selection is not empty
                if (fCurrent.attemptCommit()) {
                    // started new command
                }
                fCurrent.fStart = modelStart;
                fCurrent.fEnd = modelEnd;
                fTextBuffer.append(insertedText);
                fPreservedTextBuffer.append(replacedText);
            }
        }
    }

    /**
     * Returns the line delimiters used by the styled text widget.
     */
    private String[] getLineDelimiters() {
        // Return common line delimiters
        // Order matters: check longer delimiters first
        return new String[] { "\r\n", "\n", "\r" };
    }

    public void setMaximalUndoLevel(int undoLevel) {
        fUndoLevel = Math.max(0, undoLevel);
        trimUndoStack();
    }

    private void addToUndoStack(UndoableTextChange command) {
        // Only add compound changes when folding, or any command when not folding
        if (!fFoldingIntoCompoundChange || 
            (fFoldingIntoCompoundChange && command instanceof UndoableCompoundTextChange)) {
            fUndoStack.addLast(command);
            trimUndoStack();
        }
    }

    private void trimUndoStack() {
        while (fUndoStack.size() > fUndoLevel) {
            fUndoStack.removeFirst();
        }
    }

    private UndoableTextChange getLastAddedCommand() {
        return fUndoStack.isEmpty() ? null : fUndoStack.getLast();
    }

    public void reset() {
        fUndoStack.clear();
        fRedoStack.clear();
        fCurrent = new UndoableTextChange(this);
        fFoldingIntoCompoundChange = false;
        fInserting = false;
        fOverwriting = false;
        fTextBuffer.setLength(0);
        fPreservedTextBuffer.setLength(0);
        fLastText = fStyledText.getText();
    }

    public boolean canRedo() {
        return !fRedoStack.isEmpty() && 
               !fStyledText.isDisposed();
    }

    public boolean canUndo() {
        return !fUndoStack.isEmpty() && 
               !fStyledText.isDisposed();
    }

    public void redo() {
        if (!canRedo()) return;

        UndoableTextChange command = fRedoStack.removeLast();
        fUndoStack.addLast(command);

        fIgnoreTextChanges = true;
        try {
            command.redo();
            fLastText = fStyledText.getText();
        } finally {
            fIgnoreTextChanges = false;
        }
    }

    public void undo() {
        if (!canUndo()) return;

        commit(); // Commit any pending changes first

        UndoableTextChange command = fUndoStack.removeLast();
        fRedoStack.addLast(command);

        fIgnoreTextChanges = true;
        try {
            command.undo();
            fLastText = fStyledText.getText();
        } finally {
            fIgnoreTextChanges = false;
        }
    }

    public void dispose() {
        removeListeners();
        
        if (fUndoStack != null) {
            fUndoStack.clear();
        }
        if (fRedoStack != null) {
            fRedoStack.clear();
        }
        
        fCurrent = null;
        fPreviousDelete = null;
        fTextBuffer = null;
        fPreservedTextBuffer = null;
        fLastText = null;
    }
}