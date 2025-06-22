package com.krzysztofpk14.app.bossaapi.client;

import static org.junit.jupiter.api.Assertions.*;

import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BossaApiClientTest {
    
    private BossaApiClient client;
    
    @BeforeEach
    void setUp() {
        client = new BossaApiClient();
    }
    
    @Test
    void testConstructor() {
        // Then
        assertNotNull(client);
        assertFalse(client.isLoggedIn());
    }
    
    @Test
    void testSendOrderRequest() throws Exception {
        // Given
        OrderRequest orderRequest = createTestOrderRequest();
        
        // When/Then
        CompletableFuture<ExecutionReport> future = client.sendOrder(orderRequest);
        assertNotNull(future);
    }

    @Test
    void testSubscribeToMarketData() throws Exception {
        // Given
        MarketDataRequest request = createTestMarketDataRequest();
        
        // When/Then
        CompletableFuture<MarketDataResponse> future = client.subscribeMarketData(request);
        assertNotNull(future);
    }
    
    @Test
    void testUnsubscribeFromMarketData() throws Exception {
        // Given
        MarketDataRequest request = createTestMarketDataRequest();
        
        // When/Then
        CompletableFuture<MarketDataResponse> future = client.unsubscribeMarketData(request);
        assertNotNull(future);
    }
    
    @Test
    void testLogin() throws Exception {
        // Given
        String username = "testuser";
        String password = "testpass";
        
        // When/Then
        assertThrows(IOException.class, () -> {
            // Attempt to login without setting the connection
            client.loginAsync(username, password);
        });
    }
    
    @Test
    void testLogout() throws Exception {
        // When/Then
        CompletableFuture<UserResponse> future = client.logout();
        assertNotNull(future);
    }
    
    // Helper methods
    private OrderRequest createTestOrderRequest() {
        OrderRequest order = new OrderRequest();
        order.setClientOrderId("CL-" + UUID.randomUUID().toString().substring(0, 8));
        order.setSide(OrderRequest.BUY);
        order.setOrderType(OrderRequest.LIMIT);
        order.setPrice("150.00");
        order.setTimeInForce(OrderRequest.DAY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol("AAPL");
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity("10");
        order.setOrderQuantity(orderQty);
        
        return order;
    }
    
    private MarketDataRequest createTestMarketDataRequest() {
        MarketDataRequest request = new MarketDataRequest();
        request.setRequestId("MD-" + UUID.randomUUID().toString().substring(0, 8));
        request.setSubscriptionRequestType(MarketDataRequest.SNAPSHOT);
        request.setMarketDepth("0");
        request.addInstrument("AAPL");
        return request;
    }
}
