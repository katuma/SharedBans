package cz.majncraft.sharedbans
import org.xbill.DNS.*
import org.bukkit.event.*
import java.util.logging.Logger
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.*
import org.bukkit.event.*
import org.bukkit.event.player.*
  
class SharedBans extends JavaPlugin {
    def desc, ver, info, ipbls, nickbls, bls
    void onEnable() {
        desc = getDescription()
        def logger = Logger.getLogger("Minecraft")
        ver = desc.getVersion()
        info = { str -> logger.info("[SharedBans] $str") }
        getServer().getPluginManager().registerEvents(new SharedBansListener(this), this);
        info("$ver loaded.")
        ipbls = ["majncraft.ip.dnsbl.upal.se"]
        nickbls = ["majncraft.nick.dnsbl.upal.se"]
        bls = ipbls + nickbls;
    }
         
    void onDisable() {
        info("v$ver unloaded")
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
