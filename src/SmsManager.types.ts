// Type definitions
export interface SmsMessage {
  id: string;
  address: string;
  body: string;
  date: number;
  type: "inbox" | "sent" | "draft" | "outbox" | "failed" | "queued";
  read: boolean;
  threadId?: string;
}

export interface MmsMessage {
  id: string;
  address: string;
  body?: string;
  date: number;
  type: "inbox" | "sent" | "draft" | "outbox" | "failed" | "queued";
  read: boolean;
  threadId?: string;
  attachments: MmsAttachment[];
}

export interface MmsAttachment {
  id: string;
  contentType: string;
  fileName?: string;
  data: string; // base64 encoded
  size: number;
}

export interface SendSmsOptions {
  address: string;
  body: string;
  deliveryReceipt?: boolean;
}

export interface SendMmsOptions {
  address: string;
  body?: string;
  attachments?: {
    uri: string;
    type: string;
    name?: string;
  }[];
  deliveryReceipt?: boolean;
}

export interface SmsFilter {
  address?: string;
  startDate?: number;
  endDate?: number;
  type?: "inbox" | "sent" | "draft" | "outbox" | "failed" | "queued";
  read?: boolean;
  limit?: number;
  offset?: number;
}

export interface ConversationThread {
  threadId: string;
  address: string;
  messageCount: number;
  lastMessage: string;
  lastMessageDate: number;
  unreadCount: number;
}
