package com.krzysztofpk14.app.gui;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.request.SecurityListRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.bossaapi.model.response.SecurityList;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import com.krzysztofpk14.app.bossaapi.server.BossaApiServer;
import com.krzysztofpk14.app.bossaapi.util.FixmlGenerator;
import com.krzysztofpk14.app.gui.model.TradingAppModel;
import com.krzysztofpk14.app.strategy.MovingAverageCrossoverStrategy;
import com.krzysztofpk14.app.strategy.RSIStrategy;
import com.krzysztofpk14.app.strategy.StrategyManager;
import com.krzysztofpk14.app.strategy.StrategyParameters;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service class handling interaction with the BossaAPI.
 */
public class BossaApiService {
    private TradingAppModel model;
    private BossaApiServer server;
    private BossaApiClient client;
    private StrategyManager strategyManager;
    
    private Consumer<MarketDataResponse> marketDataHandler;
    private Consumer<ExecutionReport> executionReportHandler;
    private Consumer<String> statusUpdateHandler;
    
    public BossaApiService(TradingAppModel model) {
        this.model = model;
    }
    
    /**
     * Initialize the API service components.
     */
    public void initialize() {
        try {
            updateStatus("Starting BossaAPI server...");
            initializeServer();
            
            // Wait for server to start
            Thread.sleep(1000);
            
            updateStatus("Connecting to server...");
            initializeClient();
            
            // Login
            String username = "BOS";
            String password = "BOS";
            updateStatus("Logging in as " + username + "...");
            
            CompletableFuture<UserResponse> loginFuture = client.loginAsync(username, password);
            UserResponse response = loginFuture.get();
            logResponse(response); 
            
            if (!response.isLoginSuccessful()) {
                updateStatus("Login failed: " + response.getUserStatusText());
                return;
            }
            
            // Get securities list
            updateStatus("Fetching available instruments...");
            fetchSecurityList();
            
            // Initialize strategy manager
            initializeStrategyManager();
            
            updateStatus("Connected and ready");
            
        } catch (Exception e) {
            updateStatus("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeServer() throws IOException {
        int port = 24444;
        server = new BossaApiServer(port);
        
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                updateStatus("Error starting server: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    private void initializeClient() throws IOException {
        client = new BossaApiClient();
        client.connect("localhost", 24444);
        
        // Register handlers for asynchronous responses
        client.registerMarketDataHandler("GUI", data -> {
            if (data != null) {
                // logResponse(data); // Optionally log market data responses
                
                // Then pass to handler
                if (marketDataHandler != null) {
                    marketDataHandler.accept(data);
                }
            }
        });
        
        client.registerExecutionReportHandler("GUI", report -> {
            if (report != null) {
                logResponse(report);
                
                if (executionReportHandler != null) {
                    executionReportHandler.accept(report);
                }
            }
        });
        
        model.setClient(client);
    }

    /**
     * Sends a request and logs it.
     * 
     * @param request The request to send
     * @param responseType The expected response type
     * @return CompletableFuture for the response
     */
    public void logRequest(BaseMessage request){
        try{
            String requestXml = FixmlGenerator.generateXml(request);
            model.getApiLogger().logRequest(
                request.getMessageId(),
                requestXml);
        } catch (JAXBException e) {
            updateStatus("Error generating request XML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    /**
     * Logs the response received from the API.
     * 
     * @param response The response to log
     */ 
    public void logResponse(BaseMessage response) {
        try {
            String responseXml = FixmlGenerator.generateXml(response);
            model.getApiLogger().logResponse(
                response.getMessageId(),
                responseXml);
        } catch (JAXBException e) {
            updateStatus("Error generating response XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fetches the list of available securities and updates the model.
     */
    private void fetchSecurityList() throws Exception {
        SecurityListRequest request = new SecurityListRequest();
        request.setSubscriptionRequestType(SecurityListRequest.ALL_INSTRMNT);
        
        CompletableFuture<SecurityList> securityListFuture = client.requestSecurityList(request);
        logRequest(request);
        SecurityList securityList = securityListFuture.get();
        logResponse(securityList);
        
        List<String> symbolsList = securityList.getSecurities().stream()
                .map(sec -> sec.getInstrument().getSymbol())
                .collect(Collectors.toList());
        
        model.setAvailableSymbols(symbolsList);
    }
    
    /**
     * Initializes the strategy manager and adds predefined strategies.
     */
    private void initializeStrategyManager() {
        strategyManager = new StrategyManager(client);
        
        // Add strategies
        StrategyParameters maParams = new StrategyParameters();
        maParams.setInstruments(Arrays.asList("KGHM"));
        maParams.setParam("shortSMAPeriod", 20);
        maParams.setParam("longSMAPeriod", 60);
        maParams.setParam("tradeSize", 10.0);
        strategyManager.addStrategy(new MovingAverageCrossoverStrategy(client), maParams);
        
        StrategyParameters rsiParams = new StrategyParameters();
        rsiParams.setInstruments(Arrays.asList("PKO"));
        rsiParams.setParam("rsiPeriod", 14);
        rsiParams.setParam("overboughtThreshold", 65.0);
        rsiParams.setParam("oversoldThreshold", 35.0);
        rsiParams.setParam("tradeSize", 10.0);
        strategyManager.addStrategy(new RSIStrategy(client), rsiParams);
        
        model.setStrategyManager(strategyManager);

    }
    
    /**
     * Subscribe to market data for a symbol.
     * 
     * @param symbol The symbol to subscribe to
     * @return true if successful, false otherwise
     */
    public boolean subscribeToMarketData(String symbol) {
        if (client == null) return false;
        
        try {
            MarketDataRequest request = new MarketDataRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setSubscriptionRequestType("1"); // Subscribe
            request.addInstrument(symbol);
            
            client.subscribeMarketData(request);
            logRequest(request);
            updateStatus("Subscribed to " + symbol);
            return true;
            
        } catch (Exception e) {
            updateStatus("Error subscribing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send an order.
     * 
     * @param order The order request to send
     * @return true if successful, false otherwise
     */
    public boolean sendOrder(OrderRequest order) {
        if (client == null) return false;
        
        try {
            client.sendOrder(order);
            logRequest(order);
            return true;
        } catch (Exception e) {
            updateStatus("Error sending order: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Shutdown the API service.
     */
    public void shutdown() {
        if (strategyManager != null) {
            strategyManager.stopAllStrategies();
        }
        
        if (client != null) {
            try {
                client.logout().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // Ignore errors during logout
            }
            client.disconnect();
        }
        
        if (server != null) {
            server.stop();
        }
    }
    
    /**
     * Set market data handler.
     */
    public void setMarketDataHandler(Consumer<MarketDataResponse> handler) {
        this.marketDataHandler = handler;
    }
    
    /**
     * Set execution report handler.
     */
    public void setExecutionReportHandler(Consumer<ExecutionReport> handler) {
        this.executionReportHandler = handler;
    }
    
    /**
     * Set status update handler.
     */
    public void setStatusUpdateHandler(Consumer<String> handler) {
        this.statusUpdateHandler = handler;
    }
    
    /**
     * Update status.
     */
    private void updateStatus(String message) {
        if (statusUpdateHandler != null) {
            statusUpdateHandler.accept(message);
        }
    }
}