package my;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
	private static final int DEFALUT_PORT = 1234;

	public static void main(String[] args) throws Exception {
		int port = getPort();
		String service = getWarFileName();
		if (service == null)
			return;

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

	private static int getPort() {
		int ret = DEFALUT_PORT;
		String portString = System.getProperty("port");
		if (portString != null) {
			try {
				ret = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				ret = DEFALUT_PORT;
			}
		}
		return ret;
	}

	private static String getWarFileName() throws IOException {
		ArrayList<String> ret = new ArrayList<String>();
		ProtectionDomain domain = Main.class.getProtectionDomain();
		URL jarLocation = domain.getCodeSource().getLocation();
		for (Enumeration<JarEntry> e = new JarFile(jarLocation.getPath()).entries(); e.hasMoreElements();) {
			String entryName = e.nextElement().getName();
			if (entryName.endsWith(".war")) {
				ret.add(entryName.substring(0, entryName.length() - 4));
			}
		}
		return ret.get(0);
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