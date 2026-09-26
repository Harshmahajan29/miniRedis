package com.command;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.Expiration.*;
import com.storage.*;
import com.redis.*;

public class CommandExecutor {
    private final Storage storage;
    private final AofManager aofManager;

    public CommandExecutor(Storage storage, AofManager aofManager) {
        this.storage = storage;
        this.aofManager = aofManager;
    }

    // Public method used by live TCP client connections
    public String execute(Command command) {
        String result = executeInternal(command);

        // Append mutating commands to AOF after successful execution
        if (aofManager != null && isMutatingCommand(command.getName()) && !result.startsWith("ERR")) {
            aofManager.append(command.getRawInput());
        }

        return result;
    }

    // Internal execution method used during recovery (bypasses double-writing to AOF)
    public String executeInternal(Command command) {
        if (command == null) {
            return "ERR empty command";
        }

        ExpirationManager expiryManager = storage.getExpirationManager();
        List<String> args = command.getArgs();

        switch (command.getName()) {
            case "PING":
                return !args.isEmpty() ? args.get(0) : "PONG";

            case "SET":
                if (args.size() < 2) return "ERR wrong number of arguments for 'set' command";
                expiryManager.remove(args.get(0));
                return storage.getStringStore().set(args.get(0), args.get(1));

            case "GET":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'get' command";
                String val = storage.getStringStore().get(args.get(0), expiryManager, storage);
                return val != null ? val : "(nil)";

            case "DEL":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'del' command";
                boolean deleted = storage.getStringStore().del(args.get(0), expiryManager);
                return deleted ? "(integer) 1" : "(integer) 0";

            case "EXISTS":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'exists' command";
                boolean exists = storage.getStringStore().exists(args.get(0), expiryManager, storage);
                return exists ? "(integer) 1" : "(integer) 0";

            case "INCR":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'incr' command";
                try {
                    Long newVal = storage.getStringStore().incr(args.get(0), expiryManager, storage);
                    return "(integer) " + newVal;
                } catch (NumberFormatException e) {
                    return "ERR value is not an integer or out of range";
                }

            case "EXPIRE":
                if (args.size() < 2) return "ERR wrong number of arguments for 'expire' command";
                try {
                    long ttlSeconds = Long.parseLong(args.get(1));
                    if (!storage.getStringStore().exists(args.get(0), expiryManager, storage)) {
                        return "(integer) 0";
                    }
                    expiryManager.setTTL(args.get(0), ttlSeconds, storage);
                    return "(integer) 1";
                } catch (NumberFormatException e) {
                    return "ERR value is not an integer or out of range";
                }

            case "TTL":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'ttl' command";
                if (!storage.getStringStore().exists(args.get(0), expiryManager, storage)) {
                    return "(integer) -2";
                }
                return "(integer) " + expiryManager.getTTLSeconds(args.get(0));

            case "LPUSH":
                if (args.size() < 2) return "ERR wrong number of arguments for 'lpush' command";
                return "(integer) " + storage.getListStore().lpush(args.get(0), args.subList(1, args.size()));

            case "RPUSH":
                if (args.size() < 2) return "ERR wrong number of arguments for 'rpush' command";
                return "(integer) " + storage.getListStore().rpush(args.get(0), args.subList(1, args.size()));

            case "LPOP":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'lpop' command";
                String lpopVal = storage.getListStore().lpop(args.get(0));
                return lpopVal != null ? lpopVal : "(nil)";

            case "RPOP":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'rpop' command";
                String rpopVal = storage.getListStore().rpop(args.get(0));
                return rpopVal != null ? rpopVal : "(nil)";

            case "LRANGE":
                if (args.size() < 3) return "ERR wrong number of arguments for 'lrange' command";
                try {
                    int start = Integer.parseInt(args.get(1));
                    int stop = Integer.parseInt(args.get(2));
                    List<String> range = storage.getListStore().lrange(args.get(0), start, stop);
                    if (range.isEmpty()) return "(empty list or set)";
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < range.size(); i++) {
                        sb.append(i + 1).append(") \"").append(range.get(i)).append("\"\n");
                    }
                    return sb.toString().trim();
                } catch (NumberFormatException e) {
                    return "ERR value is not an integer or out of range";
                }

            case "SADD":
                if (args.size() < 2) return "ERR wrong number of arguments for 'sadd' command";
                return "(integer) " + storage.getSetStore().sadd(args.get(0), args.subList(1, args.size()));

            case "SREM":
                if (args.size() < 2) return "ERR wrong number of arguments for 'srem' command";
                return "(integer) " + storage.getSetStore().srem(args.get(0), args.subList(1, args.size()));

            case "SISMEMBER":
                if (args.size() < 2) return "ERR wrong number of arguments for 'sismember' command";
                return storage.getSetStore().sismember(args.get(0), args.get(1)) ? "(integer) 1" : "(integer) 0";

            case "SMEMBERS":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'smembers' command";
                Set<String> members = storage.getSetStore().smembers(args.get(0));
                if (members.isEmpty()) return "(empty list or set)";
                StringBuilder smb = new StringBuilder();
                int idx = 1;
                for (String member : members) {
                    smb.append(idx++).append(") \"").append(member).append("\"\n");
                }
                return smb.toString().trim();

            case "HSET":
                if (args.size() < 3) return "ERR wrong number of arguments for 'hset' command";
                return "(integer) " + storage.getHashStore().hset(args.get(0), args.get(1), args.get(2));

            case "HGET":
                if (args.size() < 2) return "ERR wrong number of arguments for 'hget' command";
                String hval = storage.getHashStore().hget(args.get(0), args.get(1));
                return hval != null ? hval : "(nil)";

            case "HDEL":
                if (args.size() < 2) return "ERR wrong number of arguments for 'hdel' command";
                return "(integer) " + storage.getHashStore().hdel(args.get(0), args.get(1));

            case "HGETALL":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'hgetall' command";
                Map<String, String> entries = storage.getHashStore().hgetall(args.get(0));
                if (entries.isEmpty()) return "(empty list or set)";
                StringBuilder hsb = new StringBuilder();
                int hidx = 1;
                for (Map.Entry<String, String> entry : entries.entrySet()) {
                    hsb.append(hidx++).append(") \"").append(entry.getKey()).append("\"\n");
                    hsb.append(hidx++).append(") \"").append(entry.getValue()).append("\"\n");
                }
                return hsb.toString().trim();

            default:
                return "ERR unknown command '" + command.getName() + "'";
        }
    }

    private boolean isMutatingCommand(String cmd) {
        return switch (cmd) {
            case "SET", "DEL", "INCR", "EXPIRE", "LPUSH", "RPUSH", "LPOP", "RPOP", "SADD", "SREM", "HSET", "HDEL" -> true;
            default -> false;
        };
    }
}