# Expo SMS Manager

A comprehensive SMS/MMS management module for Expo applications that provides full messaging functionality including sending, receiving, reading, and managing text messages.

## Features

- ✅ Send SMS messages
- ✅ Send MMS messages with attachments
- ✅ Read SMS/MMS messages (Android only)
- ✅ Get conversation threads
- ✅ Mark messages as read/unread
- ✅ Delete messages and threads
- ✅ Real-time message reception events
- ✅ Permission management
- ✅ Full TypeScript support

## Installation

```bash
npx expo install expo-sms-manager
```

### Configure your app

Add the following permissions to your `app.json` or `app.config.js`:

```json
{
  "expo": {
    "plugins": [
      [
        "expo-sms-manager",
        {
          "smsPermission": "This app needs access to SMS to send and receive messages.",
          "readSmsPermission": "This app needs access to read SMS messages."
        }
      ]
    ]
  }
}
```

For Android, add these permissions to your `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.WRITE_SMS" />
```

## Usage

### Basic SMS Sending

```typescript
import { sendSms, requestPermissions } from 'expo-sms-manager';

// Request permissions first
const permissions = await requestPermissions();

if (permissions.send) {
  const result = await sendSms({
    address: '+1234567890',
    body: 'Hello from Expo SMS Manager!',
    deliveryReceipt: true
  });

  if (result.success) {
    console.log('SMS sent successfully!', result.messageId);
  } else {
    console.error('Failed to send SMS:', result.error);
  }
}
```

### Send MMS with Attachments

```typescript
import { sendMms } from 'expo-sms-manager';

const result = await sendMms({
  address: '+1234567890',
  body: 'Check out this image!',
  attachments: [
    {
      uri: 'file://path/to/image.jpg',
      type: 'image/jpeg',
      name: 'photo.jpg'
    }
  ],
  deliveryReceipt: true
});
```

### Read Messages (Android Only)

```typescript
import { getAllMessages, getSmsMessages, getMmsMessages } from 'expo-sms-manager';

// Get all messages (SMS + MMS)
const allMessages = await getAllMessages({
  limit: 50,
  startDate: Date.now() - (7 * 24 * 60 * 60 * 1000), // Last 7 days
});

// Get only SMS messages
const smsMessages = await getSmsMessages({
  address: '+1234567890', // Filter by phone number
  type: 'inbox',
  read: false // Only unread messages
});

// Get only MMS messages
const mmsMessages = await getMmsMessages({
  limit: 20
});
```

### Listen for Incoming Messages

```typescript
import { onSmsReceived, onMmsReceived } from 'expo-sms-manager';

// Listen for incoming SMS
const smsSubscription = onSmsReceived((message) => {
  console.log('New SMS received:', message);
  // Handle the incoming SMS
});

// Listen for incoming MMS
const mmsSubscription = onMmsReceived((message) => {
  console.log('New MMS received:', message);
  // Handle the incoming MMS
});

// Don't forget to clean up
const cleanup = () => {
  smsSubscription.remove();
  mmsSubscription.remove();
};
```

### Manage Conversations

```typescript
import { 
  getConversationThreads, 
  getMessagesInThread, 
  markThreadAsRead 
} from 'expo-sms-manager';

// Get all conversation threads
const threads = await getConversationThreads();

// Get messages in a specific thread
const threadMessages = await getMessagesInThread(threads[0].threadId);

// Mark a thread as read
await markThreadAsRead(threads[0].threadId);
```

### Class-based Usage

```typescript
import { ExpoSmsManager } from 'expo-sms-manager';

const smsManager = new ExpoSmsManager();

// All the same methods are available on the class instance
const messages = await smsManager.getAllMessages();
const result = await smsManager.sendSms({ address: '+1234567890', body: 'Hello!' });
```

## API Reference

### Types

#### `SendSmsOptions`
- `address: string` - The recipient's phone number
- `body: string` - The message content
- `deliveryReceipt?: boolean` - Request delivery receipt (default: false)

#### `SendMmsOptions`
- `address: string` - The recipient's phone number
- `body?: string` - The message content (optional for MMS)
- `attachments?: Attachment[]` - Array of file attachments
- `deliveryReceipt?: boolean` - Request delivery receipt (default: false)

#### `SmsMessage`
- `id: string` - Unique message identifier
- `address: string` - Sender/recipient phone number
- `body: string` - Message content
- `date: number` - Message timestamp
- `type: 'inbox' | 'sent' | 'draft' | 'outbox' | 'failed' | 'queued'`
- `read: boolean` - Read status
- `threadId?: string` - Conversation thread ID

#### `MmsMessage`
Extends `SmsMessage` with:
- `attachments: MmsAttachment[]` - Array of multimedia attachments

### Methods

#### `sendSms(options: SendSmsOptions): Promise<SendResult>`
Send an SMS message.

#### `sendMms(options: SendMmsOptions): Promise<SendResult>`
Send an MMS message with optional attachments.

#### `getSmsMessages(filter?: SmsFilter): Promise<SmsMessage[]>`
Retrieve SMS messages with optional filtering.

#### `getMmsMessages(filter?: SmsFilter): Promise<MmsMessage[]>`
Retrieve MMS messages with optional filtering.

#### `getAllMessages(filter?: SmsFilter): Promise<(SmsMessage | MmsMessage)[]>`
Retrieve all messages (SMS + MMS) sorted by date.

#### `getConversationThreads(): Promise<ConversationThread[]>`
Get all conversation threads.

#### `getMessagesInThread(threadId: string): Promise<(SmsMessage | MmsMessage)[]>`
Get all messages in a specific conversation thread.

#### `markAsRead(messageId: string): Promise<boolean>`
Mark a specific message as read.

#### `markThreadAsRead(threadId: string): Promise<boolean>`
Mark all messages in a thread as read.

#### `deleteMessage(messageId: string): Promise<boolean>`
Delete a specific message.

#### `deleteThread(threadId: string): Promise<boolean>`
Delete an entire conversation thread.

#### `hasPermissions(): Promise<PermissionStatus>`
Check current permission status.

#### `requestPermissions(): Promise<PermissionStatus>`
Request necessary permissions.

### Events

#### `onSmsReceived(callback: (message: SmsMessage) => void): Subscription`
Listen for incoming SMS messages.

#### `onMmsReceived(callback: (message: MmsMessage) => void): Subscription`
Listen for incoming MMS messages.

#### `onSmsSent(callback: (result: SendResult) => void): Subscription`
Listen for SMS send status updates.

#### `onMmsSent(callback: (result: SendResult) => void): Subscription`
Listen for MMS send status updates.

## Platform Differences

### Android
- Full SMS/MMS reading and writing capabilities
- Real-time message reception
- Complete message management (mark as read, delete, etc.)
- Conversation thread management

### iOS
- SMS/MMS sending only (using native message composer)
- No access to message history or inbox
- No real-time message reception
- Limited to sending functionality due to iOS privacy restrictions

## Permissions

### Android
- `SEND_SMS` - Required for sending SMS/MMS
- `RECEIVE_SMS` - Required for receiving SMS notifications
- `READ_SMS` - Required for reading message history
- `WRITE_SMS` - Required for marking messages as read/deleting

### iOS
- No special permissions required
- Uses native `MFMessageComposeViewController` for sending


Made with [create-expo-module](https://www.npmjs.com/package/create-expo-module)
