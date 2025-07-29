import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Combo;

public class SimpleMikuPad {
    
    private Display display;
    private Shell shell;
    private UIManager uiManager;
    private SessionUIManager sessionUIManager;
    private TokenManager tokenManager;
    private GenerationManager generationManager;
    private TextOperationsManager textOperationsManager;
    
    private SessionManager sessionManager;
    private HttpClientWrapper httpClient;
    
    // UI Components (exposed for managers)
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
    private Combo sessionCombo;
    private Button newSessionButton;
    private Button deleteSessionButton;
    private Button renameSessionButton;
    private Button cloneSessionButton;
    private Button exportSessionButton;
    private Button importSessionButton;
    private Spinner fontSizeSpinner;
    private Combo fontNameCombo;
    private Button fontBoldButton;
    private Button fontItalicButton;
    
    private boolean isLoadingSession = false;
    
    public static void main(String[] args) {
        new SimpleMikuPad().run();
    }
    
    public void run() {
        display = new Display();
        shell = new Shell(display);
        
        initializeComponents();
        createUI();
        loadCurrentSession();
        
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        cleanup();
    }
    
    private void initializeComponents() {
        httpClient = new HttpClientWrapper(
            java.time.Duration.ofSeconds(30),
            java.time.Duration.ofMinutes(5)
        );
        sessionManager = new SessionManager();
        
        // Create UI manager first
        uiManager = new UIManager(this);
        // Create session UI manager early (needed for setupSessionUI)
        sessionUIManager = new SessionUIManager(this);
    }
    
    private void initializeManagers() {
        sessionUIManager = new SessionUIManager(this);
        tokenManager = new TokenManager(this);
        generationManager = new GenerationManager(this);
        textOperationsManager = new TextOperationsManager(this);
    }
    
    private void createUI() {
        uiManager.createUI();
        sessionUIManager.setupSessionUI();
        initializeManagers();
        
        // Setup event listeners after all managers and UI are created
        tokenManager.setupEventListeners();
        generationManager.setupEventListeners();
        textOperationsManager.initializeContextMenu();
    }
    
    private void loadCurrentSession() {
        sessionUIManager.loadCurrentSession();
    }
    
    private void cleanup() {
        if (tokenManager != null) {
            tokenManager.dispose();
        }
        sessionUIManager.saveCurrentSessionState();
        display.dispose();
    }
    
    // Getters for managers
    public Display getDisplay() { return display; }
    public Shell getShell() { return shell; }
    public SessionManager getSessionManager() { return sessionManager; }
    public HttpClientWrapper getHttpClient() { return httpClient; }
    public TokenManager getTokenManager() { return tokenManager; }
    public GenerationManager getGenerationManager() { return generationManager; }
    
    // UI Component getters
    public StyledText getPromptText() { return promptText; }
    public Text getEndpointText() { return endpointText; }
    public Text getApiKeyText() { return apiKeyText; }
    public Text getModelText() { return modelText; }
    public Button getSamplingParamsButton() { return samplingParamsButton; }
    public Label getSamplingParamsLabel() { return samplingParamsLabel; }
    public Button getGenerateButton() { return generateButton; }
    public Button getCancelButton() { return cancelButton; }
    public Label getStatusLabel() { return statusLabel; }
    public Button getColorToggleButton() { return colorToggleButton; }
    public Combo getSessionCombo() { return sessionCombo; }
    public Button getNewSessionButton() { return newSessionButton; }
    public Button getDeleteSessionButton() { return deleteSessionButton; }
    public Button getRenameSessionButton() { return renameSessionButton; }
    public Button getCloneSessionButton() { return cloneSessionButton; }
    public Button getExportSessionButton() { return exportSessionButton; }
    public Button getImportSessionButton() { return importSessionButton; }
    public Spinner getFontSizeSpinner() { return fontSizeSpinner; }
    public Combo getFontNameCombo() { return fontNameCombo; }
    public Button getFontBoldButton() { return fontBoldButton; }
    public Button getFontItalicButton() { return fontItalicButton; }
    
    // UI Component setters
    public void setPromptText(StyledText promptText) { this.promptText = promptText; }
    public void setEndpointText(Text endpointText) { this.endpointText = endpointText; }
    public void setApiKeyText(Text apiKeyText) { this.apiKeyText = apiKeyText; }
    public void setModelText(Text modelText) { this.modelText = modelText; }
    public void setSamplingParamsButton(Button samplingParamsButton) { this.samplingParamsButton = samplingParamsButton; }
    public void setSamplingParamsLabel(Label samplingParamsLabel) { this.samplingParamsLabel = samplingParamsLabel; }
    public void setGenerateButton(Button generateButton) { this.generateButton = generateButton; }
    public void setCancelButton(Button cancelButton) { this.cancelButton = cancelButton; }
    public void setStatusLabel(Label statusLabel) { this.statusLabel = statusLabel; }
    public void setColorToggleButton(Button colorToggleButton) { this.colorToggleButton = colorToggleButton; }
    public void setSessionCombo(Combo sessionCombo) { this.sessionCombo = sessionCombo; }
    public void setNewSessionButton(Button newSessionButton) { this.newSessionButton = newSessionButton; }
    public void setDeleteSessionButton(Button deleteSessionButton) { this.deleteSessionButton = deleteSessionButton; }
    public void setRenameSessionButton(Button renameSessionButton) { this.renameSessionButton = renameSessionButton; }
    public void setCloneSessionButton(Button cloneSessionButton) { this.cloneSessionButton = cloneSessionButton; }
    public void setExportSessionButton(Button exportSessionButton) { this.exportSessionButton = exportSessionButton; }
    public void setImportSessionButton(Button importSessionButton) { this.importSessionButton = importSessionButton; }
    public void setFontSizeSpinner(Spinner fontSizeSpinner) { this.fontSizeSpinner = fontSizeSpinner; }
    public void setFontNameCombo(Combo fontNameCombo) { this.fontNameCombo = fontNameCombo; }
    public void setFontBoldButton(Button fontBoldButton) { this.fontBoldButton = fontBoldButton; }
    public void setFontItalicButton(Button fontItalicButton) { this.fontItalicButton = fontItalicButton; }
    
    public boolean isLoadingSession() { return isLoadingSession; }
    public void setLoadingSession(boolean loading) { this.isLoadingSession = loading; }
    
    public void updateStatus(String message) {
        if (!statusLabel.isDisposed()) {
            statusLabel.setText(message);
        }
    }
    
    public void scrollToBottom() {
        if (!promptText.isDisposed()) {
            int lineCount = promptText.getLineCount();
            if (lineCount > 0) {
                promptText.setTopIndex(lineCount - 1);
            }
        }
    }
}