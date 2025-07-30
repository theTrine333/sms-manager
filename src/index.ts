// expo-sms-manager/src/index.ts
import {
  NativeModulesProxy,
  EventEmitter,
  EventSubscription as Subscription,
} from "expo-modules-core";
import ExpoSmsManagerModule from "./SmsManagerModule";
import {
  ConversationThread,
  MmsMessage,
  SendMmsOptions,
  SendSmsOptions,
  SmsFilter,
  SmsMessage,
} from "./SmsManager.types";

// Main class
export class ExpoSmsManager {
  private eventEmitter = new EventEmitter(ExpoSmsManagerModule);

  // Send SMS
  async sendSms(
    options: SendSmsOptions
  ): Promise<{ success: boolean; messageId?: string; error?: string }> {
    try {
      const result = await ExpoSmsManagerModule.sendSms(options);
      return result;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  // Send MMS
  async sendMms(
    options: SendMmsOptions
  ): Promise<{ success: boolean; messageId?: string; error?: string }> {
    try {
      const result = await ExpoSmsManagerModule.sendMms(options);
      return result;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  // Get SMS messages
  async getSmsMessages(filter?: SmsFilter): Promise<SmsMessage[]> {
    return await ExpoSmsManagerModule.getSmsMessages(filter || {});
  }

  // Get MMS messages
  async getMmsMessages(filter?: SmsFilter): Promise<MmsMessage[]> {
    return await ExpoSmsManagerModule.getMmsMessages(filter || {});
  }

  // Get all messages (SMS + MMS)
  async getAllMessages(
    filter?: SmsFilter
  ): Promise<(SmsMessage | MmsMessage)[]> {
    const [smsMessages, mmsMessages] = await Promise.all([
      this.getSmsMessages(filter),
      this.getMmsMessages(filter),
    ]);

    return [...smsMessages, ...mmsMessages].sort((a, b) => b.date - a.date);
  }

  // Get conversation threads
  async getConversationThreads(): Promise<ConversationThread[]> {
    return await ExpoSmsManagerModule.getConversationThreads();
  }

  // Get messages in a specific thread
  async getMessagesInThread(
    threadId: string
  ): Promise<(SmsMessage | MmsMessage)[]> {
    return await ExpoSmsManagerModule.getMessagesInThread(threadId);
  }

  // Mark message as read
  async markAsRead(messageId: string): Promise<boolean> {
    return await ExpoSmsManagerModule.markAsRead(messageId);
  }

  // Mark thread as read
  async markThreadAsRead(threadId: string): Promise<boolean> {
    return await ExpoSmsManagerModule.markThreadAsRead(threadId);
  }

  // Delete message
  async deleteMessage(messageId: string): Promise<boolean> {
    return await ExpoSmsManagerModule.deleteMessage(messageId);
  }

  // Delete thread
  async deleteThread(threadId: string): Promise<boolean> {
    return await ExpoSmsManagerModule.deleteThread(threadId);
  }

  // Check permissions
  async hasPermissions(): Promise<{
    read: boolean;
    send: boolean;
    receive: boolean;
  }> {
    return await ExpoSmsManagerModule.hasPermissions();
  }

  // Request permissions
  async requestPermissions(): Promise<{
    read: boolean;
    send: boolean;
    receive: boolean;
  }> {
    return await ExpoSmsManagerModule.requestPermissions();
  }

  // Event listeners
  onSmsReceived(callback: (message: SmsMessage) => void): Subscription {
    return this.eventEmitter.addListener("onSmsReceived", callback);
  }

  onMmsReceived(callback: (message: MmsMessage) => void): Subscription {
    return this.eventEmitter.addListener("onMmsReceived", callback);
  }

  onSmsSent(
    callback: (result: {
      success: boolean;
      messageId: string;
      error?: string;
    }) => void
  ): Subscription {
    return this.eventEmitter.addListener("onSmsSent", callback);
  }

  onMmsSent(
    callback: (result: {
      success: boolean;
      messageId: string;
      error?: string;
    }) => void
  ): Subscription {
    return this.eventEmitter.addListener("onMmsSent", callback);
  }

  onSmsDelivered(callback: (messageId: string) => void): Subscription {
    return this.eventEmitter.addListener("onSmsDelivered", callback);
  }

  onMmsDelivered(callback: (messageId: string) => void): Subscription {
    return this.eventEmitter.addListener("onMmsDelivered", callback);
  }
}

// Create singleton instance
const smsManager = new ExpoSmsManager();

// Export functions for backward compatibility
export const sendSms = (options: SendSmsOptions) => smsManager.sendSms(options);
export const sendMms = (options: SendMmsOptions) => smsManager.sendMms(options);
export const getSmsMessages = (filter?: SmsFilter) =>
  smsManager.getSmsMessages(filter);
export const getMmsMessages = (filter?: SmsFilter) =>
  smsManager.getMmsMessages(filter);
export const getAllMessages = (filter?: SmsFilter) =>
  smsManager.getAllMessages(filter);
export const getConversationThreads = () => smsManager.getConversationThreads();
export const getMessagesInThread = (threadId: string) =>
  smsManager.getMessagesInThread(threadId);
export const markAsRead = (messageId: string) =>
  smsManager.markAsRead(messageId);
export const markThreadAsRead = (threadId: string) =>
  smsManager.markThreadAsRead(threadId);
export const deleteMessage = (messageId: string) =>
  smsManager.deleteMessage(messageId);
export const deleteThread = (threadId: string) =>
  smsManager.deleteThread(threadId);
export const hasPermissions = () => smsManager.hasPermissions();
export const requestPermissions = () => smsManager.requestPermissions();

// Event subscription helpers
export const onSmsReceived = (callback: (message: SmsMessage) => void) =>
  smsManager.onSmsReceived(callback);
export const onMmsReceived = (callback: (message: MmsMessage) => void) =>
  smsManager.onMmsReceived(callback);
export const onSmsSent = (callback: (result: any) => void) =>
  smsManager.onSmsSent(callback);
export const onMmsSent = (callback: (result: any) => void) =>
  smsManager.onMmsSent(callback);
export const onSmsDelivered = (callback: (messageId: string) => void) =>
  smsManager.onSmsDelivered(callback);
export const onMmsDelivered = (callback: (messageId: string) => void) =>
  smsManager.onMmsDelivered(callback);

export default smsManager;
