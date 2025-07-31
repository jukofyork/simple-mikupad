import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Tabbed dialog for configuring comprehensive generation parameters.
 * Organized into logical tabs for better usability.
 */
public class SamplingParametersDialog {
    
    private Shell shell;
    private Shell parentShell;
    private SamplingParameters parameters;
    private boolean result = false;
    
    // Parameter controls
    private ParameterControl seedControl;
    private Table samplersTable;
    private Button samplersUpButton;
    private Button samplersDownButton;
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
    
    // Advanced constraint controls
    private Text grammarText;
    private Text jsonSchemaText;
    private Text logitBiasText;
    private Text stoppingStringsText;
    private Text bannedTokensText;
    private Button ignoreEosCheck;
    
    // Template controls
    private Combo templateCombo;
    private Text templateSysPrefixText;
    private Text templateSysSuffixText;
    private Text templateInstPrefixText;
    private Text templateInstSuffixText;
    private Text templateEosText;
    
    private boolean isLoadingTemplate = false;
    
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
        shell.setText("Generation Settings");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(600, 600);
    }
    
    private void createContents() {
        // Create tab folder
        TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        createBasicTab(tabFolder);
        createAdvancedTab(tabFolder);
        createConstraintsTab(tabFolder);
        createTemplatesTab(tabFolder);
        
        createButtonBar();
    }
    
    private void createBasicTab(TabFolder parent) {
        TabItem tabItem = new TabItem(parent, SWT.NONE);
        tabItem.setText("Basic");
        
        ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);
        
        Composite content = new Composite(scrolled, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        
        Group coreGroup = new Group(content, SWT.NONE);
        coreGroup.setText("General");
        coreGroup.setLayout(new GridLayout(1, false));
        coreGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
                
        seedControl = new ParameterControl(coreGroup, "Seed", Constants.DEFAULT_SEED, Constants.SEED_MIN, Constants.SEED_MAX, Constants.DEFAULT_SEED_ENABLED);
        
        Group stoppingGroup = new Group(content, SWT.NONE);
        stoppingGroup.setText("Stopping");
        stoppingGroup.setLayout(new GridLayout(1, false));
        stoppingGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        maxTokensControl = new ParameterControl(stoppingGroup, "Max Tokens", Constants.DEFAULT_MAX_TOKENS, Constants.MAX_TOKENS_MIN, Constants.MAX_TOKENS_MAX, Constants.DEFAULT_MAX_TOKENS_ENABLED);
        
        ignoreEosCheck = new Button(stoppingGroup, SWT.CHECK);
        ignoreEosCheck.setText("Ignore End of Stream");
        ignoreEosCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ignoreEosCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ignoreEosCheck.getSelection()) {
                    ignoreEosCheck.setForeground(null); // Default color
                } else {
                    ignoreEosCheck.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DISABLED_FOREGROUND));
                }
            }
        });
        // Set initial state
        ignoreEosCheck.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DISABLED_FOREGROUND));
        
        // Stopping Strings
        Composite stoppingStringsComp = new Composite(stoppingGroup, SWT.NONE);
        stoppingStringsComp.setLayout(new GridLayout(2, false));
        stoppingStringsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(stoppingStringsComp, SWT.NONE).setText("Stop Strings:");
        stoppingStringsText = new Text(stoppingStringsComp, SWT.BORDER);
        stoppingStringsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        stoppingStringsText.setToolTipText("Comma-separated");
                
        // Samplers order as separate group
        Group samplersGroup = new Group(content, SWT.NONE);
        samplersGroup.setText("Sampler Order");
        samplersGroup.setLayout(new GridLayout(2, false));
        samplersGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        samplersTable = new Table(samplersGroup, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        samplersTable.setHeaderVisible(false);
        samplersTable.setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableData.heightHint = 150;
        samplersTable.setLayoutData(tableData);
        
        TableColumn samplerColumn = new TableColumn(samplersTable, SWT.NONE);
        samplerColumn.setText("Sampler");
        samplerColumn.setWidth(200);
        
        // Populate table with all valid samplers
        for (String sampler : Constants.DEFAULT_SAMPLERS) {
            TableItem item = new TableItem(samplersTable, SWT.NONE);
            item.setText(sampler);
            item.setChecked(false);
        }
        
        Composite samplersButtons = new Composite(samplersGroup, SWT.NONE);
        samplersButtons.setLayout(new GridLayout(1, true));
        samplersButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
        
        samplersUpButton = new Button(samplersButtons, SWT.PUSH);
        samplersUpButton.setText("Up");
        samplersUpButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        samplersUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSamplerUp();
            }
        });
        
        samplersDownButton = new Button(samplersButtons, SWT.PUSH);
        samplersDownButton.setText("Down");
        samplersDownButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        samplersDownButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSamplerDown();
            }
        });
        
        Group samplingGroup = new Group(content, SWT.NONE);
        samplingGroup.setText("Sampling Methods");
        samplingGroup.setLayout(new GridLayout(1, false));
        samplingGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        temperatureControl = new ParameterControl(samplingGroup, "Temperature", Constants.DEFAULT_TEMPERATURE, Constants.TEMPERATURE_MIN, Constants.TEMPERATURE_MAX, 1, Constants.DEFAULT_TEMPERATURE_ENABLED);
        topKControl = new ParameterControl(samplingGroup, "Top-K", Constants.DEFAULT_TOP_K, Constants.TOP_K_MIN, Constants.TOP_K_MAX, Constants.DEFAULT_TOP_K_ENABLED);
        topPControl = new ParameterControl(samplingGroup, "Top-P", Constants.DEFAULT_TOP_P, Constants.TOP_P_MIN, Constants.TOP_P_MAX, 1, Constants.DEFAULT_TOP_P_ENABLED);
        minPControl = new ParameterControl(samplingGroup, "Min-P", Constants.DEFAULT_MIN_P, Constants.MIN_P_MIN, Constants.MIN_P_MAX, 1, Constants.DEFAULT_MIN_P_ENABLED);
        typicalPControl = new ParameterControl(samplingGroup, "Typical-P", Constants.DEFAULT_TYPICAL_P, Constants.TYPICAL_P_MIN, Constants.TYPICAL_P_MAX, 1, Constants.DEFAULT_TYPICAL_P_ENABLED);
        tfsZControl = new ParameterControl(samplingGroup, "TFS-Z", Constants.DEFAULT_TFS_Z, Constants.TFS_Z_MIN, Constants.TFS_Z_MAX, 1, Constants.DEFAULT_TFS_Z_ENABLED);
        
        Group penaltyGroup = new Group(content, SWT.NONE);
        penaltyGroup.setText("Penalty Methods");
        penaltyGroup.setLayout(new GridLayout(1, false));
        penaltyGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        repeatPenaltyControl = new ParameterControl(penaltyGroup, "Repeat Penalty", Constants.DEFAULT_REPEAT_PENALTY, Constants.REPEAT_PENALTY_MIN, Constants.REPEAT_PENALTY_MAX, 1, Constants.DEFAULT_REPEAT_PENALTY_ENABLED);
        presencePenaltyControl = new ParameterControl(penaltyGroup, "Presence Penalty", Constants.DEFAULT_PRESENCE_PENALTY, Constants.PRESENCE_PENALTY_MIN, Constants.PRESENCE_PENALTY_MAX, 1, Constants.DEFAULT_PRESENCE_PENALTY_ENABLED);
        frequencyPenaltyControl = new ParameterControl(penaltyGroup, "Frequency Penalty", Constants.DEFAULT_FREQUENCY_PENALTY, Constants.FREQUENCY_PENALTY_MIN, Constants.FREQUENCY_PENALTY_MAX, 1, Constants.DEFAULT_FREQUENCY_PENALTY_ENABLED);
        repeatLastNControl = new ParameterControl(penaltyGroup, "Repeat Last N", Constants.DEFAULT_REPEAT_LAST_N, Constants.REPEAT_LAST_N_MIN, Constants.REPEAT_LAST_N_MAX, Constants.DEFAULT_REPEAT_LAST_N_ENABLED);
        
        penalizeNlCheck = new Button(penaltyGroup, SWT.CHECK);
        penalizeNlCheck.setText("Penalize Newlines");
        penalizeNlCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        penalizeNlCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (penalizeNlCheck.getSelection()) {
                    penalizeNlCheck.setForeground(null); // Default color
                } else {
                    penalizeNlCheck.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DISABLED_FOREGROUND));
                }
            }
        });
        // Set initial state
        penalizeNlCheck.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DISABLED_FOREGROUND));
        
        setupChangeListener(seedControl);
        setupChangeListener(temperatureControl);
        setupChangeListener(maxTokensControl);
        setupChangeListener(topPControl);
        setupChangeListener(topKControl);
        setupChangeListener(minPControl);
        setupChangeListener(typicalPControl);
        setupChangeListener(tfsZControl);
        setupChangeListener(repeatPenaltyControl);
        setupChangeListener(repeatLastNControl);
        setupChangeListener(presencePenaltyControl);
        setupChangeListener(frequencyPenaltyControl);
        
        scrolled.setContent(content);
        scrolled.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        tabItem.setControl(scrolled);
    }
    
    private void createAdvancedTab(TabFolder parent) {
        TabItem tabItem = new TabItem(parent, SWT.NONE);
        tabItem.setText("Advanced");
        
        ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);
        
        Composite content = new Composite(scrolled, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        
        Group mirostatGroup = new Group(content, SWT.NONE);
        mirostatGroup.setText("Mirostat");
        mirostatGroup.setLayout(new GridLayout(1, false));
        mirostatGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Mirostat dropdown
        Composite mirostatComp = new Composite(mirostatGroup, SWT.NONE);
        mirostatComp.setLayout(new GridLayout(2, false));
        mirostatComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(mirostatComp, SWT.NONE).setText("Mirostat:");
        mirostatCombo = new Combo(mirostatComp, SWT.READ_ONLY);
        mirostatCombo.setItems(new String[]{"Disabled", "Mirostat v1", "Mirostat v2"});
        mirostatCombo.select(Constants.DEFAULT_MIROSTAT);
        mirostatCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        mirostatTauControl = new ParameterControl(mirostatGroup, "Mirostat Tau", Constants.DEFAULT_MIROSTAT_TAU, Constants.MIROSTAT_TAU_MIN, Constants.MIROSTAT_TAU_MAX, 1, Constants.DEFAULT_MIROSTAT_ENABLED);
        mirostatEtaControl = new ParameterControl(mirostatGroup, "Mirostat Eta", Constants.DEFAULT_MIROSTAT_ETA, Constants.MIROSTAT_ETA_MIN, Constants.MIROSTAT_ETA_MAX, 1, Constants.DEFAULT_MIROSTAT_ENABLED);
        
        Group dynatempGroup = new Group(content, SWT.NONE);
        dynatempGroup.setText("Dynatemp");
        dynatempGroup.setLayout(new GridLayout(1, false));
        dynatempGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        dynatempRangeControl = new ParameterControl(dynatempGroup, "Dynatemp Range", Constants.DEFAULT_DYNATEMP_RANGE, Constants.DYNATEMP_RANGE_MIN, Constants.DYNATEMP_RANGE_MAX, 1, Constants.DEFAULT_DYNATEMP_ENABLED);
        dynatempExponentControl = new ParameterControl(dynatempGroup, "Dynatemp Exponent", Constants.DEFAULT_DYNATEMP_EXPONENT, Constants.DYNATEMP_EXPONENT_MIN, Constants.DYNATEMP_EXPONENT_MAX, 1, Constants.DEFAULT_DYNATEMP_ENABLED);
        
        Group xtcGroup = new Group(content, SWT.NONE);
        xtcGroup.setText("XTC");
        xtcGroup.setLayout(new GridLayout(1, false));
        xtcGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        xtcThresholdControl = new ParameterControl(xtcGroup, "XTC Threshold", Constants.DEFAULT_XTC_THRESHOLD, Constants.XTC_THRESHOLD_MIN, Constants.XTC_THRESHOLD_MAX, 1, Constants.DEFAULT_XTC_ENABLED);
        xtcProbabilityControl = new ParameterControl(xtcGroup, "XTC Probability", Constants.DEFAULT_XTC_PROBABILITY, Constants.XTC_PROBABILITY_MIN, Constants.XTC_PROBABILITY_MAX, 1, Constants.DEFAULT_XTC_ENABLED);
        
        Group dryGroup = new Group(content, SWT.NONE);
        dryGroup.setText("DRY");
        dryGroup.setLayout(new GridLayout(1, false));
        dryGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        dryMultiplierControl = new ParameterControl(dryGroup, "DRY Multiplier", Constants.DEFAULT_DRY_MULTIPLIER, Constants.DRY_MULTIPLIER_MIN, Constants.DRY_MULTIPLIER_MAX, 1, Constants.DEFAULT_DRY_ENABLED);
        dryBaseControl = new ParameterControl(dryGroup, "DRY Base", Constants.DEFAULT_DRY_BASE, Constants.DRY_BASE_MIN, Constants.DRY_BASE_MAX, 1, Constants.DEFAULT_DRY_ENABLED);
        dryAllowedLengthControl = new ParameterControl(dryGroup, "DRY Allowed Length", Constants.DEFAULT_DRY_ALLOWED_LENGTH, Constants.DRY_ALLOWED_LENGTH_MIN, Constants.DRY_ALLOWED_LENGTH_MAX, Constants.DEFAULT_DRY_ENABLED);
        dryPenaltyLastNControl = new ParameterControl(dryGroup, "DRY Penalty Last N", Constants.DEFAULT_DRY_PENALTY_LAST_N, Constants.DRY_PENALTY_LAST_N_MIN, Constants.DRY_PENALTY_LAST_N_MAX, Constants.DEFAULT_DRY_ENABLED);
        
        Composite drySeqComp = new Composite(dryGroup, SWT.NONE);
        drySeqComp.setLayout(new GridLayout(2, false));
        drySeqComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        new Label(drySeqComp, SWT.NONE).setText("DRY Sequence Breakers:");
        drySequenceBreakersText = new Text(drySeqComp, SWT.BORDER);
        drySequenceBreakersText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        drySequenceBreakersText.setText(Constants.DEFAULT_DRY_SEQUENCE_BREAKERS);
        drySequenceBreakersText.setToolTipText("Space-separated");
        
        setupChangeListener(mirostatTauControl);
        setupChangeListener(mirostatEtaControl);
        setupChangeListener(dynatempRangeControl);
        setupChangeListener(dynatempExponentControl);
        setupChangeListener(xtcThresholdControl);
        setupChangeListener(xtcProbabilityControl);
        setupChangeListener(dryMultiplierControl);
        setupChangeListener(dryBaseControl);
        setupChangeListener(dryAllowedLengthControl);
        setupChangeListener(dryPenaltyLastNControl);
        
        scrolled.setContent(content);
        scrolled.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        tabItem.setControl(scrolled);
    }
    
    private void createConstraintsTab(TabFolder parent) {
        TabItem tabItem = new TabItem(parent, SWT.NONE);
        tabItem.setText("Constraints");
        
        ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);
        
        Composite content = new Composite(scrolled, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        
        Group structureGroup = new Group(content, SWT.NONE);
        structureGroup.setText("Structure Constraints");
        structureGroup.setLayout(new GridLayout(1, false));
        structureGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Grammar
        new Label(structureGroup, SWT.NONE).setText("Grammar (EBNF):");
        grammarText = new Text(structureGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        grammarText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // JSON Schema
        new Label(structureGroup, SWT.NONE).setText("JSON Schema:");
        jsonSchemaText = new Text(structureGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        jsonSchemaText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Group tokenGroup = new Group(content, SWT.NONE);
        tokenGroup.setText("Token Control");
        tokenGroup.setLayout(new GridLayout(1, false));
        tokenGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Logit Bias
        new Label(tokenGroup, SWT.NONE).setText("Logit Bias:");
        logitBiasText = new Text(tokenGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        logitBiasText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        logitBiasText.setText(Constants.DEFAULT_LOGIT_BIAS);
        logitBiasText.setToolTipText("Format: [[token_id,bias],...] or [[\"token\",bias],...]");
        
        // Banned Tokens
        new Label(tokenGroup, SWT.NONE).setText("Banned Tokens:");
        bannedTokensText = new Text(tokenGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        bannedTokensText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        bannedTokensText.setToolTipText("Comma-separated");
        
        scrolled.setContent(content);
        scrolled.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        tabItem.setControl(scrolled);
    }
    
    private void createTemplatesTab(TabFolder parent) {
        TabItem tabItem = new TabItem(parent, SWT.NONE);
        tabItem.setText("Templates");
        
        ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);
        
        Composite content = new Composite(scrolled, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        
        Group templateGroup = new Group(content, SWT.NONE);
        templateGroup.setText("Instruction Templates");
        templateGroup.setLayout(new GridLayout(2, false));
        templateGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Template selection
        new Label(templateGroup, SWT.NONE).setText("Template:");
        templateCombo = new Combo(templateGroup, SWT.READ_ONLY);
        templateCombo.setItems(Constants.getTemplateNames());
        templateCombo.select(Constants.getDefaultTemplateIndex());
        templateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        templateCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                loadTemplateFromCombo();
            }
        });
        
        // Template fields
        new Label(templateGroup, SWT.NONE).setText("System Prefix:");
        templateSysPrefixText = new Text(templateGroup, SWT.BORDER);
        templateSysPrefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        templateSysPrefixText.addModifyListener(e -> checkForCustomTemplate());
        
        new Label(templateGroup, SWT.NONE).setText("System Suffix:");
        templateSysSuffixText = new Text(templateGroup, SWT.BORDER);
        templateSysSuffixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        templateSysSuffixText.addModifyListener(e -> checkForCustomTemplate());
        
        new Label(templateGroup, SWT.NONE).setText("Instruction Prefix:");
        templateInstPrefixText = new Text(templateGroup, SWT.BORDER);
        templateInstPrefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        templateInstPrefixText.addModifyListener(e -> checkForCustomTemplate());
        
        new Label(templateGroup, SWT.NONE).setText("Instruction Suffix:");
        templateInstSuffixText = new Text(templateGroup, SWT.BORDER);
        templateInstSuffixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        templateInstSuffixText.addModifyListener(e -> checkForCustomTemplate());
        
        new Label(templateGroup, SWT.NONE).setText("EOS Token:");
        templateEosText = new Text(templateGroup, SWT.BORDER);
        templateEosText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        templateEosText.addModifyListener(e -> checkForCustomTemplate());
        
        scrolled.setContent(content);
        scrolled.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        tabItem.setControl(scrolled);
    }
    
    private void createButtonBar() {
        Composite buttonBar = new Composite(shell, SWT.NONE);
        buttonBar.setLayout(new GridLayout(3, true));
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        Button okButton = new Button(buttonBar, SWT.PUSH);
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
        
        Button cancelButton = new Button(buttonBar, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
        
        Button resetButton = new Button(buttonBar, SWT.PUSH);
        resetButton.setText("Reset to Defaults");
        resetButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        resetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetToDefaults();
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
    
    private void moveSamplerUp() {
        int index = samplersTable.getSelectionIndex();
        if (index > 0) {
            TableItem item = samplersTable.getItem(index);
            String text = item.getText();
            boolean checked = item.getChecked();
            
            item.dispose();
            
            TableItem newItem = new TableItem(samplersTable, SWT.NONE, index - 1);
            newItem.setText(text);
            newItem.setChecked(checked);
            
            samplersTable.setSelection(index - 1);
        }
    }
    
    private void moveSamplerDown() {
        int index = samplersTable.getSelectionIndex();
        if (index >= 0 && index < samplersTable.getItemCount() - 1) {
            TableItem item = samplersTable.getItem(index);
            String text = item.getText();
            boolean checked = item.getChecked();
            
            item.dispose();
            
            TableItem newItem = new TableItem(samplersTable, SWT.NONE, index + 1);
            newItem.setText(text);
            newItem.setChecked(checked);
            
            samplersTable.setSelection(index + 1);
        }
    }
    
    private void loadSamplersTable(String samplersString) {
        // Rebuild entire table to avoid disposal issues
        samplersTable.removeAll();
        
        String[] enabledSamplers = new String[0];
        if (samplersString != null && !samplersString.trim().isEmpty()) {
            enabledSamplers = samplersString.split(",");
        }
        
        // Create items in the order specified by enabledSamplers first
        java.util.Set<String> addedSamplers = new java.util.HashSet<>();
        for (String sampler : enabledSamplers) {
            if (java.util.Arrays.asList(Constants.DEFAULT_SAMPLERS).contains(sampler)) {
                TableItem item = new TableItem(samplersTable, SWT.NONE);
                item.setText(sampler);
                item.setChecked(true);
                addedSamplers.add(sampler);
            }
        }
        
        // Add remaining samplers (unchecked)
        for (String sampler : Constants.DEFAULT_SAMPLERS) {
            if (!addedSamplers.contains(sampler)) {
                TableItem item = new TableItem(samplersTable, SWT.NONE);
                item.setText(sampler);
                item.setChecked(false);
            }
        }
    }
    
    private void loadTemplateFromCombo() {
        isLoadingTemplate = true;
        try {
        String selectedTemplate = templateCombo.getText();
        String[] template = Constants.getTemplateByName(selectedTemplate);
        
        if (template != null) {
            templateSysPrefixText.setText(template[Constants.TEMPLATE_SYS_PREFIX_INDEX]);
            templateSysSuffixText.setText(template[Constants.TEMPLATE_SYS_SUFFIX_INDEX]);
            templateInstPrefixText.setText(template[Constants.TEMPLATE_INST_PREFIX_INDEX]);
            templateInstSuffixText.setText(template[Constants.TEMPLATE_INST_SUFFIX_INDEX]);
            templateEosText.setText(template[Constants.TEMPLATE_EOS_INDEX]);
        }
        } finally {
            isLoadingTemplate = false;
        }
    }
    
    private void checkForCustomTemplate() {
        if (isLoadingTemplate) {
            return; // Don't check during template loading
        }
        
        String currentTemplate = templateCombo.getText();
        if (currentTemplate.equals(Constants.CUSTOM_TEMPLATE_NAME)) {
            return; // Already showing custom
        }
        
        String[] template = Constants.getTemplateByName(currentTemplate);
        if (template != null) {
            // Check if current values match the selected template
            boolean matches = template[Constants.TEMPLATE_SYS_PREFIX_INDEX].equals(templateSysPrefixText.getText()) &&
                            template[Constants.TEMPLATE_SYS_SUFFIX_INDEX].equals(templateSysSuffixText.getText()) &&
                            template[Constants.TEMPLATE_INST_PREFIX_INDEX].equals(templateInstPrefixText.getText()) &&
                            template[Constants.TEMPLATE_INST_SUFFIX_INDEX].equals(templateInstSuffixText.getText()) &&
                            template[Constants.TEMPLATE_EOS_INDEX].equals(templateEosText.getText());
            
            if (!matches) {
                // Add "Custom" option if not already present
                String[] items = templateCombo.getItems();
                if (!java.util.Arrays.asList(items).contains(Constants.CUSTOM_TEMPLATE_NAME)) {
                    templateCombo.add(Constants.CUSTOM_TEMPLATE_NAME);
                }
                templateCombo.setText(Constants.CUSTOM_TEMPLATE_NAME);
            }
        }
    }
    
    private String getSamplersFromTable() {
        java.util.List<String> enabledSamplers = new java.util.ArrayList<>();
        
        for (TableItem item : samplersTable.getItems()) {
            if (item.getChecked()) {
                enabledSamplers.add(item.getText().trim());
            }
        }
        
        return String.join(",", enabledSamplers);
    }
    
    private void loadParameters() {
        // Basic tab
        seedControl.setIntValue(parameters.getSeed());
        seedControl.setEnabled(parameters.isSeedEnabled());
        loadSamplersTable(parameters.getSamplers());
        temperatureControl.setDoubleValue(parameters.getTemperature());
        temperatureControl.setEnabled(parameters.isTemperatureEnabled());
        maxTokensControl.setIntValue(parameters.getMaxTokens());
        maxTokensControl.setEnabled(parameters.isMaxTokensEnabled());
        topPControl.setDoubleValue(parameters.getTopP());
        topPControl.setEnabled(parameters.isTopPEnabled());
        topKControl.setIntValue(parameters.getTopK());
        topKControl.setEnabled(parameters.isTopKEnabled());
        minPControl.setDoubleValue(parameters.getMinP());
        minPControl.setEnabled(parameters.isMinPEnabled());
        
        // Repetition tab
        repeatPenaltyControl.setDoubleValue(parameters.getRepeatPenalty());
        repeatPenaltyControl.setEnabled(parameters.isRepeatPenaltyEnabled());
        repeatLastNControl.setIntValue(parameters.getRepeatLastN());
        repeatLastNControl.setEnabled(parameters.isRepeatLastNEnabled());
        presencePenaltyControl.setDoubleValue(parameters.getPresencePenalty());
        presencePenaltyControl.setEnabled(parameters.isPresencePenaltyEnabled());
        frequencyPenaltyControl.setDoubleValue(parameters.getFrequencyPenalty());
        frequencyPenaltyControl.setEnabled(parameters.isFrequencyPenaltyEnabled());
        penalizeNlCheck.setSelection(parameters.isPenalizeNl());
        
        // Advanced tab
        typicalPControl.setDoubleValue(parameters.getTypicalP());
        typicalPControl.setEnabled(parameters.isTypicalPEnabled());
        tfsZControl.setDoubleValue(parameters.getTfsZ());
        tfsZControl.setEnabled(parameters.isTfsZEnabled());
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
        
        // Constraints tab
        grammarText.setText(parameters.getGrammar());
        jsonSchemaText.setText(parameters.getJsonSchema());
        logitBiasText.setText(parameters.getLogitBias());
        stoppingStringsText.setText(parameters.getStoppingStrings());
        bannedTokensText.setText(parameters.getBannedTokens());
        ignoreEosCheck.setSelection(parameters.isIgnoreEos());
        
        isLoadingTemplate = true;
        try {
        // Templates tab
        templateCombo.setText(parameters.getTemplateName());
        templateSysPrefixText.setText(parameters.getTemplateSysPrefix());
        templateSysSuffixText.setText(parameters.getTemplateSysSuffix());
        templateInstPrefixText.setText(parameters.getTemplateInstPrefix());
        templateInstSuffixText.setText(parameters.getTemplateInstSuffix());
        templateEosText.setText(parameters.getTemplateEos());
        } finally {
            isLoadingTemplate = false;
        }
        
        updateEnabledStates();
    }
    
    private boolean saveParameters() {
        // Validate all parameters first
        if (!validateParameters()) {
            return false;
        }
        
        // Basic tab
        parameters.setSeed(seedControl.getIntValue());
        parameters.setSeedEnabled(seedControl.isEnabled());
        String currentSamplers = getSamplersFromTable();
        parameters.setSamplers(currentSamplers);
        parameters.setSamplersEnabled(!currentSamplers.equals(String.join(",", Constants.DEFAULT_SAMPLERS)));
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
        
        // Repetition tab
        parameters.setRepeatPenalty(repeatPenaltyControl.getDoubleValue());
        parameters.setRepeatPenaltyEnabled(repeatPenaltyControl.isEnabled());
        parameters.setRepeatLastN(repeatLastNControl.getIntValue());
        parameters.setRepeatLastNEnabled(repeatLastNControl.isEnabled());
        parameters.setPresencePenalty(presencePenaltyControl.getDoubleValue());
        parameters.setPresencePenaltyEnabled(presencePenaltyControl.isEnabled());
        parameters.setFrequencyPenalty(frequencyPenaltyControl.getDoubleValue());
        parameters.setFrequencyPenaltyEnabled(frequencyPenaltyControl.isEnabled());
        parameters.setPenalizeNl(penalizeNlCheck.getSelection());
        
        // Advanced tab
        parameters.setTypicalP(typicalPControl.getDoubleValue());
        parameters.setTypicalPEnabled(typicalPControl.isEnabled());
        parameters.setTfsZ(tfsZControl.getDoubleValue());
        parameters.setTfsZEnabled(tfsZControl.isEnabled());
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
        
        // Constraints tab
        parameters.setGrammar(grammarText.getText());
        parameters.setJsonSchema(jsonSchemaText.getText());
        parameters.setLogitBias(logitBiasText.getText());
        parameters.setStoppingStrings(stoppingStringsText.getText());
        parameters.setBannedTokens(bannedTokensText.getText());
        parameters.setIgnoreEos(ignoreEosCheck.getSelection());
        
        // Templates tab
        parameters.setTemplateName(templateCombo.getText());
        parameters.setTemplateSysPrefix(templateSysPrefixText.getText());
        parameters.setTemplateSysSuffix(templateSysSuffixText.getText());
        parameters.setTemplateInstPrefix(templateInstPrefixText.getText());
        parameters.setTemplateInstSuffix(templateInstSuffixText.getText());
        parameters.setTemplateEos(templateEosText.getText());
        
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