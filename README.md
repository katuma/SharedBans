SharedBans
==========

A complete P2P ban sharing solution for minecraft servers.
Supports nicknames, ip bans and generic anti-spam DNS blacklists.

This plugin operates completely decentralised without central party,
depending on server-to-server trust only.

DNS distribution is robust and scalable, compared to custom
protocols or http.

Plugin (ban query client) configuration
---------------------------------------

```yaml
##############
# generic drone blacklists returning A records, careful with this!
##############
drone-bls:
- rbl.efnetrbl.org:
  - 127.0.0.1: "Open proxy!"
  - 127.0.0.4: "Don't use TOR to play minecraft!"
  - 127.0.0.5: "Drones, hacked PC or home router!"
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
* /sbcheck <nick|ip>
  Performs ban lookup on nick or ip, outputs debugging info.

Permission nodes
----------------
* sb.sbreload
* sb.sbex
* sb.sbunex
* sb.sbcheck

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

If you're unsure about this, just use [FigAdmin queries provided in the example config](https://github.com/katuma/SharedBans/blob/master/etc/pdns.d/pdns.local)


To test your DNS server, use the following (with banned nickname in front, rest is your authoritative domain):

```
$ dig txt test.amunak.dnsbl.upal.se

; <<>> DiG 9.7.3 <<>> txt test.amunak.dnsbl.upal.se
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 52291
;; flags: qr rd ra; QUERY: 1, ANSWER: 2, AUTHORITY: 0, ADDITIONAL: 0

;; QUESTION SECTION:
;test.amunak.dnsbl.upal.se. IN  TXT

;; ANSWER SECTION:
test.amunak.dnsbl.upal.se. 3599 IN  TXT "ban do 6.12. 18:20: ban prvni"
test.amunak.dnsbl.upal.se. 3599 IN  TXT "ban do 16.4. 13:06: ban druhej"

;; Query time: 0 msec
;; SERVER: 10.109.255.51#53(10.109.255.51)
;; WHEN: Sun Feb 26 03:57:03 2012
;; MSG SIZE  rcvd: 128
```

In this case, there were two records returned by database - this is not an error, kicked
clients will just observe the message in round-robin fashion :)

Server configuration using custom python script
-----------------------------------------------
[See attached mcbans.py, it should be obvious.](https://github.com/katuma/SharedBans/blob/master/etc/mcbans.py)

