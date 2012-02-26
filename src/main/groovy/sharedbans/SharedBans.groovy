package cz.majncraft.sharedbans
import org.xbill.DNS.*
import org.bukkit.event.*
import java.util.logging.Logger
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.*
import org.bukkit.event.*
import org.bukkit.event.player.*
import org.bukkit.command.*
import org.bukkit.configuration.*
  
class SharedBans extends JavaPlugin {
    def desc, ver, info, ipbls, nickbls, bls, rbls

    void onEnable() {
        desc = getDescription()
        def logger = Logger.getLogger("Minecraft")
        ver = desc.getVersion()
        info = { str -> logger.info("[SharedBans] $str") }
        getServer().getPluginManager().registerEvents(new SharedBansListener(this), this)
        getConfig().options().copyDefaults(true)
        saveConfig()
        info("$ver loaded, using ${loadbls()} shared DNSBLs")
    }
         
    void onDisable() {
        info("v$ver unloaded")
    }

    // load blacklists from config
    int loadbls() {
        def cfg = getConfig()
        rbls = cfg.get("drone-dnsbls")
        ipbls = cfg.get("ip-dnsbls")
        nickbls = cfg.get("nick-dnsbls");
        bls = ipbls + nickbls
        return bls.size()
    }

    // plugin commands
    boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        def c = cmd.getName().toLowerCase()
        def spam = { msg -> sender.sendMessage("[SharedBans] $msg"); return true }
        def cfg = getConfig()
        if (!sender.hasPermission("sb.$c"))
                return spam("you need sb.$c permission to do that.")

        def wat=false
        switch (c) {
            case "sbreload":
                reloadConfig()
                return spam("config reloaded, using ${loadbls()} shared DNSBLs.")
            case "sbex":
                wat=true
            case "sbunex":
                if (args.length != 1) return false
                def a=args[0];
                cfg.set("nickexempts.${a.toLowerCase()}",wat);
                saveConfig()
                return spam("nick exempt for '${a}' ${wat?"added":"removed"}.")
            case "sbcheck":
                if (args.length != 1) return false
                def a=args[0];
                spam("Looking up if nick or ip '${a}' is banned, please wait...")
                check_nameip(a.contains(".")?null:a,a.contains(".")?a:null,
                    { msg -> spam("Nick/IP '$a' is banned for: $msg") }, spam )
        }
        return true
    }

    // check that given nick or ip is banned. returns true if exempt,
    // otherwise invokes
    void check_nameip(name,ip,lambdaban,lambdaok) {
        if (name && getConfig().get("nickexempts.${name.toLowerCase()}"))
            lambdaok("Nick ${name} is exempted")
        else Thread.start {
            // custom nick/ip bans DNSBL lookup
            for (bl in bls) {
                def str = ((bl in ipbls)?ip:name)
                if (!str) continue;
                str += "."+bl
                lambdaok("Looking up TXT $str")
                def l = new Lookup(str, Type.TXT)
                l.setResolver(new SimpleResolver())
                try {
                    def res = l.run()[0].getStrings()[0];
                    if (res != "") {
                        lambdaban(res)
                        return
                    }
                } catch (ex) {
                }
            }
            // traditional drone BLS lookup.
            // please excuse every()/find() retardation, its because of config format
            // fix this if you have better idea!
            if (ip) for (bl in rbls) if (!bl.every { entry ->
                def str = ip.split("\\.").reverse().join(".")+"."+entry.key
                lambdaok("Looking up A $str")
                def l = new Lookup(str, Type.A)
                l.setResolver(new SimpleResolver())
                def resip=null
                try {
                    resip = l.run()[0].getAddress().getHostAddress()
                } catch (ex) { }
                return !(entry.value.find{it.find{it.key==resip && it.value.find {lambdaban(it.value)}}})
            }) return;
            lambdaok("${name?"Nick '" + name:"IP '"+ip}' is not banned.")
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
        plugin.check_nameip(name,ip,{ msg -> p.kickPlayer(msg); plugin.info("Kicked $name@$ip for '$msg'"); return true }, { msg -> });
    }
}
