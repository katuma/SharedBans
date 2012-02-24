package cz.majncraft.sharedbans
import org.xbill.DNS.*
import org.bukkit.event.*
import java.util.logging.Logger
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.*
import org.bukkit.event.*
import org.bukkit.event.player.*
import org.bukkit.command.*
  
class SharedBans extends JavaPlugin {
    def desc, ver, info, ipbls, nickbls, bls
    void onEnable() {
        desc = getDescription()
        def logger = Logger.getLogger("Minecraft")
        ver = desc.getVersion()
        info = { str -> logger.info("[SharedBans] $str") }
        getServer().getPluginManager().registerEvents(new SharedBansListener(this), this)
        this.getConfig().options().copyDefaults(true)
        saveConfig()
        info("$ver loaded, using ${loadbls()} DNSBLs")
    }
         
    void onDisable() {
        info("v$ver unloaded")
    }

    int loadbls() {
        def cfg = this.getConfig()
        ipbls = cfg.getStringList("ip-dnsbls")
        nickbls = cfg.getStringList("nick-dnsbls");
        bls = ipbls + nickbls
        return bls.size()
    }

    boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        def c = cmd.getName().toLowerCase()
        def spam = { msg -> sender.sendMessage("[SharedBans] $msg"); return true }
        def cfg = this.getConfig()
        if (!sender.hasPermission("sb.$c"))
                return spam("you need sb.$c permission to do that.")

        def wat=false
        switch (c) {
            case "sbreload":
                reloadConfig()
                return spam("config reloaded, using ${loadbls()} DNSBLs.")
            case "sbex":
                wat=true
            case "sbunex":
                if (args.length != 1) return false
                cfg.set("nickexempts.${args[0].toLowerCase()}",wat);
                saveConfig()
                return spam("nick exempt for '${args[0]}' ${wat?"added":"removed"}.")
        }
    }
}


class SharedBansListener implements Listener {
    def plugin
    SharedBansListener(p) {
        plugin = p
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent e) {
        def p = e.getPlayer()
        def ip = p.getAddress().getAddress().getHostAddress()
        def name = p.getName()
        if (plugin.getConfig().get("nickexempts.${name.toLowerCase()}")) return;
        Thread.start {
            for (bl in plugin.bls) {
                def str = ((bl in plugin.ipbls)?ip:name)+"."+bl
                def l = new Lookup(str, Type.TXT)
                l.setResolver(new SimpleResolver())
                try {
                    p.kickPlayer("[${bl.split(".")[0]}] "+l.run()[0].toString())
                    return
                } catch (ex) {
                }
            }
        }
    }
}
