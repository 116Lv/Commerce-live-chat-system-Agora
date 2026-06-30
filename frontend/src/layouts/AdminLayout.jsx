import { useEffect, useState } from 'react';
import { NavDropdown } from 'react-bootstrap';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  CreditCard,
  ClipboardCheck,
  Flag,
  LayoutDashboard,
  LogOut,
  Shield,
  ShoppingBag,
  Tags,
  UserCog,
  UsersRound
} from 'lucide-react';
import { getAdminMe } from '../api/adminApi.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { formatAdminRole } from '../pages/adminPageUtils.js';

const adminLinks = [
  { to: '/admin', label: '대시보드', icon: LayoutDashboard, end: true },
  { to: '/admin/products', label: '상품 관리', icon: ShoppingBag, requiredPermissions: ['PRODUCT_MANAGE'] },
  { to: '/admin/users', label: '회원 관리', icon: UsersRound, requiredPermissions: ['USER_MANAGE'] },
  { to: '/admin/reports', label: '신고 관리', icon: Flag, requiredPermissions: ['REPORT_MANAGE'] },
  { to: '/admin/payments', label: '결제 관리', icon: CreditCard, requiredPermissions: ['PAYMENT_MANAGE'] },
  { to: '/admin/coupons', label: '쿠폰 관리', icon: Tags, requiredPermissions: ['COUPON_MANAGE'] },
  { to: '/admin/accounts', label: '관리자 계정', icon: UserCog, requiredPermissions: ['ADMIN_ACCOUNT_MANAGE'] },
  { to: '/admin/approval-requests', label: '승인 관리', icon: ClipboardCheck, requiredPermissions: ['APPROVAL_MANAGE'] },
  { to: '/admin/my-approval-requests', label: '내 승인 요청', icon: ClipboardCheck }
];

const hasPermission = (permissions, permission) => permissions.includes(permission);
const canSeeLink = (permissions, link) => (
  !link.requiredPermissions || link.requiredPermissions.some((permission) => hasPermission(permissions, permission))
);

export default function AdminLayout() {
  const navigate = useNavigate();
  const { logoutAdmin } = useAuth();
  const [admin, setAdmin] = useState(null);

  useEffect(() => {
    let ignore = false;

    getAdminMe()
      .then((data) => {
        if (!ignore) {
          setAdmin(data);
        }
      })
      .catch(() => {
        if (!ignore) {
          setAdmin(null);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  const role = admin?.role || '';
  const permissions = Array.isArray(admin?.permissions) ? admin.permissions : [];
  const visibleLinks = adminLinks.filter((link) => canSeeLink(permissions, link));
  const canManageAccounts = hasPermission(permissions, 'ADMIN_ACCOUNT_MANAGE');
  const avatarText = String(admin?.nickname || admin?.email || 'A').slice(0, 1).toUpperCase();

  const handleLogout = async () => {
    await logoutAdmin();
    navigate('/admin/login', { replace: true });
  };

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar" aria-label="관리자 메뉴">
        <div className="admin-brand">
          <Shield size={20} aria-hidden="true" />
          <span>Agora 관리자</span>
        </div>
        <nav className="admin-nav">
          {visibleLinks.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) => `admin-nav-link${isActive ? ' active' : ''}`}
            >
              <Icon size={18} aria-hidden="true" />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="admin-content">
        <header className="admin-topbar">
          <div>
            <strong>{formatAdminRole(role)}</strong>
            <span>{admin?.email || '관리자 정보를 불러오는 중'}</span>
          </div>
          <NavDropdown
            align="end"
            className="admin-account-dropdown"
            title={
              <span className="admin-avatar" aria-label="관리자 계정 메뉴">
                {avatarText}
              </span>
            }
          >
            <NavDropdown.Header>{admin?.nickname || admin?.email || '관리자'}</NavDropdown.Header>
            {canManageAccounts ? (
              <NavDropdown.Item as={NavLink} to="/admin/accounts">
                관리자 계정
              </NavDropdown.Item>
            ) : null}
            {canManageAccounts ? <NavDropdown.Divider /> : null}
            <NavDropdown.Item onClick={handleLogout}>
              <LogOut size={16} aria-hidden="true" />
              로그아웃
            </NavDropdown.Item>
          </NavDropdown>
        </header>
        <main className="admin-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
