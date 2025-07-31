import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import java.util.List;

public class SessionUIManager {
    
    private SimpleMikuPad app;
    
    public SessionUIManager(SimpleMikuPad app) {
        this.app = app;
    }
    
    public void setupSessionUI() {
        setupEventListeners();
    }
    
    private void setupEventListeners() {
        // Session combo listener
        app.getSessionCombo().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchToSelectedSession();
            }
        });
        
        // Session button listeners
        app.getNewSessionButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createNewSession();
            }
        });
        
        app.getDeleteSessionButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteCurrentSession();
            }
        });
        
        app.getRenameSessionButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                renameCurrentSession();
            }
        });
        
        app.getCloneSessionButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                cloneCurrentSession();
            }
        });
        
        app.getExportSessionButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                exportCurrentSession();
            }
        });
        
        app.getImportSessionButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                importSession();
            }
        });
        
        // Sampling parameters button
        app.getSamplingParamsButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openSamplingParametersDialog();
            }
        });
        
        // Auto-save listeners for text fields
        ModifyListener autoSaveListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                autoSaveSessionState();
            }
        };
        
        app.getEndpointText().addModifyListener(autoSaveListener);
        app.getApiKeyText().addModifyListener(autoSaveListener);
        app.getModelText().addModifyListener(autoSaveListener);
        
        // Prompt text listener
        app.getPromptText().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                autoSaveSessionState();
                if (!app.isLoadingSession() && app.getGenerateButton().getEnabled()) {
                    app.getTokenManager().clearTokenColoring();
                }
            }
        });
    }
    
    public void loadCurrentSession() {
        refreshSessionCombo();
        loadSessionIntoUI(app.getSessionManager().getCurrentSession());
    }
    
    public void refreshSessionCombo() {
        app.getSessionCombo().removeAll();
        List<Session> sessions = app.getSessionManager().getAllSessions();
        Session currentSession = app.getSessionManager().getCurrentSession();
        
        int selectedIndex = 0;
        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);
            app.getSessionCombo().add(session.toString());
            if (session.getId().equals(currentSession.getId())) {
                selectedIndex = i;
            }
        }
        
        app.getSessionCombo().select(selectedIndex);
        app.getDeleteSessionButton().setEnabled(sessions.size() > 1);
    }
    
    public void loadSessionIntoUI(Session session) {
        if (session == null) return;
        
        app.setLoadingSession(true);
        try {
            app.getEndpointText().setText(session.getEndpoint());
            app.getApiKeyText().setText(session.getApiKey());
            app.getModelText().setText(session.getModel());
            app.getPromptText().setText(session.getPromptText());
            
            updateSamplingParamsLabel();
            app.getTokenManager().clearTokenColoring();
            app.updateStatus("Loaded session: " + session.getName());
        } finally {
            app.setLoadingSession(false);
        }
    }
    
    public void saveCurrentSessionState() {
        if (app.isLoadingSession()) return;
        
        Session currentSession = app.getSessionManager().getCurrentSession();
        if (currentSession != null && !app.getEndpointText().isDisposed()) {
            currentSession.setEndpoint(app.getEndpointText().getText());
            currentSession.setApiKey(app.getApiKeyText().getText());
            currentSession.setModel(app.getModelText().getText());
            currentSession.setPromptText(app.getPromptText().getText());
            
            app.getSessionManager().saveCurrentState();
        }
    }
    
    private void autoSaveSessionState() {
        app.getDisplay().timerExec(500, () -> saveCurrentSessionState());
    }
    
    private void switchToSelectedSession() {
        if (app.isLoadingSession()) return;
        
        saveCurrentSessionState();
        
        int selectedIndex = app.getSessionCombo().getSelectionIndex();
        if (selectedIndex >= 0) {
            List<Session> sessions = app.getSessionManager().getAllSessions();
            if (selectedIndex < sessions.size()) {
                Session selectedSession = sessions.get(selectedIndex);
                app.getSessionManager().setCurrentSession(selectedSession.getId());
                resetUndoHistory();
                loadSessionIntoUI(selectedSession);
            }
        }
    }
    
    private void createNewSession() {
        String name = promptForSessionName("New Session");
        if (name != null) {
            saveCurrentSessionState();
            Session newSession = app.getSessionManager().createSession(name);
            app.getSessionManager().setCurrentSession(newSession.getId());
            resetUndoHistory();
            refreshSessionCombo();
            loadSessionIntoUI(newSession);
        }
    }
    
    private void deleteCurrentSession() {
        Session currentSession = app.getSessionManager().getCurrentSession();
        if (currentSession != null) {
            MessageBox messageBox = new MessageBox(app.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
            messageBox.setMessage("Are you sure you want to delete session '" + currentSession.getName() + "'?");
            messageBox.setText("Delete Session");
            
            if (messageBox.open() == SWT.YES) {
                if (app.getSessionManager().deleteSession(currentSession.getId())) {
                    resetUndoHistory();
                    refreshSessionCombo();
                    loadSessionIntoUI(app.getSessionManager().getCurrentSession());
                    app.updateStatus("Deleted session: " + currentSession.getName());
                }
            }
        }
    }
    
    private void renameCurrentSession() {
        Session currentSession = app.getSessionManager().getCurrentSession();
        if (currentSession != null) {
            String newName = promptForSessionName(currentSession.getName());
            if (newName != null && !newName.equals(currentSession.getName())) {
                app.getSessionManager().renameSession(currentSession.getId(), newName);
                refreshSessionCombo();
                app.updateStatus("Renamed session to: " + newName);
            }
        }
    }
    
    private void cloneCurrentSession() {
        Session currentSession = app.getSessionManager().getCurrentSession();
        if (currentSession != null) {
            saveCurrentSessionState();
            Session clonedSession = app.getSessionManager().cloneSession(currentSession.getId());
            if (clonedSession != null) {
                app.getSessionManager().setCurrentSession(clonedSession.getId());
                resetUndoHistory();
                refreshSessionCombo();
                loadSessionIntoUI(clonedSession);
                app.updateStatus("Cloned session: " + clonedSession.getName());
            }
        }
    }
    
    private void exportCurrentSession() {
        Session currentSession = app.getSessionManager().getCurrentSession();
        if (currentSession != null) {
            saveCurrentSessionState();
            
            FileDialog dialog = new FileDialog(app.getShell(), SWT.SAVE);
            dialog.setFilterExtensions(new String[]{"*.json"});
            dialog.setFilterNames(new String[]{"JSON Files (*.json)"});
            dialog.setFileName(currentSession.getName() + ".json");
            
            String filename = dialog.open();
            if (filename != null) {
                try {
                    app.getSessionManager().exportSession(currentSession.getId(), new java.io.File(filename));
                    app.updateStatus("Exported session to: " + filename);
                } catch (Exception e) {
                    MessageBox messageBox = new MessageBox(app.getShell(), SWT.ERROR | SWT.OK);
                    messageBox.setMessage("Failed to export session: " + e.getMessage());
                    messageBox.setText("Export Error");
                    messageBox.open();
                }
            }
        }
    }
    
    private void importSession() {
        FileDialog dialog = new FileDialog(app.getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[]{"*.json"});
        dialog.setFilterNames(new String[]{"JSON Files (*.json)"});
        
        String filename = dialog.open();
        if (filename != null) {
            try {
                Session importedSession = app.getSessionManager().importSession(new java.io.File(filename));
                app.getSessionManager().setCurrentSession(importedSession.getId());
                resetUndoHistory();
                refreshSessionCombo();
                loadSessionIntoUI(importedSession);
                app.updateStatus("Imported session: " + importedSession.getName());
            } catch (Exception e) {
                MessageBox messageBox = new MessageBox(app.getShell(), SWT.ERROR | SWT.OK);
                messageBox.setMessage("Failed to import session: " + e.getMessage());
                messageBox.setText("Import Error");
                messageBox.open();
            }
        }
    }
    
    private void openSamplingParametersDialog() {
        Session currentSession = app.getSessionManager().getCurrentSession();
        if (currentSession != null) {
            SamplingParametersDialog dialog = new SamplingParametersDialog(app.getShell(), currentSession.getSamplingParams());
            if (dialog.open()) {
                currentSession.setSamplingParams(dialog.getParameters());
                updateSamplingParamsLabel();
                autoSaveSessionState();
                app.updateStatus("Sampling parameters updated");
            }
        }
    }
    
    private void updateSamplingParamsLabel() {
        Session currentSession = app.getSessionManager().getCurrentSession();
        if (currentSession != null && app.getSamplingParamsLabel() != null) {
            SamplingParameters params = currentSession.getSamplingParams();
            String summary = String.format("Temp: %.2f, Top-K: %d, Top-P: %.2f, Min-P: %.2f", 
                params.getTemperature(), params.getTopK(), params.getTopP(), params.getMinP());
            app.getSamplingParamsLabel().setText(summary);
        }
    }
    
    /**
     * Resets the undo history - called when switching session contexts
     */
    private void resetUndoHistory() {
        app.getUndoManager().reset();
    }
    
    private String promptForSessionName(String defaultName) {
        Shell dialog = new Shell(app.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Session Name");
        dialog.setSize(300, 150);
        dialog.setLayout(new GridLayout(2, false));
        
        Point parentSize = app.getShell().getSize();
        Point parentLocation = app.getShell().getLocation();
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
}