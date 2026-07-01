import { useCallback, useEffect, useRef, useState } from 'react';
import { Alert, Badge, Button, ButtonGroup, Card, Col, Form, InputGroup, Modal, ProgressBar, Row, Tab, Table, Tabs } from 'react-bootstrap';
import { CheckCircle2, EyeOff, RefreshCw, Search, Send } from 'lucide-react';
import {
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
  requestCouponEventCreateApproval,
  requestCouponEventIndividualIssue,
  requestCouponEventStopApproval,
  rejectAdminApprovalRequest,
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
const PRODUCT_STATUSES = ['', 'SELLING', 'RESERVED', 'SOLD', 'HIDDEN'];
const ADMIN_ROLES = ['ROOT_ADMIN', 'USER_ADMIN', 'PRODUCT_ADMIN', 'SETTLEMENT_ADMIN'];
const PAYMENT_STATUSES = ['', 'READY', 'CONFIRMING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED'];
const COUPON_TYPES = ['FIRST_COME', 'NEW_SIGNUP', 'ADMIN_INDIVIDUAL'];

const APPROVAL_FILTER_STATUSES = ['PENDING', 'APPROVED', 'REJECTED'];

const includesKeyword = (value, keyword) => {
  const normalizedKeyword = String(keyword || '').trim().toLowerCase();

  if (!normalizedKeyword) {
    return true;
  }

  return String(value ?? '').toLowerCase().includes(normalizedKeyword);
};

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
      setNotice(typeof successMessage === 'function' ? successMessage(result) : successMessage);
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
  const defaultProductFilters = {
    keyword: '',
    sellerKeyword: '',
    status: '',
    approvalStatus: '',
    reportedOnly: false
  };
  const [productFilters, setProductFilters] = useState(defaultProductFilters);
  const [appliedProductFilters, setAppliedProductFilters] = useState(defaultProductFilters);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const products = useAdminResource(
    () => getAdminProducts({ ...appliedProductFilters, page: 0, size: PAGE_SIZE }),
    [
      appliedProductFilters.keyword,
      appliedProductFilters.sellerKeyword,
      appliedProductFilters.status,
      appliedProductFilters.approvalStatus,
      appliedProductFilters.reportedOnly
    ]
  );
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(products.data);

  const updateProductFilter = (key, value) => {
    setProductFilters((current) => ({ ...current, [key]: value }));
  };

  const handleProductSearch = (event) => {
    event.preventDefault();
    setAppliedProductFilters({
      keyword: productFilters.keyword.trim(),
      sellerKeyword: productFilters.sellerKeyword.trim(),
      status: productFilters.status,
      approvalStatus: productFilters.approvalStatus,
      reportedOnly: productFilters.reportedOnly
    });
  };

  const handleProductReset = () => {
    setProductFilters(defaultProductFilters);
    setAppliedProductFilters(defaultProductFilters);
  };

  const appliedProductFilterChips = [
    appliedProductFilters.keyword ? `상품명: ${appliedProductFilters.keyword}` : '',
    appliedProductFilters.sellerKeyword ? `판매자: ${appliedProductFilters.sellerKeyword}` : '',
    appliedProductFilters.status ? `판매상태: ${statusText(appliedProductFilters.status)}` : '',
    appliedProductFilters.approvalStatus ? `승인상태: ${appliedProductFilters.approvalStatus}` : '',
    appliedProductFilters.reportedOnly ? '신고 상품만' : ''
  ].filter(Boolean);

  const updateProductRow = (updated) => {
    products.setData((current) => replaceInPayload(current, updated, 'id'));
    setSelectedProduct(updated);
  };

  const handleHide = (product) => {
    const productId = getId(product, ['id', 'productId']);
    run(() => hideAdminProduct(productId), '상품을 숨김 처리했습니다.', updateProductRow);
  };

  const handleApprove = (product) => {
    const productId = getId(product, ['id', 'productId']);
    run(() => approveAdminProduct(productId), '상품을 승인했습니다.', (updated) => {
      updateProductRow(updated);
      setSelectedProduct(null);
    });
  };

  return (
    <section>
      <AdminPageHeader title="상품 관리" />
      <Form className="toolbar-panel mb-3 admin-search-panel admin-product-toolbar" onSubmit={handleProductSearch}>
        <Form.Group className="admin-search-field" controlId="admin-product-keyword">
            <Form.Label>상품명</Form.Label>
            <Form.Control
              value={productFilters.keyword}
              placeholder="상품명 검색"
              onChange={(event) => updateProductFilter('keyword', event.target.value)}
            />
          </Form.Group>
        <div className="admin-filter-options">
          <Form.Group controlId="admin-product-seller">
            <Form.Label>판매자 닉네임</Form.Label>
            <Form.Control
              value={productFilters.sellerKeyword}
              placeholder="판매자 닉네임 검색"
              onChange={(event) => updateProductFilter('sellerKeyword', event.target.value)}
            />
          </Form.Group>
          <Form.Group controlId="admin-product-status">
            <Form.Label>판매상태</Form.Label>
            <Form.Select value={productFilters.status} onChange={(event) => updateProductFilter('status', event.target.value)}>
              {PRODUCT_STATUSES.map((status) => (
                <option value={status} key={status || 'all'}>
                  {status ? statusText(status) : '전체'}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="admin-product-approval">
            <Form.Label>승인상태</Form.Label>
            <Form.Select
              value={productFilters.approvalStatus}
              onChange={(event) => updateProductFilter('approvalStatus', event.target.value)}
            >
              <option value="">전체</option>
              <option value="PENDING">PENDING</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
            </Form.Select>
          </Form.Group>
          <Form.Check
            type="switch"
            id="reported-only"
            label="신고 상품만"
            checked={productFilters.reportedOnly}
            onChange={(event) => updateProductFilter('reportedOnly', event.target.checked)}
          />
        </div>
          <div className="admin-filter-actions admin-search-actions admin-product-filter-actions">
            <ActionButton icon={Search} type="submit" variant="primary">
              검색
            </ActionButton>
            <Button type="button" variant="outline-secondary" onClick={handleProductReset}>
              초기화
            </Button>
          </div>
        {appliedProductFilterChips.length > 0 ? (
          <div className="admin-product-filter-summary" aria-label="적용된 상품 검색 조건">
            {appliedProductFilterChips.map((chip) => (
              <Badge bg="light" text="dark" key={chip}>
                {chip}
              </Badge>
            ))}
          </div>
        ) : null}
      </Form>
      <Feedback notice={notice} error={actionError} />
      {products.loading ? <LoadingState label="상품을 불러오는 중" /> : null}
      {products.error ? <ErrorState title="상품 조회 실패" message={products.error.message} onRetry={products.reload} /> : null}
      {!products.loading && !products.error && rows.length === 0 ? <EmptyState title="조건에 맞는 상품이 없습니다" /> : null}
      {!products.loading && !products.error && rows.length > 0 ? (
        <AdminTable>
          <thead>
            <tr>
              <th>상품명</th>
              <th>판매자</th>
              <th>가격</th>
              <th>판매상태</th>
              <th>승인상태</th>
              <th>신고</th>
              <th>등록일</th>
              <th className="text-end">작업</th>
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
                  <td>{product.reportCount || product.reportedCount || product.reportedOnly ? '있음' : '-'}</td>
                  <td>{formatDateTime(product.createdAt)}</td>
                  <td className="text-end admin-product-actions">
                    <ActionButton icon={Search} size="sm" variant="outline-secondary" onClick={() => setSelectedProduct(product)}>
                      상세
                    </ActionButton>
                    <ActionButton
                      icon={CheckCircle2}
                      size="sm"
                      variant="outline-primary"
                      disabled={normalizedApproval === 'APPROVED'}
                      onClick={() => handleApprove(product)}
                    >
                      승인
                    </ActionButton>
                    <ActionButton
                      icon={EyeOff}
                      size="sm"
                      variant="outline-danger"
                      disabled={normalizedStatus === 'HIDDEN'}
                      onClick={() => handleHide(product)}
                    >
                      숨김
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
          <Modal.Title>상품 승인 상세</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedProduct ? (
            <dl className="compact-list compact-list-inline admin-product-detail">
              <div>
                <dt>상품</dt>
                <dd>{selectedProduct.title || '-'}</dd>
              </div>
              <div>
                <dt>판매자</dt>
                <dd>{selectedProduct.sellerNickname || selectedProduct.sellerId || '-'}</dd>
              </div>
              <div>
                <dt>가격</dt>
                <dd>
                  <MoneyText amount={selectedProduct.price} />
                </dd>
              </div>
              <div>
                <dt>판매상태</dt>
                <dd>{selectedProduct.statusLabel || selectedProduct.status || '-'}</dd>
              </div>
              <div>
                <dt>승인상태</dt>
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
              승인
            </ActionButton>
          ) : null}
          <Button variant="outline-secondary" onClick={() => setSelectedProduct(null)}>
            닫기
          </Button>
        </Modal.Footer>
      </Modal>
    </section>
  );
}

export function AdminUsersPage() {
  const users = useAdminResource(() => getAdminUsers({ page: 0, size: PAGE_SIZE }), []);
  const { notice, actionError, run } = useActionFeedback();
  const [userFilters, setUserFilters] = useState({ keyword: '', status: '' });
  const rows = getList(users.data);
  const filteredUsers = rows.filter((user) => {
    const keywordMatch = [
      user.nickname,
      user.email,
      user.id,
      user.userId
    ].some((value) => includesKeyword(value, userFilters.keyword));
    const statusMatch = !userFilters.status || String(user.status || '').toUpperCase() === userFilters.status;

    return keywordMatch && statusMatch;
  });

  const updateUserFilter = (key, value) => {
    setUserFilters((current) => ({ ...current, [key]: value }));
  };

  const handleUserSearch = (event) => {
    event.preventDefault();
  };

  const handleUserReset = () => {
    setUserFilters({ keyword: '', status: '' });
  };

  const handleStatus = (user, status) => {
    const userId = getId(user, ['id', 'userId']);
    run(() => updateAdminUserStatus(userId, status), '회원 상태를 변경했습니다.', (updated) => {
      users.setData((current) => replaceInPayload(current, updated, 'id'));
    });
  };

  return (
    <section>
      <AdminPageHeader title="회원 관리" />
      <Form className="toolbar-panel mb-3 admin-search-panel" onSubmit={handleUserSearch}>
        <Form.Group className="admin-search-field" controlId="admin-user-keyword">
          <Form.Label>회원 검색</Form.Label>
          <Form.Control
            type="search"
            value={userFilters.keyword}
            placeholder="닉네임, 이메일, 회원 ID"
            onChange={(event) => updateUserFilter('keyword', event.target.value)}
          />
        </Form.Group>
        <div className="admin-filter-options">
          <Form.Group controlId="admin-user-status">
            <Form.Label>회원 상태</Form.Label>
            <Form.Select value={userFilters.status} onChange={(event) => updateUserFilter('status', event.target.value)}>
              <option value="">전체</option>
              {USER_STATUSES.map((status) => (
                <option value={status} key={status}>
                  {statusText(status)}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </div>
        <div className="admin-filter-actions admin-search-actions">
          <ActionButton icon={Search} type="submit" variant="primary">
            검색
          </ActionButton>
          <Button type="button" variant="outline-secondary" onClick={handleUserReset}>
            초기화
          </Button>
        </div>
      </Form>
      <Feedback notice={notice} error={actionError} />
      {users.loading ? <LoadingState label="회원을 불러오는 중" /> : null}
      {users.error ? (
        <ErrorState
          title="회원 조회 실패"
          message={users.error.message}
          onRetry={users.reload}
        />
      ) : null}
      {!users.loading && !users.error && filteredUsers.length === 0 ? (
        <EmptyState title="조건에 맞는 회원이 없습니다" />
      ) : null}
      {!users.loading && !users.error && filteredUsers.length > 0 ? (
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
            {filteredUsers.map((user) => (
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
  const [reportDraftSearch, setReportDraftSearch] = useState('');
  const reportParams = { status: reportStatus, search: reportSearch };
  const userReports = useAdminResource(() => getAdminUserReports(reportParams), [reportStatus, reportSearch], canReadUserReports);
  const productReports = useAdminResource(() => getAdminProductReports(reportParams), [reportStatus, reportSearch], canReadProductReports);
  const [memos, setMemos] = useState({});
  const { notice, actionError, run } = useActionFeedback();
  const filteredUserReports = getList(userReports.data).filter((report) => reportMatchesSearch(report, reportSearch));
  const filteredProductReports = getList(productReports.data).filter((report) => reportMatchesSearch(report, reportSearch));

  const setMemo = (key, value) => setMemos((current) => ({ ...current, [key]: value }));
  const clearMemo = (key) => setMemos((current) => ({ ...current, [key]: '' }));

  const handleReportSearch = (event) => {
    event.preventDefault();
    setReportSearch(reportDraftSearch.trim());
  };

  const handleReportReset = () => {
    setReportStatus('PENDING');
    setReportDraftSearch('');
    setReportSearch('');
  };

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
      <Form className="toolbar-panel mb-3 admin-search-panel" onSubmit={handleReportSearch}>
        <Form.Group className="admin-search-field" controlId="admin-report-keyword">
          <Form.Label>신고 검색</Form.Label>
          <Form.Control
            type="search"
            value={reportDraftSearch}
            placeholder="신고자, 신고된 회원, 상품명"
            onChange={(event) => setReportDraftSearch(event.target.value)}
          />
        </Form.Group>
        <div className="admin-filter-options">
          <Form.Group controlId="admin-report-status">
            <Form.Label>처리 상태</Form.Label>
            <Form.Select value={reportStatus} onChange={(event) => setReportStatus(event.target.value)}>
              {REPORT_STATUS_FILTERS.map((item) => (
                <option value={item.key} key={item.key}>
                  {item.label}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </div>
        <div className="admin-filter-actions admin-search-actions">
          <ActionButton icon={Search} type="submit" variant="primary">
            검색
          </ActionButton>
          <Button type="button" variant="outline-secondary" onClick={handleReportReset}>
            초기화
          </Button>
        </div>
      </Form>
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
  const [paymentFilters, setPaymentFilters] = useState({ status: '', keyword: '', settlementStatus: '' });
  const [verifiedPayment, setVerifiedPayment] = useState(null);
  const payments = useAdminResource(() => getAdminPayments({ status: paymentFilters.status, page: 0, size: PAGE_SIZE }), [paymentFilters.status]);
  const refunds = useAdminResource(getAdminRefunds, []);
  const { notice, actionError, run } = useActionFeedback();
  const paymentMatchesFilters = (payment) => {
    const keywordMatch = [
      payment.paymentId,
      payment.id,
      payment.orderId,
      payment.status,
      payment.settlementStatus
    ].some((value) => includesKeyword(value, paymentFilters.keyword));
    const settlementMatch = !paymentFilters.settlementStatus
      || String(payment.settlementStatus || '').toUpperCase() === paymentFilters.settlementStatus;

    return keywordMatch && settlementMatch;
  };
  const filteredPayments = getList(payments.data).filter(paymentMatchesFilters);
  const filteredRefunds = getList(refunds.data).filter(paymentMatchesFilters);

  const updatePaymentFilter = (key, value) => {
    setPaymentFilters((current) => ({ ...current, [key]: value }));
  };

  const handlePaymentSearch = (event) => {
    event.preventDefault();
  };

  const handlePaymentReset = () => {
    setPaymentFilters({ status: '', keyword: '', settlementStatus: '' });
    setVerifiedPayment(null);
  };

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
      <Form className="toolbar-panel mb-3 admin-search-panel" onSubmit={handlePaymentSearch}>
        <Form.Group className="admin-search-field" controlId="payment-keyword">
          <Form.Label>결제 검색</Form.Label>
          <Form.Control
            type="search"
            value={paymentFilters.keyword}
            placeholder="결제 ID, 주문번호"
            onChange={(event) => updatePaymentFilter('keyword', event.target.value)}
          />
        </Form.Group>
        <div className="admin-filter-options">
          <Form.Group controlId="payment-status">
            <Form.Label>결제 상태</Form.Label>
            <Form.Select value={paymentFilters.status} onChange={(event) => updatePaymentFilter('status', event.target.value)}>
              {PAYMENT_STATUSES.map((item) => (
                <option value={item} key={item || 'all'}>
                  {item ? statusText(item) : '전체'}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="payment-settlement-status">
            <Form.Label>정산 상태</Form.Label>
            <Form.Select
              value={paymentFilters.settlementStatus}
              onChange={(event) => updatePaymentFilter('settlementStatus', event.target.value)}
            >
              <option value="">전체</option>
              <option value="PENDING">PENDING</option>
              <option value="SETTLED">SETTLED</option>
              <option value="FAILED">FAILED</option>
            </Form.Select>
          </Form.Group>
        </div>
        <div className="admin-filter-actions admin-search-actions">
          <ActionButton icon={Search} type="submit" variant="primary">
            검색
          </ActionButton>
          <Button type="button" variant="outline-secondary" onClick={handlePaymentReset}>
            초기화
          </Button>
        </div>
      </Form>
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
        <PaymentTable rows={filteredPayments} onVerify={handleVerify} onSettle={handleSettle} showVerify />
      ) : null}
      <h2 className="section-title mt-4">환불</h2>
      {refunds.loading ? <LoadingState label="환불을 불러오는 중" /> : null}
      {refunds.error ? <ErrorState title="환불 조회 실패" message={refunds.error.message} onRetry={refunds.reload} /> : null}
      {!refunds.loading && !refunds.error ? (
        <PaymentTable rows={filteredRefunds} onVerify={handleVerify} onSettle={handleSettle} showVerify={false} />
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

function defaultCouponFilters() {
  return {
    keyword: '',
    type: '',
    status: '',
    startsFrom: '',
    endsTo: ''
  };
}

const getCouponEventId = (event) => getId(event, ['eventId', 'couponEventId', 'id']);

const getCouponApprovalPayload = (request = {}) =>
  request.couponPayload || request.payload?.couponPayload || request.payload || request.data?.couponPayload || {};

const couponApprovalToForm = (request = {}) => {
  const payload = getCouponApprovalPayload(request);

  return {
    type: payload.eventType || payload.type || defaultCouponForm().type,
    name: payload.eventName || payload.name || '',
    startAt: payload.startAt || '',
    endAt: payload.endAt || '',
    totalQuantity: formatAdminCouponMoneyInput(payload.totalQuantity ?? payload.quantity ?? ''),
    discountAmount: formatAdminCouponMoneyInput(payload.discountAmount ?? ''),
    minOrderAmount: formatAdminCouponMoneyInput(payload.minOrderAmount ?? 0),
    validDays: formatAdminCouponMoneyInput(payload.validDays ?? 30)
  };
};

const isCouponEventActive = (event = {}) => String(event?.status || '').toUpperCase() === 'ACTIVE';

function formatCouponWon(value) {
  const formatted = formatAdminCouponMoneyInput(value);
  return formatted ? `${formatted}원` : '-';
}

function formatCouponQuantity(value) {
  const formatted = formatAdminCouponMoneyInput(value);
  return formatted ? `${formatted}개` : '-';
}

const couponApprovalMessage = (label, response) => {
  const normalizedStatus = String(response?.status || '').toUpperCase();

  return normalizedStatus === 'APPROVED'
    ? `${label} 승인요청이 승인 이력으로 등록되었습니다.`
    : `${label} 승인요청이 접수되었습니다.`;
};

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
  const [accountFilters, setAccountFilters] = useState({ keyword: '', role: '', status: '' });
  const rows = getList(accounts.data);
  const currentAdminId = getId(me.data, ['id', 'adminId']);
  const filteredAccounts = rows.filter((account) => {
    const keywordMatch = [
      account.nickname,
      account.email,
      account.id,
      account.adminId
    ].some((value) => includesKeyword(value, accountFilters.keyword));
    const roleMatch = !accountFilters.role || String(account.role || '').toUpperCase() === accountFilters.role;
    const statusMatch = !accountFilters.status || String(account.status || '').toUpperCase() === accountFilters.status;

    return keywordMatch && roleMatch && statusMatch;
  });

  const updateAccountFilter = (key, value) => {
    setAccountFilters((current) => ({ ...current, [key]: value }));
  };

  const handleAccountSearch = (event) => {
    event.preventDefault();
  };

  const handleAccountReset = () => {
    setAccountFilters({ keyword: '', role: '', status: '' });
  };

  const handleRole = (account, role) => {
    const adminId = getId(account, ['id', 'adminId']);
    run(
      () => updateAdminAccountRole(adminId, role),
      '관리자 권한을 변경했습니다.',
      (updated) => {
        accounts.setData((current) => replaceInPayload(current, updated, 'id'));
      }
    );
  };

  return (
    <section>
      <AdminPageHeader title="관리자 계정 관리" />
      <Form className="toolbar-panel mb-3 admin-search-panel" onSubmit={handleAccountSearch}>
        <Form.Group className="admin-search-field" controlId="admin-account-keyword">
          <Form.Label>관리자 검색</Form.Label>
          <Form.Control
            type="search"
            value={accountFilters.keyword}
            placeholder="닉네임, 이메일, 관리자 ID"
            onChange={(event) => updateAccountFilter('keyword', event.target.value)}
          />
        </Form.Group>
        <div className="admin-filter-options">
          <Form.Group controlId="admin-account-role">
            <Form.Label>관리자 권한</Form.Label>
            <Form.Select value={accountFilters.role} onChange={(event) => updateAccountFilter('role', event.target.value)}>
              <option value="">전체</option>
              {ADMIN_ROLES.map((role) => (
                <option value={role} key={role}>
                  {ADMIN_ROLE_LABELS[role]}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="admin-account-status">
            <Form.Label>관리자 상태</Form.Label>
            <Form.Select value={accountFilters.status} onChange={(event) => updateAccountFilter('status', event.target.value)}>
              <option value="">전체</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="BLOCKED">BLOCKED</option>
            </Form.Select>
          </Form.Group>
        </div>
        <div className="admin-filter-actions admin-search-actions">
          <ActionButton icon={Search} type="submit" variant="primary">
            검색
          </ActionButton>
          <Button type="button" variant="outline-secondary" onClick={handleAccountReset}>
            초기화
          </Button>
        </div>
      </Form>
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
      {!accounts.loading && !me.loading && !accounts.error && !me.error && filteredAccounts.length === 0 ? (
        <EmptyState title="조건에 맞는 관리자 계정이 없습니다" />
      ) : null}
      {!accounts.loading && !me.loading && !accounts.error && !me.error && filteredAccounts.length > 0 ? (
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
            {filteredAccounts.map((account) => {
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

function CouponApprovalSummary({ summary }) {
  if (!summary) {
    return null;
  }

  return (
    <dl className="compact-list compact-list-inline admin-coupon-approval-summary">
      <AdminCouponMetric label="입력 수" value={summary.inputCount ?? '-'} />
      <AdminCouponMetric label="유효 대상" value={summary.validTargetCount ?? '-'} />
      <AdminCouponMetric label="중복" value={summary.duplicateCount ?? '-'} />
      <AdminCouponMetric label="제외" value={summary.excludedCount ?? '-'} />
      <AdminCouponMetric label="요청 발급" value={summary.plannedIssueCount ?? '-'} />
      <AdminCouponMetric label="예상 발급량" value={summary.expectedIssuedQuantity ?? '-'} />
      <AdminCouponMetric label="잔여 초과" value={summary.exceedsRemainingQuantity ? '예' : '아니오'} />
    </dl>
  );
}

export function AdminCouponsPage() {
  const [couponFilters, setCouponFilters] = useState(defaultCouponFilters);
  const [appliedCouponFilters, setAppliedCouponFilters] = useState(defaultCouponFilters);
  const events = useAdminResource(
    () => getAdminCouponEvents({ status: appliedCouponFilters.status }),
    [appliedCouponFilters.status]
  );
  const couponCreateApprovalRequests = useAdminResource(getMyAdminApprovalRequests, []);
  const [showCouponCreateForm, setShowCouponCreateForm] = useState(false);
  const [form, setForm] = useState(defaultCouponForm);
  const [formErrors, setFormErrors] = useState({});
  const [createCause, setCreateCause] = useState('');
  const [createCauseError, setCreateCauseError] = useState('');
  const [createFormSummary, setCreateFormSummary] = useState('새 쿠폰 이벤트 승인요청');
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [coupons, setCoupons] = useState([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [userIds, setUserIds] = useState('');
  const [issueInputError, setIssueInputError] = useState('');
  const [couponIssueSummary, setCouponIssueSummary] = useState(null);
  const [stopCause, setStopCause] = useState('');
  const [stopCauseError, setStopCauseError] = useState('');
  const detailLoadSeqRef = useRef(0);
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(events.data);
  const approvalRows = getList(couponCreateApprovalRequests.data);
  const issueTargets = parseUserIdTokens(userIds);
  const selectedCanIssue = canAdminIssueCoupon(detail);
  const selectedIsAdminIndividualCoupon = detail?.type === 'ADMIN_INDIVIDUAL';
  const selectedCanIndividuallyIssue = selectedIsAdminIndividualCoupon && selectedCanIssue && isCouponEventActive(detail);
  const selectedCanRequestStop = Boolean(detail) && isCouponEventActive(detail);
  const rejectedCouponCreateApprovals = approvalRows.filter((request) => {
    const operation = String(request.operation || '').toUpperCase();
    const status = String(request.status || '').toUpperCase();
    return status === 'REJECTED' && operation.includes('COUPON_EVENT_CREATE');
  });
  const filteredCouponEvents = rows.filter((couponEvent) => {
    const keywordMatch = [
      couponEvent.name,
      couponEvent.eventName,
      couponEvent.eventId,
      couponEvent.couponEventId
    ].some((value) => includesKeyword(value, appliedCouponFilters.keyword));
    const typeMatch = !appliedCouponFilters.type || String(couponEvent.type || '').toUpperCase() === appliedCouponFilters.type;
    const statusMatch = !appliedCouponFilters.status || String(couponEvent.status || '').toUpperCase() === appliedCouponFilters.status;
    const startsFromMatch = !appliedCouponFilters.startsFrom || String(couponEvent.startAt || '') >= appliedCouponFilters.startsFrom;
    const endsToMatch = !appliedCouponFilters.endsTo || String(couponEvent.endAt || '') <= appliedCouponFilters.endsTo;

    return keywordMatch && typeMatch && statusMatch && startsFromMatch && endsToMatch;
  });

  const updateCouponFilter = (key, value) => setCouponFilters((current) => ({ ...current, [key]: value }));

  const handleCouponSearch = (event) => {
    event.preventDefault();
    setAppliedCouponFilters({
      keyword: couponFilters.keyword.trim(),
      type: couponFilters.type,
      status: couponFilters.status,
      startsFrom: couponFilters.startsFrom,
      endsTo: couponFilters.endsTo
    });
  };

  const handleCouponReset = () => {
    const defaults = defaultCouponFilters();
    setCouponFilters(defaults);
    setAppliedCouponFilters(defaults);
  };

  const updateForm = (key, value) => {
    setForm((current) => ({ ...current, [key]: value }));
    setFormErrors((current) => {
      const next = { ...current };
      delete next[key];
      return next;
    });
  };

  const updateNumberForm = (key, value) => updateForm(key, formatAdminCouponMoneyInput(value));

  const openCouponCreateForm = () => {
    setForm(defaultCouponForm());
    setFormErrors({});
    setCreateCause('');
    setCreateCauseError('');
    setCreateFormSummary('새 쿠폰 이벤트 승인요청');
    setShowCouponCreateForm(true);
  };

  const openCouponRetryForm = (request) => {
    const requestId = getId(request, ['id', 'requestId']);
    setForm(couponApprovalToForm(request));
    setFormErrors({});
    setCreateCause(request.reason || '');
    setCreateCauseError('');
    setCreateFormSummary(`반려된 쿠폰 생성 요청 #${requestId || '-'} 수정 재요청`);
    setShowCouponCreateForm(true);
  };

  const loadEventDetail = async (eventId, options = {}) => {
    const loadSeq = detailLoadSeqRef.current + 1;
    detailLoadSeqRef.current = loadSeq;
    const isLatestDetailLoad = () => detailLoadSeqRef.current === loadSeq;

    setSelectedEventId(eventId);
    setDetailLoading(true);
    setDetailError('');
    setIssueInputError('');
    setUserIds('');
    setStopCause('');
    setStopCauseError('');

    if (!options.preserveIssueSummary) {
      setCouponIssueSummary(null);
    }

    try {
      const eventDetail = await getAdminCouponEvent(eventId);
      const issuedCoupons = await getAdminCouponEventCoupons(eventId);
      if (!isLatestDetailLoad()) {
        return;
      }

      setDetail(eventDetail);
      setCoupons(getList(issuedCoupons));
    } catch (error) {
      if (!isLatestDetailLoad()) {
        return;
      }

      setDetail(null);
      setCoupons([]);
      setDetailError(error.message || '쿠폰 상세 조회에 실패했습니다.');
    } finally {
      if (isLatestDetailLoad()) {
        setDetailLoading(false);
      }
    }
  };

  const handleCreate = (event) => {
    event.preventDefault();
    const errors = validateAdminCouponForm(form);
    const reason = createCause.trim();
    setFormErrors(errors);
    setCreateCauseError(reason ? '' : '요청 사유를 입력해 주세요.');

    if (Object.keys(errors).length > 0 || !reason) {
      return;
    }

    const payload = buildAdminCouponCreatePayload(form);
    payload.reason = reason;

    run(() => requestCouponEventCreateApproval(payload), (approval) => couponApprovalMessage('쿠폰 이벤트 생성', approval), () => {
      setForm(defaultCouponForm());
      setFormErrors({});
      setCreateCause('');
      setCreateCauseError('');
      setShowCouponCreateForm(false);
      setSelectedEventId(null);
      setDetail(null);
      setCoupons([]);
      events.reload();
      couponCreateApprovalRequests.reload();
    });
  };

  const handleIssue = (event) => {
    event.preventDefault();
    const { validIds, invalidTokens } = parseUserIdTokens(userIds);

    if (!selectedEventId || validIds.length === 0 || invalidTokens.length > 0 || !selectedCanIndividuallyIssue) {
      setIssueInputError('발급 대상 회원 ID를 확인해 주세요.');
      return;
    }

    setIssueInputError('');
    run(
      () => requestCouponEventIndividualIssue(selectedEventId, validIds),
      (summary) => couponApprovalMessage('쿠폰 개별발급', summary),
      (summary) => {
        setUserIds('');
        loadEventDetail(selectedEventId, { preserveIssueSummary: true });
        setCouponIssueSummary(summary);
        events.reload();
      }
    );
  };

  const handleStopRequest = (event) => {
    event.preventDefault();
    const reason = stopCause.trim();
    setStopCauseError(reason ? '' : '중단 사유를 입력해 주세요.');

    if (!selectedCanRequestStop || !reason) {
      return;
    }

    run(() => requestCouponEventStopApproval(selectedEventId, reason), (approval) => couponApprovalMessage('쿠폰 이벤트 중단', approval), () => {
      setStopCause('');
      loadEventDetail(selectedEventId);
      events.reload();
    });
  };

  return (
    <section>
      <AdminPageHeader
        title="쿠폰 관리"
        action={
          <ActionButton icon={Send} type="button" variant="primary" onClick={openCouponCreateForm}>
            이벤트 생성 요청
          </ActionButton>
        }
      />
      <Feedback notice={notice} error={actionError} />
      <Form className="toolbar-panel mb-3 admin-search-panel" onSubmit={handleCouponSearch}>
        <Form.Group className="admin-search-field" controlId="admin-coupon-keyword">
          <Form.Label>쿠폰 이벤트 검색</Form.Label>
          <Form.Control
            type="search"
            value={couponFilters.keyword}
            placeholder="이벤트명 또는 이벤트 ID"
            onChange={(event) => updateCouponFilter('keyword', event.target.value)}
          />
        </Form.Group>
        <div className="admin-filter-options">
          <Form.Group controlId="admin-coupon-type">
            <Form.Label>이벤트 종류</Form.Label>
            <Form.Select value={couponFilters.type} onChange={(event) => updateCouponFilter('type', event.target.value)}>
              <option value="">전체</option>
              {COUPON_TYPES.map((type) => (
                <option value={type} key={type}>
                  {formatAdminCouponType(type)}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="admin-coupon-status">
            <Form.Label>운영 상태</Form.Label>
            <Form.Select value={couponFilters.status} onChange={(event) => updateCouponFilter('status', event.target.value)}>
              <option value="">전체</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="STOP_REQUESTED">STOP_REQUESTED</option>
              <option value="STOPPED">STOPPED</option>
              <option value="ENDED">ENDED</option>
              <option value="PENDING_APPROVAL">PENDING_APPROVAL</option>
              <option value="REJECTED">REJECTED</option>
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="admin-coupon-starts-from">
            <Form.Label>시작일 이후</Form.Label>
            <Form.Control type="date" value={couponFilters.startsFrom} onChange={(event) => updateCouponFilter('startsFrom', event.target.value)} />
          </Form.Group>
          <Form.Group controlId="admin-coupon-ends-to">
            <Form.Label>종료일 이전</Form.Label>
            <Form.Control type="date" value={couponFilters.endsTo} onChange={(event) => updateCouponFilter('endsTo', event.target.value)} />
          </Form.Group>
        </div>
        <div className="admin-filter-actions admin-search-actions">
          <ActionButton icon={Search} type="submit" variant="primary">
            검색
          </ActionButton>
          <Button type="button" variant="outline-secondary" onClick={handleCouponReset}>
            초기화
          </Button>
        </div>
      </Form>
      <div className="admin-coupon-list-grid">
        <div>
          <h2 className="section-title">승인/운영 쿠폰 이벤트 목록</h2>
          {events.loading ? <LoadingState label="쿠폰 이벤트를 불러오는 중" /> : null}
          {events.error ? <ErrorState title="쿠폰 조회 실패" message={events.error.message} onRetry={events.reload} /> : null}
          {!events.loading && !events.error && filteredCouponEvents.length === 0 ? <EmptyState title="조건에 맞는 쿠폰 이벤트가 없습니다" /> : null}
          {!events.loading && !events.error && filteredCouponEvents.length > 0 ? (
            <AdminTable>
              <thead>
                <tr>
                  <th>이벤트</th>
                  <th>종류</th>
                  <th>상태</th>
                  <th>기간</th>
                  <th>수량</th>
                  <th className="text-end">작업</th>
                </tr>
              </thead>
              <tbody>
                {filteredCouponEvents.map((couponEvent) => {
                  const eventId = getCouponEventId(couponEvent);

                  return (
                    <tr key={eventId}>
                      <td>
                        <div className="fw-bold">{couponEvent.name || couponEvent.eventName || '-'}</div>
                        <small className="text-muted">#{eventId || '-'}</small>
                      </td>
                      <td>{formatAdminCouponType(couponEvent.type, couponEvent.typeLabel)}</td>
                      <td>
                        <CouponStatusBadge event={couponEvent} />
                        {couponEvent.canIssue === false ? <Badge bg="warning" className="ms-1">발급 제한</Badge> : null}
                      </td>
                      <td>{formatDateTime(couponEvent.startAt)} - {formatDateTime(couponEvent.endAt)}</td>
                      <td>
                        {formatCouponQuantity(couponEvent.remainingQuantity)} / {formatCouponQuantity(couponEvent.totalQuantity)}
                      </td>
                      <td className="text-end">
                        <ActionButton icon={Search} size="sm" variant="outline-primary" onClick={() => loadEventDetail(eventId)}>
                          상세
                        </ActionButton>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </AdminTable>
          ) : null}
        </div>
        <div className="detail-panel admin-coupon-detail">
          <div className="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
            <h2 className="section-title mb-0">이벤트 상세</h2>
            {detail ? <CouponStatusBadge event={detail} /> : null}
          </div>
          {!selectedEventId ? <EmptyState title="목록에서 쿠폰 이벤트를 선택해 주세요" /> : null}
          {detailLoading ? <LoadingState label="쿠폰 상세와 발급 목록을 불러오는 중" /> : null}
          {detailError ? (
            <Alert variant="danger" className="py-2">
              {detailError}
            </Alert>
          ) : null}
          {selectedEventId && !detailLoading && !detailError ? (
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
                  <AdminCouponMetric label="잔여 수량" value={formatCouponQuantity(detail.remainingQuantity)} />
                  <AdminCouponMetric label="발급률" value={formatIssueRate(detail.issueRate)} />
                  <AdminCouponMetric label="유효 기간" value={`${detail.validDays ?? '-'}일`} />
                </dl>
              ) : null}
              {selectedIsAdminIndividualCoupon && !selectedCanIndividuallyIssue ? (
                <Alert variant="warning" className="py-2">
                  ACTIVE 상태의 관리자 개별발급 이벤트에서만 개별발급 승인요청을 보낼 수 있습니다.
                </Alert>
              ) : null}
              {selectedCanIndividuallyIssue ? (
                <Form className="admin-coupon-issue-form mb-3" onSubmit={handleIssue} noValidate>
                  <Form.Group className="flex-grow-1" controlId="coupon-user-ids">
                    <Form.Label>개별발급 요청 회원 ID</Form.Label>
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
                      {issueInputError || '숫자 회원 ID만 입력해 주세요.'}
                    </Form.Control.Feedback>
                    <Form.Text className="text-muted">입력한 대상은 쿠폰 개별발급 승인요청으로 접수됩니다.</Form.Text>
                  </Form.Group>
                  <ActionButton
                    icon={Send}
                    type="submit"
                    variant="primary"
                    disabled={!selectedEventId || issueTargets.validIds.length === 0 || issueTargets.invalidTokens.length > 0 || !selectedCanIndividuallyIssue}
                  >
                    승인요청
                  </ActionButton>
                </Form>
              ) : null}
              {selectedCanIndividuallyIssue && userIds ? (
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
              <CouponApprovalSummary summary={couponIssueSummary} />
              {selectedCanRequestStop ? (
                <Form className="admin-coupon-stop-form mb-3" onSubmit={handleStopRequest} noValidate>
                  <Form.Group className="flex-grow-1" controlId="coupon-stop-reason">
                    <Form.Label>중단 사유</Form.Label>
                    <Form.Control
                      value={stopCause}
                      placeholder="운영 중단 승인요청 사유"
                      onChange={(event) => {
                        setStopCause(event.target.value);
                        setStopCauseError('');
                      }}
                      isInvalid={Boolean(stopCauseError)}
                    />
                    <Form.Control.Feedback type="invalid">{stopCauseError}</Form.Control.Feedback>
                  </Form.Group>
                  <ActionButton icon={EyeOff} type="submit" variant="outline-danger">
                    이벤트 중단 요청
                  </ActionButton>
                </Form>
              ) : null}
              <h3 className="section-title">발급받아간 사용자/쿠폰 목록</h3>
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
      </div>
      {showCouponCreateForm ? (
        <Form className="detail-panel mt-4 admin-coupon-create-panel" onSubmit={handleCreate} noValidate>
          <div className="d-flex flex-wrap align-items-start justify-content-between gap-2 mb-3">
            <div>
              <h2 className="section-title mb-1">생성 승인요청</h2>
              <p className="text-muted mb-0">{createFormSummary}</p>
            </div>
            <Button type="button" variant="outline-secondary" size="sm" onClick={() => setShowCouponCreateForm(false)}>
              닫기
            </Button>
          </div>
          <Row className="g-2">
            <Col md={4}>
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
            </Col>
            <Col md={8}>
              <Form.Group className="mb-2" controlId="coupon-name">
                <Form.Label>쿠폰명</Form.Label>
                <Form.Control value={form.name} onChange={(event) => updateForm('name', event.target.value)} isInvalid={Boolean(formErrors.name)} />
                <Form.Control.Feedback type="invalid">{formErrors.name}</Form.Control.Feedback>
              </Form.Group>
            </Col>
          </Row>
          <Row className="g-2">
            <Col md={6}>
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
            <Col md={6}>
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
            <Col md={3}>
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
            <Col md={3}>
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
            <Col md={3}>
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
            <Col md={3}>
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
          <Form.Group className="mb-3" controlId="coupon-create-reason">
            <Form.Label>요청 사유</Form.Label>
            <Form.Control
              as="textarea"
              rows={2}
              value={createCause}
              placeholder="승인자가 확인할 생성/재요청 사유"
              onChange={(event) => {
                setCreateCause(event.target.value);
                setCreateCauseError('');
              }}
              isInvalid={Boolean(createCauseError)}
            />
            <Form.Control.Feedback type="invalid">{createCauseError}</Form.Control.Feedback>
          </Form.Group>
          <div className="admin-coupon-request-summary mb-3">
            <h3 className="section-title">요청 요약</h3>
            <dl className="compact-list compact-list-inline">
              <AdminCouponMetric label="유형" value={formatAdminCouponType(form.type)} />
              <AdminCouponMetric label="쿠폰명" value={form.name || '-'} />
              <AdminCouponMetric label="기간" value={`${form.startAt || '-'} - ${form.endAt || '-'}`} />
              <AdminCouponMetric label="수량" value={`${form.totalQuantity || '-'}개`} />
              <AdminCouponMetric label="할인" value={`${form.discountAmount || '-'}원`} />
              <AdminCouponMetric label="최소 주문" value={`${form.minOrderAmount || '-'}원`} />
              <AdminCouponMetric label="유효 기간" value={`${form.validDays || '-'}일`} />
            </dl>
          </div>
          <div className="form-actions">
            <Button type="button" variant="outline-secondary" onClick={() => setShowCouponCreateForm(false)}>
              취소
            </Button>
            <ActionButton icon={CheckCircle2} type="submit" variant="primary">
              승인요청 접수
            </ActionButton>
          </div>
        </Form>
      ) : null}
      {rejectedCouponCreateApprovals.length > 0 ? (
        <div className="detail-panel mt-4 admin-coupon-rejected-approvals">
          <h2 className="section-title">반려된 쿠폰 생성 요청</h2>
          <AdminTable>
            <thead>
              <tr>
                <th>요청</th>
                <th>사유</th>
                <th>상태</th>
                <th className="text-end">작업</th>
              </tr>
            </thead>
            <tbody>
              {rejectedCouponCreateApprovals.map((request) => {
                const requestId = getId(request, ['id', 'requestId']);
                const couponPayload = getCouponApprovalPayload(request);

                return (
                  <tr key={requestId}>
                    <td>
                      <div>{couponPayload.eventName || couponPayload.name || `요청 #${requestId}`}</div>
                      <small className="text-muted">{formatAdminCouponType(couponPayload.eventType || couponPayload.type)}</small>
                    </td>
                    <td>{request.reason || request.memo || '-'}</td>
                    <td>
                      <ApprovalStatusBadge status={request.status} />
                    </td>
                    <td className="text-end">
                      <Button type="button" size="sm" variant="outline-primary" onClick={() => openCouponRetryForm(request)}>
                        수정하여 재요청
                      </Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </AdminTable>
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
    return <EmptyState title="조건에 맞는 승인 요청이 없습니다" />;
  }

  return (
    <AdminTable>
      <thead>
        <tr>
          <th>요청</th>
          <th>요청자</th>
          <th>대상</th>
          <th>요청 권한</th>
          <th>상태</th>
          {showActions ? <th>처리 메모</th> : null}
          {showActions ? <th className="text-end">작업</th> : null}
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
                    aria-label="승인 처리 메모"
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
                    승인
                  </ActionButton>
                  <ActionButton
                    icon={EyeOff}
                    size="sm"
                    variant="outline-danger"
                    disabled={!pending}
                    onClick={() => onReject(requestId, memo)}
                  >
                    거절
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
  const [approvalFilters, setApprovalFilters] = useState({ keyword: '', status: 'PENDING', requestedRole: '' });
  const approvals = useAdminResource(() => getAdminApprovalRequests({ status: approvalFilters.status }), [approvalFilters.status]);
  const [memos, setMemos] = useState({});
  const { notice, actionError, run } = useActionFeedback();
  const rows = getList(approvals.data);
  const filteredApprovals = rows.filter((request) => {
    const keywordMatch = [
      request.id,
      request.requestId,
      request.operation,
      request.reason,
      request.requesterNickname,
      request.requesterEmail,
      request.targetAdminNickname,
      request.targetAdminEmail,
      request.targetAdminId
    ].some((value) => includesKeyword(value, approvalFilters.keyword));
    const roleMatch = !approvalFilters.requestedRole
      || String(request.requestedRole || '').toUpperCase() === approvalFilters.requestedRole;

    return keywordMatch && roleMatch;
  });

  const updateApprovalFilter = (key, value) => {
    setApprovalFilters((current) => ({ ...current, [key]: value }));
  };

  const setMemo = (requestId, value) => setMemos((current) => ({ ...current, [requestId]: value }));
  const removeRequest = (requestId) => {
    approvals.setData((current) => removeFromPayload(current, requestId, 'id'));
    setMemos((current) => ({ ...current, [requestId]: '' }));
  };

  const handleApprove = (requestId, memo) => {
    run(() => approveAdminApprovalRequest(requestId, memo), '승인 요청을 승인했습니다.', () => removeRequest(requestId));
  };

  const handleReject = (requestId, memo) => {
    run(() => rejectAdminApprovalRequest(requestId, memo), '승인 요청을 거절했습니다.', () => removeRequest(requestId));
  };

  return (
    <section>
      <AdminPageHeader title="승인 관리" />
      <div className="toolbar-panel mb-3 admin-search-panel">
        <Form.Group className="admin-search-field" controlId="admin-approval-keyword">
          <Form.Label>승인 요청 검색</Form.Label>
          <Form.Control
            type="search"
            value={approvalFilters.keyword}
            placeholder="요청자, 대상, 사유"
            onChange={(event) => updateApprovalFilter('keyword', event.target.value)}
          />
        </Form.Group>
        <div className="admin-filter-options">
          <Form.Group controlId="admin-approval-status">
            <Form.Label>처리 상태</Form.Label>
            <Form.Select value={approvalFilters.status} onChange={(event) => updateApprovalFilter('status', event.target.value)}>
              {APPROVAL_FILTER_STATUSES.map((item) => (
                <option value={item} key={item}>
                  {item}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="admin-approval-role">
            <Form.Label>요청 권한</Form.Label>
            <Form.Select
              value={approvalFilters.requestedRole}
              onChange={(event) => updateApprovalFilter('requestedRole', event.target.value)}
            >
              <option value="">전체</option>
              {ADMIN_ROLES.map((role) => (
                <option value={role} key={role}>
                  {ADMIN_ROLE_LABELS[role]}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </div>
      </div>
      <Feedback notice={notice} error={actionError} />
      {approvals.loading ? <LoadingState label="승인 요청을 불러오는 중" /> : null}
      {approvals.error ? <ErrorState title="승인 요청 조회 실패" message={approvals.error.message} onRetry={approvals.reload} /> : null}
      {!approvals.loading && !approvals.error ? (
        <ApprovalRequestTable
          rows={filteredApprovals}
          memos={memos}
          setMemo={setMemo}
          onApprove={handleApprove}
          onReject={handleReject}
          showActions={approvalFilters.status === 'PENDING'}
        />
      ) : null}
    </section>
  );
}

export function AdminMyApprovalRequestsPage() {
  const approvals = useAdminResource(getMyAdminApprovalRequests, []);
  const [myApprovalFilters, setMyApprovalFilters] = useState({ keyword: '', status: '' });
  const rows = getList(approvals.data);
  const filteredMyApprovals = rows.filter((request) => {
    const keywordMatch = [
      request.id,
      request.requestId,
      request.operation,
      request.reason,
      request.targetAdminNickname,
      request.targetAdminEmail,
      request.targetAdminId,
      request.requestedRole
    ].some((value) => includesKeyword(value, myApprovalFilters.keyword));
    const statusMatch = !myApprovalFilters.status || String(request.status || '').toUpperCase() === myApprovalFilters.status;

    return keywordMatch && statusMatch;
  });

  const updateMyApprovalFilter = (key, value) => setMyApprovalFilters((current) => ({ ...current, [key]: value }));

  const handleMyApprovalSearch = (event) => {
    event.preventDefault();
  };

  const handleMyApprovalReset = () => {
    setMyApprovalFilters({ keyword: '', status: '' });
  };

  return (
    <section>
      <AdminPageHeader title="내 승인 요청" />
      <Form className="toolbar-panel mb-3 admin-search-panel" onSubmit={handleMyApprovalSearch}>
        <Form.Group className="admin-search-field" controlId="my-approval-keyword">
          <Form.Label>내 요청 검색</Form.Label>
          <Form.Control
            type="search"
            value={myApprovalFilters.keyword}
            placeholder="대상, 사유, 요청 권한"
            onChange={(event) => updateMyApprovalFilter('keyword', event.target.value)}
          />
        </Form.Group>
        <div className="admin-filter-options">
          <Form.Group controlId="my-approval-status">
            <Form.Label>처리 상태</Form.Label>
            <Form.Select value={myApprovalFilters.status} onChange={(event) => updateMyApprovalFilter('status', event.target.value)}>
              <option value="">전체</option>
              {APPROVAL_FILTER_STATUSES.map((item) => (
                <option value={item} key={item}>
                  {item}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </div>
        <div className="admin-filter-actions admin-search-actions">
          <ActionButton icon={Search} type="submit" variant="primary">
            검색
          </ActionButton>
          <Button type="button" variant="outline-secondary" onClick={handleMyApprovalReset}>
            초기화
          </Button>
        </div>
      </Form>
      {approvals.loading ? <LoadingState label="내 승인 요청을 불러오는 중" /> : null}
      {approvals.error ? <ErrorState title="내 승인 요청 조회 실패" message={approvals.error.message} onRetry={approvals.reload} /> : null}
      {!approvals.loading && !approvals.error ? <ApprovalRequestTable rows={filteredMyApprovals} /> : null}
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
