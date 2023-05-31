package discord.exception;

public class MemoryException extends Exception {
    public static final long MAX_MEMORY_USAGE = 512 * 1024 * 32; // 16 MB

    public void checkMemoryUsage(Runtime runtime) {
        if (runtime.totalMemory() - runtime.freeMemory() > MAX_MEMORY_USAGE)
            throw new OutOfMemoryError("Memory usage exceeded " + MAX_MEMORY_USAGE + " bytes | " + runtime.totalMemory());
    }
}
