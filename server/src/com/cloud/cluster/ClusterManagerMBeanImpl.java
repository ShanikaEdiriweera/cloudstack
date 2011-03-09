package com.cloud.cluster;

import java.util.Date;
import java.util.TimeZone;

import javax.management.StandardMBean;

import com.cloud.utils.DateUtil;

public class ClusterManagerMBeanImpl extends StandardMBean implements ClusterManagerMBean {
	private ClusterManagerImpl _clusterMgr;
	private ManagementServerHostVO _mshostVo;
	
	public ClusterManagerMBeanImpl(ClusterManagerImpl clusterMgr, ManagementServerHostVO mshostVo) {
		super(ClusterManagerMBean.class, false);
		
		_clusterMgr = clusterMgr;
		_mshostVo = mshostVo;
	}
	
	public long getMsid() {
		return _mshostVo.getMsid();
	}
	
	public String getLastUpdateTime() {
		Date date = _mshostVo.getLastUpdateTime();
		return DateUtil.getDateDisplayString(TimeZone.getDefault(), date);
	}
	
	public String getClusterNodeIP() {
		return _mshostVo.getServiceIP();
	}
	
	public String getVersion() {
		return _mshostVo.getVersion();
	}
	
	public int getHeartbeatInterval() {
		return _clusterMgr.getHeartbeatInterval();
	}
	
	public int getHeartbeatThreshold() {
		return _clusterMgr.getHeartbeatThreshold();
	}
	
	public void setHeartbeatThreshold(int threshold) {
		// to avoid accidentally screwing up cluster manager, we put some guarding logic here
    	if(threshold >= ClusterManager.DEFAULT_HEARTBEAT_THRESHOLD)
    		_clusterMgr.setHeartbeatThreshold(threshold);
	}
}
