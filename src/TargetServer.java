import java.io.Console;
import java.net.InetSocketAddress;

import javax.servlet.SessionTrackingMode;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.HouseKeeper;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Starts the local web server and registers the servlet routes.
 */
public class TargetServer {
    private final Console terminal;
    private final Server server;

    public TargetServer(Console terminalHandle) throws Exception {
        terminal = terminalHandle;
        server = new Server(new InetSocketAddress(AppConfig.HOST, AppConfig.PORT));

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        configureSessions(context);
        configureStaticAssets(context);
        configureServlets(context);
        context.setContextPath("/");
        server.setHandler(context);
    }

    private void configureSessions(ServletContextHandler context) throws Exception {
        SessionHandler sessions = context.getSessionHandler();
        sessions.setHttpOnly(true);
        sessions.setMaxInactiveInterval(AppConfig.SESSION_TIMEOUT_SECONDS);
        sessions.getSessionCookieConfig().setHttpOnly(true);
        sessions.getSessionCookieConfig().setName("northstar.sid");
        sessions.setSessionTrackingModes(java.util.Collections.singleton(SessionTrackingMode.COOKIE));

        SessionIdManager idManager = new DefaultSessionIdManager(server);
        HouseKeeper scavenger = new HouseKeeper();
        scavenger.setIntervalSec(AppConfig.SESSION_SCAVENGE_INTERVAL_SECONDS);
        idManager.setSessionHouseKeeper(scavenger);
        sessions.setSessionIdManager(idManager);
    }

    private void configureStaticAssets(ServletContextHandler context) {
        DefaultServlet defaultServlet = new DefaultServlet();
        ServletHolder holder = new ServletHolder("default", defaultServlet);
        holder.setInitParameter("resourceBase", "./WebContext/");
        holder.setInitParameter("dirAllowed", "false");
        holder.setInitParameter("cacheControl", "no-store, no-cache, must-revalidate");
        context.addServlet(holder, "/*");
    }

    private void configureServlets(ServletContextHandler context) {
        context.addServlet(ViewPageServlet.class, "/welcome");
        context.addServlet(AccountPageServlet.class, "/account");
        context.addServlet(TransferPageServlet.class, "/transfer");
        context.addServlet(CustomersListPageServlet.class, "/balance");
        context.addServlet(AvatarServlet.class, "/avatar");
        context.addServlet(LogoutServlet.class, "/logout");
    }

    public boolean startServer() {
        try {
            terminal.printf("Starting %s on http://%s:%d/welcome%n",
                    AppConfig.APP_NAME, AppConfig.HOST, AppConfig.PORT);
            server.start();
            terminal.printf("Type 'quit' and press enter to stop the server.%n");

            String line;
            while ((line = terminal.readLine()) != null) {
                if ("quit".equalsIgnoreCase(line.trim())) {
                    break;
                }
                terminal.printf("Type 'quit' to stop the server.%n");
            }

            server.stop();
            server.join();
            server.destroy();
            terminal.printf("Server exited.%n");
            return true;
        } catch (Exception e) {
            System.err.println("Unable to start or stop the server cleanly.");
            e.printStackTrace();
            try {
                if (server.isStarted() || server.isStarting()) {
                    server.stop();
                }
            } catch (Exception stopFailure) {
                stopFailure.printStackTrace();
            } finally {
                if (server.isStopped() || server.isFailed()) {
                    server.destroy();
                }
            }
            return false;
        }
    }

    public static void main(String[] args) {
        Console terminal = System.console();
        if (terminal == null) {
            System.err.println("Unable to get console. Run the server from a terminal session.");
            System.exit(-1);
        }

        boolean success;
        try {
            TargetServer targetServer = new TargetServer(terminal);
            success = targetServer.startServer();
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }

        if (!success) {
            System.exit(-1);
        }
    }
}
