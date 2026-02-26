/* eslint-disable no-undef */
import { Boxo } from 'capacitor-boxo-sdk';

const clientId = '';
const authCode = '';
const appId = '';

let requestId = '';

Boxo.setConfig({
  clientId: clientId,
  enableMultitaskMode: false,
  userId: '',
  showAboutPage: false,
  splashScreenOptions: {
    lightProgressIndicator: "#000000",
    lightProgressTrack: "#DEDEDE",
    darkProgressIndicator: "#DEDEDE",
    darkProgressTrack: "#000000",
    lightBackground:"#C495FF",
    darkBackground:"#6A22C9"
  }
});

Boxo.addListener('custom_event', customEvent => {
  console.log(customEvent);
  console.log('custom_event app_id=' + customEvent.appId);
  if (customEvent.type === 'upgrade_plan') {
    console.log('upgrade plan');
    requestId = customEvent.requestId;
    window.document.getElementById('upgrade_notification').innerHTML =
      "Received 'upgrade_plan' event";
    Boxo.hideMiniapps();
  }
});
Boxo.addListener('miniapp_lifecycle', event => {
  console.log('lifecycle_app_id=' + event.appId);
  console.log('lifecycle=' + event.lifecycle);
  if (event.lifecycle == 'onAuth') {
    console.log('get auth code from hostapp backend');
    Boxo.setAuthCode({
      appId: event.appId,
      authCode,
    });
  }
});
Boxo.addListener('payment_event', event => {
  console.log('paymentEvent_app_id=' + event.appId);
  console.log('amount=' + event.amount);
  window.document.getElementById('payment_info').innerHTML = JSON.stringify(
    event,
    null,
    2,
  );
  Boxo.hideMiniapps();
});
window.openMiniapp = () => {
  Boxo.openMiniapp({ appId: appId, saveState: false, pageAnimation: 'RIGHT_TO_LEFT' });
};

window.getMiniapps = () => {
  Boxo.getMiniapps().then(result => {
    console.log(JSON.stringify(result));
  });
};

window.submit = () => {
  const r = JSON.parse(
    window.document.getElementById('payment_info').innerHTML,
  );

  Boxo.sendPaymentEvent({
    appId,
    miniappOrderId: r.miniappOrderId,
    amount: r.amount,
    currency: r.currency,
    transactionToken: r.transactionToken,
    status: 'success',
  });
  Boxo.openMiniapp({ appId });
};

window.cancel = () => {
  const r = JSON.parse(
    window.document.getElementById('payment_info').innerHTML,
  );

  Boxo.sendPaymentEvent({
    appId,
    miniappOrderId: r.miniappOrderId,
    status: 'cancelled',
  });

  Boxo.openMiniapp({ appId });
};

window.upgrade = () => {
  Boxo.sendCustomEvent({
    appId,
    requestId,
    type: 'upgrade_plan',
    payload: { status: 'success' },
  });
  Boxo.openMiniapp({ appId });
};
window.cancelUpgrade = () => {
  Boxo.sendCustomEvent({
    appId,
    requestId,
    type: 'upgrade_plan',
    payload: { status: 'cancelled' },
  });
  Boxo.openMiniapp({ appId });
};
