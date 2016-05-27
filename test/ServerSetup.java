import com.wurmonline.server.Servers;
import com.wurmonline.server.utils.SimpleArgumentParser;
import javassist.Loader;
import mod.wurmonline.serverlauncher.ServerConsoleController;
import org.gotti.wurmunlimited.modloader.ModLoader;
import org.gotti.wurmunlimited.modloader.ServerHook;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.WurmArgsMod;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ServerSetup {
    ServerConsoleController controller;

    @Before
    public void setUp() {

        try {
            Loader loader = HookManager.getInstance().getLoader();
            loader.delegateLoadingOf("javafx.");
            loader.delegateLoadingOf("com.sun.");
            loader.delegateLoadingOf("org.controlsfx.");
            loader.delegateLoadingOf("impl.org.controlsfx");
            loader.delegateLoadingOf("com.mysql.");
            loader.delegateLoadingOf("org.sqlite.");
            loader.delegateLoadingOf("org.gotti.wurmunlimited.modloader.");
            loader.delegateLoadingOf("javassist.");

            Thread.currentThread().setContextClassLoader(loader);

            //loader.run("mod.wurmonline.serverlauncher.ServerMain", args);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }

        controller = new ServerConsoleController();

        HashSet<String> acceptedArgs = new HashSet<>(1);
        String ARG_START = "start";
        acceptedArgs.add(ARG_START);
        acceptedArgs.add("queryport");
        acceptedArgs.add("internalport");
        acceptedArgs.add("externalport");
        acceptedArgs.add("ip");
        acceptedArgs.add("rmiregport");
        acceptedArgs.add("rmiport");
        acceptedArgs.add("serverpassword");
        acceptedArgs.add("maxplayers");
        acceptedArgs.add("loginserver");
        acceptedArgs.add("pvp");
        acceptedArgs.add("homeserver");
        acceptedArgs.add("homekingdom");
        acceptedArgs.add("epicsettings");
        acceptedArgs.add("servername");
        acceptedArgs.add("adminpwd");

        List<WurmServerMod> mods = new ArrayList<>();
        try {
            mods = new ModLoader().loadModsFromModDir(Paths.get("mods"));
            ServerHook.createServerHook().addMods(mods);
            for (WurmServerMod mod : mods) {
                if (mod instanceof WurmArgsMod) {
                    WurmArgsMod argMod = (WurmArgsMod)mod;
                    acceptedArgs.addAll(argMod.getArgs());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        SimpleArgumentParser parser = new SimpleArgumentParser(new String[] {}, acceptedArgs);
        
        Servers.argumets = parser;
        String dbToStart = "";
        if(parser.hasOption(ARG_START)) {
            dbToStart = parser.getOptionValue(ARG_START);
            if(dbToStart == null || dbToStart.isEmpty()) {
                System.exit(1);
            }
        }

        // TODO
        //controller.setMods(mods);
    }

    @After
    public void tearDown() {
        controller.shutdown(0, null);
    }
}
