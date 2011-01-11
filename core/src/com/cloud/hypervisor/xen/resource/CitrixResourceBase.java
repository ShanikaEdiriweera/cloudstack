/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.hypervisor.xen.resource;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.AttachVolumeAnswer;
import com.cloud.agent.api.AttachVolumeCommand;
import com.cloud.agent.api.BackupSnapshotAnswer;
import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CheckOnHostAnswer;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.agent.api.CheckVirtualMachineAnswer;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromVolumeCommand;
import com.cloud.agent.api.CreateVolumeFromSnapshotAnswer;
import com.cloud.agent.api.CreateVolumeFromSnapshotCommand;
import com.cloud.agent.api.DeleteSnapshotBackupAnswer;
import com.cloud.agent.api.DeleteSnapshotBackupCommand;
import com.cloud.agent.api.DeleteSnapshotsDirCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.GetVncPortAnswer;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.HostStatsEntry;
import com.cloud.agent.api.MaintainAnswer;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.ManageSnapshotAnswer;
import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.api.MigrateAnswer;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.ModifySshKeysCommand;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.PingRoutingWithNwGroupsCommand;
import com.cloud.agent.api.PingRoutingWithOvsCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.PoolEjectCommand;
import com.cloud.agent.api.PrepareForMigrationAnswer;
import com.cloud.agent.api.PrepareForMigrationCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.RebootRouterCommand;
import com.cloud.agent.api.SecurityIngressRuleAnswer;
import com.cloud.agent.api.SecurityIngressRulesCmd;
import com.cloud.agent.api.SetupAnswer;
import com.cloud.agent.api.SetupCommand;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.StopCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.agent.api.check.CheckSshAnswer;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.api.proxy.CheckConsoleProxyLoadCommand;
import com.cloud.agent.api.proxy.ConsoleProxyLoadAnswer;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.agent.api.routing.IPAssocCommand;
import com.cloud.agent.api.routing.IpAssocAnswer;
import com.cloud.agent.api.routing.LoadBalancerCfgCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.RemoteAccessVpnCfgCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesAnswer;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.routing.VpnUsersCfgCommand;
import com.cloud.agent.api.storage.CopyVolumeAnswer;
import com.cloud.agent.api.storage.CopyVolumeCommand;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.CreatePrivateTemplateAnswer;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.agent.api.storage.ShareAnswer;
import com.cloud.agent.api.storage.ShareCommand;
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.dc.Vlan;
import com.cloud.exception.InternalErrorException;
import com.cloud.host.Host.Type;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.HAProxyConfigurator;
import com.cloud.network.LoadBalancerConfigurator;
import com.cloud.network.Networks;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.ovs.OvsCreateGreTunnelAnswer;
import com.cloud.network.ovs.OvsCreateGreTunnelCommand;
import com.cloud.network.ovs.OvsDeleteFlowCommand;
import com.cloud.network.ovs.OvsSetTagAndFlowAnswer;
import com.cloud.network.ovs.OvsSetTagAndFlowCommand;
import com.cloud.resource.ServerResource;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.Volume.VolumeType;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.resource.StoragePoolResource;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.template.VirtualMachineTemplate.BootloaderType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineName;
import com.trilead.ssh2.SCPClient;
import com.xensource.xenapi.Bond;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Console;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.HostCpu;
import com.xensource.xenapi.HostMetrics;
import com.xensource.xenapi.Network;
import com.xensource.xenapi.PBD;
import com.xensource.xenapi.PIF;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.IpConfigurationMode;
import com.xensource.xenapi.Types.VmPowerState;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VBD;
import com.xensource.xenapi.VDI;
import com.xensource.xenapi.VIF;
import com.xensource.xenapi.VLAN;
import com.xensource.xenapi.VM;
import com.xensource.xenapi.VMGuestMetrics;
import com.xensource.xenapi.XenAPIObject;

/**
 * Encapsulates the interface to the XenServer API.
 * 
 */
@Local(value = ServerResource.class)
public abstract class CitrixResourceBase implements ServerResource {
    private static final Logger s_logger = Logger.getLogger(CitrixResourceBase.class);
    protected static final XenServerConnectionPool _connPool = XenServerConnectionPool.getInstance();
    protected static final int MB = 1024 * 1024;
    protected String _name;
    protected String _username;
    protected String _password;
    protected final int _retry = 24;
    protected final int _sleep = 10000;
    protected long _dcId;
    protected String _pod;
    protected String _cluster;
    protected HashMap<String, State> _vms = new HashMap<String, State>(71);
    protected String _privateNetworkName;
    protected String _linkLocalPrivateNetworkName;
    protected String _publicNetworkName;
    protected String _storageNetworkName1;
    protected String _storageNetworkName2;
    protected String _guestNetworkName;
    protected int _wait;
    protected IAgentControl _agentControl;
    
    int _userVMCap = 0;
    final int _maxWeight = 256;

    protected final XenServerHost _host = new XenServerHost();

    // Guest and Host Performance Statistics
    protected boolean _collectHostStats = false;
    protected String _consolidationFunction = "AVERAGE";
    protected int _pollingIntervalInSeconds = 60;

    protected StorageLayer _storage;
    protected boolean _canBridgeFirewall = false;
    protected boolean _isOvs = false;
    protected HashMap<StoragePoolType, StoragePoolResource> _pools = new HashMap<StoragePoolType, StoragePoolResource>(5);

    public enum SRType {
        NFS, LVM, ISCSI, ISO, LVMOISCSI;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public boolean equals(String type) {
            return super.toString().equalsIgnoreCase(type);
        }
    }

    protected static HashMap<Types.VmPowerState, State> s_statesTable;
    protected String _localGateway;
    static {
        s_statesTable = new HashMap<Types.VmPowerState, State>();
        s_statesTable.put(Types.VmPowerState.HALTED, State.Stopped);
        s_statesTable.put(Types.VmPowerState.PAUSED, State.Running);
        s_statesTable.put(Types.VmPowerState.RUNNING, State.Running);
        s_statesTable.put(Types.VmPowerState.SUSPENDED, State.Running);
        s_statesTable.put(Types.VmPowerState.UNRECOGNIZED, State.Unknown);
    }
    
    
    protected boolean isRefNull(XenAPIObject object) {
        return (object == null || object.toWireString().equals("OpaqueRef:NULL"));
    }

    @Override
    public void disconnected() {
    }

    protected VDI cloudVDIcopy(Connection conn, VDI vdi, SR sr) throws BadServerResponse, XenAPIException, XmlRpcException{
        return vdi.copy(conn, sr);
    }
    

    protected Pair<VM, VM.Record> getVmByNameLabel(Connection conn, Host host, String nameLabel, boolean getRecord) throws XmlRpcException, XenAPIException {
        Set<VM> vms = host.getResidentVMs(conn);
        for (VM vm : vms) {
            VM.Record rec = null;
            String name = null;
            if (getRecord) {
                rec = vm.getRecord(conn);
                name = rec.nameLabel;
            } else {
                name = vm.getNameLabel(conn);
            }
            if (name.equals(nameLabel)) {
                return new Pair<VM, VM.Record>(vm, rec);
            }
        }

        return null;
    }

    protected boolean pingdomr(Connection conn, String host, String port) {
        String status;
        status = callHostPlugin(conn, "vmops", "pingdomr", "host", host, "port", port);

        if (status == null || status.isEmpty()) {
            return false;
        }

        return true;

    }

    protected boolean pingxenserver() {
        Session slaveSession = null;
        Connection slaveConn = null;
        try {
            URL slaveUrl = null;
            slaveUrl = new URL("http://" + _host.ip);
            slaveConn = new Connection(slaveUrl, 100);
            slaveSession = Session.slaveLocalLoginWithPassword(slaveConn, _username, _password);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if( slaveSession != null ){
                try{
                    Session.localLogout(slaveConn);
                } catch (Exception e) {
                    
                }
                slaveConn.dispose();
            }
        }
    }

    protected String logX(XenAPIObject obj, String msg) {
        return new StringBuilder("Host ").append(_host.ip).append(" ").append(obj.toWireString()).append(": ").append(msg).toString();
    }


    @Override
    public Answer executeRequest(Command cmd) {

        if (cmd instanceof CreateCommand) {
            return execute((CreateCommand) cmd);
        } else if (cmd instanceof SetPortForwardingRulesCommand) {
            return execute((SetPortForwardingRulesCommand) cmd);
        } else if (cmd instanceof LoadBalancerCfgCommand) {
            return execute((LoadBalancerCfgCommand) cmd);
        } else if (cmd instanceof LoadBalancerConfigCommand) {
            return execute((LoadBalancerConfigCommand) cmd);
        } else if (cmd instanceof IPAssocCommand) {
            return execute((IPAssocCommand) cmd);
        } else if (cmd instanceof CheckConsoleProxyLoadCommand) {
            return execute((CheckConsoleProxyLoadCommand) cmd);
        } else if (cmd instanceof WatchConsoleProxyLoadCommand) {
            return execute((WatchConsoleProxyLoadCommand) cmd);
        } else if (cmd instanceof SavePasswordCommand) {
            return execute((SavePasswordCommand) cmd);
        } else if (cmd instanceof DhcpEntryCommand) {
            return execute((DhcpEntryCommand) cmd);
        } else if (cmd instanceof VmDataCommand) {
            return execute((VmDataCommand) cmd);
        } else if (cmd instanceof ReadyCommand) {
            return execute((ReadyCommand) cmd);
        } else if (cmd instanceof GetHostStatsCommand) {
            return execute((GetHostStatsCommand) cmd);
        } else if (cmd instanceof GetVmStatsCommand) {
            return execute((GetVmStatsCommand) cmd);
        } else if (cmd instanceof CheckHealthCommand) {
            return execute((CheckHealthCommand) cmd);
        } else if (cmd instanceof StopCommand) {
            return execute((StopCommand) cmd);
        } else if (cmd instanceof RebootRouterCommand) {
            return execute((RebootRouterCommand) cmd);
        } else if (cmd instanceof RebootCommand) {
            return execute((RebootCommand) cmd);
        } else if (cmd instanceof CheckVirtualMachineCommand) {
            return execute((CheckVirtualMachineCommand) cmd);
        } else if (cmd instanceof PrepareForMigrationCommand) {
            return execute((PrepareForMigrationCommand) cmd);
        } else if (cmd instanceof MigrateCommand) {
            return execute((MigrateCommand) cmd);
        } else if (cmd instanceof DestroyCommand) {
            return execute((DestroyCommand) cmd);
        } else if (cmd instanceof ShareCommand) {
            return execute((ShareCommand) cmd);
        } else if (cmd instanceof ModifyStoragePoolCommand) {
            return execute((ModifyStoragePoolCommand) cmd);
        } else if (cmd instanceof DeleteStoragePoolCommand) {
            return execute((DeleteStoragePoolCommand) cmd);
        } else if (cmd instanceof CopyVolumeCommand) {
            return execute((CopyVolumeCommand) cmd);
        } else if (cmd instanceof AttachVolumeCommand) {
            return execute((AttachVolumeCommand) cmd);
        } else if (cmd instanceof AttachIsoCommand) {
            return execute((AttachIsoCommand) cmd);
        } else if (cmd instanceof ManageSnapshotCommand) {
            return execute((ManageSnapshotCommand) cmd);
        } else if (cmd instanceof BackupSnapshotCommand) {
            return execute((BackupSnapshotCommand) cmd);
        } else if (cmd instanceof DeleteSnapshotBackupCommand) {
            return execute((DeleteSnapshotBackupCommand) cmd);
        } else if (cmd instanceof CreateVolumeFromSnapshotCommand) {
            return execute((CreateVolumeFromSnapshotCommand) cmd);
        } else if (cmd instanceof DeleteSnapshotsDirCommand) {
            return execute((DeleteSnapshotsDirCommand) cmd);
        } else if (cmd instanceof CreatePrivateTemplateFromVolumeCommand) {
            return execute((CreatePrivateTemplateFromVolumeCommand) cmd);
        } else if (cmd instanceof CreatePrivateTemplateFromSnapshotCommand) {
            return execute((CreatePrivateTemplateFromSnapshotCommand) cmd);
        } else if (cmd instanceof GetStorageStatsCommand) {
            return execute((GetStorageStatsCommand) cmd);
        } else if (cmd instanceof PrimaryStorageDownloadCommand) {
            return execute((PrimaryStorageDownloadCommand) cmd);
        } else if (cmd instanceof GetVncPortCommand) {
            return execute((GetVncPortCommand) cmd);
        } else if (cmd instanceof SetupCommand) {
            return execute((SetupCommand) cmd);
        } else if (cmd instanceof MaintainCommand) {
            return execute((MaintainCommand) cmd);
        } else if (cmd instanceof PingTestCommand) {
            return execute((PingTestCommand) cmd);
        } else if (cmd instanceof CheckOnHostCommand) {
            return execute((CheckOnHostCommand) cmd);
        } else if (cmd instanceof ModifySshKeysCommand) {
            return execute((ModifySshKeysCommand) cmd);
        } else if (cmd instanceof PoolEjectCommand) {
            return execute((PoolEjectCommand) cmd);
        } else if (cmd instanceof StartCommand) {
            return execute((StartCommand)cmd);
        } else if (cmd instanceof RemoteAccessVpnCfgCommand) {
            return execute((RemoteAccessVpnCfgCommand)cmd);
        } else if (cmd instanceof VpnUsersCfgCommand) {
            return execute((VpnUsersCfgCommand)cmd);
        } else if (cmd instanceof CheckSshCommand) {
            return execute((CheckSshCommand)cmd);
        } else if (cmd instanceof SecurityIngressRulesCmd) {
            return execute((SecurityIngressRulesCmd) cmd);
        } else if (cmd instanceof OvsCreateGreTunnelCommand) {
        	return execute((OvsCreateGreTunnelCommand)cmd);
        } else if (cmd instanceof OvsSetTagAndFlowCommand) {
        	return execute((OvsSetTagAndFlowCommand)cmd);
        } else if (cmd instanceof OvsDeleteFlowCommand) {
        	return execute((OvsDeleteFlowCommand)cmd);
        } else {
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }
    
    Pair<Network, String> getNativeNetworkForTraffic(Connection conn, TrafficType type) throws XenAPIException, XmlRpcException {
        if (type == TrafficType.Guest) {
            return new Pair<Network, String>(Network.getByUuid(conn, _host.guestNetwork), _host.guestPif);
        } else if (type == TrafficType.Control) {
            setupLinkLocalNetwork(conn);            
            return new Pair<Network, String>(Network.getByUuid(conn, _host.linkLocalNetwork), null);
        } else if (type == TrafficType.Management) {
            return new Pair<Network, String>(Network.getByUuid(conn, _host.privateNetwork), _host.privatePif);
        } else if (type == TrafficType.Public) {
            return new Pair<Network, String>(Network.getByUuid(conn, _host.publicNetwork), _host.publicPif);
        } else if (type == TrafficType.Storage) {
            return new Pair<Network, String>(Network.getByUuid(conn, _host.storageNetwork1), _host.storagePif1);
        } else if (type == TrafficType.Vpn) {
            return new Pair<Network, String>(Network.getByUuid(conn, _host.publicNetwork), _host.publicPif);
        }
        
        throw new CloudRuntimeException("Unsupported network type: " + type);
    }
    
    /**
     * This is a tricky to create network in xenserver.
     * if you create a network then create bridge by brctl or openvswitch yourself,
     * then you will get an expection that is "REQUIRED_NETWROK" when you start a
     * vm with this network. The soultion is, create a vif of dom0 and plug it in
     * network, xenserver will create the bridge on behalf of you
     * @throws XmlRpcException 
     * @throws XenAPIException 
     */
    private void enableXenServerNetwork(Connection conn, Network nw,
    		String vifNameLabel, String networkDesc) throws XenAPIException, XmlRpcException {
    	/* Make sure there is a physical bridge on this network */
        VIF dom0vif = null;
        Pair<VM, VM.Record> vm = getControlDomain(conn);
        VM dom0 = vm.first();
        Set<VIF> vifs = dom0.getVIFs(conn);
        if (vifs.size() != 0) {
        	for (VIF vif : vifs) {
        		Map<String, String> otherConfig = vif.getOtherConfig(conn);
        		if (otherConfig != null) {
        		    String nameLabel = otherConfig.get("nameLabel");
        		    if ((nameLabel != null) && nameLabel.equalsIgnoreCase(vifNameLabel)) {
        		        dom0vif = vif;
        		    }
        		}
        	}
        }
        /* create temp VIF0 */
        if (dom0vif == null) {
        	s_logger.debug("Can't find a vif on dom0 for " + networkDesc + ", creating a new one");
        	VIF.Record vifr = new VIF.Record();
        	vifr.VM = dom0;
        	vifr.device = getLowestAvailableVIFDeviceNum(conn, dom0);
        	if (vifr.device == null) {
        		s_logger.debug("Failed to create " + networkDesc + ", no vif available");
        		return;
        	}
        	Map<String, String> config = new HashMap<String, String>();
        	config.put("nameLabel", vifNameLabel);
        	vifr.otherConfig = config;
        	vifr.MAC = "FE:FF:FF:FF:FF:FF";
        	vifr.network = nw;
        	dom0vif = VIF.create(conn, vifr);
        	dom0vif.plug(conn);
        } else {
        	s_logger.debug("already have a vif on dom0 for " + networkDesc);
        	if (!dom0vif.getCurrentlyAttached(conn)) {
        		dom0vif.plug(conn);
        	}
        }
    }
    
    private Network setupvSwitchNetwork(Connection conn) {
		try {
			if (_host.vswitchNetwork == null) {
				Network vswitchNw = null;
				Network.Record rec = new Network.Record();
				String nwName = Networks.BroadcastScheme.VSwitch.toString();
				Set<Network> networks = Network.getByNameLabel(conn, nwName);
				
				if (networks.size() == 0) {
					rec.nameDescription = "vswitch network for " + nwName;
					rec.nameLabel = nwName;
					vswitchNw = Network.create(conn, rec);
				} else {
					vswitchNw = networks.iterator().next();
				}

				enableXenServerNetwork(conn, vswitchNw, "vswitch",
						"vswicth network");
				_host.vswitchNetwork = vswitchNw;
			} 
			return _host.vswitchNetwork;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
    }
    
    protected Network getNetwork(Connection conn, NicTO nic) throws XenAPIException, XmlRpcException {
        Pair<Network, String> network = getNativeNetworkForTraffic(conn, nic.getType());
        if (nic.getBroadcastUri() != null && nic.getBroadcastUri().toString().contains("untagged")) {
            return network.first();
        } else if (nic.getBroadcastType() == BroadcastDomainType.Vlan) {
            URI broadcastUri = nic.getBroadcastUri();
            assert broadcastUri.getScheme().equals(BroadcastDomainType.Vlan.scheme());
            long vlan = Long.parseLong(broadcastUri.getHost());
            return enableVlanNetwork(conn, vlan, network.first(), network.second());
        } else if (nic.getBroadcastType() == BroadcastDomainType.Native || nic.getBroadcastType() == BroadcastDomainType.LinkLocal) {
            return network.first();
        } else if (nic.getBroadcastType() == BroadcastDomainType.Vswitch) {
        	return setupvSwitchNetwork(conn);
        }
        
        throw new CloudRuntimeException("Unable to support this type of network broadcast domain: " + nic.getBroadcastUri());
    }
    
    protected VIF createVif(Connection conn, String vmName, VM vm, NicTO nic) throws XmlRpcException, XenAPIException {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating VIF for " + vmName + " on nic " + nic);
        }
        
        VIF.Record vifr = new VIF.Record();
        vifr.VM = vm;
        vifr.device = Integer.toString(nic.getDeviceId());
        vifr.MAC = nic.getMac();

        vifr.network = getNetwork(conn, nic);
        
        if (nic.getNetworkRateMbps() != null) {
            vifr.qosAlgorithmType = "ratelimit";
            vifr.qosAlgorithmParams = new HashMap<String, String>();
            // convert mbs to kilobyte per second
            vifr.qosAlgorithmParams.put("kbps", Integer.toString(nic.getNetworkRateMbps() * 1024));
        }
        
        VIF vif = VIF.create(conn, vifr);
        if (s_logger.isDebugEnabled()) {
            vifr = vif.getRecord(conn);
            s_logger.debug("Created a vif " + vifr.uuid + " on " + nic.getDeviceId());
        }
        
        return vif;
    }
    
    protected VDI mount(Connection conn, String vmName, VolumeTO volume) throws XmlRpcException, XenAPIException {
        if (volume.getType() == VolumeType.ISO) {
        	
            String isopath = volume.getPath();
            if (isopath == null) {
            	return null;
            }
            int index = isopath.lastIndexOf("/");

            String mountpoint = isopath.substring(0, index);
            URI uri;
            try {
                uri = new URI(mountpoint);
            } catch (URISyntaxException e) {
                throw new CloudRuntimeException("Incorrect uri " + mountpoint, e);
            }
            SR isoSr = createIsoSRbyURI(conn, uri, vmName, false);

            String isoname = isopath.substring(index + 1);

            VDI isoVdi = getVDIbyLocationandSR(conn, isoname, isoSr);

            if (isoVdi == null) {
                throw new CloudRuntimeException("Unable to find ISO " + volume.getPath());
            }
            return isoVdi;
        } else {
            return VDI.getByUuid(conn, volume.getPath());
        }
    }
    
    protected VBD createVbd(Connection conn, VolumeTO volume, String vmName, VM vm, BootloaderType bootLoaderType) throws XmlRpcException, XenAPIException {
        VolumeType type = volume.getType();
        
        VDI vdi = mount(conn, vmName, volume);
        
        VBD.Record vbdr = new VBD.Record();
        vbdr.VM = vm;
        if (vdi != null) {
            vbdr.VDI = vdi;
        } else {
        	vbdr.empty = true;
        }
        if (type == VolumeType.ROOT && bootLoaderType == BootloaderType.PyGrub) {
            vbdr.bootable = true;
        }else if(type == VolumeType.ISO && bootLoaderType == BootloaderType.CD) {
        	vbdr.bootable = true;
        }
        
        vbdr.userdevice = Long.toString(volume.getDeviceId());
        if (volume.getType() == VolumeType.ISO) {
            vbdr.mode = Types.VbdMode.RO;
            vbdr.type = Types.VbdType.CD;
        } else {
            vbdr.mode = Types.VbdMode.RW;
            vbdr.type = Types.VbdType.DISK;           
        }
        
        VBD vbd = VBD.create(conn, vbdr);
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("VBD " + vbd.getUuid(conn) + " created for " + volume);
        }
        
        return vbd;
    }
    
    protected VM createVmFromTemplate(Connection conn, VirtualMachineTO vmSpec, Host host) throws XenAPIException, XmlRpcException {
        String guestOsTypeName = getGuestOsType(vmSpec.getOs(), vmSpec.getBootloader() == BootloaderType.CD);
        Set<VM> templates = VM.getByNameLabel(conn, guestOsTypeName);
        assert templates.size() == 1 : "Should only have 1 template but found " + templates.size();
        VM template = templates.iterator().next();
        
        VM vm = template.createClone(conn, vmSpec.getName());       
        VM.Record vmr = vm.getRecord(conn);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Created VM " + vmr.uuid + " for " + vmSpec.getName());
        }
        
        for (Console console : vmr.consoles) {
            console.destroy(conn);
        }
        
        vm.setIsATemplate(conn, false);
        vm.removeFromOtherConfig(conn, "disks");
        vm.setNameLabel(conn, vmSpec.getName());
        setMemory(conn, vm, vmSpec.getMinRam());
        vm.setVCPUsMax(conn, (long)vmSpec.getCpus());
        vm.setVCPUsAtStartup(conn, (long)vmSpec.getCpus());
        
        Map<String, String> vcpuParams = new HashMap<String, String>();

        Integer speed = vmSpec.getSpeed();
        if (speed != null) {
            int utilization = _userVMCap; //cpu_cap
            //Configuration cpu.uservm.cap is not available in default installation. Using this parameter is not encouraged
            
            int cpuWeight = _maxWeight; //cpu_weight
            
            // weight based allocation
            cpuWeight = (int)((speed*0.99) / _host.speed * _maxWeight);
            if (cpuWeight > _maxWeight) {
                cpuWeight = _maxWeight;
            }
            
            vcpuParams.put("weight", Integer.toString(cpuWeight));
            vcpuParams.put("cap", Integer.toString(utilization));
            
        }
        
        if (vcpuParams.size() > 0) {
            vm.setVCPUsParams(conn, vcpuParams);
        }

        vm.setActionsAfterCrash(conn, Types.OnCrashBehaviour.DESTROY);
        vm.setActionsAfterShutdown(conn, Types.OnNormalExit.DESTROY);
        
        String bootArgs = vmSpec.getBootArgs();
        if (bootArgs != null && bootArgs.length() > 0) {
            String pvargs = vm.getPVArgs(conn);
            pvargs = pvargs + vmSpec.getBootArgs();
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("PV args are " + pvargs);
            }
            vm.setPVArgs(conn, pvargs);
        }
        
        if (!(guestOsTypeName.startsWith("Windows") || guestOsTypeName.startsWith("Citrix") || guestOsTypeName.startsWith("Other"))) {
            if (vmSpec.getBootloader() == BootloaderType.CD) {
                vm.setPVBootloader(conn, "eliloader");
                Map<String, String> otherConfig = vm.getOtherConfig(conn);
                otherConfig.put( "install-repository", "cdrom");
                vm.setOtherConfig(conn, otherConfig);
            } else if (vmSpec.getBootloader() == BootloaderType.PyGrub ){
                vm.setPVBootloader(conn, "pygrub");
            } else {
                vm.destroy(conn);
                throw new CloudRuntimeException("Unable to handle boot loader type: " + vmSpec.getBootloader());
            }
        }
        
        return vm;
    }
    
    protected String handleVmStartFailure(Connection conn, String vmName, VM vm, String message, Throwable th) {
        String msg = "Unable to start " + vmName + " due to " + message;
        s_logger.warn(msg, th);
        
        if (vm == null) {
            return msg;
        }
        
        try {
            VM.Record vmr = vm.getRecord(conn);
            if (vmr.powerState == VmPowerState.RUNNING) {
                try {
                    vm.hardShutdown(conn);
                } catch (Exception e) {
                    s_logger.warn("VM hardshutdown failed due to ", e);
                }
            }
            if (vm.getPowerState(conn) == VmPowerState.HALTED) {
                try {
                    vm.destroy(conn);
                } catch (Exception e) {
                    s_logger.warn("VM destroy failed due to ", e);
                }
            }
            for (VBD vbd : vmr.VBDs) {
                try {
                    vbd.unplug(conn);
                    vbd.destroy(conn);
                } catch (Exception e) {
                    s_logger.warn("Unable to clean up VBD due to ", e);
                }
            }
            for (VIF vif : vmr.VIFs) {
                try {
                    vif.unplug(conn);
                    vif.destroy(conn);
                } catch (Exception e) {
                    s_logger.warn("Unable to cleanup VIF", e);
                }
            }
        } catch (Exception e) {
            s_logger.warn("VM getRecord failed due to ", e);
        }
        
        return msg;
    }
    
    protected VBD createPatchVbd(Connection conn, String vmName, VM vm) throws XmlRpcException, XenAPIException {
        
        if(  _host.systemvmisouuid == null ) {
            Set<SR> srs = SR.getByNameLabel(conn, "XenServer Tools");
            if( srs.size() != 1 ) {
                throw new CloudRuntimeException("There are " + srs.size() + " SRs with name XenServer Tools");
            }
            SR sr = srs.iterator().next();
            sr.scan(conn);

            SR.Record srr = sr.getRecord(conn);

            for( VDI vdi : srr.VDIs ) {
                VDI.Record vdir = vdi.getRecord(conn);
                if(vdir.nameLabel.contains("systemvm-premium")){
                    _host.systemvmisouuid = vdir.uuid;
                    break;
                }                       
            }
            if(  _host.systemvmisouuid == null ) {
                for( VDI vdi : srr.VDIs ) {
                    VDI.Record vdir = vdi.getRecord(conn);
                        if(vdir.nameLabel.contains("systemvm")){
                            _host.systemvmisouuid = vdir.uuid;
                            break;
                     }
                }
            }
            if(  _host.systemvmisouuid == null ) {
                throw new CloudRuntimeException("can not find systemvmiso");
            } 
        }
        
        VBD.Record cdromVBDR = new VBD.Record();
        cdromVBDR.VM = vm;
        cdromVBDR.empty = true;
        cdromVBDR.bootable = false;
        cdromVBDR.userdevice = "3";
        cdromVBDR.mode = Types.VbdMode.RO;
        cdromVBDR.type = Types.VbdType.CD;
        VBD cdromVBD = VBD.create(conn, cdromVBDR);
        cdromVBD.insert(conn, VDI.getByUuid(conn, _host.systemvmisouuid));
        
        return cdromVBD;
    }
    
    protected CheckSshAnswer execute(CheckSshCommand cmd) {
        Connection conn = getConnection();
        String vmName = cmd.getName();
        String privateIp = cmd.getIp();
        int cmdPort = cmd.getPort();
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Ping command port, " + privateIp + ":" + cmdPort);
        }

        try {
            String result = connect(conn, cmd.getName(), privateIp, cmdPort);
            if (result != null) {
                return new CheckSshAnswer(cmd, "Can not ping System vm " + vmName + "due to:" + result);
            } 
        } catch (Exception e) {
            return new CheckSshAnswer(cmd, e);
        }
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Ping command port succeeded for vm " + vmName);
        }
        
        return new CheckSshAnswer(cmd);
    }
    
    protected StartAnswer execute(StartCommand cmd) {
        Connection conn = getConnection();
        VirtualMachineTO vmSpec = cmd.getVirtualMachine();
        String vmName = vmSpec.getName(); 
        State state = State.Stopped;
        VM vm = null;
        try {
            Host host = Host.getByUuid(conn, _host.uuid);
            synchronized (_vms) {
                _vms.put(vmName, State.Starting);
            }
            
            vm = createVmFromTemplate(conn, vmSpec, host);
            
            for (VolumeTO disk : vmSpec.getDisks()) {
                createVbd(conn, disk, vmName, vm, vmSpec.getBootloader());
            }
            
            if (vmSpec.getType() != VirtualMachine.Type.User) {
                createPatchVbd(conn, vmName, vm);
            }
            
            for (NicTO nic : vmSpec.getNics()) {
                createVif(conn, vmName, vm, nic);
            }
            
            startVM(conn, host, vm, vmName);
            
            if (_canBridgeFirewall) {
                String result = null;
                if (vmSpec.getType() != VirtualMachine.Type.User) {
                    result = callHostPlugin(conn, "vmops", "default_network_rules_systemvm", "vmName", vmName);
                    
                    if (result == null || result.isEmpty() || !Boolean.parseBoolean(result)) {
                        s_logger.warn("Failed to program default network rules for " + vmName);
                    } else {
                        s_logger.info("Programmed default network rules for " + vmName);
                    }
                } else {
                	//For user vm, program the rules for each nic if the isolation uri scheme is ec2
                	NicTO[] nics = vmSpec.getNics();
                	for (NicTO nic : nics) { 
                		if (nic.getIsolationUri() != null && nic.getIsolationUri().getScheme().equalsIgnoreCase(IsolationType.Ec2.toString())) {
		                	result = callHostPlugin(conn, "vmops", "default_network_rules", "vmName", vmName, "vmIP", nic.getIp(), "vmMAC", nic.getMac(), "vmID", Long.toString(vmSpec.getId()));
		                	
		                    if (result == null || result.isEmpty() || !Boolean.parseBoolean(result)) {
		                        s_logger.warn("Failed to program default network rules for " + vmName+" on nic with ip:"+nic.getIp()+" mac:"+nic.getMac());
		                    } else {
		                        s_logger.info("Programmed default network rules for " + vmName+" on nic with ip:"+nic.getIp()+" mac:"+nic.getMac());
		                    }	                	
                		}
                	}
                }   
            }
            
            state = State.Running;
            return new StartAnswer(cmd);
        } catch (Exception e) {
            s_logger.warn("Catch Exception: " + e.getClass().toString() + " due to " + e.toString(), e);
            String msg = handleVmStartFailure(conn, vmName, vm, "", e);
            return new StartAnswer(cmd, msg);
        } finally {
            synchronized (_vms) {
                if (state != State.Stopped) {
                    _vms.put(vmName, state);
                } else {
                    _vms.remove(vmName);
                }
            }
        }
    }

    protected Answer execute(ModifySshKeysCommand cmd) {
    	return new Answer(cmd);
    }

    private boolean doPingTest(Connection conn, final String computingHostIp) {
        String args = "-h " + computingHostIp;
        String result = callHostPlugin(conn, "vmops", "pingtest", "args", args);
        if (result == null || result.isEmpty()) {
            return false;
        }
        return true;
    }

    protected CheckOnHostAnswer execute(CheckOnHostCommand cmd) {
        return new CheckOnHostAnswer(cmd, null, "Not Implmeneted");
    }

    private boolean doPingTest(Connection conn, final String domRIp, final String vmIp) {
        String args = "-i " + domRIp + " -p " + vmIp;
        String result = callHostPlugin(conn, "vmops", "pingtest", "args", args);
        if (result == null || result.isEmpty()) {
            return false;
        }
        return true;
    }

    private Answer execute(PingTestCommand cmd) {
        Connection conn = getConnection();
        boolean result = false;
        final String computingHostIp = cmd.getComputingHostIp();

        if (computingHostIp != null) {
            result = doPingTest(conn, computingHostIp);
        } else {
            result = doPingTest(conn, cmd.getRouterIp(), cmd.getPrivateIp());
        }

        if (!result) {
            return new Answer(cmd, false, "PingTestCommand failed");
        }
        return new Answer(cmd);
    }

    protected MaintainAnswer execute(MaintainCommand cmd) {
        Connection conn = getConnection();
        try {
            Pool pool = Pool.getByUuid(conn, _host.pool);
            Pool.Record poolr = pool.getRecord(conn);

            Host.Record hostr = poolr.master.getRecord(conn);
            if (!_host.uuid.equals(hostr.uuid)) {
                s_logger.debug("Not the master node so just return ok: " + _host.ip);
                return new MaintainAnswer(cmd);
            }
            Map<Host, Host.Record> hostMap = Host.getAllRecords(conn);
            if (hostMap.size() == 1) {
                s_logger.debug("There is the last host in pool " + poolr.uuid );
                return new MaintainAnswer(cmd);
            }
            Host newMaster = null;
            Host.Record newMasterRecord = null;
            for (Map.Entry<Host, Host.Record> entry : hostMap.entrySet()) {
                if (!_host.uuid.equals(entry.getValue().uuid)) {
                    newMaster = entry.getKey();
                    newMasterRecord = entry.getValue();
                    s_logger.debug("New master for the XenPool is " + newMasterRecord.uuid + " : " + newMasterRecord.address);
                    try {
                        _connPool.switchMaster(_host.ip, _host.pool, conn, newMaster, _username, _password, _wait);
                        return new MaintainAnswer(cmd, "New Master is " + newMasterRecord.address);
                    } catch (XenAPIException e) {
                        s_logger.warn("Unable to switch the new master to " + newMasterRecord.uuid + ": " + newMasterRecord.address + " Trying again...");
                    } catch (XmlRpcException e) {
                        s_logger.warn("Unable to switch the new master to " + newMasterRecord.uuid + ": " + newMasterRecord.address + " Trying again...");
                    }
                }
            }
            return new MaintainAnswer(cmd, false, "Unable to find an appropriate host to set as the new master");
        } catch (XenAPIException e) {
            s_logger.warn("Unable to put server in maintainence mode", e);
            return new MaintainAnswer(cmd, false, e.getMessage());
        } catch (XmlRpcException e) {
            s_logger.warn("Unable to put server in maintainence mode", e);
            return new MaintainAnswer(cmd, false, e.getMessage());
        }
    }

    protected SetupAnswer execute(SetupCommand cmd) {
        return new SetupAnswer(cmd, false);
    }

    protected SetPortForwardingRulesAnswer execute(SetPortForwardingRulesCommand cmd) {
        Connection conn = getConnection();
        String args;
        String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        String[] results = new String[cmd.getRules().length];
        int i = 0;
        for (PortForwardingRuleTO rule : cmd.getRules()) {
            if (rule.getProtocol().toLowerCase().equals(NetUtils.NAT_PROTO)){
            	//1:1 NAT needs instanceip;publicip;domrip;op
            	args = rule.revoked() ? "-D" : "-A";
            	
    	        args += " -l " + rule.getSrcIp();
    	        args += " -i " + routerIp;
    	        args += " -r " + rule.getDstIp();
    	        args += " -G " + rule.getProtocol();
            } else {
                args = rule.revoked() ? "-D" : "-A";
    
    	        args += " -P " + rule.getProtocol().toLowerCase();
    	        args += " -l " + rule.getSrcIp();
    	        args += " -p " + rule.getSrcPortRange()[0];
    	        args += " -n " + routerName;
    	        args += " -i " + routerIp;
    	        args += " -r " + rule.getDstIp();
    	        args += " -d " + rule.getDstPortRange()[0];
    	        args += " -N " + rule.getVlanNetmask();
    	
//    	        String oldPrivateIP = rule.getOldPrivateIP();
//    	        String oldPrivatePort = rule.getOldPrivatePort();
//    	
//    	        if (oldPrivateIP != null) {
//    	            args += " -w " + oldPrivateIP;
//    	        }
//    	
//    	        if (oldPrivatePort != null) {
//    	            args += " -x " + oldPrivatePort;
//    	        }
            }
            String result = callHostPlugin(conn, "vmops", "setFirewallRule", "args", args);
            results[i++] = (result == null || result.isEmpty()) ? "Failed" : null;
        }

        return new SetPortForwardingRulesAnswer(cmd, results);
    }

    protected Answer execute(final LoadBalancerCfgCommand cmd) {
        Connection conn = getConnection();
        String routerIp = cmd.getRouterIp();

        if (routerIp == null) {
            return new Answer(cmd);
        }

        String tmpCfgFilePath = "/tmp/" + cmd.getRouterIp().replace('.', '_') + ".cfg";
        String tmpCfgFileContents = "";
        for (int i = 0; i < cmd.getConfig().length; i++) {
            tmpCfgFileContents += cmd.getConfig()[i];
            tmpCfgFileContents += "\n";
        }

        String result = callHostPlugin(conn, "vmops", "createFile", "filepath", tmpCfgFilePath, "filecontents", tmpCfgFileContents);

        if (result == null || result.isEmpty()) {
            return new Answer(cmd, false, "LoadBalancerCfgCommand failed to create HA proxy cfg file.");
        }

        String[] addRules = cmd.getAddFwRules();
        String[] removeRules = cmd.getRemoveFwRules();

        String args = "";
        args += "-i " + routerIp;
        args += " -f " + tmpCfgFilePath;

        StringBuilder sb = new StringBuilder();
        if (addRules.length > 0) {
            for (int i = 0; i < addRules.length; i++) {
                sb.append(addRules[i]).append(',');
            }

            args += " -a " + sb.toString();
        }

        sb = new StringBuilder();
        if (removeRules.length > 0) {
            for (int i = 0; i < removeRules.length; i++) {
                sb.append(removeRules[i]).append(',');
            }

            args += " -d " + sb.toString();
        }

        result = callHostPlugin(conn, "vmops", "setLoadBalancerRule", "args", args);

        if (result == null || result.isEmpty()) {
            return new Answer(cmd, false, "LoadBalancerCfgCommand failed");
        }

        callHostPlugin(conn, "vmops", "deleteFile", "filepath", tmpCfgFilePath);

        return new Answer(cmd);
    }
    
    protected Answer execute(final LoadBalancerConfigCommand cmd) {
        Connection conn = getConnection();
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);

        if (routerIp == null) {
            return new Answer(cmd);
        }
        
        LoadBalancerConfigurator cfgtr = new HAProxyConfigurator();
        String[] config = cfgtr.generateConfiguration(cmd);
        String[][] rules = cfgtr.generateFwRules(cmd);
        String tmpCfgFilePath = "/tmp/" + routerIp.replace('.', '_') + ".cfg";
        String tmpCfgFileContents = "";
        for (int i = 0; i < config.length; i++) {
            tmpCfgFileContents += config[i];
            tmpCfgFileContents += "\n";
        }

        String result = callHostPlugin(conn, "vmops", "createFile", "filepath", tmpCfgFilePath, "filecontents", tmpCfgFileContents);

        if (result == null || result.isEmpty()) {
            return new Answer(cmd, false, "LoadBalancerConfigCommand failed to create HA proxy cfg file.");
        }

        String[] addRules = rules[LoadBalancerConfigurator.ADD];
        String[] removeRules = rules[LoadBalancerConfigurator.REMOVE];

        String args = "";
        args += "-i " + routerIp;
        args += " -f " + tmpCfgFilePath;

        StringBuilder sb = new StringBuilder();
        if (addRules.length > 0) {
            for (int i = 0; i < addRules.length; i++) {
                sb.append(addRules[i]).append(',');
            }

            args += " -a " + sb.toString();
        }

        sb = new StringBuilder();
        if (removeRules.length > 0) {
            for (int i = 0; i < removeRules.length; i++) {
                sb.append(removeRules[i]).append(',');
            }

            args += " -d " + sb.toString();
        }

        result = callHostPlugin(conn, "vmops", "setLoadBalancerRule", "args", args);

        if (result == null || result.isEmpty()) {
            return new Answer(cmd, false, "LoadBalancerConfigCommand failed");
        }

        callHostPlugin(conn, "vmops", "deleteFile", "filepath", tmpCfgFilePath);

        return new Answer(cmd);
    }

    protected synchronized Answer execute(final DhcpEntryCommand cmd) {
        Connection conn = getConnection();
        String args = "-r " + cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        args += " -v " + cmd.getVmIpAddress();
        args += " -m " + cmd.getVmMac();
        args += " -n " + cmd.getVmName();
        String result = callHostPlugin(conn, "vmops", "saveDhcpEntry", "args", args);
        if (result == null || result.isEmpty()) {
            return new Answer(cmd, false, "DhcpEntry failed");
        }
        return new Answer(cmd);
    }
    
    protected synchronized Answer execute(final RemoteAccessVpnCfgCommand cmd) {
        Connection conn = getConnection();
        String args = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        if (cmd.isCreate()) {
        	args += " -r " + cmd.getIpRange();
        	args += " -p " + cmd.getPresharedKey();
        	args += " -s " + cmd.getVpnServerIp();
        	args += " -l " + cmd.getLocalIp();
        	args += " -c ";
        	
        } else {
        	args += " -d ";
        	args += " -s " + cmd.getVpnServerIp();
        }
        String result = callHostPlugin(conn, "vmops", "lt2p_vpn", "args", args);
    	if (result == null || result.isEmpty()) {
    		return new Answer(cmd, false, "Configure VPN failed");
    	}
    	return new Answer(cmd);
    }
    
    protected synchronized Answer execute(final VpnUsersCfgCommand cmd) {
        Connection conn = getConnection();
        for (VpnUsersCfgCommand.UsernamePassword userpwd: cmd.getUserpwds()) {
            String args = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        	if (!userpwd.isAdd()) {
        		args += " -U " + userpwd.getUsername();
        	} else {
        		args += " -u " + userpwd.getUsernamePassword();
        	}
        	String result = callHostPlugin(conn, "vmops", "lt2p_vpn", "args", args);
        	if (result == null || result.isEmpty()) {
        		return new Answer(cmd, false, "Configure VPN user failed for user " + userpwd.getUsername());
        	}
        }
        
    	return new Answer(cmd);
    }

    protected Answer execute(final VmDataCommand cmd) {
        Connection conn = getConnection();
        String routerPrivateIpAddress = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        String vmIpAddress = cmd.getVmIpAddress();
        List<String[]> vmData = cmd.getVmData();
        String[] vmDataArgs = new String[vmData.size() * 2 + 4];
        vmDataArgs[0] = "routerIP";
        vmDataArgs[1] = routerPrivateIpAddress;
        vmDataArgs[2] = "vmIP";
        vmDataArgs[3] = vmIpAddress;
        int i = 4;
        for (String[] vmDataEntry : vmData) {
            String folder = vmDataEntry[0];
            String file = vmDataEntry[1];
            String contents = (vmDataEntry[2] != null) ? vmDataEntry[2] : "none";

            vmDataArgs[i] = folder + "," + file;
            vmDataArgs[i + 1] = contents;
            i += 2;
        }

        String result = callHostPlugin(conn, "vmops", "vm_data", vmDataArgs);

        if (result == null || result.isEmpty()) {
            return new Answer(cmd, false, "vm_data failed");
        } else {
            return new Answer(cmd);
        }

    }

    protected Answer execute(final SavePasswordCommand cmd) {
        Connection conn = getConnection();
        final String password = cmd.getPassword();
        final String routerPrivateIPAddress = cmd.getRouterPrivateIpAddress();
        final String vmName = cmd.getVmName();
        final String vmIpAddress = cmd.getVmIpAddress();
        final String local = vmName;

        // Run save_password_to_domr.sh
        String args = "-r " + routerPrivateIPAddress;
        args += " -v " + vmIpAddress;
        args += " -p " + password;
        args += " " + local;
        String result = callHostPlugin(conn, "vmops", "savePassword", "args", args);

        if (result == null || result.isEmpty()) {
            return new Answer(cmd, false, "savePassword failed");
        }
        return new Answer(cmd);
    }

    protected void assignPublicIpAddress(Connection conn, final String vmName, final String privateIpAddress, final String publicIpAddress, final boolean add, final boolean firstIP,
            final boolean sourceNat, final String vlanId, final String vlanGateway, final String vlanNetmask, final String vifMacAddress, String guestIp) throws InternalErrorException {

        try {
            VM router = getVM(conn, vmName);

            // Determine the correct VIF on DomR to associate/disassociate the
            // IP address with
            VIF correctVif = getCorrectVif(conn, router, vlanId);

            // If we are associating an IP address and DomR doesn't have a VIF
            // for the specified vlan ID, we need to add a VIF
            // If we are disassociating the last IP address in the VLAN, we need
            // to remove a VIF
            boolean addVif = false;
            boolean removeVif = false;
            if (add && correctVif == null) {
                addVif = true;
            } else if (!add && firstIP) {
                removeVif = true;
            }

            if (addVif) {
                // Add a new VIF to DomR
                String vifDeviceNum = getLowestAvailableVIFDeviceNum(conn, router);

                if (vifDeviceNum == null) {
                    throw new InternalErrorException("There were no more available slots for a new VIF on router: " + router.getNameLabel(conn));
                }
                
                NicTO nic = new NicTO();
                nic.setMac(vifMacAddress);
                nic.setType(TrafficType.Public);
                if (vlanId == null) {
                    nic.setBroadcastType(BroadcastDomainType.Native);
                } else {
                    nic.setBroadcastType(BroadcastDomainType.Vlan);
                    nic.setBroadcastUri(BroadcastDomainType.Vlan.toUri(vlanId));
                }
                nic.setDeviceId(Integer.parseInt(vifDeviceNum));
                nic.setNetworkRateMbps(200);
                
                correctVif = createVif(conn, vmName, router, nic);
                correctVif.plug(conn);
                // Add iptables rule for network usage
                networkUsage(conn, privateIpAddress, "addVif", "eth" + correctVif.getDevice(conn));
            }

            if (correctVif == null) {
                throw new InternalErrorException("Failed to find DomR VIF to associate/disassociate IP with.");
            }

            String args = null;
            
            if (add) {
                args = "-A";
            } else {
                args = "-D";
            }
            String cidrSize = Long.toString(NetUtils.getCidrSize(vlanNetmask));
            if (sourceNat) {
                args += " -f";
                args += " -l ";
                args += publicIpAddress + "/" + cidrSize;
            } else if (firstIP) {
            	args += " -l ";
                args += publicIpAddress + "/" + cidrSize;
            } else {
              	args += " -l ";
                args += publicIpAddress;
            }
            args += " -i ";
            args += privateIpAddress;
            args += " -c ";
            args += "eth" + correctVif.getDevice(conn);
            args += " -g ";
            args += vlanGateway;

            if(guestIp!=null){
            	args += " -G ";
            	args += guestIp;
            }
            
            String result = callHostPlugin(conn, "vmops", "ipassoc", "args", args);
            if (result == null || result.isEmpty()) {
                throw new InternalErrorException("Xen plugin \"ipassoc\" failed.");
            }

            if (removeVif) {
                Network network = correctVif.getNetwork(conn);

                // Mark this vif to be removed from network usage
                networkUsage(conn, privateIpAddress, "deleteVif", "eth" + correctVif.getDevice(conn));

                // Remove the VIF from DomR
                correctVif.unplug(conn);
                correctVif.destroy(conn);

                // Disable the VLAN network if necessary
                disableVlanNetwork(conn, network);
            }

        } catch (XenAPIException e) {
            String msg = "Unable to assign public IP address due to " + e.toString();
            s_logger.warn(msg, e);
            throw new InternalErrorException(msg);
        } catch (final XmlRpcException e) {
            String msg = "Unable to assign public IP address due to " + e.getMessage();
            s_logger.warn(msg, e);
            throw new InternalErrorException(msg);
        }
    }

    protected String networkUsage(Connection conn, final String privateIpAddress, final String option, final String vif) {

        if (option.equals("get")) {
            return "0:0";
        }
        return null;
    }

    protected Answer execute(final IPAssocCommand cmd) {
        Connection conn = getConnection();
        String[] results = new String[cmd.getIpAddresses().length];
        int i = 0;
        String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        try {
            IpAddressTO[] ips = cmd.getIpAddresses(); 
            for (IpAddressTO ip : ips) {
                
                assignPublicIpAddress(conn, routerName, routerIp, ip.getPublicIp(), ip.isAdd(), ip.isFirstIP(), ip.isSourceNat(), ip.getVlanId(),
                        ip.getVlanGateway(), ip.getVlanNetmask(), ip.getVifMacAddress(), ip.getGuestIp());
                results[i++] = ip.getPublicIp() + " - success";
            }
        } catch (InternalErrorException e) {
            s_logger.error(
                    "Ip Assoc failure on applying one ip due to exception:  ", e);
            results[i++] = IpAssocAnswer.errorResult;
        }

        return new IpAssocAnswer(cmd, results);
    }
    
    protected GetVncPortAnswer execute(GetVncPortCommand cmd) {
        Connection conn = getConnection();
        try {
            Set<VM> vms = VM.getByNameLabel(conn, cmd.getName());
            if(vms.size() == 1) {
                return new GetVncPortAnswer(cmd, getVncPort(conn, vms.iterator().next()));
            } else {
                return new GetVncPortAnswer(cmd, "There are " + vms.size() + " VMs named " + cmd.getName());
            }
        } catch (Exception e) {
            String msg = "Unable to get vnc port due to " + e.toString();
            s_logger.warn(msg, e);
            return new GetVncPortAnswer(cmd, msg);
        }
    }

    protected Storage.StorageResourceType getStorageResourceType() {
        return Storage.StorageResourceType.STORAGE_POOL;
    }

    protected CheckHealthAnswer execute(CheckHealthCommand cmd) {
        boolean result = pingxenserver();
        return new CheckHealthAnswer(cmd, result);
    }


    protected long[] getNetworkStats(Connection conn, String privateIP) {
        String result = networkUsage(conn, privateIP, "get", null);
        long[] stats = new long[2];
        if (result != null) {
            String[] splitResult = result.split(":");
            int i = 0;
            while (i < splitResult.length - 1) {
                stats[0] += (new Long(splitResult[i++])).longValue();
                stats[1] += (new Long(splitResult[i++])).longValue();
            }
        }
        return stats;
    }

    /**
     * This is the method called for getting the HOST stats
     * 
     * @param cmd
     * @return
     */
    protected GetHostStatsAnswer execute(GetHostStatsCommand cmd) {
        Connection conn = getConnection();
        try {
            HostStatsEntry hostStats = getHostStats(conn, cmd, cmd.getHostGuid(), cmd.getHostId());
            return new GetHostStatsAnswer(cmd, hostStats);
        } catch (Exception e) {
            String msg = "Unable to get Host stats" + e.toString();
            s_logger.warn(msg, e);
            return new GetHostStatsAnswer(cmd, null);
        }
    }

    protected HostStatsEntry getHostStats(Connection conn, GetHostStatsCommand cmd, String hostGuid, long hostId) {

        HostStatsEntry hostStats = new HostStatsEntry(hostId, 0, 0, 0, "host", 0, 0, 0, 0);
        Object[] rrdData = getRRDData(conn, 1); // call rrd method with 1 for host

        if (rrdData == null) {
            return null;
        }

        Integer numRows = (Integer) rrdData[0];
        Integer numColumns = (Integer) rrdData[1];
        Node legend = (Node) rrdData[2];
        Node dataNode = (Node) rrdData[3];

        NodeList legendChildren = legend.getChildNodes();
        for (int col = 0; col < numColumns; col++) {

            if (legendChildren == null || legendChildren.item(col) == null) {
                continue;
            }

            String columnMetadata = getXMLNodeValue(legendChildren.item(col));

            if (columnMetadata == null) {
                continue;
            }

            String[] columnMetadataList = columnMetadata.split(":");

            if (columnMetadataList.length != 4) {
                continue;
            }

            String type = columnMetadataList[1];
            String param = columnMetadataList[3];

            if (type.equalsIgnoreCase("host")) {

                if (param.contains("pif_eth0_rx")) {
                    hostStats.setNetworkReadKBs(getDataAverage(dataNode, col, numRows));
                }

                if (param.contains("pif_eth0_tx")) {
                    hostStats.setNetworkWriteKBs(getDataAverage(dataNode, col, numRows));
                }

                if (param.contains("memory_total_kib")) {
                    hostStats.setTotalMemoryKBs(getDataAverage(dataNode, col, numRows));
                }

                if (param.contains("memory_free_kib")) {
                    hostStats.setFreeMemoryKBs(getDataAverage(dataNode, col, numRows));
                }

                if (param.contains("cpu")) {
                    // hostStats.setNumCpus(hostStats.getNumCpus() + 1);
                    hostStats.setCpuUtilization(hostStats.getCpuUtilization() + getDataAverage(dataNode, col, numRows));
                }

/*                
                if (param.contains("loadavg")) {
                    hostStats.setAverageLoad((hostStats.getAverageLoad() + getDataAverage(dataNode, col, numRows)));
                }
*/                
            }
        }

        // add the host cpu utilization
/*        
        if (hostStats.getNumCpus() != 0) {
            hostStats.setCpuUtilization(hostStats.getCpuUtilization() / hostStats.getNumCpus());
            s_logger.debug("Host cpu utilization " + hostStats.getCpuUtilization());
        }
*/        

        return hostStats;
    }

    protected GetVmStatsAnswer execute( GetVmStatsCommand cmd) {
        Connection conn = getConnection();
        List<String> vmNames = cmd.getVmNames();
        HashMap<String, VmStatsEntry> vmStatsNameMap = new HashMap<String, VmStatsEntry>();
        if( vmNames.size() == 0 ) {
            return new GetVmStatsAnswer(cmd, vmStatsNameMap);
        }      
        try {

            // Determine the UUIDs of the requested VMs
            List<String> vmUUIDs = new ArrayList<String>();

            for (String vmName : vmNames) {
                VM vm = getVM(conn, vmName);
                vmUUIDs.add(vm.getUuid(conn));
            }

            HashMap<String, VmStatsEntry> vmStatsUUIDMap = getVmStats(conn, cmd, vmUUIDs, cmd.getHostGuid());
            if( vmStatsUUIDMap == null ) {
                return new GetVmStatsAnswer(cmd, vmStatsNameMap);
            }
          
            for (String vmUUID : vmStatsUUIDMap.keySet()) {
                vmStatsNameMap.put(vmNames.get(vmUUIDs.indexOf(vmUUID)), vmStatsUUIDMap.get(vmUUID));
            }

            return new GetVmStatsAnswer(cmd, vmStatsNameMap);
        } catch (XenAPIException e) {
            String msg = "Unable to get VM stats" + e.toString();
            s_logger.warn(msg, e);
            return new GetVmStatsAnswer(cmd, vmStatsNameMap);
        } catch (XmlRpcException e) {
            String msg = "Unable to get VM stats" + e.getMessage();
            s_logger.warn(msg, e);
            return new GetVmStatsAnswer(cmd, vmStatsNameMap);
        }
    }

    protected HashMap<String, VmStatsEntry> getVmStats(Connection conn, GetVmStatsCommand cmd, List<String> vmUUIDs, String hostGuid) {
        HashMap<String, VmStatsEntry> vmResponseMap = new HashMap<String, VmStatsEntry>();

        for (String vmUUID : vmUUIDs) {
            vmResponseMap.put(vmUUID, new VmStatsEntry(0, 0, 0, 0, "vm"));
        }

        Object[] rrdData = getRRDData(conn, 2); // call rrddata with 2 for vm

        if (rrdData == null) {
            return null;
        }

        Integer numRows = (Integer) rrdData[0];
        Integer numColumns = (Integer) rrdData[1];
        Node legend = (Node) rrdData[2];
        Node dataNode = (Node) rrdData[3];

        NodeList legendChildren = legend.getChildNodes();
        for (int col = 0; col < numColumns; col++) {

            if (legendChildren == null || legendChildren.item(col) == null) {
                continue;
            }

            String columnMetadata = getXMLNodeValue(legendChildren.item(col));

            if (columnMetadata == null) {
                continue;
            }

            String[] columnMetadataList = columnMetadata.split(":");

            if (columnMetadataList.length != 4) {
                continue;
            }

            String type = columnMetadataList[1];
            String uuid = columnMetadataList[2];
            String param = columnMetadataList[3];

            if (type.equals("vm") && vmResponseMap.keySet().contains(uuid)) {
                VmStatsEntry vmStatsAnswer = vmResponseMap.get(uuid);

                vmStatsAnswer.setEntityType("vm");

                if (param.contains("cpu")) {
                    vmStatsAnswer.setNumCPUs(vmStatsAnswer.getNumCPUs() + 1);
                    vmStatsAnswer.setCPUUtilization((vmStatsAnswer.getCPUUtilization() + getDataAverage(dataNode, col, numRows))*100);
                } else if (param.equals("vif_0_rx")) {
                	vmStatsAnswer.setNetworkReadKBs(getDataAverage(dataNode, col, numRows)/(8*2));
                } else if (param.equals("vif_0_tx")) {
                	vmStatsAnswer.setNetworkWriteKBs(getDataAverage(dataNode, col, numRows)/(8*2));
                }
            }

        }

        for (String vmUUID : vmResponseMap.keySet()) {
            VmStatsEntry vmStatsAnswer = vmResponseMap.get(vmUUID);

            if (vmStatsAnswer.getNumCPUs() != 0) {
                vmStatsAnswer.setCPUUtilization(vmStatsAnswer.getCPUUtilization() / vmStatsAnswer.getNumCPUs());
                s_logger.debug("Vm cpu utilization " + vmStatsAnswer.getCPUUtilization());
            }
        }

        return vmResponseMap;
    }

    protected Object[] getRRDData(Connection conn, int flag) {

        /*
         * Note: 1 => called from host, hence host stats 2 => called from vm, hence vm stats
         */
        String stats = "";

        try {
            if (flag == 1) {
                stats = getHostStatsRawXML(conn);
            }
            if (flag == 2) {
                stats = getVmStatsRawXML(conn);
            }
        } catch (Exception e1) {
            s_logger.warn("Error whilst collecting raw stats from plugin:" + e1);
            return null;
        }

        // s_logger.debug("The raw xml stream is:"+stats);
        // s_logger.debug("Length of raw xml is:"+stats.length());

        //stats are null when the host plugin call fails (host down state)
        if(stats == null) {
            return null;
        }
        
        StringReader statsReader = new StringReader(stats);
        InputSource statsSource = new InputSource(statsReader);

        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(statsSource);
        } catch (Exception e) {
        	s_logger.warn("Exception caught whilst processing the document via document factory:"+e);
        	return null;
        }

        if(doc==null){
        	s_logger.warn("Null document found after tryinh to parse the stats source");
        	return null;
        }
        
        NodeList firstLevelChildren = doc.getChildNodes();
        NodeList secondLevelChildren = (firstLevelChildren.item(0)).getChildNodes();
        Node metaNode = secondLevelChildren.item(0);
        Node dataNode = secondLevelChildren.item(1);

        Integer numRows = 0;
        Integer numColumns = 0;
        Node legend = null;
        NodeList metaNodeChildren = metaNode.getChildNodes();
        for (int i = 0; i < metaNodeChildren.getLength(); i++) {
            Node n = metaNodeChildren.item(i);
            if (n.getNodeName().equals("rows")) {
                numRows = Integer.valueOf(getXMLNodeValue(n));
            } else if (n.getNodeName().equals("columns")) {
                numColumns = Integer.valueOf(getXMLNodeValue(n));
            } else if (n.getNodeName().equals("legend")) {
                legend = n;
            }
        }

        return new Object[] { numRows, numColumns, legend, dataNode };
    }

    protected String getXMLNodeValue(Node n) {
        return n.getChildNodes().item(0).getNodeValue();
    }

    protected double getDataAverage(Node dataNode, int col, int numRows) {
        double value = 0;
        double dummy = 0;
        int numRowsUsed = 0;
        for (int row = 0; row < numRows; row++) {
            Node data = dataNode.getChildNodes().item(numRows - 1 - row).getChildNodes().item(col + 1);
            Double currentDataAsDouble = Double.valueOf(getXMLNodeValue(data));
            if (!currentDataAsDouble.equals(Double.NaN)) {
                numRowsUsed += 1;
                value += currentDataAsDouble;
            }
        }

        if(numRowsUsed == 0)
        {
        	if((!Double.isInfinite(value))&&(!Double.isNaN(value)))
        	{
        		return value;
        	}
        	else
        	{
        		s_logger.warn("Found an invalid value (infinity/NaN) in getDataAverage(), numRows=0");
        		return dummy;
        	}
        }
        else
        {
        	if((!Double.isInfinite(value/numRowsUsed))&&(!Double.isNaN(value/numRowsUsed)))
        	{
        		return (value/numRowsUsed);
        	}
        	else
        	{
        		s_logger.warn("Found an invalid value (infinity/NaN) in getDataAverage(), numRows>0");
        		return dummy;
        	}
        }	
        
    }

    protected String getHostStatsRawXML(Connection conn) {
        Date currentDate = new Date();
        String startTime = String.valueOf(currentDate.getTime() / 1000 - 1000);

        return callHostPlugin(conn, "vmops", "gethostvmstats", "collectHostStats", String.valueOf("true"), "consolidationFunction", _consolidationFunction, "interval", String
                .valueOf(_pollingIntervalInSeconds), "startTime", startTime);
    }

    protected String getVmStatsRawXML(Connection conn) {
        Date currentDate = new Date();
        String startTime = String.valueOf(currentDate.getTime() / 1000 - 1000);

        return callHostPlugin(conn, "vmops", "gethostvmstats", "collectHostStats", String.valueOf("false"), "consolidationFunction", _consolidationFunction, "interval", String
                .valueOf(_pollingIntervalInSeconds), "startTime", startTime);
    }

    protected State convertToState(Types.VmPowerState ps) {
        final State state = s_statesTable.get(ps);
        return state == null ? State.Unknown : state;
    }

    protected HashMap<String, State> getAllVms(Connection conn) {
        final HashMap<String, State> vmStates = new HashMap<String, State>();
        Set<VM> vms = null;
        for (int i = 0; i < 2; i++) {
            try {
                Host host = Host.getByUuid(conn, _host.uuid);
                vms = host.getResidentVMs(conn);
                break;
            } catch (final Throwable e) {
                s_logger.warn("Unable to get vms", e);
            }
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException ex) {

            }
        }
        if (vms == null) {
            return null;
        }
        for (VM vm : vms) {
            VM.Record record = null;
            for (int i = 0; i < 2; i++) {
                try {
                    record = vm.getRecord(conn);
                    break;
                } catch (XenAPIException e1) {
                    s_logger.debug("VM.getRecord failed on host:" + _host.uuid + " due to " + e1.toString());
                } catch (XmlRpcException e1) {
                    s_logger.debug("VM.getRecord failed on host:" + _host.uuid + " due to " + e1.getMessage());
                }
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException ex) {

                }
            }
            if (record == null) {
                continue;
            }
            if (record.isControlDomain || record.isASnapshot || record.isATemplate) {
                continue; // Skip DOM0
            }

            VmPowerState ps = record.powerState;
            final State state = convertToState(ps);
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("VM " + record.nameLabel + ": powerstate = " + ps + "; vm state=" + state.toString());
            }
            vmStates.put(record.nameLabel, state);
        }

        return vmStates;
    }

    protected State getVmState(Connection conn, final String vmName) {
        int retry = 3;
        while (retry-- > 0) {
            try {
                Set<VM> vms = VM.getByNameLabel(conn, vmName);
                for (final VM vm : vms) {
                    return convertToState(vm.getPowerState(conn));
                }
            } catch (final BadServerResponse e) {
                // There is a race condition within xen such that if a vm is
                // deleted and we
                // happen to ask for it, it throws this stupid response. So
                // if this happens,
                // we take a nap and try again which then avoids the race
                // condition because
                // the vm's information is now cleaned up by xen. The error
                // is as follows
                // com.xensource.xenapi.Types$BadServerResponse
                // [HANDLE_INVALID, VM,
                // 3dde93f9-c1df-55a7-2cde-55e1dce431ab]
                s_logger.info("Unable to get a vm PowerState due to " + e.toString() + ". We are retrying.  Count: " + retry);
                try {
                    Thread.sleep(3000);
                } catch (final InterruptedException ex) {

                }
            } catch (XenAPIException e) {
                String msg = "Unable to get a vm PowerState due to " + e.toString();
                s_logger.warn(msg, e);
                break;
            } catch (final XmlRpcException e) {
                String msg = "Unable to get a vm PowerState due to " + e.getMessage();
                s_logger.warn(msg, e);
                break;
            }
        }

        return State.Stopped;
    }

    protected CheckVirtualMachineAnswer execute(final CheckVirtualMachineCommand cmd) {
        Connection conn = getConnection();
        final String vmName = cmd.getVmName();
        final State state = getVmState(conn, vmName);
        Integer vncPort = null;
        if (state == State.Running) {
            synchronized (_vms) {
                _vms.put(vmName, State.Running);
            }
        }

        return new CheckVirtualMachineAnswer(cmd, state, vncPort);
    }

    protected PrepareForMigrationAnswer execute(PrepareForMigrationCommand cmd) {
        Connection conn = getConnection();
        
        VirtualMachineTO vm = cmd.getVirtualMachine();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Preparing host for migrating " + vm);
        }
        
        VolumeTO[] disks = vm.getDisks();
        NicTO[] nics = vm.getNics();
        try {
            for (VolumeTO disk : disks) {
                //TODO: Anthony will change this for handling ISO attached VMs.
                mount(conn, vm.getName(), disk);
            }
            
            for (NicTO nic : nics) {
                getNetwork(conn, nic);
            }
            synchronized (_vms) {
                _vms.put(vm.getName(), State.Migrating);
            }
            
            return new PrepareForMigrationAnswer(cmd);
        } catch (XenAPIException e) {
            s_logger.warn("Unable to prepare for migration ", e);
            return new PrepareForMigrationAnswer(cmd, e);
        } catch (XmlRpcException e) {
            s_logger.warn("Unable to prepare for migration ", e);
            return new PrepareForMigrationAnswer(cmd, e);
        }
        
        /*
         * 
         * String result = null;
         * 
         * List<VolumeVO> vols = cmd.getVolumes(); result = mountwithoutvdi(vols, cmd.getMappings()); if (result !=
         * null) { return new PrepareForMigrationAnswer(cmd, false, result); }
         */
//        final String vmName = cmd.getVmName();
//        try {
//            Set<Host> hosts = Host.getAll(conn);
//            // workaround before implementing xenserver pool
//            // no migration
//            if (hosts.size() <= 1) {
//                return new PrepareForMigrationAnswer(cmd, false, "not in a same xenserver pool");
//            }
//            // if the vm have CD
//            // 1. make iosSR shared
//            // 2. create pbd in target xenserver
//            SR sr = getISOSRbyVmName(conn, cmd.getVmName());
//            if (sr != null) {
//                Set<PBD> pbds = sr.getPBDs(conn);
//                boolean found = false;
//                for (PBD pbd : pbds) {
//                    if (Host.getByUuid(conn, _host.uuid).equals(pbd.getHost(conn))) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found) {
//                    sr.setShared(conn, true);
//                    PBD pbd = pbds.iterator().next();
//                    PBD.Record pbdr = new PBD.Record();
//                    pbdr.deviceConfig = pbd.getDeviceConfig(conn);
//                    pbdr.host = Host.getByUuid(conn, _host.uuid);
//                    pbdr.SR = sr;
//                    PBD newpbd = PBD.create(conn, pbdr);
//                    newpbd.plug(conn);
//                }
//            }
//            Set<VM> vms = VM.getByNameLabel(conn, vmName);
//            if (vms.size() != 1) {
//                String msg = "There are " + vms.size() + " " + vmName;
//                s_logger.warn(msg);
//                return new PrepareForMigrationAnswer(cmd, false, msg);
//            }
//
//            VM vm = vms.iterator().next();
//
//            // check network
//            Set<VIF> vifs = vm.getVIFs(conn);
//            for (VIF vif : vifs) {
//                Network network = vif.getNetwork(conn);
//                Set<PIF> pifs = network.getPIFs(conn);
//                long vlan = -1;
//                PIF npif = null;
//                for (PIF pif : pifs) {
//                    try {
//                        vlan = pif.getVLAN(conn);
//                        if (vlan != -1 ) {
//                            VLAN vland = pif.getVLANMasterOf(conn);
//                            npif = vland.getTaggedPIF(conn);
//                        }
//                        break;
//                    }catch (Exception e) {
//                        continue;
//                    }
//                }
//                if (npif == null)  {
//                    continue;
//                }
//                network = npif.getNetwork(conn);
//                String nwuuid = network.getUuid(conn);
//                
//                String pifuuid = null;
//                if(nwuuid.equalsIgnoreCase(_host.privateNetwork)) {
//                    pifuuid = _host.privatePif;
//                } else if(nwuuid.equalsIgnoreCase(_host.publicNetwork)) {
//                    pifuuid = _host.publicPif;
//                } else {
//                    continue;
//                }
//                Network vlanNetwork = enableVlanNetwork(conn, vlan, pifuuid);
//
//                if (vlanNetwork == null) {
//                    throw new InternalErrorException("Failed to enable VLAN network with tag: " + vlan);
//                }
//            }
//
//            synchronized (_vms) {
//                _vms.put(cmd.getVmName(), State.Migrating);
//            }
//            return new PrepareForMigrationAnswer(cmd, true, null);
//        } catch (Exception e) {
//            String msg = "catch exception " + e.getMessage();
//            s_logger.warn(msg, e);
//            return new PrepareForMigrationAnswer(cmd, false, msg);
//        }
    }

    public PrimaryStorageDownloadAnswer execute(final PrimaryStorageDownloadCommand cmd) {
        Connection conn = getConnection();
        SR tmpltsr = null;
        String tmplturl = cmd.getUrl();
        int index = tmplturl.lastIndexOf("/");
        String mountpoint = tmplturl.substring(0, index);
        String tmpltname = null;
        if (index < tmplturl.length() - 1) {
            tmpltname = tmplturl.substring(index + 1).replace(".vhd", "");
        }
        try {
            String pUuid = cmd.getPoolUuid();
            SR poolsr = null;
            Set<SR> srs = SR.getByNameLabel(conn, pUuid);
            if (srs.size() != 1) {
                String msg = "There are " + srs.size() + " SRs with same name: " + pUuid;
                s_logger.warn(msg);
                return new PrimaryStorageDownloadAnswer(msg);
            } else {
                poolsr = srs.iterator().next();
            }

            /* Does the template exist in primary storage pool? If yes, no copy */
            VDI vmtmpltvdi = null;
            VDI snapshotvdi = null;

            Set<VDI> vdis = VDI.getByNameLabel(conn, "Template " + cmd.getName());

            for (VDI vdi : vdis) {
                VDI.Record vdir = vdi.getRecord(conn);
                if (vdir.SR.equals(poolsr)) {
                    vmtmpltvdi = vdi;
                    break;
                }
            }
            if (vmtmpltvdi == null) {
                tmpltsr = createNfsSRbyURI(conn, new URI(mountpoint), false);
                tmpltsr.scan(conn);
                VDI tmpltvdi = null;

                if (tmpltname != null) {
                    tmpltvdi = getVDIbyUuid(conn, tmpltname);
                }
                if (tmpltvdi == null) {
                    vdis = tmpltsr.getVDIs(conn);
                    for (VDI vdi : vdis) {
                        tmpltvdi = vdi;
                        break;
                    }
                }
                if (tmpltvdi == null) {
                    String msg = "Unable to find template vdi on secondary storage" + "host:" + _host.uuid + "pool: " + tmplturl;
                    s_logger.warn(msg);
                    return new PrimaryStorageDownloadAnswer(msg);
                }
                vmtmpltvdi = cloudVDIcopy(conn, tmpltvdi, poolsr);
                snapshotvdi = vmtmpltvdi.snapshot(conn, new HashMap<String, String>());
                vmtmpltvdi.destroy(conn);
                try {
                    poolsr.scan(conn);
                } catch (Exception e) {
                }
                snapshotvdi.setNameLabel(conn, "Template " + cmd.getName());
                // vmtmpltvdi.setNameDescription(conn, cmd.getDescription());
                vmtmpltvdi = snapshotvdi;

            }
            // Determine the size of the template
            VDI.Record vdiRec = vmtmpltvdi.getRecord(conn);
            long phySize = vdiRec.physicalUtilisation;
            String uuid = vdiRec.uuid;
            String parentUuid = vdiRec.smConfig.get("vhd-parent");
            if( parentUuid != null ) {
                // base copy
                VDI parentVdi = getVDIbyUuid(conn, parentUuid);
                phySize += parentVdi.getPhysicalUtilisation(conn);
            }        
            return new PrimaryStorageDownloadAnswer(uuid, phySize);

        } catch (XenAPIException e) {
            String msg = "XenAPIException:" + e.toString() + "host:" + _host.uuid + "pool: " + tmplturl;
            s_logger.warn(msg, e);
            return new PrimaryStorageDownloadAnswer(msg);
        } catch (Exception e) {
            String msg = "XenAPIException:" + e.getMessage() + "host:" + _host.uuid + "pool: " + tmplturl;
            s_logger.warn(msg, e);
            return new PrimaryStorageDownloadAnswer(msg);
        } finally {
            removeSR(conn, tmpltsr);
        }

    }

    protected String removeSRSync(Connection conn, SR sr) {
        if (sr == null) {
            return null;
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(logX(sr, "Removing SR"));
        }
        long waittime = 0;
        try {
            Set<VDI> vdis = sr.getVDIs(conn);
            for (VDI vdi : vdis) {
                Map<java.lang.String, Types.VdiOperations> currentOperation = vdi.getCurrentOperations(conn);
                if (currentOperation == null || currentOperation.size() == 0) {
                    continue;
                }
                if (waittime >= 1800000) {
                    String msg = "This template is being used, try late time";
                    s_logger.warn(msg);
                    return msg;
                }
                waittime += 30000;
                try {
                    Thread.sleep(30000);
                } catch (final InterruptedException ex) {
                }
            }
            removeSR(conn, sr);
            return null;
        } catch (XenAPIException e) {
            s_logger.warn(logX(sr, "Unable to get current opertions " + e.toString()), e);
        } catch (XmlRpcException e) {
            s_logger.warn(logX(sr, "Unable to get current opertions " + e.getMessage()), e);
        }
        String msg = "Remove SR failed";
        s_logger.warn(msg);
        return msg;

    }

    protected void removeSR(Connection conn, SR sr) {
        if (sr == null) {
            return;
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(logX(sr, "Removing SR"));
        }

        for (int i = 0; i < 2; i++) {
            try {
                Set<VDI> vdis = sr.getVDIs(conn);
                for (VDI vdi : vdis) {
                    vdi.forget(conn);
                }
                Set<PBD> pbds = sr.getPBDs(conn);
                for (PBD pbd : pbds) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(logX(pbd, "Unplugging pbd"));
                    }
                    if (pbd.getCurrentlyAttached(conn)) {
                        pbd.unplug(conn);
                    }
                    pbd.destroy(conn);
                }

                pbds = sr.getPBDs(conn);
                if (pbds.size() == 0) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(logX(sr, "Forgetting"));
                    }
                    sr.forget(conn);
                    return;
                }

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(logX(sr, "There are still pbd attached"));
                    if (s_logger.isTraceEnabled()) {
                        for (PBD pbd : pbds) {
                            s_logger.trace(logX(pbd, " Still attached"));
                        }
                    }
                }
            } catch (XenAPIException e) {
                s_logger.debug(logX(sr, "Catch XenAPIException: " + e.toString()));
            } catch (XmlRpcException e) {
                s_logger.debug(logX(sr, "Catch Exception: " + e.getMessage()));
            }
        }
        s_logger.warn(logX(sr, "Unable to remove SR"));
    }
    
    private boolean isPVInstalled(Connection conn, VM vm) throws BadServerResponse, XenAPIException, XmlRpcException {
        VMGuestMetrics vmmetric = vm.getGuestMetrics(conn);
        if (isRefNull(vmmetric)) {
            return false;
        }
        Map<String, String> PVversion = vmmetric.getPVDriversVersion(conn);
        if (PVversion != null && PVversion.containsKey("major")) {
            return true;
        }
        return false;
    }

    protected MigrateAnswer execute(final MigrateCommand cmd) {
        Connection conn = getConnection();
        final String vmName = cmd.getVmName();
        State state = null;

        synchronized (_vms) {
            state = _vms.get(vmName);
            _vms.put(vmName, State.Stopping);
        }
        try {
            Set<VM> vms = VM.getByNameLabel(conn, vmName);

            String ipaddr = cmd.getDestinationIp();

            Set<Host> hosts = Host.getAll(conn);
            Host dsthost = null;
            for (Host host : hosts) {
                if (host.getAddress(conn).equals(ipaddr)) {
                    dsthost = host;
                    break;
                }
            }
            if ( dsthost == null ) {
                String msg = "Migration failed due to unable to find host " + ipaddr + " in XenServer pool " + _host.pool;
                s_logger.warn(msg);
                return new MigrateAnswer(cmd, false, msg, null);
            }
            for (VM vm : vms) {
                if (vm.getPVBootloader(conn).equals("pygrub") && !isPVInstalled(conn, vm)) {
                    // Only fake PV driver for PV kernel, the PV driver is installed, but XenServer doesn't think it is installed
                    String uuid = vm.getUuid(conn);
                    String result = callHostPlugin(conn, "vmops", "preparemigration", "uuid", uuid);
                    if (result == null || result.isEmpty()) {
                        return new MigrateAnswer(cmd, false, "migration failed due to preparemigration failed", null);
                    }
                    // check if pv version is successfully set up
                    int i = 0;
                    for (; i < 20; i++) {
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException ex) {
                        }
                        if( isPVInstalled(conn, vm) ) {
                            break;
                        }
                    }
                    if (i >= 20) {
                        s_logger.warn("Can not fake PV driver for " + vmName);
                    }
                }
                Set<VBD> vbds = vm.getVBDs(conn);
                for( VBD vbd : vbds) {
                    VBD.Record vbdRec = vbd.getRecord(conn);
                    if( vbdRec.type.equals(Types.VbdType.CD.toString()) && !vbdRec.empty ) {
                        vbd.eject(conn);
                        break;
                    }
                }
                try {
                    vm.poolMigrate(conn, dsthost, new HashMap<String, String>());
                } catch (Types.VmMissingPvDrivers e1) {
                    // if PV driver is missing, just shutdown the VM
                    s_logger.warn("VM " + vmName + " is stopped when trying to migrate it because PV driver is missing, Please install PV driver for this VM");                  
                    vm.hardShutdown(conn);                   
                }
                state = State.Stopping;
            }
            return new MigrateAnswer(cmd, true, "migration succeeded", null);
        } catch (Exception e) {
            String msg = "Catch Exception " + e.getClass().getName() + ": Migration failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new MigrateAnswer(cmd, false, msg, null);
        } finally {
            synchronized (_vms) {
                _vms.put(vmName, state);
            }
        }

    }

    protected State getRealPowerState(Connection conn, String label) {
        int i = 0;
        s_logger.trace("Checking on the HALTED State");
        for (; i < 20; i++) {
            try {
                Set<VM> vms = VM.getByNameLabel(conn, label);
                if (vms == null || vms.size() == 0) {
                    continue;
                }

                VM vm = vms.iterator().next();

                VmPowerState vps = vm.getPowerState(conn);
                if (vps != null && vps != VmPowerState.HALTED && vps != VmPowerState.UNRECOGNIZED) {
                    return convertToState(vps);
                }
            } catch (XenAPIException e) {
                String msg = "Unable to get real power state due to " + e.toString();
                s_logger.warn(msg, e);
            } catch (XmlRpcException e) {
                String msg = "Unable to get real power state due to " + e.getMessage();
                s_logger.warn(msg, e);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        return State.Stopped;
    }

    protected Pair<VM, VM.Record> getControlDomain(Connection conn) throws XenAPIException, XmlRpcException {
        Host host = Host.getByUuid(conn, _host.uuid);
        Set<VM> vms = null;
        vms = host.getResidentVMs(conn);
        for (VM vm : vms) {
            if (vm.getIsControlDomain(conn)) {
                return new Pair<VM, VM.Record>(vm, vm.getRecord(conn));
            }
        }

        throw new CloudRuntimeException("Com'on no control domain?  What the crap?!#@!##$@");
    }

    protected HashMap<String, State> sync(Connection conn) {
        HashMap<String, State> newStates;
        HashMap<String, State> oldStates = null;

        final HashMap<String, State> changes = new HashMap<String, State>();

        synchronized (_vms) {
            newStates = getAllVms(conn);
            if (newStates == null) {
                s_logger.debug("Unable to get the vm states so no state sync at this point.");
                return null;
            }

            oldStates = new HashMap<String, State>(_vms.size());
            oldStates.putAll(_vms);

            for (final Map.Entry<String, State> entry : newStates.entrySet()) {
                final String vm = entry.getKey();

                State newState = entry.getValue();
                final State oldState = oldStates.remove(vm);

                if (newState == State.Stopped && oldState != State.Stopping && oldState != null && oldState != State.Stopped) {
                    newState = getRealPowerState(conn, vm);
                }

                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("VM " + vm + ": xen has state " + newState + " and we have state " + (oldState != null ? oldState.toString() : "null"));
                }

                if (vm.startsWith("migrating")) {
                    s_logger.debug("Migrating from xen detected.  Skipping");
                    continue;
                }
                if (oldState == null) {
                    _vms.put(vm, newState);
                    s_logger.debug("Detecting a new state but couldn't find a old state so adding it to the changes: " + vm);
                    changes.put(vm, newState);
                } else if (oldState == State.Starting) {
                    if (newState == State.Running) {
                        _vms.put(vm, newState);
                    } else if (newState == State.Stopped) {
                        s_logger.debug("Ignoring vm " + vm + " because of a lag in starting the vm.");
                    }
                } else if (oldState == State.Migrating) {
                    if (newState == State.Running) {
                        s_logger.debug("Detected that an migrating VM is now running: " + vm);
                        _vms.put(vm, newState);
                    }
                } else if (oldState == State.Stopping) {
                    if (newState == State.Stopped) {
                        _vms.put(vm, newState);
                    } else if (newState == State.Running) {
                        s_logger.debug("Ignoring vm " + vm + " because of a lag in stopping the vm. ");
                    }
                } else if (oldState != newState) {
                    _vms.put(vm, newState);
                    if (newState == State.Stopped) {
                        /*
                         * if (_vmsKilled.remove(vm)) { s_logger.debug("VM " + vm + " has been killed for storage. ");
                         * newState = State.Error; }
                         */
                    }
                    changes.put(vm, newState);
                }
            }

            for (final Map.Entry<String, State> entry : oldStates.entrySet()) {
                final String vm = entry.getKey();
                final State oldState = entry.getValue();

                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("VM " + vm + " is now missing from xen so reporting stopped");
                }

                if (oldState == State.Stopping) {
                    s_logger.debug("Ignoring VM " + vm + " in transition state stopping.");
                    _vms.remove(vm);
                } else if (oldState == State.Starting) {
                    s_logger.debug("Ignoring VM " + vm + " in transition state starting.");
                } else if (oldState == State.Stopped) {
                    _vms.remove(vm);
                } else if (oldState == State.Migrating) {
                    s_logger.debug("Ignoring VM " + vm + " in migrating state.");
                } else {
                    State newState = State.Stopped;
                    try {
                        Set<VM> missingVMs = VM.getByNameLabel(conn, vm);
                        if( missingVMs != null && !missingVMs.isEmpty()) {
                            String shutdownInitiator = missingVMs.iterator().next().getOtherConfig(conn).get("last_shutdown_initiator");
                            if( shutdownInitiator != null && shutdownInitiator.equals("internal")) {
                                newState = State.Shutdowned;
                            }
                        }
                    } catch (Exception e) {
                    }
                    /*
                     * if (_vmsKilled.remove(entry.getKey())) { s_logger.debug("VM " + vm +
                     * " has been killed by storage monitor"); state = State.Error; }
                     */
                    changes.put(entry.getKey(), newState);
                }
            }
        }

        return changes;
    }

    protected ReadyAnswer execute(ReadyCommand cmd) {
        Connection conn = getConnection();
        Long dcId = cmd.getDataCenterId();
        // Ignore the result of the callHostPlugin. Even if unmounting the
        // snapshots dir fails, let Ready command
        // succeed.
        callHostPlugin(conn, "vmopsSnapshot", "unmountSnapshotsDir", "dcId", dcId.toString());
       
        _localGateway = callHostPlugin(conn, "vmops", "getgateway", "mgmtIP", _host.ip);
        if (_localGateway == null || _localGateway.isEmpty()) {
            String msg = "can not get gateway for host :" + _host.uuid;
            s_logger.warn(msg);
            return new ReadyAnswer(cmd, msg);
        }
        return new ReadyAnswer(cmd);
    }

    //
    // using synchronized on VM name in the caller does not prevent multiple
    // commands being sent against
    // the same VM, there will be a race condition here in finally clause and
    // the main block if
    // there are multiple requests going on
    //
    // Therefore, a lazy solution is to add a synchronized guard here
    protected int getVncPort(Connection conn, VM vm) {
        VM.Record record;
        try {
            record = vm.getRecord(conn);
        } catch (XenAPIException e) {
            String msg = "Unable to get vnc-port due to " + e.toString();
            s_logger.warn(msg, e);
            return -1;
        } catch (XmlRpcException e) {
            String msg = "Unable to get vnc-port due to " + e.getMessage();
            s_logger.warn(msg, e);
            return -1;
        }
        String hvm = "true";
        if (record.HVMBootPolicy.isEmpty()) {
            hvm = "false";
        }

        String vncport = callHostPlugin(conn, "vmops", "getvncport", "domID", record.domid.toString(), "hvm", hvm);
        if (vncport == null || vncport.isEmpty()) {
            return -1;
        }

        vncport = vncport.replace("\n", "");
        return NumbersUtil.parseInt(vncport, -1);
    }

    protected Answer execute(final RebootCommand cmd) {
        Connection conn = getConnection();
        synchronized (_vms) {
            _vms.put(cmd.getVmName(), State.Starting);
        }
        try {
            Set<VM> vms = null;
            try {
                vms = VM.getByNameLabel(conn, cmd.getVmName());
            } catch (XenAPIException e0) {
                s_logger.debug("getByNameLabel failed " + e0.toString());
                return new RebootAnswer(cmd, "getByNameLabel failed " + e0.toString());
            } catch (Exception e0) {
                s_logger.debug("getByNameLabel failed " + e0.getMessage());
                return new RebootAnswer(cmd, "getByNameLabel failed");
            }
            for (VM vm : vms) {
                try {
                    rebootVM(conn, vm, vm.getNameLabel(conn));
                } catch (Exception e) {
                    String msg = e.toString();
                    s_logger.warn(msg, e);
                    return new RebootAnswer(cmd, msg);
                }
            }
            return new RebootAnswer(cmd, "reboot succeeded", null, null);
        } finally {
            synchronized (_vms) {
                _vms.put(cmd.getVmName(), State.Running);
            }
        }
    }

    protected Answer execute(RebootRouterCommand cmd) {
        Connection conn = getConnection();
        Long bytesSent = 0L;
        Long bytesRcvd = 0L;
        if (VirtualMachineName.isValidRouterName(cmd.getVmName())) {
            long[] stats = getNetworkStats(conn, cmd.getPrivateIpAddress());
            bytesSent = stats[0];
            bytesRcvd = stats[1];
        }
        RebootAnswer answer = (RebootAnswer) execute((RebootCommand) cmd);
        answer.setBytesSent(bytesSent);
        answer.setBytesReceived(bytesRcvd);
        if (answer.getResult()) {
            String cnct = connect(conn, cmd.getVmName(), cmd.getPrivateIpAddress());
            networkUsage(conn, cmd.getPrivateIpAddress(), "create", null);
            if (cnct == null) {
                return answer;
            } else {
                return new Answer(cmd, false, cnct);
            }
        }
        return answer;
    }



    protected void startvmfailhandle(Connection conn, VM vm, List<Ternary<SR, VDI, VolumeVO>> mounts) {
        if (vm != null) {
            try {

                if (vm.getPowerState(conn) == VmPowerState.RUNNING) {
                    try {
                        vm.hardShutdown(conn);
                    } catch (Exception e) {
                        String msg = "VM hardshutdown failed due to " + e.toString();
                        s_logger.warn(msg);
                    }
                }
                if (vm.getPowerState(conn) == VmPowerState.HALTED) {
                    try {
                        vm.destroy(conn);
                    } catch (Exception e) {
                        String msg = "VM destroy failed due to " + e.toString();
                        s_logger.warn(msg);
                    }
                }
            } catch (Exception e) {
                String msg = "VM getPowerState failed due to " + e.toString();
                s_logger.warn(msg);
            }
        }
        if (mounts != null) {
            for (Ternary<SR, VDI, VolumeVO> mount : mounts) {
                VDI vdi = mount.second();
                Set<VBD> vbds = null;
                try {
                    vbds = vdi.getVBDs(conn);
                } catch (Exception e) {
                    String msg = "VDI getVBDS failed due to " + e.toString();
                    s_logger.warn(msg);
                    continue;
                }
                for (VBD vbd : vbds) {
                    try {
                        vbd.unplug(conn);
                        vbd.destroy(conn);
                    } catch (Exception e) {
                        String msg = "VBD destroy failed due to " + e.toString();
                        s_logger.warn(msg);
                    }
                }
            }
        }
    }

    protected void setMemory(Connection conn, VM vm, long memsize) throws XmlRpcException, XenAPIException {
        vm.setMemoryStaticMin(conn, memsize);
        vm.setMemoryDynamicMin(conn, memsize);

        vm.setMemoryDynamicMax(conn, memsize);
        vm.setMemoryStaticMax(conn, memsize);
    }

    void shutdownVM(Connection conn, VM vm, String vmName) throws XmlRpcException {
        try {      
            vm.cleanShutdown(conn);
        } catch (Types.XenAPIException e) {
            s_logger.debug("Unable to cleanShutdown VM(" + vmName + ") on host(" + _host.uuid +") due to " + e.toString() + ", try hard shutdown");
            try {
                vm.hardShutdown(conn);
            } catch (Exception e1) {
                String msg = "Unable to hardShutdown VM(" + vmName + ") on host(" + _host.uuid +") due to " + e.toString();
                s_logger.warn(msg, e1);
                throw new CloudRuntimeException(msg);
            }
        }
    }
    
    void rebootVM(Connection conn, VM vm, String vmName) throws XmlRpcException {
        try {
            vm.cleanReboot(conn);
        } catch (XenAPIException e) {
            s_logger.debug("Unable to Clean Reboot VM(" + vmName + ") on host(" + _host.uuid +") due to " + e.toString() + ", try hard reboot");
            try {
                vm.hardReboot(conn);
            } catch (Exception e1) {
                String msg = "Unable to hard Reboot VM(" + vmName + ") on host(" + _host.uuid +") due to " + e.toString();
                s_logger.warn(msg, e1);
                throw new CloudRuntimeException(msg);
            }
        }
    }
    
    void startVM(Connection conn, Host host, VM vm, String vmName) throws XmlRpcException {
        try {
            vm.startOn(conn, host, false, true);
        } catch (Exception e) {
            String msg = "Unable to start VM(" + vmName + ") on host(" + _host.uuid +") due to " + e.toString();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg);
        }
    }
    
    protected StopAnswer execute(final StopCommand cmd) {
        Connection conn = getConnection();
        String vmName = cmd.getVmName();
        try {
            Set<VM> vms = VM.getByNameLabel(conn, vmName);
            // stop vm which is running on this host or is in halted state
            for (VM vm : vms) {
                VM.Record vmr = vm.getRecord(conn);
                if (vmr.powerState != VmPowerState.RUNNING) {
                    continue;
                }
                if (isRefNull(vmr.residentOn)) {
                    continue;
                }
                if (vmr.residentOn.getUuid(conn).equals(_host.uuid)) {
                    continue;
                }
                vms.remove(vm);
            }

            if (vms.size() == 0) {
                s_logger.warn("VM does not exist on XenServer" + _host.uuid);
                synchronized (_vms) {
                    _vms.remove(vmName);
                }
                return new StopAnswer(cmd, "VM does not exist", 0, 0L, 0L);
            }
            Long bytesSent = 0L;
            Long bytesRcvd = 0L;
            for (VM vm : vms) {
                VM.Record vmr = vm.getRecord(conn);

                if (vmr.isControlDomain) {
                    String msg = "Tring to Shutdown control domain";
                    s_logger.warn(msg);
                    return new StopAnswer(cmd, msg);
                }

                if (vmr.powerState == VmPowerState.RUNNING && !isRefNull(vmr.residentOn) && !vmr.residentOn.getUuid(conn).equals(_host.uuid)) {
                    String msg = "Stop Vm " + vmName + " failed due to this vm is not running on this host: " + _host.uuid + " but host:" + vmr.residentOn.getUuid(conn);
                    s_logger.warn(msg);
                    return new StopAnswer(cmd, msg);
                }

                State state = null;
                synchronized (_vms) {
                    state = _vms.get(vmName);
                    _vms.put(vmName, State.Stopping);
                }

                try {
                    if (vmr.powerState == VmPowerState.RUNNING) {
                        /* when stop a vm, set affinity to current xenserver */
                        vm.setAffinity(conn, vm.getResidentOn(conn));
                        if (VirtualMachineName.isValidRouterName(vmName)) {
                            if (cmd.getPrivateRouterIpAddress() != null) {
                                long[] stats = getNetworkStats(conn, cmd.getPrivateRouterIpAddress());
                                bytesSent = stats[0];
                                bytesRcvd = stats[1];
                            }
                        }
                        if (_canBridgeFirewall) {
                            String result = callHostPlugin(conn, "vmops", "destroy_network_rules_for_vm", "vmName", cmd
                                    .getVmName());
                            if (result == null || result.isEmpty() || !Boolean.parseBoolean(result)) {
                                s_logger.warn("Failed to remove  network rules for vm " + cmd.getVmName());
                            } else {
                                s_logger.info("Removed  network rules for vm " + cmd.getVmName());
                            }
                        }
                        shutdownVM(conn, vm, vmName);
                    }
                } catch (Exception e) {
                    String msg = "Catch exception " + e.getClass().getName() + " when stop VM:" + cmd.getVmName() + " due to " + e.toString();
                    s_logger.debug(msg);
                    return new StopAnswer(cmd, msg);
                } finally {

                    try {
                        if (vm.getPowerState(conn) == VmPowerState.HALTED) {
                            Set<VIF> vifs = vm.getVIFs(conn);
                            List<Network> networks = new ArrayList<Network>();
                            for (VIF vif : vifs) {
                                networks.add(vif.getNetwork(conn));
                            }
                            List<VDI> vdis = getVdis(conn, vm);
                            vm.destroy(conn);
                            for( VDI vdi : vdis ){
                                umount(conn, vdi);
                            }
                            state = State.Stopped;
                            SR sr = getISOSRbyVmName(conn, cmd.getVmName());
                            removeSR(conn, sr);
                            // Disable any VLAN networks that aren't used
                            // anymore
                            for (Network network : networks) {
                                if (network.getNameLabel(conn).startsWith("VLAN")) {
                                    disableVlanNetwork(conn, network);
                                }
                            }
                        }
                    } catch (XenAPIException e) {
                        String msg = "VM destroy failed in Stop " + vmName + " Command due to " + e.toString();
                        s_logger.warn(msg, e);
                    } catch (Exception e) {
                        String msg = "VM destroy failed in Stop " + vmName + " Command due to " + e.getMessage();
                        s_logger.warn(msg, e);
                    } finally {
                        synchronized (_vms) {
                            _vms.put(vmName, state);
                        }
                    }
                }
            }
            return new StopAnswer(cmd, "Stop VM " + vmName + " Succeed", 0, bytesSent, bytesRcvd);
        } catch (XenAPIException e) {
            String msg = "Stop Vm " + vmName + " fail due to " + e.toString();
            s_logger.warn(msg, e);
            return new StopAnswer(cmd, msg);
        } catch (XmlRpcException e) {
            String msg = "Stop Vm " + vmName + " fail due to " + e.getMessage();
            s_logger.warn(msg, e);
            return new StopAnswer(cmd, msg);
        }
    }

    private List<VDI> getVdis(Connection conn, VM vm) {
        List<VDI> vdis = new ArrayList<VDI>();
        try {
            Set<VBD> vbds =vm.getVBDs(conn);
            for( VBD vbd : vbds ) {
                vdis.add(vbd.getVDI(conn));
            }
        } catch (XenAPIException e) {
            String msg = "getVdis can not get VPD due to " + e.toString();
            s_logger.warn(msg, e);          
        } catch (XmlRpcException e) {
            String msg = "getVdis can not get VPD due to " + e.getMessage();
            s_logger.warn(msg, e);
        }    
        return vdis;
    }

    protected String connect(Connection conn, final String vmName, final String ipAddress, final int port) {
        for (int i = 0; i <= _retry; i++) {
            try {
                Set<VM> vms = VM.getByNameLabel(conn, vmName);
                if (vms.size() < 1) {
                    String msg = "VM " + vmName + " is not running";
                    s_logger.warn(msg);
                    return msg;
                }
            } catch (Exception e) {
                String msg = "VM.getByNameLabel " + vmName + " failed due to " + e.toString();
                s_logger.warn(msg, e);
                return msg;
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Trying to connect to " + ipAddress);
            }
            if (pingdomr(conn, ipAddress, Integer.toString(port))) {
                return null;
            }
            try {
                Thread.sleep(_sleep);
            } catch (final InterruptedException e) {
            }
        }
        String msg = "Timeout, Unable to logon to " + ipAddress;
        s_logger.debug(msg);

        return msg;
    }

    protected String connect(Connection conn, final String vmname, final String ipAddress) {
        return connect(conn, vmname, ipAddress, 3922);
    }

    protected boolean isDeviceUsed(Connection conn, VM vm, Long deviceId) {
        // Figure out the disk number to attach the VM to

        String msg = null;
        try {
            Set<String> allowedVBDDevices = vm.getAllowedVBDDevices(conn);
            if (allowedVBDDevices.contains(deviceId.toString())) {
                return false;
            }
            return true;
        } catch (XmlRpcException e) {
            msg = "Catch XmlRpcException due to: " + e.getMessage();
            s_logger.warn(msg, e);
        } catch (XenAPIException e) {
            msg = "Catch XenAPIException due to: " + e.toString();
            s_logger.warn(msg, e);
        }
        throw new CloudRuntimeException("When check deviceId " + msg);
    }

    
    protected String getUnusedDeviceNum(Connection conn, VM vm) {
        // Figure out the disk number to attach the VM to
        try {
            Set<String> allowedVBDDevices = vm.getAllowedVBDDevices(conn);
            if (allowedVBDDevices.size() == 0) {
                throw new CloudRuntimeException("Could not find an available slot in VM with name: " + vm.getNameLabel(conn) + " to attach a new disk.");
            }
            return allowedVBDDevices.iterator().next();
        } catch (XmlRpcException e) {
            String msg = "Catch XmlRpcException due to: " + e.getMessage();
            s_logger.warn(msg, e);
        } catch (XenAPIException e) {
            String msg = "Catch XenAPIException due to: " + e.toString();
            s_logger.warn(msg, e);
        }
        throw new CloudRuntimeException("Could not find an available slot in VM with name to attach a new disk.");
    }

    protected String callHostPlugin(Connection conn, String plugin, String cmd, String... params) {
        //default time out is 300 s
        return callHostPluginWithTimeOut(conn, plugin, cmd, 300, params);
    }

    protected String callHostPluginWithTimeOut(Connection conn, String plugin, String cmd, int timeout, String... params) {
        Map<String, String> args = new HashMap<String, String>();

        try {

            for (int i = 0; i < params.length; i += 2) {
                args.put(params[i], params[i + 1]);
            }

            if (s_logger.isTraceEnabled()) {
                s_logger.trace("callHostPlugin executing for command " + cmd + " with " + getArgsString(args));
            }
            Host host = Host.getByUuid(conn, _host.uuid);
            String result = host.callPlugin(conn, plugin, cmd, args);
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("callHostPlugin Result: " + result);
            }
            return result.replace("\n", "");
        } catch (XenAPIException e) {
            s_logger.warn("callHostPlugin failed for cmd: " + cmd + " with args " + getArgsString(args) + " due to " + e.toString());
        } catch (XmlRpcException e) {
            s_logger.debug("callHostPlugin failed for cmd: " + cmd + " with args " + getArgsString(args) + " due to " + e.getMessage());
        }
        return null;
    }

    protected String getArgsString(Map<String, String> args) {
        StringBuilder argString = new StringBuilder();
        for (Map.Entry<String, String> arg : args.entrySet()) {
            argString.append(arg.getKey() + ": " + arg.getValue() + ", ");
        }
        return argString.toString();
    }

    protected boolean setIptables(Connection conn) {
        String result = callHostPlugin(conn, "vmops", "setIptables");
        if (result == null || result.isEmpty()) {
            return false;
        }
        return true;
    }

    protected Nic getManageMentNetwork(Connection conn) throws XmlRpcException, XenAPIException {       
        PIF mgmtPif = null;
        PIF.Record mgmtPifRec = null;
        Host host = Host.getByUuid(conn, _host.uuid);
        Set<PIF> hostPifs = host.getPIFs(conn);
        for (PIF pif : hostPifs) {
            PIF.Record rec = pif.getRecord(conn);
            if (rec.management) {
                if (rec.VLAN != null && rec.VLAN != -1) {
                    String msg = new StringBuilder("Unsupported configuration.  Management network is on a VLAN.  host=").append(_host.uuid).append("; pif=").append(rec.uuid)
                            .append("; vlan=").append(rec.VLAN).toString();
                    s_logger.warn(msg);
                    throw new CloudRuntimeException(msg);
                }
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Management network is on pif=" + rec.uuid);
                }
                mgmtPif = pif;
                mgmtPifRec = rec;
                break;
            }
        }
        if (mgmtPif == null) {
            String msg = "Unable to find management network for " + _host.uuid;
            s_logger.warn(msg);
            throw new CloudRuntimeException(msg);
        }
        Bond bond = mgmtPifRec.bondSlaveOf;
        if ( !isRefNull(bond) ) {
            String msg = "Management interface is on slave(" +mgmtPifRec.uuid + ") of bond("
                + bond.getUuid(conn) + ") on host(" +_host.uuid + "), please move management interface to bond!";
            s_logger.warn(msg);
            throw new CloudRuntimeException(msg);
        }
        Network nk =  mgmtPifRec.network;
        Network.Record nkRec = nk.getRecord(conn);
        return new Nic(nk, nkRec, mgmtPif, mgmtPifRec);
    }
    
    
    protected Nic getLocalNetwork(Connection conn, String name) throws XmlRpcException, XenAPIException {
        Set<Network> networks = Network.getByNameLabel(conn, name);
        for (Network network : networks) {
            Network.Record nr = network.getRecord(conn);
            for (PIF pif : nr.PIFs) {
                PIF.Record pr = pif.getRecord(conn);
                if (_host.uuid.equals(pr.host.getUuid(conn))) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Found a network called " + name + " on host=" + _host.ip + ";  Network="
                                + nr.uuid + "; pif=" + pr.uuid);
                    }
                    return new Nic(network, nr, pif, pr);
                }
            }
        }

        return null;
    }


    protected VIF getCorrectVif(Connection conn, VM router, String vlanId) {
        try {
            Set<VIF> routerVIFs = router.getVIFs(conn);
            for (VIF vif : routerVIFs) {
                Network vifNetwork = vif.getNetwork(conn);
                if (vlanId.equalsIgnoreCase(Vlan.UNTAGGED)) {
                    if (vifNetwork.getUuid(conn).equals(_host.publicNetwork)) {
                        return vif;
                    }
                } else {
                    if (vifNetwork.getNameLabel(conn).equals("VLAN" + vlanId)) {
                        return vif;
                    }
                }
            }
        } catch (XmlRpcException e) {
            String msg = "Caught XmlRpcException: " + e.getMessage();
            s_logger.warn(msg, e);
        } catch (XenAPIException e) {
            String msg = "Caught XenAPIException: " + e.toString();
            s_logger.warn(msg, e);
        }

        return null;
    }

    protected String getLowestAvailableVIFDeviceNum(Connection conn, VM vm) {
        try {
            Set<String> availableDeviceNums = vm.getAllowedVIFDevices(conn);
            Iterator<String> deviceNumsIterator = availableDeviceNums.iterator();
            List<Integer> sortedDeviceNums = new ArrayList<Integer>();

            while (deviceNumsIterator.hasNext()) {
                try {
                    sortedDeviceNums.add(Integer.valueOf(deviceNumsIterator.next()));
                } catch (NumberFormatException e) {
                    s_logger.debug("Obtained an invalid value for an available VIF device number for VM: " + vm.getNameLabel(conn));
                    return null;
                }
            }

            Collections.sort(sortedDeviceNums);
            return String.valueOf(sortedDeviceNums.get(0));
        } catch (XmlRpcException e) {
            String msg = "Caught XmlRpcException: " + e.getMessage();
            s_logger.warn(msg, e);
        } catch (XenAPIException e) {
            String msg = "Caught XenAPIException: " + e.toString();
            s_logger.warn(msg, e);
        }

        return null;
    }

    protected VDI mount(Connection conn, StoragePoolType pooltype, String volumeFolder, String volumePath) {
        return getVDIbyUuid(conn, volumePath);
    }
    
    protected Network getNetworkByName(Connection conn, String name) throws BadServerResponse, XenAPIException, XmlRpcException {
        Set<Network> networks = Network.getByNameLabel(conn, name);
        if (networks.size() > 0) {
            assert networks.size() == 1 : "How did we find more than one network with this name label" + name + "?  Strange....";
            return networks.iterator().next(); // Found it.
        }

        return null;
    }

    protected synchronized Network getNetworkByName(Connection conn, String name, boolean lookForPif) throws XenAPIException, XmlRpcException {
        Network found = null;
        Set<Network> networks = Network.getByNameLabel(conn, name);
        if (networks.size() == 1) {
            found = networks.iterator().next();
        } else if (networks.size() > 1) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Found more than one network with the name " + name);
            }
            for (Network network : networks) {
                if (!lookForPif) {
                    found = network;
                    break;
                }
                
                Network.Record netr = network.getRecord(conn);
                s_logger.debug("Checking network " + netr.uuid);
                if (netr.PIFs.size() == 0) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Network " + netr.uuid + " has no pifs so skipping that.");
                    }
                } else {
                    for (PIF pif : netr.PIFs) {
                        PIF.Record pifr = pif.getRecord(conn);
                        if (_host.uuid.equals(pifr.host.getUuid(conn))) {
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Network " + netr.uuid + " has a pif " + pifr.uuid + " for our host ");
                            }
                            found = network;
                            break;
                        }
                    }
                }
            }
        } 

        return found;
    }
    
    protected Network enableVlanNetwork(Connection conn, long tag, String pifUuid) throws XenAPIException, XmlRpcException {
        // In XenServer, vlan is added by
        // 1. creating a network.
        // 2. creating a vlan associating network with the pif.
        // We always create
        // 1. a network with VLAN[vlan id in decimal]
        // 2. a vlan associating the network created with the pif to private
        // network.
        Network vlanNetwork = null;
        String name = "VLAN" + Long.toString(tag);

        synchronized (name.intern()) {
            vlanNetwork = getNetworkByName(conn, name);
            if (vlanNetwork == null) { // Can't find it, then create it.
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Creating VLAN network for " + tag + " on host " + _host.ip);
                }
                Network.Record nwr = new Network.Record();
                nwr.nameLabel = name;
                nwr.bridge = name;
                vlanNetwork = Network.create(conn, nwr);
            }

            PIF nPif = PIF.getByUuid(conn, pifUuid);
            PIF.Record nPifr = nPif.getRecord(conn);

            Network.Record vlanNetworkr = vlanNetwork.getRecord(conn);
            if (vlanNetworkr.PIFs != null) {
                for (PIF pif : vlanNetworkr.PIFs) {
                    PIF.Record pifr = pif.getRecord(conn);
                    if(pifr.host.equals(nPifr.host)) {
                        if (pifr.device.equals(nPifr.device) ) {
                            pif.plug(conn);
                            return vlanNetwork;
                        } else {
                            throw new CloudRuntimeException("Creating VLAN " + tag + " on " + nPifr.device + " failed due to this VLAN is already created on " + pifr.device);                	
                        }
                        
                    }
                }
            }

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Creating VLAN " + tag + " on host " + _host.ip + " on device " + nPifr.device);
            }
            VLAN vlan = VLAN.create(conn, nPif, tag, vlanNetwork);
            PIF untaggedPif = vlan.getUntaggedPIF(conn);
            if (!untaggedPif.getCurrentlyAttached(conn)) {
                untaggedPif.plug(conn);
            }
        }

        return vlanNetwork;
    }

    protected Network enableVlanNetwork(Connection conn, long tag, Network network, String pifUuid) throws XenAPIException, XmlRpcException {
        // In XenServer, vlan is added by
        // 1. creating a network.
        // 2. creating a vlan associating network with the pif.
        // We always create
        // 1. a network with VLAN[vlan id in decimal]
        // 2. a vlan associating the network created with the pif to private
        // network.

        Network vlanNetwork = null;
        String name = "VLAN" + Long.toString(tag);

        vlanNetwork = getNetworkByName(conn, name, true);
        if (vlanNetwork == null) { // Can't find it, then create it.
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Creating VLAN network for " + tag + " on host " + _host.ip);
            }
            Network.Record nwr = new Network.Record();
            nwr.nameLabel = name;
            nwr.bridge = name;
            vlanNetwork = Network.create(conn, nwr);
        }

        PIF nPif = PIF.getByUuid(conn, pifUuid);
        PIF.Record nPifr = nPif.getRecord(conn);

        Network.Record vlanNetworkr = vlanNetwork.getRecord(conn);
        if (vlanNetworkr.PIFs != null) {
            for (PIF pif : vlanNetworkr.PIFs) {
                PIF.Record pifr = pif.getRecord(conn);
                if (pifr.device.equals(nPifr.device) && pifr.host.equals(nPifr.host)) {
                    return vlanNetwork;
                }
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating VLAN " + tag + " on host " + _host.ip + " on device " + nPifr.device);
        }
        VLAN vlan = VLAN.create(conn, nPif, tag, vlanNetwork);
        VLAN.Record vlanr = vlan.getRecord(conn);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("VLAN is created for " + tag + ".  The uuid is " + vlanr.uuid);
        }
        
        return vlanNetwork;
    }
    
    protected void disableVlanNetwork(Connection conn, Network network) {
    }

    protected SR getLocalLVMSR(Connection conn) {
        try {           
            Map<SR, SR.Record> map = SR.getAllRecords(conn);
            for (Map.Entry<SR, SR.Record> entry : map.entrySet()) {
                SR.Record srRec = entry.getValue();
                if (SRType.LVM.equals(srRec.type)) {
                    Set<PBD> pbds = srRec.PBDs;
                    if (pbds == null) {
                        continue;
                    }
                    for (PBD pbd : pbds) {
                        Host host = pbd.getHost(conn);
                        if (!isRefNull(host) && host.getUuid(conn).equals(_host.uuid)) {
                            if (!pbd.getCurrentlyAttached(conn)) {
                                pbd.plug(conn);
                            }
                            SR sr = entry.getKey();
                            sr.scan(conn);
                            return sr;
                        }
                    }
                }
            }
        } catch (XenAPIException e) {
            String msg = "Unable to get local LVMSR in host:" + _host.uuid + e.toString();
            s_logger.warn(msg);
        } catch (XmlRpcException e) {
            String msg = "Unable to get local LVMSR in host:" + _host.uuid + e.getCause();
            s_logger.warn(msg);
        }
        return null;

    }

    protected StartupStorageCommand initializeLocalSR(Connection conn) {

        SR lvmsr = getLocalLVMSR(conn);
        if (lvmsr == null) {
            return null;
        }
        try {
            String lvmuuid = lvmsr.getUuid(conn);
            long cap = lvmsr.getPhysicalSize(conn);
            if (cap < 0) {
                return null;
            }
            long avail = cap - lvmsr.getPhysicalUtilisation(conn);
            lvmsr.setNameLabel(conn, lvmuuid);
            String name = "Cloud Stack Local Storage Pool for " + _host.uuid;
            lvmsr.setNameDescription(conn, name);
            Host host = Host.getByUuid(conn, _host.uuid);
            String address = host.getAddress(conn);
            StoragePoolInfo pInfo = new StoragePoolInfo(lvmuuid, address, SRType.LVM.toString(), SRType.LVM.toString(), StoragePoolType.LVM, cap, avail);
            StartupStorageCommand cmd = new StartupStorageCommand();
            cmd.setPoolInfo(pInfo);
            cmd.setGuid(_host.uuid);
            cmd.setResourceType(Storage.StorageResourceType.STORAGE_POOL);
            return cmd;
        } catch (XenAPIException e) {
            String msg = "build startupstoragecommand err in host:" + _host.uuid + e.toString();
            s_logger.warn(msg);
        } catch (XmlRpcException e) {
            String msg = "build startupstoragecommand err in host:" + _host.uuid + e.getMessage();
            s_logger.warn(msg);
        }
        return null;

    }

    @Override
    public PingCommand getCurrentStatus(long id) {
        Connection conn = getConnection();
        try {

            if (!pingxenserver()) {
                Thread.sleep(1000);
                if (!pingxenserver()) {
                    s_logger.warn(" can not ping xenserver " + _host.uuid);
                    return null;
                }

            }
  
            HashMap<String, State> newStates = sync(conn);
            if (newStates == null) {
                newStates = new HashMap<String, State>();
            }
            if (!_canBridgeFirewall && !_isOvs) {
            	return new PingRoutingCommand(getType(), id, newStates);
            } else if (_isOvs) {
            	List<Pair<String, Long>>ovsStates = ovsFullSyncStates();
            	return new PingRoutingWithOvsCommand(getType(), id, newStates, ovsStates);
            }else {
            	HashMap<String, Pair<Long, Long>> nwGrpStates = syncNetworkGroups(conn, id);
            	return new PingRoutingWithNwGroupsCommand(getType(), id, newStates, nwGrpStates);
            }
        } catch (Exception e) {
            s_logger.warn("Unable to get current status", e);
            return null;
        }
    }
    
    private HashMap<String, Pair<Long,Long>> syncNetworkGroups(Connection conn, long id) {
    	HashMap<String, Pair<Long,Long>> states = new HashMap<String, Pair<Long,Long>>();
        
        String result = callHostPlugin(conn, "vmops", "get_rule_logs_for_vms", "host_uuid", _host.uuid);
        s_logger.trace("syncNetworkGroups: id=" + id + " got: " + result);
        String [] rulelogs = result != null ?result.split(";"): new String [0];
        for (String rulesforvm: rulelogs){
        	String [] log = rulesforvm.split(",");
        	if (log.length != 6) {
        		continue;
        	}
        	//output = ','.join([vmName, vmID, vmIP, domID, signature, seqno])
        	try {
        		states.put(log[0], new Pair<Long,Long>(Long.parseLong(log[1]), Long.parseLong(log[5])));
        	} catch (NumberFormatException nfe) {
        		states.put(log[0], new Pair<Long,Long>(-1L, -1L));
        	}
        }
    	return states;
    }

    @Override
    public Type getType() {
        return com.cloud.host.Host.Type.Routing;
    }
    
    protected boolean getHostInfo(Connection conn) throws IllegalArgumentException{
        try {
            Host myself = Host.getByUuid(conn, _host.uuid);
            
            Set<HostCpu> hcs = myself.getHostCPUs(conn);
            _host.cpus = hcs.size();
            for (final HostCpu hc : hcs) {
                _host.speed = hc.getSpeed(conn).intValue();
                break;
            }
            Nic privateNic = getManageMentNetwork(conn);
            _privateNetworkName = privateNic.nr.nameLabel;           
            _host.privatePif = privateNic.pr.uuid;
            _host.privateNetwork = privateNic.nr.uuid;
            
            _canBridgeFirewall = can_bridge_firewall(conn);
            
            _host.systemvmisouuid = null;
            
            Nic guestNic = null;
            if (_guestNetworkName != null && !_guestNetworkName.equals(_privateNetworkName)) {
            	guestNic = getLocalNetwork(conn, _guestNetworkName);
            	if (guestNic == null) {
            		s_logger.warn("Unable to find guest network " + _guestNetworkName);
            		throw new IllegalArgumentException("Unable to find guest network " + _guestNetworkName + " for host " + _host.ip);
            	}
            } else {
            	guestNic = privateNic;
            	_guestNetworkName = _privateNetworkName;
            }
            _host.guestNetwork = guestNic.nr.uuid;
            _host.guestPif = guestNic.pr.uuid;

            Nic publicNic = null;
            if (_publicNetworkName != null && !_publicNetworkName.equals(_guestNetworkName)) {
                publicNic = getLocalNetwork(conn, _publicNetworkName);
                if (publicNic == null) {
                    s_logger.warn("Unable to find public network " + _publicNetworkName + " for host " + _host.ip);
                    throw new IllegalArgumentException("Unable to find public network " + _publicNetworkName + " for host " + _host.ip);
                }
            } else {
                publicNic = guestNic;
                _publicNetworkName = _guestNetworkName;
            }
            _host.publicPif = publicNic.pr.uuid;
            _host.publicNetwork = publicNic.nr.uuid;

            Nic storageNic1 = null;
            if (_storageNetworkName1 != null && !_storageNetworkName1.equals(_guestNetworkName)) {
                storageNic1 = getLocalNetwork(conn, _storageNetworkName1);
            }
            if (storageNic1 == null) {
                storageNic1 = guestNic;
                _storageNetworkName1 = _guestNetworkName;
            }
            _host.storageNetwork1 = storageNic1.nr.uuid;
            _host.storagePif1 = storageNic1.pr.uuid;

            Nic storageNic2 = null;
            if (_storageNetworkName2 != null && !_storageNetworkName2.equals(_guestNetworkName)) {
                storageNic2 = getLocalNetwork(conn, _storageNetworkName2);
            }
            if (storageNic2 == null) {
                storageNic2 = guestNic;
                _storageNetworkName2 = _guestNetworkName;
            }
            _host.storageNetwork2 = storageNic2.nr.uuid;
            _host.storagePif2 = storageNic2.pr.uuid;
                       
            s_logger.info("Private Network is " + _privateNetworkName + " for host " + _host.ip);
            s_logger.info("Guest Network is " + _guestNetworkName + " for host " + _host.ip);
            s_logger.info("Public Network is " + _publicNetworkName + " for host " + _host.ip);
            s_logger.info("Storage Network 1 is " + _storageNetworkName1 + " for host " + _host.ip);
            s_logger.info("Storage Network 2 is " + _storageNetworkName2 + " for host " + _host.ip);
            
            return true;
        } catch (XenAPIException e) {
            s_logger.warn("Unable to get host information for " + _host.ip, e);
            return false;
        } catch (XmlRpcException e) {
            s_logger.warn("Unable to get host information for " + _host.ip, e);
            return false;
        }
    }

    private void setupLinkLocalNetwork(Connection conn) {
        try {
            Network.Record rec = new Network.Record();
            Set<Network> networks = Network.getByNameLabel(conn, _linkLocalPrivateNetworkName);
            Network linkLocal = null;

            if (networks.size() == 0) {
                rec.nameDescription = "link local network used by system vms";
                rec.nameLabel = _linkLocalPrivateNetworkName;
                Map<String, String> configs = new HashMap<String, String>();
                configs.put("ip_begin", NetUtils.getLinkLocalGateway());
                configs.put("ip_end", NetUtils.getLinkLocalIpEnd());
                configs.put("netmask", NetUtils.getLinkLocalNetMask());
                rec.otherConfig = configs;
                linkLocal = Network.create(conn, rec);

            } else {
                linkLocal = networks.iterator().next();
            }

            /* Make sure there is a physical bridge on this network */
            VIF dom0vif = null;
            Pair<VM, VM.Record> vm = getControlDomain(conn);
            VM dom0 = vm.first();
            Set<VIF> vifs = dom0.getVIFs(conn);
            if (vifs.size() != 0) {
            	for (VIF vif : vifs) {
            		Map<String, String> otherConfig = vif.getOtherConfig(conn);
            		if (otherConfig != null) {
            		    String nameLabel = otherConfig.get("nameLabel");
            		    if ((nameLabel != null) && nameLabel.equalsIgnoreCase("link_local_network_vif")) {
            		        dom0vif = vif;
            		    }
            		}
            	}
            }

            /* create temp VIF0 */
            if (dom0vif == null) {
            	s_logger.debug("Can't find a vif on dom0 for link local, creating a new one");
            	VIF.Record vifr = new VIF.Record();
            	vifr.VM = dom0;
            	vifr.device = getLowestAvailableVIFDeviceNum(conn, dom0);
            	if (vifr.device == null) {
            		s_logger.debug("Failed to create link local network, no vif available");
            		return;
            	}
            	Map<String, String> config = new HashMap<String, String>();
            	config.put("nameLabel", "link_local_network_vif");
            	vifr.otherConfig = config;
            	vifr.MAC = "FE:FF:FF:FF:FF:FF";
            	vifr.network = linkLocal;
            	dom0vif = VIF.create(conn, vifr);
            	dom0vif.plug(conn);
            } else {
            	s_logger.debug("already have a vif on dom0 for link local network");
            	if (!dom0vif.getCurrentlyAttached(conn)) {
            		dom0vif.plug(conn);
            	}
            }

            String brName = linkLocal.getBridge(conn);
            callHostPlugin(conn, "vmops", "setLinkLocalIP", "brName", brName);
            _host.linkLocalNetwork = linkLocal.getUuid(conn);

        } catch (XenAPIException e) {
            s_logger.warn("Unable to create local link network", e);
        } catch (XmlRpcException e) {
            s_logger.warn("Unable to create local link network", e);
        }
    }
    
    protected boolean transferManagementNetwork(Connection conn, Host host, PIF src, PIF.Record spr, PIF dest) throws XmlRpcException, XenAPIException {
        dest.reconfigureIp(conn, spr.ipConfigurationMode, spr.IP, spr.netmask, spr.gateway, spr.DNS);
        Host.managementReconfigure(conn, dest);
        String hostUuid = null;
        int count = 0;
        while (count < 10) {
            try {
                Thread.sleep(10000);
                hostUuid = host.getUuid(conn);
                if (hostUuid != null) {
                    break;
                }
            } catch (XmlRpcException e) {
                s_logger.debug("Waiting for host to come back: " + e.getMessage());
            } catch (XenAPIException e) {
                s_logger.debug("Waiting for host to come back: " + e.getMessage());
            } catch (InterruptedException e) {
                s_logger.debug("Gotta run");
                return false;
            }
        }
        if (hostUuid == null) {
            s_logger.warn("Unable to transfer the management network from " + spr.uuid);
            return false;
        }

        src.reconfigureIp(conn, IpConfigurationMode.NONE, null, null, null, null);
        return true;
    }
   
    @Override
    public StartupCommand[] initialize() throws IllegalArgumentException{
        Connection conn = getConnection();
        setupServer(conn);
        if (!getHostInfo(conn)) {
            s_logger.warn("Unable to get host information for " + _host.ip);
            return null;
        }

        StartupRoutingCommand cmd = new StartupRoutingCommand();
        fillHostInfo(conn, cmd);

        Map<String, State> changes = null;
        synchronized (_vms) {
            _vms.clear();
            changes = sync(conn);
        }

        cmd.setHypervisorType(HypervisorType.XenServer);
        cmd.setChanges(changes);
        cmd.setCluster(_cluster);

        StartupStorageCommand sscmd = initializeLocalSR(conn);
        if (sscmd != null) {
            return new StartupCommand[] { cmd, sscmd };
        }

        return new StartupCommand[] { cmd };
    }

    protected void setupServer(Connection conn) {
        String version = CitrixResourceBase.class.getPackage().getImplementationVersion();

        try {
            Host host = Host.getByUuid(conn, _host.uuid);
            /* enable host in case it is disabled somehow */
            host.enable(conn);
            /* push patches to XenServer */
            Host.Record hr = host.getRecord(conn);

            Iterator<String> it = hr.tags.iterator();

            while (it.hasNext()) {
                String tag = it.next();
                if (tag.startsWith("vmops-version-")) {
                    if (tag.contains(version)) {
                        s_logger.info(logX(host, "Host " + hr.address + " is already setup."));
                        return;
                    } else {
                        it.remove();
                    }
                }
            }

            com.trilead.ssh2.Connection sshConnection = new com.trilead.ssh2.Connection(hr.address, 22);
            try {
                sshConnection.connect(null, 60000, 60000);
                if (!sshConnection.authenticateWithPassword(_username, _password)) {
                    throw new CloudRuntimeException("Unable to authenticate");
                }

                SCPClient scp = new SCPClient(sshConnection);

                List<File> files = getPatchFiles();
                if( files == null || files.isEmpty() ) {
                    throw new CloudRuntimeException("Can not find patch file");
                }
                for( File file :files) {
                    String path = file.getParentFile().getAbsolutePath() + "/";
	                Properties props = new Properties();
	                props.load(new FileInputStream(file));
	
	                for (Map.Entry<Object, Object> entry : props.entrySet()) {
	                    String k = (String) entry.getKey();
	                    String v = (String) entry.getValue();
	
	                    assert (k != null && k.length() > 0 && v != null && v.length() > 0) : "Problems with " + k + "=" + v;
	
	                    String[] tokens = v.split(",");
	                    String f = null;
	                    if (tokens.length == 3 && tokens[0].length() > 0) {
	                        if (tokens[0].startsWith("/")) {
	                            f = tokens[0];
	                        } else if (tokens[0].startsWith("~")) {
	                            String homedir = System.getenv("HOME");
	                            f = homedir + tokens[0].substring(1) + k;
	                        } else {
	                            f = path + tokens[0] + '/' + k;
	                        }
	                    } else {
	                        f = path + k;
	                    }
	                    String d = tokens[tokens.length - 1];
	                    f = f.replace('/', File.separatorChar);
	
	                    String p = "0755";
	                    if (tokens.length == 3) {
	                        p = tokens[1];
	                    } else if (tokens.length == 2) {
	                        p = tokens[0];
	                    }
	
	                    if (!new File(f).exists()) {
	                        s_logger.warn("We cannot locate " + f);
	                        continue;
	                    }
	                    if (s_logger.isDebugEnabled()) {
	                        s_logger.debug("Copying " + f + " to " + d + " on " + hr.address + " with permission " + p);
	                    }
	                    scp.put(f, d, p);
	
	                }
                }

            } catch (IOException e) {
                throw new CloudRuntimeException("Unable to setup the server correctly", e);
            } finally {
                sshConnection.close();
            }

            if (!setIptables(conn)) {
                s_logger.warn("set xenserver Iptable failed");
            }

            hr.tags.add("vmops-version-" + version);
            host.setTags(conn, hr.tags);
        } catch (XenAPIException e) {
            String msg = "Xen setup failed due to " + e.toString();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException("Unable to get host information " + e.toString(), e);
        } catch (XmlRpcException e) {
            String msg = "Xen setup failed due to " + e.getMessage();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException("Unable to get host information ", e);
        }
    }

    protected List<File> getPatchFiles() {
        return null;
    }

    protected SR getSRByNameLabelandHost(Connection conn, String name) throws BadServerResponse, XenAPIException, XmlRpcException {
        Set<SR> srs = SR.getByNameLabel(conn, name);
        SR ressr = null;
        for (SR sr : srs) {
            Set<PBD> pbds;
            pbds = sr.getPBDs(conn);
            for (PBD pbd : pbds) {
                PBD.Record pbdr = pbd.getRecord(conn);
                if (pbdr.host != null && pbdr.host.getUuid(conn).equals(_host.uuid)) {
                    if (!pbdr.currentlyAttached) {
                        pbd.plug(conn);
                    }
                    ressr = sr;
                    break;
                }
            }
        }
        return ressr;
    }

    protected GetStorageStatsAnswer execute(final GetStorageStatsCommand cmd) {
        Connection conn = getConnection();
        try {
            Set<SR> srs = SR.getByNameLabel(conn, cmd.getStorageId());
            if (srs.size() != 1) {
                String msg = "There are " + srs.size() + " storageid: " + cmd.getStorageId();
                s_logger.warn(msg);
                return new GetStorageStatsAnswer(cmd, msg);
            }
            SR sr = srs.iterator().next();
            sr.scan(conn);
            long capacity = sr.getPhysicalSize(conn);
            long used = sr.getPhysicalUtilisation(conn);
            return new GetStorageStatsAnswer(cmd, capacity, used);
        } catch (XenAPIException e) {
            String msg = "GetStorageStats Exception:" + e.toString() + "host:" + _host.uuid + "storageid: " + cmd.getStorageId();
            s_logger.warn(msg);
            return new GetStorageStatsAnswer(cmd, msg);
        } catch (XmlRpcException e) {
            String msg = "GetStorageStats Exception:" + e.getMessage() + "host:" + _host.uuid + "storageid: " + cmd.getStorageId();
            s_logger.warn(msg);
            return new GetStorageStatsAnswer(cmd, msg);
        }
    }
    

    private void pbdPlug(Connection conn, PBD pbd) {
        String pbdUuid = "";
        String hostAddr = "";
        try {
            pbdUuid = pbd.getUuid(conn);
            hostAddr = pbd.getHost(conn).getAddress(conn);
            pbd.plug(conn);
        } catch (Exception e) {
            String msg = "PBD " + pbdUuid + " is not attached! and PBD plug failed due to "
            + e.toString() + ". Please check this PBD in host : " + hostAddr;
            s_logger.warn(msg, e);
        }
    }

    protected boolean checkSR(Connection conn, SR sr) {
        try {
            SR.Record srr = sr.getRecord(conn);
            Set<PBD> pbds = sr.getPBDs(conn);
            if (pbds.size() == 0) {
                String msg = "There is no PBDs for this SR: " + srr.nameLabel + " on host:" + _host.uuid;
                s_logger.warn(msg);
                return false;
            }
            Set<Host> hosts = null;
            if (srr.shared) {
                hosts = Host.getAll(conn);

                for (Host host : hosts) {
                    boolean found = false;
                    for (PBD pbd : pbds) {
                        if (host.equals(pbd.getHost(conn))) {
                            PBD.Record pbdr = pbd.getRecord(conn);
                            if (!pbdr.currentlyAttached) {
                                pbdPlug(conn, pbd); 
                            }
                            pbds.remove(pbd);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        PBD.Record pbdr = srr.PBDs.iterator().next().getRecord(conn);
                        pbdr.host = host;
                        pbdr.uuid = "";
                        PBD pbd = PBD.create(conn, pbdr);
                        pbdPlug(conn, pbd); 
                    }
                }
            } else {
                for (PBD pbd : pbds) {
                    PBD.Record pbdr = pbd.getRecord(conn);
                    if (!pbdr.currentlyAttached) {
                        pbdPlug(conn, pbd);
                    }
                }
            }

        } catch (Exception e) {
            String msg = "checkSR failed host:" + _host.uuid + " due to " + e.toString();
            s_logger.warn(msg, e);
            return false;
        }
        return true;
    }

    protected Answer execute(ModifyStoragePoolCommand cmd) {
        Connection conn = getConnection();
        StorageFilerTO pool = cmd.getPool();
        try {
            SR sr = getStorageRepository(conn, pool);
            long capacity = sr.getPhysicalSize(conn);
            long available = capacity - sr.getPhysicalUtilisation(conn);
            if (capacity == -1) {
                String msg = "Pool capacity is -1! pool: " + pool.getHost() + pool.getPath();
                s_logger.warn(msg);
                return new Answer(cmd, false, msg);
            }
            Map<String, TemplateInfo> tInfo = new HashMap<String, TemplateInfo>();
            ModifyStoragePoolAnswer answer = new ModifyStoragePoolAnswer(cmd, capacity, available, tInfo);
            return answer;
        } catch (XenAPIException e) {
            String msg = "ModifyStoragePoolCommand XenAPIException:" + e.toString() + " host:" + _host.uuid + " pool: " + pool.getHost() + pool.getPath();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        } catch (Exception e) {
            String msg = "ModifyStoragePoolCommand XenAPIException:" + e.getMessage() + " host:" + _host.uuid + " pool: " + pool.getHost() + pool.getPath();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        }

    }
    
    protected boolean can_bridge_firewall(Connection conn) {   
        return Boolean.valueOf(callHostPlugin(conn, "vmops", "can_bridge_firewall", "host_uuid", _host.uuid));
    }
    
    private Answer execute(OvsDeleteFlowCommand cmd) {
    	_isOvs = true;
    	
    	Connection conn = getConnection();
    	try {
    		Network nw = setupvSwitchNetwork(conn);
    		String bridge = nw.getBridge(conn);
    		String result = callHostPlugin(conn, "ovsgre", "ovs_delete_flow", "bridge", bridge,
    				"vmName", cmd.getVmName());
    		
    		if (result.equalsIgnoreCase("SUCCESS")) {
    			return new Answer(cmd, true, "success to delete flows for " + cmd.getVmName());
    		} else {
    			return new Answer(cmd, false, result);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return new Answer(cmd, false, "failed to delete flow for " + cmd.getVmName());
    }
    
    private List<Pair<String, Long>> ovsFullSyncStates() {
    	Connection conn = getConnection();
    	try {
    		String result = callHostPlugin(conn, "ovsgre", "ovs_get_vm_log", "host_uuid", _host.uuid);
    		String [] logs = result != null ?result.split(";"): new String [0];
    		List<Pair<String, Long>> states = new ArrayList<Pair<String, Long>>();
    		for (String log: logs){
            	String [] info = log.split(",");
            	if (info.length != 5) {
            		s_logger.warn("Wrong element number in ovs log(" + log +")");
            		continue;
            	}
            	
            	//','.join([bridge, vmName, vmId, seqno, tag])
            	try {
            		states.add(new Pair<String,Long>(info[0], Long.parseLong(info[3])));
            	} catch (NumberFormatException nfe) {
            		states.add(new Pair<String,Long>(info[0], -1L));
            	}
            }
    		
    		return states;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return null;
    }
    
    private OvsSetTagAndFlowAnswer execute(OvsSetTagAndFlowCommand cmd) {
    	_isOvs = true;
    	
    	Connection conn = getConnection();
    	try {
    		Network nw = setupvSwitchNetwork(conn);
    		String bridge = nw.getBridge(conn);
    		
    		/*If VM is domainRouter, this will try to set flow and tag on its
    		 * none guest network nic. don't worry, it will fail silently at host
    		 * plugin side
    		 */
    		String result = callHostPlugin(conn, "ovsgre", "ovs_set_tag_and_flow", "bridge", bridge,
    				"vmName", cmd.getVmName(), "tag", cmd.getTag(),
    				"vlans", cmd.getVlans(), "seqno", cmd.getSeqNo());
			s_logger.debug("set flow for " + cmd.getVmName() + " " + result);
		
			if (result.equalsIgnoreCase("SUCCESS")) {
				return new OvsSetTagAndFlowAnswer(cmd, true, result);
			} else {
				return new OvsSetTagAndFlowAnswer(cmd, false, result);
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
		return new OvsSetTagAndFlowAnswer(cmd, false, "EXCEPTION");
    }
    
    
    private OvsCreateGreTunnelAnswer execute(OvsCreateGreTunnelCommand cmd) {
    	_isOvs = true;
    	
    	Connection conn = getConnection();
    	String bridge = "unkonwn";	
    	try {
    		Network nw = setupvSwitchNetwork(conn);
    		bridge = nw.getBridge(conn);
    		
			String result = callHostPlugin(conn, "ovsgre", "ovs_create_gre", "bridge", bridge,
					"remoteIP", cmd.getRemoteIp(), "greKey", cmd.getKey(), "from",
					Long.toString(cmd.getFrom()), "to", Long.toString(cmd.getTo()));
			String[] res = result.split(":");
			if (res.length != 2 || (res.length == 2 && res[1] == "[]")) {
				return new OvsCreateGreTunnelAnswer(cmd, false, result,
						_host.ip, bridge);
			} else {
				return new OvsCreateGreTunnelAnswer(cmd, true, result, _host.ip, bridge, Integer.parseInt(res[1]));
			}	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
		return new OvsCreateGreTunnelAnswer(cmd, false, "EXCEPTION", _host.ip, bridge);
    }
    
    private Answer execute(SecurityIngressRulesCmd cmd) {
        Connection conn = getConnection();
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Sending network rules command to " + _host.ip);
        }

        if (!_canBridgeFirewall) {
            s_logger.info("Host " + _host.ip + " cannot do bridge firewalling");
            return new SecurityIngressRuleAnswer(cmd, false, "Host " + _host.ip + " cannot do bridge firewalling");
        }
      
        String result = callHostPlugin(conn, "vmops", "network_rules",
                "vmName", cmd.getVmName(),
                "vmIP", cmd.getGuestIp(),
                "vmMAC", cmd.getGuestMac(),
                "vmID", Long.toString(cmd.getVmId()),
                "signature", cmd.getSignature(),
                "seqno", Long.toString(cmd.getSeqNum()),
                "rules", cmd.stringifyRules());

        if (result == null || result.isEmpty() || !Boolean.parseBoolean(result)) {
            s_logger.warn("Failed to program network rules for vm " + cmd.getVmName());
            return new SecurityIngressRuleAnswer(cmd, false, "programming network rules failed");
        } else {
            s_logger.info("Programmed network rules for vm " + cmd.getVmName() + " guestIp=" + cmd.getGuestIp() + ", numrules=" + cmd.getRuleSet().length);
            return new SecurityIngressRuleAnswer(cmd);
        }
    }

    protected Answer execute(DeleteStoragePoolCommand cmd) {
        Connection conn = getConnection();
        StoragePoolVO pool = cmd.getPool();
        StorageFilerTO poolTO = new StorageFilerTO(pool);
        try {
            SR sr = getStorageRepository(conn, poolTO);
            removeSR(conn, sr);
            Answer answer = new Answer(cmd, true, "success");
            return answer;
        } catch (Exception e) {
            String msg = "DeleteStoragePoolCommand XenAPIException:" + e.getMessage() + " host:" + _host.uuid + " pool: " + pool.getName() + pool.getHostAddress() + pool.getPath();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        }

    }

    public Connection getConnection() {
        return _connPool.connect(_host.uuid, _host.pool, _host.ip, _username, _password, _wait);
    }

    protected void fillHostInfo(Connection conn, StartupRoutingCommand cmd) {
        final StringBuilder caps = new StringBuilder();
        try {

            Host host = Host.getByUuid(conn, _host.uuid);
            Host.Record hr = host.getRecord(conn);

            Map<String, String> details = cmd.getHostDetails();
            if (details == null) {
                details = new HashMap<String, String>();
            }
            if (_privateNetworkName != null) {
            details.put("private.network.device", _privateNetworkName);
            }
            if (_publicNetworkName != null) {
            details.put("public.network.device", _publicNetworkName);
            } 
            if (_guestNetworkName != null) {
            details.put("guest.network.device", _guestNetworkName);
            }
            details.put("can_bridge_firewall", Boolean.toString(_canBridgeFirewall));
            cmd.setHostDetails(details);
            cmd.setName(hr.nameLabel);
            cmd.setGuid(_host.uuid);
            cmd.setDataCenter(Long.toString(_dcId));
            for (final String cap : hr.capabilities) {
                if (cap.length() > 0) {
                    caps.append(cap).append(" , ");
                }
            }
            if (caps.length() > 0) {
                caps.delete(caps.length() - 3, caps.length());
            }
            cmd.setCaps(caps.toString());

            cmd.setSpeed(_host.speed);
            cmd.setCpus(_host.cpus);

            HostMetrics hm = host.getMetrics(conn);

            long ram = 0;
            long dom0Ram = 0;
            ram = hm.getMemoryTotal(conn);
            Set<VM> vms = host.getResidentVMs(conn);
            for (VM vm : vms) {
                if (vm.getIsControlDomain(conn)) {
                    dom0Ram = vm.getMemoryDynamicMax(conn);
                    break;
                }
            }
            // assume the memory Virtualization overhead is 1/64
            // xen hypervisor used 128 M
            ram = (ram - dom0Ram - (128 * 1024 * 1024)) * 63/64;
            cmd.setMemory(ram);
            cmd.setDom0MinMemory(dom0Ram);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Total Ram: " + ram + " dom0 Ram: " + dom0Ram);
            }
            
            PIF pif = PIF.getByUuid(conn, _host.privatePif);
            PIF.Record pifr = pif.getRecord(conn);
            if (pifr.IP != null && pifr.IP.length() > 0) {
                cmd.setPrivateIpAddress(pifr.IP);
                cmd.setPrivateMacAddress(pifr.MAC);
                cmd.setPrivateNetmask(pifr.netmask);
            } else {
                String msg = "Private network " + _privateNetworkName + " doesn't have IP address, please check the host network configuration";
                s_logger.error(msg);
                throw new CloudRuntimeException(msg);
            }

            pif = PIF.getByUuid(conn, _host.storagePif1);
            pifr = pif.getRecord(conn);
            if (pifr.IP != null && pifr.IP.length() > 0) {
                cmd.setStorageIpAddress(pifr.IP);
                cmd.setStorageMacAddress(pifr.MAC);
                cmd.setStorageNetmask(pifr.netmask);
            }

            if (_host.storagePif2 != null) {
                pif = PIF.getByUuid(conn, _host.storagePif2);
                pifr = pif.getRecord(conn);
                if (pifr.IP != null && pifr.IP.length() > 0) {
                    cmd.setStorageIpAddressDeux(pifr.IP);
                    cmd.setStorageMacAddressDeux(pifr.MAC);
                    cmd.setStorageNetmaskDeux(pifr.netmask);
                }
            }

            Map<String, String> configs = hr.otherConfig;
            cmd.setIqn(configs.get("iscsi_iqn"));

            cmd.setPod(_pod);
            cmd.setVersion(CitrixResourceBase.class.getPackage().getImplementationVersion());

        } catch (final XmlRpcException e) {
            throw new CloudRuntimeException("XML RPC Exception" + e.getMessage(), e);
        } catch (XenAPIException e) {
            throw new CloudRuntimeException("XenAPIException" + e.toString(), e);
        }
    }

    public CitrixResourceBase() {
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;
        _host.uuid = (String) params.get("guid");
        try {
            _dcId = Long.parseLong((String) params.get("zone"));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Unable to get the zone " + params.get("zone"));
        }
        _name = _host.uuid;
        _host.ip = (String) params.get("ipaddress");
        _host.pool = (String) params.get("pool");
        _username = (String) params.get("username");
        _password = (String) params.get("password");
        _pod = (String) params.get("pod");
        _cluster = (String)params.get("cluster");
        _privateNetworkName = (String) params.get("private.network.device");
        _publicNetworkName = (String) params.get("public.network.device");
        _guestNetworkName = (String)params.get("guest.network.device");
        
        _linkLocalPrivateNetworkName = (String) params.get("private.linkLocal.device");
        if (_linkLocalPrivateNetworkName == null) {
            _linkLocalPrivateNetworkName = "cloud_link_local_network";
        }

        _storageNetworkName1 = (String) params.get("storage.network.device1");
        _storageNetworkName2 = (String) params.get("storage.network.device2");

        String value = (String) params.get("wait");
        _wait = NumbersUtil.parseInt(value, 1800);

        if (_pod == null) {
            throw new ConfigurationException("Unable to get the pod");
        }

        if (_host.ip == null) {
            throw new ConfigurationException("Unable to get the host address");
        }

        if (_username == null) {
            throw new ConfigurationException("Unable to get the username");
        }

        if (_password == null) {
            throw new ConfigurationException("Unable to get the password");
        }

        if (_host.uuid == null) {
            throw new ConfigurationException("Unable to get the uuid");
        }

        _storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (_storage == null) {
            value = (String) params.get(StorageLayer.ClassConfigKey);
            if (value == null) {
                value = "com.cloud.storage.JavaStorageLayer";
            }

            try {
                Class<?> clazz = Class.forName(value);
                _storage = (StorageLayer) ComponentLocator.inject(clazz);
                _storage.configure("StorageLayer", params);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Unable to find class " + value);
            }
        }

        return true;
    }

    void destroyVDI(Connection conn, VDI vdi) {
        try {
            vdi.destroy(conn);
        } catch (Exception e) {
            String msg = "destroy VDI failed due to " + e.toString();
            s_logger.warn(msg);
        }
    }

    public CreateAnswer execute(CreateCommand cmd) {
        Connection conn = getConnection();
        StorageFilerTO pool = cmd.getPool();
        DiskProfile dskch = cmd.getDiskCharacteristics();
        VDI vdi = null;
        try {
            SR poolSr = getStorageRepository(conn, pool);

            if (cmd.getTemplateUrl() != null) {
                VDI tmpltvdi = null;

                tmpltvdi = getVDIbyUuid(conn, cmd.getTemplateUrl());
                vdi = tmpltvdi.createClone(conn, new HashMap<String, String>());
                vdi.setNameLabel(conn, dskch.getName());
            } else {
                VDI.Record vdir = new VDI.Record();
                vdir.nameLabel = dskch.getName();
                vdir.SR = poolSr;
                vdir.type = Types.VdiType.USER;
                            
                vdir.virtualSize = dskch.getSize();
                vdi = VDI.create(conn, vdir);
            }

            VDI.Record vdir;
            vdir = vdi.getRecord(conn);
            s_logger.debug("Succesfully created VDI for " + cmd + ".  Uuid = " + vdir.uuid);

            VolumeTO vol = new VolumeTO(cmd.getVolumeId(), dskch.getType(), Storage.StorageResourceType.STORAGE_POOL, pool.getType(), pool.getUuid(), 
        		vdir.nameLabel, pool.getPath(), vdir.uuid, vdir.virtualSize, null);
            return new CreateAnswer(cmd, vol);
        } catch (Exception e) {
            s_logger.warn("Unable to create volume; Pool=" + pool + "; Disk: " + dskch, e);
            return new CreateAnswer(cmd, e);
        }
    }

    protected SR getISOSRbyVmName(Connection conn, String vmName) {
        try {
            Set<SR> srs = SR.getByNameLabel(conn, vmName + "-ISO");
            if (srs.size() == 0) {
                return null;
            } else if (srs.size() == 1) {
                return srs.iterator().next();
            } else {
                String msg = "getIsoSRbyVmName failed due to there are more than 1 SR having same Label";
                s_logger.warn(msg);
            }
        } catch (XenAPIException e) {
            String msg = "getIsoSRbyVmName failed due to " + e.toString();
            s_logger.warn(msg, e);
        } catch (Exception e) {
            String msg = "getIsoSRbyVmName failed due to " + e.getMessage();
            s_logger.warn(msg, e);
        }
        return null;
    }

    protected SR createNfsSRbyURI(Connection conn, URI uri, boolean shared) {
        try {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Creating a " + (shared ? "shared SR for " : "not shared SR for ") + uri);
            }

            Map<String, String> deviceConfig = new HashMap<String, String>();
            String path = uri.getPath();
            path = path.replace("//", "/");
            deviceConfig.put("server", uri.getHost());
            deviceConfig.put("serverpath", path);
            String name = UUID.nameUUIDFromBytes(new String(uri.getHost() + path).getBytes()).toString();
            if (!shared) {
                Set<SR> srs = SR.getByNameLabel(conn, name);
                for (SR sr : srs) {
                    SR.Record record = sr.getRecord(conn);
                    if (SRType.NFS.equals(record.type) && record.contentType.equals("user") && !record.shared) {
                        removeSRSync(conn, sr);
                    }
                }
            }

            Host host = Host.getByUuid(conn, _host.uuid);

            SR sr = SR.create(conn, host, deviceConfig, new Long(0), name, uri.getHost() + uri.getPath(), SRType.NFS.toString(), "user", shared, new HashMap<String, String>());
            if( !checkSR(conn, sr) ) {
                throw new Exception("no attached PBD");
            }   
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(logX(sr, "Created a SR; UUID is " + sr.getUuid(conn) + " device config is " + deviceConfig));
            }
            sr.scan(conn);
            return sr;
        } catch (XenAPIException e) {
            String msg = "Can not create second storage SR mountpoint: " + uri.getHost() + uri.getPath() + " due to " + e.toString();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "Can not create second storage SR mountpoint: " + uri.getHost() + uri.getPath() + " due to " + e.getMessage();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg, e);
        }
    }

    protected SR createIsoSRbyURI(Connection conn, URI uri, String vmName, boolean shared) {
        try {
            Map<String, String> deviceConfig = new HashMap<String, String>();
            String path = uri.getPath();
            path = path.replace("//", "/");
            deviceConfig.put("location", uri.getHost() + ":" + uri.getPath());
            Host host = Host.getByUuid(conn, _host.uuid);
            SR sr = SR.create(conn, host, deviceConfig, new Long(0), uri.getHost() + uri.getPath(), "iso", "iso", "iso", shared, new HashMap<String, String>());
            sr.setNameLabel(conn, vmName + "-ISO");
            sr.setNameDescription(conn, deviceConfig.get("location"));
            
            sr.scan(conn);
            return sr;
        } catch (XenAPIException e) {
            String msg = "createIsoSRbyURI failed! mountpoint: " + uri.getHost() + uri.getPath() + " due to " + e.toString();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "createIsoSRbyURI failed! mountpoint: " + uri.getHost() + uri.getPath() + " due to " + e.getMessage();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg, e);
        }
    }

    protected VDI getVDIbyLocationandSR(Connection conn, String loc, SR sr) {
        try {
            Set<VDI> vdis = sr.getVDIs(conn);
            for (VDI vdi : vdis) {
                if (vdi.getLocation(conn).startsWith(loc)) {
                    return vdi;
                }
            }

            String msg = "can not getVDIbyLocationandSR " + loc;
            s_logger.warn(msg);
            return null;
        } catch (XenAPIException e) {
            String msg = "getVDIbyLocationandSR exception " + loc + " due to " + e.toString();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "getVDIbyLocationandSR exception " + loc + " due to " + e.getMessage();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg, e);
        }

    }

    protected VDI getVDIbyUuid(Connection conn, String uuid) {
        try {
            return VDI.getByUuid(conn, uuid);
        } catch (XenAPIException e) {
            String msg = "VDI getByUuid for uuid: " + uuid + " failed due to " + e.toString();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "VDI getByUuid for uuid: " + uuid + " failed due to " + e.getMessage();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg, e);
        }
    }

    protected SR getIscsiSR(Connection conn, StorageFilerTO pool) {
        synchronized (pool.getUuid().intern()) {
            Map<String, String> deviceConfig = new HashMap<String, String>();
            try {
                String target = pool.getHost();
                String path = pool.getPath();
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }

                String tmp[] = path.split("/");
                if (tmp.length != 3) {
                    String msg = "Wrong iscsi path " + pool.getPath() + " it should be /targetIQN/LUN";
                    s_logger.warn(msg);
                    throw new CloudRuntimeException(msg);
                }
                String targetiqn = tmp[1].trim();
                String lunid = tmp[2].trim();
                String scsiid = "";

                Set<SR> srs = SR.getByNameLabel(conn, pool.getUuid());
                for (SR sr : srs) {
                    if (!SRType.LVMOISCSI.equals(sr.getType(conn))) {
                        continue;
                    }
                    Set<PBD> pbds = sr.getPBDs(conn);
                    if (pbds.isEmpty()) {
                        continue;
                    }
                    PBD pbd = pbds.iterator().next();
                    Map<String, String> dc = pbd.getDeviceConfig(conn);
                    if (dc == null) {
                        continue;
                    }
                    if (dc.get("target") == null) {
                        continue;
                    }
                    if (dc.get("targetIQN") == null) {
                        continue;
                    }
                    if (dc.get("lunid") == null) {
                        continue;
                    }
                    if (target.equals(dc.get("target")) && targetiqn.equals(dc.get("targetIQN")) && lunid.equals(dc.get("lunid"))) {
                        if (checkSR(conn, sr)) {
                            return sr;
                        }
                        throw new CloudRuntimeException("SR check failed for storage pool: " + pool.getUuid() + "on host:" + _host.uuid);
                    }
                }
                deviceConfig.put("target", target);
                deviceConfig.put("targetIQN", targetiqn);

                Host host = Host.getByUuid(conn, _host.uuid);
                Map<String, String> smConfig = new HashMap<String, String>();
                String type = SRType.LVMOISCSI.toString();
                String poolId = Long.toString(pool.getId());
                SR sr = null;
                try {
                    sr = SR.create(conn, host, deviceConfig, new Long(0), pool.getUuid(), poolId, type, "user", true,
                            smConfig);
                } catch (XenAPIException e) {
                    String errmsg = e.toString();
                    if (errmsg.contains("SR_BACKEND_FAILURE_107")) {
                        String lun[] = errmsg.split("<LUN>");
                        boolean found = false;
                        for (int i = 1; i < lun.length; i++) {
                            int blunindex = lun[i].indexOf("<LUNid>") + 7;
                            int elunindex = lun[i].indexOf("</LUNid>");
                            String ilun = lun[i].substring(blunindex, elunindex);
                            ilun = ilun.trim();
                            if (ilun.equals(lunid)) {
                                int bscsiindex = lun[i].indexOf("<SCSIid>") + 8;
                                int escsiindex = lun[i].indexOf("</SCSIid>");
                                scsiid = lun[i].substring(bscsiindex, escsiindex);
                                scsiid = scsiid.trim();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            String msg = "can not find LUN " + lunid + " in " + errmsg;
                            s_logger.warn(msg);
                            throw new CloudRuntimeException(msg);
                        }
                    } else {
                        String msg = "Unable to create Iscsi SR  " + deviceConfig + " due to  " + e.toString();
                        s_logger.warn(msg, e);
                        throw new CloudRuntimeException(msg, e);
                    }
                }              
                deviceConfig.put("SCSIid", scsiid);

                String result = SR.probe(conn, host, deviceConfig, type , smConfig);
                String pooluuid = null;
                if( result.indexOf("<UUID>") != -1) {
                    pooluuid = result.substring(result.indexOf("<UUID>") + 6, result.indexOf("</UUID>")).trim();
                }
                if( pooluuid == null || pooluuid.length() != 36) {
                    sr = SR.create(conn, host, deviceConfig, new Long(0), pool.getUuid(), poolId, type, "user", true,
                        smConfig);
                } else {
                    sr = SR.introduce(conn, pooluuid, pool.getUuid(), poolId, 
                            type, "user", true, smConfig);
                    PBD.Record rec = new PBD.Record();
                    rec.deviceConfig = deviceConfig;
                    rec.host = host;
                    rec.SR = sr;
                    PBD pbd = PBD.create(conn, rec);
                    pbd.plug(conn);
                }
                sr.scan(conn);
                return sr;
            } catch (XenAPIException e) {
                String msg = "Unable to create Iscsi SR  " + deviceConfig + " due to  " + e.toString();
                s_logger.warn(msg, e);
                throw new CloudRuntimeException(msg, e);
            } catch (Exception e) {
                String msg = "Unable to create Iscsi SR  " + deviceConfig + " due to  " + e.getMessage();
                s_logger.warn(msg, e);
                throw new CloudRuntimeException(msg, e);
            }
        }
    }

    protected SR getNfsSR(Connection conn, StorageFilerTO pool) {
        Map<String, String> deviceConfig = new HashMap<String, String>();
        try {
            String server = pool.getHost();
            String serverpath = pool.getPath();
            serverpath = serverpath.replace("//", "/");
            Set<SR> srs = SR.getAll(conn);
            for (SR sr : srs) {
                if (!SRType.NFS.equals(sr.getType(conn))) {
                    continue;
                }

                Set<PBD> pbds = sr.getPBDs(conn);
                if (pbds.isEmpty()) {
                    continue;
                }

                PBD pbd = pbds.iterator().next();

                Map<String, String> dc = pbd.getDeviceConfig(conn);

                if (dc == null) {
                    continue;
                }

                if (dc.get("server") == null) {
                    continue;
                }

                if (dc.get("serverpath") == null) {
                    continue;
                }

                if (server.equals(dc.get("server")) && serverpath.equals(dc.get("serverpath"))) {
                    if (checkSR(conn, sr)) {
                        return sr;
                    }
                    throw new CloudRuntimeException("SR check failed for storage pool: " + pool.getUuid() + "on host:" + _host.uuid);
                }

            }

            deviceConfig.put("server", server);
            deviceConfig.put("serverpath", serverpath);
            Host host = Host.getByUuid(conn, _host.uuid);
            SR sr = SR.create(conn, host, deviceConfig, new Long(0), pool.getUuid(), Long.toString(pool.getId()), SRType.NFS.toString(), "user", true,
                    new HashMap<String, String>());
            sr.scan(conn);
            return sr;
        } catch (XenAPIException e) {
            throw new CloudRuntimeException("Unable to create NFS SR " + pool.toString(), e);
        } catch (XmlRpcException e) {
            throw new CloudRuntimeException("Unable to create NFS SR " + pool.toString(), e);
        }
    }

    public Answer execute(DestroyCommand cmd) {
        Connection conn = getConnection();
        VolumeTO vol = cmd.getVolume();
        // Look up the VDI
        String volumeUUID = vol.getPath();
        VDI vdi = null;
        try {
            vdi = getVDIbyUuid(conn, volumeUUID);
        } catch (Exception e) {
            String msg = "getVDIbyUuid for " + volumeUUID + " failed due to " + e.toString();
            s_logger.warn(msg);
            return new Answer(cmd, true, "Success");
        }
        Set<VBD> vbds = null;
        try {
            vbds = vdi.getVBDs(conn);
        } catch (Exception e) {
            String msg = "VDI getVBDS for " + volumeUUID + " failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        }
        for (VBD vbd : vbds) {
            try {
                vbd.unplug(conn);
                vbd.destroy(conn);
            } catch (Exception e) {
                String msg = "VM destroy for " + volumeUUID + "  failed due to " + e.toString();
                s_logger.warn(msg, e);
                return new Answer(cmd, false, msg);
            }
        }
        try {
            vdi.destroy(conn);
        } catch (Exception e) {
            String msg = "VDI destroy for " + volumeUUID + " failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        }

        return new Answer(cmd, true, "Success");
    }

    public ShareAnswer execute(final ShareCommand cmd) {
        Connection conn = getConnection();
        if (!cmd.isShare()) {
            SR sr = getISOSRbyVmName(conn, cmd.getVmName());
            try {
                if (sr != null) {
                    Set<VM> vms = VM.getByNameLabel(conn, cmd.getVmName());
                    if (vms.size() == 0) {
                        removeSR(conn, sr);
                    }
                }
            } catch (Exception e) {
                String msg = "SR.getNameLabel failed due to  " + e.getMessage() + e.toString();
                s_logger.warn(msg);
            }
        }
        return new ShareAnswer(cmd, new HashMap<String, Integer>());
    }

    public CopyVolumeAnswer execute(final CopyVolumeCommand cmd) {
        Connection conn = getConnection();
        String volumeUUID = cmd.getVolumePath();
        StoragePoolVO pool = cmd.getPool();
        StorageFilerTO poolTO = new StorageFilerTO(pool);
        String secondaryStorageURL = cmd.getSecondaryStorageURL();

        URI uri = null;
        try {
            uri = new URI(secondaryStorageURL);
        } catch (URISyntaxException e) {
            return new CopyVolumeAnswer(cmd, false, "Invalid secondary storage URL specified.", null, null);
        }

        String remoteVolumesMountPath = uri.getHost() + ":" + uri.getPath() + "/volumes/";
        String volumeFolder = String.valueOf(cmd.getVolumeId()) + "/";
        boolean toSecondaryStorage = cmd.toSecondaryStorage();

        String errorMsg = "Failed to copy volume";
        SR primaryStoragePool = null;
        SR secondaryStorage = null;
        VDI srcVolume = null;
        VDI destVolume = null;
        try {
            if (toSecondaryStorage) {
                // Create the volume folder
                if (!createSecondaryStorageFolder(conn, remoteVolumesMountPath, volumeFolder)) {
                    throw new InternalErrorException("Failed to create the volume folder.");
                }

                // Create a SR for the volume UUID folder
                secondaryStorage = createNfsSRbyURI(conn, new URI(secondaryStorageURL + "/volumes/" + volumeFolder), false);

                // Look up the volume on the source primary storage pool
                srcVolume = getVDIbyUuid(conn, volumeUUID);

                // Copy the volume to secondary storage
                destVolume = cloudVDIcopy(conn, srcVolume, secondaryStorage);
            } else {
                // Mount the volume folder
                secondaryStorage = createNfsSRbyURI(conn, new URI(secondaryStorageURL + "/volumes/" + volumeFolder), false);

                // Look up the volume on secondary storage
                Set<VDI> vdis = secondaryStorage.getVDIs(conn);
                for (VDI vdi : vdis) {
                    if (vdi.getUuid(conn).equals(volumeUUID)) {
                        srcVolume = vdi;
                        break;
                    }
                }

                if (srcVolume == null) {
                    throw new InternalErrorException("Failed to find volume on secondary storage.");
                }

                // Copy the volume to the primary storage pool
                primaryStoragePool = getStorageRepository(conn, poolTO);
                destVolume = cloudVDIcopy(conn, srcVolume, primaryStoragePool);
            }

            String srUUID;

            if (primaryStoragePool == null) {
                srUUID = secondaryStorage.getUuid(conn);
            } else {
                srUUID = primaryStoragePool.getUuid(conn);
            }

            String destVolumeUUID = destVolume.getUuid(conn);

            return new CopyVolumeAnswer(cmd, true, null, srUUID, destVolumeUUID);
        } catch (XenAPIException e) {
            s_logger.warn(errorMsg + ": " + e.toString(), e);
            return new CopyVolumeAnswer(cmd, false, e.toString(), null, null);
        } catch (Exception e) {
            s_logger.warn(errorMsg + ": " + e.toString(), e);
            return new CopyVolumeAnswer(cmd, false, e.getMessage(), null, null);
        } finally {
            if (!toSecondaryStorage && srcVolume != null) {
                // Delete the volume on secondary storage
                destroyVDI(conn, srcVolume);
            }

            removeSR(conn, secondaryStorage);
            if (!toSecondaryStorage) {
                // Delete the volume folder on secondary storage
                deleteSecondaryStorageFolder(conn, remoteVolumesMountPath, volumeFolder);
            }
        }

    }

    protected AttachVolumeAnswer execute(final AttachVolumeCommand cmd) {
        Connection conn = getConnection();
        boolean attach = cmd.getAttach();
        String vmName = cmd.getVmName();
        Long deviceId = cmd.getDeviceId();

        String errorMsg;
        if (attach) {
            errorMsg = "Failed to attach volume";
        } else {
            errorMsg = "Failed to detach volume";
        }

        try {
            // Look up the VDI
            VDI vdi = mount(conn, cmd.getPooltype(), cmd.getVolumeFolder(),cmd.getVolumePath());
            // Look up the VM
            VM vm = getVM(conn, vmName);
            /* For HVM guest, if no pv driver installed, no attach/detach */
            boolean isHVM;
            if (vm.getPVBootloader(conn).equalsIgnoreCase("")) {
                isHVM = true;
            } else {
                isHVM = false;
            }
            VMGuestMetrics vgm = vm.getGuestMetrics(conn);
            boolean pvDrvInstalled = false;
            if (!isRefNull(vgm) && vgm.getPVDriversUpToDate(conn)) {
                pvDrvInstalled = true;
            }
            if (isHVM && !pvDrvInstalled) {
                s_logger.warn(errorMsg + ": You attempted an operation on a VM which requires PV drivers to be installed but the drivers were not detected");
                return new AttachVolumeAnswer(cmd, "You attempted an operation that requires PV drivers to be installed on the VM. Please install them by inserting xen-pv-drv.iso.");
            }
            if (attach) {
                // Figure out the disk number to attach the VM to
                String diskNumber = null;
                if( deviceId != null ) {
                    if( deviceId.longValue() == 3 ) {
                        String msg = "Device 3 is reserved for CD-ROM, choose other device";
                        return new AttachVolumeAnswer(cmd,msg);          
                    }
                    if(isDeviceUsed(conn, vm, deviceId)) {
                        String msg = "Device " + deviceId + " is used in VM " + vmName;
                        return new AttachVolumeAnswer(cmd,msg);
                    }                 
                    diskNumber = deviceId.toString();
                } else {
                    diskNumber = getUnusedDeviceNum(conn, vm);
                }
                // Create a new VBD
                VBD.Record vbdr = new VBD.Record();
                vbdr.VM = vm;
                vbdr.VDI = vdi;
                vbdr.bootable = false;
                vbdr.userdevice = diskNumber;
                vbdr.mode = Types.VbdMode.RW;
                vbdr.type = Types.VbdType.DISK;
                vbdr.unpluggable = true;
                VBD vbd = VBD.create(conn, vbdr);

                // Attach the VBD to the VM
                vbd.plug(conn);

                // Update the VDI's label to include the VM name
                vdi.setNameLabel(conn, vmName + "-DATA");

                return new AttachVolumeAnswer(cmd, Long.parseLong(diskNumber));
            } else {
                // Look up all VBDs for this VDI
                Set<VBD> vbds = vdi.getVBDs(conn);

                // Detach each VBD from its VM, and then destroy it
                for (VBD vbd : vbds) {
                    VBD.Record vbdr = vbd.getRecord(conn);

                    if (vbdr.currentlyAttached) {
                        vbd.unplug(conn);
                    }

                    vbd.destroy(conn);
                }

                // Update the VDI's label to be "detached"
                vdi.setNameLabel(conn, "detached");
                
                umount(conn, vdi);

                return new AttachVolumeAnswer(cmd);
            }
        } catch (XenAPIException e) {
            String msg = errorMsg + " for uuid: " + cmd.getVolumePath() + "  due to " + e.toString();
            s_logger.warn(msg, e);
            return new AttachVolumeAnswer(cmd, msg);
        } catch (Exception e) {
            String msg = errorMsg + " for uuid: " + cmd.getVolumePath() + "  due to "  + e.getMessage();
            s_logger.warn(msg, e);
            return new AttachVolumeAnswer(cmd, msg);
        }

    }

    protected void umount(Connection conn, VDI vdi) {
        
    }

    protected Answer execute(final AttachIsoCommand cmd) {
        Connection conn = getConnection();
        boolean attach = cmd.isAttach();
        String vmName = cmd.getVmName();
        String isoURL = cmd.getIsoPath();

        String errorMsg;
        if (attach) {
            errorMsg = "Failed to attach ISO";
        } else {
            errorMsg = "Failed to detach ISO";
        }
        try {
            if (attach) {
                VBD isoVBD = null;

                // Find the VM
                VM vm = getVM(conn, vmName);

                // Find the ISO VDI
                VDI isoVDI = getIsoVDIByURL(conn, vmName, isoURL);

                // Find the VM's CD-ROM VBD
                Set<VBD> vbds = vm.getVBDs(conn);
                for (VBD vbd : vbds) {
                    String userDevice = vbd.getUserdevice(conn);
                    Types.VbdType type = vbd.getType(conn);

                    if (userDevice.equals("3") && type == Types.VbdType.CD) {
                        isoVBD = vbd;
                        break;
                    }
                }

                if (isoVBD == null) {
                    throw new CloudRuntimeException("Unable to find CD-ROM VBD for VM: " + vmName);
                } else {
                    // If an ISO is already inserted, eject it
                    if (isoVBD.getEmpty(conn) == false) {
                        isoVBD.eject(conn);
                    }

                    // Insert the new ISO
                    isoVBD.insert(conn, isoVDI);
                }

                return new Answer(cmd);
            } else {
                // Find the VM
                VM vm = getVM(conn, vmName);
                String vmUUID = vm.getUuid(conn);

                // Find the ISO VDI
                VDI isoVDI = getIsoVDIByURL(conn, vmName, isoURL);

                SR sr = isoVDI.getSR(conn);

                // Look up all VBDs for this VDI
                Set<VBD> vbds = isoVDI.getVBDs(conn);

                // Iterate through VBDs, and if the VBD belongs the VM, eject
                // the ISO from it
                for (VBD vbd : vbds) {
                    VM vbdVM = vbd.getVM(conn);
                    String vbdVmUUID = vbdVM.getUuid(conn);

                    if (vbdVmUUID.equals(vmUUID)) {
                        // If an ISO is already inserted, eject it
                        if (!vbd.getEmpty(conn)) {
                            vbd.eject(conn);
                        }

                        break;
                    }
                }

                if (!sr.getNameLabel(conn).startsWith("XenServer Tools")) {
                    removeSR(conn, sr);
                }

                return new Answer(cmd);
            }
        } catch (XenAPIException e) {
            s_logger.warn(errorMsg + ": " + e.toString(), e);
            return new Answer(cmd, false, e.toString());
        } catch (Exception e) {
            s_logger.warn(errorMsg + ": " + e.toString(), e);
            return new Answer(cmd, false, e.getMessage());
        }
    }

    boolean IsISCSI(String type) {
        return SRType.LVMOISCSI.equals(type) || SRType.LVM.equals(type) ;
    }
    
    protected ManageSnapshotAnswer execute(final ManageSnapshotCommand cmd) {
        Connection conn = getConnection();
        long snapshotId = cmd.getSnapshotId();
        String snapshotName = cmd.getSnapshotName();

        // By default assume failure
        boolean success = false;
        String cmdSwitch = cmd.getCommandSwitch();
        String snapshotOp = "Unsupported snapshot command." + cmdSwitch;
        if (cmdSwitch.equals(ManageSnapshotCommand.CREATE_SNAPSHOT)) {
            snapshotOp = "create";
        } else if (cmdSwitch.equals(ManageSnapshotCommand.DESTROY_SNAPSHOT)) {
            snapshotOp = "destroy";
        }
        String details = "ManageSnapshotCommand operation: " + snapshotOp + " Failed for snapshotId: " + snapshotId;
        String snapshotUUID = null;

        try {
            if (cmdSwitch.equals(ManageSnapshotCommand.CREATE_SNAPSHOT)) {
                // Look up the volume
                String volumeUUID = cmd.getVolumePath();
                VDI volume = VDI.getByUuid(conn, volumeUUID);

                // Create a snapshot
                VDI snapshot = volume.snapshot(conn, new HashMap<String, String>());
                
                if (snapshotName != null) {
                    snapshot.setNameLabel(conn, snapshotName);
                }
                // Determine the UUID of the snapshot

                snapshotUUID = snapshot.getUuid(conn);
                String preSnapshotUUID = cmd.getSnapshotPath();
                //check if it is a empty snapshot
                if( preSnapshotUUID != null) {
                    SR sr = volume.getSR(conn);
                    String srUUID = sr.getUuid(conn);
                    String type = sr.getType(conn);
                    Boolean isISCSI = IsISCSI(type);
                    String snapshotParentUUID = getVhdParent(conn, srUUID, snapshotUUID, isISCSI);
                    
                    String preSnapshotParentUUID = getVhdParent(conn, srUUID, preSnapshotUUID, isISCSI);
                    if( snapshotParentUUID != null && snapshotParentUUID.equals(preSnapshotParentUUID)) {
                        // this is empty snapshot, remove it
                        snapshot.destroy(conn);
                        snapshotUUID = preSnapshotUUID;
                    }
                    
                }
                
                success = true;
                details = null;
            } else if (cmd.getCommandSwitch().equals(ManageSnapshotCommand.DESTROY_SNAPSHOT)) {
                // Look up the snapshot
                snapshotUUID = cmd.getSnapshotPath();
                VDI snapshot = getVDIbyUuid(conn, snapshotUUID);

                snapshot.destroy(conn);
                snapshotUUID = null;
                success = true;
                details = null;
            }
        } catch (XenAPIException e) {
            details += ", reason: " + e.toString();
            s_logger.warn(details, e);
        } catch (Exception e) {
            details += ", reason: " + e.toString();
            s_logger.warn(details, e);
        }

        return new ManageSnapshotAnswer(cmd, snapshotId, snapshotUUID, success, details);
    }

    protected CreatePrivateTemplateAnswer execute(final CreatePrivateTemplateFromVolumeCommand cmd) {
        Connection conn = getConnection();
        String secondaryStoragePoolURL = cmd.getSecondaryStorageURL();
        String volumeUUID = cmd.getVolumePath();
        Long accountId = cmd.getAccountId();
        String userSpecifiedName = cmd.getTemplateName();
        Long templateId = cmd.getTemplateId();

        String details = null;
        SR tmpltSR = null;
        boolean result = false;
        try {
            
            URI uri = new URI(secondaryStoragePoolURL);
            String secondaryStorageMountPath = uri.getHost() + ":" + uri.getPath();
            String installPath = "template/tmpl/" + accountId + "/" + templateId;
            if( !createSecondaryStorageFolder(conn, secondaryStorageMountPath, installPath)) {
                details = " Filed to create folder " + installPath + " in secondary storage";
                s_logger.warn(details);
                return new CreatePrivateTemplateAnswer(cmd, false, details);
            }
            VDI volume = getVDIbyUuid(conn, volumeUUID);
            // create template SR
            URI tmpltURI = new URI(secondaryStoragePoolURL + "/" + installPath);
            tmpltSR = createNfsSRbyURI(conn, tmpltURI, false);

            // copy volume to template SR
            VDI tmpltVDI = cloudVDIcopy(conn, volume, tmpltSR);
            
            if (userSpecifiedName != null) {
                tmpltVDI.setNameLabel(conn, userSpecifiedName);
            }

            String tmpltSrUUID = tmpltSR.getUuid(conn);
            String tmpltUUID = tmpltVDI.getUuid(conn);
            String tmpltFilename = tmpltUUID + ".vhd";
            long virtualSize = tmpltVDI.getVirtualSize(conn);
            long physicalSize = tmpltVDI.getPhysicalUtilisation(conn);
            // create the template.properties file
            result = postCreatePrivateTemplate(conn, tmpltSrUUID, tmpltFilename, tmpltUUID, userSpecifiedName, null, physicalSize, virtualSize, templateId);
            if (!result) {
                throw new CloudRuntimeException("Could not create the template.properties file on secondary storage dir: " + tmpltURI);
            }
            installPath = installPath + "/" + tmpltFilename;
            return new CreatePrivateTemplateAnswer(cmd, true, null, installPath, virtualSize, physicalSize, tmpltUUID, ImageFormat.VHD);
        } catch (XenAPIException e) {
            details = "Creating template from volume " + volumeUUID + " failed due to " + e.getMessage();
            s_logger.error(details, e);
        } catch (Exception e) {
            details = "Creating template from volume " + volumeUUID + " failed due to " + e.getMessage();
            s_logger.error(details, e);
        } finally {
            // Remove the secondary storage SR
            removeSR(conn, tmpltSR);
        }
        return new CreatePrivateTemplateAnswer(cmd, result, details);
    }

    protected CreatePrivateTemplateAnswer execute(final CreatePrivateTemplateFromSnapshotCommand cmd) {
        Connection conn = getConnection();
        Long accountId = cmd.getAccountId();
        Long volumeId = cmd.getVolumeId();
        String secondaryStoragePoolURL = cmd.getSecondaryStoragePoolURL();
        String backedUpSnapshotUuid = cmd.getSnapshotUuid();
        Long newTemplateId = cmd.getNewTemplateId();
        String userSpecifiedName = cmd.getTemplateName();

        // By default, assume failure
        String details = null;
        SR snapshotSR = null;
        SR tmpltSR = null; 
        boolean result = false;
        try {
            URI uri = new URI(secondaryStoragePoolURL);
            String secondaryStorageMountPath = uri.getHost() + ":" + uri.getPath();
            String installPath = "template/tmpl/" + accountId + "/" + newTemplateId;
            if( !createSecondaryStorageFolder(conn, secondaryStorageMountPath, installPath)) {
                details = " Filed to create folder " + installPath + " in secondary storage";
                s_logger.warn(details);
                return new CreatePrivateTemplateAnswer(cmd, false, details);
            }
            // create snapshot SR
            URI snapshotURI = new URI(secondaryStoragePoolURL + "/snapshots/" + accountId + "/" + volumeId );
            snapshotSR = createNfsSRbyURI(conn, snapshotURI, false);
            snapshotSR.scan(conn);
            VDI snapshotVDI = getVDIbyUuid(conn, backedUpSnapshotUuid);
            
            // create template SR
            URI tmpltURI = new URI(secondaryStoragePoolURL + "/" + installPath);
            tmpltSR = createNfsSRbyURI(conn, tmpltURI, false);
            // copy snapshotVDI to template SR
            VDI tmpltVDI = cloudVDIcopy(conn, snapshotVDI, tmpltSR);
            
            String tmpltSrUUID = tmpltSR.getUuid(conn);
            String tmpltUUID = tmpltVDI.getUuid(conn);
            String tmpltFilename = tmpltUUID + ".vhd";
            long virtualSize = tmpltVDI.getVirtualSize(conn);
            long physicalSize = tmpltVDI.getPhysicalUtilisation(conn);

            // create the template.properties file
            result = postCreatePrivateTemplate(conn, tmpltSrUUID, tmpltFilename, tmpltUUID, userSpecifiedName, null, physicalSize, virtualSize, newTemplateId);
            if (!result) {
                throw new CloudRuntimeException("Could not create the template.properties file on secondary storage dir: " + tmpltURI);
            } 
            installPath = installPath + "/" + tmpltFilename;
            return new CreatePrivateTemplateAnswer(cmd, true, null, installPath, virtualSize, physicalSize, tmpltUUID, ImageFormat.VHD);
        } catch (XenAPIException e) {
            details = "Creating template from snapshot " + backedUpSnapshotUuid + " failed due to " + e.getMessage();
            s_logger.error(details, e);
        } catch (Exception e) {
            details = "Creating template from snapshot " + backedUpSnapshotUuid + " failed due to " + e.getMessage();
            s_logger.error(details, e);
        } finally {
            // Remove the secondary storage SR
            removeSR(conn, snapshotSR);
            removeSR(conn, tmpltSR);
        }
        return new CreatePrivateTemplateAnswer(cmd, result, details);
    }
    
    private boolean destroySnapshotOnPrimaryStorageExceptThis(Connection conn, String volumeUuid, String avoidSnapshotUuid){
        try {
            VDI volume = getVDIbyUuid(conn, volumeUuid);
            if (volume == null) {
                throw new InternalErrorException("Could not destroy snapshot on volume " + volumeUuid + " due to can not find it");
            }
            Set<VDI> snapshots = volume.getSnapshots(conn);
            for( VDI snapshot : snapshots ) {
                    try {
                            if(! snapshot.getUuid(conn).equals(avoidSnapshotUuid)) {
                            snapshot.destroy(conn);
                            }
                } catch (Exception e) {
                    String msg = "Destroying snapshot: " + snapshot+ " on primary storage failed due to " + e.toString();
                    s_logger.warn(msg, e);
                }
            }
            s_logger.debug("Successfully destroyed snapshot on volume: " + volumeUuid + " execept this current snapshot "+ avoidSnapshotUuid );
            return true;
        } catch (XenAPIException e) {
            String msg = "Destroying snapshot on volume: " + volumeUuid + " execept this current snapshot "+ avoidSnapshotUuid + " failed due to " + e.toString();
            s_logger.error(msg, e);
        } catch (Exception e) {
            String msg = "Destroying snapshot on volume: " + volumeUuid + " execept this current snapshot "+ avoidSnapshotUuid + " failed due to " + e.toString();
            s_logger.warn(msg, e);
        }

        return false;
    }

    

    protected BackupSnapshotAnswer execute(final BackupSnapshotCommand cmd) {
        Connection conn = getConnection();
        String primaryStorageNameLabel = cmd.getPrimaryStoragePoolNameLabel();
        Long dcId = cmd.getDataCenterId();
        Long accountId = cmd.getAccountId();
        Long volumeId = cmd.getVolumeId();
        String secondaryStoragePoolURL = cmd.getSecondaryStoragePoolURL();
        String snapshotUuid = cmd.getSnapshotUuid(); // not null: Precondition.
        String prevBackupUuid = cmd.getPrevBackupUuid();
        // By default assume failure
        String details = null;
        boolean success = false;
        String snapshotBackupUuid = null;
        try {
            SR primaryStorageSR = getSRByNameLabelandHost(conn, primaryStorageNameLabel);
            if (primaryStorageSR == null) {
                throw new InternalErrorException("Could not backup snapshot because the primary Storage SR could not be created from the name label: " + primaryStorageNameLabel);
            }

            URI uri = new URI(secondaryStoragePoolURL);
            String secondaryStorageMountPath = uri.getHost() + ":" + uri.getPath();
            

            if (prevBackupUuid == null) {
                // the first snapshot is always a full snapshot
                String folder = "snapshots/" + accountId + "/" + volumeId;
                if( !createSecondaryStorageFolder(conn, secondaryStorageMountPath, folder)) {
                    details = " Filed to create folder " + folder + " in secondary storage";
                    s_logger.warn(details);
                    return new BackupSnapshotAnswer(cmd, success, details, snapshotBackupUuid);
                }

                String snapshotMountpoint = secondaryStoragePoolURL + "/" + folder;
                SR snapshotSr = null;
                try {
                    snapshotSr = createNfsSRbyURI(conn, new URI(snapshotMountpoint), false);
                    VDI snapshotVdi = getVDIbyUuid(conn, snapshotUuid);
                    VDI backedVdi = cloudVDIcopy(conn, snapshotVdi, snapshotSr);
                    snapshotBackupUuid = backedVdi.getUuid(conn);
                    success = true;
                } finally {
                    if( snapshotSr != null) {
                        removeSR(conn, snapshotSr);
                    }
                }
            } else {
                    String primaryStorageSRUuid = primaryStorageSR.getUuid(conn);
                    Boolean isISCSI = IsISCSI(primaryStorageSR.getType(conn));
                    snapshotBackupUuid = backupSnapshot(conn, primaryStorageSRUuid, dcId, accountId, volumeId, secondaryStorageMountPath, 
                            snapshotUuid, prevBackupUuid, isISCSI);
                    success = (snapshotBackupUuid != null);
            }

            if (success) {
                details = "Successfully backedUp the snapshotUuid: " + snapshotUuid + " to secondary storage.";
                
                String volumeUuid = cmd.getVolumePath();
                destroySnapshotOnPrimaryStorageExceptThis(conn, volumeUuid, snapshotUuid);

            }

        } catch (XenAPIException e) {
            details = "BackupSnapshot Failed due to " + e.toString();
            s_logger.warn(details, e);
        } catch (Exception e) {
            details = "BackupSnapshot Failed due to " + e.getMessage();
            s_logger.warn(details, e);
        }

        return new BackupSnapshotAnswer(cmd, success, details, snapshotBackupUuid);
    }

    protected CreateVolumeFromSnapshotAnswer execute(final CreateVolumeFromSnapshotCommand cmd) {
        Connection conn = getConnection();
        String primaryStorageNameLabel = cmd.getPrimaryStoragePoolNameLabel();
        Long accountId = cmd.getAccountId();
        Long volumeId = cmd.getVolumeId();
        String secondaryStoragePoolURL = cmd.getSecondaryStoragePoolURL();
        String backedUpSnapshotUuid = cmd.getSnapshotUuid();

        // By default, assume the command has failed and set the params to be
        // passed to CreateVolumeFromSnapshotAnswer appropriately
        boolean result = false;
        // Generic error message.
        String details = null;
        String volumeUUID = null;
        SR snapshotSR = null;
        
        if (secondaryStoragePoolURL == null) {
            details += " because the URL passed: " + secondaryStoragePoolURL + " is invalid.";
            return new CreateVolumeFromSnapshotAnswer(cmd, result, details, volumeUUID);
        }
        try {
            SR primaryStorageSR = getSRByNameLabelandHost(conn, primaryStorageNameLabel);
            if (primaryStorageSR == null) {
                throw new InternalErrorException("Could not create volume from snapshot because the primary Storage SR could not be created from the name label: "
                        + primaryStorageNameLabel);
            }
            // Get the absolute path of the snapshot on the secondary storage.
            URI snapshotURI = new URI(secondaryStoragePoolURL + "/snapshots/" + accountId + "/" + volumeId );
            
            snapshotSR = createNfsSRbyURI(conn, snapshotURI, false);
            snapshotSR.scan(conn);
            VDI snapshotVDI = getVDIbyUuid(conn, backedUpSnapshotUuid);

            VDI volumeVDI = cloudVDIcopy(conn, snapshotVDI, primaryStorageSR);
            
            volumeUUID = volumeVDI.getUuid(conn);
            
            
            result = true;

        } catch (XenAPIException e) {
            details += " due to " + e.toString();
            s_logger.warn(details, e);
        } catch (Exception e) {
            details += " due to " + e.getMessage();
            s_logger.warn(details, e);
        } finally {
            // In all cases, if the temporary SR was created, forget it.
            if (snapshotSR != null) {
                removeSR(conn, snapshotSR);
            }
        }
        if (!result) {
            // Is this logged at a higher level?
            s_logger.error(details);
        }

        // In all cases return something.
        return new CreateVolumeFromSnapshotAnswer(cmd, result, details, volumeUUID);
    }

    protected DeleteSnapshotBackupAnswer execute(final DeleteSnapshotBackupCommand cmd) {
        Connection conn = getConnection();
        Long dcId = cmd.getDataCenterId();
        Long accountId = cmd.getAccountId();
        Long volumeId = cmd.getVolumeId();
        String secondaryStoragePoolURL = cmd.getSecondaryStoragePoolURL();
        String backupUUID = cmd.getSnapshotUuid();
        String details = null;
        boolean success = false;

        URI uri = null;
        try {
            uri = new URI(secondaryStoragePoolURL);
        } catch (URISyntaxException e) {
            details = "Error finding the secondary storage URL" + e.getMessage();
            s_logger.error(details, e);
        }
        if (uri != null) {
            String secondaryStorageMountPath = uri.getHost() + ":" + uri.getPath();

            if (secondaryStorageMountPath == null) {
                details = "Couldn't delete snapshot because the URL passed: " + secondaryStoragePoolURL
                        + " is invalid.";
            } else {
                details = deleteSnapshotBackup(conn, dcId, accountId, volumeId, secondaryStorageMountPath, backupUUID);
                success = (details != null && details.equals("1"));
                if (success) {
                    s_logger.debug("Successfully deleted snapshot backup " + backupUUID);
                }
            }
        }
        return new DeleteSnapshotBackupAnswer(cmd, success, details);
    }

    protected Answer execute(DeleteSnapshotsDirCommand cmd) {
        Connection conn = getConnection();
        Long dcId = cmd.getDataCenterId();
        Long accountId = cmd.getAccountId();
        Long volumeId = cmd.getVolumeId();
        String secondaryStoragePoolURL = cmd.getSecondaryStoragePoolURL();
        String snapshotUUID = cmd.getSnapshotUuid();
        String primaryStorageNameLabel = cmd.getPrimaryStoragePoolNameLabel();

        String details = null;
        boolean success = false;

        SR primaryStorageSR = null;
        try {
            primaryStorageSR = getSRByNameLabelandHost(conn, primaryStorageNameLabel);
            if (primaryStorageSR == null) {
                details = "Primary Storage SR could not be created from the name label: " + primaryStorageNameLabel;
            }
        } catch (XenAPIException e) {
            details = "Couldn't determine primary SR type " + e.getMessage();
            s_logger.error(details, e);
        } catch (Exception e) {
            details = "Couldn't determine primary SR type " + e.getMessage();
            s_logger.error(details, e);
        }

        if (primaryStorageSR != null) {
            if (snapshotUUID != null) {
                VDI snapshotVDI = getVDIbyUuid(conn, snapshotUUID);
                if (snapshotVDI != null) {
                    destroyVDI(conn, snapshotVDI);
                }
            }
        }
        URI uri = null;
        try {
            uri = new URI(secondaryStoragePoolURL);
        } catch (URISyntaxException e) {
            details = "Error finding the secondary storage URL" + e.getMessage();
            s_logger.error(details, e);
        }
        if (uri != null) {
            String secondaryStorageMountPath = uri.getHost() + ":" + uri.getPath();

            if (secondaryStorageMountPath == null) {
                details = "Couldn't delete snapshotsDir because the URL passed: " + secondaryStoragePoolURL + " is invalid.";
            } else {
                details = deleteSnapshotsDir(conn, dcId, accountId, volumeId, secondaryStorageMountPath);
                success = (details != null && details.equals("1"));
                if (success) {
                    s_logger.debug("Successfully deleted snapshotsDir for volume: " + volumeId);
                }
            }
        }

        return new Answer(cmd, success, details);
    }

    protected VM getVM(Connection conn, String vmName) {
        // Look up VMs with the specified name
        Set<VM> vms;
        try {
            vms = VM.getByNameLabel(conn, vmName);
        } catch (XenAPIException e) {
            throw new CloudRuntimeException("Unable to get " + vmName + ": " + e.toString(), e);
        } catch (Exception e) {
            throw new CloudRuntimeException("Unable to get " + vmName + ": " + e.getMessage(), e);
        }

        // If there are no VMs, throw an exception
        if (vms.size() == 0) {
            throw new CloudRuntimeException("VM with name: " + vmName + " does not exist.");
        }

        // If there is more than one VM, print a warning
        if (vms.size() > 1) {
            s_logger.warn("Found " + vms.size() + " VMs with name: " + vmName);
        }

        // Return the first VM in the set
        return vms.iterator().next();
    }

    protected VDI getIsoVDIByURL(Connection conn, String vmName, String isoURL) {
        SR isoSR = null;
        String mountpoint = null;
        if (isoURL.startsWith("xs-tools")) {
            try {
                Set<VDI> vdis = VDI.getByNameLabel(conn, isoURL);
                if (vdis.isEmpty()) {
                    throw new CloudRuntimeException("Could not find ISO with URL: " + isoURL);
                }
                return vdis.iterator().next();

            } catch (XenAPIException e) {
                throw new CloudRuntimeException("Unable to get pv iso: " + isoURL + " due to " + e.toString());
            } catch (Exception e) {
                throw new CloudRuntimeException("Unable to get pv iso: " + isoURL + " due to " + e.toString());
            }
        }

        int index = isoURL.lastIndexOf("/");
        mountpoint = isoURL.substring(0, index);

        URI uri;
        try {
            uri = new URI(mountpoint);
        } catch (URISyntaxException e) {
            throw new CloudRuntimeException("isoURL is wrong: " + isoURL);
        }
        isoSR = getISOSRbyVmName(conn, vmName);
        if (isoSR == null) {
            isoSR = createIsoSRbyURI(conn, uri, vmName, false);
        }

        String isoName = isoURL.substring(index + 1);

        VDI isoVDI = getVDIbyLocationandSR(conn, isoName, isoSR);

        if (isoVDI != null) {
            return isoVDI;
        } else {
            throw new CloudRuntimeException("Could not find ISO with URL: " + isoURL);
        }
    }

    protected SR getStorageRepository(Connection conn, StorageFilerTO pool) {
        Set<SR> srs;
        try {
            srs = SR.getByNameLabel(conn, pool.getUuid());
        } catch (XenAPIException e) {
            throw new CloudRuntimeException("Unable to get SR " + pool.getUuid() + " due to " + e.toString(), e);
        } catch (Exception e) {
            throw new CloudRuntimeException("Unable to get SR " + pool.getUuid() + " due to " + e.getMessage(), e);
        }

        if (srs.size() > 1) {
            throw new CloudRuntimeException("More than one storage repository was found for pool with uuid: " + pool.getUuid());
        } else if (srs.size() == 1) {
            SR sr = srs.iterator().next();
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("SR retrieved for " + pool.getId() + " is mapped to " + sr.toString());
            }

            if (checkSR(conn, sr)) {
                return sr;
            }
            throw new CloudRuntimeException("SR check failed for storage pool: " + pool.getUuid() + "on host:" + _host.uuid);
        } else {
            
	
	        if (pool.getType() == StoragePoolType.NetworkFilesystem) {
                return getNfsSR(conn, pool);
            } else if (pool.getType() == StoragePoolType.IscsiLUN) {
                return getIscsiSR(conn, pool);
            } else {
                throw new CloudRuntimeException("The pool type: " + pool.getType().name() + " is not supported.");
            }
        }

    }

    protected Answer execute(final CheckConsoleProxyLoadCommand cmd) {
        return executeProxyLoadScan(cmd, cmd.getProxyVmId(), cmd.getProxyVmName(), cmd.getProxyManagementIp(), cmd.getProxyCmdPort());
    }

    protected Answer execute(final WatchConsoleProxyLoadCommand cmd) {
        return executeProxyLoadScan(cmd, cmd.getProxyVmId(), cmd.getProxyVmName(), cmd.getProxyManagementIp(), cmd.getProxyCmdPort());
    }

    protected Answer executeProxyLoadScan(final Command cmd, final long proxyVmId, final String proxyVmName, final String proxyManagementIp, final int cmdPort) {
        String result = null;

        final StringBuffer sb = new StringBuffer();
        sb.append("http://").append(proxyManagementIp).append(":" + cmdPort).append("/cmd/getstatus");

        boolean success = true;
        try {
            final URL url = new URL(sb.toString());
            final URLConnection conn = url.openConnection();

            // setting TIMEOUTs to avoid possible waiting until death situations
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            final InputStream is = conn.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            final StringBuilder sb2 = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb2.append(line + "\n");
                }
                result = sb2.toString();
            } catch (final IOException e) {
                success = false;
            } finally {
                try {
                    is.close();
                } catch (final IOException e) {
                    s_logger.warn("Exception when closing , console proxy address : " + proxyManagementIp);
                    success = false;
                }
            }
        } catch (final IOException e) {
            s_logger.warn("Unable to open console proxy command port url, console proxy address : " + proxyManagementIp);
            success = false;
        }

        return new ConsoleProxyLoadAnswer(cmd, proxyVmId, proxyVmName, success, result);
    }

    protected boolean createSecondaryStorageFolder(Connection conn, String remoteMountPath, String newFolder) {
        String result = callHostPlugin(conn, "vmopsSnapshot", "create_secondary_storage_folder", "remoteMountPath", remoteMountPath, "newFolder", newFolder);
        return (result != null);
    }

    protected boolean deleteSecondaryStorageFolder(Connection conn, String remoteMountPath, String folder) {
        String result = callHostPlugin(conn, "vmopsSnapshot", "delete_secondary_storage_folder", "remoteMountPath", remoteMountPath, "folder", folder);
        return (result != null);
    }

    protected boolean postCreatePrivateTemplate(Connection conn, String tmpltSrUUID,String tmpltFilename, String templateName, String templateDescription, String checksum, long size, long virtualSize, long templateId) {

        if (templateDescription == null) {
            templateDescription = "";
        }

        if (checksum == null) {
            checksum = "";
        }

        String result = callHostPluginWithTimeOut(conn, "vmopsSnapshot", "post_create_private_template", 110*60, "tmpltSrUUID", tmpltSrUUID, "templateFilename", tmpltFilename, "templateName", templateName, "templateDescription", templateDescription,
                "checksum", checksum, "size", String.valueOf(size), "virtualSize", String.valueOf(virtualSize), "templateId", String.valueOf(templateId));

        boolean success = false;
        if (result != null && !result.isEmpty()) {
            // Else, command threw an exception which has already been logged.

            if (result.equalsIgnoreCase("1")) {
                s_logger.debug("Successfully created template.properties file on secondary storage for " + tmpltFilename);
                success = true;
            } else {
                s_logger.warn("Could not create template.properties file on secondary storage for " + tmpltFilename + " for templateId: " + templateId);
            }
        }

        return success;
    }

    // Each argument is put in a separate line for readability.
    // Using more lines does not harm the environment.
    protected String backupSnapshot(Connection conn, String primaryStorageSRUuid, Long dcId, Long accountId, Long volumeId, String secondaryStorageMountPath,
            String snapshotUuid, String prevBackupUuid, Boolean isISCSI) {
        String backupSnapshotUuid = null;

        if (prevBackupUuid == null) {
            prevBackupUuid = "";
        }

        // Each argument is put in a separate line for readability.
        // Using more lines does not harm the environment.
        String results = callHostPluginWithTimeOut(conn, "vmopsSnapshot", "backupSnapshot", 110*60, "primaryStorageSRUuid", primaryStorageSRUuid, "dcId", 
                dcId.toString(), "accountId", accountId.toString(), "volumeId", volumeId.toString(), "secondaryStorageMountPath", 
                secondaryStorageMountPath, "snapshotUuid", snapshotUuid, "prevBackupUuid", prevBackupUuid, "isISCSI", isISCSI.toString());

        if (results == null || results.isEmpty()) {
            // errString is already logged.
            return null;
        }

        String[] tmp = results.split("#");
        String status = tmp[0];
        backupSnapshotUuid = tmp[1];

        // status == "1" if and only if backupSnapshotUuid != null
        // So we don't rely on status value but return backupSnapshotUuid as an
        // indicator of success.
        String failureString = "Could not copy backupUuid: " + backupSnapshotUuid + " of volumeId: " + volumeId + " from primary storage " + primaryStorageSRUuid
                + " to secondary storage " + secondaryStorageMountPath;
        if (status != null && status.equalsIgnoreCase("1") && backupSnapshotUuid != null) {
            s_logger.debug("Successfully copied backupUuid: " + backupSnapshotUuid + " of volumeId: " + volumeId + " to secondary storage");
        } else {
            s_logger.debug(failureString + ". Failed with status: " + status);
            return null;
        }

        return backupSnapshotUuid;
    }
    
    
    protected String getVhdParent(Connection conn, String primaryStorageSRUuid, String snapshotUuid, Boolean isISCSI) {
        String parentUuid = callHostPlugin(conn, "vmopsSnapshot", "getVhdParent", "primaryStorageSRUuid", primaryStorageSRUuid, 
                "snapshotUuid", snapshotUuid, "isISCSI", isISCSI.toString());

        if (parentUuid == null || parentUuid.isEmpty()) {
            s_logger.debug("Unable to get parent of VHD " + snapshotUuid + " in SR " + primaryStorageSRUuid);
            // errString is already logged.
            return null;
        }
        return parentUuid;
    }

    protected boolean destroySnapshotOnPrimaryStorage(Connection conn, String snapshotUuid) {
        // Precondition snapshotUuid != null
        try {
            VDI snapshot = getVDIbyUuid(conn, snapshotUuid);
            if (snapshot == null) {
                throw new InternalErrorException("Could not destroy snapshot " + snapshotUuid + " because the snapshot VDI was null");
            }
            snapshot.destroy(conn);
            s_logger.debug("Successfully destroyed snapshotUuid: " + snapshotUuid + " on primary storage");
            return true;
        } catch (XenAPIException e) {
            String msg = "Destroy snapshotUuid: " + snapshotUuid + " on primary storage failed due to " + e.toString();
            s_logger.error(msg, e);
        } catch (Exception e) {
            String msg = "Destroy snapshotUuid: " + snapshotUuid + " on primary storage failed due to " + e.getMessage();
            s_logger.warn(msg, e);
        }

        return false;
    }

    protected String deleteSnapshotBackup(Connection conn, Long dcId, Long accountId, Long volumeId, String secondaryStorageMountPath, String backupUUID) {

        // If anybody modifies the formatting below again, I'll skin them
        String result = callHostPlugin(conn, "vmopsSnapshot", "deleteSnapshotBackup", "backupUUID", backupUUID, "dcId", dcId.toString(), "accountId", accountId.toString(),
                "volumeId", volumeId.toString(), "secondaryStorageMountPath", secondaryStorageMountPath);

        return result;
    }

    protected String deleteSnapshotsDir(Connection conn, Long dcId, Long accountId, Long volumeId, String secondaryStorageMountPath) {
        // If anybody modifies the formatting below again, I'll skin them
        String result = callHostPlugin(conn, "vmopsSnapshot", "deleteSnapshotsDir", "dcId", dcId.toString(), "accountId", accountId.toString(), "volumeId", volumeId.toString(),
                "secondaryStorageMountPath", secondaryStorageMountPath);

        return result;
    }


    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        disconnected();
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public IAgentControl getAgentControl() {
        return _agentControl;
    }

    @Override
    public void setAgentControl(IAgentControl agentControl) {
        _agentControl = agentControl;
    }
    
    protected Answer execute(PoolEjectCommand cmd) {
        Connection conn = getConnection();
        String hostuuid = cmd.getHostuuid();
        try {
            Map<Host, Host.Record> hostrs = Host.getAllRecords(conn);
            boolean found = false;
            for( Host.Record hr : hostrs.values() ) {
            	if( hr.uuid.equals(hostuuid)) {
            		found = true;
            	}
            }
            if( ! found) {
                s_logger.debug("host " + hostuuid + " has already been ejected from pool " + _host.pool);
                return new Answer(cmd);
            }
            
            Pool pool = Pool.getAll(conn).iterator().next();
            Pool.Record poolr = pool.getRecord(conn);

            Host.Record masterRec = poolr.master.getRecord(conn);
            if (hostuuid.equals(masterRec.uuid)) {
                s_logger.debug("This is last host to eject, so don't need to eject: " + hostuuid);
                return new Answer(cmd);
            }
            
            Host host = Host.getByUuid(conn, hostuuid);
            // remove all tags cloud stack add before eject
            Host.Record hr = host.getRecord(conn);
            Iterator<String> it = hr.tags.iterator();
            while (it.hasNext()) {
                String tag = it.next();
                if (tag.startsWith("vmops-version-")) {
                    it.remove();
                }
            }
            // eject from pool
            try {
            	Pool.eject(conn, host);
            	try {
            	    Thread.sleep(10 * 1000);
            	} catch (InterruptedException e) {
            	}
            } catch (XenAPIException e) {
                String msg = "Unable to eject host " + _host.uuid + " due to " + e.toString();
                s_logger.warn(msg);   
                host.destroy(conn);
            }
            return new Answer(cmd);
        } catch (XenAPIException e) {
            String msg = "XenAPIException Unable to destroy host " + _host.uuid + " in xenserver database due to " + e.toString();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        } catch (Exception e) {
            String msg = "Exception Unable to destroy host " + _host.uuid + " in xenserver database due to " + e.getMessage();
            s_logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        } 
    }

    protected class Nic {
        public Network n;
        public Network.Record nr;
        public PIF p;
        public PIF.Record pr;

        public Nic(Network n, Network.Record nr, PIF p, PIF.Record pr) {
            this.n = n;
            this.nr = nr;
            this.p = p;
            this.pr = pr;
        }
    }

    // A list of UUIDs that are gathered from the XenServer when
    // the resource first connects to XenServer. These UUIDs do
    // not change over time.
    protected class XenServerHost {
        public String systemvmisouuid;
        public String uuid;
        public String ip;
        public String publicNetwork;
        public String privateNetwork;
        public String linkLocalNetwork;
        public Network vswitchNetwork;
        public String storageNetwork1;
        public String storageNetwork2;
        public String guestNetwork;
        public String guestPif;
        public String publicPif;
        public String privatePif;
        public String storagePif1;
        public String storagePif2;
        public String pool;
        public int speed;
        public int cpus;
    }

    /*Override by subclass*/
	protected String getGuestOsType(String stdType, boolean bootFromCD) {
		return stdType;
	}
}
