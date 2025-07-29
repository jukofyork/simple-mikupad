import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class UIManager {
    
    private SimpleMikuPad app;
    
    public UIManager(SimpleMikuPad app) {
        this.app = app;
    }
    
    public void createUI() {
        Shell shell = app.getShell();
        shell.setText("Simple MikuPad with Token Highlighting");
        shell.setSize(900, 750);
        shell.setLayout(new GridLayout(1, false));
        
        createSessionGroup();
        createModelSettingsGroup();
        createSamplingGroup();
        createFontGroup();
        createPromptArea();
        createControlButtons();
        createStatusBar();
    }
    
    private void createSessionGroup() {
        Group sessionGroup = new Group(app.getShell(), SWT.NONE);
        sessionGroup.setText("Session Management");
        sessionGroup.setLayout(new GridLayout(7, false));
        sessionGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Session dropdown
        new Label(sessionGroup, SWT.NONE).setText("Current Session:");
        Combo sessionCombo = new Combo(sessionGroup, SWT.READ_ONLY);
        sessionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setSessionCombo(sessionCombo);
        
        // Session management buttons
        Button newSessionButton = new Button(sessionGroup, SWT.PUSH);
        newSessionButton.setText("New");
        app.setNewSessionButton(newSessionButton);
        
        Button deleteSessionButton = new Button(sessionGroup, SWT.PUSH);
        deleteSessionButton.setText("Delete");
        app.setDeleteSessionButton(deleteSessionButton);
        
        Button renameSessionButton = new Button(sessionGroup, SWT.PUSH);
        renameSessionButton.setText("Rename");
        app.setRenameSessionButton(renameSessionButton);
        
        Button cloneSessionButton = new Button(sessionGroup, SWT.PUSH);
        cloneSessionButton.setText("Clone");
        app.setCloneSessionButton(cloneSessionButton);
        
        Button exportSessionButton = new Button(sessionGroup, SWT.PUSH);
        exportSessionButton.setText("Export");
        app.setExportSessionButton(exportSessionButton);
        
        Button importSessionButton = new Button(sessionGroup, SWT.PUSH);
        importSessionButton.setText("Import");
        app.setImportSessionButton(importSessionButton);
    }
    
    private void createModelSettingsGroup() {
        Group settingsGroup = new Group(app.getShell(), SWT.NONE);
        settingsGroup.setText("Model Settings");
        settingsGroup.setLayout(new GridLayout(2, false));
        settingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Endpoint
        new Label(settingsGroup, SWT.NONE).setText("Endpoint:");
        Text endpointText = new Text(settingsGroup, SWT.BORDER);
        endpointText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setEndpointText(endpointText);
        
        // API Key
        new Label(settingsGroup, SWT.NONE).setText("API Key:");
        Text apiKeyText = new Text(settingsGroup, SWT.BORDER | SWT.PASSWORD);
        apiKeyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setApiKeyText(apiKeyText);
        
        // Model
        new Label(settingsGroup, SWT.NONE).setText("Model:");
        Text modelText = new Text(settingsGroup, SWT.BORDER);
        modelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setModelText(modelText);
    }
    
    private void createSamplingGroup() {
        Group samplingGroup = new Group(app.getShell(), SWT.NONE);
        samplingGroup.setText("Generation Parameters");
        samplingGroup.setLayout(new GridLayout(1, false));
        samplingGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Composite samplingComp = new Composite(samplingGroup, SWT.NONE);
        samplingComp.setLayout(new GridLayout(2, false));
        samplingComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button samplingParamsButton = new Button(samplingComp, SWT.PUSH);
        samplingParamsButton.setText("Configure...");
        app.setSamplingParamsButton(samplingParamsButton);
        
        Label samplingParamsLabel = new Label(samplingComp, SWT.NONE);
        samplingParamsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setSamplingParamsLabel(samplingParamsLabel);
        
        Composite advancedComp = new Composite(samplingGroup, SWT.NONE);
        advancedComp.setLayout(new GridLayout(2, false));
        advancedComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button advancedSettingsButton = new Button(advancedComp, SWT.PUSH);
        advancedSettingsButton.setText("Configure...");
        app.setAdvancedSettingsButton(advancedSettingsButton);
        
        Label advancedSettingsLabel = new Label(advancedComp, SWT.NONE);
        advancedSettingsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setAdvancedSettingsLabel(advancedSettingsLabel);
    }
    
    private void createFontGroup() {
        Group fontGroup = new Group(app.getShell(), SWT.NONE);
        fontGroup.setText("Font Settings");
        fontGroup.setLayout(new GridLayout(6, false));
        fontGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Font Name
        new Label(fontGroup, SWT.NONE).setText("Font:");
        Combo fontNameCombo = new Combo(fontGroup, SWT.READ_ONLY);
        fontNameCombo.setItems(new String[]{"Consolas", "Courier New", "Monaco", "Menlo", "DejaVu Sans Mono", "Liberation Mono"});
        fontNameCombo.select(0);
        app.setFontNameCombo(fontNameCombo);
        
        // Font Size
        new Label(fontGroup, SWT.NONE).setText("Size:");
        Spinner fontSizeSpinner = new Spinner(fontGroup, SWT.BORDER);
        fontSizeSpinner.setMinimum(8);
        fontSizeSpinner.setMaximum(72);
        fontSizeSpinner.setSelection(10);
        app.setFontSizeSpinner(fontSizeSpinner);
        
        // Bold
        Button fontBoldButton = new Button(fontGroup, SWT.CHECK);
        fontBoldButton.setText("Bold");
        app.setFontBoldButton(fontBoldButton);
        
        // Italic
        Button fontItalicButton = new Button(fontGroup, SWT.CHECK);
        fontItalicButton.setText("Italic");
        app.setFontItalicButton(fontItalicButton);
    }
    
    private void createPromptArea() {
        Group promptGroup = new Group(app.getShell(), SWT.NONE);
        promptGroup.setText("Prompt");
        promptGroup.setLayout(new GridLayout(1, false));
        promptGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Prompt text area
        StyledText promptText = new StyledText(promptGroup, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        promptText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        promptText.setMargins(3, 3, 3, 3);
        app.setPromptText(promptText);
    }
    
    private void createControlButtons() {
        Composite buttonComposite = new Composite(app.getShell(), SWT.NONE);
        buttonComposite.setLayout(new GridLayout(3, false));
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Button generateButton = new Button(buttonComposite, SWT.PUSH);
        generateButton.setText("Generate");
        generateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setGenerateButton(generateButton);
        
        Button cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.setEnabled(false);
        app.setCancelButton(cancelButton);
        
        Button colorToggleButton = new Button(buttonComposite, SWT.TOGGLE);
        colorToggleButton.setText("Token Coloring");
        colorToggleButton.setSelection(true);
        colorToggleButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setColorToggleButton(colorToggleButton);
    }
    
    private void createStatusBar() {
        Label statusLabel = new Label(app.getShell(), SWT.NONE);
        statusLabel.setText("Ready");
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        app.setStatusLabel(statusLabel);
    }
}