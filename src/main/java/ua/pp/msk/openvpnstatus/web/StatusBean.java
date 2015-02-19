/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.openvpnstatus.web;

import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
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
@ManagedBean()
@ViewScoped
public class StatusBean implements Serializable{

    private Status status;
    private int port = 0;
    private InetAddress host;
    private transient Connection mc;

    public StatusBean() {
    }

    @PostConstruct
    public void init() {
        try {

            if (host == null) {
                ResourceBundle openVpnBundle = ResourceBundle.getBundle("ua/pp/msk/server/OpenVpn");
                if (openVpnBundle.containsKey("host")) {
                    host = InetAddress.getByName(openVpnBundle.getString("host"));
                }
            }
            if (port < 1) {
                ResourceBundle openVpnBundle = ResourceBundle.getBundle("ua/pp/msk/server/OpenVpn");
                if (openVpnBundle.containsKey("port")) {
                    port = Integer.parseInt(openVpnBundle.getString("port"));
                }
            }
            mc = ManagementConnection.getConnection(host, port);

            if (!mc.isConnected()) {
                mc.connect();
            }
            
        } 
        catch (IOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).error("Connection IO error at", ex);
        }
    }

    @PreDestroy
    public void tearDown() {
        if (mc != null) {
            try {
                mc.close();
            } catch (Exception ex) {
                LoggerFactory.getLogger(StatusBean.class.getName()).error("Cannot close connection", ex);
            }
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) throws UnknownHostException {
        this.host = Inet4Address.getByName(host);
    }

    public String getHost() {

        return (host == null) ? "" : host.getHostName();
    }

    public void setHost(InetAddress addr) {
        this.host = addr;
    }

    private synchronized void checkStatus() throws OpenVpnParseException, OpenVpnIOException, IOException {
       
        if (mc == null) {
            init();
            status = null;
        }
        
        if (status == null) {
            try {
               
                
                if (!mc.isConnected()) {
                    try {
                        mc.connect();
                    } catch (SocketException sex) {
                         LoggerFactory.getLogger(StatusBean.class.getName()).warn("Socket error" + sex.getMessage(), sex);
                         mc.close();
                         init();
                    }
                }
                status = mc.getStatus();
            } catch (IOException ex) {
                LoggerFactory.getLogger(StatusBean.class.getName()).error("Connection IO error", ex);
            } catch (Exception ex) {
                 LoggerFactory.getLogger(StatusBean.class.getName()).error("Cannot repopen connection", ex);
            }
        }
    }

    public Status getStatus() {
        try {
            checkStatus();
        } catch (IOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("IO error", ex);
        } catch (OpenVpnIOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("VPN IO error", ex);
        } catch (OpenVpnParseException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("Parsing error", ex);
        }
        return status;
    }

    public Calendar getUpdateTime() {
        Calendar ut = null;
        try {
            checkStatus();
            ut = status.getUpdateTime();
        } catch (IOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("IO error", ex);
        } catch (OpenVpnIOException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("VPN IO error", ex);
        } catch (OpenVpnParseException ex) {
            LoggerFactory.getLogger(StatusBean.class.getName()).warn("Parsing error", ex);
        }
        return ut;
    }

    public List<Client> getConnectedClients() {
        List<Client> clientList = new ArrayList<>();
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

    public Set<Route> getRoutes() {
        Set<Route> routes = new HashSet<>();
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

    public String process() {
        return "status.xhtml";
    }

}
