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
    private ParameterControl seedControl;
    private Text samplersText;
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
    private ParameterControl dryPenaltyLastNControl;
    
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
        createRepetitionGroup(content);
        createAdvancedGroup(content);
        createButtonBar();
        
        scrolled.setContent(content);
        scrolled.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
    
    private void createBasicGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Basic Parameters");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        seedControl = new ParameterControl(group, "Seed", Constants.DEFAULT_SEED, Constants.SEED_MIN, Constants.SEED_MAX, true);
        temperatureControl = new ParameterControl(group, "Temperature", Constants.DEFAULT_TEMPERATURE, Constants.TEMPERATURE_MIN, Constants.TEMPERATURE_MAX, 1, Constants.DEFAULT_TEMPERATURE_ENABLED);
        topKControl = new ParameterControl(group, "Top-K", Constants.DEFAULT_TOP_K, Constants.TOP_K_MIN, Constants.TOP_K_MAX, Constants.DEFAULT_TOP_K_ENABLED);
        topPControl = new ParameterControl(group, "Top-P", Constants.DEFAULT_TOP_P, Constants.TOP_P_MIN, Constants.TOP_P_MAX, 1, Constants.DEFAULT_TOP_P_ENABLED);
        minPControl = new ParameterControl(group, "Min-P", Constants.DEFAULT_MIN_P, Constants.MIN_P_MIN, Constants.MIN_P_MAX, 1, Constants.DEFAULT_MIN_P_ENABLED);
        maxTokensControl = new ParameterControl(group, "Max Tokens", Constants.DEFAULT_MAX_TOKENS, Constants.MAX_TOKENS_MIN, Constants.MAX_TOKENS_MAX, Constants.DEFAULT_MAX_TOKENS_ENABLED);
        
        setupChangeListener(seedControl);
        setupChangeListener(temperatureControl);
        setupChangeListener(maxTokensControl);
        setupChangeListener(topPControl);
        setupChangeListener(topKControl);
        setupChangeListener(minPControl);
    }
    
    private void createRepetitionGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Repetition Control");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        repeatPenaltyControl = new ParameterControl(group, "Repeat Penalty", Constants.DEFAULT_REPEAT_PENALTY, Constants.REPEAT_PENALTY_MIN, Constants.REPEAT_PENALTY_MAX, 1, Constants.DEFAULT_REPEAT_PENALTY_ENABLED);
        presencePenaltyControl = new ParameterControl(group, "Presence Penalty", Constants.DEFAULT_PRESENCE_PENALTY, Constants.PRESENCE_PENALTY_MIN, Constants.PRESENCE_PENALTY_MAX, 1, Constants.DEFAULT_PRESENCE_PENALTY_ENABLED);
        frequencyPenaltyControl = new ParameterControl(group, "Frequency Penalty", Constants.DEFAULT_FREQUENCY_PENALTY, Constants.FREQUENCY_PENALTY_MIN, Constants.FREQUENCY_PENALTY_MAX, 1, Constants.DEFAULT_FREQUENCY_PENALTY_ENABLED);
        repeatLastNControl = new ParameterControl(group, "Repeat Last N", Constants.DEFAULT_REPEAT_LAST_N, Constants.REPEAT_LAST_N_MIN, Constants.REPEAT_LAST_N_MAX, Constants.DEFAULT_REPEAT_LAST_N_ENABLED);

        penalizeNlCheck = new Button(group, SWT.CHECK);
        penalizeNlCheck.setText("Penalize Newlines");
        penalizeNlCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
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
        mirostatComp.setLayout(new GridLayout(2, false));
        mirostatComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(mirostatComp, SWT.NONE).setText("Mirostat:");
        mirostatCombo = new Combo(mirostatComp, SWT.READ_ONLY);
        mirostatCombo.setItems(new String[]{"Disabled", "Mirostat v1", "Mirostat v2"});
        mirostatCombo.select(Constants.DEFAULT_MIROSTAT);
        mirostatCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        mirostatTauControl = new ParameterControl(group, "Mirostat Tau", Constants.DEFAULT_MIROSTAT_TAU, Constants.MIROSTAT_TAU_MIN, Constants.MIROSTAT_TAU_MAX, 1, Constants.DEFAULT_MIROSTAT_ENABLED);
        mirostatEtaControl = new ParameterControl(group, "Mirostat Eta", Constants.DEFAULT_MIROSTAT_ETA, Constants.MIROSTAT_ETA_MIN, Constants.MIROSTAT_ETA_MAX, 1, Constants.DEFAULT_MIROSTAT_ENABLED);
        
        // Advanced sampling methods (moved after Mirostat)
        typicalPControl = new ParameterControl(group, "Typical-P", Constants.DEFAULT_TYPICAL_P, Constants.TYPICAL_P_MIN, Constants.TYPICAL_P_MAX, 1, Constants.DEFAULT_TYPICAL_P_ENABLED);
        tfsZControl = new ParameterControl(group, "TFS-Z", Constants.DEFAULT_TFS_Z, Constants.TFS_Z_MIN, Constants.TFS_Z_MAX, 1, Constants.DEFAULT_TFS_Z_ENABLED);
        
        // Dynamic Temperature
        dynatempRangeControl = new ParameterControl(group, "Dynatemp Range", Constants.DEFAULT_DYNATEMP_RANGE, Constants.DYNATEMP_RANGE_MIN, Constants.DYNATEMP_RANGE_MAX, 1, Constants.DEFAULT_DYNATEMP_ENABLED);
        dynatempExponentControl = new ParameterControl(group, "Dynatemp Exponent", Constants.DEFAULT_DYNATEMP_EXPONENT, Constants.DYNATEMP_EXPONENT_MIN, Constants.DYNATEMP_EXPONENT_MAX, 1, Constants.DEFAULT_DYNATEMP_ENABLED);
        
        // XTC
        xtcThresholdControl = new ParameterControl(group, "XTC Threshold", Constants.DEFAULT_XTC_THRESHOLD, Constants.XTC_THRESHOLD_MIN, Constants.XTC_THRESHOLD_MAX, 1, Constants.DEFAULT_XTC_ENABLED);
        xtcProbabilityControl = new ParameterControl(group, "XTC Probability", Constants.DEFAULT_XTC_PROBABILITY, Constants.XTC_PROBABILITY_MIN, Constants.XTC_PROBABILITY_MAX, 1, Constants.DEFAULT_XTC_ENABLED);
        
        // DRY
        dryMultiplierControl = new ParameterControl(group, "DRY Multiplier", Constants.DEFAULT_DRY_MULTIPLIER, Constants.DRY_MULTIPLIER_MIN, Constants.DRY_MULTIPLIER_MAX, 1, Constants.DEFAULT_DRY_ENABLED);
        dryBaseControl = new ParameterControl(group, "DRY Base", Constants.DEFAULT_DRY_BASE, Constants.DRY_BASE_MIN, Constants.DRY_BASE_MAX, 1, Constants.DEFAULT_DRY_ENABLED);
        dryAllowedLengthControl = new ParameterControl(group, "DRY Allowed Length", Constants.DEFAULT_DRY_ALLOWED_LENGTH, Constants.DRY_ALLOWED_LENGTH_MIN, Constants.DRY_ALLOWED_LENGTH_MAX, Constants.DEFAULT_DRY_ENABLED);
        dryPenaltyLastNControl = new ParameterControl(group, "DRY Penalty Last N", Constants.DEFAULT_DRY_PENALTY_LAST_N, Constants.DRY_PENALTY_LAST_N_MIN, Constants.DRY_PENALTY_LAST_N_MAX, Constants.DEFAULT_DRY_ENABLED);
        
        Composite drySeqComp = new Composite(group, SWT.NONE);
        drySeqComp.setLayout(new GridLayout(2, false));
        drySeqComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(drySeqComp, SWT.NONE).setText("DRY Sequence Breakers:");
        drySequenceBreakersText = new Text(drySeqComp, SWT.BORDER);
        drySequenceBreakersText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        drySequenceBreakersText.setText(Constants.DEFAULT_DRY_SEQUENCE_BREAKERS);
        drySequenceBreakersText.setToolTipText("Space-delimited sequence breakers (e.g., \\n , . ! ? ; :)");
        
        // Samplers order (moved to bottom)
        Composite samplersComp = new Composite(group, SWT.NONE);
        samplersComp.setLayout(new GridLayout(2, false));
        samplersComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(samplersComp, SWT.NONE).setText("Samplers Order:");
        samplersText = new Text(samplersComp, SWT.BORDER);
        samplersText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        samplersText.setText(Constants.DEFAULT_SAMPLERS);
        samplersText.setToolTipText("Space-delimited sampler names (e.g., dry top_k typ_p top_p min_p xtc temperature)");
        
        setupChangeListener(mirostatTauControl);
        setupChangeListener(mirostatEtaControl);
        setupChangeListener(typicalPControl);
        setupChangeListener(tfsZControl);
        setupChangeListener(dynatempRangeControl);
        setupChangeListener(dynatempExponentControl);
        setupChangeListener(xtcThresholdControl);
        setupChangeListener(xtcProbabilityControl);
        setupChangeListener(dryMultiplierControl);
        setupChangeListener(dryBaseControl);
        setupChangeListener(dryAllowedLengthControl);
        setupChangeListener(dryPenaltyLastNControl);
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
        dryPenaltyLastNControl.setEnabled(dryEnabled);
    }
    
    private void loadParameters() {
        seedControl.setIntValue(parameters.getSeed());
        samplersText.setText(parameters.getSamplers());
        temperatureControl.setDoubleValue(parameters.getTemperature());
        temperatureControl.setEnabled(parameters.isTemperatureEnabled());
        
        topPControl.setDoubleValue(parameters.getTopP());
        topPControl.setEnabled(parameters.isTopPEnabled());
        
        topKControl.setIntValue(parameters.getTopK());
        topKControl.setEnabled(parameters.isTopKEnabled());
        
        minPControl.setDoubleValue(parameters.getMinP());
        minPControl.setEnabled(parameters.isMinPEnabled());
        
        maxTokensControl.setIntValue(parameters.getMaxTokens());
        maxTokensControl.setEnabled(parameters.isMaxTokensEnabled());
        
        typicalPControl.setDoubleValue(parameters.getTypicalP());
        typicalPControl.setEnabled(parameters.isTypicalPEnabled());
        
        tfsZControl.setDoubleValue(parameters.getTfsZ());
        tfsZControl.setEnabled(parameters.isTfsZEnabled());
        
        repeatPenaltyControl.setDoubleValue(parameters.getRepeatPenalty());
        repeatPenaltyControl.setEnabled(parameters.isRepeatPenaltyEnabled());
        
        repeatLastNControl.setIntValue(parameters.getRepeatLastN());
        repeatLastNControl.setEnabled(parameters.isRepeatLastNEnabled());
        
        presencePenaltyControl.setDoubleValue(parameters.getPresencePenalty());
        presencePenaltyControl.setEnabled(parameters.isPresencePenaltyEnabled());
        
        frequencyPenaltyControl.setDoubleValue(parameters.getFrequencyPenalty());
        frequencyPenaltyControl.setEnabled(parameters.isFrequencyPenaltyEnabled());
        
        penalizeNlCheck.setSelection(parameters.isPenalizeNl());
        
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
        dryPenaltyLastNControl.setIntValue(parameters.getDryPenaltyLastN());
        
        updateEnabledStates();
    }
    
    private boolean saveParameters() {
        // Validate all parameters first
        if (!validateParameters()) {
            return false;
        }
        
        parameters.setSeed(seedControl.getIntValue());
        parameters.setSamplers(samplersText.getText());
        parameters.setTemperature(temperatureControl.getDoubleValue());
        parameters.setTemperatureEnabled(temperatureControl.isEnabled());
        
        parameters.setMaxTokens(maxTokensControl.getIntValue());
        parameters.setMaxTokensEnabled(maxTokensControl.isEnabled());
        
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
        parameters.setRepeatLastNEnabled(repeatLastNControl.isEnabled());
        
        parameters.setPresencePenalty(presencePenaltyControl.getDoubleValue());
        parameters.setPresencePenaltyEnabled(presencePenaltyControl.isEnabled());
        
        parameters.setFrequencyPenalty(frequencyPenaltyControl.getDoubleValue());
        parameters.setFrequencyPenaltyEnabled(frequencyPenaltyControl.isEnabled());
        
        parameters.setPenalizeNl(penalizeNlCheck.getSelection());
        
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
        parameters.setDryPenaltyLastN(dryPenaltyLastNControl.getIntValue());
        
        return true;
    }
    
    private boolean validateParameters() {
        // Check all parameter controls for validity
        ParameterControl[] controls = {
            seedControl, temperatureControl, maxTokensControl, topPControl, topKControl,
            minPControl, typicalPControl, tfsZControl, repeatPenaltyControl,
            repeatLastNControl, presencePenaltyControl, frequencyPenaltyControl,
            mirostatTauControl, mirostatEtaControl, dynatempRangeControl,
            dynatempExponentControl, xtcThresholdControl, xtcProbabilityControl,
            dryMultiplierControl, dryBaseControl, dryAllowedLengthControl, dryPenaltyLastNControl
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
        
        // Validate samplers
        if (!Constants.validateSamplers(samplersText.getText())) {
            MessageBox messageBox = new MessageBox(shell, SWT.ERROR | SWT.OK);
            messageBox.setMessage("Invalid sampler names. Only these are allowed: dry, top_k, typ_p, top_p, min_p, xtc, temperature");
            messageBox.setText("Validation Error");
            messageBox.open();
            return false;
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