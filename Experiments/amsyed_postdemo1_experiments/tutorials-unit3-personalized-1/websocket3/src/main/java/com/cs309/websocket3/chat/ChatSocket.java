package com.cs309.websocket3.chat;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller      // this is needed for this to be an endpoint to springboot
@ServerEndpoint(value = "/chat/{username}")  // this is Websocket url
public class ChatSocket {

  // cannot autowire static directly (instead we do it by the below
  // method
	private static MessageRepository msgRepo; 

	/*
   * Grabs the MessageRepository singleton from the Spring Application
   * Context.  This works because of the @Controller annotation on this
   * class and because the variable is declared as static.
   * There are other ways to set this. However, this approach is
   * easiest.
	 */
	@Autowired
	public void setMessageRepository(MessageRepository repo) {
		msgRepo = repo;  // we are setting the static variable
	}

	// Store all socket session and their corresponding username.
	private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
	private static Map<String, Session> usernameSessionMap = new Hashtable<>();

	private final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

	// room -> (Session -> username)
	private static final Map<String, Map<Session, String>> roomSessionMap = new Hashtable<>();

	// session -> active room
	private static final Map<Session, String> sessionActiveRoom = new Hashtable<>();

	private static final String DEFAULT_ROOM = "general";

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) 
      throws IOException {
		// your existing: maps for session<->username, greet, history, etc.
		sessionUsernameMap.put(session, username);
		usernameSessionMap.put(username, session);

		// rooms
		joinRoom(session, username, DEFAULT_ROOM);

		// (optional) send a short tip
		safeSend(session, "[system] Commands: /join <room>, /leave, /rooms, /who");
		// send history if you have it (see Section 2)
	}


	@OnMessage
	public void onMessage(Session session, String message) {
		String username = sessionUsernameMap.get(session);
		if (username == null) return;

		// Commands

		if (message.startsWith("/join ")) { // join room
			String room = message.substring(6).trim();
			if (room.isEmpty()) { safeSend(session, "[system] Usage: /join <room>"); return; }
			try { setActiveRoom(session, username, room); }
			catch (IOException ignored) {}
			broadcastToRoom("[system] " + username + " joined " + room, room);
			return;
		} else if (message.equals("/leave")) { // leave current room
			try { leaveCurrentRoom(session, username); } catch (IOException ignored) {}
			return;
		} else if (message.equals("/rooms")) { // number of rooms
			safeSend(session, listRooms()); return;
		} else if (message.equals("/who")) { // people in this room
			String room = getActiveRoom(session);
			safeSend(session, listUsersInRoom(room)); return;
		}

		// DM - same as b4
		if (message.startsWith("@")) {
			String[] split_msg = message.split("\\s+");
			if (split_msg.length >= 2) {
				String destUserName = split_msg[0].substring(1);
				StringBuilder actualMessageBuilder = new StringBuilder();
				for (int i = 1; i < split_msg.length; i++) actualMessageBuilder.append(split_msg[i]).append(" ");
				String actualMessage = actualMessageBuilder.toString().trim();
				sendMessageToPArticularUser(destUserName, "[DM from " + username + "]: " + actualMessage);
				sendMessageToPArticularUser(username, "[DM to " + destUserName + "]: " + actualMessage);
				// (optional persist)
			}
			return;
		}

		// Broadcast to room
		String room = getActiveRoom(session);
		String line = "[" + room + "] " + username + ": " + message;
		broadcastToRoom(line, room);
		// (optional persist with room)
	}

	@OnClose
	public void onClose(Session session) {
		String username = sessionUsernameMap.remove(session);
		if (username != null) usernameSessionMap.remove(username);

		String room = getActiveRoom(session);
		Map<Session,String> m = roomSessionMap.get(room);
		if (m != null) {
			m.remove(session);
			broadcastToRoom("[system] " + username + " disconnected", room);
		}
		sessionActiveRoom.remove(session);
	}


	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.info("Entered into Error");
		throwable.printStackTrace();
	}


	private void sendMessageToPArticularUser(String username, String message) {
		try {
			usernameSessionMap.get(username).getBasicRemote().sendText(message);
		} 
    catch (IOException e) {
			logger.info("Exception: " + e.getMessage().toString());
			e.printStackTrace();
		}
	}


	private void broadcast(String message) {
		sessionUsernameMap.forEach((session, username) -> {
			try {
				session.getBasicRemote().sendText(message);
			} 
      catch (IOException e) {
				logger.info("Exception: " + e.getMessage().toString());
				e.printStackTrace();
			}

		});

	}
	

  // Gets the Chat history from the repository
	private String getChatHistory() {
		List<Message> messages = msgRepo.findAll();
    
    // convert the list to a string
		StringBuilder sb = new StringBuilder();
		if(messages != null && messages.size() != 0) {
			for (Message message : messages) {
				sb.append(message.getUserName() + ": " + message.getContent() + "\n");
			}
		}
		return sb.toString();
	}

	// Helpers for rooms

	private void ensureRoomExists(String room) {
		roomSessionMap.computeIfAbsent(room, r -> new Hashtable<>());
	}

	private String getActiveRoom(Session s) {
		return sessionActiveRoom.getOrDefault(s, DEFAULT_ROOM);
	}

	private void setActiveRoom(Session s, String username, String room) throws IOException {
		// remove from old room
		String old = getActiveRoom(s);
		if (!old.equals(room)) {
			Map<Session,String> oldMap = roomSessionMap.get(old);
			if (oldMap != null) oldMap.remove(s);
		}
		// join new room
		ensureRoomExists(room);
		roomSessionMap.get(room).put(s, username);
		sessionActiveRoom.put(s, room);
		safeSend(s, "[system] Active room → " + room);
	}

	private void broadcastToRoom(String message, String room) {
		Map<Session,String> m = roomSessionMap.get(room);
		if (m == null) return;
		m.forEach((sess, user) -> safeSend(sess, message));
	}

	private void safeSend(Session s, String msg) {
		try { s.getBasicRemote().sendText(msg); }
		catch (Exception e) {/**/}
	}

	private void joinRoom(Session s, String username, String room) throws IOException {
		ensureRoomExists(room);
		roomSessionMap.get(room).put(s, username);
		sessionActiveRoom.put(s, room);
		broadcastToRoom("[system] " + username + " joined " + room, room);
	}

	private void leaveCurrentRoom(Session s, String username) throws IOException {
		String room = getActiveRoom(s);
		Map<Session,String> m = roomSessionMap.get(room);
		if (m != null) {
			m.remove(s);
			broadcastToRoom("[system] " + username + " left " + room, room);
		}
		// move to default room
		setActiveRoom(s, username, DEFAULT_ROOM);
	}

	private String listRooms() {
		return "[system] Rooms: " + String.join(", ", roomSessionMap.keySet());
	}

	private String listUsersInRoom(String room) {
		Map<Session,String> m = roomSessionMap.get(room);
		if (m == null || m.isEmpty()) return "[system] (empty)";
		return "[system] Users in " + room + ": " + String.join(", ", m.values());
	}
} // end of Class
