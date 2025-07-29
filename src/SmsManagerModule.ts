import { NativeModulesProxy } from "expo-modules-core";
import { SmsManagerModule } from "./SmsManager.types";

const SmsManager = NativeModulesProxy.SmsManager as SmsManagerModule;

export default SmsManager;
