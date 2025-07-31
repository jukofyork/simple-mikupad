import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class TextOperationsManager {
    
    private SimpleMikuPad app;
    
    public TextOperationsManager(SimpleMikuPad app) {
        this.app = app;
    }
    
    public void initializeContextMenu() {
        setupFontControls();
        setupKeyListener();
        Menu contextMenu = new Menu(app.getPromptText());
        
        // Template operations
        MenuItem wrapSystemItem = new MenuItem(contextMenu, SWT.PUSH);
        wrapSystemItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                wrapWithTemplate("system");
            }
        });
        
        MenuItem wrapInstructionItem = new MenuItem(contextMenu, SWT.PUSH);
        wrapInstructionItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                wrapWithTemplate("instruction");
            }
        });
        
        MenuItem addEosItem = new MenuItem(contextMenu, SWT.PUSH);
        addEosItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addEosToken();
            }
        });
        
        // Separator
        new MenuItem(contextMenu, SWT.SEPARATOR);
        
        // Undo
        MenuItem undoItem = new MenuItem(contextMenu, SWT.PUSH);
        undoItem.setText("Undo");
        undoItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                executeTextOperation("undo");
            }
        });
        
        // Redo
        MenuItem redoItem = new MenuItem(contextMenu, SWT.PUSH);
        redoItem.setText("Redo");
        redoItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                executeTextOperation("redo");
            }
        });
        
        // Separator
        new MenuItem(contextMenu, SWT.SEPARATOR);
        
        // Cut
        MenuItem cutItem = new MenuItem(contextMenu, SWT.PUSH);
        cutItem.setText("Cut");
        cutItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                executeTextOperation("cut");
            }
        });
        
        // Copy
        MenuItem copyItem = new MenuItem(contextMenu, SWT.PUSH);
        copyItem.setText("Copy");
        copyItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                executeTextOperation("copy");
            }
        });
        
        // Paste
        MenuItem pasteItem = new MenuItem(contextMenu, SWT.PUSH);
        pasteItem.setText("Paste");
        pasteItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                executeTextOperation("paste");
            }
        });
        
        // Separator
        new MenuItem(contextMenu, SWT.SEPARATOR);
        
        // Select All
        MenuItem selectAllItem = new MenuItem(contextMenu, SWT.PUSH);
        selectAllItem.setText("Select All");
        selectAllItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                executeTextOperation("selectAll");
            }
        });
        
        // Update menu text based on selection when menu is shown
        contextMenu.addListener(SWT.Show, new org.eclipse.swt.widgets.Listener() {
            @Override
            public void handleEvent(Event event) {
                boolean hasSelection = app.getPromptText().getSelectionCount() > 0;
                
                if (hasSelection) {
                    wrapSystemItem.setText("Wrap as System Message");
                    wrapInstructionItem.setText("Wrap as User Prompt");
                    addEosItem.setText("Replace with EOS");
                } else {
                    wrapSystemItem.setText("Insert System Message");
                    wrapInstructionItem.setText("Insert User Prompt");
                    addEosItem.setText("Insert EOS");
                }
            }
        });
        
        app.getPromptText().setMenu(contextMenu);
    }
    
    private void wrapWithTemplate(String type) {
        if (app.getPromptText().isDisposed()) return;
        
        Session currentSession = app.getSessionManager().getCurrentSession();
        Settings settings = currentSession.getSettings();
        String prefix, suffix;
        
        if ("system".equals(type)) {
            prefix = Constants.processEscapeSequences(settings.getTemplateSysPrefix());
            suffix = Constants.processEscapeSequences(settings.getTemplateSysSuffix());
        } else { // instruction
            prefix = Constants.processEscapeSequences(settings.getTemplateInstPrefix());
            suffix = Constants.processEscapeSequences(settings.getTemplateInstSuffix());
        }
        
        Point selection = app.getPromptText().getSelection();
        String selectedText = app.getPromptText().getSelectionText();
        
        String replacement = prefix + selectedText + suffix;
        app.getPromptText().replaceTextRange(selection.x, selection.y - selection.x, replacement);
        
        // Position cursor after the inserted text
        app.getPromptText().setSelection(selection.x + replacement.length());
    }
    
    private void addEosToken() {
        if (app.getPromptText().isDisposed()) return;
        
        Session currentSession = app.getSessionManager().getCurrentSession();
        Settings settings = currentSession.getSettings();
        String eosToken = Constants.processEscapeSequences(settings.getTemplateEos());
        
        Point selection = app.getPromptText().getSelection();
        app.getPromptText().replaceTextRange(selection.x, selection.y - selection.x, eosToken);
        
        // Position cursor after the inserted token
        app.getPromptText().setSelection(selection.x + eosToken.length());
    }
    
    private void setupKeyListener() {
        app.getPromptText().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 'z' || e.keyCode == 'Z') {
                    if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1) {
                        // Ctrl+Z for undo
                        executeTextOperation("undo");
                        e.doit = false;
                    } else if ((e.stateMask & SWT.MODIFIER_MASK) == (SWT.MOD1 | SWT.MOD2)) {
                        // Ctrl+Shift+Z for redo
                        executeTextOperation("redo");
                        e.doit = false;
                    }
                } else if (e.keyCode == 'a' || e.keyCode == 'A') {
                    if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1) {
                        // Ctrl+A for select all
                        executeTextOperation("selectAll");
                        e.doit = false;
                    }
                }
            }
        });
    }
    
    private void setupFontControls() {
        app.getFontSizeSpinner().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateFont();
            }
        });
        
        app.getFontNameCombo().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFont();
            }
        });
        
        app.getFontBoldButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFont();
            }
        });
        
        app.getFontItalicButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFont();
            }
        });
        
        // Set initial font
        updateFont();
    }
    
    private void updateFont() {
        int fontSize = app.getFontSizeSpinner().getSelection();
        String fontName = app.getFontNameCombo().getText();
        boolean bold = app.getFontBoldButton().getSelection();
        boolean italic = app.getFontItalicButton().getSelection();
        
        int style = SWT.NORMAL;
        if (bold) style |= SWT.BOLD;
        if (italic) style |= SWT.ITALIC;
        
        FontData fontData = new FontData(fontName, fontSize, style);
        Font newFont = new Font(app.getDisplay(), fontData);
        app.getPromptText().setFont(newFont);
    }
    
    private void executeTextOperation(String operationType) {
        if (app.getPromptText().isDisposed()) return;
        
        switch (operationType) {
            case "undo":
                if (app.getUndoManager() != null && app.getUndoManager().canUndo()) {
                    app.getUndoManager().undo();
                }
                break;
            case "redo":
                if (app.getUndoManager() != null && app.getUndoManager().canRedo()) {
                    app.getUndoManager().redo();
                }
                break;
            case "cut":
                app.getPromptText().cut();
                break;
            case "copy":
                app.getPromptText().copy();
                break;
            case "paste":
                app.getPromptText().paste();
                break;
            case "selectAll":
                app.getPromptText().selectAll();
                break;
        }
    }
}