package com.jarvis.deploy.config;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Watches a config file or directory for changes and notifies registered listeners.
 */
public class ConfigWatcher implements AutoCloseable {

    private final Path watchPath;
    private final Consumer<Path> onChange;
    private final ExecutorService executor;
    private volatile boolean running;
    private WatchService watchService;

    public ConfigWatcher(Path watchPath, Consumer<Path> onChange) {
        this.watchPath = watchPath;
        this.onChange = onChange;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "config-watcher");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() throws IOException {
        Path dirToWatch = Files.isDirectory(watchPath) ? watchPath : watchPath.getParent();
        watchService = FileSystems.getDefault().newWatchService();
        dirToWatch.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE);
        running = true;
        executor.submit(this::pollLoop);
    }

    private void pollLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                Thread.currentThread().interrupt();
                break;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) continue;
                @SuppressWarnings("unchecked")
                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                Path changed = watchPath.getParent() != null
                        ? watchPath.getParent().resolve(pathEvent.context())
                        : pathEvent.context();
                if (Files.isDirectory(watchPath) || changed.equals(watchPath)) {
                    onChange.accept(changed);
                }
            }
            if (!key.reset()) break;
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void close() throws IOException {
        running = false;
        if (watchService != null) {
            watchService.close();
        }
        executor.shutdownNow();
    }
}
