/* eslint-disable no-undef */
import { Appboxo } from 'capacitor-boxo-sdk';

const clientId = '';
const authCode = '';
const appId = '';

let requestId = '';

Appboxo.setConfig({
  clientId: clientId,
  enableMultitaskMode: true,
  userId: '',
  showAboutPage: false
});

Appboxo.addListener('custom_event', customEvent => {
  console.log(customEvent);
  console.log('custom_event app_id=' + customEvent.appId);
  if (customEvent.type === 'upgrade_plan') {
    console.log('upgrade plan');
    requestId = customEvent.requestId;
    window.document.getElementById('upgrade_notification').innerHTML =
      "Received 'upgrade_plan' event";
    Appboxo.hideMiniapps();
  }
});
Appboxo.addListener('miniapp_lifecycle', event => {
  console.log('lifecycle_app_id=' + event.appId);
  console.log('lifecycle=' + event.lifecycle);
  if (event.lifecycle == 'onAuth') {
    console.log('get auth code from hostapp backend');
    Appboxo.setAuthCode({
      appId: event.appId,
      authCode,
    });
  }
});
Appboxo.addListener('payment_event', event => {
  console.log('paymentEvent_app_id=' + event.appId);
  console.log('amount=' + event.amount);
  window.document.getElementById('payment_info').innerHTML = JSON.stringify(
    event,
    null,
    2,
  );
  Appboxo.hideMiniapps();
});
window.openMiniapp = () => {
  Appboxo.openMiniapp({ appId: appId, saveState: false });
};

window.getMiniapps = () => {
  Appboxo.getMiniapps().then(result => {
    console.log(JSON.stringify(result));
  });
};

window.submit = () => {
  const r = JSON.parse(
    window.document.getElementById('payment_info').innerHTML,
  );

  Appboxo.sendPaymentEvent({
    appId,
    miniappOrderId: r.miniappOrderId,
    amount: r.amount,
    currency: r.currency,
    transactionToken: r.transactionToken,
    status: 'success',
  });
  Appboxo.openMiniapp({ appId });
};

window.cancel = () => {
  const r = JSON.parse(
    window.document.getElementById('payment_info').innerHTML,
  );

  Appboxo.sendPaymentEvent({
    appId,
    miniappOrderId: r.miniappOrderId,
    status: 'cancelled',
  });

  Appboxo.openMiniapp({ appId });
};

window.upgrade = () => {
  Appboxo.sendCustomEvent({
    appId,
    requestId,
    type: 'upgrade_plan',
    payload: { status: 'success' },
  });
  Appboxo.openMiniapp({ appId });
};
window.cancelUpgrade = () => {
  Appboxo.sendCustomEvent({
    appId,
    requestId,
    type: 'upgrade_plan',
    payload: { status: 'cancelled' },
  });
  Appboxo.openMiniapp({ appId });
};
