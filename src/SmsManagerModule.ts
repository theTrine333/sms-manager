import { NativeModule } from "expo-modules-core";

export default {
  sendSms: async (options: any) => {},
  sendMms: async (options: any) => {},
  getSmsMessages: async (filter: any) => {},
  getMmsMessages: async (filter: any) => {},
  getConversationThreads: async () => {},
  getMessagesInThread: async (threadId: string) => {},
  markAsRead: async (messageId: string) => {},
  markThreadAsRead: async (threadId: string) => {},
  deleteMessage: async (messageId: string) => {},
  deleteThread: async (threadId: string) => {},
  hasPermissions: async () => {},
  requestPermissions: async () => {},
} as NativeModule;
