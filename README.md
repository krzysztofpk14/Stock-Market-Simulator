# Stock Market Simulator

A modular Java trading application with a client-server architecture for financial market data and trading simulation. The Client and Server communication are inspired and include several components from the [BOS S.A. API](https://bossa.pl/sites/b30/files/2021-04/document/Podrecznik_bossaAPI.pdf). The application includes a graphical user interface and implements various trading strategies.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Main Components](#main-components)
3. [Getting Started](#getting-started)
4. [Development](#development)
5. [Testing](#testing)
6. [Dependencies](#dependencies)

## Architecture Overview

This application is built with a modular architecture consisting of the following main components:

- **BossaApiClient**: Client library for communicating with the BossaAPI
- **BossaApiServer**: Server implementation that simulates the BossaAPI for development and testing
- **TradingAppGUI**: JavaFX-based user interface for interacting with the application
- **Trading Strategies**: Implementations of various algorithmic trading strategies

The application follows an event-driven design where components communicate through asynchronous messages and callbacks.

## Main Components

### BossaApiClient

The BossaApiClient serves as the communication layer between the trading application and the Bossa API server. It handles:

- Authentication and session management
- Market data requests and subscriptions
- Order submission and management
- Asynchronous response handling using CompletableFuture

Example usage:
```java
BossaApiClient client = new BossaApiClient();
client.connect("localhost", 24444)
    .thenCompose(connected -> client.login("username", "password"))
    .thenAccept(loginResponse -> {
        System.out.println("Login successful: " + loginResponse.isSuccess());
    })
    .exceptionally(ex -> {
        System.err.println("Error: " + ex.getMessage());
        return null;
    });
```

### BossaApiServer

The BossaApiServer is a simulation server that implements the Bossa API protocol for development and testing purposes. Key features:

- Socket-based communication with multiple client connections
- Simulated order execution
- Market data generation
- Session management

The server consists of several managers:
- **SessionManager**: Handles client connections and authentication
- **OrderManager**: Processes and executes trading orders
- **MarketDataManager**: Generates and distributes market data
- **SecurityManager**: Manages available securities and their details

### TradingAppGUI

The GUI application provides a user-friendly interface for:

- Viewing market data in real-time
- Placing and monitoring orders
- Configuring and running trading strategies
- Visualizing price charts and technical indicators

The UI is organized into tabs:
- Market Data
- Orders
- Charts
- Strategies
- Debug/Logs

### Trading Strategies

The application includes several trading strategies that can be configured and executed:

1. **RSI Strategy**: Makes trading decisions based on the Relative Strength Index (RSI)
   - Buys when RSI is below the oversold threshold
   - Sells when RSI is above the overbought threshold

2. **MovingAverageCrossover Strategy**: Makes trading decisions based on moving average crossovers
   - Buys when the fast moving average crosses above the slow moving average
   - Sells when the fast moving average crosses below the slow moving average

All strategies are managed by the **StrategyManager**, which handles:
- Strategy initialization and parameter configuration
- Market data and execution report distribution to strategies
- Strategy execution and monitoring

## Getting Started

### Prerequisites

- Java 24 or newer
- Maven 3.8 or newer

### Running the Application

1. Clone the repository
2. Build the project using Maven:
   ```
   mvn clean install
   ```
3. Run the GUI application:
   ```
   mvn exec:java
   ```

## Development

### Project Structure

```
src/
  main/
    java/
      com/
        krzysztofpk14/
          app/
            bossaapi/
              client/     # Client API components
              model/      # Data model classes
              server/     # Server components
              util/       # Utility classes
            gui/          # GUI components
            strategy/     # Trading strategies
    resources/
      samples/            # Sample data files
      xsd/                # XML Schema Definition files
  test/
    java/                 # Test classes
```

## Testing

The project includes comprehensive unit tests for core components. Run the tests with:

```
mvn test
```

Test coverage includes:
- Client/server communication
- Order processing
- Market data handling
- Strategy execution
- GUI controllers

## Dependencies

- **JavaFX**: User interface framework
- **JAXB**: XML processing
- **Jackson**: JSON processing
- **JUnit 5**: Testing framework
- **FIX Protocol**: Financial Information eXchange protocol implementation

## License

This project is licensed under the MIT License