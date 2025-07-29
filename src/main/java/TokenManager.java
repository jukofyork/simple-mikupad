import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenManager {
    
    private SimpleMikuPad app;
    private Map<Integer, TokenInfo> tokenMap = new HashMap<>();
    private Shell currentTooltip;
    private TokenInfo currentHoverToken;
    private boolean coloringEnabled = true;
    private List<Color> colors = new ArrayList<>();
    
    public TokenManager(SimpleMikuPad app) {
        this.app = app;
        // Don't setup listeners in constructor - promptText may not exist yet
    }
    
    public void setupEventListeners() {
        // Create colors when setting up (Display is available now)
        if (colors.isEmpty()) {
            createColors();
        }
        
        if (app.getPromptText() == null) {
            return; // UI not ready yet
        }
        
        if (app.getColorToggleButton() == null) {
            return; // UI not ready yet
        }
        
        // Mouse hover listener for tooltips
        app.getPromptText().addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                handleMouseHover(e);
            }
        });
        
        // Color toggle button
        app.getColorToggleButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                coloringEnabled = app.getColorToggleButton().getSelection();
                if (!coloringEnabled) {
                    clearTokenColoring();
                }
            }
        });
    }
    
    private void createColors() {
        for (int i = 0; i <= 100; i++) {
            float ratio = i / 100.0f;
            RGB rgb;
            
            if (ratio < 0.5f) {
                // Dark red to dark orange (0.0 to 0.5)
                float localRatio = ratio * 2;
                rgb = new RGB(
                    120 + (int)(60 * localRatio),  // 120-180 red
                    (int)(40 * localRatio),        // 0-40 green
                    0);                            // 0 blue
            } else {
                // Dark orange to dark green (0.5 to 1.0)
                float localRatio = (ratio - 0.5f) * 2;
                rgb = new RGB(
                    180 - (int)(180 * localRatio), // 180-0 red (remove all red)
                    40 + (int)(100 * localRatio),  // 40-140 green (more green)
                    0);                            // 0 blue
            }
            
            colors.add(new Color(app.getDisplay(), rgb));
        }
    }
    
    public void appendSingleToken(String token, double probability, List<TokenAlternative> alternatives) {
        if (app.getPromptText().isDisposed()) return;
        
        int startOffset = app.getPromptText().getCharCount();
        app.getPromptText().append(token);
        
        TokenInfo tokenInfo = new TokenInfo(token, probability, alternatives);
        tokenMap.put(startOffset, tokenInfo);
        
        if (coloringEnabled) {
            StyleRange style = new StyleRange();
            style.start = startOffset;
            style.length = token.length();
            style.background = getColorForProbability(probability);
            
            app.getPromptText().setStyleRange(style);
        }
    }
    
    public void clearTokenColoring() {
        app.getDisplay().asyncExec(() -> {
            if (!app.getPromptText().isDisposed()) {
                app.getPromptText().setStyleRanges(new StyleRange[0]);
                tokenMap.clear();
                hideTooltip();
            }
        });
    }
    
    private Color getColorForProbability(double probability) {
        probability = Math.max(0, Math.min(1, probability));
        int index = (int)(probability * 100);
        return colors.get(index);
    }
    
    private void handleMouseHover(MouseEvent e) {
        if (!coloringEnabled) return;
        
        try {
            int offset = app.getPromptText().getOffsetAtLocation(new Point(e.x, e.y));
            TokenInfo tokenInfo = findTokenAtOffset(offset);
            
            if (tokenInfo != null) {
                if (currentHoverToken != tokenInfo) {
                    currentHoverToken = tokenInfo;
                    showTooltip(e.x, e.y, tokenInfo);
                }
            } else {
                currentHoverToken = null;
                hideTooltip();
            }
        } catch (IllegalArgumentException ex) {
            currentHoverToken = null;
            hideTooltip();
        }
    }
    
    private TokenInfo findTokenAtOffset(int offset) {
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
        hideTooltip();
        
        currentTooltip = new Shell(app.getShell(), SWT.ON_TOP | SWT.TOOL);
        currentTooltip.setLayout(new GridLayout(1, false));
        currentTooltip.setBackground(app.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        
        Label altLabel = new Label(currentTooltip, SWT.NONE);
        StringBuilder message = new StringBuilder();
        
        if (tokenInfo.alternatives != null && !tokenInfo.alternatives.isEmpty()) {
            for (TokenAlternative alt : tokenInfo.alternatives) {
                double percentage = alt.probability * 100;
                message.append(String.format("%.1f%% - \"%s\"", percentage, escapeForTooltip(alt.token)));
                if (alt != tokenInfo.alternatives.get(tokenInfo.alternatives.size() - 1)) {
                    message.append("\n");
                }
            }
        }
        
        altLabel.setText(message.toString());
        altLabel.setBackground(app.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        altLabel.setForeground(app.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        
        currentTooltip.pack();
        
        Point displayPoint = app.getPromptText().toDisplay(x, y);
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
        return text.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r");
    }
    
    public void dispose() {
        for (Color color : colors) {
            if (!color.isDisposed()) {
                color.dispose();
            }
        }
    }
    
    // Helper classes
    public static class TokenInfo {
        final String text;
        final double probability;
        final List<TokenAlternative> alternatives;
        
        TokenInfo(String text, double probability, List<TokenAlternative> alternatives) {
            this.text = text;
            this.probability = probability;
            this.alternatives = alternatives;
        }
    }
    
    public static class TokenAlternative {
        final String token;
        final double probability;
        
        TokenAlternative(String token, double probability) {
            this.token = token;
            this.probability = probability;
        }
    }
}