import apiClient from './client.js';

export const preparePayment = (tradeId, config = {}) =>
  apiClient.post(`/api/payments/trades/${tradeId}/prepare`, null, config);

export const confirmPayment = (paymentId, payload, config = {}) =>
  apiClient.post(`/api/payments/${paymentId}/confirm`, payload, config);

export const refundPayment = (paymentId, payload, config = {}) =>
  apiClient.post(`/api/payments/${paymentId}/refund`, payload, config);

export const getRefundStatus = (paymentId, config = {}) => apiClient.get(`/api/payments/${paymentId}/refund`, config);

const getBrowserWindow = () => (typeof window === 'undefined' ? null : window);

const getPaymentName = (payment) =>
  payment.orderName || payment.productName || payment.itemName || `Order ${payment.orderId || payment.paymentId}`;

const getPaymentKeyFromApproval = (approval = {}) =>
  approval.paymentKey || approval.transactionId || approval.txId || approval.imp_uid || approval.merchant_uid;

const getPortOneStoreId = (payment) => payment.storeId || import.meta.env?.VITE_PORTONE_STORE_ID;

const getPortOneChannelKey = (payment) => payment.channelKey || import.meta.env?.VITE_PORTONE_CHANNEL_KEY;

const getBuyerEmail = (payment) => payment.buyerEmail || payment.customer?.email;

const getBuyerName = (payment) => payment.buyerName || payment.customer?.fullName || payment.customer?.name;

const getBuyerTel = (payment) => payment.buyerTel || payment.customer?.phoneNumber || payment.customer?.phone;

const getLocalMode = (payment) => {
  const mode = String(payment?.paymentMode || payment?.pgMode || payment?.mode || '').toUpperCase();

  return mode === 'LOCAL' || mode === 'TEST' || payment?.localPayment === true;
};

const buildPortOneCustomer = (payment) => {
  if (payment.customer) {
    return payment.customer;
  }

  const customer = {
    email: getBuyerEmail(payment),
    fullName: getBuyerName(payment),
    phoneNumber: getBuyerTel(payment)
  };

  Object.keys(customer).forEach((key) => {
    if (!customer[key]) {
      delete customer[key];
    }
  });

  return Object.keys(customer).length > 0 ? customer : undefined;
};

const buildResult = (payment, approval, local = false) => {
  const paymentKey = getPaymentKeyFromApproval(approval);

  if (!paymentKey) {
    throw new Error('PG 승인 정보를 확인할 수 없습니다.');
  }

  return {
    paymentId: payment.paymentId,
    paymentKey,
    local
  };
};

const requestPortOnePayment = async (payment, win, redirectUrl) => {
  const storeId = getPortOneStoreId(payment);
  const channelKey = getPortOneChannelKey(payment);

  if (!storeId || !channelKey) {
    throw new Error('PortOne 결제 설정을 확인할 수 없습니다.');
  }

  const approval = await win.PortOne.requestPayment({
    storeId,
    channelKey,
    paymentId: payment.orderId || String(payment.paymentId),
    orderName: getPaymentName(payment),
    totalAmount: Number(payment.amount || 0),
    currency: payment.currency || 'CURRENCY_KRW',
    payMethod: payment.payMethod || 'CARD',
    customer: buildPortOneCustomer(payment),
    redirectUrl
  });

  if (approval?.code || approval?.error_code) {
    throw new Error(approval.message || approval.error_msg || 'PG 결제 승인이 취소되었습니다.');
  }

  return buildResult(payment, approval);
};

const requestImpPayment = (payment, win, redirectUrl) =>
  new Promise((resolve, reject) => {
    if (payment.merchantCode || payment.impMerchantCode) {
      win.IMP.init(payment.merchantCode || payment.impMerchantCode);
    }

    win.IMP.request_pay(
      {
        pg: payment.pg,
        pay_method: payment.payMethod || 'card',
        merchant_uid: payment.orderId || String(payment.paymentId),
        name: getPaymentName(payment),
        amount: Number(payment.amount || 0),
        buyer_email: getBuyerEmail(payment),
        buyer_name: getBuyerName(payment),
        buyer_tel: getBuyerTel(payment),
        m_redirect_url: redirectUrl
      },
      (approval) => {
        if (!approval?.success) {
          reject(new Error(approval?.error_msg || 'PG 결제 승인이 취소되었습니다.'));
          return;
        }

        try {
          resolve(buildResult(payment, approval));
        } catch (err) {
          reject(err);
        }
      }
    );
  });

export const requestPaymentApproval = async (payment, options = {}) => {
  if (!payment?.paymentId || !payment?.orderId) {
    throw new Error('결제 주문 정보를 확인할 수 없습니다.');
  }

  const win = options.window || getBrowserWindow();
  const redirectUrl = options.redirectUrl;

  if (getLocalMode(payment)) {
    return buildResult(payment, { paymentKey: `local-${payment.orderId}` }, true);
  }

  if (win?.PortOne?.requestPayment) {
    return requestPortOnePayment(payment, win, redirectUrl);
  }

  if (win?.IMP?.request_pay) {
    return requestImpPayment(payment, win, redirectUrl);
  }

  throw new Error('사용 가능한 PG 결제 SDK를 찾을 수 없습니다.');
};
