// @ts-nocheck
import { WebPlugin } from '@capacitor/core';

import type {
  AppboxoPlugin,
  ConfigOptions,
  CustomEvent,
  MiniappListResult,
  OpenMiniappOptions,
  PaymentEvent,
} from './definitions';

export class AppboxoWeb extends WebPlugin implements AppboxoPlugin {
  setConfig(options: ConfigOptions): Promise<void> {
    throw new Error('Method not implemented.');
  }
  openMiniapp(options: OpenMiniappOptions): Promise<void> {
    throw new Error('Method not implemented.');
  }
  setAuthCode(options: { appId: string; authCode: string }): Promise<void> {
    throw new Error('Method not implemented.');
  }
  closeMiniapp(options: { appId: string }): Promise<void> {
    throw new Error('Method not implemented.');
  }
  sendCustomEvent(customEvent: CustomEvent): Promise<void> {
    throw new Error('Method not implemented.');
  }
  sendPaymentEvent(paymentEvent: PaymentEvent): Promise<void> {
    throw new Error('Method not implemented.');
  }
  getMiniapps(): Promise<MiniappListResult> {
    throw new Error('Method not implemented.');
  }
  hideMiniapps(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  logout(): Promise<void> {
    throw new Error('Method not implemented.');
  }
}
