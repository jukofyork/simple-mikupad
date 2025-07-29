import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SimpleMikuPad {
    
    private Display display;
    private Shell shell;
    private StyledText promptText;
    private Text endpointText;
    private Text apiKeyText;
    private Text modelText;
    private Button samplingParamsButton;
    private Label samplingParamsLabel;
    private Button generateButton;
    private Button cancelButton;
    private Label statusLabel;
    private Button colorToggleButton;
    
    // Session management UI
    private Combo sessionCombo;
    private Button newSessionButton;
    private Button deleteSessionButton;
    private Button renameSessionButton;
    private Button cloneSessionButton;
    private Button exportSessionButton;
    private Button importSessionButton;
    
    private HttpClientWrapper httpClient;
    private SessionManager sessionManager;
    private volatile boolean isCancelled = false;
    private boolean coloringEnabled = true;
    private boolean isLoadingSession = false; // Prevent auto-save during session loading
    
    // Color management
    private List<Color> colors = new ArrayList<>();
    
    // Token information storage for tooltips
    private Map<Integer, TokenInfo> tokenMap = new HashMap<>();
    private Shell currentTooltip;
    
    private TokenInfo currentHoverToken;
    
    public static void main(String[] args) {
        new SimpleMikuPad().run();
    }
    
    public void run() {
        display = new Display();
        shell = new Shell(display);
        
        initializeHttpClient();
        initializeSessionManager();
        createColors();
        createUI();
        loadCurrentSession();
        
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        // Clean up colors
        for (Color color : colors) {
            color.dispose();
        }
        
        // Save current session state before exit
        saveCurrentSessionState();
        display.dispose();
    }
    
    private void createColors() {
        // Create a range of colors from red (low probability) to green (high probability)
        for (int i = 0; i <= 100; i++) {
            float ratio = i / 100.0f;
            RGB rgb;
            
            if (ratio < 0.5f) {
                // Red to Yellow (0.0 to 0.5)
                float localRatio = ratio * 2;
                rgb = new RGB(
                    255,
                    (int)(255 * localRatio),
                    0
                );
            } else {
                // Yellow to Green (0.5 to 1.0)
                float localRatio = (ratio - 0.5f) * 2;
                rgb = new RGB(
                    (int)(255 * (1 - localRatio)),
                    255,
                    0
                );
            }
            
            colors.add(new Color(display, rgb));
        }
    }
    
    private Color getColorForProbability(double probability) {
        // Clamp probability between 0 and 1
        probability = Math.max(0, Math.min(1, probability));
        int index = (int)(probability * 100);
        return colors.get(index);
    }
    
    private void initializeHttpClient() {
        httpClient = new HttpClientWrapper(
            Duration.ofSeconds(30),
            Duration.ofMinutes(5)
        );
    }
    
    private void initializeSessionManager() {
        sessionManager = new SessionManager();
    }
    
    private void createUI() {
        shell.setText("Simple MikuPad with Token Highlighting");
        shell.setSize(900, 750);
        shell.setLayout(new GridLayout(1, false));
        
        createSessionGroup();
        createSettingsGroup();
        createPromptArea();
        createControlButtons();
        createStatusBar();
    }
    
    private void createSessionGroup() {
        Group sessionGroup = new Group(shell, SWT.NONE);
        sessionGroup.setText("Session Management");
        sessionGroup.setLayout(new GridLayout(7, false));
        sessionGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Session dropdown
        new Label(sessionGroup, SWT.NONE).setText("Current Session:");
        sessionCombo = new Combo(sessionGroup, SWT.READ_ONLY);
        sessionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        sessionCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchToSelectedSession();
            }
        });
        
        // Session management buttons
        newSessionButton = new Button(sessionGroup, SWT.PUSH);
        newSessionButton.setText("New");
        newSessionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createNewSession();
            }
        });
        
        deleteSessionButton = new Button(sessionGroup, SWT.PUSH);
        deleteSessionButton.setText("Delete");
        deleteSessionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteCurrentSession();
            }
        });
        
        renameSessionButton = new Button(sessionGroup, SWT.PUSH);
        renameSessionButton.setText("Rename");
        renameSessionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                renameCurrentSession();
            }
        });
        
        cloneSessionButton = new Button(sessionGroup, SWT.PUSH);
        cloneSessionButton.setText("Clone");
        cloneSessionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                cloneCurrentSession();
            }
        });
        
        exportSessionButton = new Button(sessionGroup, SWT.PUSH);
        exportSessionButton.setText("Export");
        exportSessionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                exportCurrentSession();
            }
        });
        
        importSessionButton = new Button(sessionGroup, SWT.PUSH);
        importSessionButton.setText("Import");
        importSessionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                importSession();
            }
        });
    }
    
    private void createSettingsGroup() {
        Group settingsGroup = new Group(shell, SWT.NONE);
        settingsGroup.setText("Settings");
        settingsGroup.setLayout(new GridLayout(2, false));
        settingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Endpoint
        new Label(settingsGroup, SWT.NONE).setText("Endpoint:");
        endpointText = new Text(settingsGroup, SWT.BORDER);
        endpointText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        endpointText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                autoSaveSessionState();
            }
        });
        
        // API Key
        new Label(settingsGroup, SWT.NONE).setText("API Key:");
        apiKeyText = new Text(settingsGroup, SWT.BORDER | SWT.PASSWORD);
        apiKeyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        apiKeyText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                autoSaveSessionState();
            }
        });
        
        // Model
        new Label(settingsGroup, SWT.NONE).setText("Model:");
        modelText = new Text(settingsGroup, SWT.BORDER);
        modelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        modelText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                autoSaveSessionState();
            }
        });
        
        // Sampling Parameters
        new Label(settingsGroup, SWT.NONE).setText("Sampling Parameters:");
        Composite samplingComp = new Composite(settingsGroup, SWT.NONE);
        samplingComp.setLayout(new GridLayout(2, false));
        samplingComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        samplingParamsButton = new Button(samplingComp, SWT.PUSH);
        samplingParamsButton.setText("Configure...");
        samplingParamsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openSamplingParametersDialog();
            }
        });
        
        samplingParamsLabel = new Label(samplingComp, SWT.NONE);
        samplingParamsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        updateSamplingParamsLabel();
    }
    
    private void createPromptArea() {
        Group promptGroup = new Group(shell, SWT.NONE);
        promptGroup.setText("Prompt");
        promptGroup.setLayout(new GridLayout(1, false));
        promptGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Prompt text area
        promptText = new StyledText(promptGroup, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        promptText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        promptText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                autoSaveSessionState();
                // Only clear token highlighting when user manually edits (not during generation or session loading)
                if (!isLoadingSession && generateButton.getEnabled()) {
                    clearTokenColoring();
                }
            }
        });
        
        // Add mouse hover listener for tooltips
        promptText.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                handleMouseHover(e);
            }
        });
        
        initializeContextMenu();
    }
    
    private void handleMouseHover(MouseEvent e) {
        if (!coloringEnabled) return;
        
        try {
            int offset = promptText.getOffsetAtLocation(new Point(e.x, e.y));
            TokenInfo tokenInfo = findTokenAtOffset(offset);
            
            if (tokenInfo != null) {
                // Only show tooltip if we're hovering over a different token
                if (currentHoverToken != tokenInfo) {
                    currentHoverToken = tokenInfo;
                    showTooltip(e.x, e.y, tokenInfo);
                }
            } else {
                currentHoverToken = null;
                hideTooltip();
            }
        } catch (IllegalArgumentException ex) {
            // Mouse is not over text
            currentHoverToken = null;
            hideTooltip();
        }
    }
    
    private TokenInfo findTokenAtOffset(int offset) {
        // Find the token that contains this offset
        for (Map.Entry<Integer, TokenInfo> entry : tokenMap.entrySet()) {
            int tokenStart = entry.getKey();
            TokenInfo tokenInfo = entry.getValue();
            int tokenEnd = tokenStart + tokenInfo.text.length();
            
            if (offset >= tokenStart && offset < tokenEnd) {
                return tokenInfo;
            }
        }
        return null;
    }
    
    private void showTooltip(int x, int y, TokenInfo tokenInfo) {
        hideTooltip(); // Hide any existing tooltip
        
        // Create a simple shell-based tooltip
        currentTooltip = new Shell(shell, SWT.ON_TOP | SWT.TOOL);
        currentTooltip.setLayout(new GridLayout(1, false));
        currentTooltip.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        
        Label titleLabel = new Label(currentTooltip, SWT.NONE);
        titleLabel.setText("Token: \"" + escapeForTooltip(tokenInfo.text) + "\"");
        titleLabel.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        titleLabel.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        
        Label probLabel = new Label(currentTooltip, SWT.NONE);
        probLabel.setText(String.format("Probability: %.1f%%", tokenInfo.probability * 100));
        probLabel.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        probLabel.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        
        if (tokenInfo.alternatives != null && !tokenInfo.alternatives.isEmpty()) {
            Label separator = new Label(currentTooltip, SWT.SEPARATOR | SWT.HORIZONTAL);
            separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            
            Label altLabel = new Label(currentTooltip, SWT.NONE);
            StringBuilder message = new StringBuilder("Alternatives:\n");
            
            for (TokenAlternative alt : tokenInfo.alternatives) {
                double percentage = alt.probability * 100;
                message.append(String.format("%.1f%% - \"%s\"\n", percentage, escapeForTooltip(alt.token)));
            }
            
            altLabel.setText(message.toString());
            altLabel.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            altLabel.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        }
        
        currentTooltip.pack();
        
        // Convert widget coordinates to display coordinates
        Point displayPoint = promptText.toDisplay(x, y);
        currentTooltip.setLocation(displayPoint.x + 10, displayPoint.y + 10);
        currentTooltip.setVisible(true);
    }
    
    private void hideTooltip() {
        if (currentTooltip != null && !currentTooltip.isDisposed()) {
            currentTooltip.setVisible(false);
            currentTooltip.dispose();
            currentTooltip = null;
        }
    }
    
    private String escapeForTooltip(String text) {
        // Escape special characters for tooltip display
        return text.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r");
    }
    
    private void createControlButtons() {
        Composite buttonComposite = new Composite(shell, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(3, false));
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        generateButton = new Button(buttonComposite, SWT.PUSH);
        generateButton.setText("Generate");
        generateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        generateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                generateCompletion();
            }
        });
        
        cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.setEnabled(false);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                cancelGeneration();
            }
        });
        
        colorToggleButton = new Button(buttonComposite, SWT.TOGGLE);
        colorToggleButton.setText("Token Coloring");
        colorToggleButton.setSelection(coloringEnabled);
        colorToggleButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        colorToggleButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                coloringEnabled = colorToggleButton.getSelection();
                if (!coloringEnabled) {
                    clearTokenColoring();
                }
            }
        });
    }
    
    private void createStatusBar() {
        statusLabel = new Label(shell, SWT.NONE);
        statusLabel.setText("Ready");
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }
    
    /**
     * Initializes the context menu for the prompt text area. Adds standard text
     * editing operations such as cut, copy, and paste.
     */
    private void initializeContextMenu() {
        Menu contextMenu = new Menu(promptText);
        
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
        
        promptText.setMenu(contextMenu);
    }

    /**
     * Executes a specified text operation on the prompt text area.
     *
     * @param operationType the type of operation to execute
     */
    private void executeTextOperation(String operationType) {
        if (promptText.isDisposed()) return;
        
        switch (operationType) {
            case "cut":
                promptText.cut();
                break;
            case "copy":
                promptText.copy();
                break;
            case "paste":
                promptText.paste();
                break;
            case "selectAll":
                promptText.selectAll();
                break;
        }
    }

    // Sampling Parameters Methods
    
    private void openSamplingParametersDialog() {
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession != null) {
            SamplingParametersDialog dialog = new SamplingParametersDialog(shell, currentSession.getSamplingParams());
            if (dialog.open()) {
                currentSession.setSamplingParams(dialog.getParameters());
                updateSamplingParamsLabel();
                autoSaveSessionState();
                updateStatus("Sampling parameters updated");
            }
        }
    }
    
    private void updateSamplingParamsLabel() {
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession != null && samplingParamsLabel != null) {
            SamplingParameters params = currentSession.getSamplingParams();
            String summary = String.format("Temp: %.2f, Max: %d, Top-P: %.2f, Top-K: %d", 
                params.getTemperature(), params.getMaxTokens(), params.getTopP(), params.getTopK());
            samplingParamsLabel.setText(summary);
        }
    }
    
    // Session Management Methods
    
    private void loadCurrentSession() {
        refreshSessionCombo();
        loadSessionIntoUI(sessionManager.getCurrentSession());
    }
    
    private void refreshSessionCombo() {
        sessionCombo.removeAll();
        List<Session> sessions = sessionManager.getAllSessions();
        Session currentSession = sessionManager.getCurrentSession();
        
        int selectedIndex = 0;
        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);
            sessionCombo.add(session.toString());
            if (session.getId().equals(currentSession.getId())) {
                selectedIndex = i;
            }
        }
        
        sessionCombo.select(selectedIndex);
        
        // Enable/disable delete button based on session count
        deleteSessionButton.setEnabled(sessions.size() > 1);
    }
    
    private void loadSessionIntoUI(Session session) {
        if (session == null) return;
        
        isLoadingSession = true;
        try {
            endpointText.setText(session.getEndpoint());
            apiKeyText.setText(session.getApiKey());
            modelText.setText(session.getModel());
            promptText.setText(session.getPromptText());
            
            // Update sampling parameters label
            updateSamplingParamsLabel();
            
            // Clear token coloring when loading new session
            clearTokenColoring();
            
            updateStatus("Loaded session: " + session.getName());
        } finally {
            isLoadingSession = false;
        }
    }
    
    private void saveCurrentSessionState() {
        if (isLoadingSession) return;
        
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession != null && !endpointText.isDisposed()) {
            currentSession.setEndpoint(endpointText.getText());
            currentSession.setApiKey(apiKeyText.getText());
            currentSession.setModel(modelText.getText());
            currentSession.setPromptText(promptText.getText());
            
            sessionManager.saveCurrentState();
        }
    }
    
    private void autoSaveSessionState() {
        // Auto-save with a slight delay to avoid excessive saves during typing
        display.timerExec(500, () -> saveCurrentSessionState());
    }
    
    private void switchToSelectedSession() {
        if (isLoadingSession) return;
        
        // Save current session state first
        saveCurrentSessionState();
        
        // Get selected session
        int selectedIndex = sessionCombo.getSelectionIndex();
        if (selectedIndex >= 0) {
            List<Session> sessions = sessionManager.getAllSessions();
            if (selectedIndex < sessions.size()) {
                Session selectedSession = sessions.get(selectedIndex);
                sessionManager.setCurrentSession(selectedSession.getId());
                loadSessionIntoUI(selectedSession);
            }
        }
    }
    
    private void createNewSession() {
        String name = promptForSessionName("New Session");
        if (name != null) {
            saveCurrentSessionState(); // Save current before switching
            Session newSession = sessionManager.createSession(name);
            sessionManager.setCurrentSession(newSession.getId());
            refreshSessionCombo();
            loadSessionIntoUI(newSession);
        }
    }
    
    private void deleteCurrentSession() {
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession != null) {
            MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
            messageBox.setMessage("Are you sure you want to delete session '" + currentSession.getName() + "'?");
            messageBox.setText("Delete Session");
            
            if (messageBox.open() == SWT.YES) {
                if (sessionManager.deleteSession(currentSession.getId())) {
                    refreshSessionCombo();
                    loadSessionIntoUI(sessionManager.getCurrentSession());
                    updateStatus("Deleted session: " + currentSession.getName());
                }
            }
        }
    }
    
    private void renameCurrentSession() {
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession != null) {
            String newName = promptForSessionName(currentSession.getName());
            if (newName != null && !newName.equals(currentSession.getName())) {
                sessionManager.renameSession(currentSession.getId(), newName);
                refreshSessionCombo();
                updateStatus("Renamed session to: " + newName);
            }
        }
    }
    
    private void cloneCurrentSession() {
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession != null) {
            saveCurrentSessionState(); // Save current state first
            Session clonedSession = sessionManager.cloneSession(currentSession.getId());
            if (clonedSession != null) {
                sessionManager.setCurrentSession(clonedSession.getId());
                refreshSessionCombo();
                loadSessionIntoUI(clonedSession);
                updateStatus("Cloned session: " + clonedSession.getName());
            }
        }
    }
    
    private void exportCurrentSession() {
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession != null) {
            saveCurrentSessionState(); // Save current state first
            
            FileDialog dialog = new FileDialog(shell, SWT.SAVE);
            dialog.setFilterExtensions(new String[]{"*.json"});
            dialog.setFilterNames(new String[]{"JSON Files (*.json)"});
            dialog.setFileName(currentSession.getName() + ".json");
            
            String filename = dialog.open();
            if (filename != null) {
                try {
                    sessionManager.exportSession(currentSession.getId(), new java.io.File(filename));
                    updateStatus("Exported session to: " + filename);
                } catch (Exception e) {
                    MessageBox messageBox = new MessageBox(shell, SWT.ERROR | SWT.OK);
                    messageBox.setMessage("Failed to export session: " + e.getMessage());
                    messageBox.setText("Export Error");
                    messageBox.open();
                }
            }
        }
    }
    
    private void importSession() {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[]{"*.json"});
        dialog.setFilterNames(new String[]{"JSON Files (*.json)"});
        
        String filename = dialog.open();
        if (filename != null) {
            try {
                Session importedSession = sessionManager.importSession(new java.io.File(filename));
                sessionManager.setCurrentSession(importedSession.getId());
                refreshSessionCombo();
                loadSessionIntoUI(importedSession);
                updateStatus("Imported session: " + importedSession.getName());
            } catch (Exception e) {
                MessageBox messageBox = new MessageBox(shell, SWT.ERROR | SWT.OK);
                messageBox.setMessage("Failed to import session: " + e.getMessage());
                messageBox.setText("Import Error");
                messageBox.open();
            }
        }
    }
    
    private String promptForSessionName(String defaultName) {
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Session Name");
        dialog.setSize(300, 150);
        dialog.setLayout(new GridLayout(2, false));
        
        // Center the dialog
        Point parentSize = shell.getSize();
        Point parentLocation = shell.getLocation();
        Point dialogSize = dialog.getSize();
        int x = parentLocation.x + (parentSize.x - dialogSize.x) / 2;
        int y = parentLocation.y + (parentSize.y - dialogSize.y) / 2;
        dialog.setLocation(x, y);
        
        Label label = new Label(dialog, SWT.NONE);
        label.setText("Enter session name:");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        Text nameText = new Text(dialog, SWT.BORDER);
        nameText.setText(defaultName);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        nameText.selectAll();
        
        final String[] result = new String[1];
        
        Button okButton = new Button(dialog, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String name = nameText.getText().trim();
                if (!name.isEmpty()) {
                    result[0] = name;
                    dialog.close();
                }
            }
        });
        
        Button cancelButton = new Button(dialog, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dialog.close();
            }
        });
        
        // Set OK as default button and handle Enter key
        dialog.setDefaultButton(okButton);
        nameText.addListener(SWT.DefaultSelection, e -> okButton.notifyListeners(SWT.Selection, new Event()));
        
        dialog.open();
        nameText.setFocus();
        
        Display display = dialog.getDisplay();
        while (!dialog.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        return result[0];
    }
    
    private void clearTokenColoring() {
        display.asyncExec(() -> {
            if (!promptText.isDisposed()) {
                promptText.setStyleRanges(new StyleRange[0]);
                tokenMap.clear();
                hideTooltip();
            }
        });
    }
    
    private void generateCompletion() {
        isCancelled = false;
        generateButton.setEnabled(false);
        cancelButton.setEnabled(true);
        tokenMap.clear(); // Clear previous token data
        
        String endpoint = endpointText.getText().trim();
        String apiKey = apiKeyText.getText().trim();
        String model = modelText.getText().trim();
        String prompt = promptText.getText();
        
        // Get sampling parameters from current session
        Session currentSession = sessionManager.getCurrentSession();
        SamplingParameters samplingParams = currentSession.getSamplingParams();
        
        updateStatus("Generating completion...");
        
        CompletableFuture.runAsync(() -> {
            try {
                // Build request JSON using sampling parameters
                JsonObject request = samplingParams.toJson();
                request.addProperty("prompt", prompt);
                request.addProperty("stream", true);
                request.addProperty("logprobs", 5);
                
                if (!model.isEmpty()) {
                    request.addProperty("model", model);
                }
                
                String requestBody = new Gson().toJson(request);
                
                // Send streaming request
                URI uri = URI.create(endpoint + "/v1/completions");
                HttpResponse<InputStream> response = httpClient.sendRequest(
                    uri, 
                    apiKey.isEmpty() ? null : apiKey, 
                    requestBody, 
                    true
                );
                
                if (isCancelled) return;
                
                // Process streaming response
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                    String line;
                    while ((line = reader.readLine()) != null && !isCancelled) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            
                            try {
                                JsonObject chunk = JsonParser.parseString(data).getAsJsonObject();
                                if (chunk.has("choices") && chunk.getAsJsonArray("choices").size() > 0) {
                                    JsonObject choice = chunk.getAsJsonArray("choices").get(0).getAsJsonObject();
                                    
                                    if (choice.has("text")) {
                                        String token = choice.get("text").getAsString();
                                        double probability = extractProbability(choice);
                                        List<TokenAlternative> alternatives = extractAlternatives(choice);
                                        
                                        // Update UI on main thread
                                        final String finalToken = token;
                                        final double finalProbability = probability;
                                        final List<TokenAlternative> finalAlternatives = alternatives;
                                        display.asyncExec(() -> {
                                            if (!isCancelled && !promptText.isDisposed()) {
                                                appendSingleToken(finalToken, finalProbability, finalAlternatives);
                                            }
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                // Skip malformed chunks
                                System.err.println("Error parsing chunk: " + e.getMessage());
                            }
                        }
                    }
                }
                
                display.asyncExec(() -> {
                    if (!isCancelled) {
                        updateStatus("Generation completed");
                        // Auto-save session after generation
                        saveCurrentSessionState();
                    }
                    resetButtons();
                });
                
            } catch (Exception ex) {
                display.asyncExec(() -> {
                    updateStatus("Error: " + ex.getMessage());
                    resetButtons();
                });
            }
        });
    }
    
    private double extractProbability(JsonObject choice) {
        if (choice.has("logprobs") && !choice.get("logprobs").isJsonNull()) {
            JsonObject logprobs = choice.getAsJsonObject("logprobs");
            
            if (logprobs.has("content")) {
                JsonArray content = logprobs.getAsJsonArray("content");
                if (content.size() > 0) {
                    JsonObject currentToken = content.get(content.size() - 1).getAsJsonObject();
                    if (currentToken.has("logprob")) {
                        double logprob = currentToken.get("logprob").getAsDouble();
                        return Math.exp(logprob);
                    }
                }
            }
            
            // Fallback methods (keep the existing ones)
            if (logprobs.has("token_logprobs")) {
                JsonArray tokenLogprobs = logprobs.getAsJsonArray("token_logprobs");
                if (tokenLogprobs.size() > 0) {
                    JsonElement logprobElement = tokenLogprobs.get(tokenLogprobs.size() - 1);
                    if (!logprobElement.isJsonNull()) {
                        double logprob = logprobElement.getAsDouble();
                        return Math.exp(logprob);
                    }
                }
            }
        }
        
        return Math.random();
    }
    
    private List<TokenAlternative> extractAlternatives(JsonObject choice) {
        List<TokenAlternative> alternatives = new ArrayList<>();
        
        if (choice.has("logprobs") && !choice.get("logprobs").isJsonNull()) {
            JsonObject logprobs = choice.getAsJsonObject("logprobs");
            
            if (logprobs.has("content")) {
                JsonArray content = logprobs.getAsJsonArray("content");
                if (content.size() > 0) {
                    // Get the last content item (current token)
                    JsonObject currentToken = content.get(content.size() - 1).getAsJsonObject();
                    
                    if (currentToken.has("top_logprobs")) {
                        JsonArray topLogprobs = currentToken.getAsJsonArray("top_logprobs");
                        
                        for (int i = 0; i < topLogprobs.size(); i++) {
                            JsonObject altToken = topLogprobs.get(i).getAsJsonObject();
                            if (altToken.has("token") && altToken.has("logprob")) {
                                String tokenText = altToken.get("token").getAsString();
                                double logprob = altToken.get("logprob").getAsDouble();
                                double probability = Math.exp(logprob);
                                alternatives.add(new TokenAlternative(tokenText, probability));
                            }
                        }
                        
                        // Sort alternatives by probability (highest first)
                        alternatives.sort((a, b) -> Double.compare(b.probability, a.probability));
                    }
                }
            }
        }
        
        return alternatives;
    }
    
    private void appendSingleToken(String token, double probability, List<TokenAlternative> alternatives) {
        if (promptText.isDisposed()) return;
        
        int startOffset = promptText.getCharCount();
        promptText.append(token);
        
        // Store token info for tooltip lookup
        TokenInfo tokenInfo = new TokenInfo(token, probability, alternatives);
        tokenMap.put(startOffset, tokenInfo);
        
        if (coloringEnabled) {
            StyleRange style = new StyleRange();
            style.start = startOffset;
            style.length = token.length();
            style.background = getColorForProbability(probability);
            
            promptText.setStyleRange(style);
        }
    }
    
    private void cancelGeneration() {
        isCancelled = true;
        updateStatus("Cancelling...");
        resetButtons();
    }
    
    private void resetButtons() {
        generateButton.setEnabled(true);
        cancelButton.setEnabled(false);
    }
    
    private void updateStatus(String message) {
        if (!statusLabel.isDisposed()) {
            statusLabel.setText(message);
        }
    }
    
    // Helper class to store token information
    private static class TokenInfo {
        final String text;
        final double probability;
        final List<TokenAlternative> alternatives;
        
        TokenInfo(String text, double probability, List<TokenAlternative> alternatives) {
            this.text = text;
            this.probability = probability;
            this.alternatives = alternatives;
        }
    }
    
    // Helper class for token alternatives
    private static class TokenAlternative {
        final String token;
        final double probability;
        
        TokenAlternative(String token, double probability) {
            this.token = token;
            this.probability = probability;
        }
    }
}