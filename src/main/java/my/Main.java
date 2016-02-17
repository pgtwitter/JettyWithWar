package my;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class Main {
	public static void main(String[] args) throws Exception {
		String service = "HelloJersey";
		int port = 1234;

		Server server = new Server(port);
		try {
			server.setHandler(getWebAppContext(service));
			server.start();
			openBrowser(service, port);
			server.join();
		} finally {
			server.destroy();
		}
	}

	private static WebAppContext getWebAppContext(String service) throws IOException {
		WebAppContext war = new WebAppContext();
		war.setContextPath("/" + service);
		war.setWar(pickoutWarFile(service));
		Configuration[] configurations = { new AnnotationConfiguration(), new WebInfConfiguration(),
				new WebXmlConfiguration(), new MetaInfConfiguration(), new FragmentConfiguration(),
				new EnvConfiguration(), new PlusConfiguration(), new JettyWebXmlConfiguration() };
		war.setConfigurations(configurations);
		return war;
	}

	private static void openBrowser(String service, int port) throws IOException {
		String OS_NAME = System.getProperty("os.name").toLowerCase();
		String cmd = "xdg-open";
		if (OS_NAME.startsWith("windows")) {
			cmd = "cmd /c start";
		} else if (OS_NAME.startsWith("mac")) {
			cmd = "open";
		}
		Runtime.getRuntime().exec(cmd + " http://localhost:" + port + "/" + service + "/");
	}

	private static String pickoutWarFile(String service) throws IOException {
		InputStream stream = Main.class.getClassLoader().getResourceAsStream(service + ".war");
		Path tmpWebappsPath = Files.createTempDirectory("webapps");
		Path tmpWarPath = Paths.get(tmpWebappsPath.toString(), service + ".war");
		tmpWarPath.toFile().deleteOnExit();
		if (stream != null) {
			Files.copy(stream, tmpWarPath);
		}
		return tmpWarPath.toString();
	}
}