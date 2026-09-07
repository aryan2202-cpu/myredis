# MyRedis — A Redis Clone in Java

A from-scratch Redis server implementation in plain Java, built to understand networking, the RESP protocol, and in-memory data structures.

## Features
- Custom TCP server (multi-threaded, one thread per client)
- RESP (REdis Serialization Protocol) parser and encoder — compatible with real Redis clients
- **Strings**: SET (with EX/PX expiry), GET, DEL, EXISTS
- **TTL**: EXPIRE, TTL
- **Lists**: LPUSH, RPUSH, LPOP, RPOP, LLEN, LRANGE
- **Hashes**: HSET, HGET, HDEL, HEXISTS, HGETALL, HLEN
- **Sets**: SADD, SREM, SISMEMBER, SMEMBERS, SCARD
- **Persistence**: SAVE command + automatic load on startup / save on shutdown

## Run it
```bash
javac -d out src/main/java/myredis/*.java
java -cp out myredis.Main
```
Server starts on port 6379, same as real Redis — connect with `redis-cli -p 6379` or any RESP client.

## Architecture
- `Main.java` — TCP server, accepts connections, spawns a thread per client
- `RespParser.java` — parses incoming RESP-encoded commands
- `RespWriter.java` — encodes responses back into RESP
- `CommandDispatcher.java` — routes parsed commands to store operations
- `Store.java` — in-memory data store (strings, lists, hashes, sets) + persistence
