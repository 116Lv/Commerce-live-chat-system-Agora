import { useCallback, useEffect, useState } from 'react';
import { Alert, Badge, Button, ButtonGroup, Card, Col, Form, InputGroup, Modal, ProgressBar, Row, Tab, Table, Tabs } from 'react-bootstrap';
import { CheckCircle2, EyeOff, RefreshCw, Search, Send } from 'lucide-react';
import {
  createCouponEvent,
  approveAdminApprovalRequest,
  approveAdminProduct,
  getAdminAccounts,
  getAdminApprovalRequests,
  getAdminCouponEvent,
  getAdminCouponEventCoupons,
  getAdminCouponEvents,
  getAdminDashboard,
  getAdminMe,
  getMyAdminApprovalRequests,
  getAdminPayments,
  getAdminProducts,
  getAdminProductReports,
  getAdminRefunds,
  getAdminUserReports,
  getAdminUsers,
  hideAdminProduct,
  issueCouponEventToUsers,
  rejectAdminApprovalRequest,
  requestAdminRoleChangeApproval,
  resolveAdminProductReport,
  resolveAdminUserReport,
  settleAdminSettlement,
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
  ADMIN_ROLE_LABELS,
  REPORT_STATUS_FILTERS,
  canSettlePayment,
  buildAdminCouponCreatePayload,
  canAdminIssueCoupon,
  dashboardStats,
  formatAdminRole,
  formatAdminCouponMoneyInput,
  formatAdminCouponStatus,
  formatAdminCouponType,
  formatIssueRate,
  getReportStatusLabel,
  getCouponTargetChipKey,
  getAdminCouponStatusVariant,
  getId,
  getList,
  getReportTabs,
  paymentVerificationSummary,
  parseUserIdTokens,
  reportMatchesSearch,
  removeFromPayload,
  replaceInPayload,
  validateAdminCouponForm
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
  const [approvalStatus, setApprovalStatus] = useState('');
  const [selectedProduct, setSelectedProduct] = useState(null);
  const products = useAdminResource(
    () => getAdminProducts({ reportedOnly, approvalStatus, page: 0, size: PAGE_SIZE }),
    [reportedOnly, approvalStatus]
  );
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(products.data);

  const updateProductRow = (updated) => {
    products.setData((current) => replaceInPayload(current, updated, 'id'));
    setSelectedProduct(updated);
  };

  const handleHide = (product) => {
    const productId = getId(product, ['id', 'productId']);
    run(() => hideAdminProduct(productId), 'Product has been hidden.', updateProductRow);
  };

  const handleApprove = (product) => {
    const productId = getId(product, ['id', 'productId']);
    run(() => approveAdminProduct(productId), 'Product has been approved.', updateProductRow);
  };

  return (
    <section>
      <AdminPageHeader title="Product management" />
      <div className="toolbar-panel mb-3 admin-product-toolbar">
        <Form.Check
          type="switch"
          id="reported-only"
          label="Reported only"
          checked={reportedOnly}
          onChange={(event) => setReportedOnly(event.target.checked)}
        />
        <Form.Select
          value={approvalStatus}
          aria-label="Product approval status"
          onChange={(event) => setApprovalStatus(event.target.value)}
        >
          <option value="">All approval statuses</option>
          <option value="PENDING">PENDING</option>
          <option value="APPROVED">APPROVED</option>
          <option value="REJECTED">REJECTED</option>
        </Form.Select>
      </div>
      <Feedback notice={notice} error={actionError} />
      {products.loading ? <LoadingState label="Loading products" /> : null}
      {products.error ? <ErrorState title="Product load failed" message={products.error.message} onRetry={products.reload} /> : null}
      {!products.loading && !products.error && rows.length === 0 ? <EmptyState title="No products" /> : null}
      {!products.loading && !products.error && rows.length > 0 ? (
        <AdminTable>
          <thead>
            <tr>
              <th>Product</th>
              <th>Seller</th>
              <th>Price</th>
              <th>Status</th>
              <th>Approval</th>
              <th className="text-end">Actions</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((product) => {
              const productId = getId(product, ['id', 'productId']);
              const normalizedApproval = String(product.approvalStatus || 'APPROVED').toUpperCase();
              const normalizedStatus = String(product.status || '').toUpperCase();

              return (
                <tr key={productId}>
                  <td>{product.title || '-'}</td>
                  <td>
                    <div>{product.sellerNickname || '-'}</div>
                    <small className="text-muted">#{product.sellerId || '-'}</small>
                  </td>
                  <td>
                    <MoneyText amount={product.price} />
                  </td>
                  <td>
                    <StatusBadge status={product.status} />
                  </td>
                  <td>
                    <Badge bg={normalizedApproval === 'REJECTED' ? 'danger' : normalizedApproval === 'PENDING' ? 'warning' : 'success'}>
                      {normalizedApproval}
                    </Badge>
                  </td>
                  <td className="text-end admin-product-actions">
                    <ActionButton icon={Search} size="sm" variant="outline-secondary" onClick={() => setSelectedProduct(product)}>
                      Detail
                    </ActionButton>
                    <ActionButton
                      icon={CheckCircle2}
                      size="sm"
                      variant="outline-primary"
                      disabled={normalizedApproval === 'APPROVED'}
                      onClick={() => handleApprove(product)}
                    >
                      Approve
                    </ActionButton>
                    <ActionButton
                      icon={EyeOff}
                      size="sm"
                      variant="outline-danger"
                      disabled={normalizedStatus === 'HIDDEN'}
                      onClick={() => handleHide(product)}
                    >
                      Hide
                    </ActionButton>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </AdminTable>
      ) : null}
      <Modal show={Boolean(selectedProduct)} onHide={() => setSelectedProduct(null)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Product approval detail</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedProduct ? (
            <dl className="compact-list compact-list-inline admin-product-detail">
              <div>
                <dt>Product</dt>
                <dd>{selectedProduct.title || '-'}</dd>
              </div>
              <div>
                <dt>Seller</dt>
                <dd>{selectedProduct.sellerNickname || selectedProduct.sellerId || '-'}</dd>
              </div>
              <div>
                <dt>Price</dt>
                <dd>
                  <MoneyText amount={selectedProduct.price} />
                </dd>
              </div>
              <div>
                <dt>Status</dt>
                <dd>{selectedProduct.statusLabel || selectedProduct.status || '-'}</dd>
              </div>
              <div>
                <dt>Approval</dt>
                <dd>{selectedProduct.approvalStatus || 'APPROVED'}</dd>
              </div>
            </dl>
          ) : null}
        </Modal.Body>
        <Modal.Footer>
          {selectedProduct ? (
            <ActionButton
              icon={CheckCircle2}
              variant="primary"
              disabled={String(selectedProduct.approvalStatus || 'APPROVED').toUpperCase() === 'APPROVED'}
              onClick={() => handleApprove(selectedProduct)}
            >
              Approve
            </ActionButton>
          ) : null}
          <Button variant="outline-secondary" onClick={() => setSelectedProduct(null)}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </section>
  );
}

export function AdminUsersPage() {
  const users = useAdminResource(() => getAdminUsers({ page: 0, size: PAGE_SIZE }), []);
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(users.data);

  const handleStatus = (user, status) => {
    const userId = getId(user, ['id', 'userId']);
    run(() => updateAdminUserStatus(userId, status), '회원 상태를 변경했습니다.', (updated) => {
      users.setData((current) => replaceInPayload(current, updated, 'id'));
    });
  };

  return (
    <section>
      <AdminPageHeader title="회원 관리" />
      <Feedback notice={notice} error={actionError} />
      {users.loading ? <LoadingState label="회원을 불러오는 중" /> : null}
      {users.error ? (
        <ErrorState
          title="회원 조회 실패"
          message={users.error.message}
          onRetry={users.reload}
        />
      ) : null}
      {!users.loading && !users.error && rows.length === 0 ? (
        <EmptyState title="회원이 없습니다" />
      ) : null}
      {!users.loading && !users.error && rows.length > 0 ? (
        <AdminTable>
          <thead>
            <tr>
              <th>회원</th>
              <th>이메일</th>
              <th>권한</th>
              <th>상태</th>
              <th>변경</th>
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
    return <EmptyState title="조건에 맞는 신고 내역이 없습니다" />;
  }

  return (
    <AdminTable>
      <thead>
        <tr>
          <th>신고</th>
          <th>신고자</th>
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
          const targetTitle = type === 'product'
            ? report.productTitle || `상품 #${report.productId}`
            : report.reportedUserNickname || `회원 #${report.reportedUserId}`;
          const targetSubText = type === 'product'
            ? `신고 대상 ${report.reportedUserNickname || report.reportedUserEmail || report.reportedUserId || '-'}`
            : report.reportedUserEmail || `회원 ID ${report.reportedUserId || '-'}`;

          return (
            <tr key={memoKey}>
              <td>#{reportId}</td>
              <td>
                <div>{report.reporterNickname || `회원 #${report.reporterId}`}</div>
                <small className="text-muted">{report.reporterEmail || '-'}</small>
              </td>
              <td>
                <div>{targetTitle}</div>
                <small className="text-muted">{targetSubText}</small>
              </td>
              <td>{report.reason || '-'}</td>
              <td>
                <Badge bg={String(report.status).toUpperCase() === 'PENDING' ? 'warning' : 'success'}>
                  {getReportStatusLabel(report.status)}
                </Badge>
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
  const [reportStatus, setReportStatus] = useState('PENDING');
  const [reportSearch, setReportSearch] = useState('');
  const reportParams = { status: reportStatus, search: reportSearch };
  const userReports = useAdminResource(() => getAdminUserReports(reportParams), [reportStatus, reportSearch], canReadUserReports);
  const productReports = useAdminResource(() => getAdminProductReports(reportParams), [reportStatus, reportSearch], canReadProductReports);
  const [memos, setMemos] = useState({});
  const { notice, actionError, run } = useActionFeedback();
  const filteredUserReports = getList(userReports.data).filter((report) => reportMatchesSearch(report, reportSearch));
  const filteredProductReports = getList(productReports.data).filter((report) => reportMatchesSearch(report, reportSearch));

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
      <div className="toolbar-panel mb-3 admin-report-toolbar">
        <ButtonGroup aria-label="신고 처리 상태">
          {REPORT_STATUS_FILTERS.map((item) => (
            <Button
              key={item.key}
              type="button"
              variant={reportStatus === item.key ? 'primary' : 'outline-primary'}
              onClick={() => setReportStatus(item.key)}
            >
              {item.label}
            </Button>
          ))}
        </ButtonGroup>
        <Form.Control
          type="search"
          value={reportSearch}
          placeholder="신고자, 대상, 상품명 검색"
          aria-label="신고 검색"
          onChange={(event) => setReportSearch(event.target.value)}
        />
      </div>
      <Feedback notice={notice} error={actionError} />
      <Tabs defaultActiveKey={tabs[0].key} className="mb-3">
        {canReadUserReports ? (
          <Tab eventKey="users" title="회원 신고">
            <ReportTable
              rows={filteredUserReports}
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
              rows={filteredProductReports}
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
          <th>검증 결과</th>
          <th>정산</th>
          <th>요청일</th>
          <th className="text-end">작업</th>
        </tr>
      </thead>
      <tbody>
        {rows.map((payment) => {
          const paymentId = getId(payment, ['paymentId', 'id']);
          const settlementId = payment.settlementId;
          const verification = paymentVerificationSummary(payment);

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
              <td>
                <Badge bg={verification.variant} className="verification-result">
                  {verification.label}
                </Badge>
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
  const [verifiedPayment, setVerifiedPayment] = useState(null);
  const payments = useAdminResource(() => getAdminPayments({ status, page: 0, size: PAGE_SIZE }), [status]);
  const refunds = useAdminResource(getAdminRefunds, []);
  const { notice, actionError, run } = useActionFeedback();

  const handleVerify = (paymentId) => {
    run(() => verifyAdminPayment(paymentId), '결제를 검증했습니다.', (updated) => {
      payments.setData((current) => replaceInPayload(current, updated, 'paymentId'));
      setVerifiedPayment(updated);
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
      {verifiedPayment ? (
        <Alert variant={paymentVerificationSummary(verifiedPayment).variant} className="verification-result">
          결제 #{verifiedPayment.paymentId} {paymentVerificationSummary(verifiedPayment).label}
        </Alert>
      ) : null}
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
    totalQuantity: '1',
    discountAmount: '',
    minOrderAmount: '0',
    validDays: '30'
  };
}

function formatCouponWon(value) {
  const formatted = formatAdminCouponMoneyInput(value);
  return formatted ? `${formatted}원` : '-';
}

function formatCouponQuantity(value) {
  const formatted = formatAdminCouponMoneyInput(value);
  return formatted ? `${formatted}개` : '-';
}

function CouponStatusBadge({ event }) {
  return (
    <Badge bg={getAdminCouponStatusVariant(event)}>
      {formatAdminCouponStatus(event?.status, event?.statusLabel)}
    </Badge>
  );
}

export function AdminAccountsPage() {
  const accounts = useAdminResource(getAdminAccounts, []);
  const me = useAdminResource(getAdminMe, []);
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(accounts.data);
  const currentAdminId = getId(me.data, ['id', 'adminId']);

  const handleRole = (account, role) => {
    const adminId = getId(account, ['id', 'adminId']);
    run(
      () => requestAdminRoleChangeApproval({
        targetAdminId: adminId,
        requestedRole: role,
        reason: `Role change requested from ${account.role || 'UNKNOWN'} to ${role}`
      }),
      '관리자 권한 변경 승인 요청을 생성했습니다.'
    );
  };

  return (
    <section>
      <AdminPageHeader title="관리자 계정 관리" />
      <Feedback notice={notice} error={actionError} />
      {accounts.loading || me.loading ? <LoadingState label="관리자 계정을 불러오는 중" /> : null}
      {accounts.error || me.error ? (
        <ErrorState
          title="관리자 계정 조회 실패"
          message={(accounts.error || me.error).message}
          onRetry={() => {
            accounts.reload();
            me.reload();
          }}
        />
      ) : null}
      {!accounts.loading && !me.loading && !accounts.error && !me.error && rows.length === 0 ? (
        <EmptyState title="관리자 계정이 없습니다" />
      ) : null}
      {!accounts.loading && !me.loading && !accounts.error && !me.error && rows.length > 0 ? (
        <AdminTable>
          <thead>
            <tr>
              <th>관리자</th>
              <th>이메일</th>
              <th>현재 권한</th>
              <th>상태</th>
              <th>권한 변경</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((account) => {
              const adminId = getId(account, ['id', 'adminId']);
              const isSelf = currentAdminId !== undefined && currentAdminId === adminId;

              return (
                <tr key={adminId}>
                  <td>
                    {account.nickname || '-'}
                    {isSelf ? <Badge bg="light" text="dark" className="ms-2">내 계정</Badge> : null}
                  </td>
                  <td>{account.email || '-'}</td>
                  <td>{formatAdminRole(account.role)}</td>
                  <td>
                    <StatusBadge status={account.status} />
                  </td>
                  <td>
                    <Form.Select
                      size="sm"
                      value={account.role || ''}
                      aria-label="관리자 권한 변경"
                      disabled={isSelf}
                      onChange={(event) => handleRole(account, event.target.value)}
                    >
                      {ADMIN_ROLES.map((role) => (
                        <option value={role} key={role}>
                          {ADMIN_ROLE_LABELS[role]}
                        </option>
                      ))}
                    </Form.Select>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </AdminTable>
      ) : null}
    </section>
  );
}

function AdminCouponMetric({ label, value }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

export function AdminCouponsPage() {
  const events = useAdminResource(getAdminCouponEvents, []);
  const [form, setForm] = useState(defaultCouponForm);
  const [formErrors, setFormErrors] = useState({});
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [coupons, setCoupons] = useState([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [userIds, setUserIds] = useState('');
  const [issueInputError, setIssueInputError] = useState('');
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(events.data);
  const issueTargets = parseUserIdTokens(userIds);
  const selectedCanIssue = canAdminIssueCoupon(detail);

  const updateForm = (key, value) => {
    setForm((current) => ({ ...current, [key]: value }));
    setFormErrors((current) => {
      const next = { ...current };
      delete next[key];
      return next;
    });
  };

  const updateNumberForm = (key, value) => updateForm(key, formatAdminCouponMoneyInput(value));

  const loadEventDetail = async (eventId) => {
    setSelectedEventId(eventId);
    setDetailLoading(true);
    setDetailError('');
    setIssueInputError('');

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
      setDetailError(error.message || '쿠폰 상세 조회에 실패했습니다.');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleCreate = (event) => {
    event.preventDefault();
    const errors = validateAdminCouponForm(form);
    setFormErrors(errors);

    if (Object.keys(errors).length > 0) {
      return;
    }

    const payload = buildAdminCouponCreatePayload(form);

    run(() => createCouponEvent(payload), '쿠폰 이벤트를 만들었습니다.', (created) => {
      events.setData((current) => [created, ...getList(current)]);
      setForm(defaultCouponForm());
      setFormErrors({});
      setSelectedEventId(created.eventId);
      loadEventDetail(created.eventId);
    });
  };

  const handleIssue = (event) => {
    event.preventDefault();
    const { validIds, invalidTokens } = parseUserIdTokens(userIds);

    if (!selectedEventId || validIds.length === 0 || invalidTokens.length > 0 || !selectedCanIssue) {
      setIssueInputError('발급할 회원 ID를 확인해 주세요.');
      return;
    }

    setIssueInputError('');
    run(() => issueCouponEventToUsers(selectedEventId, validIds), '쿠폰을 발급했습니다.', () => {
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
          <Form className="form-card p-3" onSubmit={handleCreate} noValidate>
            <h2 className="section-title">이벤트 생성</h2>
            <Form.Group className="mb-2" controlId="coupon-type">
              <Form.Label>유형</Form.Label>
              <Form.Select value={form.type} onChange={(event) => updateForm('type', event.target.value)}>
                {COUPON_TYPES.map((type) => (
                  <option value={type} key={type}>
                    {formatAdminCouponType(type)}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-2" controlId="coupon-name">
              <Form.Label>쿠폰명</Form.Label>
              <Form.Control
                value={form.name}
                onChange={(event) => updateForm('name', event.target.value)}
                isInvalid={Boolean(formErrors.name)}
              />
              <Form.Control.Feedback type="invalid">{formErrors.name}</Form.Control.Feedback>
            </Form.Group>
            <Row className="g-2">
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-start">
                  <Form.Label>시작 일시</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={form.startAt}
                    onChange={(event) => updateForm('startAt', event.target.value)}
                    isInvalid={Boolean(formErrors.startAt)}
                  />
                  <Form.Control.Feedback type="invalid">{formErrors.startAt}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-end">
                  <Form.Label>종료 일시</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={form.endAt}
                    onChange={(event) => updateForm('endAt', event.target.value)}
                    isInvalid={Boolean(formErrors.endAt)}
                  />
                  <Form.Control.Feedback type="invalid">{formErrors.endAt}</Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>
            <Row className="g-2">
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-quantity">
                  <Form.Label>발급 수량</Form.Label>
                  <InputGroup hasValidation>
                    <Form.Control
                      type="text"
                      inputMode="numeric"
                      value={form.totalQuantity}
                      onChange={(event) => updateNumberForm('totalQuantity', event.target.value)}
                      isInvalid={Boolean(formErrors.totalQuantity)}
                    />
                    <InputGroup.Text>개</InputGroup.Text>
                    <Form.Control.Feedback type="invalid">{formErrors.totalQuantity}</Form.Control.Feedback>
                  </InputGroup>
                </Form.Group>
              </Col>
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-valid-days">
                  <Form.Label>유효 기간</Form.Label>
                  <InputGroup hasValidation>
                    <Form.Control
                      type="text"
                      inputMode="numeric"
                      value={form.validDays}
                      onChange={(event) => updateNumberForm('validDays', event.target.value)}
                      isInvalid={Boolean(formErrors.validDays)}
                    />
                    <InputGroup.Text>일</InputGroup.Text>
                    <Form.Control.Feedback type="invalid">{formErrors.validDays}</Form.Control.Feedback>
                  </InputGroup>
                </Form.Group>
              </Col>
            </Row>
            <Row className="g-2">
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-discount">
                  <Form.Label>할인 금액</Form.Label>
                  <InputGroup hasValidation>
                    <Form.Control
                      type="text"
                      inputMode="numeric"
                      value={form.discountAmount}
                      placeholder="5,000"
                      onChange={(event) => updateNumberForm('discountAmount', event.target.value)}
                      isInvalid={Boolean(formErrors.discountAmount)}
                    />
                    <InputGroup.Text>원</InputGroup.Text>
                    <Form.Control.Feedback type="invalid">{formErrors.discountAmount}</Form.Control.Feedback>
                  </InputGroup>
                </Form.Group>
              </Col>
              <Col sm={6}>
                <Form.Group className="mb-2" controlId="coupon-min-order">
                  <Form.Label>최소 주문 금액</Form.Label>
                  <InputGroup hasValidation>
                    <Form.Control
                      type="text"
                      inputMode="numeric"
                      value={form.minOrderAmount}
                      onChange={(event) => updateNumberForm('minOrderAmount', event.target.value)}
                      isInvalid={Boolean(formErrors.minOrderAmount)}
                    />
                    <InputGroup.Text>원</InputGroup.Text>
                    <Form.Control.Feedback type="invalid">{formErrors.minOrderAmount}</Form.Control.Feedback>
                  </InputGroup>
                </Form.Group>
              </Col>
            </Row>
            <Form.Text className="text-muted d-block mb-3">금액과 수량은 쉼표를 포함해 입력할 수 있으며 API에는 숫자로 전송됩니다.</Form.Text>
            <ActionButton icon={CheckCircle2} type="submit" variant="primary" className="w-100">
              생성
            </ActionButton>
          </Form>
        </Col>
        <Col lg={7}>
          <h2 className="section-title">이벤트 목록</h2>
          {events.loading ? <LoadingState label="쿠폰 이벤트를 불러오는 중" /> : null}
          {events.error ? <ErrorState title="쿠폰 조회 실패" message={events.error.message} onRetry={events.reload} /> : null}
          {!events.loading && !events.error && rows.length === 0 ? <EmptyState title="쿠폰 이벤트가 없습니다" /> : null}
          {!events.loading && !events.error && rows.length > 0 ? (
            <div className="stack-list">
              {rows.map((couponEvent) => {
                const issuePercent = Number(couponEvent.issueRate) * 100;
                const progressValue = Number.isFinite(issuePercent) ? issuePercent : 0;

                return (
                  <div className="list-card p-3 admin-coupon-event-row" key={couponEvent.eventId}>
                    <div className="admin-coupon-event-main">
                      <div className="d-flex flex-wrap gap-2 align-items-center mb-2">
                        <h2>{couponEvent.name}</h2>
                        <Badge bg="info">{formatAdminCouponType(couponEvent.type, couponEvent.typeLabel)}</Badge>
                        <CouponStatusBadge event={couponEvent} />
                        {couponEvent.soldOut ? <Badge bg="secondary">소진</Badge> : null}
                        {couponEvent.ended ? <Badge bg="dark">종료됨</Badge> : null}
                        {couponEvent.canIssue === false ? <Badge bg="warning">발급 제한</Badge> : null}
                      </div>
                      <p className="mb-1 text-muted">
                        {formatDateTime(couponEvent.startAt)} - {formatDateTime(couponEvent.endAt)}
                      </p>
                      <div className="admin-coupon-event-meta">
                        <span>할인 {formatCouponWon(couponEvent.discountAmount)}</span>
                        <span>최소 주문 {formatCouponWon(couponEvent.minOrderAmount)}</span>
                        <span>
                          남은 수량 {formatCouponQuantity(couponEvent.remainingQuantity)} / 전체 {formatCouponQuantity(couponEvent.totalQuantity)}
                        </span>
                        <span>발급률 {formatIssueRate(couponEvent.issueRate)}</span>
                      </div>
                      <ProgressBar now={progressValue} min={0} max={100} className="admin-coupon-progress mt-2" />
                    </div>
                    <ActionButton icon={Search} size="sm" variant="outline-primary" onClick={() => loadEventDetail(couponEvent.eventId)}>
                      상세
                    </ActionButton>
                  </div>
                );
              })}
            </div>
          ) : null}
        </Col>
      </Row>
      {selectedEventId ? (
        <div className="detail-panel mt-4 admin-coupon-detail">
          <div className="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
            <h2 className="section-title mb-0">발급 관리</h2>
            {detail ? <CouponStatusBadge event={detail} /> : null}
          </div>
          {detailLoading ? <LoadingState label="발급 내역을 불러오는 중" /> : null}
          {detailError ? (
            <Alert variant="danger" className="py-2">
              {detailError}
            </Alert>
          ) : null}
          {!detailLoading ? (
            <>
              <p className="fw-bold mb-2">{detail?.name || `이벤트 #${selectedEventId}`}</p>
              {detail ? (
                <dl className="compact-list compact-list-inline admin-coupon-summary">
                  <AdminCouponMetric label="유형" value={formatAdminCouponType(detail.type, detail.typeLabel)} />
                  <AdminCouponMetric label="기간" value={`${formatDateTime(detail.startAt)} - ${formatDateTime(detail.endAt)}`} />
                  <AdminCouponMetric label="할인" value={formatCouponWon(detail.discountAmount)} />
                  <AdminCouponMetric label="최소 주문" value={formatCouponWon(detail.minOrderAmount)} />
                  <AdminCouponMetric label="전체 수량" value={formatCouponQuantity(detail.totalQuantity)} />
                  <AdminCouponMetric label="발급 수량" value={formatCouponQuantity(detail.issuedQuantity)} />
                  <AdminCouponMetric label="남은 수량" value={formatCouponQuantity(detail.remainingQuantity)} />
                  <AdminCouponMetric label="발급률" value={formatIssueRate(detail.issueRate)} />
                  <AdminCouponMetric label="유효 기간" value={`${detail.validDays ?? '-'}일`} />
                </dl>
              ) : null}
              {!selectedCanIssue ? (
                <Alert variant="warning" className="py-2">
                  종료되었거나 소진된 이벤트라 추가 발급할 수 없습니다.
                </Alert>
              ) : null}
              <Form className="admin-coupon-issue-form mb-3" onSubmit={handleIssue} noValidate>
                <Form.Group className="flex-grow-1" controlId="coupon-user-ids">
                  <Form.Label>발급 대상 회원 ID</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    value={userIds}
                    placeholder="1, 2, 3 또는 줄바꿈으로 입력"
                    onChange={(event) => {
                      setUserIds(event.target.value);
                      setIssueInputError('');
                    }}
                    isInvalid={Boolean(issueInputError || issueTargets.invalidTokens.length)}
                  />
                  <Form.Control.Feedback type="invalid">
                    {issueInputError || '숫자인 회원 ID만 입력해 주세요.'}
                  </Form.Control.Feedback>
                  <Form.Text className="text-muted">쉼표, 공백, 줄바꿈으로 여러 회원 ID를 입력할 수 있습니다.</Form.Text>
                </Form.Group>
                <ActionButton
                  icon={Send}
                  type="submit"
                  variant="primary"
                  disabled={!selectedEventId || issueTargets.validIds.length === 0 || issueTargets.invalidTokens.length > 0 || !selectedCanIssue}
                >
                  발급
                </ActionButton>
              </Form>
              {userIds ? (
                <div className="coupon-target-chip-list mb-3">
                  {issueTargets.validIds.map((id) => (
                    <Badge bg="light" text="dark" className="coupon-target-chip" key={id}>
                      #{id}
                    </Badge>
                  ))}
                  {issueTargets.invalidTokens.map((token, index) => (
                    <Badge bg="danger" className="coupon-target-chip coupon-target-chip-invalid" key={getCouponTargetChipKey(token, index)}>
                      {token}
                    </Badge>
                  ))}
                </div>
              ) : null}
              {coupons.length === 0 ? <EmptyState title="발급된 쿠폰이 없습니다" /> : null}
              {coupons.length > 0 ? (
                <AdminTable>
                  <thead>
                    <tr>
                      <th>쿠폰</th>
                      <th>회원</th>
                      <th>이벤트</th>
                      <th>할인</th>
                      <th>최소 주문</th>
                      <th>상태</th>
                      <th>발급일</th>
                      <th>만료일</th>
                    </tr>
                  </thead>
                  <tbody>
                    {coupons.map((coupon) => (
                      <tr key={coupon.couponId}>
                        <td>#{coupon.couponId}</td>
                        <td>
                          <div>{coupon.userNickname || coupon.userId || '-'}</div>
                          <small className="text-muted">{coupon.userEmail || '-'}</small>
                        </td>
                        <td>{coupon.eventName || detail?.name || '-'}</td>
                        <td>{formatCouponWon(coupon.discountAmount ?? detail?.discountAmount)}</td>
                        <td>{formatCouponWon(coupon.minOrderAmount ?? detail?.minOrderAmount)}</td>
                        <td>
                          <Badge bg={getAdminCouponStatusVariant(coupon)}>
                            {formatAdminCouponStatus(coupon.status, coupon.statusLabel)}
                          </Badge>
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
function ApprovalStatusBadge({ status }) {
  const normalized = String(status || 'PENDING').toUpperCase();
  const variant = normalized === 'APPROVED' ? 'success' : normalized === 'REJECTED' ? 'danger' : 'warning';

  return <Badge bg={variant}>{normalized}</Badge>;
}

function ApprovalRequestTable({ rows, memos = {}, setMemo, onApprove, onReject, showActions = false }) {
  if (rows.length === 0) {
    return <EmptyState title="No approval requests" />;
  }

  return (
    <AdminTable>
      <thead>
        <tr>
          <th>Request</th>
          <th>Requester</th>
          <th>Target</th>
          <th>Requested role</th>
          <th>Status</th>
          {showActions ? <th>Decision memo</th> : null}
          {showActions ? <th className="text-end">Actions</th> : null}
        </tr>
      </thead>
      <tbody>
        {rows.map((request) => {
          const requestId = getId(request, ['id', 'requestId']);
          const memo = memos[requestId] || '';
          const pending = String(request.status || '').toUpperCase() === 'PENDING';

          return (
            <tr key={requestId}>
              <td>
                <div>#{requestId} {request.operation || 'ADMIN_ROLE_CHANGE'}</div>
                <small className="text-muted">{request.reason || '-'}</small>
              </td>
              <td>
                <div>{request.requesterNickname || request.requesterId || '-'}</div>
                <small className="text-muted">{request.requesterEmail || '-'}</small>
              </td>
              <td>
                <div>{request.targetAdminNickname || request.targetAdminId || '-'}</div>
                <small className="text-muted">{request.targetAdminEmail || '-'}</small>
              </td>
              <td>{formatAdminRole(request.requestedRole)}</td>
              <td>
                <ApprovalStatusBadge status={request.status} />
              </td>
              {showActions ? (
                <td>
                  <Form.Control
                    size="sm"
                    value={memo}
                    aria-label="Approval decision memo"
                    onChange={(event) => setMemo(requestId, event.target.value)}
                  />
                </td>
              ) : null}
              {showActions ? (
                <td className="text-end admin-approval-actions">
                  <ActionButton
                    icon={CheckCircle2}
                    size="sm"
                    variant="outline-primary"
                    disabled={!pending}
                    onClick={() => onApprove(requestId, memo)}
                  >
                    Approve
                  </ActionButton>
                  <ActionButton
                    icon={EyeOff}
                    size="sm"
                    variant="outline-danger"
                    disabled={!pending}
                    onClick={() => onReject(requestId, memo)}
                  >
                    Reject
                  </ActionButton>
                </td>
              ) : null}
            </tr>
          );
        })}
      </tbody>
    </AdminTable>
  );
}

export function AdminApprovalsPage() {
  const [status, setStatus] = useState('PENDING');
  const approvals = useAdminResource(() => getAdminApprovalRequests({ status }), [status]);
  const [memos, setMemos] = useState({});
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(approvals.data);

  const setMemo = (requestId, value) => setMemos((current) => ({ ...current, [requestId]: value }));
  const removeRequest = (requestId) => {
    approvals.setData((current) => removeFromPayload(current, requestId, 'id'));
    setMemos((current) => ({ ...current, [requestId]: '' }));
  };

  const handleApprove = (requestId, memo) => {
    run(() => approveAdminApprovalRequest(requestId, memo), 'Approval request has been approved.', () => removeRequest(requestId));
  };

  const handleReject = (requestId, memo) => {
    run(() => rejectAdminApprovalRequest(requestId, memo), 'Approval request has been rejected.', () => removeRequest(requestId));
  };

  return (
    <section>
      <AdminPageHeader title="Approval management" />
      <div className="toolbar-panel mb-3 admin-approval-toolbar">
        <ButtonGroup aria-label="Approval request status">
          {['PENDING', 'APPROVED', 'REJECTED'].map((item) => (
            <Button
              key={item}
              type="button"
              variant={status === item ? 'primary' : 'outline-primary'}
              onClick={() => setStatus(item)}
            >
              {item}
            </Button>
          ))}
        </ButtonGroup>
      </div>
      <Feedback notice={notice} error={actionError} />
      {approvals.loading ? <LoadingState label="Loading approval requests" /> : null}
      {approvals.error ? <ErrorState title="Approval request load failed" message={approvals.error.message} onRetry={approvals.reload} /> : null}
      {!approvals.loading && !approvals.error ? (
        <ApprovalRequestTable
          rows={rows}
          memos={memos}
          setMemo={setMemo}
          onApprove={handleApprove}
          onReject={handleReject}
          showActions={status === 'PENDING'}
        />
      ) : null}
    </section>
  );
}

export function AdminMyApprovalRequestsPage() {
  const approvals = useAdminResource(getMyAdminApprovalRequests, []);
  const [form, setForm] = useState({ targetAdminId: '', requestedRole: 'PRODUCT_ADMIN', reason: '' });
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(approvals.data);

  const updateForm = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const handleSubmit = (event) => {
    event.preventDefault();
    const payload = {
      targetAdminId: Number(form.targetAdminId),
      requestedRole: form.requestedRole,
      reason: form.reason.trim()
    };

    run(() => requestAdminRoleChangeApproval(payload), 'Approval request has been created.', (created) => {
      approvals.setData((current) => [created, ...getList(current)]);
      setForm({ targetAdminId: '', requestedRole: 'PRODUCT_ADMIN', reason: '' });
    });
  };

  return (
    <section>
      <AdminPageHeader title="My approval requests" />
      <Feedback notice={notice} error={actionError} />
      <Form className="form-card p-3 mb-4 admin-approval-request-form" onSubmit={handleSubmit} noValidate>
        <h2 className="section-title">Request admin role change</h2>
        <Row className="g-2">
          <Col md={3}>
            <Form.Group controlId="approval-target-admin">
              <Form.Label>Target admin ID</Form.Label>
              <Form.Control
                type="number"
                min="1"
                value={form.targetAdminId}
                onChange={(event) => updateForm('targetAdminId', event.target.value)}
                required
              />
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group controlId="approval-requested-role">
              <Form.Label>Requested role</Form.Label>
              <Form.Select value={form.requestedRole} onChange={(event) => updateForm('requestedRole', event.target.value)}>
                {ADMIN_ROLES.map((role) => (
                  <option value={role} key={role}>
                    {ADMIN_ROLE_LABELS[role]}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={5}>
            <Form.Group controlId="approval-reason">
              <Form.Label>Reason</Form.Label>
              <Form.Control value={form.reason} onChange={(event) => updateForm('reason', event.target.value)} required />
            </Form.Group>
          </Col>
        </Row>
        <ActionButton
          icon={Send}
          type="submit"
          variant="primary"
          className="mt-3"
          disabled={!form.targetAdminId || !form.reason.trim()}
        >
          Request approval
        </ActionButton>
      </Form>
      {approvals.loading ? <LoadingState label="Loading my approval requests" /> : null}
      {approvals.error ? <ErrorState title="My approval request load failed" message={approvals.error.message} onRetry={approvals.reload} /> : null}
      {!approvals.loading && !approvals.error ? <ApprovalRequestTable rows={rows} /> : null}
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
