package com.github.drxaos.shell;

import com.jcraft.jcterm.Connection;
import com.jcraft.jcterm.JCTermSwing;
import com.jcraft.jcterm.JSchSession;
import com.jcraft.jcterm.Term;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.UserInfo;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.auth.SimpleAuthenticationPlugin;
import org.crsh.ssh.SSHPlugin;
import org.crsh.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {

        SimpleAuthenticationPlugin auth = new SimpleAuthenticationPlugin();

        SimpleLifeCycle lifeCycle = new SimpleLifeCycle();
        lifeCycle.setProperty(SSHPlugin.SSH_PORT, 9788);
        lifeCycle.setProperty(SSHPlugin.SSH_SERVER_IDLE_TIMEOUT, 10 * 60 * 1000);
        lifeCycle.setProperty(SSHPlugin.SSH_SERVER_AUTH_TIMEOUT, 10 * 60 * 1000);
        lifeCycle.setProperty(SSHPlugin.SSH_ENCODING, Utils.UTF_8);
        lifeCycle.setProperty(AuthenticationPlugin.AUTH, Arrays.asList(auth.getName()));
        lifeCycle.setProperty(SimpleAuthenticationPlugin.SIMPLE_USERNAME, "root");
        lifeCycle.setProperty(SimpleAuthenticationPlugin.SIMPLE_PASSWORD, "root");
        lifeCycle.start();


        JSchSession jschsession = JSchSession.getSession("root", "root", "localhost", 9788, new UserInfo() {
            public String getPassphrase() {
                return "root";
            }

            public String getPassword() {
                return "root";
            }

            public boolean promptPassword(String message) {
                return true;
            }

            public boolean promptPassphrase(String message) {
                return true;
            }

            public boolean promptYesNo(String message) {
                return true;
            }

            public void showMessage(String message) {
                System.out.println(message);
            }
        }, null);

        jschsession.getSession().setConfig("compression.s2c", "none");
        jschsession.getSession().setConfig("compression.c2s", "none");
        jschsession.getSession().setConfig("compression_level", "0");

        Channel channel = jschsession.getSession().openChannel("shell");
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();
        channel.connect();

        final OutputStream fout = out;
        final InputStream fin = in;
        final Channel fchannel = channel;

        Connection connection = new Connection() {
            public InputStream getInputStream() {
                return fin;
            }

            public OutputStream getOutputStream() {
                return fout;
            }

            public void requestResize(Term term) {
                if (fchannel instanceof ChannelShell) {
                    int c = term.getColumnCount();
                    int r = term.getRowCount();
                    ((ChannelShell) fchannel).setPtySize(c, r, c * term.getCharWidth(),
                            r * term.getCharHeight());
                }
            }

            public void close() {
                fchannel.disconnect();
            }
        };

        final JCTermSwing term = new JCTermSwing();

        JFrame jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.add(term);

        ComponentAdapter l = new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Component c = e.getComponent();
                Container cp = ((JFrame) c).getContentPane();
                int cw = c.getWidth();
                int ch = c.getHeight();
                int cwm = c.getWidth() - cp.getWidth();
                int chm = c.getHeight() - cp.getHeight();
                cw -= cwm;
                ch -= chm;
                term.setSize(cw, ch);
            }
        };
        jFrame.addComponentListener(l);
        jFrame.setSize(800, 600);

        jFrame.setVisible(true);

        term.start(connection);
    }
}
