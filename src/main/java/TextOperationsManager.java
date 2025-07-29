import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class TextOperationsManager {
    
    private SimpleMikuPad app;
    
    public TextOperationsManager(SimpleMikuPad app) {
        this.app = app;
    }
    
    public void initializeContextMenu() {
        Menu contextMenu = new Menu(app.getPromptText());
        
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
        
        app.getPromptText().setMenu(contextMenu);
    }
    
    private void executeTextOperation(String operationType) {
        if (app.getPromptText().isDisposed()) return;
        
        switch (operationType) {
            case "cut":
                app.getPromptText().cut();
                break;
            case "copy":
                app.getPromptText().copy();
                break;
            case "paste":
                app.getPromptText().paste();
                break;
            case "selectAll":
                app.getPromptText().selectAll();
                break;
        }
    }
}