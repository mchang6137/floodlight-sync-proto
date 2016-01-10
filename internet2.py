#!/usr/bin/python

from mininet.net import Mininet
from mininet.topo import Topo
from mininet.log import lg, setLogLevel
from mininet.cli import CLI
from mininet.node import RemoteController

CORES = {
  'S1': {'dpid': '000000000000010%s'},
  'S2': {'dpid': '000000000000020%s'},
  'S3': {'dpid': '000000000000030%s'},
  'S4': {'dpid': '000000000000040%s'},
  'S5': {'dpid': '000000000000050%s'},
  }

FANOUT = 1
    
class I2Topo(Topo):

  def __init__(self, enable_all = True):
    "Create Simple policy"

    # Add default members to class.
    super(I2Topo, self).__init__()

    # Add core switches
    self.cores = {}
    for switch in CORES:
      self.cores[switch] = self.addSwitch(switch, dpid=(CORES[switch]['dpid'] % '0'))

    switchcount = 0

    # Add hosts and connect them to their core switch
    for switch in CORES:
      for count in xrange(1, FANOUT + 1):
        # Add hosts
        host = 'h_%s_%s' % (switch, count)
        print host
        ip = '10.0.0.%s' % (count + switchcount)
        print ip
        mac = CORES[switch]['dpid'][4:] % count
        h = self.addHost(host, ip=ip, mac=mac)
        # Connect hosts to core switches
        self.addLink(h, self.cores[switch])
        switchcount = switchcount + 1

    # Connect core switches
    self.addLink(self.cores['S1'], self.cores['S2'])
    self.addLink(self.cores['S2'], self.cores['S5'])
    self.addLink(self.cores['S1'], self.cores['S3'])
    self.addLink(self.cores['S3'], self.cores['S4'])
    self.addLink(self.cores['S4'], self.cores['S5'])


if __name__ == '__main__':
   topo = I2Topo()
   ip = '127.0.0.1'
   masterPort = 6633
   slavePort = 6634
   master = RemoteController('c', ip=ip, port=masterPort)
   slave = RemoteController('c', ip=ip, port=slavePort)
   net = Mininet(topo=topo, autoSetMacs=True, xterms=False, controller=None)
   net.addController(master)
   net.addController(slave)
   net.start()
   print "Hosts configured with IPs, switches pointing to bIfROST at %s:%s" % (ip, masterPort)
   CLI(net)
   net.stop()

