import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  Button,
  TextInput,
  FlatList,
  Alert,
  StyleSheet,
} from "react-native";
import {
  ExpoSmsManager,
  sendSms,
  sendMms,
  getAllMessages,
  getConversationThreads,
  requestPermissions,
  onSmsReceived,
  onMmsReceived,
} from "sms-manager";

import {
  SmsMessage,
  MmsMessage,
  ConversationThread,
} from "sms-manager/SmsManager.types";

export default function App() {
  const [phoneNumber, setPhoneNumber] = useState("");
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState<(SmsMessage | MmsMessage)[]>([]);
  const [threads, setThreads] = useState<ConversationThread[]>([]);
  const [permissions, setPermissions] = useState({
    read: false,
    send: false,
    receive: false,
  });

  useEffect(() => {
    initializeApp();
  }, []);

  const initializeApp = async () => {
    // Request permissions
    const perms = await requestPermissions();
    setPermissions(perms);

    if (perms.read) {
      // Load messages and threads
      const allMessages = await getAllMessages({ limit: 50 });
      setMessages(allMessages);

      const conversationThreads = await getConversationThreads();
      setThreads(conversationThreads);
    }

    // Set up event listeners
    const smsSubscription = onSmsReceived((sms) => {
      console.log("New SMS received:", sms);
      setMessages((prev) => [sms, ...prev]);
    });

    const mmsSubscription = onMmsReceived((mms) => {
      console.log("New MMS received:", mms);
      setMessages((prev) => [mms, ...prev]);
    });

    return () => {
      smsSubscription.remove();
      mmsSubscription.remove();
    };
  };

  const handleSendSms = async () => {
    if (!phoneNumber || !message) {
      Alert.alert("Error", "Please enter both phone number and message");
      return;
    }

    if (!permissions.send) {
      Alert.alert("Error", "SMS sending permission not granted");
      return;
    }

    try {
      const result = await sendSms({
        address: phoneNumber,
        body: message,
        deliveryReceipt: true,
      });

      if (result.success) {
        Alert.alert("Success", "SMS sent successfully");
        setMessage("");
      } else {
        Alert.alert("Error", result.error || "Failed to send SMS");
      }
    } catch (error) {
      Alert.alert("Error", "Failed to send SMS");
    }
  };

  const handleSendMms = async () => {
    if (!phoneNumber || !message) {
      Alert.alert("Error", "Please enter both phone number and message");
      return;
    }

    if (!permissions.send) {
      Alert.alert("Error", "MMS sending permission not granted");
      return;
    }

    try {
      const result = await sendMms({
        address: phoneNumber,
        body: message,
        deliveryReceipt: true,
        attachments: [
          // Example attachment
          // {
          //   uri: 'file://path/to/image.jpg',
          //   type: 'image/jpeg',
          //   name: 'image.jpg'
          // }
        ],
      });

      if (result.success) {
        Alert.alert("Success", "MMS sent successfully");
        setMessage("");
      } else {
        Alert.alert("Error", result.error || "Failed to send MMS");
      }
    } catch (error) {
      Alert.alert("Error", "Failed to send MMS");
    }
  };

  const refreshMessages = async () => {
    if (permissions.read) {
      const allMessages = await getAllMessages({ limit: 50 });
      setMessages(allMessages);
    }
  };

  const renderMessage = ({ item }: { item: SmsMessage | MmsMessage }) => (
    <View style={styles.messageItem}>
      <Text style={styles.address}>{item.address}</Text>
      <Text style={styles.body}>{item.body}</Text>
      <Text style={styles.date}>{new Date(item.date).toLocaleString()}</Text>
      <Text style={styles.type}>
        {item.type} {item.read ? "(Read)" : "(Unread)"}
      </Text>
      {"attachments" in item && item.attachments.length > 0 && (
        <Text style={styles.attachments}>
          📎 {item.attachments.length} attachment(s)
        </Text>
      )}
    </View>
  );

  const renderThread = ({ item }: { item: ConversationThread }) => (
    <View style={styles.threadItem}>
      <Text style={styles.address}>{item.address}</Text>
      <Text style={styles.lastMessage}>{item.lastMessage}</Text>
      <Text style={styles.messageCount}>
        {item.messageCount} messages ({item.unreadCount} unread)
      </Text>
      <Text style={styles.date}>
        {new Date(item.lastMessageDate).toLocaleString()}
      </Text>
    </View>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.title}>SMS/MMS Manager</Text>

      <View style={styles.permissionStatus}>
        <Text>
          Permissions: Read: {permissions.read ? "✅" : "❌"} | Send:{" "}
          {permissions.send ? "✅" : "❌"} | Receive:{" "}
          {permissions.receive ? "✅" : "❌"}
        </Text>
      </View>

      <View style={styles.inputSection}>
        <TextInput
          style={styles.input}
          placeholder="Phone Number"
          value={phoneNumber}
          onChangeText={setPhoneNumber}
          keyboardType="phone-pad"
        />
        <TextInput
          style={[styles.input, styles.messageInput]}
          placeholder="Message"
          value={message}
          onChangeText={setMessage}
          multiline
        />
        <View style={styles.buttonRow}>
          <Button title="Send SMS" onPress={handleSendSms} />
          <Button title="Send MMS" onPress={handleSendMms} />
        </View>
      </View>

      <View style={styles.actionButtons}>
        <Button title="Refresh Messages" onPress={refreshMessages} />
      </View>

      <Text style={styles.sectionTitle}>
        Recent Messages ({messages.length})
      </Text>
      <FlatList
        data={messages.slice(0, 10)}
        renderItem={renderMessage}
        keyExtractor={(item) => item.id}
        style={styles.messagesList}
      />

      <Text style={styles.sectionTitle}>
        Conversation Threads ({threads.length})
      </Text>
      <FlatList
        data={threads.slice(0, 5)}
        renderItem={renderThread}
        keyExtractor={(item) => item.threadId}
        style={styles.threadsList}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: "#f5f5f5",
  },
  title: {
    fontSize: 24,
    fontWeight: "bold",
    textAlign: "center",
    marginTop: 40,
    marginBottom: 20,
  },
  permissionStatus: {
    backgroundColor: "#e0e0e0",
    padding: 10,
    borderRadius: 5,
    marginBottom: 20,
  },
  inputSection: {
    backgroundColor: "white",
    padding: 15,
    borderRadius: 10,
    marginBottom: 20,
  },
  input: {
    borderWidth: 1,
    borderColor: "#ddd",
    padding: 10,
    borderRadius: 5,
    marginBottom: 10,
  },
  messageInput: {
    height: 80,
    textAlignVertical: "top",
  },
  buttonRow: {
    flexDirection: "row",
    justifyContent: "space-around",
  },
  actionButtons: {
    marginBottom: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: "bold",
    marginBottom: 10,
  },
  messagesList: {
    maxHeight: 200,
    marginBottom: 20,
  },
  threadsList: {
    maxHeight: 150,
  },
  messageItem: {
    backgroundColor: "white",
    padding: 10,
    borderRadius: 5,
    marginBottom: 5,
  },
  threadItem: {
    backgroundColor: "white",
    padding: 10,
    borderRadius: 5,
    marginBottom: 5,
  },
  address: {
    fontWeight: "bold",
    color: "#333",
  },
  body: {
    color: "#666",
    marginVertical: 5,
  },
  lastMessage: {
    color: "#666",
    marginVertical: 5,
    fontStyle: "italic",
  },
  date: {
    fontSize: 12,
    color: "#999",
  },
  type: {
    fontSize: 12,
    color: "#999",
  },
  messageCount: {
    fontSize: 12,
    color: "#007AFF",
  },
  attachments: {
    fontSize: 12,
    color: "#FF6B35",
  },
});
