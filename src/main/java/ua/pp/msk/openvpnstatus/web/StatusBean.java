/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.openvpnstatus.web;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.ManagedBean;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import org.slf4j.LoggerFactory;
import ua.pp.msk.openvpnstatus.api.Client;
import ua.pp.msk.openvpnstatus.api.Route;
import ua.pp.msk.openvpnstatus.api.Status;
import ua.pp.msk.openvpnstatus.exceptions.OpenVpnIOException;
import ua.pp.msk.openvpnstatus.exceptions.OpenVpnParseException;
import ua.pp.msk.openvpnstatus.net.Connection;
import ua.pp.msk.openvpnstatus.net.ManagementConnection;

/**
 *
 * @author Maksym Shkolnyi aka maskimko
 */
@ManagedBean("status")
@RequestScoped
public class StatusBean {

    private Status status;
    private int port;
    private InetAddress host;
    private Connection mc;

    public StatusBean() {
    }

    public void serPort(int port) {
        this.port = port;
    }

    public void setHost(String host) throws UnknownHostException {
        this.host = Inet4Address.getByName(host);
    }

    public void setHost(InetAddress addr) {
        this.host = addr;
    }

    private void checkStatus() throws OpenVpnParseException, OpenVpnIOException, IOException {
        if (status == null) {
            if (mc == null) {
                mc = ManagementConnection.getConnection(host, port);
            }
            status = mc.getStatus();
        }
    }

    public Calendar getUpdateTime() {
        Calendar ut = null; 
        try {
            checkStatus();
            ut = status.getUpdateTime();
        } catch (IOException ex) {
             LoggerFactory.getLogger(StatusBean.class.getName()).warn("IO error", ex);
        } catch (OpenVpnIOException ex ) {
              LoggerFactory.getLogger(StatusBean.class.getName()).warn("VPN IO error", ex);
        } catch (OpenVpnParseException ex) {
             LoggerFactory.getLogger(StatusBean.class.getName()).warn("Parsing error", ex);
        }
        return ut;
    }

    public List<Client> getConnectedClients() {
        List<Client> clientList = null;
        try {
            checkStatus();
            clientList = status.getClientList();
        } catch (IOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("IO error", ex);
        } catch (OpenVpnIOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("VPN IO error", ex);
        } catch (OpenVpnParseException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("Parsing error", ex);
        }
        return clientList;
    }
    
    public Set<Route> getRoutes(){
         Set<Route> routes = null;
        try {
            checkStatus();
            routes = status.getRoutes();
        } catch (IOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("IO error", ex);
        } catch (OpenVpnIOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("VPN IO error", ex);
        } catch (OpenVpnParseException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("Parsing error", ex);
        }
        return routes;
    }

    @PreDestroy
    public void tearDown() {
        if (mc != null) {
            try {
                mc.close();
            } catch (Exception ex) {
                LoggerFactory.getLogger(StatusBean.class.getName()).warn("Cannot close the connection", ex);
            }
        }
    }
}
