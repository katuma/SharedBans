SharedBans
==========

A complete P2P ban sharing solution for minecraft servers.
Supports nicknames, ip bans and generic anti-spam DNS blacklists.

This plugin operates completely decentralised without central party,
depending on server-to-server trust only.

DNS distribution is robust and scalable, compared to custom
protocols or http.

Plugin (ban query client) onfiguration
--------------------------------------

```yaml
##############
# generic drone blacklists returning A records, careful with this!
##############
drone-bls:
- rbl.efnetrbl.org:
  - "127.0.0.1": "Open proxy!"
  - "127.0.0.4": "Don't use TOR to play minecraft!"
  - "127.0.0.5": "Drones, hacked PC or home router!"
# add some more drone BLs if you have some...
# You MUST list every possible reply with ban reason, otherwise the client will get through.

###########################
# custom TXT based blacklists
###########################

# ip-based bans (FigAdmin etc). returns TXT with ban reason.
# ips are in non-reversed format, contrary to tradional drone DNSBLs!
ip-dnsbls:
- majncraft.ip.dnsbl.upal.se

# nick-based bans (custom lists, mcbans gateways, FigAdmin, etc) returns TXT with ban reason.
nick-dnsbls:
# random shared database, uses PowerDNS
- amunak.dnsbl.upal.se
# some random server (uses local FigAdmin & PowerDNS)
- majncraft.nick.dnsbl.upal.se
# dont let anyone with rep less than 5 in! (uses mcbans.py & PowerDNS)
- mcbans.5.dnsbl.upal.se
- some.other.dnsbl.mc.server.com
```

Game commands
-------------
* /sbreload
  Reloads SharedBans config
* /sbex <nick>
  Gives exemption from bans to nick, "Unbans" given nick locally (ie remote banlists are ignored)
* /sbunex <nick>
  Removes aforementioned exemption.

Permission nodes
----------------
* sb.sbreload
* sb.sbex
* sb.sbunex

Server configuration using MySQL
--------------------------------
Setting up your own public banlist is not trivial, so it is recommended only for experienced admins!

DNS records are populated via PowerDNS and gmysql module with custom select queries.
Only gmysql-any-query is necessary, other MUST return empty result set. PDNS provides
ban query in the '%1$s' parameter, you should return:
* content: complete ban reason
* ttl: how long to cache this information (should be 3600 or more if your banlist contains only long-lived bans)
* name: should just return '%1$s' parameter
* domain_id: an unique id within your DNSBL
* type: must be always 'TXT'

When any of these is missing, powerdns will just crash! Use /etc/init.d/pdns monitor to figure out correct queries.

If you're unsure about this, just use FigAdmin queries provided in the example config.


To test your DNS server, use the following (with banned nickname in front, rest is your authoritative domain):

```
$ dig txt Madyafaka77.majncraft.nick.dnsbl.upal.se @127.0.0.1
;; QUESTION SECTION:
;Madyafaka77.majncraft.nick.dnsbl.upal.se. IN TXT

;; ANSWER SECTION:
Madyafaka77.majncraft.nick.dnsbl.upal.se. 3600 IN TXT "[Majncraft] ban do 28.2. 21:44: Grief u Nepstera [DzejPi]"
```

Server configuration using custom python script
-----------------------------------------------
See attached mcbans.py, it should be obvious.

