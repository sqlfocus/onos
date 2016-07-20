/*
 * Copyright 2014-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.sdnip;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.app.ApplicationService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.incubator.component.ComponentService;
import org.onosproject.incubator.net.intf.InterfaceService;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.routing.IntentSynchronizationAdminService;
import org.onosproject.routing.IntentSynchronizationService;
import org.onosproject.routing.RoutingService;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Component for the SDN-IP peering application.
 */
@Component(immediate = true)
public class SdnIp {

    public static final String SDN_IP_APP = "org.onosproject.sdnip";
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ApplicationService applicationService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService networkConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InterfaceService interfaceService;

    /* 流同步器：集群模式时，节点信息暂时发送到此同步器，后续由集群leader提交变更；
     *           非集群模式，由本节点直接提交变更
     * <NOTE>相当于多了一个中间层， 不过拓展性更好了！ */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentSynchronizationService intentSynchronizer;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentSynchronizationAdminService intentSynchronizerAdmin;

    /* OSGI框架的模块儿管理类 */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentService componentService;

    private PeerConnectivityManager peerConnectivity;

    private ApplicationId appId;

    /* 关联的模块儿，用来和BGP协议的实现模块儿通信 */
    private static List<String> components = new ArrayList<>();
    static {
        components.add("org.onosproject.routing.bgp.BgpSessionManager");    /* BGP的会话管理类，用来管理BGP tcp连接 */
        components.add(org.onosproject.sdnip.SdnIpFib.class.getName());
    }

    @Activate
    protected void activate() {
        components.forEach(name -> componentService.activate(appId, name));

        /* 注册本模块儿到核心服务层 */
        appId = coreService.registerApplication(SDN_IP_APP);

        /* 启动BGP对等关系链接监听 */
        peerConnectivity = new PeerConnectivityManager(appId,
                                                       intentSynchronizer,
                                                       networkConfigService,
                coreService.registerApplication(RoutingService.ROUTER_APP_ID),
                                                       interfaceService);
        peerConnectivity.start();

        /* 注册清除intent接口 */
        applicationService.registerDeactivateHook(appId, () -> {
            intentSynchronizer.removeIntentsByAppId(appId);
        });

        log.info("SDN-IP started");
    }

    @Deactivate
    protected void deactivate() {
        components.forEach(name -> componentService.deactivate(appId, name));

        peerConnectivity.stop();

        log.info("SDN-IP Stopped");
    }

    /**
     * Converts DPIDs of the form xx:xx:xx:xx:xx:xx:xx to OpenFlow provider
     * device URIs.
     *
     * @param dpid the DPID string to convert
     * @return the URI string for this device
     */
    static String dpidToUri(String dpid) {
        return "of:" + dpid.replace(":", "");
    }


}
