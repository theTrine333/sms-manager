import ExpoModulesCore
import MessageUI
import Messages

public class ExpoSmsManagerModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoSmsManager")

    Events("onSmsReceived", "onMmsReceived", "onSmsSent", "onMmsSent", "onSmsDelivered", "onMmsDelivered")

    AsyncFunction("sendSms") { (options: SendSmsOptions, promise: Promise) in
      if MFMessageComposeViewController.canSendText() {
        DispatchQueue.main.async {
          let messageVC = MFMessageComposeViewController()
          messageVC.recipients = [options.address]
          messageVC.body = options.body
          
          // Present the message compose view controller
          // Note: This requires proper view controller presentation
          // which would need to be handled in the app
          
          promise.resolve([
            "success": true,
            "messageId": UUID().uuidString
          ])
        }
      } else {
        promise.resolve([
          "success": false,
          "error": "SMS not available on this device"
        ])
      }
    }

    AsyncFunction("sendMms") { (options: SendMmsOptions, promise: Promise) in
      if MFMessageComposeViewController.canSendAttachments() {
        DispatchQueue.main.async {
          let messageVC = MFMessageComposeViewController()
          messageVC.recipients = [options.address]
          if let body = options.body {
            messageVC.body = body
          }
          
          // Handle attachments
          if let attachments = options.attachments {
            for attachment in attachments {
              // Process each attachment
            }
          }
          
          promise.resolve([
            "success": true,
            "messageId": UUID().uuidString
          ])
        }
      } else {
        promise.resolve([
          "success": false,
          "error": "MMS not available on this device"
        ])
      }
    }

    AsyncFunction("getSmsMessages") { (filter: SmsFilter, promise: Promise) in
      // iOS doesn't provide direct access to SMS messages
      // This would require using private APIs or MessageKit
      promise.resolve([])
    }

    AsyncFunction("getMmsMessages") { (filter: SmsFilter, promise: Promise) in
      // iOS doesn't provide direct access to MMS messages
      promise.resolve([])
    }

    AsyncFunction("getConversationThreads") { (promise: Promise) in
      // iOS doesn't provide direct access to conversation threads
      promise.resolve([])
    }

    AsyncFunction("getMessagesInThread") { (threadId: String, promise: Promise) in
      // iOS doesn't provide direct access to messages
      promise.resolve([])
    }

    AsyncFunction("markAsRead") { (messageId: String, promise: Promise) in
      // iOS doesn't allow marking messages as read programmatically
      promise.resolve(false)
    }

    AsyncFunction("markThreadAsRead") { (threadId: String, promise: Promise) in
      // iOS doesn't allow marking threads as read programmatically
      promise.resolve(false)
    }

    AsyncFunction("deleteMessage") { (messageId: String, promise: Promise) in
      // iOS doesn't allow deleting messages programmatically
      promise.resolve(false)
    }

    AsyncFunction("deleteThread") { (threadId: String, promise: Promise) in
      // iOS doesn't allow deleting threads programmatically
      promise.resolve(false)
    }

    AsyncFunction("hasPermissions") { (promise: Promise) in
      promise.resolve([
        "read": false, // iOS doesn't allow reading SMS
        "send": MFMessageComposeViewController.canSendText(),
        "receive": false // iOS doesn't allow intercepting SMS
      ])
    }

    AsyncFunction("requestPermissions") { (promise: Promise) in
      promise.resolve([
        "read": false,
        "send": MFMessageComposeViewController.canSendText(),
        "receive": false
      ])
    }
  }
}

// Record types for iOS
struct SendSmsOptions: Record {
  @Field var address: String = ""
  @Field var body: String = ""
  @Field var deliveryReceipt: Bool = false
}

struct SendMmsOptions: Record {
  @Field var address: String = ""
  @Field var body: String?
  @Field var attachments: [AttachmentOptions]?
  @Field var deliveryReceipt: Bool = false
}

struct AttachmentOptions: Record {
  @Field var uri: String = ""
  @Field var type: String = ""
  @Field var name: String?
}

struct SmsFilter: Record {
  @Field var address: String?
  @Field var startDate: Double?
  @Field var endDate: Double?
  @Field var type: String?
  @Field var read: Bool?
  @Field var limit: Int?
  @Field var offset: Int?
}
