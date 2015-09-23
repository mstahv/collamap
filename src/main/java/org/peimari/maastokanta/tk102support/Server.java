package org.peimari.maastokanta.tk102support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.peimari.maastokanta.backend.LocationRepository;
import org.peimari.maastokanta.domain.DeviceMapping;
import org.peimari.maastokanta.domain.Location;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.addon.leaflet.util.JTSUtil;

/**
 * Created by se on 19/06/14.
 */
@Service
public class Server {

    @Autowired
    LocationRepository repo;

    private final int portNumber;
    private boolean running = false;

    public Server() {
        this(51234);
    }

    public Server(int portNumber) {
        this.portNumber = portNumber;

    }

    public boolean isRunning() {
        return running;
    }

    @PostConstruct
    public void start() {

        running = true;

        Logger.getLogger(Server.class.getName()).
                log(Level.INFO, "Starting TCP  server to listen TK102 devices");
        new Thread() {

            @Override
            public void run() {
                ServerSocket serverSocket;
                try {
                    serverSocket = new ServerSocket(portNumber);
                    while (isRunning()) {
                        new ServerThread(Server.this, serverSocket.accept()).
                                start();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    @PreDestroy
    public void stop() {
        this.running = false;
    }

    static public void main(String[] args) {
        new Server().start();

    }

    void persist(Update update) {

        Logger.getLogger(Server.class.getName()).
                log(Level.INFO, "Received update " + update.toString());

        DeviceMapping mapping = repo.getDeviceMapping(update.getImei());
        if (mapping != null) {
            Location location = new Location(mapping.getName(), JTSUtil.toPoint(
                    new Point(update.getLat(), update.getLon())), Instant.now(),
                    40);
            repo.saveLocationWithTail(mapping.getGroup(), location);
            Logger.getLogger(Server.class.getName()).
                    log(Level.INFO, "Saved location!");
        }
    }

}
