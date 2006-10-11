/**
 * $RCSfile$
 * $Revision: $
 * $Date: $
 *
 * Copyright (C) 2006 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution.
 */

package org.jivesoftware.multiplexer;

import org.dom4j.Element;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The session represents a connection between the server and a client (c2s) or
 * another server (s2s) as well as a connection with a component. Authentication and
 * user accounts are associated with c2s connections while s2s has an optional authentication
 * association but no single user user.<p>
 *
 * Obtain object managers from the session in order to access server resources.
 *
 * @author Gaston Dombiak
 */
public abstract class Session {

    /**
     * Version of the XMPP spec supported as MAJOR_VERSION.MINOR_VERSION (e.g. 1.0).
     */
    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 0;

    /**
     * The utf-8 charset for decoding and encoding Jabber packet streams.
     */
    protected static String CHARSET = "UTF-8";

    public static final int STATUS_CLOSED = -1;
    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_STREAMING = 2;
    public static final int STATUS_AUTHENTICATED = 3;

    /**
     * The stream id for this session (random and unique).
     */
    private String streamID;

    /**
     * The current session status.
     */
    protected int status = STATUS_CONNECTED;

    /**
     * The connection that this session represents.
     */
    protected Connection conn;

    private String serverName;

    private Date startDate = new Date();

    /**
     * Map of existing sessions. A session is added just after the initial stream header
     * was processed. Key: stream ID, value: the session.
     */
    private static Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    public static StreamIDFactory idFactory = new StreamIDFactory();

    public static void addSession(String streamID, Session session) {
        sessions.put(streamID, session);
    }

    protected static void removeSession(String streamID) {
        sessions.remove(streamID);
    }

        /**
     * Returns the session whose stream ID matches the specified stream ID.
     *
     * @param streamID the stream ID of the session to look for.
     * @return the session whose stream ID matches the specified stream ID.
     */
    public static Session getSession(String streamID) {
        return sessions.get(streamID);
    }

    /**
     * Closes connections of connected clients since the server or the connection
     * manager is being shut down. If the server is the one that is being shut down
     * then the connection manager will keep running and will try to establish new
     * connections to the server (on demand).
     */
    public static void closeAll() {
        for (Session session : sessions.values()) {
            session.close(true);
        }
    }

    /**
     * Creates a session with an underlying connection and permission protection.
     *
     * @param connection The connection we are proxying
     */
    public Session(String serverName, Connection connection, String streamID) {
        conn = connection;
        this.streamID = streamID;
        this.serverName = serverName;
    }

    /**
     * Obtain the current status of this session.
     *
     * @return The status code for this session
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set the new status of this session. Setting a status may trigger
     * certain events to occur (setting a closed status will close this
     * session).
     *
     * @param status The new status code for this session
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Obtain the stream ID associated with this sesison. Stream ID's are generated by the server
     * and should be unique and random.
     *
     * @return This session's assigned stream ID
     */
    public String getStreamID() {
        return streamID;
    }

    /**
     * Obtain the name of the server this session belongs to.
     *
     * @return the server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Obtain the date the session was created.
     *
     * @return the session's creation date.
     */
    public Date getCreationDate() {
        return startDate;
    }

    /**
     * Returns a text with the available stream features. Each subclass may return different
     * values depending whether the session has been authenticated or not.
     *
     * @return a text with the available stream features or <tt>null</tt> to add nothing.
     */
    public abstract String getAvailableStreamFeatures();

    /**
     * Indicate the server that the session has been closed. Do nothing if the session
     * was the one that originated the close action.
     */
    public abstract void close();

    public abstract void close(boolean isServerShuttingDown);

    public abstract void deliver(Element stanza);

    public String toString() {
        return super.toString() + " status: " + status + " id: " + streamID;
    }

    protected static int[] decodeVersion(String version) {
        int[] answer = new int[] {0 , 0};
        String [] versionString = version.split("\\.");
        answer[0] = Integer.parseInt(versionString[0]);
        answer[1] = Integer.parseInt(versionString[1]);
        return answer;
    }

}