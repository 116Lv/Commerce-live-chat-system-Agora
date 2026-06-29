import { useCallback, useEffect, useState } from 'react';
import { Alert, Button, ButtonGroup, Card, Col, Form, Row, Tab, Table, Tabs } from 'react-bootstrap';
import { CheckCircle2, EyeOff, RefreshCw, Search, Send } from 'lucide-react';
import {
  createCouponEvent,
  getAdminCouponEvent,
  getAdminCouponEventCoupons,
  getAdminCouponEvents,
  getAdminDashboard,
  getAdminMe,
  getAdminPayments,
  getAdminProducts,
  getAdminProductReports,
  getAdminRefunds,
  getAdminUserReports,
  getAdminUsers,
  hideAdminProduct,
  issueCouponEventToUsers,
  resolveAdminProductReport,
  resolveAdminUserReport,
  settleAdminSettlement,
  updateAdminAccountRole,
  updateAdminUserStatus,
  verifyAdminPayment
} from '../api/adminApi.js';
import EmptyState from '../components/EmptyState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { formatDateTime, statusText } from './pageUtils.jsx';
import {
  canSettlePayment,
  canUpdateAdminRoles,
  dashboardStats,
  getId,
  getList,
  getReportTabs,
  parseUserIds,
  removeFromPayload,
  replaceInPayload
} from './adminPageUtils.js';

const PAGE_SIZE = 20;
const USER_STATUSES = ['ACTIVE', 'SUSPENDED', 'BLOCKED', 'DELETED'];
const ADMIN_ROLES = ['ROOT_ADMIN', 'USER_ADMIN', 'PRODUCT_ADMIN', 'SETTLEMENT_ADMIN'];
const PAYMENT_STATUSES = ['', 'READY', 'CONFIRMING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED'];
const COUPON_TYPES = ['FIRST_COME', 'NEW_SIGNUP', 'ADMIN_INDIVIDUAL'];

function useAdminResource(loader, deps = [], enabled = true) {
  const [state, setState] = useState({ data: null, error: null, loading: enabled });

  const load = useCallback(async () => {
    if (!enabled) {
      setState({ data: null, error: null, loading: false });
      return;
    }

    setState((current) => ({ ...current, error: null, loading: true }));

    try {
      const data = await loader();
      setState({ data, error: null, loading: false });
    } catch (error) {
      setState({ data: null, error, loading: false });
    }
  }, [enabled, ...deps]);

  useEffect(() => {
    load();
  }, [load]);

  const setData = useCallback((updater) => {
    setState((current) => ({
      ...current,
      data: typeof updater === 'function' ? updater(current.data) : updater
    }));
  }, []);

  return { ...state, reload: load, setData };
}

function AdminPageHeader({ title, eyebrow = 'Admin', action }) {
  return (
    <div className="page-header admin-page-header">
      <div>
        <p className="page-eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
      </div>
      {action ? <div>{action}</div> : null}
    </div>
  );
}

function Feedback({ notice, error }) {
  if (error) {
    return (
      <Alert variant="danger" className="py-2">
        {error}
      </Alert>
    );
  }

  if (notice) {
    return (
      <Alert variant="success" className="py-2">
        {notice}
      </Alert>
    );
  }

  return null;
}

function StatCard({ label, value }) {
  return (
    <Card className="admin-stat-card">
      <Card.Body>
        <span>{label}</span>
        <strong>{value}</strong>
      </Card.Body>
    </Card>
  );
}

function AdminTable({ children }) {
  return (
    <div className="list-card table-responsive">
      <Table hover className="mb-0 align-middle">
        {children}
      </Table>
    </div>
  );
}

function ActionButton({ icon: Icon, children, ...props }) {
  return (
    <Button {...props}>
      {Icon ? <Icon size={16} className="me-1" aria-hidden="true" /> : null}
      {children}
    </Button>
  );
}

function useActionFeedback() {
  const [notice, setNotice] = useState('');
  const [actionError, setActionError] = useState('');

  const run = async (action, successMessage, onSuccess) => {
    setNotice('');
    setActionError('');

    try {
      const result = await action();
      onSuccess?.(result);
      setNotice(successMessage);
    } catch (error) {
      setActionError(error.message || '요청 실패');
    }
  };

  return { notice, actionError, run };
}

export function AdminDashboardPage() {
  const dashboard = useAdminResource(getAdminDashboard, []);
  const me = useAdminResource(getAdminMe, []);

  if (dashboard.loading || me.loading) {
    return <LoadingState label="관리자 정보를 불러오는 중" />;
  }

  if (dashboard.error || me.error) {
    return (
      <ErrorState
        title="대시보드 조회 실패"
        message={(dashboard.error || me.error).message}
        onRetry={() => {
          dashboard.reload();
          me.reload();
        }}
      />
    );
  }

  const stats = dashboardStats(dashboard.data);

  return (
    <section>
      <AdminPageHeader title="대시보드" />
      <Row className="g-3 mb-4">
        {stats.map((stat) => (
          <Col xs={12} md={4} key={stat.label}>
            <StatCard label={stat.label} value={stat.value} />
          </Col>
        ))}
      </Row>
      <div className="detail-panel">
        <h2 className="section-title">관리자 정보</h2>
        <dl className="compact-list compact-list-inline">
          <div>
            <dt>이메일</dt>
            <dd>{me.data?.email || '-'}</dd>
          </div>
          <div>
            <dt>이름</dt>
            <dd>{me.data?.nickname || me.data?.name || '-'}</dd>
          </div>
          <div>
            <dt>권한</dt>
            <dd>{me.data?.role || dashboard.data?.role || '-'}</dd>
          </div>
          <div>
            <dt>ID</dt>
            <dd>{me.data?.id || me.data?.adminId || '-'}</dd>
          </div>
        </dl>
      </div>
    </section>
  );
}

export function AdminProductsPage() {
  const [reportedOnly, setReportedOnly] = useState(false);
  const products = useAdminResource(
    () => getAdminProducts({ reportedOnly, page: 0, size: PAGE_SIZE }),
    [reportedOnly]
  );
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(products.data);

  const handleHide = (product) => {
    const productId = getId(product, ['id', 'productId']);
    run(() => hideAdminProduct(productId), '상품을 숨김 처리했습니다.', (updated) => {
      products.setData((current) => replaceInPayload(current, updated, 'id'));
    });
  };

  return (
    <section>
      <AdminPageHeader title="상품 관리" />
      <div className="toolbar-panel mb-3">
        <Form.Check
          type="switch"
          id="reported-only"
          label="신고 상품만"
          checked={reportedOnly}
          onChange={(event) => setReportedOnly(event.target.checked)}
        />
      </div>
      <Feedback notice={notice} error={actionError} />
      {products.loading ? <LoadingState label="상품을 불러오는 중" /> : null}
      {products.error ? <ErrorState title="상품 조회 실패" message={products.error.message} onRetry={products.reload} /> : null}
      {!products.loading && !products.error && rows.length === 0 ? <EmptyState title="상품이 없습니다" /> : null}
      {!products.loading && !products.error && rows.length > 0 ? (
        <AdminTable>
          <thead>
            <tr>
              <th>상품</th>
              <th>판매자</th>
              <th>가격</th>
              <th>상태</th>
              <th className="text-end">작업</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((product) => (
              <tr key={getId(product, ['id', 'productId'])}>
                <td>{product.title || '-'}</td>
                <td>{product.sellerId || '-'}</td>
                <td>
                  <MoneyText amount={product.price} />
                </td>
                <td>
                  <StatusBadge status={product.status} />
                </td>
                <td className="text-end">
                  <ActionButton
                    icon={EyeOff}
                    size="sm"
                    variant="outline-danger"
                    disabled={String(product.status).toUpperCase() === 'HIDDEN'}
                    onClick={() => handleHide(product)}
                  >
                    숨김
                  </ActionButton>
                </td>
              </tr>
            ))}
          </tbody>
        </AdminTable>
      ) : null}
    </section>
  );
}

export function AdminUsersPage() {
  const users = useAdminResource(() => getAdminUsers({ page: 0, size: PAGE_SIZE }), []);
  const me = useAdminResource(getAdminMe, []);
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(users.data);
  const canEditRoles = canUpdateAdminRoles(me.data);

  const handleStatus = (user, status) => {
    const userId = getId(user, ['id', 'userId']);
    run(() => updateAdminUserStatus(userId, status), '회원 상태를 변경했습니다.', (updated) => {
      users.setData((current) => replaceInPayload(current, updated, 'id'));
    });
  };

  const handleRole = (user, role) => {
    const userId = getId(user, ['id', 'userId']);
    run(() => updateAdminAccountRole(userId, role), '관리자 권한을 변경했습니다.', (updated) => {
      users.setData((current) => replaceInPayload(current, updated, 'id'));
    });
  };

  return (
    <section>
      <AdminPageHeader title="회원 관리" />
      <Feedback notice={notice} error={actionError} />
      {users.loading || me.loading ? <LoadingState label="회원을 불러오는 중" /> : null}
      {users.error || me.error ? (
        <ErrorState
          title="회원 조회 실패"
          message={(users.error || me.error).message}
          onRetry={() => {
            users.reload();
            me.reload();
          }}
        />
      ) : null}
      {!users.loading && !me.loading && !users.error && !me.error && rows.length === 0 ? (
        <EmptyState title="회원이 없습니다" />
      ) : null}
      {!users.loading && !me.loading && !users.error && !me.error && rows.length > 0 ? (
        <AdminTable>
          <thead>
            <tr>
              <th>회원</th>
              <th>이메일</th>
              <th>권한</th>
              <th>상태</th>
              <th>변경</th>
              {canEditRoles ? <th>권한 변경</th> : null}
            </tr>
          </thead>
          <tbody>
            {rows.map((user) => (
              <tr key={getId(user, ['id', 'userId'])}>
                <td>{user.nickname || '-'}</td>
                <td>{user.email || '-'}</td>
                <td>{user.role || '-'}</td>
                <td>
                  <StatusBadge status={user.status} />
                </td>
                <td>
                  <Form.Select
                    size="sm"
                    value={user.status || ''}
                    aria-label="회원 상태 변경"
                    onChange={(event) => handleStatus(user, event.target.value)}
                  >
                    {USER_STATUSES.map((status) => (
                      <option value={status} key={status}>
                        {statusText(status)}
                      </option>
                    ))}
                  </Form.Select>
                </td>
                {canEditRoles ? (
                  <td>
                    <Form.Select
                      size="sm"
                      value={user.role || ''}
                      aria-label="관리자 권한 변경"
                      onChange={(event) => handleRole(user, event.target.value)}
                    >
                      {ADMIN_ROLES.map((role) => (
                        <option value={role} key={role}>
                          {role}
                        </option>
                      ))}
                    </Form.Select>
                  </td>
                ) : null}
              </tr>
            ))}
          </tbody>
        </AdminTable>
      ) : null}
    </section>
  );
}

function ReportTable({ rows, type, memos, setMemo, onResolve }) {
  if (rows.length === 0) {
    return <EmptyState title="신고 내역이 없습니다" />;
  }

  return (
    <AdminTable>
      <thead>
        <tr>
          <th>신고</th>
          <th>대상</th>
          <th>사유</th>
          <th>상태</th>
          <th>처리 메모</th>
          <th className="text-end">작업</th>
        </tr>
      </thead>
      <tbody>
        {rows.map((report) => {
          const reportId = report.reportId;
          const memoKey = `${type}-${reportId}`;
          const target = type === 'product' ? report.productId : report.reportedUserId;

          return (
            <tr key={memoKey}>
              <td>#{reportId}</td>
              <td>{target || '-'}</td>
              <td>{report.reason || '-'}</td>
              <td>
                <StatusBadge status={report.status} />
              </td>
              <td>
                <Form.Control
                  size="sm"
                  value={memos[memoKey] || ''}
                  placeholder="처리 메모"
                  onChange={(event) => setMemo(memoKey, event.target.value)}
                />
              </td>
              <td className="text-end">
                <ActionButton
                  icon={CheckCircle2}
                  size="sm"
                  variant="outline-primary"
                  disabled={!String(memos[memoKey] || '').trim()}
                  onClick={() => onResolve(reportId, memos[memoKey] || '')}
                >
                  처리
                </ActionButton>
              </td>
            </tr>
          );
        })}
      </tbody>
    </AdminTable>
  );
}

export function AdminReportsPage() {
  const me = useAdminResource(getAdminMe, []);
  const tabs = getReportTabs(me.data);
  const canReadUserReports = tabs.some((tab) => tab.key === 'users');
  const canReadProductReports = tabs.some((tab) => tab.key === 'products');
  const userReports = useAdminResource(getAdminUserReports, [], canReadUserReports);
  const productReports = useAdminResource(getAdminProductReports, [], canReadProductReports);
  const [memos, setMemos] = useState({});
  const { notice, actionError, run } = useActionFeedback();

  const setMemo = (key, value) => setMemos((current) => ({ ...current, [key]: value }));
  const clearMemo = (key) => setMemos((current) => ({ ...current, [key]: '' }));

  const handleResolve = (type, reportId, memo) => {
    const key = `${type}-${reportId}`;
    const resource = type === 'user' ? userReports : productReports;
    const action = type === 'user' ? resolveAdminUserReport : resolveAdminProductReport;

    run(() => action(reportId, memo), '신고를 처리했습니다.', () => {
      resource.setData((current) => removeFromPayload(current, reportId, 'reportId'));
      clearMemo(key);
    });
  };

  if (me.loading || userReports.loading || productReports.loading) {
    return <LoadingState label="신고를 불러오는 중" />;
  }

  if (me.error) {
    return <ErrorState title="관리자 정보 조회 실패" message={me.error.message} onRetry={me.reload} />;
  }

  if (tabs.length === 0) {
    return (
      <section>
        <AdminPageHeader title="신고 관리" />
        <EmptyState title="조회 가능한 신고 메뉴가 없습니다" />
      </section>
    );
  }

  if ((canReadUserReports && userReports.error) || (canReadProductReports && productReports.error)) {
    return (
      <ErrorState
        title="신고 조회 실패"
        message={(userReports.error || productReports.error).message}
        onRetry={() => {
          userReports.reload();
          productReports.reload();
        }}
      />
    );
  }

  return (
    <section>
      <AdminPageHeader title="신고 관리" />
      <Feedback notice={notice} error={actionError} />
      <Tabs defaultActiveKey={tabs[0].key} className="mb-3">
        {canReadUserReports ? (
          <Tab eventKey="users" title="회원 신고">
            <ReportTable
              rows={getList(userReports.data)}
              type="user"
              memos={memos}
              setMemo={setMemo}
              onResolve={(reportId, memo) => handleResolve('user', reportId, memo)}
            />
          </Tab>
        ) : null}
        {canReadProductReports ? (
          <Tab eventKey="products" title="상품 신고">
            <ReportTable
              rows={getList(productReports.data)}
              type="product"
              memos={memos}
              setMemo={setMemo}
              onResolve={(reportId, memo) => handleResolve('product', reportId, memo)}
            />
          </Tab>
        ) : null}
      </Tabs>
    </section>
  );
}

function PaymentTable({ rows, onVerify, onSettle, showVerify }) {
  if (rows.length === 0) {
    return <EmptyState title="결제 내역이 없습니다" />;
  }

  return (
    <AdminTable>
      <thead>
        <tr>
          <th>결제</th>
          <th>주문</th>
          <th>금액</th>
          <th>상태</th>
          <th>정산</th>
          <th>요청일</th>
          <th className="text-end">작업</th>
        </tr>
      </thead>
      <tbody>
        {rows.map((payment) => {
          const paymentId = getId(payment, ['paymentId', 'id']);
          const settlementId = payment.settlementId;

          return (
            <tr key={`${paymentId}-${settlementId || 'payment'}`}>
              <td>#{paymentId}</td>
              <td>{payment.orderId || '-'}</td>
              <td>
                <MoneyText amount={payment.amount} />
              </td>
              <td>
                <StatusBadge status={payment.status} />
              </td>
              <td>{payment.settlementStatus ? <StatusBadge status={payment.settlementStatus} /> : '-'}</td>
              <td>{formatDateTime(payment.requestedAt || payment.settledAt)}</td>
              <td className="text-end">
                <ButtonGroup size="sm">
                  {showVerify ? (
                    <ActionButton icon={RefreshCw} variant="outline-primary" onClick={() => onVerify(paymentId)}>
                      검증
                    </ActionButton>
                  ) : null}
                  {canSettlePayment(payment) ? (
                    <ActionButton icon={CheckCircle2} variant="outline-success" onClick={() => onSettle(settlementId)}>
                      정산
                    </ActionButton>
                  ) : null}
                </ButtonGroup>
              </td>
            </tr>
          );
        })}
      </tbody>
    </AdminTable>
  );
}

export function AdminPaymentsPage() {
  const [status, setStatus] = useState('');
  const payments = useAdminResource(() => getAdminPayments({ status, page: 0, size: PAGE_SIZE }), [status]);
  const refunds = useAdminResource(getAdminRefunds, []);
  const { notice, actionError, run } = useActionFeedback();

  const handleVerify = (paymentId) => {
    run(() => verifyAdminPayment(paymentId), '결제를 검증했습니다.', (updated) => {
      payments.setData((current) => replaceInPayload(current, updated, 'paymentId'));
    });
  };

  const handleSettle = (settlementId) => {
    run(() => settleAdminSettlement(settlementId), '정산을 실행했습니다.', () => {
      payments.reload();
      refunds.reload();
    });
  };

  return (
    <section>
      <AdminPageHeader title="결제 관리" />
      <div className="toolbar-panel mb-3">
        <Form.Label htmlFor="payment-status">결제 상태</Form.Label>
        <Form.Select id="payment-status" value={status} onChange={(event) => setStatus(event.target.value)}>
          {PAYMENT_STATUSES.map((item) => (
            <option value={item} key={item || 'all'}>
              {item ? statusText(item) : '전체'}
            </option>
          ))}
        </Form.Select>
      </div>
      <Feedback notice={notice} error={actionError} />
      <h2 className="section-title">결제</h2>
      {payments.loading ? <LoadingState label="결제를 불러오는 중" /> : null}
      {payments.error ? <ErrorState title="결제 조회 실패" message={payments.error.message} onRetry={payments.reload} /> : null}
      {!payments.loading && !payments.error ? (
        <PaymentTable rows={getList(payments.data)} onVerify={handleVerify} onSettle={handleSettle} showVerify />
      ) : null}
      <h2 className="section-title mt-4">환불</h2>
      {refunds.loading ? <LoadingState label="환불을 불러오는 중" /> : null}
      {refunds.error ? <ErrorState title="환불 조회 실패" message={refunds.error.message} onRetry={refunds.reload} /> : null}
      {!refunds.loading && !refunds.error ? (
        <PaymentTable rows={getList(refunds.data)} onVerify={handleVerify} onSettle={handleSettle} showVerify={false} />
      ) : null}
    </section>
  );
}

function defaultCouponForm() {
  return {
    type: 'ADMIN_INDIVIDUAL',
    name: '',
    startAt: '',
    endAt: '',
    totalQuantity: 1,
    discountAmount: 0,
    minOrderAmount: 0,
    validDays: 30
  };
}

export function AdminCouponsPage() {
  const events = useAdminResource(getAdminCouponEvents, []);
  const [form, setForm] = useState(defaultCouponForm);
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [coupons, setCoupons] = useState([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [userIds, setUserIds] = useState('');
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(events.data);

  const updateForm = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const loadEventDetail = async (eventId) => {
    setSelectedEventId(eventId);
    setDetailLoading(true);
    setDetailError('');

    try {
      const [eventDetail, issuedCoupons] = await Promise.all([
        getAdminCouponEvent(eventId),
        getAdminCouponEventCoupons(eventId)
      ]);
      setDetail(eventDetail);
      setCoupons(getList(issuedCoupons));
    } catch (error) {
      setDetail(null);
      setCoupons([]);
      setDetailError(error.message || '쿠폰 상세 조회 실패');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleCreate = (event) => {
    event.preventDefault();
    run(
      () =>
        createCouponEvent({
          ...form,
          totalQuantity: Number(form.totalQuantity),
          discountAmount: Number(form.discountAmount),
          minOrderAmount: Number(form.minOrderAmount),
          validDays: Number(form.validDays)
        }),
      '쿠폰 이벤트를 만들었습니다.',
      (created) => {
        events.setData((current) => [created, ...getList(current)]);
        setForm(defaultCouponForm());
        setSelectedEventId(created.eventId);
        loadEventDetail(created.eventId);
      }
    );
  };

  const handleIssue = (event) => {
    event.preventDefault();
    const ids = parseUserIds(userIds);

    if (!selectedEventId || ids.length === 0) {
      return;
    }

    run(() => issueCouponEventToUsers(selectedEventId, ids), '쿠폰을 발급했습니다.', () => {
      setUserIds('');
      loadEventDetail(selectedEventId);
      events.reload();
    });
  };

  return (
    <section>
      <AdminPageHeader title="쿠폰 관리" />
      <Feedback notice={notice} error={actionError} />
      <Row className="g-3">
        <Col lg={5}>
          <Form className="form-card p-3" onSubmit={handleCreate}>
            <h2 className="section-title">이벤트 생성</h2>
            <Form.Group className="mb-2" controlId="coupon-type">
              <Form.Label>유형</Form.Label>
              <Form.Select value={form.type} onChange={(event) => updateForm('type', event.target.value)}>
                {COUPON_TYPES.map((type) => (
                  <option value={type} key={type}>
                    {type}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-2" controlId="coupon-name">
              <Form.Label>이름</Form.Label>
              <Form.Control value={form.name} onChange={(event) => updateForm('name', event.target.value)} required />
            </Form.Group>
            <Row className="g-2">
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-start">
                  <Form.Label>시작</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={form.startAt}
                    onChange={(event) => updateForm('startAt', event.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-end">
                  <Form.Label>종료</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={form.endAt}
                    onChange={(event) => updateForm('endAt', event.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>
            <Row className="g-2">
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-quantity">
                  <Form.Label>수량</Form.Label>
                  <Form.Control
                    type="number"
                    min="1"
                    value={form.totalQuantity}
                    onChange={(event) => updateForm('totalQuantity', event.target.value)}
                  />
                </Form.Group>
              </Col>
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-valid-days">
                  <Form.Label>유효일</Form.Label>
                  <Form.Control
                    type="number"
                    min="1"
                    value={form.validDays}
                    onChange={(event) => updateForm('validDays', event.target.value)}
                  />
                </Form.Group>
              </Col>
            </Row>
            <Row className="g-2">
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-discount">
                  <Form.Label>할인액</Form.Label>
                  <Form.Control
                    type="number"
                    min="0"
                    value={form.discountAmount}
                    onChange={(event) => updateForm('discountAmount', event.target.value)}
                  />
                </Form.Group>
              </Col>
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-min-order">
                  <Form.Label>최소주문</Form.Label>
                  <Form.Control
                    type="number"
                    min="0"
                    value={form.minOrderAmount}
                    onChange={(event) => updateForm('minOrderAmount', event.target.value)}
                  />
                </Form.Group>
              </Col>
            </Row>
            <ActionButton icon={CheckCircle2} type="submit" variant="primary" className="w-100">
              생성
            </ActionButton>
          </Form>
        </Col>
        <Col lg={7}>
          <h2 className="section-title">이벤트</h2>
          {events.loading ? <LoadingState label="쿠폰 이벤트를 불러오는 중" /> : null}
          {events.error ? <ErrorState title="쿠폰 조회 실패" message={events.error.message} onRetry={events.reload} /> : null}
          {!events.loading && !events.error && rows.length === 0 ? <EmptyState title="쿠폰 이벤트가 없습니다" /> : null}
          {!events.loading && !events.error && rows.length > 0 ? (
            <div className="stack-list">
              {rows.map((event) => (
                <div className="list-card p-3 list-card-row" key={event.eventId}>
                  <div>
                    <h2>{event.name}</h2>
                    <p className="mb-1 text-muted">
                      {formatDateTime(event.startAt)} - {formatDateTime(event.endAt)}
                    </p>
                    <StatusBadge status={event.status} /> <span className="ms-2">{event.issuedQuantity}/{event.totalQuantity}</span>
                  </div>
                  <ActionButton icon={Search} size="sm" variant="outline-primary" onClick={() => loadEventDetail(event.eventId)}>
                    상세
                  </ActionButton>
                </div>
              ))}
            </div>
          ) : null}
        </Col>
      </Row>
      {selectedEventId ? (
        <div className="detail-panel mt-4">
          <h2 className="section-title">발급 관리</h2>
          {detailLoading ? <LoadingState label="발급 내역을 불러오는 중" /> : null}
          {detailError ? (
            <Alert variant="danger" className="py-2">
              {detailError}
            </Alert>
          ) : null}
          {!detailLoading ? (
            <>
              <p className="fw-bold mb-2">{detail?.name || `이벤트 #${selectedEventId}`}</p>
              <Form className="profile-form-row mb-3" onSubmit={handleIssue}>
                <Form.Control
                  value={userIds}
                  placeholder="회원 ID 입력: 1, 2, 3"
                  onChange={(event) => setUserIds(event.target.value)}
                />
                <ActionButton icon={Send} type="submit" variant="primary" disabled={parseUserIds(userIds).length === 0}>
                  발급
                </ActionButton>
              </Form>
              {coupons.length === 0 ? <EmptyState title="발급 쿠폰이 없습니다" /> : null}
              {coupons.length > 0 ? (
                <AdminTable>
                  <thead>
                    <tr>
                      <th>쿠폰</th>
                      <th>회원</th>
                      <th>상태</th>
                      <th>발급일</th>
                      <th>만료일</th>
                    </tr>
                  </thead>
                  <tbody>
                    {coupons.map((coupon) => (
                      <tr key={coupon.couponId}>
                        <td>#{coupon.couponId}</td>
                        <td>{coupon.userNickname || coupon.userId || '-'}</td>
                        <td>
                          <StatusBadge status={coupon.status} />
                        </td>
                        <td>{formatDateTime(coupon.issuedAt)}</td>
                        <td>{formatDateTime(coupon.expiresAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </AdminTable>
              ) : null}
            </>
          ) : null}
        </div>
      ) : null}
    </section>
  );
}

export function AdminNotFoundPage() {
  return (
    <section>
      <AdminPageHeader title="페이지 없음" />
      <EmptyState title="관리자 페이지를 찾을 수 없습니다" />
    </section>
  );
}
