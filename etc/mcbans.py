#!/usr/bin/python -u
MCBANS_APIKEY='ffffffffffffffffffffffffffffffffff'
DNSBL_DOMAIN=r'^([^.]*)\.mcbans\.([0-9]*)\.dnsbl\.upal.se'
TTL=3600
MSG='"Your MCBans reputation of %.2f is too low (need %d)"'

import re
import json
from urllib import *
raw_input()
print("OK\t")
while True:
    try:
        q=raw_input().split()
        assert q[3] in ("TXT","ANY")
        nick,minrep=re.findall(DNSBL_DOMAIN,q[1])[0]
        minrep=int(minrep)
        hisrep=json.loads(urlopen("http://api.mcbans.com/v2/%s"%MCBANS_APIKEY,urlencode((("player",nick),("exec","playerLookup")))).read())['reputation']
        assert hisrep < minrep
        q[0:6]="DATA",q[1],q[2],"TXT",str(TTL),q[4],(MSG%(hisrep,minrep))
        print("\t".join(q))
    except BaseException:
        pass
    print("END")

