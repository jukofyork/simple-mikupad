import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Dialog for configuring advanced generation settings like grammar constraints,
 * stopping strings, and banned tokens.
 */
public class AdvancedSettingsDialog {
    
    private Shell shell;
    private Shell parentShell;
    private AdvancedSettings settings;
    private boolean result = false;
    
    // Controls
    private Text grammarText;
    private Text jsonSchemaText;
    private Text logitBiasText;
    private Text stoppingStringsText;
    private Text bannedTokensText;
    
    public AdvancedSettingsDialog(Shell parent, AdvancedSettings settings) {
        this.parentShell = parent;
        this.settings = new AdvancedSettings(settings); // Work on a copy
    }
    
    public boolean open() {
        createShell();
        createContents();
        loadSettings();
        
        shell.pack();
        centerOnParent();
        shell.open();
        
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        return result;
    }
    
    public AdvancedSettings getSettings() {
        return settings;
    }
    
    private void createShell() {
        shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        shell.setText("Advanced Generation Settings");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(500, 400);
    }
    
    private void createContents() {
        createMainGroup();
        createButtonBar();
    }
    
    private void createMainGroup() {
        Group group = new Group(shell, SWT.NONE);
        group.setText("Generation Constraints");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Grammar
        new Label(group, SWT.NONE).setText("Grammar (EBNF):");
        Label grammarHelp = new Label(group, SWT.NONE);
        grammarHelp.setText("(Optional: Constrains output to follow EBNF grammar rules)");
        grammarHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        grammarText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        GridData grammarData = new GridData(SWT.FILL, SWT.FILL, true, true);
        grammarData.horizontalSpan = 2;
        grammarData.heightHint = 100;
        grammarText.setLayoutData(grammarData);
        
        // JSON Schema
        new Label(group, SWT.NONE).setText("JSON Schema:");
        Label jsonHelp = new Label(group, SWT.NONE);
        jsonHelp.setText("(Optional: JSON schema to constrain output structure)");
        jsonHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        jsonSchemaText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        GridData jsonData = new GridData(SWT.FILL, SWT.FILL, true, true);
        jsonData.horizontalSpan = 2;
        jsonData.heightHint = 80;
        jsonSchemaText.setLayoutData(jsonData);
        
        // Logit Bias
        new Label(group, SWT.NONE).setText("Logit Bias:");
        Label biasHelp = new Label(group, SWT.NONE);
        biasHelp.setText("(Format: [[token_id,bias],...] or [[\"token\",bias],...])");
        biasHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        logitBiasText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        GridData biasData = new GridData(SWT.FILL, SWT.FILL, true, true);
        biasData.horizontalSpan = 2;
        biasData.heightHint = 60;
        logitBiasText.setLayoutData(biasData);
        logitBiasText.setText("[]");
        
        // Stopping Strings
        new Label(group, SWT.NONE).setText("Stopping Strings:");
        Label stopHelp = new Label(group, SWT.NONE);
        stopHelp.setText("(Comma-separated: Generation stops when any of these appear)");
        stopHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        stoppingStringsText = new Text(group, SWT.BORDER);
        GridData stopData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        stopData.horizontalSpan = 2;
        stoppingStringsText.setLayoutData(stopData);
        
        // Banned Tokens
        new Label(group, SWT.NONE).setText("Banned Tokens:");
        Label banHelp = new Label(group, SWT.NONE);
        banHelp.setText("(Comma-separated: These tokens will never be generated)");
        banHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        bannedTokensText = new Text(group, SWT.BORDER);
        GridData banData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        banData.horizontalSpan = 2;
        bannedTokensText.setLayoutData(banData);
    }
    
    private void createButtonBar() {
        Composite buttonBar = new Composite(shell, SWT.NONE);
        buttonBar.setLayout(new GridLayout(3, false));
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        Button clearButton = new Button(buttonBar, SWT.PUSH);
        clearButton.setText("Clear All");
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clearAll();
            }
        });
        
        // Spacer
        new Label(buttonBar, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Composite okCancelComp = new Composite(buttonBar, SWT.NONE);
        okCancelComp.setLayout(new GridLayout(2, true));
        
        Button okButton = new Button(okCancelComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveSettings();
                result = true;
                shell.close();
            }
        });
        
        Button cancelButton = new Button(okCancelComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
        
        shell.setDefaultButton(okButton);
    }
    
    private void loadSettings() {
        grammarText.setText(settings.getGrammar());
        jsonSchemaText.setText(settings.getJsonSchema());
        logitBiasText.setText(settings.getLogitBias());
        stoppingStringsText.setText(settings.getStoppingStrings());
        bannedTokensText.setText(settings.getBannedTokens());
    }
    
    private void saveSettings() {
        settings.setGrammar(grammarText.getText());
        settings.setJsonSchema(jsonSchemaText.getText());
        settings.setLogitBias(logitBiasText.getText());
        settings.setStoppingStrings(stoppingStringsText.getText());
        settings.setBannedTokens(bannedTokensText.getText());
    }
    
    private void clearAll() {
        grammarText.setText("");
        jsonSchemaText.setText("");
        logitBiasText.setText("[]");
        stoppingStringsText.setText("");
        bannedTokensText.setText("");
    }
    
    private void centerOnParent() {
        Point parentSize = parentShell.getSize();
        Point parentLocation = parentShell.getLocation();
        Point shellSize = shell.getSize();
        
        int x = parentLocation.x + (parentSize.x - shellSize.x) / 2;
        int y = parentLocation.y + (parentSize.y - shellSize.y) / 2;
        
        shell.setLocation(x, y);
    }
}