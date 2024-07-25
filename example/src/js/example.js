/* eslint-disable no-undef */
import { Appboxo } from 'capacitor-boxo-sdk';

const clientId = '';
const authCode = '';
const appId = '';

Appboxo.setConfig({ clientId: clientId, enableMultitaskMode: true });

Appboxo.addListener('custom_event', customEvent => {
  console.log('custom_event app_id=' + customEvent.appId);
  customEvent.payload = { test: 'test msg' };
  Appboxo.sendCustomEvent(customEvent);
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
  Appboxo.openMiniapp({ appId });
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
    status: 'fail',
  });

  Appboxo.openMiniapp({ appId });
};
