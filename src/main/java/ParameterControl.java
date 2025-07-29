import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * A reusable parameter control widget with checkbox, label, and value input.
 * Supports both integer and double values with proper precision.
 */
public class ParameterControl extends Composite {
    
    public interface ParameterChangeListener {
        void onParameterChanged();
    }
    
    private Button enabledCheckbox;
    private Label nameLabel;
    private Text valueText;
    private Spinner intSpinner;
    private Label rangeLabel;
    
    private boolean isInteger;
    private double minValue;
    private double maxValue;
    private int decimalPlaces;
    private ParameterChangeListener changeListener;
    
    /**
     * Creates a double parameter control
     */
    public ParameterControl(Composite parent, String name, double defaultValue, 
                           double minValue, double maxValue, int decimalPlaces, 
                           boolean defaultEnabled) {
        super(parent, SWT.NONE);
        this.isInteger = false;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.decimalPlaces = decimalPlaces;
        
        createUI(name, String.valueOf(defaultValue), defaultEnabled);
    }
    
    /**
     * Creates an integer parameter control
     */
    public ParameterControl(Composite parent, String name, int defaultValue, 
                           int minValue, int maxValue, boolean defaultEnabled) {
        super(parent, SWT.NONE);
        this.isInteger = true;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.decimalPlaces = 0;
        
        createUI(name, String.valueOf(defaultValue), defaultEnabled);
    }
    
    private void createUI(String name, String defaultValue, boolean defaultEnabled) {
        setLayout(new GridLayout(4, false));
        setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Enabled checkbox
        enabledCheckbox = new Button(this, SWT.CHECK);
        enabledCheckbox.setSelection(defaultEnabled);
        enabledCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabledState();
                notifyChange();
            }
        });
        
        // Parameter name label
        nameLabel = new Label(this, SWT.NONE);
        nameLabel.setText(name + ":");
        nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        // Value input
        if (isInteger) {
            intSpinner = new Spinner(this, SWT.BORDER);
            intSpinner.setMinimum((int)minValue);
            intSpinner.setMaximum((int)maxValue);
            intSpinner.setSelection(Integer.parseInt(defaultValue));
            intSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            intSpinner.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    notifyChange();
                }
            });
        } else {
            valueText = new Text(this, SWT.BORDER);
            valueText.setText(defaultValue);
            valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            valueText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    validateDoubleInput();
                    notifyChange();
                }
            });
        }
        
        // Range label
        rangeLabel = new Label(this, SWT.NONE);
        if (isInteger) {
            if (name.toLowerCase().contains("seed")) {
                rangeLabel.setText("(-1 = random)");
            } else if (name.toLowerCase().contains("repeat last n")) {
                rangeLabel.setText("(0=disabled, -1=ctx_size)");
            } else {
                rangeLabel.setText("(" + (int)minValue + " - " + (int)maxValue + ")");
            }
        } else {
            rangeLabel.setText("(" + formatDouble(minValue) + " - " + formatDouble(maxValue) + ")");
        }
        rangeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        updateEnabledState();
    }
    
    private void updateEnabledState() {
        boolean enabled = enabledCheckbox.getSelection();
        nameLabel.setEnabled(enabled);
        if (valueText != null) {
            valueText.setEnabled(enabled);
        }
        if (intSpinner != null) {
            intSpinner.setEnabled(enabled);
        }
        rangeLabel.setEnabled(enabled);
    }
    
    private void validateDoubleInput() {
        if (valueText == null) return;
        
        try {
            double value = Double.parseDouble(valueText.getText());
            if (value < minValue || value > maxValue) {
                valueText.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
            } else {
                valueText.setBackground(null);
            }
        } catch (NumberFormatException e) {
            valueText.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
        }
    }
    
    private String formatDouble(double value) {
        if (decimalPlaces == 0) {
            return String.valueOf((int)value);
        } else {
            return String.format("%." + decimalPlaces + "f", value);
        }
    }
    
    private void notifyChange() {
        if (changeListener != null) {
            changeListener.onParameterChanged();
        }
    }
    
    // Public methods
    
    public void setChangeListener(ParameterChangeListener listener) {
        this.changeListener = listener;
    }
    
    public boolean isEnabled() {
        return enabledCheckbox.getSelection();
    }
    
    public void setEnabled(boolean enabled) {
        enabledCheckbox.setSelection(enabled);
        updateEnabledState();
    }
    
    public double getDoubleValue() {
        if (isInteger) {
            return intSpinner.getSelection();
        } else {
            try {
                return Double.parseDouble(valueText.getText());
            } catch (NumberFormatException e) {
                return minValue; // Return safe default
            }
        }
    }
    
    public int getIntValue() {
        if (isInteger) {
            return intSpinner.getSelection();
        } else {
            return (int)getDoubleValue();
        }
    }
    
    public void setDoubleValue(double value) {
        if (isInteger) {
            intSpinner.setSelection((int)value);
        } else {
            valueText.setText(formatDouble(value));
        }
    }
    
    public void setIntValue(int value) {
        if (isInteger) {
            intSpinner.setSelection(value);
        } else {
            valueText.setText(String.valueOf(value));
        }
    }
    
    public boolean isValid() {
        if (!isEnabled()) return true;
        
        try {
            double value = getDoubleValue();
            return value >= minValue && value <= maxValue;
        } catch (Exception e) {
            return false;
        }
    }
}