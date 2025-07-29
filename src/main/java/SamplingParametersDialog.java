import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Modal dialog for configuring comprehensive sampling parameters.
 * Organized into collapsible groups for better usability.
 */
public class SamplingParametersDialog {
    
    private Shell shell;
    private Shell parentShell;
    private SamplingParameters parameters;
    private boolean result = false;
    
    // Parameter controls
    private ParameterControl temperatureControl;
    private ParameterControl maxTokensControl;
    private ParameterControl topPControl;
    private ParameterControl topKControl;
    private ParameterControl minPControl;
    private ParameterControl typicalPControl;
    private ParameterControl tfsZControl;
    private ParameterControl repeatPenaltyControl;
    private ParameterControl repeatLastNControl;
    private Button penalizeNlCheck;
    private ParameterControl presencePenaltyControl;
    private ParameterControl frequencyPenaltyControl;
    private Combo mirostatCombo;
    private ParameterControl mirostatTauControl;
    private ParameterControl mirostatEtaControl;
    private ParameterControl dynatempRangeControl;
    private ParameterControl dynatempExponentControl;
    private ParameterControl xtcThresholdControl;
    private ParameterControl xtcProbabilityControl;
    private ParameterControl dryMultiplierControl;
    private ParameterControl dryBaseControl;
    private ParameterControl dryAllowedLengthControl;
    private Text drySequenceBreakersText;
    private Text grammarText;
    private Text stoppingStringsText;
    private Text bannedTokensText;
    
    public SamplingParametersDialog(Shell parent, SamplingParameters parameters) {
        this.parentShell = parent;
        this.parameters = new SamplingParameters(parameters); // Work on a copy
    }
    
    public boolean open() {
        createShell();
        createContents();
        loadParameters();
        
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
    
    public SamplingParameters getParameters() {
        return parameters;
    }
    
    private void createShell() {
        shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        shell.setText("Sampling Parameters");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(600, 700);
    }
    
    private void createContents() {
        // Create scrolled composite for all the parameters
        ScrolledComposite scrolled = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
        scrolled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);
        
        Composite content = new Composite(scrolled, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        
        createBasicGroup(content);
        createTopSamplingGroup(content);
        createRepetitionGroup(content);
        createAdvancedGroup(content);
        createTextConstraintsGroup(content);
        createButtonBar();
        
        scrolled.setContent(content);
        scrolled.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
    
    private void createBasicGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Basic Parameters");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        temperatureControl = new ParameterControl(group, "Temperature", 0.7, 0.01, 5.0, 2, true);
        maxTokensControl = new ParameterControl(group, "Max Tokens", 512, 1, 4096, true);
        
        setupChangeListener(temperatureControl);
        setupChangeListener(maxTokensControl);
    }
    
    private void createTopSamplingGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Top Sampling");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        topPControl = new ParameterControl(group, "Top-P", 0.95, 0.0, 1.0, 3, true);
        topKControl = new ParameterControl(group, "Top-K", 40, 0, 200, true);
        minPControl = new ParameterControl(group, "Min-P", 0.0, 0.0, 1.0, 3, false);
        typicalPControl = new ParameterControl(group, "Typical-P", 1.0, 0.0, 1.0, 3, false);
        tfsZControl = new ParameterControl(group, "TFS-Z", 1.0, 0.0, 1.0, 3, false);
        
        setupChangeListener(topPControl);
        setupChangeListener(topKControl);
        setupChangeListener(minPControl);
        setupChangeListener(typicalPControl);
        setupChangeListener(tfsZControl);
    }
    
    private void createRepetitionGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Repetition Control");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        repeatPenaltyControl = new ParameterControl(group, "Repeat Penalty", 1.1, 0.0, 2.0, 2, true);
        repeatLastNControl = new ParameterControl(group, "Repeat Last N", 256, 0, 2048, true);
        
        penalizeNlCheck = new Button(group, SWT.CHECK);
        penalizeNlCheck.setText("Penalize Newlines");
        penalizeNlCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        presencePenaltyControl = new ParameterControl(group, "Presence Penalty", 0.0, -2.0, 2.0, 2, false);
        frequencyPenaltyControl = new ParameterControl(group, "Frequency Penalty", 0.0, -2.0, 2.0, 2, false);
        
        setupChangeListener(repeatPenaltyControl);
        setupChangeListener(repeatLastNControl);
        setupChangeListener(presencePenaltyControl);
        setupChangeListener(frequencyPenaltyControl);
    }
    
    private void createAdvancedGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Advanced Sampling");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Mirostat
        Composite mirostatComp = new Composite(group, SWT.NONE);
        mirostatComp.setLayout(new GridLayout(3, false));
        mirostatComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(mirostatComp, SWT.NONE).setText("Mirostat:");
        mirostatCombo = new Combo(mirostatComp, SWT.READ_ONLY);
        mirostatCombo.setItems(new String[]{"Disabled", "Mirostat v1", "Mirostat v2"});
        mirostatCombo.select(0);
        mirostatCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        new Label(mirostatComp, SWT.NONE).setText("(0=off, 1=v1, 2=v2)");
        
        mirostatTauControl = new ParameterControl(group, "Mirostat Tau", 5.0, 0.1, 10.0, 1, false);
        mirostatEtaControl = new ParameterControl(group, "Mirostat Eta", 0.1, 0.01, 1.0, 2, false);
        
        // Dynamic Temperature
        dynatempRangeControl = new ParameterControl(group, "Dynatemp Range", 0.0, 0.0, 5.0, 2, false);
        dynatempExponentControl = new ParameterControl(group, "Dynatemp Exponent", 1.0, 0.1, 5.0, 2, false);
        
        // XTC
        xtcThresholdControl = new ParameterControl(group, "XTC Threshold", 0.1, 0.0, 1.0, 3, false);
        xtcProbabilityControl = new ParameterControl(group, "XTC Probability", 0.0, 0.0, 1.0, 3, false);
        
        // DRY
        dryMultiplierControl = new ParameterControl(group, "DRY Multiplier", 0.0, 0.0, 5.0, 2, false);
        dryBaseControl = new ParameterControl(group, "DRY Base", 1.75, 1.0, 4.0, 2, false);
        dryAllowedLengthControl = new ParameterControl(group, "DRY Allowed Length", 2, 1, 20, false);
        
        Composite drySeqComp = new Composite(group, SWT.NONE);
        drySeqComp.setLayout(new GridLayout(2, false));
        drySeqComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(drySeqComp, SWT.NONE).setText("DRY Sequence Breakers:");
        drySequenceBreakersText = new Text(drySeqComp, SWT.BORDER);
        drySequenceBreakersText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        drySequenceBreakersText.setText("\\n,.,!,?,;,:");
        
        setupChangeListener(mirostatTauControl);
        setupChangeListener(mirostatEtaControl);
        setupChangeListener(dynatempRangeControl);
        setupChangeListener(dynatempExponentControl);
        setupChangeListener(xtcThresholdControl);
        setupChangeListener(xtcProbabilityControl);
        setupChangeListener(dryMultiplierControl);
        setupChangeListener(dryBaseControl);
        setupChangeListener(dryAllowedLengthControl);
    }
    
    private void createTextConstraintsGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Text Constraints");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        new Label(group, SWT.NONE).setText("Grammar (EBNF):");
        grammarText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        GridData grammarData = new GridData(SWT.FILL, SWT.FILL, true, false);
        grammarData.heightHint = 60;
        grammarText.setLayoutData(grammarData);
        
        new Label(group, SWT.NONE).setText("Stopping Strings (comma-separated):");
        stoppingStringsText = new Text(group, SWT.BORDER);
        stoppingStringsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(group, SWT.NONE).setText("Banned Tokens (comma-separated):");
        bannedTokensText = new Text(group, SWT.BORDER);
        bannedTokensText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }
    
    private void createButtonBar() {
        Composite buttonBar = new Composite(shell, SWT.NONE);
        buttonBar.setLayout(new GridLayout(3, false));
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        Button resetButton = new Button(buttonBar, SWT.PUSH);
        resetButton.setText("Reset to Defaults");
        resetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetToDefaults();
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
                if (saveParameters()) {
                    result = true;
                    shell.close();
                }
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
    
    private void setupChangeListener(ParameterControl control) {
        control.setChangeListener(() -> {
            // Auto-update enabled states based on values
            updateEnabledStates();
        });
    }
    
    private void updateEnabledStates() {
        // Enable mirostat sub-parameters when mirostat is enabled
        boolean mirostatEnabled = mirostatCombo.getSelectionIndex() > 0;
        mirostatTauControl.setEnabled(mirostatEnabled);
        mirostatEtaControl.setEnabled(mirostatEnabled);
        
        // Enable dynatemp exponent when range > 0
        boolean dynatempEnabled = dynatempRangeControl.getDoubleValue() > 0;
        dynatempExponentControl.setEnabled(dynatempEnabled);
        
        // Enable XTC threshold when probability > 0
        boolean xtcEnabled = xtcProbabilityControl.getDoubleValue() > 0;
        xtcThresholdControl.setEnabled(xtcEnabled);
        
        // Enable DRY sub-parameters when multiplier > 0
        boolean dryEnabled = dryMultiplierControl.getDoubleValue() > 0;
        dryBaseControl.setEnabled(dryEnabled);
        dryAllowedLengthControl.setEnabled(dryEnabled);
    }
    
    private void loadParameters() {
        temperatureControl.setDoubleValue(parameters.getTemperature());
        temperatureControl.setEnabled(parameters.isTemperatureEnabled());
        
        maxTokensControl.setIntValue(parameters.getMaxTokens());
        
        topPControl.setDoubleValue(parameters.getTopP());
        topPControl.setEnabled(parameters.isTopPEnabled());
        
        topKControl.setIntValue(parameters.getTopK());
        topKControl.setEnabled(parameters.isTopKEnabled());
        
        minPControl.setDoubleValue(parameters.getMinP());
        minPControl.setEnabled(parameters.isMinPEnabled());
        
        typicalPControl.setDoubleValue(parameters.getTypicalP());
        typicalPControl.setEnabled(parameters.isTypicalPEnabled());
        
        tfsZControl.setDoubleValue(parameters.getTfsZ());
        tfsZControl.setEnabled(parameters.isTfsZEnabled());
        
        repeatPenaltyControl.setDoubleValue(parameters.getRepeatPenalty());
        repeatPenaltyControl.setEnabled(parameters.isRepeatPenaltyEnabled());
        
        repeatLastNControl.setIntValue(parameters.getRepeatLastN());
        penalizeNlCheck.setSelection(parameters.isPenalizeNl());
        
        presencePenaltyControl.setDoubleValue(parameters.getPresencePenalty());
        presencePenaltyControl.setEnabled(parameters.isPresencePenaltyEnabled());
        
        frequencyPenaltyControl.setDoubleValue(parameters.getFrequencyPenalty());
        frequencyPenaltyControl.setEnabled(parameters.isFrequencyPenaltyEnabled());
        
        mirostatCombo.select(parameters.getMirostat());
        mirostatTauControl.setDoubleValue(parameters.getMirostatTau());
        mirostatEtaControl.setDoubleValue(parameters.getMirostatEta());
        
        dynatempRangeControl.setDoubleValue(parameters.getDynatempRange());
        dynatempRangeControl.setEnabled(parameters.isDynatempEnabled());
        dynatempExponentControl.setDoubleValue(parameters.getDynatempExponent());
        
        xtcThresholdControl.setDoubleValue(parameters.getXtcThreshold());
        xtcProbabilityControl.setDoubleValue(parameters.getXtcProbability());
        xtcProbabilityControl.setEnabled(parameters.isXtcEnabled());
        
        dryMultiplierControl.setDoubleValue(parameters.getDryMultiplier());
        dryMultiplierControl.setEnabled(parameters.isDryEnabled());
        dryBaseControl.setDoubleValue(parameters.getDryBase());
        dryAllowedLengthControl.setIntValue(parameters.getDryAllowedLength());
        drySequenceBreakersText.setText(parameters.getDrySequenceBreakers());
        
        grammarText.setText(parameters.getGrammar());
        stoppingStringsText.setText(parameters.getStoppingStrings());
        bannedTokensText.setText(parameters.getBannedTokens());
        
        updateEnabledStates();
    }
    
    private boolean saveParameters() {
        // Validate all parameters first
        if (!validateParameters()) {
            return false;
        }
        
        parameters.setTemperature(temperatureControl.getDoubleValue());
        parameters.setTemperatureEnabled(temperatureControl.isEnabled());
        
        parameters.setMaxTokens(maxTokensControl.getIntValue());
        
        parameters.setTopP(topPControl.getDoubleValue());
        parameters.setTopPEnabled(topPControl.isEnabled());
        
        parameters.setTopK(topKControl.getIntValue());
        parameters.setTopKEnabled(topKControl.isEnabled());
        
        parameters.setMinP(minPControl.getDoubleValue());
        parameters.setMinPEnabled(minPControl.isEnabled());
        
        parameters.setTypicalP(typicalPControl.getDoubleValue());
        parameters.setTypicalPEnabled(typicalPControl.isEnabled());
        
        parameters.setTfsZ(tfsZControl.getDoubleValue());
        parameters.setTfsZEnabled(tfsZControl.isEnabled());
        
        parameters.setRepeatPenalty(repeatPenaltyControl.getDoubleValue());
        parameters.setRepeatPenaltyEnabled(repeatPenaltyControl.isEnabled());
        
        parameters.setRepeatLastN(repeatLastNControl.getIntValue());
        parameters.setPenalizeNl(penalizeNlCheck.getSelection());
        
        parameters.setPresencePenalty(presencePenaltyControl.getDoubleValue());
        parameters.setPresencePenaltyEnabled(presencePenaltyControl.isEnabled());
        
        parameters.setFrequencyPenalty(frequencyPenaltyControl.getDoubleValue());
        parameters.setFrequencyPenaltyEnabled(frequencyPenaltyControl.isEnabled());
        
        parameters.setMirostat(mirostatCombo.getSelectionIndex());
        parameters.setMirostatEnabled(mirostatCombo.getSelectionIndex() > 0);
        parameters.setMirostatTau(mirostatTauControl.getDoubleValue());
        parameters.setMirostatEta(mirostatEtaControl.getDoubleValue());
        
        parameters.setDynatempRange(dynatempRangeControl.getDoubleValue());
        parameters.setDynatempEnabled(dynatempRangeControl.getDoubleValue() > 0);
        parameters.setDynatempExponent(dynatempExponentControl.getDoubleValue());
        
        parameters.setXtcThreshold(xtcThresholdControl.getDoubleValue());
        parameters.setXtcProbability(xtcProbabilityControl.getDoubleValue());
        parameters.setXtcEnabled(xtcProbabilityControl.getDoubleValue() > 0);
        
        parameters.setDryMultiplier(dryMultiplierControl.getDoubleValue());
        parameters.setDryEnabled(dryMultiplierControl.getDoubleValue() > 0);
        parameters.setDryBase(dryBaseControl.getDoubleValue());
        parameters.setDryAllowedLength(dryAllowedLengthControl.getIntValue());
        parameters.setDrySequenceBreakers(drySequenceBreakersText.getText());
        
        parameters.setGrammar(grammarText.getText());
        parameters.setStoppingStrings(stoppingStringsText.getText());
        parameters.setBannedTokens(bannedTokensText.getText());
        
        return true;
    }
    
    private boolean validateParameters() {
        // Check all parameter controls for validity
        ParameterControl[] controls = {
            temperatureControl, maxTokensControl, topPControl, topKControl,
            minPControl, typicalPControl, tfsZControl, repeatPenaltyControl,
            repeatLastNControl, presencePenaltyControl, frequencyPenaltyControl,
            mirostatTauControl, mirostatEtaControl, dynatempRangeControl,
            dynatempExponentControl, xtcThresholdControl, xtcProbabilityControl,
            dryMultiplierControl, dryBaseControl, dryAllowedLengthControl
        };
        
        for (ParameterControl control : controls) {
            if (!control.isValid()) {
                MessageBox messageBox = new MessageBox(shell, SWT.ERROR | SWT.OK);
                messageBox.setMessage("Invalid parameter value. Please check highlighted fields.");
                messageBox.setText("Validation Error");
                messageBox.open();
                return false;
            }
        }
        
        return true;
    }
    
    private void resetToDefaults() {
        parameters = new SamplingParameters();
        loadParameters();
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