import { NavLink, Outlet } from 'react-router-dom';
import { CreditCard, Flag, LayoutDashboard, ShoppingBag, Tags, UsersRound } from 'lucide-react';

const adminLinks = [
  { to: '/admin', label: '대시보드', icon: LayoutDashboard, end: true },
  { to: '/admin/products', label: '상품', icon: ShoppingBag },
  { to: '/admin/users', label: '회원', icon: UsersRound },
  { to: '/admin/reports', label: '신고', icon: Flag },
  { to: '/admin/payments', label: '결제', icon: CreditCard },
  { to: '/admin/coupons', label: '쿠폰', icon: Tags }
];

export default function AdminLayout() {
  return (
    <div className="admin-shell">
      <aside className="admin-sidebar" aria-label="관리자 메뉴">
        <div className="admin-brand">Agora 관리자</div>
        <nav className="admin-nav">
          {adminLinks.map(({ to, label, icon: Icon, end }) => (
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
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
