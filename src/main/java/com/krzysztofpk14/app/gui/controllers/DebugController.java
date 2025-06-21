package com.krzysztofpk14.app.gui.controllers;

import com.krzysztofpk14.app.gui.model.TradingAppModel;
import com.krzysztofpk14.app.gui.util.ApiCommunicationLogger;
import com.krzysztofpk14.app.gui.util.XMLFormatter;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;

/**
 * Controller for Debug tab showing API communication.
 */
public class DebugController {
    private TradingAppModel model;
    private TableView<ApiCommunicationLogger.ApiLogEntry> logTable;
    private TextArea detailTextArea;
    private WebView detailWebView;
    private final ObservableList<ApiCommunicationLogger.ApiLogEntry> logEntries = 
            FXCollections.observableArrayList();
    private CheckBox autoScrollCheckBox;
    private CheckBox showXmlFormatted;
    private CheckBox showWebView;
    
    public DebugController(TradingAppModel model) {
        this.model = model;
        
        // Register for log updates
        model.getApiLogger().setLogEntryAddedListener(this::onLogEntryAdded);
        
        // Initialize with existing logs
        logEntries.addAll(model.getApiLogger().getLogEntries());
    }
    
    /**
     * Creates the Debug tab.
     */
    public Tab createTab() {
        Tab tab = new Tab("Debug");
        tab.setClosable(false);
        
        BorderPane mainPane = new BorderPane();
        
        // Top control bar
        HBox controlBox = createControlBox();
        
        // Center split pane
        SplitPane splitPane = new SplitPane();
        
        // Log table on top
        logTable = createLogTable();
        VBox.setVgrow(logTable, Priority.ALWAYS);
        
        // Detail view at bottom
        VBox detailPane = createDetailPane();
        VBox.setVgrow(detailPane, Priority.ALWAYS);
        
        splitPane.getItems().addAll(logTable, detailPane);
        splitPane.setDividerPositions(0.5);
        
        mainPane.setTop(controlBox);
        mainPane.setCenter(splitPane);
        
        tab.setContent(mainPane);
        
        // Setup selection listener for the log table
        logTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> showLogDetails(newSelection));
        
        return tab;
    }
    
    /**
     * Creates the control box with buttons and controls.
     */
    private HBox createControlBox() {
        HBox controlBox = new HBox(10);
        controlBox.setPadding(new Insets(10));
        
        Button clearButton = new Button("Clear Logs");
        clearButton.setOnAction(e -> {
            model.getApiLogger().clearLogs();
            logEntries.clear();
            detailTextArea.clear();
            detailWebView.getEngine().loadContent("");
        });
        
        autoScrollCheckBox = new CheckBox("Auto-scroll");
        autoScrollCheckBox.setSelected(true);
        
        showXmlFormatted = new CheckBox("Format XML");
        showXmlFormatted.setSelected(true);
        showXmlFormatted.setOnAction(e -> {
            // Refresh the current selection if any
            ApiCommunicationLogger.ApiLogEntry selected = 
                    logTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showLogDetails(selected);
            }
        });
        
        showWebView = new CheckBox("HTML View");
        showWebView.setSelected(false);
        showWebView.setOnAction(e -> {
            boolean useWebView = showWebView.isSelected();
            detailTextArea.setVisible(!useWebView);
            detailTextArea.setManaged(!useWebView);
            detailWebView.setVisible(useWebView);
            detailWebView.setManaged(useWebView);
            
            // Refresh the current selection
            ApiCommunicationLogger.ApiLogEntry selected = 
                    logTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showLogDetails(selected);
            }
        });
        
        controlBox.getChildren().addAll(
                clearButton, 
                autoScrollCheckBox, 
                showXmlFormatted,
                showWebView);
        
        return controlBox;
    }
    
    /**
     * Creates the log table.
     */
    private TableView<ApiCommunicationLogger.ApiLogEntry> createLogTable() {
        TableView<ApiCommunicationLogger.ApiLogEntry> table = new TableView<>();
        
        TableColumn<ApiCommunicationLogger.ApiLogEntry, String> timeCol = 
                new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getFormattedTimestamp()));
        timeCol.setPrefWidth(150);
        
        TableColumn<ApiCommunicationLogger.ApiLogEntry, String> directionCol = 
                new TableColumn<>("Type");
        directionCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDirection().name()));
        directionCol.setPrefWidth(80);
        
        TableColumn<ApiCommunicationLogger.ApiLogEntry, String> requestIdCol = 
                new TableColumn<>("Request ID");
        requestIdCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getRequestId()));
        requestIdCol.setPrefWidth(150);
        
        TableColumn<ApiCommunicationLogger.ApiLogEntry, String> contentPreviewCol = 
                new TableColumn<>("Content Preview");
        contentPreviewCol.setCellValueFactory(cellData -> {
            String content = cellData.getValue().getContent();
            if (content.length() > 50) {
                content = content.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(content);
        });
        contentPreviewCol.setPrefWidth(300);
        
        table.getColumns().addAll(timeCol, directionCol, requestIdCol, contentPreviewCol);
        table.setItems(logEntries);
        
        // Style based on direction
        table.setRowFactory(tv -> {
            TableRow<ApiCommunicationLogger.ApiLogEntry> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    if (newItem.getDirection() == ApiCommunicationLogger.ApiLogEntry.Direction.REQUEST) {
                        row.setStyle("-fx-background-color: #f0f8ff;"); // Light blue for requests
                    } else {
                        row.setStyle("-fx-background-color: #f0fff0;"); // Light green for responses
                    }
                } else {
                    row.setStyle("");
                }
            });
            // Add selection listener to change text color on click
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    // Bold black text for selected row
                    String currentStyle = row.getStyle();
                    row.setStyle(currentStyle + "; -fx-text-fill: #000000; -fx-font-weight: bold;");
                } else {
                    // Normal text for non-selected rows
                    ApiCommunicationLogger.ApiLogEntry item = row.getItem();
                    if (item != null) {
                        if (item.getDirection() == ApiCommunicationLogger.ApiLogEntry.Direction.REQUEST) {
                            row.setStyle("-fx-background-color: #f0f8ff; -fx-text-fill: #303030;");
                        } else {
                            row.setStyle("-fx-background-color: #f0fff0; -fx-text-fill: #303030;");
                        }
                    } else {
                        row.setStyle("-fx-text-fill: #303030;");
                    }
                }
            });
        
            return row;
        });
        
        return table;
    }
    
    /**
     * Creates the detail pane for showing log content.
     */
    private VBox createDetailPane() {
        VBox detailPane = new VBox();
        detailPane.setPadding(new Insets(5));
        
        detailTextArea = new TextArea();
        detailTextArea.setFont(Font.font("Monospaced", 12));
        detailTextArea.setEditable(false);
        detailTextArea.setWrapText(true);
        
        detailWebView = new WebView();
        detailWebView.setVisible(false);
        detailWebView.setManaged(false);
        
        Label detailLabel = new Label("Message Details:");
        
        detailPane.getChildren().addAll(detailLabel, detailTextArea, detailWebView);
        VBox.setVgrow(detailTextArea, Priority.ALWAYS);
        VBox.setVgrow(detailWebView, Priority.ALWAYS);
        
        return detailPane;
    }
    
    /**
     * Handles a new log entry being added.
     */
    private void onLogEntryAdded(ApiCommunicationLogger.ApiLogEntry entry) {
        Platform.runLater(() -> {
            logEntries.add(entry);
            
            if (autoScrollCheckBox != null && autoScrollCheckBox.isSelected()) {
                // Scroll to the new entry
                logTable.scrollTo(logEntries.size() - 1);
            }
        });
    }
    
    /**
     * Shows the details of a log entry in the detail view.
     */
    private void showLogDetails(ApiCommunicationLogger.ApiLogEntry entry) {
        if (entry == null) {
            detailTextArea.clear();
            detailWebView.getEngine().loadContent("");
            return;
        }
        
        String content = entry.getContent();
        
        // Format XML if needed
        if (showXmlFormatted.isSelected() && content.trim().startsWith("<")) {
            try {
                content = XMLFormatter.format(content);
            } catch (Exception e) {
                // If formatting fails, show original
                System.err.println("Failed to format XML: " + e.getMessage());
            }
        }
        
        // Update detail view
        if (showWebView.isSelected()) {
            // For web view, create HTML with syntax highlighting
            String html = createHtmlWithSyntaxHighlighting(entry, content);
            detailWebView.getEngine().loadContent(html);
        } else {
            // For text area, just show the text
            detailTextArea.setText(content);
            detailTextArea.positionCaret(0);
            if (entry.getDirection() == ApiCommunicationLogger.ApiLogEntry.Direction.REQUEST) {
                detailTextArea.setStyle("-fx-control-inner-background: #f0f8ff;"); // Light blue for requests
            } else {
                detailTextArea.setStyle("-fx-control-inner-background: #f0fff0;"); // Light green for responses
            }
        }
    }
    
    /**
     * Creates HTML content with syntax highlighting for XML.
     */
    private String createHtmlWithSyntaxHighlighting(
            ApiCommunicationLogger.ApiLogEntry entry, String content) {
        
        // Simple approach - could be enhanced with a proper XML syntax highlighter
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append("body { font-family: monospace; font-size: 12px; white-space: pre; }");
        html.append(".request { background-color: #f0f8ff; }");
        html.append(".response { background-color: #f0fff0; }");
        html.append(".tag { color: #0000ff; }");
        html.append(".attr { color: #990000; }");
        html.append(".value { color: #008800; }");
        html.append(".text { color: #000000; }");
        html.append("</style></head>");
        
        // Body class based on direction
        String bodyClass = entry.getDirection() == ApiCommunicationLogger.ApiLogEntry.Direction.REQUEST 
                ? "request" : "response";
        
        html.append("<body class='").append(bodyClass).append("'>");
        
        // Header info
        html.append("<div><b>Time:</b> ").append(entry.getFormattedTimestamp()).append("</div>");
        html.append("<div><b>Type:</b> ").append(entry.getDirection()).append("</div>");
        html.append("<div><b>Request ID:</b> ").append(entry.getRequestId()).append("</div>");
        html.append("<hr/>");
        
        // Content
        if (content.trim().startsWith("<")) {
            // Basic XML highlighting (a more comprehensive solution would use a proper XML parser)
            String highlighted = content
                    .replaceAll("<(/?[^>]+)>", "<span class='tag'>&lt;$1&gt;</span>")
                    .replaceAll("(\\w+)=\"([^\"]*)\"", "<span class='attr'>$1</span>=\"<span class='value'>$2</span>\"")
                    .replaceAll("\n", "<br/>");
            html.append(highlighted);
        } else {
            // Plain text
            html.append("<pre>").append(content.replace("<", "&lt;").replace(">", "&gt;")).append("</pre>");
        }
        
        html.append("</body></html>");
        return html.toString();
    }
}