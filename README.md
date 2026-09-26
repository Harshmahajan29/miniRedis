# MiniRedis — Redis-Inspired In-Memory Database

MiniRedis is a Redis-inspired in-memory database server built from scratch in Java. The project focuses on understanding how database servers work internally, including TCP networking, command parsing, concurrent client handling, in-memory storage, expiration, and persistence.

The core server is implemented using plain Java without Redis libraries or Spring Boot.

## Features

* TCP-based client-server communication
* Concurrent handling of multiple client connections
* Command parsing and execution
* In-memory key-value storage
* String data type support
* List, Set, and Hash data structures
* Key expiration and TTL support
* Append-Only File (AOF) persistence
* Crash recovery through command replay
* Transaction support
* Publish/Subscribe messaging
* Thread-safe data access
* Basic server metrics and monitoring
* Authentication and configuration support
* Memory management and eviction
* Experimental primary-replica replication

> Features are implemented incrementally as part of the project's development roadmap.

## Architecture

```text
                    +-------------------+
                    |     MiniRedis     |
                    |   Application     |
                    +---------+---------+
                              |
                              v
                    +-------------------+
                    |   RedisServer     |
                    |  TCP Server       |
                    +---------+---------+
                              |
                    +---------+---------+
                    |                   |
                    v                   v
          +----------------+   +----------------+
          | ClientConnection|   | ClientConnection|
          +-------+--------+   +-------+--------+
                  |                    |
                  +---------+----------+
                            |
                            v
                   +------------------+
                   |  Request Parser  |
                   +--------+---------+
                            |
                            v
                   +------------------+
                   | Command Executor |
                   +--------+---------+
                            |
                            v
                   +------------------+
                   |     Storage      |
                   +--------+---------+
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
        StringStore    ListStore      SetStore
                            |
                            v
                       HashStore

        Persistence / Recovery / TTL / Transactions
                    operate around storage
```

## Tech Stack

* **Language:** Java
* **Build Tool:** Maven
* **Networking:** Java `Socket` / `ServerSocket`
* **Concurrency:** Java Threads / ExecutorService
* **Storage:** Java Collections / Concurrent Collections
* **Persistence:** Java File I/O
* **Testing:** JUnit
* **Version Control:** Git

## Project Structure

```text
miniredis/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── redis/
    │               ├── MiniRedis.java
    │               ├── RedisServer.java
    │               ├── ClientConnection.java
    │               ├── RequestParser.java
    │               ├── ResponseWriter.java
    │               ├── Command.java
    │               ├── CommandParser.java
    │               ├── CommandExecutor.java
    │               │
    │               ├── storage/
    │               │   ├── Storage.java
    │               │   ├── StringStore.java
    │               │   ├── ListStore.java
    │               │   ├── SetStore.java
    │               │   └── HashStore.java
    │               │
    │               └── persistence/
    │                   ├── AofManager.java
    │                   └── RecoveryManager.java
    │
    └── test/
        └── java/
```

The project follows a layered design where networking, command processing, storage, and persistence are kept separate.

## How It Works

### 1. Client Connection

MiniRedis starts a TCP server and listens for incoming client connections.

```text
Client
   |
   | TCP Connection
   v
RedisServer
   |
   v
ClientConnection
```

Each connected client is handled independently so multiple clients can communicate with the server concurrently.

### 2. Command Processing

A client sends a command to the server.

For example:

```text
SET name Harsh
```

The request passes through:

```text
Raw Request
     |
     v
RequestParser
     |
     v
Command
     |
     v
CommandExecutor
     |
     v
Storage
```

The executor identifies the command and performs the required operation on the appropriate data structure.

### 3. In-Memory Storage

MiniRedis stores data in memory for fast access.

Example:

```text
SET name Harsh
```

Results in a logical structure similar to:

```text
name -> Harsh
```

Supported data structures are planned to include:

```text
String
List
Set
Hash
```

## Supported Commands

The command set is being implemented incrementally.

### String Commands

```text
SET key value
GET key
DEL key
EXISTS key
INCR key
```

### List Commands

```text
LPUSH key value
RPUSH key value
LPOP key
RPOP key
LRANGE key start stop
```

### Set Commands

```text
SADD key value
SREM key value
SISMEMBER key value
SMEMBERS key
```

### Hash Commands

```text
HSET key field value
HGET key field
HDEL key field
HGETALL key
```

### Expiration

```text
EXPIRE key seconds
TTL key
```

Additional commands will be added as the project evolves.

## Concurrency

MiniRedis is designed to support multiple clients simultaneously.

Each client connection is handled independently:

```text
                RedisServer
                    |
          +---------+---------+
          |         |         |
          v         v         v
       Client 1  Client 2  Client 3
        Thread    Thread    Thread
```

Thread-safe collections and appropriate synchronization mechanisms are used where shared state is accessed concurrently.

## Persistence

The server is designed to support Append-Only File persistence.

Instead of storing only the current state, write operations can be recorded as commands:

```text
SET name Harsh
SET age 20
DEL age
```

These commands can later be replayed to reconstruct the database state.

```text
             AOF File
                |
                v
        RecoveryManager
                |
                v
          Command Replay
                |
                v
          In-Memory Store
```

This allows MiniRedis to recover data after a server restart.

## TTL and Expiration

Keys can have an expiration time.

Example:

```text
SET session abc123
EXPIRE session 60
```

The key becomes invalid after the specified duration.

Expiration management is separated from the storage layer so that time-based behavior does not become tightly coupled with individual data structures.

## Transactions

MiniRedis is planned to support transaction-style command execution:

```text
MULTI
SET name Harsh
SET age 20
EXEC
```

The transaction manager will collect commands and execute them as a group.

## Publish / Subscribe

A publish-subscribe system is planned for communication between connected clients.

Example:

```text
SUBSCRIBE notifications
```

Another client can publish:

```text
PUBLISH notifications "Server started"
```

Subscribers can receive messages published to the corresponding channel.

## Project Roadmap

### Phase 1 — Networking

* [x] Create Maven project
* [x] Create application entry point
* [x] Create TCP server
* [ ] Accept client connections
* [ ] Implement client communication

### Phase 2 — Command System

* [ ] Request parser
* [ ] Command representation
* [ ] Command parser
* [ ] Command executor
* [ ] Response handling
* [ ] Basic commands

### Phase 3 — Data Structures

* [ ] String storage
* [ ] List storage
* [ ] Set storage
* [ ] Hash storage

### Phase 4 — Expiration

* [ ] `EXPIRE`
* [ ] `TTL`
* [ ] Expiration manager
* [ ] Automatic cleanup

### Phase 5 — Concurrency

* [ ] Multiple client support
* [ ] Thread-safe storage
* [ ] Executor-based connection handling
* [ ] Concurrent access testing

### Phase 6 — Persistence

* [ ] AOF manager
* [ ] Command logging
* [ ] Recovery manager
* [ ] Restart recovery testing

### Phase 7 — Advanced Features

* [ ] Transactions
* [ ] Pub/Sub
* [ ] Memory limits
* [ ] Eviction policies
* [ ] Metrics
* [ ] Authentication
* [ ] Configuration file
* [ ] Primary-replica replication

## Running the Project

### Prerequisites

* Java 17 or higher
* Maven 3.8+
* Git

### Clone the Repository

```bash
git clone <repository-url>
cd miniredis
```

### Build

```bash
mvn clean package
```

### Run

```bash
mvn exec:java
```

Or run the `MiniRedis` class directly from your IDE.

The server will start on the configured port.

Example:

```text
MiniRedis is starting on port 5000
```

## Testing

Run the test suite using:

```bash
mvn test
```

Tests will cover command parsing, storage operations, expiration, concurrency, and persistence as the respective features are implemented.

## Design Goals

MiniRedis is primarily an educational systems project designed to understand the internal components involved in building a database server.

The main goals are:

* Understand TCP client-server architecture
* Implement a command-driven server
* Learn concurrent programming in Java
* Design thread-safe in-memory storage
* Understand database persistence
* Implement crash recovery
* Work with low-level Java networking and file I/O
* Build a non-trivial system without relying on an existing database engine

## Future Improvements

Potential future improvements include:

* RESP-compatible protocol support
* Improved command validation
* Connection pooling
* More advanced eviction policies
* Memory usage tracking
* Server benchmarking
* Authentication and access control
* Replication
* Administrative monitoring API
* Performance optimization

## License

This project is licensed under the MIT License.

## Author

**Harsh Mahajan**

B.Tech Information Technology
Walchand College of Engineering, Sangli
