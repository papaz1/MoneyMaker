package se.moneymaker.serviceprovider;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DebugHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import se.moneymaker.db.DBServices;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;

public class ServiceProvider {

    private static final String CLASSNAME = "BESServiceProvider";
    private Server server;
    private static final int PORT = 8077;
    private DBServices services;

    public ServiceProvider() {
        final String METHOD = "BESServiceProvider";
        try {
            services = new DBServices(true);
            server = new Server(PORT);
            RolloverFileOutputStream outputStream = new RolloverFileOutputStream("C:\\Users\\Baran.Solen\\Documents\\Dev\\MoneyMaker2\\baran_jetty.txt", true, 10);

            DebugHandler debugHandler = new DebugHandler();
            debugHandler.setOutputStream(outputStream);
            debugHandler.setHandler(server.getHandler());
            server.setHandler(debugHandler);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/services");
            context.addServlet(new ServletHolder(new Login()), "/login/*");
            context.addServlet(new ServletHolder(new PlaceBet(services)), "/placeBet/*");
            context.addServlet(new ServletHolder(new ReadPrice(services)), "/readPrice/*");

            server.setHandler(context);
            server.setStopAtShutdown(true);
            Log.logMessage(CLASSNAME, METHOD, "Serviceprovider running...", LogLevelEnum.INFO, true);
        } catch (Exception e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        }
    }

    public void start() {
        final String METHOD = "start";
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        }
    }
}
