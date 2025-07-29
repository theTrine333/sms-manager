export type SmsMessage = {
  id: string;
  threadId: string;
  address: string;
  body: string;
  date: number;
  type: number;
  contactName?: string;
};

export interface SmsManagerModule {
  getAllSms(): Promise<SmsMessage[]>;
  getRecentThreads(): Promise<SmsMessage[]>;
  sendSms(phoneNumber: string, message: string): Promise<boolean>;
  deleteSms(id: string): Promise<number>;
}
